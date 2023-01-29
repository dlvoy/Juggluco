/*      This file is part of Juggluco, an Android app to receive and display         */
/*      glucose values from Freestyle Libre 2 and 3 sensors.                         */
/*                                                                                   */
/*      Copyright (C) 2021 Jaap Korthals Altes <jaapkorthalsaltes@gmail.com>         */
/*                                                                                   */
/*      Juggluco is free software: you can redistribute it and/or modify             */
/*      it under the terms of the GNU General Public License as published            */
/*      by the Free Software Foundation, either version 3 of the License, or         */
/*      (at your option) any later version.                                          */
/*                                                                                   */
/*      Juggluco is distributed in the hope that it will be useful, but              */
/*      WITHOUT ANY WARRANTY; without even the implied warranty of                   */
/*      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         */
/*      See the GNU General Public License for more details.                         */
/*                                                                                   */
/*      You should have received a copy of the GNU General Public License            */
/*      along with Juggluco. If not, see <https://www.gnu.org/licenses/>.            */
/*                                                                                   */
/*      Fri Jan 27 15:32:11 CET 2023                                                 */


package tk.glucodata;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.security.ProviderInstaller;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import java.util.UUID;

import androidx.annotation.Keep;
import tk.glucodata.settings.LibreNumbers;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static java.net.HttpURLConnection.HTTP_OK;
import static tk.glucodata.Backup.getedit;
import static tk.glucodata.Log.stackline;
import static tk.glucodata.Natives.clearlibreview;
import static tk.glucodata.Natives.getlibreDeviceID;
import static tk.glucodata.Natives.getlibrebaseurl;
import static tk.glucodata.Natives.getlibreemail;
import static tk.glucodata.Natives.getlibrepass;
import static tk.glucodata.Natives.getnewYuApiKey;
import static tk.glucodata.Natives.getuselibreview;
import static tk.glucodata.Natives.savelibrerubbish;
import static tk.glucodata.Natives.setlibreAccountID;
import static tk.glucodata.Natives.setlibreDeviceID;
import static tk.glucodata.Natives.setlibrebaseurl;
import static tk.glucodata.Natives.setlibreemail;
import static tk.glucodata.Natives.setlibrepass;
import static tk.glucodata.Natives.setnewYuApiKey;
import static tk.glucodata.Natives.setuselibreview;
import static tk.glucodata.Natives.wakelibreview;
import static tk.glucodata.RingTones.EnableControls;
import static tk.glucodata.bluediag.datestr;
import static tk.glucodata.help.help;
import static tk.glucodata.settings.Settings.editoptions;
import static tk.glucodata.settings.Settings.removeContentView;
import static tk.glucodata.util.getbutton;
import static tk.glucodata.util.getcheckbox;
import static tk.glucodata.util.getlabel;

public class Libreview  {
	private static final String LOG_ID="Libreview";
/*
Doen:
Repeat with setDevice True
login and pass variable
Culture
get DeviceId
get urlConnection.setRequestProperty("x-api-key", "9D8JYWU1Ja9ai3N7HKNzC5zv31Wlmp368Sf8Voar");
from previous input
"newYuGateway": "FSLibreLink3.Android",
"newYuApiKey": "9D8JYWU1Ja9ai3N7HKNzC5zv31Wlmp368Sf8Voar",

"newYuGateway": "FSLibreLink.Android",
"newYuApiKey": "pwbOvCxvDE7qPIECSX7dK6uZHRGvh3q815fxep5r",

"newYuUrl": "https://api-eu.libreview.io/lsl"....................................
"newYuUrl": "https://lsl1.newyu.net",
https://api-eu.libreview.io/lsl/api/nisperson/getauthentication
https://lsl1.newyu.net/api/nisperson/getauthentication

Data:
usertoken
accountid

for every sensor:
   didput sensor
   lasthistid
   notsend
*/
//static String gateway="FSLibreLink.Android";
//static String usertoken=null;
//static String accountid=null;
private static String getputtext(String sensorid,String usertoken,String gateway) {
 return "{\"DomainData\":\"{\\\"activeSensor\\\":\\\""+sensorid+"\\\"}\",\"UserToken\":\""+usertoken +"\",\"Domain\":\"Libreview\",\"GatewayType\":\""+gateway+"\"}";
 }


private static String getDeviceID(boolean libre3) {
	String id=getlibreDeviceID(libre3);
	if(id==null||id.length()<36)  {
		id=UUID.randomUUID().toString();
		setlibreDeviceID(libre3,id);
		}
	return id;
	}
	/*
private static int getalldata(HttpURLConnection urlConnection,byte[] buf) throws IOException {
	try(InputStream in = urlConnection.getInputStream()) {
		int off=0,len;
		while((len=in.read(buf,off,alllen-off))>0) {
			off+=len;
			}
		return off;
		}
	finally {
		urlConnection.disconnect();
		}

	}
static JSONObject  readJSONObject(HttpURLConnection urlConnection)  throws IOException, JSONException {
	byte[] buf=new byte[10*4096];
	int len=getalldata(urlConnection,buf);
	String ant=new String(getSlice(buf, 0, len));
	Log.i(LOG_ID,"readJSONObject len="len+" "+ant);
 	return new JSONObject(ant);
	}*/

static String getstring(HttpURLConnection con)  throws IOException{
	try(BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
		StringBuffer response = new StringBuffer();
		String inputLine;
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
			}
		return response.toString();
		}
	finally {
		con.disconnect();
		}
	}
static JSONObject  readJSONObject(HttpURLConnection urlConnection)  throws IOException, JSONException {
	String ant=getstring(urlConnection);
	Log.format("%s: readJSONObject len=%d %s",LOG_ID,ant.length(),ant);
 	return new JSONObject(ant);
	}
final private static String success="Success".intern();
final private static String nothing="Tried nothing".intern();

private static String librestatus=nothing;
/*
@Keep
static boolean putsensor(String sensorid) {
	boolean libre3=false;
	if(librestatus==nothing||librestatus==success)
		librestatus=datestr(System.currentTimeMillis())+" start putsensor";
	try {
		final String gateway=getlibregateway(libre3);
		final String baseurl=getlibrebaseurl(libre3);
		URL url = new URL(baseurl+"/api/nisperson");
		HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
		urlConnection.setDoOutput(true);
	       urlConnection.setRequestMethod("PUT");
		urlConnection.setRequestProperty("Content-Type","application/json; charset=UTF-8");
		String usertoken=Natives.getlibreUserToken(libre3);
		String text=getputtext(sensorid, usertoken,gateway);
		byte[] textbytes=text.getBytes();
		urlConnection.setRequestProperty( "Content-Length", Integer.toString( textbytes.length ));
		OutputStream outputPost = new BufferedOutputStream(urlConnection.getOutputStream());
		outputPost.write(textbytes);
		outputPost.flush();
		outputPost.close();
 		JSONObject object = readJSONObject(urlConnection);
		final int status=object.getInt("status");
		if(status!=0) {
			String reason=object.getString("reason");
			librestatus="putsensor: status="+status+reason==null?"":(" reason="+reason);
			}
		return status==0;
		}  
	catch(Throwable th) {
		librestatus="putsensor "+sensorid+":\n"+ stackline(th);
		Log.e(LOG_ID,librestatus);
		return false;
		}
	}
	*/
@Keep
static boolean putsensor(boolean libre3,byte[] textbytes) {
	if(librestatus==nothing||librestatus==success)
		librestatus=datestr(System.currentTimeMillis())+" start putsensor";
	try {
		final String gateway=getlibregateway(libre3);
		final String baseurl=getlibrebaseurl(libre3);
		URL url = new URL(baseurl+"/api/nisperson");
		HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
		urlConnection.setDoOutput(true);
	       urlConnection.setRequestMethod("PUT");
		urlConnection.setRequestProperty("Content-Type","application/json; charset=UTF-8");
		String usertoken=Natives.getlibreUserToken(libre3);
		urlConnection.setRequestProperty( "Content-Length", Integer.toString( textbytes.length ));
		OutputStream outputPost = new BufferedOutputStream(urlConnection.getOutputStream());
		outputPost.write(textbytes);
		outputPost.flush();
		outputPost.close();
 		JSONObject object = readJSONObject(urlConnection);
		final int status=object.getInt("status");
		if(status!=0) {
			String reason=object.getString("reason");
			librestatus="putsensor: status="+status+reason==null?"":(" reason="+reason);
			}
		return status==0;
		}  
	catch(Throwable th) {
		librestatus="putsensor "+ stackline(th);
		Log.e(LOG_ID,librestatus);
		return false;
		}
	}
//{"status":20,"reason":"wrongDeviceForUser"}
static String getlibregateway(boolean libre3) {
	if(libre3)
		return "FSLibreLink3.Android";
	return "FSLibreLink.Android";
	}
	/*
static String getlibreDeviceID() {
	return "b76f19a9-d4a0-4d67-8999-56b89b0968ee";
	}

public static  String getlibreemail( ) {
	String login="jkaltes@hotmail.nl";
	return login;
	}

public static  String getlibrepass( ) {
	String password="%Olp4VBlo6";
	return password;
	}
public static String getnewYuApiKey() {
	return "pwbOvCxvDE7qPIECSX7dK6uZHRGvh3q815fxep5r";
	} */
static private boolean gettermversion(String lang) {
	try {
		if(termsofuseversionurl==null) {
				Log.d(LOG_ID, "termsofuseversionurl==null");
				return false;
			}
		String rep=termsofuseversionurl.replace("<locale>",lang);

		URL url = new URL(rep);
		HttpURLConnection  urlConnection = (HttpURLConnection) url.openConnection();
		urlConnection.setRequestMethod("GET");
		final int code=urlConnection.getResponseCode();
		if(code==HTTP_OK) {
			Log.i(LOG_ID,"gettermversion  success");
			return true;
			}
		else {
			Log.e(LOG_ID,"gettermversion code="+code);
			return false;
			}

		}
	catch(Throwable th) {
		Log.stack(LOG_ID,"gettermversion",th);
		return false;
		}
	}

static boolean postgetauth(boolean libre3) {
	String gateway=getlibregateway(libre3);
	String one=getDeviceID(libre3);

	String password=getlibrepass();

	String login=getlibreemail();
	Log.i(LOG_ID,"postgetauth "+login+" "+password);

	var loc= Locale.getDefault();
	String language=loc.getLanguage()+'-'+loc.getCountry();
	String culture=language;
	String setdevice="false";
	while(true) {
		try {
		final String baseurl=getlibrebaseurl(libre3);
		URL url = new URL(baseurl+"/api/nisperson/getauthentication");
		HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
		urlConnection.setRequestMethod("POST");
		urlConnection.setDoOutput(true);
		urlConnection.setRequestProperty("Content-Type", "application/json");
		if(libre3) {
			urlConnection.setRequestProperty("Platform","Android");
			urlConnection.setRequestProperty("Version","3.3.0");
			urlConnection.setRequestProperty("Abbott-ADC-App-Platform","Android/"+((Object) Build.VERSION.RELEASE) +"/FSL3/3.3.0.9092");
			urlConnection.setRequestProperty("Accept-Language",language);
			final String newYuApiKey=getnewYuApiKey(libre3);
			urlConnection.setRequestProperty("x-api-key", newYuApiKey);
			urlConnection.setRequestProperty("x-newyu-token",""); 
			}
		String getauthtext="{\n"+
		"  \"Culture\": \""+culture+"\",\n"+
		"  \"DeviceId\": \""+one+"\",\n"+
		"  \"Password\": \""+password+"\",\n"+
		"  \"SetDevice\": "+setdevice+",\n"+
		"  \"UserName\": \""+login+"\",\n"+
		"  \"Domain\": \"Libreview\",\n"+
		"  \"GatewayType\": \""+ gateway+ "\"\n"+
		"}\n";
		byte[] textbytes=getauthtext.getBytes();
		Log.i(LOG_ID,"postauth: "+getauthtext);
	       urlConnection.setRequestProperty( "Content-Length", Integer.toString( textbytes.length ));

	//	Log.i(LOG_ID,getauthtext);
		OutputStream outputPost = new BufferedOutputStream(urlConnection.getOutputStream());
		outputPost.write(textbytes);
		outputPost.flush();
		outputPost.close();
		final int code=urlConnection.getResponseCode();
		
		Log.i(LOG_ID,"ResponseCode="+code);
		if(code==HTTP_OK) {
			JSONObject object = readJSONObject(urlConnection);
			int status=object.getInt("status");
			if(status!=0) {
				String reason=object.getString("reason");
				String poststatus="postgetauth: status="+status+" reason="+reason;
				Log.e(LOG_ID,poststatus);
				if(status==20) {
					if("wrongDeviceForUser".equals(reason)) {
						setdevice="true";
						continue;	
						}
					}
				librestatus=poststatus;
				return false;
				}
			Log.i(LOG_ID,"getauth Success");
			JSONObject result=object.getJSONObject("result");
			String usertoken=result.getString("UserToken");
			Natives.setlibreUserToken(libre3,usertoken);
			String accountid=result.getString("AccountId");
			setlibreAccountID(accountid);
			librestatus="Received AccountID";
			if(libre3) {//TODO enkel als send to libreview aanstaat?
				String DateOfBirth=result.getString("DateOfBirth");
				int dat=Integer.parseInt(DateOfBirth);
				String FirstName=result.getString("FirstName");
				String LastName=result.getString("LastName");
				String GuardianLastName=result.getString("GuardianLastName");
				String GuardianFirstName=result.getString("GuardianFirstName");
				savelibrerubbish(FirstName,LastName,dat,GuardianFirstName,GuardianLastName);
				String UiLanguage=result.getString("UiLanguage");
				gettermversion(UiLanguage);
				}
			return true;
			}
		else {
			librestatus="postgetauth: urlConnection.getResponseCode()="+code;
			return false;
			}
		 }
		catch(Throwable th) {
			librestatus="postgetauth:\t"+ stackline(th);

			Log.e(LOG_ID,librestatus);
			return false;
			}
	}
 }
 /*
@Keep
static boolean postmeasurements(byte[] measurementdata) {
	return postmeasurements(false, measurementdata);
	}*/

static String posttime=null;
@Keep
static boolean postmeasurements(boolean libre3,byte[] measurementdata) {
	String nowstr=datestr(System.currentTimeMillis());
	if(librestatus==nothing||librestatus==success)
		librestatus=nowstr+" start posting";
	try {
	for(int i=0;i<3;i++) {
		final String baseurl=getlibrebaseurl(libre3);
		URL url = new URL(baseurl+"/api/measurements");
		HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
		urlConnection.setConnectTimeout(10000);
		urlConnection.setReadTimeout(60000);
		urlConnection.setRequestMethod("POST");
		urlConnection.setDoOutput(true);
		urlConnection.setRequestProperty("Content-Type", "application/json");
	       urlConnection.setRequestProperty( "Content-Length", Integer.toString( measurementdata.length ));
		if(libre3) {
			urlConnection.setRequestProperty("Platform","Android");
			urlConnection.setRequestProperty("Version","3.3.0");
			urlConnection.setRequestProperty("Abbott-ADC-App-Platform","Android/"+((Object) Build.VERSION.RELEASE) +"/FSL3/3.3.0.9092");
			var loc= Locale.getDefault();
			String language=loc.getLanguage()+'-'+loc.getCountry();
			urlConnection.setRequestProperty("Accept-Language",language);
			final String newYuApiKey=getnewYuApiKey(libre3);
			urlConnection.setRequestProperty("x-api-key", newYuApiKey);
			final String usertoken=Natives.getlibreUserToken(libre3);
			urlConnection.setRequestProperty("x-newyu-token",usertoken); 
			}
		OutputStream outputPost = new BufferedOutputStream(urlConnection.getOutputStream());
		outputPost.write(measurementdata);
		outputPost.flush();
		outputPost.close();
		final int code=urlConnection.getResponseCode();
		if(code==HTTP_OK) {
			JSONObject object = readJSONObject(urlConnection);
			int status=object.getInt("status");
			if(status!=0) {
				Log.e(LOG_ID,"Post with status "+status);
				String reason=object.getString("reason");
				if(status==20) {
					if("wrongDeviceInToken".equals(reason)) {
						switch(i) {
							case 0:{
								if(!postgetauth(libre3))
									return false;
								};break;
							case 1: {
								if(!libreconfig(libre3,false))
									return false;
								};break;

							}
//						return postmeasurements(libre3,measurementdata);
						continue;
						}
					}
				librestatus="postmeasurements status="+code+" reason="+reason;
				return false;
				}
			posttime=nowstr;
			librestatus=success;
			return true;
			}
		else {
			librestatus="postmeasurements ResponseCode="+code;
			Log.i(LOG_ID,librestatus);
			return false;
			}
			}
		return false;
		 }
	catch(Throwable th) {
		final String posterror="postmeasurements\n"+stackline(th);
		librestatus=posterror;
		Log.e(LOG_ID,posterror);
		return false;
		}
 }
 /*TODO: where:
		  try {
			  ProviderInstaller.installIfNeeded(Applic.app);
		  }
		catch(Throwable th) {
			librestatus= "ProviderInstaller.installIfNeeded: \n"+stackline(th);
			 Log.e(LOG_ID,librestatus);
			  }
*/
//	https://fsll3.freestyleserver.com/Payloads/Mobile/FFSLibre3/Android/Assets/3.3.0%2FDE.json
private static String termsofuseversionurl=null;
private static final String libre3start="https://fsll3.freestyleserver.com/Payloads/Mobile/FSLibre3/Android/Assets/3.3.0/DE.json";
private static String  libre3getconfigURL() {
	try {

		URL url = new URL(libre3start);
		HttpURLConnection  urlConnection = (HttpURLConnection) url.openConnection();
		urlConnection.setRequestMethod("GET");
		final int code=urlConnection.getResponseCode();
		if(code==HTTP_OK) {
			JSONObject object =  readJSONObject(urlConnection) ;
			final String conurl=object.getString( "Configuration");
			try {
			   termsofuseversionurl=object.getString( "TermsOfUseVersion");
			 }
			 catch(Throwable th) {
				Log.stack(LOG_ID,"libre3getconfigURL",th);
				}
			finally {
				return conurl;
				} 
			}
		else {
			Log.e(LOG_ID,"libre3getconfigURL code="+code);
			return null;
			}

		}
	catch(Throwable th) {
		Log.stack(LOG_ID,"libre3getconfigURL",th);
		return null;
		}
	}
	/*
public static void testlibre3() { 
	String url=libre3getconfigURL();
	Log.i(LOG_ID,"libre3getconfigURL()="+(url==null?"null":url));
	}*/
//https://fsll.freestyleserver.com/Payloads/Mobile/Android/FSLibreLink/Config/FreeStyleLibreLink_Android_2.3_DE_config.json
@Keep
public static boolean libreconfig(boolean libre3,boolean restart){
	if(restart||librestatus==nothing||librestatus==success)
		librestatus=datestr(System.currentTimeMillis())+" libreconfig";
	Log.i(LOG_ID,librestatus);
	  try {
		  ProviderInstaller.installIfNeeded(Applic.app);
	  }
	catch(Throwable th) {
		librestatus= "ProviderInstaller.installIfNeeded: \n"+stackline(th);
		 Log.e(LOG_ID,librestatus);
		  }

//	final String libre23url= "https://www.google.com";
	final String libre23url= "https://fsll.freestyleserver.com/Payloads/Mobile/Android/FSLibreLink/Config/FreeStyleLibreLink_Android_2.3_DE_config.json";
//final String libre33url="https://fsll3.freestyleserver.com/Payloads/Mobile/FSLibre3/Android/Config/FSLibre3_Android_3.3_DE_config_production.json";

	final String libre33url=(libre3)?libre3getconfigURL():null;
	try {
		URL url = new URL(libre3?libre33url:libre23url);
		if(url==null)
			return false;
		HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
	/*	
		  if(android.os.Build.VERSION.SDK_INT < 20) {
			  TrustManager[] trustAllCerts = new TrustManager[]{
			    new X509TrustManager() {
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				    return null;
				}
				public void checkClientTrusted(
				    java.security.cert.X509Certificate[] certs, String authType) {
				}
				public void checkServerTrusted(
				    java.security.cert.X509Certificate[] certs, String authType) {
				}
			    }
			};
		  	try {
				  SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
	//			  SSLContext sslContext = SSLContext.getInstance("TLS");
			//	  sslContext.init(null, null, null);
				  sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
				  urlConnection.setSSLSocketFactory(sslContext.getSocketFactory());
				} catch (Exception e) {
					librestatus="libreconfig:\n"+stackline(e);
					Log.e(LOG_ID,librestatus);
					}
			  } */
		urlConnection.setRequestMethod("GET");

		final int code=urlConnection.getResponseCode();
		if(code==HTTP_OK) {
			JSONObject object =  readJSONObject(urlConnection) ;
			final String baseurl=object.getString( "newYuUrl");
			setlibrebaseurl(libre3,baseurl);
			setnewYuApiKey(libre3,object.getString("newYuApiKey"));
			return postgetauth(libre3);
			}
		else {
			librestatus="urlConnection.getResponseCode()="+code;
			Log.e(LOG_ID,librestatus);
			return false;
			}

		}
	catch(Throwable th) {
		librestatus="libreconfig:\n"+stackline(th);

		Log.e(LOG_ID,librestatus);
		return false;
		}
	}

private static	void askclearlibreview(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.resendquestion).
	 setMessage(R.string.resendmessage).
           setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
			clearlibreview() ;

                    }
                }) .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        }).show();
	}

private static void		confirmGetAccountID(Activity context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.getaccountidquestion).
	 setMessage(R.string.getaccountidmessage).
           setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
			Natives.askServerforAccountID();
                    }
                }) .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        }).show();
	}
//		Natives.askServerforAccountID();
public static void  config(MainActivity act, View settingsview,CheckBox sendto,boolean[] donothing) {
	EnableControls(settingsview,false);
	var emaillabel=getlabel(act,"E-Mail:");
	var email=getedit(act, getlibreemail());
        email.setMinEms(12);

	var passlabel=getlabel(act,"Password:");
	var      editpass= new EditText(act);
        editpass.setImeOptions(editoptions);
        editpass.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        editpass.setTransformationMethod(new PasswordTransformationMethod());
        editpass.setMinEms(12);
	String passwas=getlibrepass();
	if(passwas!=null) {
		editpass.setText(passwas);
		}
	var send=getbutton(act,act.getString(R.string.sendnow));
	var ok=getbutton(act,R.string.ok);
	var cancel=getbutton(act,R.string.cancel);
	var help=getbutton(act,R.string.helpname);
	help.setOnClickListener(v-> help(R.string.libreview,act));
	boolean usedlibre= getuselibreview();
	var sendtolibreview=getcheckbox(act,R.string.uselibreview,usedlibre);
	var numbers=getcheckbox(act,R.string.sendamounts,Natives.getSendNumbers());
	boolean[] nochangeamounts={false};
	numbers.setOnCheckedChangeListener( (buttonView,  isChecked) -> {
		if(!nochangeamounts[0]) {
			nochangeamounts[0]=true;
			numbers.setChecked(!isChecked);
			LibreNumbers.mklayout(act,numbers,nochangeamounts);
			}
		});
	sendtolibreview.setOnCheckedChangeListener( (buttonView,  isChecked) -> {
		numbers.setVisibility(isChecked?VISIBLE:INVISIBLE);;
		});
	var clear=getbutton(act,"Resend data");	
	  var statusview=getlabel(act,librestatus==success?(posttime+": "+librestatus):librestatus);
	  int statuspad=  (int)tk.glucodata.GlucoseCurve.metrics.density*7;
	statusview.setPadding(statuspad,statuspad,statuspad,statuspad);
	clear.setOnClickListener(v->  {
			askclearlibreview(act);
			});
	
//	  clear.setPadding(0,0,0,pad*5);
	long accountidnum=Natives.getlibreAccountIDnumber();
	var accountid=getlabel(act, String.valueOf(accountidnum));
	var getaccountid=getbutton(act,"Get Account ID");
var space=getlabel(act,"        ");
	final Layout layout=new Layout(act, (lay, w, h) -> {
		var height=GlucoseCurve.getheight();
		var width=GlucoseCurve.getwidth();
                        if(w>=width||h>=height) {
                                lay.setX(0);
                                lay.setY(0);
                                }
                        else {
                                lay.setX((width-w)/2); lay.setY(0);
                                };
                        return new int[] {w,h};}, new View[]{emaillabel,email},new View[]{passlabel,editpass},new View[]{clear,accountid,getaccountid},new View[]{statusview},new View[]{sendtolibreview,numbers},new View[]{send,help,cancel,ok});
	if(usedlibre) {
		send.setOnClickListener(v-> wakelibreview(0));
		}
	else  {
		numbers.setVisibility(INVISIBLE);;
		send.setVisibility(INVISIBLE);
		}

     

	Runnable closerun=()-> {
		layout.setVisibility(GONE);
		removeContentView(layout);
		EnableControls(settingsview,true);
		sendto.setChecked(usedlibre);
		donothing[0]=false;
		};
	act.setonback(closerun);
	cancel.setOnClickListener(v->  {
			act.poponback();
			closerun.run();
			});
 	Predicate<Boolean> getgegs= use -> {
			String emailstr=email.getText().toString();
			String passstr=editpass.getText().toString();
			if(use) {
				if(emailstr.length()<3) {
					Toast.makeText(act, "E-Mail address too short "+emailstr, Toast.LENGTH_SHORT).show();
					return false;
					}
				if(emailstr.length()>255) {
					Toast.makeText(act, "E-Mail address too long "+emailstr, Toast.LENGTH_SHORT).show();
					return false;
					}
				if(passstr.length()<3) {
					Toast.makeText(act, "Password should be at least 8 characters long "+passstr, Toast.LENGTH_SHORT).show();
					return false;
					}
				if(passstr.length()>36) {
					Toast.makeText(act, "Maximal password length is 36 characters "+passstr, Toast.LENGTH_SHORT).show();
					return false;
					}
				}	
			setlibreemail(emailstr);
			setlibrepass(passstr);
			if((emailstr.length()==0&&passstr.length()==0)) {
				clearlibreview();
				}
			return true;
		};
	ok.setOnClickListener(v-> {
			boolean turnonlibre=sendtolibreview.isChecked();
			if(!getgegs.test(turnonlibre))
				return;
			setuselibreview(turnonlibre);
				
			act.poponback();
			layout.setVisibility(GONE);
			removeContentView(layout);
			EnableControls(settingsview,true);
			sendto.setChecked(turnonlibre);
			donothing[0]=false;
			});
	getaccountid.setOnClickListener(v->  {
		if(!getgegs.test(true))
				return;
		confirmGetAccountID(act);
//		Natives.askServerforAccountID();
		});
	      layout.setBackgroundResource(R.drawable.dialogbackground);
	      int pad= (int)tk.glucodata.GlucoseCurve.metrics.density*7;
		  layout.setPadding(pad,pad,pad,pad);
		act.addContentView(layout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT));
	
	}



}




