/**
 * 
 */
package stack;

import peersim.core.Node;
import peersim.core.Protocol;
import stat.Generator;

/**
 * @author guthemberg
 *
 */
public class App implements Protocol {
	Node[] servers=null;
	int npid;
	long computed_messages;
	long downloaded;
	long bwd;
	long ts;

	/**
	 * 
	 */
	public App(String prefix) {
		this.computed_messages=0L;
		this.downloaded=0L;
//		this.duration=0L;
	}
    public Object clone() {
        App local_storage = null;
        try {
        	local_storage = (App) super.clone();
        } catch (CloneNotSupportedException e) {
        	System.err.println("Unexpected error in:"+this.toString());
        } 
		local_storage.computed_messages=0;
    	return local_storage;
    }
    
    public void bootstrap(Node[] list,int npid, long bwd, long ts){
    	this.servers=list;
    	this.npid = npid;
    	this.downloaded=0L;
    	this.bwd=bwd;
    	this.ts=ts;
    }
    
    public void fetch(Node dst){
    	long length = Generator.nextLong();
    	Node src = this.servers[Generator.getRandomIndex(this.servers.length)];
    	Message msg = new Message(src,dst,length);
		Net net = (Net)(src).getProtocol(this.npid);
		net.send(src, dst, this.npid, msg);
    }
    
    public void receiveData(Message msg){
		this.computed_messages++;
		this.downloaded+=msg.getLength();
		this.fetch(msg.getDst());
    }
    public long getComputedMessages(){
    	return this.computed_messages;
    }
    public void resetComputedMessages(){
    	this.computed_messages=0L;
    }
//    public long getDuration(){
//    	return this.duration;
//    }
//    public void resetDurations(){
//    	this.duration=0L;
//    }
    public long getDownloaded(){
    	return this.downloaded;
    }
    public void resetDownloaded(){
    	this.downloaded=0L;
    }
    
}
