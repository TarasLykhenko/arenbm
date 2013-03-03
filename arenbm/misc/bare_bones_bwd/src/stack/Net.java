/**
 * 
 */
package stack;

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
	long bwd=0L;
	long ts=0L;
	/**
	 * 
	 */
	public Net(String prefix) {
	}
 
	public Object clone() {
        Net local = null;
        try {
        	local= (Net) super.clone();
        } catch (CloneNotSupportedException e) {
        	System.err.println("Unexpected error in:"+this.toString());
        } 
    	return local;
    }

	public void send(Node src,Node dst, int network, Message msg){
	   long duration = ((msg.getLength()* this.ts)/(this.bwd));
	   EDSimulator.add(duration, msg, dst, network);		
	}
	
	/* (non-Javadoc)
	 * @see peersim.edsim.EDProtocol#processEvent(peersim.core.Node, int, java.lang.Object)
	 */
	@Override
	public void processEvent(Node dst_node, int pid, Object msg) {
		Message message = (Message)msg;
		App app = (App)(dst_node).getProtocol(this.app_protocol);
		app.receiveData(message);
	}

	public void bootstrap(int application,long bwd, long ts){
		this.app_protocol=application;
		this.bwd=bwd;
		this.ts=ts;
	}

}
