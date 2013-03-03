/**
 * 
 */
package net;

import java.util.Enumeration;
import java.util.Hashtable;

import peersim.core.CommonState;
import peersim.edsim.EDSimulator;

/**
 * @author guthemberg
 *
 */
public class NetKernel {

	//initialization  flag
	private static boolean ready=false;
	
	//messages table
	private static Hashtable<Long, NetworkMessage> messages;

	//connections to be changed table
	private static Hashtable<Long, ConnectionInfo> connections;
	
	private static long conn_id_gen;
	private static long ts_dance_control;
	private static Hashtable<Long, NetworkController> uploads;
	private static Hashtable<Long, NetworkController> downloads;
	private static final long PASSO=10;
	/**
	 * 
	 */
	public NetKernel() {
		// TODO Auto-generated constructor stub
	}

	public static void bootstrap(){
		if(!ready){
			messages = new Hashtable<Long, NetworkMessage>();
			connections = new Hashtable<Long, ConnectionInfo>();
			conn_id_gen=0;
			ready=true;
			ts_dance_control=CommonState.getTime();
			uploads=new Hashtable<Long, NetworkController>();
			downloads=new Hashtable<Long, NetworkController>();
		}
	}
	
	public static long getNewConnectionId(){
		return conn_id_gen++;
	}
	
	public static void clear(){
		ready=true;
		messages.clear();
		connections.clear();
		conn_id_gen=0;
		ts_dance_control=CommonState.getTime();
		uploads.clear();
		downloads.clear();
	}
	/**
	 * adds a event to the NetKernel scheduler 
	 * 
	 * @param ts when the message must be opened
	 * @param event the type of message
	 * @param c connections to be treated
	 */
	public static void add(long ts, byte event, Connection c){
		long now = CommonState.getTime();
		NetworkMessage msg = messages.get(Long.valueOf(ts));
		//first, create and add the message to the queue
		if(msg==null){
			msg=new NetworkMessage(ts, c.getSender(), c.getReceiver());
			messages.put(Long.valueOf(ts), msg);
            EDSimulator.add(ts-now, msg, c.getReceiver(), c.getNPId());
		}
		msg.putConnection(event, c);
			
	}
	
	public static void update(long c_time, long n_time, byte event, Connection c){
		NetworkMessage src_msg = messages.get(Long.valueOf(c_time));
		if(src_msg!=null){
			src_msg.removeConnection(event, c);
		}
		add(n_time,event,c);		
	}
	public static Enumeration<Connection> getConnections(long ts, byte event){
		NetworkMessage msg = messages.get(Long.valueOf(ts));
		if(msg!=null){
			msg.getConnections(event);
		}
		return null;
	}
	
	public static void updateModifiedConnections(){
		Enumeration<ConnectionInfo> infos = connections.elements();
		while (infos.hasMoreElements()) {
			ConnectionInfo info = infos.nextElement();
			if(info.getCurrentTime()!=info.getConnection().getEnd())
				update(info.getCurrentTime(), 
						info.getConnection().getEnd(), 
						NetworkMessage.SEND_FIN, info.getConnection());
		}
		connections.clear();
	}
	
	public static void addModifiedConnection(long c_time, Connection c){
		ConnectionInfo info = connections.get(Long.valueOf(c.getId()));
		if(info==null){
			info = new ConnectionInfo(c_time, c);
			connections.put(Long.valueOf(c.getId()), info);
		}
	}
	

	public static void remove(long ts){
		messages.remove(Long.valueOf(ts));
	}

	public static boolean removeConnection(long ts, Connection c){
		return removeConnection(ts, c.getId());
	}
	public static boolean removeConnection(long ts, long cid){
		connections.remove(cid);
		NetworkMessage msg = messages.get(Long.valueOf(ts));
		if(msg!=null){
			if(msg.removeConnection(cid)!=null)
				return true;
		}
		return false;
	}
	public static Hashtable<Long, NetworkController> getUploads(){
		return uploads;
	}
	public static Hashtable<Long, NetworkController> getDownloads(){
		return downloads;
	}
	
    public static void dance(){
    	long now = CommonState.getTime();
    	if(ts_dance_control<=now){
    		//update ts_dance_control
    		while (ts_dance_control<=now) {
				ts_dance_control+=PASSO;
			}
    		//now run the procedures
        	//do forro uploads
        	Enumeration<Long> ids = uploads.keys();
        	while (ids.hasMoreElements()) {
    			Long id = ids.nextElement();
    			uploads.get(id).forroUploads(id.longValue());
    		}
        	uploads.clear();
        	//do forro uploads
        	ids = downloads.keys();
        	while (ids.hasMoreElements()) {
    			Long id = ids.nextElement();
    			downloads.get(id).forroDownloads(id.longValue());
    		}    	
        	downloads.clear();
    	}
    }
    
    public static int getConnectionsSize(){
    	return connections.size();
    }

    public static int getEventsSize(){
    	return messages.size();
    }

    public static int getNetLayersSize(){
    	return uploads.size()+downloads.size();
    }
	
		
}
