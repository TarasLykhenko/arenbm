/**
 * 
 */
package stack;

import peersim.core.CommonState;
import peersim.core.Node;

/**
 * @author guthemberg
 *
 */
public class Message {

    protected final static byte QUEUED = 1;
    protected final static byte SENT = 2;

    long id;
	Node src;
	Node dst;
	byte status;
	int slots;
	long start;
	long length;
	long tx;
	/**
	 * 
	 */
	public Message(Node src, Node dst,long id, long length) {
		// TODO Auto-generated constructor stub
		this.src=src;
		this.dst=dst;
		this.id=id;
		this.length=length;
    	this.status=Message.QUEUED;
    	this.slots = 0;
    	this.start=CommonState.getTime();
    	this.tx=0;
	}
    public Object clone() {
        Message msg = null;
        try {
        	msg = new Message(this.getSrc(), this.getDst(), this.getId(), this.length);
        	msg.start=this.start;
        	msg.tx=this.tx;
        } catch (Exception e) {
        	System.err.println("Unexpected error in:"+this.toString());
        	e.printStackTrace();
        	System.exit(-1);
        } 
    	return msg;
    }

	public Node getSrc(){
		return this.src;
	}
	public Node getDst(){
		return this.dst;
	}
	public long getId(){
		return this.id;
	}
	
    public boolean equals(Object msg) {
    	Message message= (Message)msg;
    	if((message.getId()==this.getId()))
    		return true;
    	else
    		return false;
    	
    }

    /**
     * hashCode: is this object identifier.
     * 
     * @return hash code of the connection id.
     */
    public int  hashCode(){
    	return Long.valueOf(this.id).hashCode();    	
    }
    
    public void setToSent(){
    	this.status=Message.SENT;
    }
    public void setToQueued(){
    	this.status=Message.QUEUED;
    }
    
    public byte getStatus(){
    	return this.status;
    }
 
    public void setSlots(int slots){
    	this.slots=slots;
    }
    public int getSlots(){
    	return this.slots;
    }
    
    public long getLength(){
    	return this.length;
    }
    
    public long getStart(){
    	return this.start;
    }
    public String toString(){
    	return "id: "+this.id+", src: "+this.getSrc().getID()+", dst: "+this.getDst().getID()+", length: "+this.length;
    }
    
}
