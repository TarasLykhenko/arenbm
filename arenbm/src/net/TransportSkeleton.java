package net;


/**
 * This interface defines the behaviour of the communication 
 * between transport layer and the direct uplayer (the client
 * layer or caller)
 * 
 */

public interface TransportSkeleton {

	/**
	 * Notifies uplayer caller when the connection is 
	 * accomplished
	 * 
	 * @param connection_id that just ends
	 */
    public void processConnEnd (long connection_id);
    
    
	/**
	 * Notifies uplayer caller if a connection cancelled
	 * either refused or failed
	 * 
	 * @param connection_id that just fails.
	 */
    public void processFailedConn (long connection_id);
    public void processDeletedConn (long connection_id);


    /**
	 * Notifies uplayer caller the arrival of a new incoming connection
	 * 
	 * @param connection_id that just fails.
	 */
    public void processInConn (long connection_id, long src, long dest, Object content);

    /**
	 * Notifies upper-layer the acknowledgement of a new
	 * connection request
	 * 
	 * @param connection_id that just fails.
	 */
    public void processConnAck (long connection_id, long end);

	/**
	 * gets the reservation requester, either
	 * source or destination id.
	 * 
	 * Assuming that transfers/flows are created
	 * from sources, this have to be called 
	 * from sources side only.
	 * 
	 * @param long connections identifier.
	 * 
	 * @return reservation requester id, or 
	 * NetworkController.ERROR_CODE if
	 * requester is unknown or invalid
	 */
    public long getRequesterId(long connection_id);

    /**
	 * fix the deadline of a request.
	 * 
	 * this is mainly because there is
	 * not enough resources on the requester
	 * side in order to fulfil his own
	 * reservation request
	 * 
	 * @param connection_id connections identifier.
	 * @param new_deadline new dealine.
	 * 
	 */
    public void adjustDeadline(long connection_id, long new_deadline);

}
