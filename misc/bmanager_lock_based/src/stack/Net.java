/**
 * 
 */
package stack;

import java.util.ArrayList;
import java.util.List;

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
	boolean uploads;
	boolean downloads;
	long tx;
	/**
	 * 
	 */
	public Net(String prefix) {
		// TODO Auto-generated constructor stub
		this.queue=new ArrayList<Message>();
		this.uploads=true;
		this.downloads=true;
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
		local.uploads=true;
		local.downloads=true;
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
		src.locktUpload();
		dst.lockDownload();
		long duration = (long)((((double)message.getLength())* ((double)this.ts))/((double)this.getBwd())) ;
		EDSimulator.add(duration, message, message.getDst(), network);		
	}
	public boolean hasSent(Message msg, int network){		
		Net src_net = (Net)(msg.getSrc()).getProtocol(network);
		Net dst_net = (Net)(msg.getDst()).getProtocol(network);
		boolean src_available=src_net.isAvailableForUpload();
		boolean dst_available=dst_net.isAvailableForDownload();
		if((!src_available)||(!dst_available)){
			return false;
			//src_net.queue(msg);
		} else {
			msg.setSlots(1);
			this.connect(src_net, dst_net, network, msg);
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
			src_net.unlockUpload();
			dst_net.unlockDownload();
			src_net.addTx(message.getLength());
			src_net.retry(network);
			App app = (App)(message.getDst()).getProtocol(this.app_protocol);
			app.receiveData(message);
		}

	   
		// TODO Auto-generated method stub
	}
	public void bootstrap(long bwd, long ts, int app_protocol){
		this.bwd=bwd;
		this.ts=ts;
		this.app_protocol=app_protocol;
		this.queue=new ArrayList<Message>();
		this.uploads=true;
		this.downloads=true;
		this.tx=0L;
	}
	
	public long getBwd(){
		return (this.bwd);
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

	public boolean isAvailableForUpload(){
		return this.uploads;
	}
	public boolean isAvailableForDownload(){
		return this.downloads;
	}
	public void locktUpload(){
		this.uploads=false;
	}
	public void lockDownload(){
		this.downloads=false;
	}
	public void unlockUpload(){
		this.uploads=true;
	}
	public void unlockDownload(){
		this.downloads=true;
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
		while (loop>0&&this.isAvailableForUpload()) {
			Message qmsg = this.removeQueuedMessage();
			if(this.hasSent(qmsg, network)){
				break;
			} else {
				this.queue(qmsg);
			}
			loop--;
		}		
	}
	
}
