/**
 * 
 */
package common.net;


/**
 * This class provides the common primitives for network
 * communication between any two nodes' layers
 * 
 * @author guthemberg
 *
 */
public class Message {


	/**
	 * Common header size
	 * 
	 * This size is based on a pseudo ipv6 header size
	 */
	public static final long NETWORK_HEADER_SIZE=96;
	/**
	 * Common header size
	 * 
	 * This size is based on a pseudo udp header size
	 */
	public static final long DATAGRAM_HEADER_SIZE=64;
	
	/**
	 * Common protocol values 
	 */

	/**
	 * Generic application protocol layer 
	 */
	public static final int APPLICATION_PROTOCOL=2;

	/**
	 * Generic application protocol layer 
	 */
	public static final int TRANSPORT_PROTOCOL=3;

	/**
	 * Generic unknown protocol layer 
	 */
	public static final int UNKNOWN_PROTOCOL=4;

	/**
	 * All known known protocols
	 */
	public static final int[] MESSAGE_PROTOCOLS = {APPLICATION_PROTOCOL,TRANSPORT_PROTOCOL};

	/**
	 * Message protocol
	 */

	private int protocol;

	/**
	 * when this message was created
	 */

	protected long timestamp;
	
	/**
	 * Source node who has created this
	 */
	protected long src_id;
	
	/**
	 * Recipient node identifier
	 */
	protected long dst_id;
	
	/**
	 * Payload/content length in bits.
	 * 
	 * It is always defined by the user
	 * regardless the type of the message
	 */
	private long clength; 

	/**
	 * Header length in bits.
	 * 
	 * It is always defined by the user
	 * regardless the type of the message
	 */
	private long hlength; 
	
	/**
	 * Put message content here!
	 */
	private Object content;
	
	/**
	 * New message needs 
	 *   -> now: when this message was created
	 *   -> a type (check valid known types 
	 *   in Common.network.Message.MESSAGE_TYPES)
	 *   -> src,dst node identifiers, this must be 
	 *   meaningful for both end-to-end communication 
	 *   and local layer client-server layers 
	 *   communication
	 *   -> payload is likely to the the object size 
	 *   -> obj message content
	 *   @param clength content length
	 *   @param hlength header length
	 * 
	 */
	public Message(long src_id, long dst_id, int protocol, long hlength, long clength, Object content, long now) {
		this.timestamp = now;
		this.src_id = src_id;
		this.dst_id = dst_id;
		this.clength = clength;
		this.content = content;
				
		this.protocol=protocol;
		/*for (int i = 0; i < Message.MESSAGE_PROTOCOLS.length; i++) {
			if (protocol==Message.MESSAGE_PROTOCOLS[i]) 
				this.protocol = protocol;
		}*/
		//we assume that this message has a datagram-like header by default
		this.hlength = hlength;
	}

	/**
	 * common get operations
	 */


	/**
	 * get message upper-layer protocol
	 */
	public int getProtocol(){
		return this.protocol;
	}

	/**
	 * who sends
	 */
	public long getSrcID(){
		return this.src_id;
	}

	/**
	 * who receives message
	 */
	public long getDstID(){
		return this.dst_id;
	}

	/**
	 * Payload length in bits
	 */
	public long getPayloadLength(){
		return this.clength;
	}

	/**
	 * Header length in bits
	 */
	public long getHeaderLength(){
		return this.clength;
	}

	/**
	 * getSize: payload plus headers size in bits
	 */
	public long getLength(){
		return this.clength+this.hlength;
	}

	/**
	 * get message's content
	 */
	public Object getContent(){
		return this.content;
	}

	/**
	 * returns the message object
	 * 
	 * this is just a wrapper function
	 * to common.netwotk.Message.getObject()
	 */
	public Object getPayload(){
		return this.getContent();
	}
	
	/**
	 * known when this message was created
	 */
	public long getTimeStamp(){
		return this.timestamp;
	}

}
