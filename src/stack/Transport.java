/**
 * 
 */
package stack;

import java.util.Enumeration;
import java.util.Hashtable;

import net.NetworkController;
import net.TransportSkeleton;

import peersim.core.CommonState;
import peersim.core.Network;
import peersim.core.Node;
import peersim.core.Protocol;
import peersim.edsim.EDProtocol;
import peersim.edsim.EDSimulator;
import stack.Message;

/**
 * @author guthemberg
 *
 */
public class Transport implements EDProtocol, Protocol, TransportSkeleton {

	int application;
	long last_update;
	long tx;
	long rx;
	long last_tx;
	long last_rx;
	long bwd=0;
	long ts=0;
	int transport;
	int network;
	Node node;
	Hashtable<Long, Message> uploads;
	Hashtable<Long, Message> downloads;
	long update_time;
	long transient_tx;
	/**
	 * 
	 */
	public Transport(String prefix) {
//		this.rx=0;
//		this.tx=0;
	}
 
	public Object clone() {
        Transport local = null;
        try {
        	local= (Transport) super.clone();
        } catch (CloneNotSupportedException e) {
        	System.err.println("Unexpected error in:"+this.toString());
        } 
//        local.rx=0;
//        local.tx=0;
    	return local;
    }

	public void send(Node src,Node dst, int transport,Message message){
		message.setToQueued();
		Transport src_net = (Transport)((message).getSrc()).getProtocol(transport);
		long conn_id = src_net.send(message);
		if(conn_id>=0){
			Transport dst_net = (Transport)((message).getDst()).getProtocol(transport);
			src_net.uploads.put(new Long(conn_id), message);
			dst_net.downloads.put(new Long(conn_id), message);
		}else
			System.err.print("connection refused");
	}

	/* (non-Javadoc)
	 * @see peersim.edsim.EDProtocol#processEvent(peersim.core.Node, int, java.lang.Object)
	 */
	@Override
	public void processEvent(Node node, int transport, Object msg) {
		Message message = (Message)msg;
		if(message.getStatus()==Message.UPDATE){
			for (int i = 0; i < Network.size(); i++) {
				NetworkController network = (NetworkController)(Network.get(i)).getProtocol(this.network);
				network.updateAllConnections();				
			}
			EDSimulator.add(this.update_time, msg, node, transport);
		}
	}
//	public void addTx(long tx){
//		this.tx+=tx;
//	}
//	public void addRx(long rx){
//		this.rx+=rx;
//	}
//	public void resetTx(){
//		this.tx=0L;
//	}
//	public void resetRx(){
//		this.rx=0L;
//	}
//	public long getTx(){
//		return this.tx;
//	}
//	public long getRx(){
//		return this.rx;
//	}
	public void bootstrap(
			long bwd, long ts, int application, 
			int transport, 
			int network, long update_time,
			Node node){
		this.bwd=bwd;
		this.ts=ts;
		this.application=application;
		this.uploads=new Hashtable<Long, Message>();
		this.downloads=new Hashtable<Long, Message>();
		this.transport=transport;
		this.network=network;
		this.update_time=update_time;
		this.last_update=0L;
		this.node=node;
		this.last_tx=0L;
		this.last_rx=0L;
		this.tx=0L;
		this.rx=0L;
		this.transient_tx=0L;
	}
	
	
	public int getActiveFlows(){
		return this.uploads.size()+this.downloads.size();
	}
	/**
	 * send: main send method
	 * 
	 * this sends information from the local node
	 * to a dst node
	 * 
	 * @param dst destination node id
	 * @param msg message
	 * 
	 * @return connection identifier
	 */
	public long send(Message msg){
		NetworkController network = (NetworkController)(msg.getSrc()).getProtocol(this.network);
			
		long eedelay = 0L;
        long connection_id = network.send(msg.getSrc(), msg.getDst(), msg.getLength(), eedelay,msg,msg.getSrc().getID(),msg.getDst().getID(), msg.getDeadline());
		//check if connection id is valid
        //if it is invalid return transport error code
        if(connection_id==NetworkController.ERROR_CODE)
			return -1;
		else {
	        return connection_id;
		}
	}

	//net interface
    public void processConnEnd (long connection_id){
    	//to implement
    	//compute message only in the destination (download)
    	if(this.downloads.containsKey(new Long(connection_id))){
    		Message message = this.downloads.remove(new Long(connection_id));
    		Transport src_net = (Transport)((message).getSrc()).getProtocol(this.transport);
//    		Transport dst_net = (Transport)((message).getDst()).getProtocol(this.transport);
    		src_net.removeUpload(connection_id);
    		src_net.addTransientTx(message.getLength()-message.getTransientTx());
    		App app = (App)(message.getDst()).getProtocol(this.application);
    		app.receiveData(message);
    		app = (App)(message.getSrc()).getProtocol(this.application);
    		app.addTx(message.getLength());
//    		src_net.addTx(message.getLength());
//    		dst_net.addRx(message.getLength());
    	}
    }
    public void processFailedConn (long connection_id){
    	
    }
    public void processDeletedConn (long connection_id){
    	
    }
    public void processInConn (long connection_id, long src, long dest, Object content){
    	
    }
    public void processConnAck (long connection_id, long end){
    	
    }
    public long getRequesterId(long connection_id){
    	return 0;
    }
    public void adjustDeadline(long connection_id, long new_deadline){
    	
    }

    public long getTx(){
		long result = 0L;
    	result=this.tx;
    	if(this.last_update<CommonState.getTime()){
	    	long tx = 0L;
			NetworkController network = (NetworkController)(this.node).getProtocol(this.network);
			network.updateAllConnections();
			Enumeration<Long> c_ids = this.uploads.keys();
			Long key=null;
			long ttx=0;
			Message msg=null;
			while (c_ids.hasMoreElements()) {
				key=c_ids.nextElement();
				ttx=network.getTb(key.longValue());
				msg=((Message)this.uploads.get(key));
				tx+=ttx-msg.getTransientTx();
				msg.setTransientTx(ttx);
			}
	    	result=tx+this.transient_tx;
	    	this.transient_tx=0L;
	    	this.tx=result;
    		this.last_update=CommonState.getTime();
    	}
    	return result;
    }
    
    public void addTransientTx(long tx){
    	this.transient_tx+=tx;
    }
    
    public void removeUpload(long c_id){
		this.uploads.remove(new Long(c_id));   	
    }
	
}
