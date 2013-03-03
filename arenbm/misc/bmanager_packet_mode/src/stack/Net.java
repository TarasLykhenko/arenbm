/**
 * 
 */
package stack;

import java.util.ArrayList;
import java.util.List;

import peersim.core.CommonState;
import peersim.core.Node;
import peersim.core.Protocol;
import peersim.edsim.EDProtocol;
import peersim.edsim.EDSimulator;
import stack.Message;;

/**
 * @author guthemberg
 *
 */
public class Net implements EDProtocol, Protocol {

	int app_protocol;
	long bwd=0;
	long ts=0;
	int slots=0;
	List<Message> queue;
	int uploads;
	int downloads;
	long tx;
	long chunk_size;
	/**
	 * 
	 */
	public Net(String prefix) {
		// TODO Auto-generated constructor stub
		this.queue=new ArrayList<Message>();
		this.uploads=0;
		this.downloads=0;
		this.tx=0;
	}
 
	public Object clone() {
        Net local = null;
        try {
        	local= (Net) super.clone();
        } catch (CloneNotSupportedException e) {
        	System.err.println("Unexpected error in:"+this.toString());
        } 
		local.queue=new ArrayList<Message>();
		local.uploads=0;
		local.downloads=0;
    	return local;
    }

	public void send(Node src,Node dst, int network,Message msg){
		if(!this.hasSent(msg, network)){
			((Net)(msg.getSrc()).getProtocol(network)).queue(msg);
		}
//	   msg.setToQueued();
//	   EDSimulator.add(delay, msg, src, network);		
	}

	/*public void connect(Message message,int network){
		Net dst_net = (Net)(message.getDst()).getProtocol(network);
		Net src_net = (Net)(message.getDst()).getProtocol(network);
		dst_net.incrementDownloads(message.getNChunks());
		src_net.incrementUploads(message.getNChunks());
		message.setToSent();
		long duration = ((message.getChunkSize())/(this.getSlotBwd())) * this.ts;
		EDSimulator.add(duration, message, message.getDst(), network);		
	}*/
	public void connect(Net src, Net dst, int network, Message message){
		message.setToSent();
		src.addtUpload(message.getChunks());
		dst.adddDownload(message.getChunks());
		long duration = this.ts;
		EDSimulator.add(duration, message, message.getDst(), network);		
	}
	public boolean hasSent(Message msg, int network){		
		Net src_net = (Net)(msg.getSrc()).getProtocol(network);
		Net dst_net = (Net)(msg.getDst()).getProtocol(network);
		int src_available_slots=src_net.getAvailableUpBwdSlots();
		int dst_available_slots=dst_net.getAvailableDownBwdSlots();
		if((src_available_slots==0)||(dst_available_slots==0)){
			return false;
			//src_net.queue(msg);
		} else {
			int free_slots = Math.min(dst_available_slots, src_available_slots);
			int r_chunks = msg.getRemainingChunks();
			int chunks_to_be_sent = Math.min(free_slots, r_chunks);
			if(chunks_to_be_sent<r_chunks){
				msg.updateChunks(chunks_to_be_sent);
				Message qmsgs = (Message)msg.clone();
				src_net.queue(qmsgs);
				src_net.connect(src_net, dst_net, network, msg);
			} else {
				msg.updateChunks(chunks_to_be_sent);
				src_net.connect(src_net, dst_net, network, msg);
			}
			return true;
//			/*msg.setSlots(slots);
//			this.connect(src_net, dst_net, network, msg);
//			return true;*/
//			if(slots>=msg.getNChunks()){
//				int min_slots = Math.min(slots, msg.getNChunks());
//				msg.setSlots(min_slots);
//				this.connect(src_net, dst_net, network, msg);
//				return true;
//			} else {
//				int partition = msg.getNChunks()/slots;
//				int remaining= msg.getNChunks();
//				Message n_message = (Message)msg.clone();
//				int r_chunks = msg.updateNChunks(partition);
//				remaining-=partition;
//				msg.setSlots(1);
//				this.connect(src_net, dst_net, network, msg);
//				while (r_chunks>0) {
//					type type = (type) en.nextElement();
//					
//				}
//				for (int i = 1; i < slots; i++) {
//					if(i==(slots-1))
//						partition=remaining;
//					n_message = (Message)msg.clone();
//				}
//				Message n_message = (Message)msg.clone();
//				int r_chunks = msg.updateNChunks(slots);
//				msg.setSlots(slots);
//				this.connect(src_net, dst_net, network, msg);
//				n_message.updateNChunks(r_chunks);
//				src_net.queue(n_message);
//				return true;
//			}
		}
	}
	/* (non-Javadoc)
	 * @see peersim.edsim.EDProtocol#processEvent(peersim.core.Node, int, java.lang.Object)
	 */
	@Override
	public void processEvent(Node node, int network, Object msg) {
		Message message = (Message)msg;
		if(message.getStatus()==Message.QUEUED){
			this.send(message.getSrc(), message.getDst(), network, message);
		} else if(message.getStatus()==Message.SENT){
			Net src_net = (Net)((message).getSrc()).getProtocol(network);
			Net dst_net = (Net)((message).getDst()).getProtocol(network);
			src_net.removeUpload(message.getChunks());
			dst_net.removeDownload(message.getChunks());
			src_net.addTx(message.getLength());
			//System.out.println("("+CommonState.getTime()+"): "+message);
			src_net.retry(network);
			if(message.getRemainingChunks()==0){
				App app = (App)(message.getDst()).getProtocol(this.app_protocol);
				app.receiveData(message);
			}
		}

	   
		// TODO Auto-generated method stub
	}
	public void bootstrap(long bwd, long ts, 
			int app_protocol, 
			long chunk_size){
		this.bwd=bwd;
		this.ts=ts;
		this.app_protocol=app_protocol;
		this.queue=new ArrayList<Message>();
		this.uploads=0;
		this.downloads=0;
		this.tx=0L;
		this.chunk_size=chunk_size;
		this.slots=(((int)bwd)/((int)chunk_size));
	}
	
	public long getSlotBwd(){
		return (this.bwd/this.slots);
	}
	
	public int getQueueSize(){
		return this.queue.size();
	}

	public void queue(Message msg){
		this.queue.add(msg);
	}

	public Message removeQueuedMessage(){
		return this.queue.remove(0);
	}

	public Message getFirstMessage(){
		if(this.queue.size()>0)
			return this.queue.get(0);
		else 
			return null;
	}

	public boolean hasAvailableUploadBwd(){
		return this.uploads<this.slots;
	}
	public boolean hasAvailableDownloadBwd(){
		return this.downloads<this.slots;
	}
	public void addtUpload(int slots){
		this.uploads+=slots;
	}
	public void adddDownload(int slots){
		this.downloads+=slots;
	}
	public void removeUpload(int slots){
		this.uploads-=slots;
	}
	public void removeDownload(int slots){
		this.downloads-=slots;
	}
	public int getAvailableDownBwdSlots(){
		return this.slots-this.downloads;
	}
	public int getAvailableUpBwdSlots(){
		return this.slots-this.uploads;
	}
	
	public void addTx(long tx){
		this.tx+=tx;
	}
	public long getTx(){
		return this.tx;
	}
	
	public void resetTx(){
		this.tx=0L;
	}
	
	public void retry(int network){
		int loop = this.getQueueSize();
		int idle_slots=this.getAvailableUpBwdSlots();
		int busy_slots=0;
		while (loop>0&&this.hasAvailableUploadBwd()) {
			Message qmsg = this.removeQueuedMessage();
			if(this.hasSent(qmsg, network)){
				busy_slots+=qmsg.getChunks();
				if(busy_slots==idle_slots)
					break;
			} else {
				this.queue(qmsg);
			}
			loop--;
		}		
	}
	
}
