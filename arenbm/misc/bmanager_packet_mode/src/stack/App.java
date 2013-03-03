/**
 * 
 */
package stack;

import java.util.ArrayList;
import java.util.List;

import peersim.core.Node;
import peersim.core.Protocol;
import stat.Generator;

/**
 * @author guthemberg
 *
 */
public class App implements Protocol {
	Node[] servers=null;
	int network;
	static public int msg_id_gen=0;
	long bwd;
	long ts;
	long computed_messages;
	long downloaded;
	long chunk_size;
	
	
//	long duration;
	/**
	 * 
	 */
	public App(String prefix) {
		this.computed_messages=0L;
	}
	
	public Node[] getSources(int size){
		Node[] sources=new Node[size];
		//create all options vector
		List<Integer> options = new ArrayList<Integer>();
		for (int i = 0; i < this.servers.length; i++) {
			options.add(new Integer(i));
		}
		int i=0;
		int index=0;
		//selections
		do {
			index = Generator.getRandomIndex(options.size());
			sources[i]=this.servers[index];
			options.remove(index);
			i++;
		} while (i<size);
		return sources;
	}
    public Object clone() {
        App local_storage = null;
        try {
        	local_storage = (App) super.clone();
        } catch (CloneNotSupportedException e) {
        	System.err.println("Unexpected error in:"+this.toString());
        } 
    	return local_storage;
    }
    
    public int[] getChunksDistribution(int n_sources, int n_chunks){
    	int partition = n_chunks/n_sources;
    	int[] distribution = new int[n_sources];
    	int remaining_chunks=n_chunks;
    	for (int i = 0; i < distribution.length; i++) {
    		if(i==(distribution.length-1))
    			distribution[i]=remaining_chunks;
    		else
    			distribution[i]=partition;
    		remaining_chunks-=partition;
		}
    	return distribution;
    }

    public void fetchData(Node dst){
    	//get sources
    	//int n_chunks = (int)this.objectSizeDistrib.nextLong(App.generator);
    	//int n_chunks = Generator.getNChunks();
    	long length = Generator.nextLong();
//    	length = 1801000000;
    	//if(length>2000000L)
    	//	System.err.println("BIGGER");
    	Node[] sources = this.getSources(1);
    	//if (n_chunks==0)
    	//System.err.println("return 0 to "+App.msg_id_gen);
    	//long random_delay = 
    		//this.getRandomIndex(((int)(((this.chunk_size*2/*(this.max_chunks/)*/)/this.bwd)*this.ts)))+shitf;
    	Node src=null;
    	Net src_net = null;
    	Message msg=null;
    	for (int i = 0; i < sources.length; i++) {
			src=sources[i];
			src_net = (Net)(src).getProtocol(this.network);
			msg=new Message(src,dst, App.msg_id_gen, 
					length, this.chunk_size);
			src_net.send(src, dst, this.network, msg);
		}
		App.msg_id_gen++; 
    }
    
    public void receiveData(Message msg){
		this.computed_messages++;
		this.downloaded+=(msg.getMessageLength());
		this.fetchData(msg.dst);
    }
        
    public void bootstrap(long bwd, long ts,
    		int network,
    		Node[] list,
    		long chunk_size){
		this.bwd=bwd;
		this.ts=ts;
		this.network=network;
    	this.servers=list;
		this.computed_messages=0L;
		this.downloaded=0;
		this.chunk_size=chunk_size;

//		this.duration=0;
    }
    	
	public long getComputedMessages(){
		return this.computed_messages;
	}
	public void resetComputedMessages(){
		this.computed_messages=0L;
	}
	public long getDownloaded(){
		return this.downloaded;
	}
	public void resetDownloaded(){
		this.downloaded=0L;
	}
//	public long getDuration(){
//		return this.duration;
//	}
//	public void resetDuration(){
//		this.duration=0;
//	}
	//public int getMsgSize(){
	//	return this.messages.;
	//}
}
