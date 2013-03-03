package net;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;


import peersim.core.Network;
import peersim.core.Protocol;
import peersim.core.Node;
import peersim.core.CommonState;

/**
 * The class implements the data structure for the bandiwdth protocol.<p>
 * The data structure and the main methods used to provide the bandwdith mechanism
 * are in this class. <p> In particular the method for computing the transfer time:
 * it computes the time needed to delivery to set of data from the sender to the
 * receiver, otherwise it returns an error code which reflects the needed of up/down-link bandwidth.
 *
 * @author Alessandro Russo
 * @version $Revision: 0.02$
 */
public class NetworkController implements Protocol, NetworkSkeleton {
    public static final long ERROR_CODE=-1L;

    private static final long MIN_ID=0;
    
    /*private static final 
    long ID_RANGE = 9223372036854775800L-0+1;*/

    long conn_id_gen;

    /**
     * HOST_BD_ID: host broadcast domain
     * identifier
     * 
     * This is the main broadcast domain, and it
     * can not be removed.
     * 
     */
    private long 
    default_local_address;
    
    /**
     * INVALID_BD_ID: invalid broadcast domain
     * identifier
     * 
     */
    protected static final 
    int INVALID_BD_ID = -1;

    /**
     * INVALID_ID: invalid identifier
     * for src/dst (in local map)
     * 
     */
    protected static final 
    long INVALID_ID = -1L;


    /**
     * conn_retry_counter: connection
     * retry counter 
     * 
     * it is updated when a connection 
     * fails to be established and a 
     * retry is necessary
     */
    protected int conn_retry_counter;

    
    /**
     * conn_ref_counter: connection
     * refused counter 
     * 
     * it is updated when all attempts
     * to established fail
     */
    protected int conn_ref_counter;

    protected static final 
    long DEFAULT_NETWORK_CAPACITY = 10L*1000L*1000L;
    
    /**
     * DEFAULT_NETWORK_CAPACITY: default
     * network capacity
     * 
     * for example: 
     * 10Mbps = 10*1000*1000 
     * 
     */
    
    /**
     * broadcasts: is a list with all broadcasts domain 
     * of this network
     * 
     * The most important broadcast domain is that
     * with HOST_BD_ID id. It is the main broadcast 
     * domain, also called "host" broadcast domain.
     * 
     * It is possible to add other broadcast domains
     * ( such as "guest"), if there is enough 
     * bandwidth (from network capacity) resources.
     * 
     * For example, a host domain with 100Mbps can 
     * have up to 10 guests broadcasts domain with
     * 10Mbps each. In this case, there would be 11
     * broadcast domain in this network layer, 
     * 1 host and 10 guests.
     * 
     */
    private List<BroadcastDomain> broadcasts;

    /**
     * maps local address to broadcast domain.
     * 
     */
    private Hashtable<Long, BroadcastDomain> local_map;

    
    //private static final int TRANSP=0;
    /**
     * Minimum upload bandiwdth.
     */
	public static final long DEFAULT_DELAY=10;
	public static final long DEFAULT_SEED=9381048245L;

    /**
     * Current number of passive download.
     */
    private int pid;

    /**
     * Current number of passive download.
     */
    private long id;
    /**
     * Current number of passive download.
     */
    private int uplayer_pid;
    /**
     * MBF: minimum bandwidth factor, it stablishes a relation between max and mim bandwidth values.
     */
    //private double mbf;
    /**
     * second definition.
     */
    private long ts;

    private boolean hard_limit_flag;
    /**
     * Data structure used to collect elements produced during the process of
     * time delivery computation.
     */
    private Node node;
    
    private long renew_delay;
	protected long retry_delay;

	protected int retry_attempts;
    
	protected java.util.Random rgen;

	//private long conn_id_g;

	protected long balance_ts = 0;
	protected long close_ts = 0;
	protected long close_conn_ts = 0;
	protected long notify_close_conn_ts = 0;
	protected long process_flow = 0;
	protected long end_task = 0;
	protected long fixbw_ts = 0;
	protected long network_time = 0;
	//profiling events
	protected long receive_data_time=0;
	//protected long ack_time=0;
	protected long check_time=0;
	protected long event_counter=0;
	protected long send_req_counter=0;
	protected long send_ref_counter=0;
	protected long send_ack_counter=0;
	protected long upd_down_counter=0;
	protected long transfer_acc_counter=0;
	protected long balance_up_counter=0;
	private boolean bwd_control;
	private float max_bwd_reservation_percentage;
	private long seed;
    /**
     * Constructor is empty
     * @param prefix
     */
    public NetworkController(String prefix) {
        super();
        this.broadcasts = new ArrayList<BroadcastDomain>();
        BroadcastDomain bd = 
        	new BroadcastDomain(
        			BroadcastDomain.HOST_BD_ID, 
        			NetworkController.DEFAULT_NETWORK_CAPACITY, false,0L);
        this.broadcasts.add(bd);
        this.conn_id_gen=NetworkController.MIN_ID;
        this.conn_ref_counter=0;
        this.conn_retry_counter=0;
        this.bwd_control=false;
        //80% is the default max reservation percentage
        this.max_bwd_reservation_percentage=0.8f;
        this.hard_limit_flag=false;
        this.seed=NetworkController.DEFAULT_SEED;
        //this.conn_id_g = NetworkController.MIN_ID;
    }

    /**
     * Clone method implemented for Protocol class.
     * @return An object which is the clone of the current on.
     */
    public Object clone() {
        NetworkController network = null;
        try {
            network = (NetworkController) super.clone();
        } catch (CloneNotSupportedException e) {
        }
        network.pid = new Integer(0);
        network.uplayer_pid = new Integer(0);
        network.broadcasts = new ArrayList<BroadcastDomain>();
        BroadcastDomain bd = new BroadcastDomain(
        		BroadcastDomain.HOST_BD_ID, 
        		NetworkController.DEFAULT_NETWORK_CAPACITY,false,this.renew_delay);
        network.broadcasts.add(bd);
        network.node=null;
        network.conn_id_gen=NetworkController.MIN_ID;
        network.conn_ref_counter=0;
        network.conn_retry_counter=0;
        //80% is the default max reservation percentage
        network.max_bwd_reservation_percentage=0.8f;
        network.hard_limit_flag=this.hard_limit_flag;
        network.seed=this.seed;
        //bat.conn_id_g=NetworkController.MIN_ID;
        return network;
    }
    /**
	 * bootstrap: initialises key attributes
	 * 
   * @param id storage element id, it is 
   * such as transport address
   * @param n peersim Node
   * @param bw maximum bandwidth to be assigned 
   * to both uplink and downlink
	 */
    public void bootstrap(long id, Node n, 
    		long bw, long seed, boolean has_bottleneck, long renew_delay,
    		long retry_delay, int retry_attempts, boolean bwd_control,
    		float max_bwd_reservation_percentage) {
    	this.setMaxBwdReservationPercentage(max_bwd_reservation_percentage);
    	this.bwd_control=bwd_control;
    	this.fixbw_ts = 0;
    	this.balance_ts=0;
    	this.close_ts=0;
    	this.close_conn_ts=0;
    	this.notify_close_conn_ts=0;
    	this.process_flow=0;
    	this.network_time = 0;
    	//this.ack_time=0;
    	this.check_time=0;
    	this.event_counter=0;
    	this.send_req_counter=0;
    	this.send_ref_counter=0;
    	this.send_ack_counter=0;
    	this.upd_down_counter=0;
    	this.transfer_acc_counter=0;
    	this.balance_up_counter=0;
        this.id=id;
        this.node=n;
        this.default_local_address=this.id;
        this.renew_delay = renew_delay;
        this.retry_delay = retry_delay;
        this.retry_attempts = retry_attempts;
        this.seed = seed;
		this.rgen = new Random(this.seed);
//		this.rgen.setSeed(this.seed);        
        //this.forceInitUpload(bw);
        //this.download_connection_list=new ConnectionList();
        //this.upload_connection_list=new ConnectionList();
        //add the default broadcast domain (host)
        BroadcastDomain bd = new BroadcastDomain(
        		BroadcastDomain.HOST_BD_ID, bw, has_bottleneck,this.renew_delay);
        this.broadcasts = new ArrayList<BroadcastDomain>();
        this.broadcasts.add(bd);
        //adding default broadcast domain (host)
        this.local_map=new Hashtable<Long, BroadcastDomain>();
        //first initial local address
        //null means it is a new element
        if(this.local_map.put(Long.valueOf(this.id), bd)==null)
        	bd.addAddresse(this.id);
        this.conn_id_gen=NetworkController.MIN_ID;
        this.conn_ref_counter=0;
        this.conn_retry_counter=0;
        //this.conn_id_g=NetworkController.MIN_ID;
    }

	/**
	* reset: resets network state
	*/
    public void clear(){
		this.rgen = new Random(this.seed);
    	this.fixbw_ts = 0;
    	this.balance_ts=0;
    	this.close_ts=0;
    	this.close_conn_ts=0;
    	this.notify_close_conn_ts=0;
    	this.process_flow=0;
    	this.network_time = 0;
    	//this.ack_time=0;
    	this.check_time=0;
    	this.event_counter=0;
    	this.send_req_counter=0;
    	this.send_ref_counter=0;
    	this.send_ack_counter=0;
    	this.upd_down_counter=0;
    	this.transfer_acc_counter=0;
    	this.balance_up_counter=0;
    	this.bwd_control=false;
    	//reset broadcast domains
    	BroadcastDomain bd = null;
    	for (int i = 0; i < this.broadcasts.size(); i++) {
			bd=this.broadcasts.get(i);
			bd.clear();
		}
        this.conn_id_gen=NetworkController.MIN_ID;
        this.conn_ref_counter=0;
        this.conn_retry_counter=0;
    }




    public void setId(long id) {
    	this.id = id;
    }

    public void setBwdControl(boolean flag) {
    	this.bwd_control=flag;
    }

    public void setMaxBwdReservationPercentage(float max_bwd_reservation_percentage) {
    	if(max_bwd_reservation_percentage>0.0f&&max_bwd_reservation_percentage<1.0f)
    		this.max_bwd_reservation_percentage=max_bwd_reservation_percentage;
    }

    public long getId() {
    	return this.id;
    }

    /**
     * Return the current upload bandwidth.
     * 
     * By default, it returns the sum of
     * upload bandwidth of a the default 
     * broadcast domain
     * 
     * @return Current upload bandwidth 
     * (from default bradcast domain).
     */
    public long getUpload() {
        //return upload of default broadcast domain
    	return this.getUpload(BroadcastDomain.HOST_BD_ID);
    }
    
    /**
     * Return the current upload bandwidth.
     *
     * @param bd_id broadcast identifier
     * 
     * @return Current upload bandwidth , or 
     */
    public long getUpload(int bd_id) {
        //return upload of default broadcast domain
    	BroadcastDomain bd = 
    		this.getBroadcastDomain(bd_id);
    	if(bd==null)
    		return NetworkController.ERROR_CODE;
    	else
    		return bd.getUploadConsuption();
    }

    


    /**
     * Set the definition of second in event time units.
     * @param evnet time units that represent a second. for exmaple, if time is equal to 1000, it means that one second represents 1000 time units of PeerSim 
     */
    public void setTS(long ts) {
        this.ts=ts;
    }
    public void setHardLimitFlag(boolean flag){
    	this.hard_limit_flag = flag;
    }

    /**
     * Set the definition of second in event time units.
     * @param evnet time units that represent a second. for exmaple, if time is equal to 1000, it means that one second represents 1000 time units of PeerSim 
     */
    public long getTS() {
        return this.ts;
    }


    /**
     * Return the current download bandwidth.
     * 
     * note that the download sum comes from
     * the default broadcast domain
     * 
     * @return Current download bandwidth.
     */
    public long getDownload() {
        //return upload of default broadcast domain
    	return this.getDownload(
    			BroadcastDomain.HOST_BD_ID);
    }

    /**
     * Return the current download bandwidth.
     * 
     * @return Current download bandwidth, or
     * NetworkController.ERROR_CODE if broadcast
     * identifier does not exist.
     */
    public long getDownload(int bd_id) {
        //return upload of default broadcast domain
    	BroadcastDomain bd = 
    		this.getBroadcastDomain(bd_id);
    	if(bd==null)
    		return NetworkController.ERROR_CODE;
    	else
    		return bd.getDownloadConsuption();
    }

    /**
     * Set the minimum download.
     * @param _download_min Minimum download.
     */
    /*public void setDownloadMin(long _download_min) {
        this.download_min = _download_min;
    }*/

    /**
     * Get the minimum download.
     * @return Minimum download.
     */
    /*public long getDownloadMin() {
        return this.download_min;
    }*/

    /**
     * Set the maximum download.
     * @param _download_max Maximum download.
     */
    /*public void setDownloadMax(long _download_max) {
        this.download_max = _download_max;
    }*/

    /**
     * Get teh maximum download.
     * @return Maximum download.
     */
    /*public long getDownloadMax() {
        return this.download_max;
    }*/




    /**
     * Get the whole list of current upload 
     * connections 
     * 
     * by default, it returns the upload 
     * connections from the default broadcast
     * domain
     * 
     * @return A list of connections in upload.
     */
    public ConnectionList getUploadConnections() {
        //return upload list 
    	//from default broadcast domain
    	return this.getUploadConnections(
    			BroadcastDomain.HOST_BD_ID);
    }

    /**
     * Get the whole list of current upload 
     * connections 
     * 
     * @param bd_id braocast identifier
     * 
     * @return A list of connections in upload,
     * or null if
     * bd_id is invalid in this network.
     */
    public ConnectionList getUploadConnections(int bd_id) {
        //return upload list 
    	//from default broadcast domain
    	BroadcastDomain bd = 
    		this.getBroadcastDomain(bd_id);
    	if(bd==null)
    		return null;
    	else
    		return bd.getUploads();
    }

    /**
     * Get the whole list of current download
     * connections 
     * 
     * By default, it returns download connections
     * from the default broadcast domain
     * 
     * @return A list of connections in download.
     */
    public ConnectionList getDownloadConnections() {
        //return download list 
    	//from default broadcast domain
    	return this.getDownloadConnections(
    			BroadcastDomain.HOST_BD_ID);
    }
    /**
     * Get the whole list of current download
     * connections 
     * 
     * @param bd_id broadcast domain
     * 
     * @return A list of connections in download,
     * or null if bd_id is invalid.
     */
    public ConnectionList getDownloadConnections(int bd_id) {
        //return download list 
    	//from default broadcast domain
    	BroadcastDomain bd = 
    		this.getBroadcastDomain(bd_id);
    	if(bd==null)
    		return null;
    	else
    		return bd.getDownloads();
    }
    
	/**
	 * updateAllConnections: it updates all active 
	 * connections by computing the number of 
	 * transfered bits
	 * 
	 */
	// it updates all active connections according to the current time (now)
	//it is the best way to update connections
	public void updateAllConnections(){
    	BroadcastDomain bd=null;
    	for (int i = 0; i < this.broadcasts.size(); i++) {
			bd = this.broadcasts.get(i);
			//update uploads
			bd.setTransientTx(this.updateTbConnList(bd.getUploads()));
			//update downloads
			bd.setTransientRx(this.updateTbConnList(bd.getDownloads()));
		}
    	
	}

    /**
     * updateTb: of a connection.
     */
    /*public void updateTb(long cid) {
    	Connection c = this.getConnection(cid);
    	c.updateTb();
    }*/

    /**
     * updateTbDownConn: updates the trabsmitted bits counter for all
     * download connections.
     */
    public void updateTbDownConn() {
    	this.updateTbDownConn(BroadcastDomain.HOST_BD_ID);
    }
    /**
     * updateTbDownConn: updates the trabsmitted bits counter for all
     * download connections.
     */
    public void updateTbDownConn(int bd_id) {
    	BroadcastDomain bd = this.getBroadcastDomain(bd_id);
    	ConnectionList download_connection_list = bd.getDownloads();

    	for (int i = 0; i < download_connection_list.getSize(); i++) {
			download_connection_list.getElement(i).updateTb();
		}
    }
    /**
     * updateTbConnList: updates the trabsmitted bits counter for all
     * connections of a list.
     * 
     * @return total of transient bits
     */
    public long updateTbConnList(ConnectionList list) {
    	long total=0L;
    	Connection c=null;
    	for (int i = 0; i < list.getSize(); i++) {
    		c=list.getElement(i);
    		total+=c.updateTb();
		}
    	return total;
    }

    /**
     * updateTbDownConn: updates the trabsmitted bits counter for all
     * upload connections.
     */
    public void updateTbUpConn() {
    	this.updateTbUpConn(BroadcastDomain.HOST_BD_ID);
    }
    /**
     * updateTbDownConn: updates the trabsmitted bits counter for all
     * upload connections.
     */
    public void updateTbUpConn(int bd_id) {
    	BroadcastDomain bd = this.getBroadcastDomain(bd_id);
    	ConnectionList upload_connection_list = bd.getUploads();
    	for (int i = 0; i < upload_connection_list.getSize(); i++) {
			upload_connection_list.getElement(i).updateTb();
		}
    }

    //XXX Bandwidth fluctuation to implement
    /**
     * Provide bandwidth fluctuation during the simulation. To implement!
     */
    public void fluctuationUpload() {
    }

    /**
     * Provide bandwidth fluctuation during the simulation. To implement!
     */
    public void fluctuationDownload() {
    }

    /*public String toString() {
        String result = "\n\t>> Upload: " + this.upload + " [" + this.upload_min + ":" + this.upload_max + "] - ";
        result += "Active " + this.getActiveUp() + "(" + this.getActiveUpload() + ")" + " Passive " + this.getPassiveUp() + "(" + this.getPassiveUpload() + ")";
        result += "\n\t<< Download: " + this.download + " [" + this.download_min + ":" + this.download_max + "] - ";
        result += "Active " + this.getActiveDw() + "(" + this.getActiveDownload() + ")" + " Passive " + this.getPassiveDw() + "(" + this.getPassiveDownload() + ")";
        return result;

    }*/

    /**
     * Returns how much download bandwidth 
     * is available to a new connection
     * (by default, from host broadcast 
     * domain)
     * 
     * note that it does not consider
     * the end-to-end communication,
     * it is just a local value
     *  
     * @return the amount of available download 
     * bandwidth to a new connection.
     * 
     */
    public long getAvailableDownloadBandwidthEstimation() {
    	return this.getAvailableDownloadBandwidthEstimation(NetworkController.INVALID_ID,this.default_local_address);
    }
    /**
     * Returns how much upload bandwidth 
     * is available to a new connection
     * (by default, from host broadcast 
     * domain)
     * 
     * note that it does not consider
     * the end-to-end communication,
     * it is just a local value
     *  
     * @return the amount of available download 
     * bandwidth to a new connection.
     * 
     */
    public long getAvailableUploadBandwidthEstimation() {
    	return this.getAvailableUploadBandwidthEstimation(this.default_local_address,NetworkController.INVALID_ID);
    }
    /**
     * Returns how much download bandwidth 
     * is available to a new connection
     * 
     * We assume that guests have higher priority
     * to use network resources. So that, host
     * broadcast domain is not able to
     * borrow bandwidth from
     * guests

     * @param bd_id broadcast identifier
     *  
     * @return the amount of available download 
     * bandwidth to a new connection.
     * 
     */
    public long getAvailableDownloadBandwidthEstimation(long src_id, long dst_id) {
    	long max_download_bandwidth=0;
    	BroadcastDomain host_bd = this.local_map.get(Long.valueOf(this.default_local_address));
    	BroadcastDomain dst_bd = this.local_map.get(Long.valueOf(dst_id));
    	BroadcastDomain src_bd = this.local_map.get(Long.valueOf(src_id));
    	long bandwidth=0L, bandwidth_dst=0L;
    	if(dst_bd==null){
    		return 0L;
    	}
		int total_n_connections = 0;
    	//first check if it is a 
    	//intra/inter-network connection
		//if true, it a intra-network
		//connection
		//intra-network
		max_download_bandwidth = dst_bd.getModifiableDownload();			
		total_n_connections = dst_bd.getNumberModifiableDownloads();
		//here bellow 1 means the new connections
    	bandwidth = Math.round(
    			((double)max_download_bandwidth)/
    			(((double)(total_n_connections))+((double)1))
    			);
		if(src_bd==null&&(host_bd.hasBottleneck())){
	    	bandwidth_dst = bandwidth;
			long spare = host_bd.getSpareDownload();
			if(spare>0)
		    	bandwidth = Math.min(spare, bandwidth_dst);
			else {
				ConnectionList downloads = host_bd.getDownloads();
				Connection c= null;
				int attempts = downloads.getSize();
				long share_bw=0L;
				while (attempts>0) {
					c = downloads.getElement(0);
					share_bw = (c.getBandwidth()-c.getMinBandwidth())/2;
					if(share_bw>0){
						c.updateConnection(c.getBandwidth()-share_bw);
						downloads.moveFirsttoEnd();
				    	bandwidth = Math.min(share_bw, bandwidth_dst);
				    	break;
					} else {
						downloads.moveFirsttoEnd();
					}
					attempts--;
				}
				
			}
			/*
    		//inter-network
			//check if there is anough outbound bandwidth
			host_down_bandwidth = host_bd.getNetCapacity() 
			- host_bd.getDownloadConsuption() - bandwidth;
			//there is not enougth resources
			//recompute bandwith in function of 
			//host domain resources and source node
			if (host_down_bandwidth<0){
				max_download_bandwidth = host_bd.getSoftDownload();
				total_n_connections = host_bd.getDownloadContibutors();
		    	bandwidth_dst = bandwidth;
		    	bandwidth_host = Math.round(
		    			((double)max_download_bandwidth)/
		    			(((double)(total_n_connections))+((double)1))
		    			);
		    	bandwidth = Math.min(bandwidth_host, bandwidth_dst);
			}*/
		}
		return bandwidth;
    }
    
    /**
     * Returns how much download bandwidth 
     * is available to a new connection
     * 
     * @param bd_id broadcast identifier
     *  
     * @return the amount of available upload 
     * bandwidth to a new connection.
     * 
     */
    public long getAvailableUploadBandwidthEstimation(long src_id, long dst_id) {
    	long max_upload_bandwidth=0;
    	BroadcastDomain host_bd = this.local_map.get(Long.valueOf(this.default_local_address));
    	BroadcastDomain src_bd = this.local_map.get(Long.valueOf(src_id));
    	BroadcastDomain dst_bd = this.local_map.get(Long.valueOf(dst_id));
    	long bandwidth=0L, bandwidth_src=0L;
    	if(src_bd==null)
    		return 0L;
		int total_n_connections = 0;
    	//first check if it is a 
    	//intra/inter-network connection
		//if true, it a intra-network
		//connection
		//intra bandwidth
		max_upload_bandwidth = src_bd.getModifiableUpload();
		total_n_connections = src_bd.getNumberModifiableUploads();
		//here bellow 1 means the new connections
    	bandwidth = Math.round(
    			((double)max_upload_bandwidth)/
    			(((double)(total_n_connections))+((double)1))
    			);
		if((dst_bd==null)&&(host_bd.hasBottleneck())){
	    	bandwidth_src = bandwidth;
			long spare = host_bd.getSpareUpload();
			if(spare>0)
		    	bandwidth = Math.min(spare, bandwidth_src);
			else {
				ConnectionList uploads = host_bd.getUploads();
				Connection c= null;
				int attempts = uploads.getSize();
				long share_bw=0L;
				while (attempts>0) {
					c = uploads.getElement(0);
					share_bw = (c.getBandwidth()-c.getMinBandwidth())/2;
					if(share_bw>0){
						c.updateConnection(c.getBandwidth()-share_bw);
						uploads.moveFirsttoEnd();
				    	bandwidth = Math.min(share_bw, bandwidth_src);
				    	break;
					} else {
						uploads.moveFirsttoEnd();
					}
					attempts--;
				}
				
			}
    	}
    		
    	return bandwidth;
    }

    /**
     * Returns how much upload bandwidth 
     * is available to a connections
     * that needs a minimum bandwidth
     * 
     * @param src_id source address
     * @param dst_id destination address
     *  
     * @return the amount of available download 
     * bandwidth to a new connection.
     * 
     */
    public long getAvailableUploadBandwidthBestfit(long src_id, long dst_id) {
    	BroadcastDomain host_bd = this.local_map.get(Long.valueOf(this.default_local_address));
    	BroadcastDomain src_bd = this.local_map.get(Long.valueOf(src_id));
    	BroadcastDomain dst_bd = this.local_map.get(Long.valueOf(dst_id));
    	long bandwidth=0L, bandwidth_src=0L;
    	if(src_bd==null)
    		return 0L;
    	//first check if it is a 
    	//intra/inter-network connection
		//if true, it a intra-network
		//connection
		//intra bandwidth
		//max_upload_bandwidth from this node
		bandwidth  = src_bd.getNetCapacity()-src_bd.getRevervedUploadBwd();
    	//check if this is an inter-connected
    	//and if the host bd has a bottleneck 
		if((dst_bd==null)&&(host_bd.hasBottleneck())){
	    	bandwidth_src = bandwidth;
			long spare = host_bd.getSpareUpload();
	    	bandwidth = Math.min(spare, bandwidth_src);
    	}
    		
    	return bandwidth;
    }
    /**
     * Returns how much download bandwidth 
     * is available to a connections
     * that needs a minimum bandwidth
     * 
     * @param src_id source address
     * @param dst_id destination address
     *  
     * @return the amount of available download 
     * bandwidth to a new connection.
     * 
     */
    public long getAvailableDownloadBandwidthBestfit(long src_id, long dst_id) {
    	BroadcastDomain host_bd = this.local_map.get(Long.valueOf(this.default_local_address));
    	BroadcastDomain dst_bd = this.local_map.get(Long.valueOf(dst_id));
    	BroadcastDomain src_bd = this.local_map.get(Long.valueOf(src_id));
    	long bandwidth=0L, bandwidth_dst=0L;
    	if(dst_bd==null)
    		return 0L;
    	//first check if it is a 
    	//intra/inter-network connection
		//if true, it a intra-network
		//connection
		//intra-network
		//max_download_bandwidth
		bandwidth = dst_bd.getNetCapacity()-dst_bd.getRevervedDownloadBwd();			
		if(src_bd==null&&(host_bd.hasBottleneck())){
	    	bandwidth_dst = bandwidth;
			long spare = host_bd.getSpareDownload();
	    	bandwidth = Math.min(spare, bandwidth_dst);
		}
		return bandwidth;
    }

    public long getConnectionEnd(long src_id, long connection_id) {
    	Connection c = null;
    	BroadcastDomain bd = this.getBroadcastDomain(Long.valueOf(src_id));
    	long result=NetworkController.ERROR_CODE;
    	if(bd!=null){
    		c = bd.getConnection(connection_id);
    		if(c!=null)
    			result=c.getEnd();
    	}
    	return result;
    }

    /**
     * search the longest download connection
     * throughout all broadcast domains
     * 
     * @return returns the longest connection, 
     * excepts if there is no connection (null)
     * 
     */
    
    public Connection getLongerDownloadConnection() {
    	long duration=0;
    	Connection longest_c = null;
    	ConnectionList downloads=null;
    	for (int i = 0; i < this.broadcasts.size(); i++) {
			downloads = this.broadcasts.get(i).getDownloads();
	    	for (int j = 0; j < downloads.getSize(); j++) {
	    		if(duration<downloads.getElement(i).getEnd()){
	    			duration = downloads.getElement(j).getEnd();
	    			longest_c = downloads.getElement(j);
	    		}
			}
		}
    	return longest_c;

    }
    /**
     * search the longest upload connection
     * throughout all broadcast domains
     * 
     * @return returns the longest connection, 
     * excepts if there is no connection (null)
     * 
     */
    
    public Connection getLongerUploadConnection() {
    	long duration=0;
    	Connection longest_c = null;
    	ConnectionList uploads=null;
    	for (int i = 0; i < this.broadcasts.size(); i++) {
    		uploads = this.broadcasts.get(i).getUploads();
	    	for (int j = 0; j < uploads.getSize(); j++) {
	    		if(duration<uploads.getElement(i).getEnd()){
	    			duration = uploads.getElement(j).getEnd();
	    			longest_c = uploads.getElement(j);
	    		}
			}
		}
    	return longest_c;

    }

    /**
     * spare bandwidth from a pool of connection and its maximum bandwidth
     * 
     * @param list of connections
     * @param maximum bandwidth
     * 
     * @return spare bandwidth
     * 
     */
    
    public long getSpareBandwidth(ConnectionList connections, long maximum_bandwidth) {
    	for (int i = 0; i < connections.getSize(); i++) {
    		maximum_bandwidth-=connections.getElement(i).getBandwidth();
    	}
    	if (maximum_bandwidth<0) {
    		System.err.println("CRITICAL ERROR in BandwidthAwareTransport: bandwidth reached negative value");
    	}
    	return maximum_bandwidth;
    }
    /**
     * gets the spare download bandwidth.  
     * 
     * note that an unbalanced connection 
     * list can return negative values 
     * 
     * by default, it returns host domain
     * spare bandwidth
     * 
     * @return spare download bandwidth
     * 
     */
    
    public long getSpareDownloadBandwidth() {
    	return this.getSpareDownloadBandwidth(BroadcastDomain.HOST_BD_ID);
    }
    /**
     * gets the spare download bandwidth.  
     * 
     * note that an unbalanced connection 
     * list can return negative values 
     * 
     * @param bd_id broadcast domain identifier
     * 
     * @return spare download bandwidth
     * 
     */
    
    public long getSpareDownloadBandwidth(int bd_id) {
    	//get spare download
    	BroadcastDomain bd = 
    		this.getBroadcastDomain(bd_id);
    	if(bd!=null)
    		return bd.getNetCapacity()-bd.getDownloadConsuption();
    	else
    		return NetworkController.ERROR_CODE;
    }
    /**
     * gets the spare download bandwidth.  
     * 
     * note that an unbalanced connection 
     * list can return negative values 
     * 
     * @param dst_id destination identifier identifier
     * 
     * @return spare download bandwidth
     * 
     */
    
    public long getSpareDownloadBandwidth(long dst_id) {
    	//get spare download
    	BroadcastDomain bd = this.local_map.get(Long.valueOf(dst_id));
    	return bd.getSpareDownload();
    }

    /**
     * gets the spare upload bandwidth.  
     * 
     * note that an unbalanced connection 
     * list can return negative values 
     * 
     * by default, it returns host domain
     * spare bandwidth
     * 
     * @return spare download bandwidth
     * 
     */
    
    public long getSpareUploadBandwidth() {
    	return this.getSpareUploadBandwidth(BroadcastDomain.HOST_BD_ID);
    }
    /**
     * gets the spare download bandwidth.  
     * 
     * note that an unbalanced connection 
     * list can return negative values 
     * 
     * @param bd_id broadcast domain identifier
     * 
     * @return spare download bandwidth
     * 
     */
    
    public long getSpareUploadBandwidth(int bd_id) {
    	//get spare download
    	BroadcastDomain bd = 
    		this.getBroadcastDomain(bd_id);
    	if(bd!=null)
    		return bd.getNetCapacity()-bd.getUploadConsuption();
    	else
    		return NetworkController.ERROR_CODE;
    }

    /**
     * gets the spare upload bandwidth.  
     * 
     * note that an unbalanced connection 
     * list can return negative values. 
     * For example, it is normal when a new
     * connection has just been added
     * 
     * @param dst_id destination identifier 
     * identifier
     * 
     * @return spare upload bandwidth
     * 
     */
    
    public long getSpareUploadBandwidth(long src_id) {
    	//get spare upload
    	BroadcastDomain bd = 
    		this.local_map.get(Long.valueOf(src_id));
    	return bd.getSpareUpload();

    }
    

    /**
     * gets the number of unmodified upload connections
     * 
     * 
     * @return number of unmodified upload connections
     * 
     */
    
    public long getUploadUnmodifiedConnections() {
    	long unmodified_upload=0;
    	long now=CommonState.getTime();
    	for (int i = 0; i < this.getUploadConnections().getSize(); i++) {
			if (this.getUploadConnections().getElement(i).getUpdateTimestamp()<(now))
				unmodified_upload++;
    	}
    	return unmodified_upload;
    }

    /**
     * gets the number of unmodified upload connections
     * 
     * 
     * @return number of unmodified upload connections
     * 
     */
    
    public long getDownloadUnmodifiedConnections() {
    	long unmodified_download=0;
    	long now=CommonState.getTime();
    	for (int i = 0; i < this.getDownloadConnections().getSize(); i++) {
			if (this.getDownloadConnections().getElement(i).getUpdateTimestamp()<(now))
				unmodified_download++;
    	}
    	return unmodified_download;
    }

    /**
     * gets the unmodified download bandwidth
     * 
     * 
     * @return unmodified download bandwidth
     * 
     */
    
    public long getDownloadBandwidthUnmodified() {
    	long unmodified_download=0;
    	long now=CommonState.getTime();
    	for (int i = 0; i < this.getDownloadConnections().getSize(); i++) {
			if (this.getDownloadConnections().getElement(i).getUpdateTimestamp()<(now))
				unmodified_download+=this.getDownloadConnections().getElement(i).getBandwidth();
    	}
		//System.out.println("FINAL:"+unmodified_download);
    	return unmodified_download;
    }

    /**
     * gets the unmodified upload bandwidth
     * 
     * 
     * @return unmodified upload bandwidth
     * 
     */
    
    public long getUploadBandwidthUnmodified() {
    	long unmodified_upload=0;
    	long now=CommonState.getTime();
    	for (int i = 0; i < this.getUploadConnections().getSize(); i++) {
			if (this.getUploadConnections().getElement(i).getUpdateTimestamp()<(now))
				unmodified_upload+=this.getUploadConnections().getElement(i).getBandwidth();
    	}
    	return unmodified_upload;
    }

    /**
     * update all (upload or download) connections timestamp of a node
     * 
     * @param list of connections
     * @param maximum bandwidth
     * 
     * @return spare bandwidth
     * 
     */
    
    public void updateAllConnectionsTimestap(ConnectionList connections) {
    	for (int i = 0; i < connections.getSize(); i++) {
    		connections.getElement(i).setUpdateTimestamp();
    	}
    }

    /**
     * in fair-sharing approach, it means
     * the maximum bandwith that a connection
     * it supposed to have
     * 
     * DEPRECATED with limits and 
     * broadcast new concepts
     * 
     * By default it computes and returns a value
     * from default braodcast domain
     * 
     */
    /*public long getCurrentConnMaxDown(){
    	return this.getCurrentConnMaxDown(
    			NetworkController.HOST_BD_ID);
    }*/
    /**
     * in fair-sharing approach, it means
     * the maximum bandwith that a connection
     * it supposed to have
     * 
     * DEPRECATED with limits and 
     * broadcast new concepts
     * 
     * @return NetworkController.ERROR_CODE
     * if bd_id is invalid
     */
    /*public long getCurrentConnMaxDown(int bd_id){
    	BroadcastDomain bd = this.getBroadcastDomain(
    			bd_id);
    	if(bd==null)
    		return NetworkController.ERROR_CODE;
    	else {
        	double downloads = ((double)bd.getDownloads().getSize());
        	if(downloads>0)
        		return Math.round(((double)this.download_max)/downloads);
        	else
        		return this.download_max;
    	}
    }*/
    /**
     * in fair-sharing approach, it means
     * the maximum bandwith that a connection
     * it supposed to have
     * 
     * it has become DEPRECATED since 
     * connection minimum limit and 
     * broadcast domain concepts were
     * introduced (20110822)
     * 
     * By default it computes and returns a value
     * from default braodcast domain
     * 
     */
    /*public long getCurrentConnMaxUp(){
    	return this.getCurrentConnMaxUp(
    			NetworkController.HOST_BD_ID);
    }*/
    /**
     * in fair-sharing approach, it means
     * the maximum bandwith that a connection
     * it supposed to have
     * 
     * it has become DEPRECATED since 
     * connection minimum limit and 
     * broadcast domain concepts were
     * introduced (20110822)
     * 
     * By default it computes and returns a value
     * from default braodcast domain
     * 
     */
    /*public long getCurrentConnMaxUp(int bd_id){
    	BroadcastDomain bd = this.getBroadcastDomain(
    			bd_id);
    	if(bd==null)
    		return NetworkController.ERROR_CODE;
    	else {
        	long uploads = Math.round(((double)this.upload_max)/((double)bd.getUploads().getSize()));
        	if(uploads>0)
        		return Math.round(((double)this.upload_max)/uploads);
        	else
        		return this.upload_max;
    	}
    }*/
    
    /*public long fixUpload(long src_id, long conn_id){
    	BroadcastDomain bd = this.local_map.get(Long.valueOf(src_id));
    	
    	if(bd==null)
    		return NetworkController.ERROR_CODE;
    	

    	ConnectionList connections = bd.getUploads();
    	Connection connection=null;
    	long now = CommonState.getTime();
    	long spare_up = 0L, current_bandwith=0L,
    	up_contributors=0L, new_bandwidth=0L,
    	target_mean=0L;
    	
		for (int i = 0; i < connections.getSize(); i++) {
			connection = connections.getElement(i);
			spare_up=bd.getSpareUpload();
			up_contributors = bd.getUploadContibutors();
			//if it fixed, break up
			if(spare_up>=0||up_contributors==0)
				break;
			current_bandwith = connection.getBandwidth();
			target_mean=
				Math.round(((double)bd.getSoftUpload())/
						((double)up_contributors));

			if(connection.getUpdateTimestamp()<now){
				if (up_contributors==1) {
					if((connection.getBandwidth()+spare_up)>0){
						new_bandwidth = connection.getBandwidth()+spare_up;
						if(new_bandwidth<connection.getMinBandwidth()){
							//dont modify it
							//new_bandwidth=connection.getBandwidth();
							break;
						}
					} else {
						//dont modify it
						//new_bandwidth = connection.getBandwidth();
						break;
					}
				} else {
					new_bandwidth=Math.max(target_mean,connection.getMinBandwidth());
					//
					//fair sharing police, 
					//fair enougth when
					//there are spare bandwidth
					//because limits have already
					//been taken into account
					//when getSoftUpload was called
					//bandwidth_contribution = Math.abs(Math.round(((((double)connection.getBandwidth())-(double)(connection.getMinBandwidth()))/((double)bd.getUnmodifiedUpload()))*(double)residual_spare_bandwidth));
					//new_bandwidth = connection.getBandwidth() - bandwidth_contribution ;
					//if(new_bandwidth<connection.getMinBandwidth())
					//	new_bandwidth=connection.getBandwidth();
				}
				if ((new_bandwidth<=0)||
						new_bandwidth>=current_bandwith )
					continue;
				connection.updateConnection(new_bandwidth, this.ts);				
			}
		}
    	
    	return bd.getSpareUpload();
    }*/
    
    public long fixDownload(long dst_id, boolean min_flag){
    	BroadcastDomain bd = this.local_map.get(Long.valueOf(dst_id));
    	
    	if(bd==null)
    		return NetworkController.ERROR_CODE;
    	if(min_flag)
    		return bd.getDownloadDonnersReloadMin();
    	return bd.getDownloadDonnersReload();
    	
    }
    
    public long fixUpload(long src_id, boolean min_flag){

    	BroadcastDomain bd = this.local_map.get(Long.valueOf(src_id));
    	
    	if(bd==null)
    		return NetworkController.ERROR_CODE;
    	
    	if(min_flag)
    		bd.getUploadDonnersReloadMin();
    	return bd.getUploadDonnersReload();

    }
    public long forroDownloads(long dst_id){
    	BroadcastDomain bd = this.local_map.get(Long.valueOf(dst_id));
    	
    	if(bd==null)
    		return NetworkController.ERROR_CODE;
    	return bd.doForroDownloads();
    	
    }
    
    public long forroUploads(long src_id){

    	BroadcastDomain bd = this.local_map.get(Long.valueOf(src_id));
    	
    	
    	if(bd==null)
    		return NetworkController.ERROR_CODE;
    	
    	return bd.doForroUploads();

    }

    

    

    /**
    *
    * This is a wrap function for sending data with 
    * bandwidth awareness. 
    * 
    * It proceeds as follows:
    * 1) create a connection identifier
    * 2) create a new network message with upper
    * layer message as content
    * 3) send a network message to the destination
    * 4) return the connection id to the caller (
    * apper layer)
    * 
    * The destination must be able to treat the 
    * network message and decides if the connection
    * might be established or not
    * 
    * Please note that the connection is NOT created
    * here, it is up to the destination to create it
    * and notfy upper layeers
    * 
    * @param data_in_bits Amount of data to 
    * transmit in bits.
    * @param source Sender node.
    * @param destination Receiver node.
    * @param end_to_end_delay in milliseconds
    * @param content upper layer message
    * 
    * please note that if the default braodcast
    * is selected, src_id and dst_id must belong to
    * different broadcast domains
    * 
    * @return returns a connection identifier. 
    * It allows application to keep track of this 
    * connection.
    * 
    */
    public long send(Node source, Node destination,
    		long data_in_bits, long end_to_end_delay,
    		Object content,long src_id, long dst_id, 
    		long deadline) {
    	int dst_bd_id=NetworkController.INVALID_BD_ID;
    	long now = CommonState.getTime();
    	//we assume that this is called
    	//in the source side (NetworkMessage.SEND_REQUEST)
    	//look for a broadcast domain
    	BroadcastDomain src_bd = this.local_map.get(Long.valueOf(src_id));
    	BroadcastDomain dst_bd = this.local_map.get(Long.valueOf(dst_id));
		//if there is deadline, check the minimun bandwidth required
		//if(network_message.getDeadline()>0)
		//create a new connection element
		//Calculating minumum bandwidth
		long min_band = 0L;
//		if((this.bwd_control)&&(deadline>0)){
//			min_band = this.getMinBandwidth(data_in_bits, deadline);
//		}
    	if(dst_bd!=null)
    		dst_bd_id=dst_bd.getId();
    	
    	if(src_bd==null){
    		return ERROR_CODE;
    	}
    	//if they belong to the same broadcast domain
    	//and this broadcast domain if the default
    	if((dst_bd_id==src_bd.getId())&&(src_bd.getId()==BroadcastDomain.HOST_BD_ID)){ 
    		return ERROR_CODE;
    	}else {
        	long connection_identifier = NetKernel.getNewConnectionId();
        	Connection c = new Connection(source, destination, min_band, 
        			now, (now)+end_to_end_delay, connection_identifier, 
        			min_band, this.ts, data_in_bits, this.getProtocolID(),
        			src_id,dst_id, end_to_end_delay,content, deadline,
        			this.hard_limit_flag);
        	NetKernel.add(((now)+end_to_end_delay), NetworkMessage.SEND, c);
        	return connection_identifier;
    	}
   }    


    public long getMaxUpBwdReservation(long address){
		BroadcastDomain bd = 
			this.local_map.get(Long.valueOf(address));
		if(bd == null){
			return NetworkController.ERROR_CODE;
		}
		long max_reserved_capacity = Math.round(bd.getNetCapacity()*this.max_bwd_reservation_percentage);
		long spare_reservation = max_reserved_capacity-
		bd.getRevervedUploadBwd();
		return spare_reservation;
    }
    public long getMaxDownBwdReservation(long address){
		BroadcastDomain bd = 
			this.local_map.get(Long.valueOf(address));
		if(bd == null){
			return NetworkController.ERROR_CODE;
		}
		long max_reserved_capacity = Math.round(bd.getNetCapacity()*this.max_bwd_reservation_percentage);
		long spare_reservation = max_reserved_capacity-
		bd.getRevervedDownloadBwd();
		return spare_reservation;
    }
    
    public void removeOngoingConnectionEvent(long ts, long cid){
    	NetKernel.removeConnection(ts, cid);
    }
	/**
	*
	* download: computes a neetwork message in order
	* to establish a connection 
	* 
	* @param network_message network message 
	* with all useful information about the 
	* new connection
	* 
	* @return Time needed to perform the trasmission, 
	* otherwise NetworkController.ERROR_CODE .
	* 
	*/
    public byte receive(Connection c) {
    	//we are in the destination 
    	//
    	//verify broadcast domain
		//if broadcast domain does not exist
		//return error
    	
		Node src_node = c.getSender();
		Node dst_node = c.getReceiver();
		NetworkController src_network = ((NetworkController) (src_node.getProtocol(this.getProtocolID())));
		BroadcastDomain src_bd = 
			src_network.local_map.get(Long.valueOf(c.getSrcId()));
		if(src_bd == null){
			return NetworkMessage.ERROR	;
		}
		NetworkController dst_network = ((NetworkController) (dst_node.getProtocol(this.getProtocolID())));
		BroadcastDomain dst_bd = 
			dst_network.local_map.get(Long.valueOf(c.getDstId()));
		if(dst_bd == null){
			return NetworkMessage.ERROR;
		}
		long dest_available_download_bw =0L;
		long src_available_upload_bw = 0L;
		long bandwidth = 0;
		long fair_sharing_bandwidth = 0L, best_fit_bandwidth=0L;
		long now = CommonState.getTime();
		
		long start = System.currentTimeMillis();
		//accelerator Tip
		//checks if both node have already one connection
		//if it is true, it just divise their
		//bandwidth by 2
		
		long min_band = 0L, requester_id=0L, current_min_band = 0L,
		src_maxup_bwd=0L,dst_maxdown_bwd=0L, duration=0L, end=0L,
		req_bwd=0L, avail_bwd=0L;

		//get the requester id, either the src or 
		//the dst
		//we assume that transfers are performed
		//always from sources, from where the 
		//reservation requester is known
		TransportSkeleton src_transport = 
			(TransportSkeleton)src_node.getProtocol(this.getUplayerProtocolID());        		
		requester_id = src_transport.getRequesterId(c.getId());
		//requester not found, its an 
		//invalid/cancelled request
		if(requester_id<0)
			return NetworkMessage.REQ_NOTFOUND;

		//min_bandiwdth have to be computed
		//if bwd control is enabled
		if(this.bwd_control&&(c.getDeadline()>0)){
			//we assume that if the reservation requester does not
			//have enough resources for fulfil the minimum 
			//bandwidth requested, the deadline have 
			//to be reviewed 
			//if src is equal to the source or destination
			//check if there is enougth bandwidth (unreserved) available
			//and eventually changes min_bandiwith if necessary
			current_min_band= this.getMinBandwidth(c.getLoad(), c.getDeadline());
			min_band=current_min_band;
			if(requester_id==c.getSrcId()){
				src_maxup_bwd = src_network.getMaxUpBwdReservation(c.getSrcId());
				if(src_maxup_bwd==0)
					//no more resources
					return NetworkMessage.NO_RESOURCES;
				if(current_min_band>src_maxup_bwd){
					min_band=src_maxup_bwd;
					//there is not enough resources
					//from src to fulfil request
					//so that the reservation end
					//must be adjusted
					duration = this.getTransferDuration(c.getLoad(), src_maxup_bwd);
					end=(duration)+(now);
					src_transport.adjustDeadline(c.getId(), end);
					c.setDeadline(end);
				}
			} else if(requester_id==c.getDstId()){
				dst_maxdown_bwd = dst_network.getMaxDownBwdReservation(c.getDstId());
				if(dst_maxdown_bwd==0)
					//no more resources
					return NetworkMessage.NO_RESOURCES;
				if(current_min_band>dst_maxdown_bwd){
					min_band=dst_maxdown_bwd;
					//there is not enough resources
					//from src to fulfil request
					//so that the reservation end
					//must be adjusted
					duration = this.getTransferDuration(c.getLoad(), dst_maxdown_bwd);
					end=(duration)+(now);
					//even for dst, fix on the src of transfer side only
					src_transport.adjustDeadline(c.getId(), end);
					c.setDeadline(end);
				}
			} else {
				//unkown/invalid requester id
				return NetworkMessage.BWD_FAILED;
			}
			//try to fit this request with the minimum bandwidth
			//enforce min bandwidth
			c.setMinBandwidth(min_band);
			
			
			//source available bandwidth for
			//a new upload
			src_available_upload_bw =
				src_network.getAvailableUploadBandwidthBestfit(c.getSrcId(),c.getDstId());
			//destination available bandwidth for
			//a new download
			dest_available_download_bw = 
				dst_network.getAvailableDownloadBandwidthBestfit(c.getSrcId(),c.getDstId());
			best_fit_bandwidth = Math.min(src_available_upload_bw, dest_available_download_bw);
			if(best_fit_bandwidth<min_band){
				//there is no enough resources
				return NetworkMessage.BWD_FAILED;
			}
			bandwidth = best_fit_bandwidth;
		} else {
			//do best-effort - fair sharing

			//first double-check
			//check if there are enough resources
			//on the requester side for fulfilling
			//the deadline
			req_bwd=this.getMinBandwidth(c.getLoad(), c.getDeadline());	
			if(req_bwd>0) { //there is a deadline
				if(requester_id==c.getSrcId()){
					avail_bwd=src_network.getAvailableUpBwd(c.getSrcId());
					if(avail_bwd==0)
						//no more resources
						return NetworkMessage.NO_RESOURCES;
					if(req_bwd>avail_bwd){
						//there is not enough resources
						//from src to fulfil request
						//so that the reservation end
						//must be adjusted
						duration = this.getTransferDuration(c.getLoad(), avail_bwd);
						end=(duration)+(now);
						//if thre is not enought
						//spare resources, revise deadline 
						//properly
						src_transport.adjustDeadline(c.getId(), end);
						c.setDeadline(end);
					}
				} else if(requester_id==c.getDstId()){
					avail_bwd=dst_network.getAvailableDownBwd(c.getDstId());
					if(avail_bwd==0)
						//no more resources
						return NetworkMessage.NO_RESOURCES;
					if(req_bwd>avail_bwd){
						//there is not enough resources
						//from src to fulfil request
						//so that the reservation end
						//must be adjusted
						duration = this.getTransferDuration(c.getLoad(), avail_bwd);
						end=(duration)+(now);
						//if thre is not enought
						//spare resources, revise deadline 
						//properly
						//always towards src
						src_transport.adjustDeadline(c.getId(), end);
						c.setDeadline(end);
					}
				} else {
					//unkown/invalid requester id
					return NetworkMessage.BWD_FAILED;
				}			
			}

			
			
			//do best-effort - fair sharing
			c.setMinBandwidth(0L);  // enforce 0
			//source available bandwidth for
			//a new upload
			src_available_upload_bw =
				src_network.getAvailableUploadBandwidthEstimation(c.getSrcId(),c.getDstId());
			//destination available bandwidth for
			//a new download
			dest_available_download_bw = 
				dst_network.getAvailableDownloadBandwidthEstimation(c.getSrcId(),c.getDstId());
			//keep the lowest value to be fair
			fair_sharing_bandwidth = Math.min(src_available_upload_bw, dest_available_download_bw);
			bandwidth = fair_sharing_bandwidth;
		}


		

		
		if (bandwidth<=0) {
			return NetworkMessage.ERROR;
		}
		//TODO check min bandwith
		
		long connection_end_time=0;
		//recovering connection identifier
		long transfer_duration = 0;
		if((this.bwd_control&&(c.getDeadline()>0))&&(c.getHardLimitFlag())){
			bandwidth=c.getMinBandwidth();
			transfer_duration = this.getTransferDuration(c.getLoad(), bandwidth);
		} else
			transfer_duration = this.getTransferDuration(c.getLoad(), bandwidth);
		
		connection_end_time=(transfer_duration)+(now);
		c.setEndtime(connection_end_time);
		c.setBandwidth(bandwidth);
		
		dst_network.addDownloadConnection(c, c.getDstId());
		src_network.addUploadConnection(c, c.getSrcId());
		//balancing connections
		long sender_spare_bandwidth = 0;
		long receiver_spare_bandwidth = 0;
		
		/*boolean inter = (!(connection.isIntraConnected()));

		if(inter){
			//fix bandwidths
			//source upload
			//destination download				
			BroadcastDomain  src_host_bd = sender.getBroadcastDomain(BroadcastDomain.HOST_BD_ID);
			BroadcastDomain  dst_host_bd = receiver.getBroadcastDomain(BroadcastDomain.HOST_BD_ID);
			if(src_host_bd.hasBottleneck()){
				if (src_host_bd.getSpareUpload()<0)
					sender.fixUpload((long)BroadcastDomain.HOST_BD_ID);
			}
			if(dst_host_bd.hasBottleneck()){
				if(dst_host_bd.getSpareDownload()<0)
					receiver.fixDownload((long)BroadcastDomain.HOST_BD_ID);
			}
		}*/			
		//if(src_bd.getSpareUpload()<0)
		//long t_mean = Math.round(((double)(dst_bd.getNetCapacity()+dst_bd.getSpareDownload()))/((double)dst_bd.getDownloads().getSize()));
			sender_spare_bandwidth = src_network.fixUpload(c.getSrcId(),this.bwd_control);
    	//if(dst_bd.getSoftDownload()<0)
    		receiver_spare_bandwidth = dst_network.fixDownload(c.getDstId(),this.bwd_control);		
		NetworkController oracle = ((NetworkController) (Network.get(0).getProtocol(this.getProtocolID())));
		oracle.addFiwBWTS(System.currentTimeMillis()-start);

		//if it fails, get rid of new connections
		if ((sender_spare_bandwidth<0)||(receiver_spare_bandwidth<0)) {
			src_network.removeUploadConnection(c, c.getSrcId());
			dst_network.removeDownloadConnection(c, c.getDstId());
			if(min_band>0L)
				return NetworkMessage.BWD_FAILED;
			else 
				return NetworkMessage.ERROR;
		} else {
			//successful connection creation 
			//send a event to the destination
			NetKernel.add(c.getEnd(), NetworkMessage.SEND_FIN, c);
			return NetworkMessage.OK;
		}

  }


   
  /**
  *
  * hasUploadConnection: checks if a specific 
  * upload connection exists
  * 
  * @param connection_id connection identifier.
  * 
  * @return true if it is a valid upload id 
  * for this network.
  * 
  */
   public boolean hasUploadConnection(long connection_id) {
	   BroadcastDomain bd = null;
	   ConnectionList uploads = null;
	   for (int i = 0; i < this.broadcasts.size(); i++) {
		   bd=this.broadcasts.get(i);
		   uploads = bd.getUploads();
		   for (int j = 0; j < uploads.getSize(); j++) {
			   if (uploads.getElement(i).getId()
					   ==connection_id)
				   return true;			
		   }
	   }
	   return false;
   }

   

   /**
   *
   * hasDownloadConnection: checks if a specific 
   * download connection exists
   * 
   * @param connection_id connection identifier.
   * 
   * @return true if it is a valid download id 
   * for this network.
   * 
   */
   public boolean hasDownloadConnection(long connection_id) {
	   BroadcastDomain bd = null;
	   ConnectionList downloads = null;
	   for (int i = 0; i < this.broadcasts.size(); i++) {
		   bd=this.broadcasts.get(i);
		   downloads = bd.getDownloads();
		   for (int j = 0; j < downloads.getSize(); j++) {
			   if (downloads.getElement(i).getId()
					   ==connection_id)
				   return true;			
		   }
	   }
	   return false;
   }

   /**
   *
   * hasConnection: if a connection exists
   * 
   * @param connection_id connection identifier.
   * 
   * @return true if it is a valid connection id 
   * for this network.
   * 
   */
   public boolean hasConnection(long connection_id) {
	   BroadcastDomain bd = null;
	   Connection c = null;
	   for (int i = 0; i < this.broadcasts.size(); i++) {
		   bd=this.broadcasts.get(i);
		   c = bd.getConnection(connection_id);
		   if(c!=null)
			   return true;
	   }
	   return false;
   }

   /**
   *
   * hasBroadCastDomain: checks if a 
   * braodcast domain identifier is valid
   * 
   * @param bd_id broadcast domain identifier.
   * 
   * @return true if bd_id is a valid broadcast
   * domain id in this network
   * 
   */
   public boolean hasBroadcastDomain(int bd_id) {
	   for (int i = 0; i < this.broadcasts.size(); i++) {
		   if(this.broadcasts.get(i).getId()==bd_id)
			   return true;
	   }
	   return false;
   }

   /**
   *
   * addUploadConnection: adds a upload connection
   * to the network
   * 
   * 
   * @param bd_id braodcast identifier.
   * @param connection object as parameter.
   * 
   * @return false if the connection has already 
   * been added or if there is not enough 
   * remaining resources for adding it
   */
   public boolean addUploadConnection(Connection connection, long src_id) {
	   BroadcastDomain host_bd = this.local_map.get(Long.valueOf(this.default_local_address));
	   BroadcastDomain bd = this.local_map.get(Long.valueOf(src_id));
	   if(bd==null)
		   return false;
	   else{
		   if(bd.addUpload(connection)){
			   boolean inter = (!(connection.isIntraConnected()));
			   if(inter)
				   host_bd.addUpload(connection);
			   return true;
		   } else
			   return false;
	   }
   }
   
   /**
   *
   * addDownloadConnection: adds a upload connection
   * to the network
   * 
   * @param connection object as parameter.
   * @param bd_id broadcast identifier.
   * 
   * @return false if the connection has already 
   * been added or if there is not enough 
   * remaining resources for adding it. Or 
   * it also returns false if bd_id is invalid
   */
   public boolean addDownloadConnection(Connection connection, long dst_id) {
	   BroadcastDomain host_bd = this.local_map.get(Long.valueOf(this.default_local_address));
	   BroadcastDomain bd = this.local_map.get(Long.valueOf(dst_id));
	   if(bd==null)
		   return false;
	   else {
		   if(bd.addDownload(connection)){
			   boolean inter = (!(connection.isIntraConnected()));
			   if(inter)
				   host_bd.addDownload(connection);
			   return true;
		   } else
			   return false;
			
	   }
   }
   
   /**
   *
   * removeUploadConnection: removes a upload 
   * connection to the network
   * 
   * By default, this connection is removed from
   * the default network broadcast domain
   * (NetworkController.DEFAULT_NETWORK_DOMAIN id)
   * 
   * @param connection object as parameter.
   * 
   * @return false if the connection has already 
   * been added or if there is not enough 
   * remaining resources for adding it. Or it also
   * returns false if bd_id is invalid
   */
   public boolean removeUploadConnection(Connection connection, long src_id) {
	   BroadcastDomain host_bd = this.local_map.get(Long.valueOf(this.default_local_address));
	   BroadcastDomain bd = this.local_map.get(Long.valueOf(src_id));
	   if(bd==null)
		   return false;
	   else{
		   if(bd.getUploads().remConnection(connection)){
			   boolean inter = (!(connection.isIntraConnected()));
			   if(inter)
				   host_bd.getUploads().remConnection(connection);
			   return true;
		   } else
			   return false;
	   }
   }

   public boolean  removeMessages(Connection connection){
	   return NetKernel.removeConnection(connection.getEnd(),connection);	   
   }
   public boolean removeMessages(long ts, long cid){
	   return NetKernel.removeConnection(ts,cid);	   
   }
   /**
   *
   * removeDownloadConnection: removes a download 
   * connection to the network
   * 
   * @param connection object as parameter.
   * @param bd_id broadcast identifier.
   * 
   * @return false if the connection has already 
   * been added or if there is not enough 
   * remaining resources for adding it. It also
   * returns false if bd_id is invalid
   */
   public boolean removeDownloadConnection(Connection connection, long dst_id) {
	   BroadcastDomain host_bd = this.local_map.get(Long.valueOf(this.default_local_address));
	   BroadcastDomain bd = this.local_map.get(Long.valueOf(dst_id));
	   if(bd==null)
		   return false;
	   else{
		   if(bd.getDownloads().remConnection(connection)){
			   boolean inter = (!(connection.isIntraConnected()));
			   if(inter)
				   host_bd.getDownloads().remConnection(connection);
			   return true;
		   } else
			   return false;
	   }
   }
   

   public long getLongerDownloadTime() {
	   long now=CommonState.getTime();
	   long time = now;
	   for (int i = 0; i < this.getDownloadConnections().getSize(); i++) {
		   if(this.getDownloadConnections().getElement(i).getEnd()> (time))
			   time = this.getDownloadConnections().getElement(i).getEnd();
	   }
	   return time;
   }

   public long getLongerUploadTime() {
	   long now=CommonState.getTime();
	   long time = now;
	   for (int i = 0; i < this.getUploadConnections().getSize(); i++) {
		   if(this.getUploadConnections().getElement(i).getEnd()> time)
			   time = this.getUploadConnections().getElement(i).getEnd();
	   }
	   return time;
   }
   /**
    * return bandwidth/transport pid.
    */
   public int getProtocolID() {
	   return this.pid;
   }

   /**
    * return client uplayer pid.
    */
   public int getUplayerProtocolID(){
	   return this.uplayer_pid;
   }
   /**
    * return bandwidth/transport pid.
    */
   public void setProtocolID(int pid) {
	   this.pid= pid;
   }

   /**
    * return client uplayer pid.
    */
   public void setUplayerProtocolID(int uplayer_pid){
	   this.uplayer_pid=uplayer_pid;
   }
   
	public void showConnState(long node_id) {
		NetworkController transport=null;
		transport = (NetworkController)Network.get((int)node_id).getProtocol(this.pid);
		ConnectionList connection_list=null;
		Connection conn_element=null;
		String connections="";
		connection_list = transport.getDownloadConnections();
		connections = "\n\tdownload connections:";
		for (int j = 0; j < connection_list.getSize(); j++) {
			conn_element = connection_list.getElement(j);
			connections = connections + "\n\t\tconnection ("+j+")";
			connections = connections + "\n\t\tconnection id ("+conn_element.getId()+")";
			connections = connections + "\n\t\t\tbandwidth:"+conn_element.getBandwidth();
			connections = connections + "\n\t\t\tsource node:"+conn_element.getDstId();
			connections = connections + "\n\t\t\tdestination node:"+conn_element.getDstId();
			connections = connections + "\n\t\t\tupdate:"+conn_element.getUpdateTimestamp();
			connections = connections + "\n\t\t\tstart:"+conn_element.getStart();
			connections = connections + "\n\t\t\tend:"+conn_element.getEnd();
		}
		connections = connections +"\n\tupload connections:";
		connection_list = transport.getUploadConnections();
		for (int j = 0; j < connection_list.getSize(); j++) {
			conn_element = connection_list.getElement(j);
			connections = connections + "\n\t\tconnection ("+j+")";
			connections = connections + "\n\t\tconnection id ("+conn_element.getId()+")";
			connections = connections + "\n\t\t\tbandwidth:"+conn_element.getBandwidth();
			connections = connections + "\n\t\t\tsource node:"+conn_element.getDstId();
			connections = connections + "\n\t\t\tdestination node:"+conn_element.getDstId();
			connections = connections + "\n\t\t\tupdate:"+conn_element.getUpdateTimestamp();
			connections = connections + "\n\t\t\tstart:"+conn_element.getStart();
			connections = connections + "\n\t\t\tend:"+conn_element.getEnd();
		}
		System.out.println("*****************************\nConnection state report:\n" +
        		"\n\t***Node "+node_id+" ***"+ 
        		"\n\tupload bandwidth: "+ transport.getUpload() +
        		"\n\tdownload bandwidth: "+ transport.getDownload() +
        		"\n\tspare download bandwidth: "+ transport.getSpareDownloadBandwidth() +
        		"\n\tspare upload bandwidth: "+ transport.getSpareUploadBandwidth() +
        		connections +
        		"\n*****************************\n");
		transport=null;
		
	}
	public void showConnState() {
		long node_id=this.getId();
		NetworkController transport=null;
		transport = (NetworkController)Network.get((int)node_id).getProtocol(this.pid);
		ConnectionList connection_list=null;
		Connection conn_element=null;
		String connections="";
		connection_list = transport.getDownloadConnections();
		connections = "\n\tdownload connections:";
		for (int j = 0; j < connection_list.getSize(); j++) {
			conn_element = connection_list.getElement(j);
			connections = connections + "\n\t\tconnection ("+j+")";
			connections = connections + "\n\t\tconnection id ("+conn_element.getId()+")";
			connections = connections + "\n\t\t\tbandwidth:"+conn_element.getBandwidth();
			connections = connections + "\n\t\t\tsource node:"+conn_element.getSender().getID();
			connections = connections + "\n\t\t\tdestination node:"+conn_element.getReceiver().getID();
			connections = connections + "\n\t\t\tstart:"+conn_element.getStart();
			connections = connections + "\n\t\t\tend:"+conn_element.getEnd();
		}
		connections = connections +"\n\tupload connections:";
		connection_list = transport.getUploadConnections();
		for (int j = 0; j < connection_list.getSize(); j++) {
			conn_element = connection_list.getElement(j);
			connections = connections + "\n\t\tconnection ("+j+")";
			connections = connections + "\n\t\tconnection id ("+conn_element.getId()+")";
			connections = connections + "\n\t\t\tbandwidth:"+conn_element.getBandwidth();
			connections = connections + "\n\t\t\tsource node:"+conn_element.getSender().getID();
			connections = connections + "\n\t\t\tdestination node:"+conn_element.getReceiver().getID();
			connections = connections + "\n\t\t\tstart:"+conn_element.getStart();
			connections = connections + "\n\t\t\tend:"+conn_element.getEnd();
		}
		System.out.println("*****************************\nConnection state report:\n" +
        		"\n\t***Node "+node_id+" ***"+ 
        		"\n\tupload bandwidth: "+ transport.getUpload() +
        		"\n\tdownload bandwidth: "+ transport.getDownload() +
        		"\n\tspare download bandwidth: "+ transport.getSpareDownloadBandwidth() +
        		"\n\tspare upload bandwidth: "+ transport.getSpareUploadBandwidth() +
        		connections +
        		"\n*****************************\n");
		transport=null;
		
	}
	public void showConnState(int bd_id) {
		long node_id=this.getId();
		NetworkController transport=null;
		transport = (NetworkController)Network.get((int)node_id).getProtocol(this.pid);
		ConnectionList connection_list=null;
		Connection conn_element=null;
		String connections="";
		connection_list = transport.getDownloadConnections(bd_id);
		connections = "\n\tdownload connections("+connection_list.getSize()+"):";
		if(connection_list!=null){
			for (int j = 0; j < connection_list.getSize(); j++) {
				conn_element = connection_list.getElement(j);
				connections = connections + "\n\t\tconnection ("+j+")";
				connections = connections + "\n\t\tconnection id ("+conn_element.getId()+")";
				connections = connections + "\n\t\t\tbandwidth:"+conn_element.getBandwidth();
				connections = connections + "\n\t\t\tsource node:"+conn_element.getSrcId();
				connections = connections + "\n\t\t\tdestination node:"+conn_element.getDstId();
				connections = connections + "\n\t\t\tupdate:"+conn_element.getUpdateTimestamp();
				connections = connections + "\n\t\t\tstart:"+conn_element.getStart();
				connections = connections + "\n\t\t\tend:"+conn_element.getEnd();
			}
		}
		connection_list = transport.getUploadConnections(bd_id);
		connections = connections +"\n\tupload connections("+connection_list.getSize()+"):";
		if(connection_list!=null){
			for (int j = 0; j < connection_list.getSize(); j++) {
				conn_element = connection_list.getElement(j);
				connections = connections + "\n\t\tconnection ("+j+")";
				connections = connections + "\n\t\tconnection id ("+conn_element.getId()+")";
				connections = connections + "\n\t\t\tbandwidth:"+conn_element.getBandwidth();
				connections = connections + "\n\t\t\tsource node:"+conn_element.getSrcId();
				connections = connections + "\n\t\t\tdestination node:"+conn_element.getDstId();
				connections = connections + "\n\t\t\tupdate:"+conn_element.getUpdateTimestamp();
				connections = connections + "\n\t\t\tstart:"+conn_element.getStart();
				connections = connections + "\n\t\t\tend:"+conn_element.getEnd();
			}
		}
		BroadcastDomain bd = transport.getBroadcastDomain(bd_id);
		int up_cont = (int)NetworkController.ERROR_CODE;
		int down_cont = (int)NetworkController.ERROR_CODE;
		if(bd!=null){
			up_cont = bd.getUploadContibutors();
			down_cont = bd.getDownloadContibutors();
		}

		System.out.println("*****************************\nConnection state report:\n" +
        		"\n\t***Node "+node_id+" ***"+ 
        		"\n\tupload bandwidth: "+ transport.getUpload(bd_id) +
        		"\n\tdownload bandwidth: "+ transport.getDownload(bd_id) +
        		"\n\tsoft upload: "+ transport.getSpareUploadBandwidth(bd_id)+
        		"\n\tsoft download: "+ transport.getSpareDownloadBandwidth(bd_id)+
        		"\n\tcontibutors upload: "+ up_cont+
        		"\n\tcontibutors download: "+ down_cont+
        		//"\n\tspare download bandwidth: "+ transport.getSpareDownloadBandwidth() +
        		//"\n\tspare upload bandwidth: "+ transport.getSpareUploadBandwidth() +
        		connections +
        		"\n*****************************\n");
		transport=null;
		
	}
	
	public long getTransferDuration(long data_in_bits, long bandwidth){
		long duration=0;
	    double precise_transfer_duration = (((double)data_in_bits)/((double)bandwidth));
	    precise_transfer_duration = ((precise_transfer_duration)* ((double) this.ts));
	    duration = Math.round(precise_transfer_duration);
	    return duration;
	}

	public long getMinBandwidth(long data_in_bits, long deadline){
		long now = CommonState.getTime();
		if(deadline>now){
		    double precise_bandwidth = (((double)data_in_bits)/((((double)(deadline-now))/((double)this.ts))));
		    long min_bwd = Math.round(precise_bandwidth);
		    return min_bwd;
		}
		return 0L;
	}

	public long getRemainingData(Connection conn) {
		long total = 0;
		Double duration = new Double((conn.getEnd() - (CommonState.getTime()))/this.ts);
		total = conn.getBandwidth()*(duration.longValue());
		return total;
	}

	public long getRemainingData(long conn_id) {
		long remaining=0;
        ConnectionList download_connections = this.getDownloadConnections();
        Connection connection=null;
        for (int i = 0; i < download_connections.getSize(); i++) {
			connection = download_connections.getElement(i);
			if (connection.getId()==conn_id){
				Double duration = new Double(((double)connection.getEnd()- ((double)(CommonState.getTime())) )/(double)this.ts);
				remaining = connection.getBandwidth()*(duration.longValue());				
			}
		}
        return remaining;
	}

	// returns the end time of a connection, 
	// either upload or download
	// if the connection doesn't exist, it returns 0
	public long getConnecionEnd(long conn_id) {
        Connection connection=null;
        ConnectionList connections = this.getDownloadConnections();
        for (int i = 0; i < connections.getSize(); i++) {
			connection = connections.getElement(i);
			if (connection.getId()==conn_id)
				return connection.getEnd();
		}
        connections = this.getUploadConnections();
        for (int i = 0; i < connections.getSize(); i++) {
			connection = connections.getElement(i);
			if (connection.getId()==conn_id)
				return connection.getEnd();
		}
		return 0;
	}

    /**
     * getTb: gets transmitted bits of a connection.
     * 
     * @param conn_id connection identifier.

     * @return current number of transmitted bits of
     * a given connection.
     */
	public long getTb(long conn_id) {
        Connection connection=null;
        ConnectionList connections = this.getDownloadConnections();
        for (int i = 0; i < connections.getSize(); i++) {
			connection = connections.getElement(i);
			if (connection.getId()==conn_id)
				return connection.getTb();
		}
        connections = this.getUploadConnections();
        for (int i = 0; i < connections.getSize(); i++) {
			connection = connections.getElement(i);
			if (connection.getId()==conn_id)
				return connection.getTb();
		}
		return 0;
	}

	public long getDataToBeReceived(long conn_id){
		long total = 0;
        ConnectionList download_connections = this.getDownloadConnections();
        Connection connection=null;
        for (int i = 0; i < download_connections.getSize(); i++) {
			connection = download_connections.getElement(i);
			if (connection.getId()==conn_id)
				total+=this.getRemainingData(connection);
		}
        return total;
	}

	//get remaining bits of a connection
	public long getRb(long conn_id){
        Connection connection=null;
        ConnectionList upload_connections = this.getUploadConnections();
        for (int i = 0; i < upload_connections.getSize(); i++) {
			connection = upload_connections.getElement(i);
			if (connection.getId()==conn_id)
				return this.getRemainingData(connection);
		}
        ConnectionList download_connections = this.getDownloadConnections();
        for (int i = 0; i < download_connections.getSize(); i++) {
			connection = download_connections.getElement(i);
			if (connection.getId()==conn_id)
				return this.getRemainingData(connection);
		}
        return 0L;
	}
	
	public void setNode(Node node){
		this.node = node;
	}

	public Node getNode(){
		return this.node;
	}

	/**
	 * forceAddBroadcastDomain: forces the 
	 * addition of a new guest broadcast domain
	 *
	 * it tires to create a new braodcast domain
	 * with the demanded bandwidth as the network
	 * capacity for both upload and download links
	 * 
	 * note that it does not check the
	 * remaining bandwidth available. 
	 * 
	 * @param bd_id broadcast domain identifier
	 * @param bw bandwidth
	 * 
	 * @return broadcast domain, or null if it 
	 * fails
	 */
	public BroadcastDomain forceBroadcastDomainCreation(int bd_id, long bw){
		//invalid bandwidth
		if((bw<=0)||(bd_id<=NetworkController.INVALID_BD_ID))
			return null;
		BroadcastDomain guest=null;
		boolean new_bd = (!(this.hasBroadcastDomain(bd_id)));
		if(new_bd) {
			//guests never has bottlenecks=false(here below)
			guest = new BroadcastDomain(bd_id, bw, false, this.renew_delay);
			this.broadcasts.add(guest);
			return guest;
		} else
			return null;
	}
	/**
	 * getBroadcastDomain adds a broadcast domain
	 *
	 * it tires to create a new braodcast domain
	 * with the demanded bandwidth as the network
	 * capacity for both upload and download links
	 * 
	 * @param broadcast domain identifier
	 * 
	 * @return broadcast domain, or null if it 
	 * failed to create it
	 */
	public BroadcastDomain createBroadcastDomain(int bd_id, long bw){
		//invalid bandwidth
		if((bw<=0)||(bd_id<=NetworkController.INVALID_BD_ID))
			return null;
		BroadcastDomain guest=null;
		boolean new_bd = (!(this.hasBroadcastDomain(bd_id)));
		if(new_bd) {
			BroadcastDomain host_bd = 
				this.getBroadcastDomain(BroadcastDomain.HOST_BD_ID);
			long remaining_bw=host_bd.getNetCapacity();
			for (int i = 0; i < broadcasts.size(); i++) {
				guest = broadcasts.get(i);
				if(guest.getId()!=BroadcastDomain.HOST_BD_ID)
					remaining_bw-=guest.getNetCapacity();
			}
			if(remaining_bw>=bw){
				guest = new BroadcastDomain(bd_id, bw, false, this.renew_delay);
				this.broadcasts.add(guest);
				return guest;
			}else
				return null;
		} else
			return null;
	}

	/**
	 * getBroadcastDomain gets a broadcast domain
	 * 
	 * @param broadcast domain identifier
	 * 
	 * @return broadcast domain, or null if the id
	 * is invalid
	 */
	public BroadcastDomain getBroadcastDomain(int bd_id){
		BroadcastDomain bd = null;
		for (int i = 0; i < this.broadcasts.size(); i++) {
			bd = this.broadcasts.get(i);
			if(bd.getId()==bd_id)
				return bd;
		}
		return null;
	}

	/**
	 * getBroadcastDomain gets a broadcast domain<br>
	 * <br>
	 * @param key for a broadcast, in this case
	 * the key is a long object of a local address<br>
	 * 
	 * @return broadcast domain, or null if the key
	 * is invalid
	 */
	public BroadcastDomain getBroadcastDomain(Long key){
		return this.local_map.get(key);
	}
	
	/**
	 * removeBroadcastDomain removes a broadcast domain
	 *
	 * it is not possible to remove the broadcast
	 * domain NetworkController.HOST_BD_ID
	 * 
	 * note that it will also delete all addresses
	 * entries to this broadcast domain from
	 * the local address table
	 * 
	 * @param broadcast domain identifier
	 * 
	 * @return broadcast domain, or null if it 
	 * failed to remove it, or if ti does not exist
	 */
	public BroadcastDomain deleteBroadcastDomain(int bd_id){
		if(bd_id==BroadcastDomain.HOST_BD_ID)
			return null;
		else {
			BroadcastDomain guest = this.getBroadcastDomain(bd_id);
			if(guest==null)
				return null;
			else {
				if(this.broadcasts.remove(guest)){
					List<Long> addresses = guest.getAddresses();
					for (int i = 0; i < addresses.size(); i++) {
						this.local_map.remove(addresses.get(i));
					}
					return guest;
				}else 
					return null;
			}
		}
	}
	/**
	 * getTotalDownGuestConnections returns the 
	 * total number of connections of guests
	 * 
	 * @return number of connections made 
	 * from guests
	 */
	public int getTotalDownGuestConnections(){
		int downloads = 0;
		BroadcastDomain bd;
		for (int i = 0; i < this.broadcasts.size(); i++) {
			bd=this.broadcasts.get(i);
			if(bd.getId()!=BroadcastDomain.HOST_BD_ID)
				downloads+=bd.getDownloads().getSize();
		}
		return downloads;
	}
	/**
	 * getTotalUpGuestConnections returns the 
	 * total number of current upload 
	 * connections of guests
	 * 
	 * @return number of connections made 
	 * from guests
	 */
	public int getTotalUpGuestConnections(){
		int uploads = 0;
		BroadcastDomain bd;
		for (int i = 0; i < this.broadcasts.size(); i++) {
			bd=this.broadcasts.get(i);
			if(bd.getId()!=BroadcastDomain.HOST_BD_ID)
				uploads+=bd.getUploads().getSize();
		}
		return uploads;
	}
	/**
	 * getDownSumGuests gets the sum of
	 * all download connections of guest
	 * domains
	 * 
	 * @return sum of download bandwidth of all
	 * guests
	 */
	public long getDownSumGuests(){
		long download = 0;
		BroadcastDomain bd;
		for (int i = 0; i < this.broadcasts.size(); i++) {
			bd=this.broadcasts.get(i);
			if(bd.getId()!=BroadcastDomain.HOST_BD_ID)
				download+=bd.getDownloadConsuption();
		}
		return download;
	}
	/**
	 * getUpSumGuests gets the sum of
	 * all uploads connections of guest
	 * domains
	 * 
	 * @return sum of upload bandwidth of all
	 * guests
	 */
	public long getUpSumGuests(){
		long upload = 0;
		BroadcastDomain bd;
		for (int i = 0; i < this.broadcasts.size(); i++) {
			bd=this.broadcasts.get(i);
			if(bd.getId()!=BroadcastDomain.HOST_BD_ID)
				upload+=bd.getUploadConsuption();
		}
		return upload;
	}
	/**
	 * getIntraDownSumGuests gets the sum of
	 * all download connections of guest
	 * domains
	 * 
	 * compute values of connections of the same
	 * network layer only
	 * 
	 * @return sum of download bandwidth of all
	 * guests
	 */
	public long getIntraDownSumGuests(){
		long download = 0;
		BroadcastDomain bd;
		for (int i = 0; i < this.broadcasts.size(); i++) {
			bd=this.broadcasts.get(i);
			if(bd.getId()!=BroadcastDomain.HOST_BD_ID)
				download+=bd.getIntraDownloadConsuption();
		}
		return download;
	}
	/**
	 * getIntraUpSumGuests gets the sum of
	 * all uploads connections of guest
	 * domains
	 * 
	 * compute values of connections of the same
	 * network layer only
	 * 
	 * @return sum of upload bandwidth of all
	 * guests
	 */
	public long getIntraUpSumGuests(){
		long upload = 0;
		BroadcastDomain bd;
		for (int i = 0; i < this.broadcasts.size(); i++) {
			bd=this.broadcasts.get(i);
			if(bd.getId()!=BroadcastDomain.HOST_BD_ID)
				upload+=bd.getIntraUploadConsuption();
		}
		return upload;
	}
	/**
	 * getHardDownSumGuests gets the sum of
	 * all download connections of guest
	 * domains that have just been modified 
	 * (now). "hard" here means connections
	 * with timestamp equal to now in peersim
	 * time
	 * 
	 * please note that this information is 
	 * essential for creating new connections
	 * just connections that
	 * have not just beenmodified (now) might 
	 * be taken into account
	 * 
	 * @return sum of download bandwidth of all
	 * guests that can not be modified
	 */
	public long getHardDownSumGuests(){
		long hard_download = 0;
		BroadcastDomain bd=null;
		ConnectionList downloads=null;
		//just connection with timestamp time
		//equal to now will be taken into
		//account
		//check this.getHardSum for details
		for (int i = 0; i < this.broadcasts.size(); i++) {
			bd=this.broadcasts.get(i);
			if((bd.getId()!=BroadcastDomain.HOST_BD_ID)){
				downloads=bd.getDownloads();
				hard_download+=this.getHardSum(downloads);
			}
		}
		return hard_download;
	}
	/**
	 * getHardUpSumGuests gets the sum of
	 * all old uploads connections of guest
	 * domains. . "hard" here means connections
	 * with timestamp equal to now in peersim
	 * time
	 * 
	 * please note that this information is 
	 * essential for creating new connections
	 * just connections that
	 * have not just beenmodified (now) might 
	 * be taken into account

	 * 
	 * @return sum of upload bandwidth of all
	 * guests that can not be modified
	 */
	public long getHardUpSumGuests(){
		long hard_upload = 0;
		BroadcastDomain bd=null;
		ConnectionList uploads=null;
		//just connection with timestamp time
		//equal to now will be taken into
		//account
		//check this.getHardSum for details
		for (int i = 0; i < this.broadcasts.size(); i++) {
			bd=this.broadcasts.get(i);
			if((bd.getId()!=BroadcastDomain.HOST_BD_ID)){
				uploads=bd.getUploads();
				hard_upload+=this.getHardSum(uploads);
			}
		}
		return hard_upload;
	}
	/**
	 * getHardSum the sum of bandwidth of a
	 * list of connections that can not be
	 * modified now. hard here means unable
	 * to be modified 
	 * 
	 * @param connections list of connections
	 * 
	 * @return sum of bandwidth of a list
	 * of connections that can not be 
	 * modified
	 */
	public long getHardSum(ConnectionList connections){
		long hard_bandwidth = 0;
		//just connection with timestamp time
		//equal to now will be taken into
		//account
		long now = CommonState.getTime();
		for (int j = 0; j < connections.getSize(); j++) {
			if(connections.getElement(j).getUpdateTimestamp()==(now))
				hard_bandwidth+=connections.getElement(j).getBandwidth();
		}
		return hard_bandwidth;
	}
	
	/**
	 * addLocalAddress: maps local address to 
	 * broadcast domain
	 * 
	 * @return true if it was added suceesfully 
	 */
	public boolean addLocalAddress(long id, BroadcastDomain bd){
		if((id>NetworkController.INVALID_ID)&&(bd!=null)){
			if((this.local_map.put(Long.valueOf(id), bd))==null){
				bd.addAddresse(id);
				return true;
			}else
				return false;
		}else
			return false;
	}

	/**
	 * removeLocalAddress: removes the local
	 * address entry to a broadcast map
	 * 
	 * @return broadcast domain or null if
	 * it does not exist in the local_map
	 */
	public BroadcastDomain removeLocalAddress(long id){
		BroadcastDomain bd = 
			this.local_map.remove(Long.valueOf(id));
		if(bd!=null)
			bd.removeAddresse(id);
		return bd;
	}

	/**
	 * getMaxNetCapacityGuests the maximum 
	 * network pacacity of all guests
	 * 
	 * @return sum of network capacities of
	 * all guests
	 */
	public long getMaxNetCapacityGuests(){
		long max=0;
		BroadcastDomain bd = null;
		for (int i = 0; i < this.broadcasts.size(); i++) {
			bd=this.broadcasts.get(i);
			if(bd.getId()!=BroadcastDomain.HOST_BD_ID)
				max+=bd.getNetCapacity();
		}
		return max;
	}
	
	/**
	 * getDefaultAddress: gets default address from
	 * the (host/default) broadcast domain
	 * 
	 * @return long address
	 */
	public long getDefaultAddress(){
		return this.default_local_address;
	}
    /**
     * It balances an amount of bandwidth among 
     * connections. it modifies all current 
     * connections and try to fit to the 
     * maximum bandwidth
     * 
     * @param duration is the time for computing 
     * modifications of balancing 
     * @param src_id source identifier (from upper 
     * layer)
     * @param dst_id destination identifier (from 
     * upper layer)
     * 
     * @return the spare bandwidth, values smaller 
     * than zero must be interpreted as error
     */
    /*public long balanceUploadReload(long duration_time, 
    		long src_id, long dst_id) {
    	BroadcastDomain bd = 
    		this.local_map.get(Long.valueOf(src_id));
    	Node counterpart=null;
        NetworkController 
        counterpart_network= null;
    	long 	current_bandwith, 
    			current_end_connection_time,
		    	intersection_duration_time, 
		    	bandwidth_contribution,
		    	new_bandwidth;
    	Connection connection=null;

    	//spare bandwidth
    	long spare_bandwidth = bd.getSpareUpload();
    	long now = CommonState.getTime();
    	boolean up_unavaible = (!(bd.uploadAvailable()));
    	//check if uploads are available to
    	//update/balance
    	if(up_unavaible||duration_time<=0)
    		return spare_bandwidth;
    	
    	
        ConnectionList 
        	upload_connections = bd.getChangableUpload();
        
        long bwd_modifiable = bd.getChangableUpBwd();
        long residual_spare = spare_bandwidth;
        
        if(upload_connections.getSize()==0)
        	return spare_bandwidth;
        
    	//upband: upload bandwidth from the source
    	long spare_downband=0;

    	long propagation_duration_time=duration_time;
    	
    	
        //sort connection numerically by bandwidth 
    	//(increasing order)
		//upload_connections.sort();
    	//if bandwidth lacks, reverse it
    	//if (spare_bandwidth<0){ 
    	//	upload_connections.reverse();
    	//}
    	
		//if this value is positive, it means that 
    	//the target connection has been increased
		//negative values mean more spare space
    	long spare_upband_src=0, spare_downband_dst=0;
		long counterpart_result=0L;
		long modification_duration_time=0L;
		boolean inter =false;
		while(upload_connections.getSize()>0){
			connection = upload_connections.remConnection(0);
			current_bandwith = connection.getBandwidth();
			new_bandwidth = current_bandwith;
			current_end_connection_time=connection.getEnd();
			intersection_duration_time = 
				Math.min(current_end_connection_time-now,
						propagation_duration_time);
			//is it inter connected??
			inter = (!(connection.isIntraConnected()));
			//counterpart source 
			counterpart = connection.getReceiver();
			counterpart_network = ((NetworkController) (counterpart.getProtocol(this.getProtocolID())));

			
			
			//check residual spare
			if(residual_spare<0){
				bandwidth_contribution = Math.abs(Math.round(((((double)connection.getBandwidth())-(double)(connection.getMinBandwidth()))/((double)bwd_modifiable))*(double)residual_spare));
				//fair sharing police, 
				//fair enougth when
				//there are spare bandwidth
				//because limits have already
				//been taken into account
				//when getSoftUpload was called
				if((connection.getBandwidth()-bandwidth_contribution)>=connection.getMinBandwidth()){
					new_bandwidth = connection.getBandwidth()-bandwidth_contribution;
					modification_duration_time = Math.round((((double)current_bandwith*(double)intersection_duration_time)/(double)new_bandwidth));
					connection.updateConnection(new_bandwidth);    					  					
					if(new_bandwidth!=current_bandwith)
						counterpart_result = counterpart_network.balanceDownloadReload(modification_duration_time,connection.getSrcId(),connection.getDstId());
					//update was successfully finished
					if (counterpart_result<0)
					//if not, unmodify connection
						new_bandwidth = current_bandwith;
					else {
						bwd_modifiable-=current_bandwith;
						residual_spare+=bandwidth_contribution;
					}
				}
			} else if(residual_spare>0){

				
				//bandwidth contribution definitions
				//
				//first of all, check if it is
				//an inter or intra connection
				//note that, inter means connection
				//among different peersim nodes
				if(inter){
					if(counterpart_network.getBroadcastDomain(BroadcastDomain.HOST_BD_ID).hasBottleneck()){
						spare_downband_dst = 
							Math.min(
								counterpart_network.getSpareDownloadBandwidth(
										BroadcastDomain.HOST_BD_ID
										),
								counterpart_network.getSpareDownloadBandwidth(
										connection.getDstId()
										)
									);
						
					} else {
						spare_downband_dst = counterpart_network.getSpareDownloadBandwidth(
								connection.getDstId()
						);
						
					}
					if(this.getBroadcastDomain(BroadcastDomain.HOST_BD_ID).hasBottleneck()){
						spare_upband_src = 
							Math.min(
									this.getSpareUploadBandwidth(BroadcastDomain.HOST_BD_ID),
									this.getSpareUploadBandwidth(
											connection.getSrcId())
									);
						
					}else {
						spare_upband_src = this.getSpareUploadBandwidth(
								connection.getSrcId())
								;
						
					}
					spare_downband = Math.min(spare_upband_src,spare_downband_dst);
				} else {
					spare_downband = counterpart_network.getSpareDownloadBandwidth(connection.getDstId());
				}
				bandwidth_contribution = Math.abs(Math.round(((((double)connection.getBandwidth())-(double)(connection.getMinBandwidth()))/((double)bwd_modifiable))*(double)residual_spare));
				//this.getUploadBandwidthUnmodified() method was by bd.getSoftUpload()
				//
				//fair sharing police, 
				//fair enougth when
				//there are spare bandwidth
				//because limits have already
				//been taken into account
				//when getSoftUpload was called
				//bandwidth_contribution = Math.abs(Math.round(((double)connection.getBandwidth()/((double)bd.getSoftUpload()))*(double)residual_spare_bandwidth));
				if(bandwidth_contribution>(spare_downband)){
					new_bandwidth = connection.getBandwidth()+spare_downband ;
				} else {
					new_bandwidth = connection.getBandwidth() + bandwidth_contribution;
				}
				if(new_bandwidth!=current_bandwith){
					modification_duration_time = Math.round((((double)current_bandwith*(double)intersection_duration_time)/(double)new_bandwidth));
					connection.updateConnection(new_bandwidth);    					
	    	        //System.out.println("("+connection.getTxId()+")"+"(+)upload connection band: " +connection.getBandwidth());
					counterpart_result = counterpart_network.balanceDownloadReload(modification_duration_time, connection.getSrcId(), connection.getDstId());
					if (counterpart_result<0){
						new_bandwidth = current_bandwith;    					  												
						//NetworkMessage plus = connection.getNetworkMessage(); 
						//NetworkMessage plusplus = new NetworkMessage(plus.getSrc(), plus.getDst(), plus.getPayloadLength(), NetworkMessage.UPD_DOWN, plus.getEndToEndDelay(), plus.getUplayerProtocolID(), plus.getContent(),connection.getId(), plus.getSrcID(), plus.getDstID(), plus.getDeadline(),plus.getNPid());
						//EDSimulator.add((connection.getEnd()-now), plusplus, connection.getSender(), this.getProtocolID());
					} else {
						bwd_modifiable-=current_bandwith;
						residual_spare-=(new_bandwidth-current_bandwith);
					}
					
				}

				
				
			} else
				return residual_spare;
			
			connection.updateConnection(new_bandwidth);
		}
		return residual_spare;			
			
			
			
    }*/
    public boolean isDownloadAvailable(long dst){
    	
    	BroadcastDomain bd = 
    		this.local_map.get(Long.valueOf(dst));
    	if(bd!=null){
    		return bd.downloadAvailable();
    		
    	}else
    		return false;
    }

    public boolean isUploadAvailable(long src){
    	
    	BroadcastDomain bd = 
    		this.local_map.get(Long.valueOf(src));
    	if(bd!=null)
    		return bd.uploadAvailable();
    	else
    		return false;
    }
    /**
     * It balances an amount of bandwidth among 
     * connections. it modifies all current 
     * connections and try to fit to the 
     * maximum bandwidth
     * 
     * @param duration is the time for computing 
     * modifications of balancing 
     * @param src_id source identifier (from upper 
     * layer)
     * @param dst_id destination identifier (from 
     * upper layer)
     * 
     * @return the spare bandwidth, values smaller 
     * than zero must be interpreted as error
     */
    /*public long balanceDownloadReload(long duration_time, 
    		long src_id, long dst_id) {
    	BroadcastDomain bd = 
    		this.local_map.get(Long.valueOf(dst_id));
    	Node counterpart=null;
        NetworkController 
        counterpart_network= null;
    	long 	current_bandwith, 
    			current_end_connection_time,
		    	intersection_duration_time, 
		    	bandwidth_contribution,
		    	new_bandwidth;
    	Connection connection=null;

    	//spare bandwidth
    	long spare_bandwidth = bd.getSpareDownload();
    	long now = CommonState.getTime();
    	boolean down_unavaible = (!(bd.downloadAvailable()));
    	//check if downloads are available to
    	//update/balance
    	if(down_unavaible||duration_time<=0)
    		return spare_bandwidth;
    	
        ConnectionList 
        	download_connections = bd.getChangableDownload();
        
        
        if(download_connections.getSize()==0)
        	return spare_bandwidth;

        long bwd_modifiable = bd.getChangableDownBwd();
        long residual_spare = spare_bandwidth;
        
    	//upband: upload bandwidth from the source
    	long spare_upband=0L;

    	long propagation_duration_time=duration_time;
    	
    	
        //sort connection numerically by bandwidth 
    	//(increasing order)
		//download_connections.sort();
    	//if bandwidth lacks, reverse it
    	//if (spare_bandwidth<0){ 
    	//	download_connections.reverse();
    	//}
    	
		//if this value is positive, it means that 
    	//the target connection has been increased
		//negative values mean more spare space
    	long spare_upband_src=0, spare_downband_dst=0;
		long counterpart_result=0L;
		long modification_duration_time=0L;
		boolean inter =false;
		while(download_connections.getSize()>0){
			connection = download_connections.remConnection(0);
			current_bandwith = connection.getBandwidth();
			new_bandwidth = current_bandwith;
			current_end_connection_time=connection.getEnd();
			intersection_duration_time = 
				Math.min(current_end_connection_time-now,
						propagation_duration_time);
			//is it inter connected??
			inter = (!(connection.isIntraConnected()));
			//counterpart source 
			counterpart = connection.getSender();
			counterpart_network = ((NetworkController) (counterpart.getProtocol(this.getProtocolID())));

			//check residual spare
			if(residual_spare<0){
				bandwidth_contribution = Math.abs(Math.round(((((double)connection.getBandwidth())-(double)(connection.getMinBandwidth()))/((double)bwd_modifiable))*(double)residual_spare));
				//fair sharing police, 
				//fair enougth when
				//there are spare bandwidth
				//because limits have already
				//been taken into account
				//when getSoftUpload was called
				if((connection.getBandwidth()-bandwidth_contribution)>=connection.getMinBandwidth()){
					new_bandwidth = connection.getBandwidth()-bandwidth_contribution;
					modification_duration_time = Math.round((((double)current_bandwith*(double)intersection_duration_time)/(double)new_bandwidth));
					connection.updateConnection(new_bandwidth);    					  					
					if(new_bandwidth!=current_bandwith)
						counterpart_result = counterpart_network.balanceUploadReload(modification_duration_time,connection.getSrcId(),connection.getDstId());
					//update was successfully finished
					if (counterpart_result<0)
					//if not, unmodify connection
						new_bandwidth = current_bandwith;
					else {
						bwd_modifiable-=current_bandwith;
						residual_spare+=bandwidth_contribution;
					}
				}
			} else if(residual_spare>0){

				
				//bandwidth contribution definitions
				//
				//first of all, check if it is
				//an inter or intra connection
				//note that, inter means connection
				//among different peersim nodes
				if(inter){
					if(counterpart_network.getBroadcastDomain(BroadcastDomain.HOST_BD_ID).hasBottleneck()){
						spare_upband_src = 
							Math.min(
								counterpart_network.getSpareUploadBandwidth(
										BroadcastDomain.HOST_BD_ID
										),
								counterpart_network.getSpareUploadBandwidth(
										connection.getSrcId()
										)
									);
					} else {
						spare_upband_src = counterpart_network.getSpareUploadBandwidth(
								connection.getSrcId()
						);
						
					}
					if(this.getBroadcastDomain(BroadcastDomain.HOST_BD_ID).hasBottleneck()){
						spare_downband_dst = 
							Math.min(
									this.getSpareDownloadBandwidth(BroadcastDomain.HOST_BD_ID),
									this.getSpareDownloadBandwidth(
											connection.getDstId())
									);
						
					} else {
						spare_downband_dst = this.getSpareDownloadBandwidth(
								connection.getDstId());
					}
					spare_upband = Math.min(spare_upband_src,spare_downband_dst);
				} else {
					spare_upband = counterpart_network.getSpareUploadBandwidth(connection.getSrcId());
				}
				bandwidth_contribution = Math.abs(Math.round(((((double)connection.getBandwidth())-(double)(connection.getMinBandwidth()))/((double)bwd_modifiable))*(double)residual_spare));
				//this.getUploadBandwidthUnmodified() method was by bd.getSoftUpload()
				//
				//fair sharing police, 
				//fair enougth when
				//there are spare bandwidth
				//because limits have already
				//been taken into account
				//when getSoftUpload was called
				//bandwidth_contribution = Math.abs(Math.round(((double)connection.getBandwidth()/((double)bd.getSoftUpload()))*(double)residual_spare_bandwidth));
				if(bandwidth_contribution>(spare_upband)){
					new_bandwidth = connection.getBandwidth()+spare_upband ;
				} else {
					new_bandwidth = connection.getBandwidth() + bandwidth_contribution;
				}
				if(new_bandwidth!=current_bandwith){
					modification_duration_time = Math.round((((double)current_bandwith*(double)intersection_duration_time)/(double)new_bandwidth));
					connection.updateConnection(new_bandwidth);    					
	    	        //System.out.println("("+connection.getTxId()+")"+"(+)upload connection band: " +connection.getBandwidth());
					counterpart_result = counterpart_network.balanceUploadReload(modification_duration_time, connection.getSrcId(), connection.getDstId());
					if (counterpart_result<0){
						new_bandwidth = current_bandwith;    					  												
						//NetworkMessage plus = connection.getNetworkMessage(); 
						//NetworkMessage plusplus = new NetworkMessage(plus.getSrc(), plus.getDst(), plus.getPayloadLength(), NetworkMessage.UPD_DOWN, plus.getEndToEndDelay(), plus.getUplayerProtocolID(), plus.getContent(),connection.getId(), plus.getSrcID(), plus.getDstID(), plus.getDeadline(),plus.getNPid());
						//EDSimulator.add((connection.getEnd()-now), plusplus, connection.getSender(), this.getProtocolID());
					} else {
						bwd_modifiable-=current_bandwith;
						residual_spare-=(new_bandwidth-current_bandwith);
					}
					
				}

				
				
			} else
				return residual_spare;
			
			connection.updateConnection(new_bandwidth);
		}
		return residual_spare;
    }*/

    
    public long balanceDownloadQuickly( 
    		long dst_id) {
    	BroadcastDomain bd = 
    		this.local_map.get(Long.valueOf(dst_id));
		ConnectionList downloads = bd.getDownloads();
		int n_downloads = downloads.getSize();
		if(n_downloads==0)
			return 0L;
		long t_mean = bd.getNetCapacity()/n_downloads;
    	Node counterpart=null;
        NetworkController 
        counterpart_network= null;
    	long 	current_bandwith, 
		    	bandwidth_contribution,
		    	new_bandwidth;
    	Connection connection=null;

    	//spare bandwidth
    	long residual_spare = bd.getSpareDownload();
    	BroadcastDomain host=null, guest=null;
    	//check if downloads are available to
    	//update/balance
                
    	//upband: upload bandwidth from the source
    	long spare_upband=0L;

    	
        //sort connection numerically by bandwidth 
    	//(increasing order)
		//download_connections.sort();
    	//if bandwidth lacks, reverse it
    	//if (spare_bandwidth<0){ 
    	//	download_connections.reverse();
    	//}
    	
		//if this value is positive, it means that 
    	//the target connection has been increased
		//negative values mean more spare space
    	long spare_upband_src=0, spare_downband_dst=0;
		boolean inter =false;
		long max_share=0L,bwd=0L;
		for (int i = 0; i < n_downloads; i++) {
			connection = downloads.getElement(i);
			current_bandwith = connection.getBandwidth();
			new_bandwidth = current_bandwith;
			//is it inter connected??
			inter = (!(connection.isIntraConnected()));
			//counterpart source 
			counterpart = connection.getSender();
			counterpart_network = ((NetworkController) (counterpart.getProtocol(this.getProtocolID())));
			if(residual_spare>0){			
				//has more than needed, continue
				if(current_bandwith>t_mean){
					if(connection.getMinBandwidth()>=t_mean)
						continue;
					max_share=Math.min(current_bandwith-connection.getMinBandwidth(), current_bandwith-t_mean);
					new_bandwidth = max_share;
					connection.updateConnection(new_bandwidth);    					
					residual_spare+=(current_bandwith-new_bandwidth);				
					continue;
				}
				//bandwidth contribution definitions
				//
				//first of all, check if it is
				//an inter or intra connection
				//note that, inter means connection
				//among different peersim nodes
				if(inter){
					if(counterpart_network.getBroadcastDomain(BroadcastDomain.HOST_BD_ID).hasBottleneck()){
						host = counterpart_network.getBroadcastDomain(
								BroadcastDomain.HOST_BD_ID
						);
						guest = counterpart_network.getBroadcastDomain(
								Long.valueOf(connection.getSrcId())
						);
						spare_upband_src = 
							Math.min(host.getSpareUpload(),
									guest.getSpareUpload()
									);
					} else {
						guest = counterpart_network.getBroadcastDomain(
								Long.valueOf(connection.getSrcId())
						);
						spare_upband_src = guest.getSpareUpload();
						
					}
					if(this.getBroadcastDomain(BroadcastDomain.HOST_BD_ID).hasBottleneck()){
						host = this.getBroadcastDomain(BroadcastDomain.HOST_BD_ID);
						guest = this.getBroadcastDomain(
								Long.valueOf(connection.getDstId())
								);
						spare_downband_dst = 
							Math.min(
									host.getSpareDownload(),
									guest.getSpareDownload()
									);
						
					} else {
						guest = this.getBroadcastDomain(
								Long.valueOf(connection.getDstId())
								);
						spare_downband_dst = guest.getSpareDownload();
					}
					spare_upband = Math.min(spare_upband_src,spare_downband_dst);
				} else {
					host = counterpart_network.getBroadcastDomain(Long.valueOf(connection.getSrcId()));
					spare_upband = host.getSpareUpload();
				}
				if(i==(n_downloads-1))
					bandwidth_contribution = residual_spare;
				else
					bandwidth_contribution = t_mean - current_bandwith;
				//this.getUploadBandwidthUnmodified() method was by bd.getSoftUpload()
				//
				//fair sharing police, 
				//fair enougth when
				//there are spare bandwidth
				//because limits have already
				//been taken into account
				//when getSoftUpload was called
				//bandwidth_contribution = Math.abs(Math.round(((double)connection.getBandwidth()/((double)bd.getSoftUpload()))*(double)residual_spare_bandwidth));
				if(bandwidth_contribution>(spare_upband)){
					new_bandwidth = connection.getBandwidth()+spare_upband ;
				} else {
					new_bandwidth = connection.getBandwidth() + bandwidth_contribution;
				}
				if(new_bandwidth!=current_bandwith){
					bwd=new_bandwidth-current_bandwith;
					connection.updateConnection(new_bandwidth);    					
					residual_spare-=(bwd);
					
				}				
			} else
				return residual_spare;
			
		}
		return residual_spare;
    }
    public long balanceUploadQuickly(
    		long src_id) {
    	BroadcastDomain bd = 
    		this.local_map.get(Long.valueOf(src_id));
		ConnectionList uploads = bd.getUploads();
		int n_uploads = uploads.getSize();
		if(n_uploads==0)
			return 0L;
		long t_mean = bd.getNetCapacity()/n_uploads;
    	Node counterpart=null;
        NetworkController 
        counterpart_network= null;
    	long 	current_bandwith, 
		    	bandwidth_contribution,
		    	new_bandwidth;
    	Connection connection=null;

    	//spare bandwidth
    	long residual_spare = bd.getSpareUpload();
    	//check if uploads are available to
    	//update/balance
    	        
    	//upband: upload bandwidth from the source
    	long spare_downband=0;
    	
    	
        //sort connection numerically by bandwidth 
    	//(increasing order)
		//upload_connections.sort();
    	//if bandwidth lacks, reverse it
    	//if (spare_bandwidth<0){ 
    	//	upload_connections.reverse();
    	//}
    	
		//if this value is positive, it means that 
    	//the target connection has been increased
		//negative values mean more spare space
    	long spare_upband_src=0, spare_downband_dst=0;
		boolean inter =false;
		BroadcastDomain host=null,guest=null;
		long max_share=0L, bwd=0L;
		for (int i = 0; i < n_uploads; i++) {
			connection = uploads.getElement(i);
			current_bandwith = connection.getBandwidth();
			new_bandwidth = current_bandwith;
			//is it inter connected??
			inter = (!(connection.isIntraConnected()));
			//counterpart source 
			counterpart = connection.getReceiver();
			counterpart_network = ((NetworkController) (counterpart.getProtocol(this.getProtocolID())));
			if(residual_spare>0){
				//exceptions
				//has more than needed, continue
				if(current_bandwith>t_mean){
					if(connection.getMinBandwidth()>=t_mean)
						continue;
					max_share=Math.min(current_bandwith-connection.getMinBandwidth(), current_bandwith-t_mean);
					new_bandwidth = max_share;
					connection.updateConnection(new_bandwidth);    					
					residual_spare+=(current_bandwith-new_bandwidth);				
					continue;
				}
				//bandwidth contribution definitions
				//
				//first of all, check if it is
				//an inter or intra connection
				//note that, inter means connection
				//among different peersim nodes
				if(inter){
					if(counterpart_network.getBroadcastDomain(BroadcastDomain.HOST_BD_ID).hasBottleneck()){
						host = counterpart_network.getBroadcastDomain(BroadcastDomain.HOST_BD_ID);
						guest = counterpart_network.getBroadcastDomain(Long.valueOf(connection.getDstId()));
						spare_downband_dst = 
							Math.min(host.getSpareDownload(),
								guest.getSpareDownload());
						
					} else {
						guest = counterpart_network.getBroadcastDomain(Long.valueOf(connection.getDstId()));
						spare_downband_dst = guest.getSpareDownload();
						
					}
					if(this.getBroadcastDomain(BroadcastDomain.HOST_BD_ID).hasBottleneck()){
						host = this.getBroadcastDomain(BroadcastDomain.HOST_BD_ID);
						guest = this.getBroadcastDomain(Long.valueOf(connection.getSrcId()));
						spare_upband_src = 
							Math.min(
									host.getSpareUpload(),
									guest.getSpareUpload());
						
					}else {
						guest = this.getBroadcastDomain(Long.valueOf(connection.getSrcId()));
						spare_upband_src = guest.getSpareUpload();
						
					}
					spare_downband = Math.min(spare_upband_src,spare_downband_dst);
				} else {
					host = counterpart_network.getBroadcastDomain(Long.valueOf(connection.getDstId()));
					spare_downband = host.getSpareDownload();
				}
				if(i==(n_uploads-1))
					bandwidth_contribution = residual_spare;
				else
					bandwidth_contribution = t_mean - current_bandwith;
				//this.getUploadBandwidthUnmodified() method was by bd.getSoftUpload()
				//
				//fair sharing police, 
				//fair enougth when
				//there are spare bandwidth
				//because limits have already
				//been taken into account
				//when getSoftUpload was called
				//bandwidth_contribution = Math.abs(Math.round(((double)connection.getBandwidth()/((double)bd.getSoftUpload()))*(double)residual_spare_bandwidth));
				if(bandwidth_contribution>(spare_downband)){
					new_bandwidth = connection.getBandwidth()+spare_downband ;
				} else {
					new_bandwidth = connection.getBandwidth() + bandwidth_contribution;
				}
				if(new_bandwidth!=current_bandwith){
					bwd=new_bandwidth-current_bandwith;
					connection.updateConnection(new_bandwidth);    					
					residual_spare-=(bwd);
				}

				
				
			} else
				return residual_spare;		
		}
		return residual_spare;
		
    }

    
	public long getNewConnId(){
		return this.conn_id_gen++;
	}
	
	/**
	 * getConnRefusedCounter: gets the 
	 * number of connections that have 
	 * been refused
	 * 
	 * @return number of connections 
	 * refused
	 */
	public int getConnRefusedCounter(){
		return this.conn_ref_counter;
	}
	/**
	 * getDownloadReserved: retrieves the
	 * amount of download reserved
	 * 
	 * @return bwd in bits per second
	 */
	public long getAvailableDownBwd(long address){
		BroadcastDomain bd = 
			this.local_map.get(Long.valueOf(address));
		if(bd!=null)
			return (bd.getNetCapacity()-bd.getRevervedDownloadBwd());
		return NetworkController.ERROR_CODE;
	}
	
	/**
	 * getUploadReserved: retrieves the
	 * amount of upload free, netcapacity
	 * minus reserved bandwidth
	 * 
	 * @return bwd in bits per second
	 */
	public long getAvailableUpBwd(long address){
		BroadcastDomain bd = 
			this.local_map.get(Long.valueOf(address));
		if(bd!=null)
			return (bd.getNetCapacity()-bd.getRevervedUploadBwd());
		return NetworkController.ERROR_CODE;
	}
	
	/**
	 * getConnRetryCounter: gets the 
	 * number of new connections attempts
	 * 
	 * @return number of connections 
	 * retries
	 */
	public int getConnRetryCounter(){
		return this.conn_retry_counter;
	}
	public void addFiwBWTS(long t){
		this.fixbw_ts+=t;
	}
	public void resetFiwBWTS(){
		this.fixbw_ts=0;
	}
	public long getFiwBWTS(){
		return this.fixbw_ts;
	}
	public void addNetworkTime(long t){
		this.network_time+=t;
	}
	public void resetNetworkTime(){
		this.network_time=0;
	}
	public long getNetworkTime(){
		return this.network_time;
	}
	public void addReceiveDataTime(long t){
		this.receive_data_time+=t;
	}
	public void resetReceiveDataTime(){
		this.receive_data_time=0;
	}
	public long getReceiveDataTime(){
		return this.receive_data_time;
	}
	/*public void addAckTime(long t){
		this.ack_time+=t;
	}
	public void resetAckTime(){
		this.ack_time=0;
	}
	public long getAckTime(){
		return this.ack_time;
	}*/
	public void addCheckTime(long t){
		this.check_time+=t;
	}
	public void resetCheckTime(){
		this.check_time=0;
	}
	public long getCheckTime(){
		return this.check_time;
	}
	public void addBalanceTS(long t){
		this.balance_ts+=t;
	}
	public void resetBalanceTS(){
		this.balance_ts=0;
	}
	public long getBalanceTS(){
		return this.balance_ts;
	}
	public void addCloseTS(long t){
		this.close_ts+=t;
	}
	public void resetCloseTS(){
		this.close_ts=0;
	}
	public long getCloseTS(){
		return this.close_ts;
	}
	//for profiling close
	public void addCloseConn(long t){
		this.close_conn_ts+=t;
	}
	public void resetCloseConn(){
		this.close_conn_ts=0;
	}
	public long getCloseConn(){
		return this.close_conn_ts;
	}
	public void addNotifyCloseConn(long t){
		this.notify_close_conn_ts+=t;
	}
	public void resetNotifyCloseConn(){
		this.notify_close_conn_ts=0;
	}
	public long getNotifyCloseConn(){
		return this.notify_close_conn_ts;
	}
	public void addProcessFlow(long t){
		this.process_flow+=t;
	}
	public void resetProcessFlow(){
		this.process_flow=0;
	}
	public long getProcessFlow(){
		return this.process_flow;
	}
	
	//for profiling performEndTask
	public void addEndTask(long t){
		this.end_task+=t;
	}
	public void resetEndTask(){
		this.end_task=0;
	}
	public long getEndTask(){
		return this.end_task;
	}
	
	//counters
	/*
    	this.balance_up_counter=0;

	 */
	public void addEventCounter(){
		this.event_counter++;
	}
	public void resetEventCounter(){
		this.event_counter=0;
	}
	public long getEventCounter(){
		return this.event_counter;
	}
	public void addSendReqCounter(){
		this.send_req_counter++;
	}
	public void resetSendReqCounter(){
		this.send_req_counter=0;
	}
	public long getSendReqCounter(){
		return this.send_req_counter;
	}
	public void addSendRefCounter(){
		this.send_ref_counter++;
	}
	public void resetSendRefCounter(){
		this.send_ref_counter=0;
	}
	public long getSendRefCounter(){
		return this.send_ref_counter;
	}
	public void addUpdDownCounter(){
		this.upd_down_counter++;
	}
	public void resetUpdDownCounter(){
		this.upd_down_counter=0;
	}
	public long getUpdDownCounter(){
		return this.upd_down_counter;
	}
	public void addTransfAccCounter(){
		this.transfer_acc_counter++;
	}
	public void resetTransfAccCounter(){
		this.transfer_acc_counter=0;
	}
	public long getTransfAccCounter(){
		return this.transfer_acc_counter;
	}
	public void addSendAckCounter(){
		this.send_ack_counter++;
	}
	public void resetSendAckCounter(){
		this.send_ack_counter=0;
	}
	public long getSendAckCounter(){
		return this.send_ack_counter;
	}
	public void addBalanceUpCounter(){
		this.balance_up_counter++;
	}
	public void resetBalanceUpCounter(){
		this.balance_up_counter=0;
	}
	public long getBalanceUpCounter(){
		return this.balance_up_counter;
	}
	public void show(){
		BroadcastDomain bd = null;
		for (int i = 0; i < broadcasts.size(); i++) {
			bd = this.broadcasts.get(i);
			if(bd.active()){
				bd.show();
			}
		}
	}
	
    public void updateDownloadBDStatus( 
    		long address, boolean inter, long load){
    	BroadcastDomain guest_bd = this.getBroadcastDomain(Long.valueOf(address));
		long length=0L;
		int acc_conn=0;
		//update rx for destination
		if(guest_bd!=null){
			length=guest_bd.getRx();
			acc_conn=guest_bd.getAccNDownloads();
			guest_bd.setRx((length+load));
			guest_bd.setAccNDownloads((acc_conn+1));
			//update host domain if inter connected
			if(inter){
			  BroadcastDomain host_bd = 
				  this.getBroadcastDomain(BroadcastDomain.HOST_BD_ID);
				if((host_bd!=null)&&(host_bd!=guest_bd)){
					length=host_bd.getRx();
					acc_conn=host_bd.getAccNDownloads();
					host_bd.setRx((length+load));
					host_bd.setAccNDownloads((acc_conn+1));
				}
	  		}
		}
    }

    public void updateUploadBDStatus(
    		long address, boolean inter, long load){
    	BroadcastDomain guest_bd = this.getBroadcastDomain(Long.valueOf(address));
		long length=0L;
		int acc_conn=0;
		//update tx for source
		if(guest_bd!=null){
			length=guest_bd.getTx();
			acc_conn=guest_bd.getAccNUploads();
			guest_bd.setTx((length+load));
			guest_bd.setAccNUploads((acc_conn+1));
			//update host domain if inter connected
			if(inter){
			  BroadcastDomain host_bd = 
				  this.getBroadcastDomain(BroadcastDomain.HOST_BD_ID);
				if((host_bd!=null)&&(host_bd!=guest_bd)){
					length=host_bd.getTx();
					acc_conn=host_bd.getAccNUploads();
					host_bd.setTx((length+load));
					host_bd.setAccNUploads((acc_conn+1));
				}
	  		}
		}
    }
    
    public String getOutputStatus(){
    	String output="";
    	BroadcastDomain bd = null;
    	long connections=0L;
    	for (int i = 0; i < this.broadcasts.size(); i++) {
			bd = this.broadcasts.get(i);
			connections+=bd.getDownloads().getSize();
			connections+=bd.getUploads().getSize();
		}
    	output=String.valueOf(connections);
    	return output;
    }

}
