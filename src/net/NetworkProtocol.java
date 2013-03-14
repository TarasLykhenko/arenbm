package net;

import java.util.Enumeration;
import java.util.Hashtable;

import peersim.core.Network;
import peersim.core.Node;
import peersim.core.CommonState;
import peersim.edsim.EDProtocol;

/**
 * Core protocol for bandwidth management system. <p>
 * This class implements a priority sharing bandwidth mechanism, 
 * which leads to give as much resource as possible to first 
 * transmission, then to the second and so on.<p>
 * The protocol provides a method which computes
 * the time needed to perform the transfer or an error code
 * if either the upload or download bandwidth is not available.
 *
 *
 * @author Alessandro Russo
 * @version $Revision: 0.02$
 */
public class NetworkProtocol extends NetworkController implements EDProtocol {


    /**
     * Main constructor
     * @param prefix String prefix in the config file.
     */
    public NetworkProtocol(String prefix) {
        super(prefix);
    }
    
    public void balanceQuickly(Connection c){
    	NetworkProtocol object_sender, object_receiver;
    	Node file_source, file_destination;
    	//connection ends on the side where there are writting
		file_source = c.getSender();
		file_destination = c.getReceiver();
		object_sender = (NetworkProtocol) file_source.getProtocol(this.getProtocolID());
		object_receiver = (NetworkProtocol) file_destination.getProtocol(this.getProtocolID());
		object_sender.balanceUploadQuickly(c.getSrcId());
		object_receiver.balanceDownloadQuickly(c.getDstId());
    }
    
    /*public void balance(Connection connection){
    	NetworkProtocol object_sender, object_receiver;
    	Node file_source, file_destination;
    	long now = CommonState.getTime();
    	//connection ends on the side where there are writting
		file_source = connection.getSender();
		file_destination = connection.getReceiver();
		object_sender = (NetworkProtocol) file_source.getProtocol(this.getProtocolID());
		object_receiver = (NetworkProtocol) file_destination.getProtocol(this.getProtocolID());
        long duration_time=0;
        if (connection!=null){
        	if (connection.getEnd()*ratio<=now) {
        		duration_time = object_receiver.getLongerDownloadTime() - now;
        		if (((duration_time>0))&&(object_receiver.isDownloadAvailable(connection.getDstId()))) {
        			object_receiver.balanceDownloadReload(duration_time, connection.getSrcId(),connection.getDstId());
        	   }
        	   duration_time = object_sender.getLongerUploadTime() - now;
        	   if (((duration_time>0))&&(object_sender.isUploadAvailable(connection.getSrcId()))){
        		   object_sender.balanceUploadReload(duration_time, connection.getSrcId(),connection.getDstId());
        	   }
        	}
        }
    	
    }*/
    		
    
    public void closeConnection(Connection connection){
    	NetworkProtocol src_network, dst_network;
    	Node src_node, dst_node;
    	long now = CommonState.getTime();
    	//connection ends on the side where there are writting
		src_node = connection.getSender();
		dst_node = connection.getReceiver();
		src_network = (NetworkProtocol) src_node.getProtocol(this.getProtocolID());
		dst_network = (NetworkProtocol) dst_node.getProtocol(this.getProtocolID());
        if (connection!=null){
        	if (connection.getEnd()<=(now)) {
        		boolean inter = (!(connection.isIntraConnected()));
        		TransportSkeleton transport = null;
        		if(dst_network.removeDownloadConnection(connection,connection.getDstId())){
        		  //connection finished in the destination
        		  //update rx/tx for destination
        		  dst_network.updateDownloadBDStatus(connection.getDstId(), inter, connection.getLoad());
        		  //let destination know about this
        		  transport = (TransportSkeleton)dst_node.getProtocol(this.getUplayerProtocolID());
        		  transport.processConnEnd(connection.getId());
        		}

        		if(src_network.removeUploadConnection(connection,connection.getSrcId())){
        		  //and notify the source on time
        		  //src_id and dst_id are not inverted here
        		  //file_destination is replace for file destination for peersim
        		  src_network.updateUploadBDStatus(connection.getSrcId(), inter, connection.getLoad());
        		  //connection finished in the source
        		  transport = (TransportSkeleton)src_node.getProtocol(this.getUplayerProtocolID());
        		  transport.processConnEnd(connection.getId());
        		}

        		connection=null;

        		/*boolean inter = (!(connection.isIntraConnected()));
        		object_receiver.removeDownloadConnection(connection,connection.getDstId());
        		object_sender.removeUploadConnection(connection,connection.getSrcId());
        		TransportSkeleton connection_event = (TransportSkeleton)file_destination.getProtocol(this.getUplayerProtocolID());        		
				//connection finished in the destination
				//update rx/tx for destination
				long length=0L;
				int acc_conn=0;
				BroadcastDomain dst_bd = object_receiver.getBroadcastDomain(Long.valueOf(connection.getDstId()));
				if(dst_bd!=null){
					length=dst_bd.getRx();
					acc_conn=dst_bd.getAccNDownloads();
					dst_bd.setRx((length+connection.getLoad()));
					dst_bd.setAccNDownloads((acc_conn+1));
				}
				//update host domain if inter connected
				if(inter){
					BroadcastDomain dst_host_bd = object_receiver.getBroadcastDomain(BroadcastDomain.HOST_BD_ID);
					if((dst_host_bd!=null)&&(dst_host_bd!=dst_bd)){
						length=dst_host_bd.getRx();
						acc_conn=dst_host_bd.getAccNDownloads();
						dst_host_bd.setRx((length+connection.getLoad()));
						dst_host_bd.setAccNDownloads((acc_conn+1));
					}
				   
				}
				//let destination know about this
		    	start_update = System.currentTimeMillis();

				connection_event.processConnEnd(connection.getId());
				this.addNotifyCloseConn(System.currentTimeMillis()-start_update);
				//and notify the source on time
				//src_id and dst_id are not inverted here
				//file_destination is replace for file destination for peersim
				connection_event = (TransportSkeleton)file_source.getProtocol(this.getUplayerProtocolID());
				BroadcastDomain src_bd = object_sender.getBroadcastDomain(Long.valueOf(connection.getSrcId()));
				length=0L;
				acc_conn=0;
           		if(src_bd!=null){
           			length=src_bd.getTx();
           			acc_conn=src_bd.getAccNUploads();
           			src_bd.setTx((length+connection.getLoad()));
           			src_bd.setAccNUploads((acc_conn+1));
           		}
           		inter = (!(connection.getSender().getID()==connection.getReceiver().getID()));
				if(inter){
					BroadcastDomain src_host_bd = object_sender.getBroadcastDomain(BroadcastDomain.HOST_BD_ID);
					if((src_host_bd!=null)&&(src_host_bd!=src_bd)){
						length=src_host_bd.getTx();
						acc_conn=src_host_bd.getAccNUploads();
						src_host_bd.setTx((length+connection.getLoad()));
						src_host_bd.setAccNUploads((acc_conn+1));
					}
				}
    			//connection finished in the destination
		    	start_update = System.currentTimeMillis();
    			connection_event.processConnEnd(connection.getId());
				this.addNotifyCloseConn(System.currentTimeMillis()-start_update);
    			connection=null;
        		*/
        	}
        }
    	
    }
    /**
     * receiveData: computes incoming SEND_REQUEST
     * messages in the destination
     * 
     * It is a essential step in the connection
     * establishment procedure. It takes place
     * in the destination side. It means that
     * destination should compute this network 
     * message (SEND_REQUEST)as download request
     * 
     * @param network_message a network message 
     * to be computed
     */
    public void receiveData(Connection c){
    	//long now=CommonState.getTime();
    	//please note that you should be into the 
    	//destination node of the network message
        NetworkProtocol destination = 
        	(NetworkProtocol) c.getReceiver().getProtocol(this.getProtocolID());
        //We assume that this network message will 
        //be computed as a download request
        byte receive_result = destination.receive(c);
    	if(receive_result==NetworkMessage.BWD_FAILED){
    		//handle errors of insufficient bandwidth resources
    		//for allocating the minimum required
    		//NetKernel.add((now)+c.getEEDelay(), NetworkMessage.BWD_FAILED, c);
    		this.computeBwdFailedConnection(c);
    	} else if(receive_result==NetworkMessage.REQ_NOTFOUND) {
    		//requester not found, just ignore it
    		//it might happen when a connection 
    		//request is cancelled between its start  
    		//on the source side and its treatment 
    		//on the dst side
    	}  else if (receive_result==NetworkMessage.NO_RESOURCES) {
    		//if it failed to send, notify the application source
    		this.computeDeletedConnection(c);
    	} else if (receive_result==NetworkMessage.ERROR) {
    		//if it failed to send, notify the application source
            //transport_message.setStart(value)
            //let the source aware of this refuse
    		//NetKernel.add((now)+c.getEEDelay(), NetworkMessage.SEND_REFUSED, c);
    		this.computeRefusedConnection(c);
    	} else if (receive_result==NetworkMessage.OK) {
    		//if connection is ok, send a notification to source and destination
    		//notifying dest, the local host
    		Node dest = c.getReceiver();
    		TransportSkeleton connection_event = (TransportSkeleton)dest.getProtocol(this.getUplayerProtocolID());
    		connection_event.processInConn(c.getId(), c.getSrcId(), c.getDstId(), c.getContent());
            //notifying source
	   		//src_id and dst_id are not inverted here
	   		//file_destination is replace for file destination for peersim
        	connection_event = (TransportSkeleton)c.getSender().getProtocol(this.getUplayerProtocolID());
    		connection_event.processConnAck(c.getId(),c.getEnd());
    	} else {
    		System.err.println("FATAL ERROR: unkown result on receive(), bye bye.");
    		System.exit(-1);
    	}
    }

	/**
	 * Schedule the events, delivering the object cointaing the event to the correct protocol instance PID in the node NODE.
	 * @param node Node to invoce.
	 * @param pid Protocol which message refers to.
	 * @param event event threat.
	 */
    public void processEvent(Node node, int pid, Object event) {
    	long start_event = System.currentTimeMillis();
		long now = CommonState.getTime();
		NetworkMessage network_message = (NetworkMessage) event;
		NetworkController main_oracle = ((NetworkController) (Network.get(0).getProtocol(this.getProtocolID())));
		main_oracle.addEventCounter();
		Hashtable<Long, NetworkController> uploads=NetKernel.getUploads();
		Hashtable<Long, NetworkController> downloads=NetKernel.getDownloads();
		//remove the message
		NetKernel.remove(now);
		//compute request in order
		
		//long start_denied = System.currentTimeMillis();
    	/*long start_denied = System.currentTimeMillis();
		//denied connection
		Enumeration<Connection> refused_connections = network_message.getRefusedSends();
		this.computeRefusedCollection(refused_connections);
		this.addAckTime(System.currentTimeMillis()-start_denied);

		//bandwidth limit failed connection
		Enumeration<Connection> bwd_failed_connections = network_message.getBwdFailedSends();
		this.computeBwdFailedCollection(bwd_failed_connections);
		this.addAckTime(System.currentTimeMillis()-start_denied);*/
		
    	long start_end = System.currentTimeMillis();
		//connections that have been accomplished
		//first erase these connections
    	this.close(network_message,uploads,downloads);

    	
    	this.addCloseTS(System.currentTimeMillis()-start_end);
		
    	long start_new = System.currentTimeMillis();
		//new connections
    	Enumeration<Connection> new_connections= network_message.getSendRequests();
    	this.computeNewConnections(new_connections,uploads,downloads);

    	
    	this.addReceiveDataTime(System.currentTimeMillis()-start_new);
		
    	start_end = System.currentTimeMillis();
    	NetKernel.dance();
    	this.addBalanceTS(System.currentTimeMillis()-start_end);
    	
    	long start_update = System.currentTimeMillis();
		//finally update modified connections

    	NetKernel.updateModifiedConnections();
		this.addCheckTime(System.currentTimeMillis()-start_update);

		this.addNetworkTime(System.currentTimeMillis()-start_event);
		
		//show all connections status
		/*System.err.println("\nSTATUS on "+CommonState.getTime());
		for (int i = 0; i < Network.size(); i++) {
			System.err.println("node "+i);
			NetworkController n = (NetworkController)Network.get(i).getProtocol(this.getProtocolID());
			n.show();
		}
		System.err.println("");*/

    }
    
    /**
     * computes a collection of connections which
     * result status is SEND_REFUSED. It is 
     * normally useful for processing 
     * connections that have been added to 
     * NetKernel (.add), and calls 
     * this.computeDeniedConnection method
     * 
     * @param refused_connections a collection of
     * connections with SEND_REFUSED status
     */
    /*public void computeRefusedCollection(Enumeration<Connection> refused_connections){
    	Connection c=null;
		while (refused_connections.hasMoreElements()) {
			c = refused_connections
					.nextElement();
			this.computeRefusedConnection(c);
		}
    	
    }*/

    /**
     * This computes a connections which
     * result status is SEND_REFUSED. 
     * 
     * it updates the status of the source
     * of the transfer only
     * 
     * @param c connection that had its 
     * SEND_REFUSED result
     */
    public void computeRefusedConnection(Connection c){
    	long now = CommonState.getTime();
        System.out.println("(time:"+now+", cid:"+
        		c.getId()+")event: SEND_REFUSED node is "+
        		c.getSender().getID());
		//connection ends on the source, 
		//so just sender has its status updated
		Node source = c.getSender();
		NetworkProtocol network = 
			(NetworkProtocol)source.getProtocol(this.getProtocolID());
		//updating status
		//update rx/tx for the source
		BroadcastDomain src_bd = 
			network.getBroadcastDomain(Long.valueOf(c.getSrcId()));
		int acc_conn=0;
		if(src_bd!=null){
			acc_conn=src_bd.getRefusedUploads();
			src_bd.setRefusedUploads((acc_conn+1));
		}
		boolean inter = (!(c.isIntraConnected()));
		if(inter){
			BroadcastDomain src_host_bd = 
				network.getBroadcastDomain(BroadcastDomain.HOST_BD_ID);
			if((src_host_bd!=null)&&(src_host_bd!=src_bd)){
				acc_conn=src_host_bd.getRefusedUploads();
				src_host_bd.setRefusedUploads((acc_conn+1));
			}
		}
		
		//let upper-layer process this event too
		TransportSkeleton transport = 
			(TransportSkeleton)source.getProtocol(this.getUplayerProtocolID());
		transport.processFailedConn(c.getId());			
    }

    /**
     * computes a collection of connections which
     * result status is BWD_FAILED. It is 
     * normally useful for processing 
     * connections that have been added to 
     * NetKernel (.add), and calls 
     * this.computeBwdFailedConnection method
     * 
     * @param bwd_failed_connections a collection of
     * connections with BWD_FAILED status
     * 
     */
    /*public void computeBwdFailedCollection(Enumeration<Connection> bwd_failed_connections){
    	Connection c=null;
		while (bwd_failed_connections.hasMoreElements()) {
			c = bwd_failed_connections
					.nextElement();
            this.computeBwdFailedConnection(c);
		}
    }*/

    /**
     * This computes a connections which
     * result status is BWD_FAILED. 
     * 
     * it updates the status of the source
     * of the transfer only
     * 
     * @param c connection that had its 
     * BWD_FAILED result
     */
    public void computeBwdFailedConnection(Connection c){
		//Debugging
//        System.err.println("(time:"+now+", cid:"+c.getId()+
//        		")event: BWD_FAILED node is "+
//        		c.getSender().getID()+" bwd limit failed.");
		//connection ends on the source, 
		//so just sender has its status updated
		Node source = c.getSender();
		NetworkProtocol network = 
			(NetworkProtocol)source.getProtocol(this.getProtocolID());
		//updating status
		//update rx/tx for the source
		BroadcastDomain src_bd = 
			network.getBroadcastDomain(Long.valueOf(c.getSrcId()));
		int acc_conn=0;
		if(src_bd!=null){
			acc_conn=src_bd.getFailedUploads();
			src_bd.setFailedUploads((acc_conn+1));
		}
		boolean inter = (!(c.isIntraConnected()));
		if(inter){
			BroadcastDomain src_host_bd = 
				network.getBroadcastDomain(BroadcastDomain.HOST_BD_ID);
			if((src_host_bd!=null)&&(src_host_bd!=src_bd)){
				acc_conn=src_host_bd.getFailedUploads();
				src_host_bd.setFailedUploads((acc_conn+1));
			}
		}
		
		//let upper-layer compute this event too
		TransportSkeleton transport = 
			(TransportSkeleton)source.getProtocol(this.getUplayerProtocolID());
		transport.processFailedConn(c.getId());			
    }
    /**
     * This computes a connections which
     * result status is NO_RESOURCES. 
     * 
     * it does not update the of neither 
     * source nor destination
     * 
     * @param c connection that had its 
     * NO_RESOURCES result
     */
    public void computeDeletedConnection(Connection c){
		//connection ends on the source, 
		//so just sender has its status updated
		Node source = c.getSender();
		//let upper-layer compute this event too
		TransportSkeleton transport = 
			(TransportSkeleton)source.getProtocol(this.getUplayerProtocolID());
		transport.processDeletedConn(c.getId());					
    }
    
    /**
     * computes a collection of connections which
     * result status is SEND_FIN. It is 
     * normally useful for processing 
     * connections that have been added to 
     * NetKernel (.add)
     * 
     * @param msg message with a collection
     * of accomplished connections
     * @param uploads map
     * @param downloads map 
     */
    public void close(NetworkMessage msg, 
    		Hashtable<Long, NetworkController> uploads,
    		Hashtable<Long, NetworkController> downloads){
    	Enumeration<Connection> accomlished_connections = msg.getEndSends();
    	Connection c=null;
    	long start_end = 0L;
		while (accomlished_connections.hasMoreElements()) {
			c = accomlished_connections
					.nextElement();
			uploads.put(Long.valueOf(c.getSrcId()), 
					(NetworkController)c.getSender().getProtocol(c.getNPId()));
			downloads.put(Long.valueOf(c.getDstId()), 
					(NetworkController)c.getReceiver().getProtocol(c.getNPId()));
        	start_end = System.currentTimeMillis();
			this.closeConnection(c);
        	this.addCloseConn(System.currentTimeMillis()-start_end);
		}
		
    	
    }

    /**
     * computes a collection of connections which
     * result status is SEND (SYN). It is 
     * normally useful for processing 
     * connections that have been added to 
     * NetKernel (.add)
     * 
     * @param new_connections collection of new 
     * connections
     * @param uploads map
     * @param downloads map
     */
    public void computeNewConnections(Enumeration<Connection> new_connections, 
    		Hashtable<Long, NetworkController> uploads,
    		Hashtable<Long, NetworkController> downloads){
    	Connection c=null;
		while (new_connections.hasMoreElements()) {
			c = new_connections.nextElement();
			uploads.remove(Long.valueOf(c.getSrcId()));
			downloads.remove(Long.valueOf(c.getDstId()));
			this.receiveData(c);
		}
    	
    }

}
