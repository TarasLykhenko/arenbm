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
    protected final static byte UPDATE = 3;

    int id;
	Node src;
	Node dst;
	byte status;
	long start;
	long length;
	long transient_tx;
	/**
	 * 
	 */
	public Message(Node src, Node dst,int id, long length) {
		// TODO Auto-generated constructor stub
		this.src=src;
		this.dst=dst;
		this.id=id;
		this.length=length;
    	this.status=Message.QUEUED;
    	this.start=CommonState.getTime();
    	this.transient_tx=0L;
	}
	public static Message createUpdateMessage() {
		Message message = new Message(null, null, 0, 0);
		// TODO Auto-generated constructor stub
    	message.status=Message.UPDATE;
    	return message;
	}
    public Object clone() {
        Message msg = null;
        try {
        	msg = new Message(this.getSrc(), this.getDst(), this.getId(), this.length);
        } catch (Exception e) {
        	System.err.println("Unexpected error in:"+this.toString());
        	e.printStackTrace();
        	System.exit(-1);
        } 
    	msg.transient_tx=0L;
    	msg.start=this.start;
    	return msg;
    }

	public Node getSrc(){
		return this.src;
	}
	public Node getDst(){
		return this.dst;
	}
	public int getId(){
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
    	return Integer.valueOf(this.id).hashCode();    	
    }
    
    public void setToSent(){
    	this.status=Message.SENT;
    }
    public void setToUpdate(){
    	this.status=Message.UPDATE;
    }
    public void setToQueued(){
    	this.status=Message.QUEUED;
    }
    
    public byte getStatus(){
    	return this.status;
    }
    
    public String toString(){
    	return "id: "+this.id+", src: "+this.getSrc().getID()+", dst: "+this.getDst().getID()+", length: "+this.length;
    }
    
    public long getLength(){
    	return this.length;
    }

    public long getDeadline(){
    	return 0L;
    }
    public long getStart(){
    	return this.start;
    }
    
    public void setTransientTx(long tx){
    	this.transient_tx=tx;
    }
    public long getTransientTx(){
    	return this.transient_tx;
    }
    
}
