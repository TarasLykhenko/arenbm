package net;

import java.util.Enumeration;
import java.util.Hashtable;

import common.net.Message;

import peersim.core.Node;

/**
 * Cointains the messages used in the bandwidth management protocol and
 * the error codes for data trasnfer.
 *
 * @author Alessandro Russo
 * @version $Revision: 0.02$
 */


public class NetworkMessage extends Message {

    /**
     * Sender node
     */
    protected final Node src_node;

    

    /**
     * Receiver node
     */
    protected final Node dst_node;

    /**
     * re_send_counter.
     */
    protected int re_send_counter;
 
    /**
     * Message for updating upload, when pending upload is present
     */
    protected final static byte UPD_UP = 10;

    /**
     * Message to balance downloads
     */
    //protected final static byte BALANCE_DOWNLOAD = 20;
    /**
     * Message to balance downloads
     */
    //protected final static byte BALANCE_UPLOAD = 30;
    /**
     * Message to send request
     */
    protected final static byte SEND = 40;
    /**
     * Message send ok!!!
     */
    //protected final static byte SEND_ACCEPTED = 41;
    /**
     * Message to send request
     */
    protected final static byte SEND_REFUSED = 45;
    /**
     * Message to re-send request, after failures
     */
    //protected final static byte RESEND_REQUEST = 46;
    /**
     * control bandwidth failure notification. It is 
     * related errors of BWD_CONTROL option
     * 
     * it happens when a minimum bwd was requested and
     * there was not enough resource for proper 
     * allocation
     */
    protected final static byte BWD_FAILED = 47;
    /**
     * requester not found
     * 
     * it might be useful when a flow is 
     * cancelled for any reason
     */
    protected final static byte REQ_NOTFOUND = 48;
    protected final static byte NO_RESOURCES = 49;
    protected final static byte OK = 50;
    protected final static byte ERROR = 51;
    /**
     * Message to send request
     */
    //protected final static byte RETRIVE_REQUEST = 50;
    /**
     * Message to send request
     */
    //protected final static byte TRANSFER_ACCOMPLISHED = 60;
    /**
     * connection closed
     */
    //protected final static byte CONNECTION_CLOSED = 90;
    /**
     * Message for updating download, when pending download is present
     */
    protected final static byte SEND_FIN = 100;
    /**
     * Message used to notificate no upload bandwidth
     */
    //public final static byte NO_UP = -1;
    /**
     * Message used to notificate no download bandwidth
     */
    //public final static byte NO_DOWN = -2;

    


    //keep state of requests of this message
	/*
	 * SEND_REQUEST
	 * SEND_ACCEPTED
	 * SEND_REFUSED
	 * ??? TRANSFER_ACCOMPLISHED
	 * UPD_DOWN ACTIVE! END
	 */
	//connections tables
	private Hashtable<Long, Connection> send_requests;
	//private Hashtable<Long, Connection> refused_sends;
	//private Hashtable<Long, Connection> bwd_failed_sends;
	private Hashtable<Long, Connection> end_sends;
	private boolean control;
	

    /**
     * NetworkMessage is a default constructor.
     * 
     * @param source source (peersim) node.
     * @param destination destination (peersim) node.
     * @param message_type network message type
     * as defined by NetworkMessage constants.
     * @param clength content length in bits.
     * @param end_to_end_delay end to end delay
     * in milliseconds
     * @param uplayer_id uuper layer identifier
     * , it is useful for callback methods that
     * performs procedures such as process
     * end connection, new incoming connections
     * or failures
     * @param content with the upper layer content
     * @param conn_id with a unique connection 
     * identifier
     * @param bd_id brodcast domain id
     * @param deadline deadline time
     */

    public NetworkMessage(long ts, Node source, 
    		Node destination) {
    	//for changing header values, please check
    	//Message.* constants
    	super(source.getID(), 
    			destination.getID(), 0, 0L,
    			0L, null, ts);
        this.src_node = source;
        this.dst_node = destination;
        //this.transport_id=transport_id;
		//we assume that this message has a datagram-like header by default
		//this.header_len_in_bits = Message.DATAGRAM_HEADER_SIZE;
        this.re_send_counter=0;
        this.send_requests= new Hashtable<Long, Connection>();
    	//this.refused_sends= new Hashtable<Long, Connection>();
    	this.end_sends= new Hashtable<Long, Connection>();
    	//this.bwd_failed_sends = new Hashtable<Long, Connection>();
    	this.control=false;
    }

	/**
	 * 
	 * clone: useful for recreate network messages.
	 * 
	 * IMPORTANT TIP:
	 * It is essential to recreates a new network 
	 * message before re-sending it through peersim
	 *
	 * 
	 */
    
	public Object clone() {
        NetworkMessage new_msg = new NetworkMessage(this.timestamp,this.src_node, this.dst_node);		
        return new_msg;
    }
	

    /**
     * Get the sender node.
     * @return Sender node.
     */
    public Node getSrc() {
        return this.src_node;
    }

    /**
     * Get the receiver node.
     * @return Receiver node.
     */
    public Node getDst() {
        return this.dst_node;
    }



    //public void setConnectionIdentifier(long connection_id) {
    //	this.connection_id=connection_id;
    //}
    
    public int getResendCounter(){
    	return this.re_send_counter;
    }
    public void setResendCounter(int counter){
    	this.re_send_counter=counter;
    }
    /**
     * Check if the Network message element is the same of the one given or not.
     * @param o object.
     * @return True if they are the same, false otherwise.
     */
    public boolean equals(Object o) {
    	NetworkMessage msg = null;
    	if(o==null) return false;
		try {
	    	msg = (NetworkMessage)o;
		} catch (ClassCastException e) {
			return false;
		} 
    	if((msg.getSrc()==this.src_node)&&(msg.getTimeStamp()==this.timestamp))
    		return true;
    	else
    		return false;
    	
    }
    /**
     *Printable version of bandwidth message.
     * @return String containing labels and values of current bandwidth message.
     */
    public String toString() {
        return "Sender " + this.src_node.getID() + " | Receiver " + this.dst_node.getID() + " | Timestamp " + this.timestamp + ".";
    }
    
    public Connection removeConnection(byte event, Connection c){
    	if((event==NetworkMessage.SEND)){
    		//send request SYN
    		return this.removeSendRequest(c);
    	}else if(event==NetworkMessage.SEND_FIN){
    		//accepted and on going connection
    		//connection end
    		return this.removeEndSend(c);
    	}/*else if(event==NetworkMessage.BWD_FAILED){
    		return this.removeBwdFailedSend(c);
    	}else if(event==NetworkMessage.SEND_REFUSED){
    		return this.removeRefusedSend(c);
    	}*/
    	return null;
    }
    

    public Connection removeConnection(long cid){
    	Connection removed = this.end_sends.remove(Long.valueOf(cid));
    	if(removed!=null)
    		return removed;
		removed = this.send_requests.remove(Long.valueOf(cid));
    	if(removed!=null)
    		return removed;
		/*removed = this.bwd_failed_sends.remove(Long.valueOf(cid));
    	if(removed!=null)
    		return removed;
		removed = this.refused_sends.remove(Long.valueOf(cid));*/
		return removed;
    }

    /**
     * puts a connections to a message. it is about
     * a event that happens in the timestamp
     * of this message (a way to gathering and 
     * treating messages in correct order)
     * 
     * by now, it is able to handle the following 
     * types of messages:
     * NetworkMessage.SEND_REQUEST, 
     * NetworkMessage.SEND_REFUSED,
     * and NetworkMessage.UPD_DOWN
     * 
     * @param event type of event (check valid types
     * 			here above
     * @param c connection concerned by this event
     * 
     * @return true if the connection was successfully 
     * added
     */
    public boolean putConnection(byte event, Connection c){
    	if((event==NetworkMessage.SEND)){
    		//send request, SYN
    		if(this.putSendRequest(c)==null)
    			return true;
    	}else if(event==NetworkMessage.SEND_FIN){
    		//connection accepted and on going
    		//connection end, FIN
    		if(this.putEndSend(c)==null)
    			return true;    		
    	}/*else if(event==NetworkMessage.BWD_FAILED){
    		if(this.putBwdFailedSend(c)==null)
    			return true;    		
    	}else if(event==NetworkMessage.SEND_REFUSED){
    		if(this.putRefusedSend(c)==null)
    			return true;    		
    	}*/
    	return false;
    }
    
    public Enumeration<Connection> getConnections(byte event){
    	if(event==NetworkMessage.SEND){
    		//accepted and on going connections
    		return this.send_requests.elements();
    	}else if(event==NetworkMessage.SEND_FIN){
    		//connection end
    		return this.end_sends.elements();
    	}/*else if(event==NetworkMessage.BWD_FAILED){
    		return this.bwd_failed_sends.elements();
    	}else if(event==NetworkMessage.SEND_REFUSED){
    		return this.refused_sends.elements();
    	}*/
    	return null;
    }
/*
 *         this.send_requests= new Hashtable<Long, Connection>();
        this.accepted_sends= new Hashtable<Long, Connection>();
    	this.refused_sends= new Hashtable<Long, Connection>();
    	this.end_sends= new Hashtable<Long, Connection>();
    
 */
    //removes
    public Connection removeSendRequest(Connection c){
    	return this.send_requests.remove(Long.valueOf(c.getId()));
    }
    /*public Connection removeRefusedSend(Connection c){
    	return this.refused_sends.remove(Long.valueOf(c.getId()));
    }
    public Connection removeBwdFailedSend(Connection c){
    	return this.bwd_failed_sends.remove(Long.valueOf(c.getId()));
    }*/
    public Connection removeEndSend(Connection c){
    	return this.end_sends.remove(Long.valueOf(c.getId()));
    }
    //puts
    public Connection putSendRequest(Connection c){
    	return this.send_requests.put(Long.valueOf(c.getId()),c);
    }
    /*public Connection putRefusedSend(Connection c){
    	return this.refused_sends.put(Long.valueOf(c.getId()),c);
    }
    public Connection putBwdFailedSend(Connection c){
    	return this.bwd_failed_sends.put(Long.valueOf(c.getId()),c);
    }*/
    public Connection putEndSend(Connection c){
    	return this.end_sends.put(Long.valueOf(c.getId()),c);
    }
    
    /*
	private Hashtable<Long, Connection> send_requests;
	private Hashtable<Long, Connection> accepted_sends;
	private Hashtable<Long, Connection> refused_sends;
	private Hashtable<Long, Connection> end_sends;
     */
    public Enumeration<Connection> getSendRequests(){
    	return this.send_requests.elements();
    }
    /*public Enumeration<Connection> getRefusedSends(){
    	return this.refused_sends.elements();
    }
    public Enumeration<Connection> getBwdFailedSends(){
    	return this.bwd_failed_sends.elements();
    }*/
    public Enumeration<Connection> getEndSends(){
    	return this.end_sends.elements();
    }
    public void setControl(){
    	this.control=true;
    }
    public boolean isControl(){
    	return this.control;
    }
}
