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


package tk.glucodata

//import androidx.activity.Context
//import androidx.lifecycle.Lifecycle
//import androidx.lifecycle.lifecycleScope
import android.content.Context
import androidx.annotation.Keep
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import tk.glucodata.Applic.JUGGLUCOIDENT;
import tk.glucodata.Applic.isWearable
//import tk.glucodata.Applic.messagesender


class MessageSender(val activity: Context):CapabilityClient.OnCapabilityChangedListener {
    private val messageClient by lazy { Wearable.getMessageClient(activity) }
    private val capabilityClient by lazy { Wearable.getCapabilityClient(activity) }
    private val nodeClient by lazy { Wearable.getNodeClient(activity) }
    public val localnode by lazy { Tasks.await(nodeClient.localNode).id }


    var nodes: Set<Node>? = null
    var nexttimes:LongArray?=null

    private fun setnodes(ns:Set<Node>) {
        nodes = ns
        val len: Int = nodes?.size ?: 0
        nexttimes = LongArray(len)
	sendnetinfo();
    }
    public fun nulltimes() {
        nexttimes?.fill(0L)
    }
    private suspend fun findWearDevicesWithApp() {

        Log.d(LOG_ID, "findWearDevicesWithApp()")

        try {
            val capabilityInfo = capabilityClient.getCapability( JUGGLUCOIDENT, CapabilityClient.FILTER_REACHABLE).await()
                setnodes(capabilityInfo.nodes)
                Log.d(LOG_ID, "Capable Nodes: $nodes")
        } catch (cancellationException: CancellationException) {
            throw cancellationException
        } catch (th: Throwable) {
            Log.stack(LOG_ID, "findDev",th)
        }
    }
public fun finddevices() {
            val sender=this
	    scope.launch {
		findWearDevicesWithApp()
		}
		Wearable.getCapabilityClient(activity).addListener(sender, JUGGLUCOIDENT)

	}
	init {
		finddevices()
		}


    public fun startActivity() {
	val data=Natives.bytesettings()
      sendmessage(START_PATH, data) 
    }

public fun startActivity(node:Node) {
	val data=Natives.bytesettings()
	nodeSendmessage(node,START_PATH,data)
	}
/*
private fun startnodedetection(context: Context):String? {
    Wearable.getCapabilityClient(context).addListener( this,JUGGLUCOIDENT )
    val capabilityInfo: CapabilityInfo = Tasks.await(capabilityClient.getCapability( JUGGLUCOIDENT, CapabilityClient.FILTER_REACHABLE))
	return pickBestNodeId(capabilityInfo.nodes)
}
*/
//private var transcriptionNodeId: String? = null



override fun onCapabilityChanged(cap: CapabilityInfo) {
	    scope.launch {
    		setnodes(cap.nodes)
		}
	}
    private fun sendmessage(path:String,data:ByteArray) {

            try {
	    when {
            nodes == null -> {
                Log.d(LOG_ID, "sendmessage nodes=null")
            }
            nodes?.isEmpty() == true -> {
                Log.d(LOG_ID, "sendmessage nodes.isEmpty")
            }
            else -> {
                    nodes?.map { node ->
                        scope.launch {
                            Log.i(LOG_ID, "sendMessage(${node.id} ${node.displayName}, $path,)")
                            try {
                                messageClient.sendMessage(node.id, path, data)
                            } catch (th: Throwable) {
                                Log.stack(LOG_ID, th);
                            }
                           }
			            }
                    Log.d(LOG_ID, "Starting requests sent successfully")
                }
            }
            } catch (exception: Exception) {
                Log.d(LOG_ID, "Starting activity failed: $exception")
        }
    }
private fun nameSendMessage(name:String, path:String, data:ByteArray) {
	scope.launch {
	    Log.i(LOG_ID, "sendNameMessage($name $path,... )")
	    try {
		messageClient.sendMessage(name, path, data)
                }
	    catch (th: Throwable) { Log.stack(LOG_ID, th); }
		}
	}
private fun nameSendMessageResult(name:String, path:String, data:ByteArray):Boolean {
	    try {
		val res=Tasks.await(messageClient.sendMessage(name, path, data))
		Log.i(LOG_ID,"nameSendMessageResult "+res)
		return true
		}
	    catch (th: Throwable) {
            Log.stack(LOG_ID, th)
	    	return false
		}

	}

private fun nodeSendmessage(node:Node,path:String,data:ByteArray) {
	nameSendMessage(node.id,path,data);
	}
		
    public fun sendnetinfo(data:ByteArray) {
	sendmessage(NET_PATH,data);
    	}
    public fun sendnetinfo( node:Node,data:ByteArray) {
	nodeSendmessage(node,NET_PATH,data);
    	}
    public fun sendnetinfo( node:String,data:ByteArray) {
	nameSendMessage(node,NET_PATH,data);
    	}
    public fun sendsettings() {
	val data=Natives.bytesettings()
	sendmessage(SETTINGS_PATH,data)
	}
    public fun sendbluetooth( node:Node,on:Boolean) {
	sendbool(BLUETOOTH_PATH,node.id,on)

	 }
    public fun sendOnmessages( node:String,on:Boolean) {
	sendbool(MESSAGES_PATH,node,on)
	 }
	 /*
    public fun sendbluetooth(on:Boolean) {
	sendbool(BLUETOOTH_PATH,on)
	 }
    public fun sendbool(String path,on:Boolean) {
        val onbyte:Byte=if(on) 1;else 0;
        val onar:ByteArray= byteArrayOf(onbyte)
	sendmessage(path,onar)
	 } */
    public fun sendbool( path:String,nodeName:String,on:Boolean) {
        val onbyte:Byte=if(on) 1;else 0;
        val onar:ByteArray= byteArrayOf(onbyte)
	nameSendMessage(nodeName,path,onar)
	 }



companion object {
        private const val LOG_ID = "MessageSender"
	    const val WAKE_PATH = "/wake"
	    const val WAKESTREAM_PATH = "/wakestream"
	    const val NET_PATH = "/netinfo"
	    const val START_PATH = "/start"
	    const val SETTINGS_PATH = "/settings"
	    const val BLUETOOTH_PATH = "/bluetooth"
        const val DATA_PATH = "/data"
        const val MESSAGES_PATH = "/messages"
val scope=CoroutineScope(Dispatchers.IO)
private var 	messagesender:MessageSender? = null
@JvmStatic 	public fun getMessageSender():MessageSender? {
        return messagesender
}
        private var nodenames: Array<String>? = null
        fun getNodeName(ident: Int): String {
            if (nodenames == null)
                throw  NullPointerException("getNodeName nodenames==null")
            else
                return nodenames!!.get(ident)
        }

@JvmStatic
public fun sendwake() {
	 val sender = messagesender ?: return
    val ar =byteArrayOf(0);
	sender.sendmessage(WAKE_PATH,ar)
	}
@JvmStatic
public fun sendwakestream() {
	 val sender = messagesender ?: return
    val ar =byteArrayOf(0);
	sender.sendmessage(WAKESTREAM_PATH,ar)
	}
@Keep
@JvmStatic
public fun sendDatawithName(ident: String, data: ByteArray):Boolean {
	 val sender = messagesender ?: return false
	return sender.nameSendMessageResult(ident, DATA_PATH, data)
    }
@Keep
@JvmStatic
public fun sendData(data: ByteArray):Boolean {
    val sender = messagesender ?: return false
    val nodes = sender.nodes
    if (nodes == null || nodes.isEmpty()) return false
	return sendDatawithName(nodes.elementAt(0).id,data)
    }
@Keep
@JvmStatic
public fun sendNameMessageOn(ident: String, on: Boolean) {
	 val sender = messagesender ?: return
	return sender.sendOnmessages( ident,on);
    }
@Keep
@JvmStatic
public fun sendMessageOn(on: Boolean) {
    val sender = messagesender ?: return
    val nodes = sender.nodes
    if (nodes == null || nodes.isEmpty()) return
	return sendNameMessageOn(nodes.elementAt(0).id,on)
    }
/*
@Keep
@JvmStatic
public fun sendDatawithInt(ident: Int, data: ByteArray) {
	    try {
		messagesender?.nameSendMessage(getNodeName(ident), DATA_PATH, data)
	    } catch (th: Throwable) {
		Log.stack(LOG_ID, "sendData $ident", th);
	    }
    } */

    @JvmStatic 	public fun initwearos(app: Context) {
	Log.i(LOG_ID,"before new MessageSender");
	messagesender=MessageSender(app)
//	Log.i(LOG_ID,"before sendnetinfo");
//	sendnetinfo();
	}
      @JvmStatic 	public fun cansend():Boolean {
		val sender:MessageSender?=messagesender
		if(sender==null) {
			Log.e(LOG_ID,"messagesender==null");
			return false
			}
                val tmp=sender.nodes
		 if(tmp==null||tmp.isEmpty()) {
		 	Log.e(LOG_ID,"no sender.nodes");
			return false
			}
		return true
		}

        private const val netwait = (1000 * 60).toLong()
    private fun inargsendnetinfo(id: String) {
	    Log.i(LOG_ID,"sendnetinfo($id)");
            if(!cansend()) {
                Log.i(LOG_ID, "!cansend()")
                return
            }
            if(messagesender==null) {
                Log.e(LOG_ID, "no messagesender")
                return
            }
            val sender:MessageSender = messagesender as MessageSender

            val nodes = sender.nodes
            if(nodes == null || nodes.isEmpty()) {
	    	Log.e(LOG_ID,"no nodes")
	    	return
		}
            val times = sender.nexttimes
            val num = nodes.size
            var it = 0
            while(true) {
                if(it == num) {
                    Log.e(LOG_ID, "Can't find $id")
                    return
                }
                if(id == nodes.elementAt(it).getId()) break
                it++
            }
            val nu = System.currentTimeMillis()
            if(times!![it] > nu) {
	    	Log.i(LOG_ID,"times!![it] > nu) it=$id times!![it]=${times!![it]} nu=$nu ")
                return
            }
	if(sender.localnode==null) {
		Log.d(LOG_ID,"localnode==null")
	    	return
		}
            val netinfo: ByteArray?

            netinfo = if(isWearable) { Natives.getmynetinfo(sender.localnode, true, 0) } else { Natives.getmynetinfo(id, false, 0) }
            if(netinfo == null) {
	    	Log.e(LOG_ID,"netinfo=null")
	    	return
		}
	    Log.i(LOG_ID, "sender.sendnetinfo($id, netinfo)");
            sender.sendnetinfo(id, netinfo)
            times[it] = nu + netwait
        }

        @JvmStatic 	public fun sendnetinfo(id: String) {
		scope.launch {	
        		inargsendnetinfo(id) 
			}
		}
	private fun insendnetinfo() {
		Log.i(LOG_ID,"sendnetinfo()")

            val nu = System.currentTimeMillis()
            if (!cansend()) {
                Log.i(LOG_ID, "!cansend()")
                return
            }
            val sender = messagesender ?: return
            val nodes = sender.nodes
            if (nodes == null || nodes.isEmpty()) return
            val times = sender.nexttimes
            val nextnetinfo = nu + netwait
            val num = nodes.size
            for (i in 0 until num) {
                val node: Node = nodes.elementAt(i)
                if (times!![i] < nu) {
                    val name = if (isWearable) sender.localnode else node.id
		    if(name==null) {
		    	Log.d(LOG_ID,"name=null")
		    	continue
			}
                    val netinfo = Natives.getmynetinfo(name, isWearable, 0) ?: continue
                    sender.sendnetinfo(node, netinfo)
                    times[i] = nextnetinfo
                } else {
                    Log.i(LOG_ID, "sendnetinfo already done " + node.id)
                }
            }
        }
        @JvmStatic 	public fun sendnetinfo() {
		scope.launch {	
        		insendnetinfo()
			}
		}
    }

}
