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
	long start;
	long length;
	long tx;
	int chunks;
	long chunk_size;
	long rest;
	int r_chunks;
	/**
	 * 
	 */
	public Message(Node src, Node dst,long id, 
			long length, long chunk_size) {
		// TODO Auto-generated constructor stub
		this.src=src;
		this.dst=dst;
		this.id=id;
		this.length=length;
    	this.status=Message.QUEUED;
    	this.start=CommonState.getTime();
    	this.tx=0;
    	this.chunk_size = chunk_size;
    	this.chunks=this.computeChunks(length,chunk_size);
    	this.r_chunks=this.chunks;
    	this.rest=length-((this.chunks-1)*chunk_size);
	}

	public Object clone() {
        Message msg = null;
        try {
        	msg = new Message(this.getSrc(), this.getDst(), 
        			this.getId(), this.length, this.chunk_size);
        	msg.start=this.start;
        	msg.tx=this.tx;
        	msg.r_chunks=this.r_chunks;
        	msg.chunks=this.r_chunks;
        	msg.rest=this.rest;
        } catch (Exception e) {
        	System.err.println("Unexpected error in:"+this.toString());
        	e.printStackTrace();
        	System.exit(-1);
        } 
    	return msg;
    }

	public int computeChunks(long length, long chunk_size){
		int entire_chunks = (int)(length/chunk_size);
		if((entire_chunks*chunk_size)==length)
			return entire_chunks;
		else
			return entire_chunks+1;
	}	

    public long getLength(){
    	if(this.r_chunks==0)
    		return ((this.chunks-1)*this.chunk_size)+this.rest;
    	else{
    		long length = (((long)this.chunks)*this.chunk_size);
    		return length;
    	}
    }
    public long getMessageLength(){
    	return ((this.length));
    }
    public int updateChunks(int chunks){
    	this.r_chunks-=chunks;
    	this.chunks=chunks;
    	return this.r_chunks;
    }
    public int getChunks(){
    	return this.chunks;
    }
    public int getRemainingChunks(){
    	return this.r_chunks;
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
    public long getStart(){
    	return this.start;
    }
    public String toString(){
    	return "id: "+this.id+", src: "+this.getSrc().getID()+", dst: "+this.getDst().getID()+", length: "+this.getLength()+", c_size: "+this.chunk_size+", chunks: "+this.chunks+", r_chunks: "+this.r_chunks;
    }
    
}
