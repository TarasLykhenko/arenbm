/**
 * 
 */
package stack;

import peersim.core.Node;

/**
 * @author guthemberg
 *
 */
public class Message {

	Node src;
	Node dst;
	long length;
	/**
	 * 
	 */
	public Message(Node src, Node dst, long length) {
		this.src=src;
		this.dst=dst;
		this.length=length;
	}
	public Node getSrc(){
		return this.src;
	}
	public Node getDst(){
		return this.dst;
	}
	public long getLength(){
		return this.length;
	}
}
