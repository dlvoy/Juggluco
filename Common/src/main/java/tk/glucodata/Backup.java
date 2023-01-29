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
/*      Fri Jan 27 15:31:05 CET 2023                                                 */


package tk.glucodata;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Arrays;
import java.util.Enumeration;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static tk.glucodata.Applic.backgroundcolor;
import static tk.glucodata.Applic.isWearable;
import static tk.glucodata.Natives.getBlueMessage;
import static tk.glucodata.Natives.getWifi;
import static tk.glucodata.Natives.isWearOS;
import static tk.glucodata.UseWifi.usewifi;
import static tk.glucodata.help.hidekeyboard;
import static tk.glucodata.Applic.isRelease;
import static tk.glucodata.settings.Settings.removeContentView;
import static tk.glucodata.util.getbutton;
import static tk.glucodata.util.getcheckbox;
import static tk.glucodata.util.getlabel;
import static tk.glucodata.settings.Settings.editoptions;

//import org.w3c.dom.Text;

class Backup {
static final private String LOG_ID="backup";
static class changer implements TextWatcher {
	View view;
	changer(View v) {
		view=v;
		}
	 public void	afterTextChanged(Editable s) {}

	 public void	beforeTextChanged(CharSequence s, int start, int count, int after) {}

	public void	onTextChanged(CharSequence s, int start, int before, int count) {
		view.setVisibility(VISIBLE);
		}
	}
	/*
static class makevis implements View.OnClickListener {
View view;
makevis(View v) {
	view=v;
	}
public  void onClick (View v) {
		view.setVisibility(VISIBLE);
	}

};
*/
static void hideSystemUI(Context cnt) {}
static public  EditText getedit(Context act, String text) {
	EditText label=new EditText(act);
        label.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        label.setImeOptions(editoptions);
        label.setMinEms(6);
	label.setText(text);
	return label;
	}

static public  EditText getnumedit(Context act, String text) {
	EditText label=new EditText(act);
        label.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        label.setImeOptions(editoptions);
        label.setMinEms(3);
	label.setText(text);
	return label;
	}
static String[] gethostnames() {
        String p2p=null;
        String norm=null;
        String bluepan=null;
	String hasone=null;
    try {
            Enumeration<NetworkInterface> inter = NetworkInterface.getNetworkInterfaces();
            while(inter.hasMoreElements()) {
                NetworkInterface in=inter.nextElement();
                Enumeration<InetAddress> addrs= in.getInetAddresses();
                while(addrs.hasMoreElements()) {
                    InetAddress a=addrs.nextElement();
                    String sa = a.getHostAddress();
                    String name=in.getName();
                    if(name.startsWith("p2p")) {
						if(sa!=null&&sa.startsWith("192.168.")) {
									p2p=sa;
						hasone=p2p;
						}
						}
                    else {
                        if (!in.isVirtual()) {
                            if(name.startsWith("wlan")) {
                                norm = sa;
			        hasone=norm;
                            } else {
                                if(name.startsWith("bt-pan")) {
                                    bluepan = sa;
				     hasone=bluepan;
                                   }
                            }
                        }
                    }
                }


            }
        }
        catch(Throwable e) {
           String mess=e.getMessage() ;
           if(mess==null)
                mess="Network error";
            Log.stack(LOG_ID,mess,e);
        }
	return new String[]{p2p,norm,bluepan,hasone};
        }



//String defaultport="7345";
boolean isasender=false;
boolean[] sendchecked;

	private static final String defaultport=isRelease?"8795":"9113";
	private	CheckBox Amounts =null;
	private CheckBox Scans =null;
	private CheckBox Stream =null,receive=null,detect=null;
	private RadioButton activeonly=null,passiveonly=null,both=null;
	private final EditText[] hostname={null,null,null,null};
	private View[] messages=null;
	private EditText editpass=null;
	private EditText portedit=null;
	private ScrollView hostview=null;
	private CheckBox Password;
	private Button reset=null;
	  private CheckBox testip,haslabel;
	private   EditText label;
private RadioButton[] sendfrom;
private View[] fromrow;

 private CheckBox   visible;
//	private Layout layout=null;
	int hostindex=-1;

	static void setradio(RadioButton[] radios) {
	   for(RadioButton but:radios) {
		but.setOnCheckedChangeListener( (buttonView,  isChecked) -> {
		   if(isChecked) {
			for(RadioButton b:radios) 
			    if(b!=buttonView)
				b.setChecked(false);
			   }
			});
		}
	  }
	static void setradiotest(RadioButton[] radios,Object[] ap) {
	   for(RadioButton but:radios) {
		but.setOnCheckedChangeListener( (buttonView,  isChecked) -> {
		   if(isChecked) {
		   	for(var o:ap) {
				  var a = (Consumer<View>) o;
				   a.accept(buttonView);
				}
			for(RadioButton b:radios) 
			    if(b!=buttonView)
				b.setChecked(false);
			   }
			});
		}
	  }
	@SuppressWarnings("deprecation")
	public static int agetColor(Context context, int id) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			return context.getColor(id);
		} else {
			//noinspection deprecation
			return context.getResources().getColor(id);
		}
	}
	@SuppressWarnings("deprecation")
	public static void setColorFilter(@NonNull Drawable drawable, @ColorInt int color) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			drawable.setColorFilter(new BlendModeColorFilter(color, BlendMode.SRC_ATOP));
		} else {
			drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
		}
	}
private void deleteconfirmation(MainActivity act) {
        AlertDialog.Builder builder = new AlertDialog.Builder(act);
        builder.setTitle(R.string.deleteconnection).
//	 setMessage(mess).
           setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
			if(hostindex>=0) {
				Natives.deletebackuphost(hostindex);
				hostadapt.notifyItemRemoved(hostindex);
				}
			hostview.setVisibility(GONE);
			hidekeyboard(act); 
			 act.poponback();
                    }
                }) .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        }).show();
	}
private void resentconfirmation(MainActivity act,int hostindex) {
        AlertDialog.Builder builder = new AlertDialog.Builder(act);
        builder.setTitle("Resend data?").
	 setMessage("All data will be send again which takes time and can have consequences for Libreview and Kerfstok").
           setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
			Natives.resetbackuphost(hostindex);
			configchanged=true;
                    }
                }) .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        }).show();
	}
void makehostview(MainActivity act) {
	for(int i=0;i<hostname.length;i++) {
		hostname[i]=new EditText(act);
		hostname[i].setMinEms(6);
		hostname[i].setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
		hostname[i].setImeOptions(editoptions);
//		hostname[i].getBackground().mutate().setColorFilter(agetColor(act,android.R.color.holo_blue_light), PorterDuff.Mode.SRC_ATOP);
		setColorFilter(hostname[i].getBackground().mutate(),agetColor(act,android.R.color.holo_blue_light));
		}
	portedit=new EditText(act);
	portedit.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
	portedit.setImeOptions(editoptions);
	portedit.setMinEms(3);
	Button save=getbutton(act,R.string.save);
	TextView Hostlabel=getlabel(act,R.string.ips);
	detect = new CheckBox(act);
	detect.setText(R.string.detect);
	detect.setOnCheckedChangeListener( (buttonView,  isChecked)-> {
			final int vis=isChecked?INVISIBLE:VISIBLE;
			final int lastip=hostname.length-(haslabel.isChecked()?1:0)-1;
			hostname[lastip].setVisibility(vis);
			});
	detect.setVisibility(INVISIBLE);

	testip= new CheckBox(act); testip.setText(R.string.testip);

	haslabel= new CheckBox(act); haslabel.setText(R.string.testlabel);
	label = new EditText(act);
        label.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);

        label.setImeOptions(editoptions);
        label.setMinEms(10);
//	label.setBackgroundColor(0x25212b);
//label.getBackground().mutate().setColorFilter(agetColor(act,android.R.color.holo_red_light), PorterDuff.Mode.SRC_ATOP);

	setColorFilter(label.getBackground().mutate(),agetColor(act,android.R.color.holo_red_light));
//	label.setBackgroundColor(YELLOW);
	haslabel.setOnCheckedChangeListener( (buttonView,  isChecked)-> {
			final int vis=isChecked?VISIBLE:INVISIBLE;
			label.setVisibility(vis);
			label.requestFocus();
			final int vis2=isChecked?INVISIBLE:VISIBLE;
			final int lastip=hostname.length-(detect.isChecked()?1:0)-1;
			hostname[lastip].setVisibility(vis2);
	//		label.setEnabled(isChecked);
			});

		
	passiveonly=new RadioButton(act);
	passiveonly.setText(R.string.passiveonly);
	TextView Portlabel=getlabel(act,R.string.port);
	  activeonly = new RadioButton(act);
	  activeonly.setText(R.string.activeonly);
	  both = new RadioButton(act);
	  both.setText(R.string.both);
	  RadioButton[] actives={passiveonly,activeonly,both};
	Consumer<View> test1=
	buttonView-> {
		if(buttonView==activeonly)
			detect.setChecked(false);
		final var vis=buttonView==passiveonly?INVISIBLE:VISIBLE;
		Portlabel.setVisibility(vis);
		portedit.setVisibility(vis);
		final var vis2=(buttonView==activeonly||(buttonView==passiveonly&&!testip.isChecked()))?INVISIBLE:VISIBLE;
		detect.setVisibility(vis2);
		final var vis4=(buttonView==passiveonly&&!testip.isChecked())?INVISIBLE:VISIBLE;
		final int ipnr=hostname.length-(haslabel.isChecked()?1:0)-(detect.isChecked()?1:0);
		for(int i=0;i<ipnr;i++)
			hostname[i].setVisibility(vis4);
		final var vis3=buttonView==activeonly?INVISIBLE:VISIBLE;
		testip.setVisibility(vis3);
	};
	Object[] tests={test1};
	  setradiotest(actives,tests);
	testip.setOnCheckedChangeListener( (buttonView,  isChecked)-> {
		final var vis2=(passiveonly.isChecked()&&!isChecked)?INVISIBLE:VISIBLE;
		final var vis=(activeonly.isChecked()||(passiveonly.isChecked()&&!testip.isChecked()))?INVISIBLE:VISIBLE;
		detect.setVisibility(vis);
		final int ipnr=hostname.length-(haslabel.isChecked()?1:0)-(detect.isChecked()?1:0);
		for(int i=0;i<ipnr;i++)
			hostname[i].setVisibility(vis2);
		});
	receive = new CheckBox(act);
	receive.setText(R.string.receivefrom);
	/*
	receive.setOnCheckedChangeListener( (buttonView,  isChecked)-> {
			if(!isChecked)  {
				detect.setChecked(false);
				}
			final int vis=isChecked?VISIBLE:INVISIBLE;
			detect.setVisibility(vis);
			}); */

	TextView Sendlabel=getlabel(act,R.string.sendto);

	   Amounts = new CheckBox(act); Amounts.setText(R.string.amountsname);
	   Scans = new CheckBox(act); Scans.setText(R.string.scansname);
	   Stream = new CheckBox(act); Stream.setText(R.string.streamname);
//	   TextView startlabel=getlabel(act,"Send
	RadioButton fromnow=new RadioButton(act);
 	RadioButton alldata=new RadioButton(act);
 	RadioButton screenpos=new RadioButton(act);
   TextView startlabel=getlabel(act,act.getString(R.string.datapresentuntil));
    	alldata.setText(R.string.start);
    	fromnow.setText(R.string.now);
//	screenpos.setText("From Screen position");
	sendfrom=new RadioButton[]{alldata,fromnow,screenpos};
	 fromrow=new View[]{startlabel, alldata,fromnow,screenpos};

	setradio(sendfrom);
	CheckBox restore=new CheckBox(act);restore.setText("Restore");
	if(!Natives.backuphasrestore( ))
		restore.setVisibility(GONE);

	Button Help=getbutton(act,R.string.helpname);
	Help.setOnClickListener(v-> help.help(R.string.addconnection,act));

	Button delete=getbutton(act,act.getString(R.string.delete));
	Button Close=getbutton(act,R.string.cancel);
	    Password = new CheckBox(act); Password.setText(R.string.password);
	   Password.setChecked(true);
	 editpass= new EditText(act);
        editpass.setImeOptions(editoptions);
        editpass.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
	editpass.setTransformationMethod(new PasswordTransformationMethod());
        editpass.setMinEms(6);
       visible = new CheckBox(act); visible.setText(R.string.visible);
	visible.setOnCheckedChangeListener( (buttonView,  isChecked)-> {

        		editpass.setInputType(isChecked?InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD:InputType.TYPE_TEXT_VARIATION_PASSWORD);
        		if(isChecked)
					editpass.setTransformationMethod(null);
        		else
					editpass.setTransformationMethod(new PasswordTransformationMethod());
			});

	Password.setOnCheckedChangeListener( (buttonView,  isChecked)-> {
			final int vis=isChecked?VISIBLE:INVISIBLE;
			editpass.setVisibility(vis);
			visible.setVisibility(vis);
			});
	 Password.setChecked(false); 
	save.setOnClickListener(v->{ 
		final boolean sender= Amounts.isChecked()|| Stream.isChecked()|| Scans.isChecked();
		final boolean receiver=receive.isChecked();
		if(!sender&&!receiver) {
			Toast.makeText(act, R.string.specifyreceiveordata,Toast.LENGTH_SHORT).show();
			return;
			}
		if(receiver&& Amounts.isChecked()&& Stream.isChecked()&& Scans.isChecked()) {
			Toast.makeText(act,"When everything is sent to a host, \"Receive from\" should be turned off." ,Toast.LENGTH_LONG).show();
			return;
			}		
		hidekeyboard(act); //USE
		int hostnr=Natives.backuphostNr( );
		String[] names=new String[hostname.length];
		int struse=0;
		if(testip.isChecked()||!passiveonly.isChecked()) {
			for (EditText editText : hostname) {
				String name = editText.getText().toString();
				if (name.length() != 0) {
					names[struse++] = name;
				}
			}
			}
		final boolean dodetect= detect.isChecked()&&!activeonly.isChecked();
		int ipmax=hostname.length-(dodetect?1:0)-(haslabel.isChecked()?1:0);
		if(struse>=ipmax)
			struse=ipmax;
		if((testip.isChecked()&&!dodetect)||activeonly.isChecked()) {
			if(struse==0) {
				Toast.makeText(act, R.string.specifyip,Toast.LENGTH_SHORT).show();
				return ;
				}
			}

		long starttime=(alldata.getVisibility()!=VISIBLE||alldata.isChecked())?0L:(fromnow.isChecked()? System.currentTimeMillis():Natives.getstarttime())/1000L;

		int pos=Natives.changebackuphost(hostindex,names,struse,dodetect,portedit.getText().toString(), Amounts.isChecked(),Stream.isChecked(),Scans.isChecked(),restore.isChecked(),receiver,activeonly.isChecked(),passiveonly.isChecked(),Password.isChecked()?editpass.getText().toString():null,starttime,haslabel.isChecked()?label.getText().toString():null,testip.isChecked());

		if(pos<0) {
			String mess;
			switch(pos) {
				case -1: mess=act.getString(R.string.portrange);break;
				case -2: mess="Error parsing ip";break;
				case  -3: mess="Maximal number of hosts exceeded";break;
				case  -4: mess="Juggluco can sent to maximal 4 hosts";break;
				case -6: mess="Database busy, try again";break;
				default: mess="Error";break;
				}
			Toast.makeText(act,mess,Toast.LENGTH_SHORT).show();
			return ;
			}	
		configchanged=true;
	        if(pos==hostnr)  {
		    delete.setVisibility(VISIBLE);
		    hostadapt.notifyItemInserted(pos);
		    }
		   else
			hostadapt.notifyItemChanged(pos);
		 hostview.setVisibility(GONE);
		 act.poponback();
	  	//alarms.setEnabled( Natives.isreceiving( ));
		}); 
	delete.setOnClickListener(v->{ 
		deleteconfirmation(act) ;
	  	//alarms.setEnabled( Natives.isreceiving( ));
		});
//	Portlabel.setVisibility(INVISIBLE);
//	portedit.setVisibility(INVISIBLE);
	//online.setVisibility(INVISIBLE);
	reset=getbutton(act,R.string.resenddata);
	reset.setOnClickListener(v->{ 
		if(hostindex>=0) {
			resentconfirmation(act,hostindex);
			}
		});
	CheckBox[] boxes={Amounts,Scans,Stream,restore};
	 CompoundButton.OnCheckedChangeListener needport =(buttonView, isChecked)-> {
		//if(!isasender) 
		if(sendchecked==null)
			return;

		var vis=INVISIBLE;
		for(int i=0;i<3;i++) {
			if(!sendchecked[i]&&boxes[i].isChecked()) {
				vis=VISIBLE;
				}
			}
		for(View v:fromrow)
			v.setVisibility(vis);
		};
	for(CheckBox vi:boxes) {
		vi.setOnCheckedChangeListener(needport);
		}
        hostview=new ScrollView(act);

	Layout layout;
	messages=new View[]{getlabel(act,"Messages")};
	if(isWearable) {
		layout=new Layout(act, (l, w, h) -> {
			hideSystemUI(act);
			final int[] ret={w,h};
			return ret;
		}, new View[]{ Portlabel},new View[] {portedit},new View[]{new Space(act),Hostlabel,detect,new Space(act)}, Arrays.copyOfRange(hostname,0,hostname.length/2),Arrays.copyOfRange(hostname,hostname.length/2,hostname.length) ,new View[] {testip,haslabel},new View[]{label},
				new View[]{passiveonly,activeonly},new View[]{both,receive},new View[] {Sendlabel,Amounts},new View[]{Scans,Stream},new View[]{startlabel},new View[]{alldata,fromnow},new View[]{screenpos} ,new View[]{Password,visible },new View[]{editpass},new View[]{delete,Close},new View[] {reset},new View[]{save}, messages);
		}
	else {
		layout = new Layout(act, (l, w, h) -> {
			hideSystemUI(act);
			final int[] ret = {w, h};
			return ret;
		}, new View[]{Portlabel, portedit, Hostlabel, detect}, hostname, new View[]{testip, haslabel, label},
				new View[]{passiveonly, activeonly, both}, new View[]{receive, Sendlabel, Amounts, Scans, Stream, restore}, fromrow, new View[]{Password, editpass, visible}, new View[]{delete, Close, reset, Help, save},messages);
		}
	Close.setOnClickListener(v-> act.doonback());
	hostview.addView(layout);
	hostview.setFillViewport(true);
	hostview.setSmoothScrollingEnabled(false);
        hostview.setVerticalScrollBarEnabled(false);
//	act.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE|WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        act.addContentView(hostview, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT));
        hostview.setBackgroundColor(backgroundcolor);

	}
void changehostview(MainActivity act,final int index,String[] names,boolean dodetect,String port,String pass) {
	if(hostview==null)
		makehostview(act);
	else {
		hostview.setVisibility(VISIBLE);
        	hostview.bringToFront();
		visible.setChecked(false);
		}
	act.setonback(() -> {
			hidekeyboard(act);
			hostview.setVisibility(GONE);
			});
	boolean stream,scans,amounts;
       if(names!=null) {
	      stream=Natives.getbackuphoststream(index);
	       scans=Natives.getbackuphostscans(index);
	       amounts=Natives.getbackuphostnums(index);
		int recnum=Natives.getbackuphostreceive(index);
		boolean doreceive= (recnum&2)!=0;
	       receive.setChecked(doreceive);
//	        passiveonly.setChecked(doreceive&&(recnum&1)==0);
		final boolean dotestip=Natives.getbackuptestip(index);
		final boolean ispassive=Natives.getbackuphostpassive(index);
		testip.setChecked(dotestip);
		final var vis=(ispassive&&!dotestip)?INVISIBLE:VISIBLE;
		   detect.setChecked(dodetect);



	 	final String labelstr=Natives.getbackuplabel(index);
		if(labelstr!=null) {
			label.setText(labelstr);
			haslabel.setChecked(true); 
			  }
		 else {
			label.setText("");
			haslabel.setChecked(false); 
			label.setVisibility(INVISIBLE);
			}
		int maxhosts=hostname.length-(dodetect?1:0)-(labelstr==null?0:1);
		   for(int i=0;i<Math.min(names.length,maxhosts);i++) {
			   hostname[i].setText(names[i]);
		   }

		   for(int i=0;i<maxhosts;i++)
		       hostname[i].setVisibility(vis);
		   boolean isactiveonly =Natives.getbackuphostactive(index);
		   detect.setVisibility((ispassive&&!dotestip||isactiveonly)?INVISIBLE:VISIBLE);
		if(isactiveonly)
			activeonly.setChecked(true);
		else {
			if(ispassive) {
				passiveonly.setChecked(true);
				}
			else
				both.setChecked(true);
			}
		boolean iswearos=isWearOS(index);
		Log.i(LOG_ID,(labelstr!=null?labelstr:"")+" Iswearos("+index+")="+iswearos);
		messages[0].setVisibility((iswearos&& getBlueMessage( ) )?VISIBLE:GONE);
		}
	else {
		messages[0].setVisibility(GONE);
		 stream=false;scans=false;amounts=false;

	       receive.setChecked(false);
	       detect.setChecked(false);
		both.setChecked(true);
		testip.setChecked(true);
		haslabel.setChecked(false);
		label.setVisibility(INVISIBLE);
		label.setText("");
		 } 

       Stream.setChecked(stream); Scans.setChecked(scans); Amounts.setChecked(amounts);
	isasender=stream||scans||amounts;
	sendchecked=new boolean[]{amounts,scans,stream};
	sendfrom[2].setText( tk.glucodata.util.timestring(Natives.getstarttime()));
	if(!isasender) {
	/*	for(View v:sendfrom) 
			v.setVisibility(VISIBLE);
		sendfrom[2].setText(GarminStatus.timestring(Natives.getstarttime()));*/
		reset.setVisibility(GONE);
		}
	else {
		reset.setVisibility(VISIBLE);
		}
	

	sendfrom[0].setChecked(true);
//	sendfrom[1].setChecked(false);
	for(View v:fromrow) 
		v.setVisibility(GONE);
      for(int i=names==null?0:names.length;i<hostname.length;i++) 
	       hostname[i].setText("");


       portedit.setText(port);
       if(pass!=null&&pass.length()>0) {
       		editpass.setText(pass);
			Password.setChecked(true);
       		editpass.setVisibility(VISIBLE);
       		}
	else {
       		editpass.setText("");
		Password.setChecked(false);
       		editpass.setVisibility(INVISIBLE);
		}
       hostindex=index;
	}
void changehostview(MainActivity act,int index) {
	String[] names=Natives.getbackuphostnames(index);
	String port=Natives.getbackuphostport(index);
	String pass= Natives.getbackuppassword(index);
	changehostview(act,index,names,Natives.detectIP(index),port,pass) ;
	}
void addhostview(MainActivity act) {
//	int hostnr=Natives.backuphostNr( );
	changehostview(act,-1,null,false,defaultport,"") ;
	}


HostViewAdapter hostadapt;
Button alarms;
public  void mkbackupview(MainActivity act) {
	act.showui=true;
	act.showSystemUI();
	Applic.app.getHandler().postDelayed( ()-> realmkbackupview(act),1);
	}
private  void realmkbackupview(MainActivity act) {
configchanged=false;
 // activity=act;
 String[] thishost=gethostnames();
 if(thishost[3]!=null)
 	Natives.networkpresent();
 TextView ip=getlabel(act,"wlan: "+thishost[1]);
View p2p= (thishost[0]==null)?new Space(act):getlabel(act,"p2p: "+thishost[0]);
View blpan= (thishost[2]==null)?new Space(act):getlabel(act,"bt-pan: "+thishost[2]);
  String port=Natives.getreceiveport();
  TextView labport=getlabel(act,R.string.port);
  EditText portview=getnumedit(act, port);

  Button hosts=getbutton(act,R.string.addconnectionbutton);
   hosts.setOnClickListener(v-> addhostview(act));
  Button Help=getbutton(act,R.string.helpname);
   Help.setOnClickListener(v->
   	help.help(R.string.connectionoverview,act) );

  Button Sync=getbutton(act,act.getString(R.string.sync));
   Sync.setOnClickListener(v-> Applic.wakemirrors());
  Button reinit=getbutton(act,"Reinit");
   reinit.setOnClickListener(v-> Natives.resetnetwork());
  boolean[] issaved={false};
   alarms=getbutton(act,R.string.alarms);
   alarms.setOnClickListener(v-> tk.glucodata.settings.Settings.alarmsettings(act,null,issaved));
//      if(!Natives.isreceiving( )) { alarms.setEnabled(false); }

  final Button battery = new Button(act);


  Button Cancel=getbutton(act,R.string.closename);
	Button Save=getbutton(act,R.string.save);
	Save.setVisibility(INVISIBLE);
	changer ch=new changer(Save);
	portview.addTextChangedListener(ch);
	RecyclerView recycle = new RecyclerView(act);
	LinearLayoutManager lin = new LinearLayoutManager(act);
	recycle.setLayoutManager(lin);
 hostadapt = new HostViewAdapter(); //USE
	recycle.setAdapter(hostadapt);
	recycle.setLayoutParams(new ViewGroup.LayoutParams(  MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        CheckBox staticnum = new CheckBox(act);
	staticnum.setOnCheckedChangeListener( (buttonView,  isChecked)-> Natives.setstaticnum(isChecked));
	staticnum.setText(R.string.dontchangeamounts);
	staticnum.setChecked(Natives.staticnum());
	var lineheight=staticnum.getLineHeight();

	recycle.setMinimumHeight(lineheight*6);
	View lay;
/*	var wifion=getbutton(act,"WifiOn");
	wifion.setOnClickListener(v-> 
		{
		new UseWifi().usewifi();
		});*/

	if(isWearable) {
		CheckBox wifi=getcheckbox(act,act.getString(R.string.wifi),getWifi());
		wifi.setOnCheckedChangeListener( (buttonView,  isChecked)-> {
			Natives.setWifi(isChecked);
			if(isChecked) {
				usewifi(); 
				}
			else
				UseWifi.stopusewifi();
			});
		final Layout layout=new Layout(act, new View[]{getlabel(act,act.getString(R.string.thishost))},new View[]{blpan},new View[]{p2p},new View[]{ip},new View[]{new Space(act),labport,portview,Save,new Space(act)},new View[]{recycle},new View[] {hosts},new View[]{staticnum},new View[]{Sync,reinit},new View[]{wifi,alarms},new View[]{Cancel});
		var hori=new NestedScrollView(act);
		hori.setFillViewport(true);
		hori.setSmoothScrollingEnabled(false);
                hori.setVerticalScrollBarEnabled(false);
                hori.setHorizontalScrollBarEnabled(false);
		int height=GlucoseCurve.getheight();
		hori.setMinimumHeight(height);
		hori.addView(layout);
		lay=hori;
		}
	else {
		lay=new Layout(act, new View[]{ip,blpan,p2p,labport,portview,Save},new View[]{recycle},new View[] {battery,Help,alarms,staticnum},new View[]{Sync,reinit,hosts,Cancel});
		}
	Save.setOnClickListener(v->  {
	/*
		if(issaved[0]) {
			Applic.sendsettings();
			} */
		Natives.setreceiveport(portview.getText().toString());
		Save.setVisibility(GONE);
		hidekeyboard(act);
	});
	Runnable closerun= ()-> {
	/*(
		if(issaved[0]) {
			Applic.sendsettings();
			} */
		if(hostview!=null)
			removeContentView(hostview);
		hidekeyboard(act);
		removeContentView(lay);
		if(configchanged)
			Applic.wakemirrors();
		Applic.updateservice(act,Natives.getusebluetooth());
		act.showui=false;
		Applic.app.getHandler().postDelayed(act::hideSystemUI,1);

		};
	act.setonback(closerun);	
	Cancel.setOnClickListener(v->  {
		act.poponback();	
		closerun.run();
		});

	if(!isWearable&&android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
		battery.setText(R.string.dozemode);
		battery.setOnClickListener(v-> Battery.batteryscreen(act));
		}
	else {
		battery.setVisibility(GONE);
	}
	lay.setBackgroundColor(Applic.backgroundcolor);
	act.addContentView(lay, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT));
	}


  class HostViewHolder extends RecyclerView.ViewHolder {
    public HostViewHolder(View view) {
        super(view);
       view.setOnClickListener(v -> {
		int pos=getAbsoluteAdapterPosition();
//		int pos=getAdapterPosition();
		changehostview((MainActivity)(v.getContext()),pos);
		});

    }

}
 public class HostViewAdapter extends RecyclerView.Adapter<HostViewHolder> {
    	

    @NonNull
	@Override
    public HostViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    	TextView view=new Button( parent.getContext());
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f);
	view.setLayoutParams(new ViewGroup.LayoutParams(  ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
       view.setGravity(Gravity.LEFT);
        return new HostViewHolder(view);

    }

	@Override
	public void onBindViewHolder(final HostViewHolder holder, int pos) {
		TextView text=(TextView)holder.itemView;
		String[] names =Natives.getbackuphostnames(pos);
		 StringBuilder sb = new StringBuilder();
		String port=Natives.getbackuphostport(pos);
		long date=Natives.lastuptodate(pos);
	        boolean passive=Natives.getbackuphostpassive(pos);
		String label=Natives.getbackuplabel(pos);
	       boolean stream=Natives.getbackuphoststream(pos);
	       boolean scans=Natives.getbackuphostscans(pos);
	       boolean amounts=Natives.getbackuphostnums(pos);
		int recnum=Natives.getbackuphostreceive(pos);
		boolean doreceive= (recnum&2)!=0;
		  if(label!=null) {
		  	sb.append(label);
		  	sb.append(" ");
			}

		  sb.append((names!=null&&names.length!=0)?names[0]:(Natives.detectIP(pos)?"Detect":"---"));
		  if(!passive) {
			  sb.append(" ");
			  sb.append(port);
			  }
		  sb.append(' ');
		 if(amounts) {
			  sb.append("n");
			  }
		 if(scans) {
			  sb.append("s");
			  }
		 if(stream) { 
			  sb.append("b");
		 	}
		 if(doreceive) { 
			  sb.append("r");
		 	}
		if(date!=0L) {
			String str=bluediag.datestr(date);
			sb.append("   \u21CB ").append(str);
			}
		text.setText(sb);
	    }
        @Override
        public int getItemCount() {
		return Natives.backuphostNr( );

        }

}
boolean configchanged=false;

}