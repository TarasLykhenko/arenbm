package net;

import peersim.core.Node;

/**
 * Skeleton to initialize the data structure of the protocol.
 *
 * @author Alessandro Russo
 * @version $Revision: 0.02$
 */
public interface NetworkSkeleton {


    /**
     * force upload initialization by setting its value to 
     * them call again the initializer
     * 
     * This approach was chosen for compatibility reasons
     * 
     * @param bandwidth to this node.
     */
    //public void forceInitUpload(long bandwidth);

    
    /**
     * Initialize all the upload resources. It also initializes the download bandwidth invoking initDownload.
     * @param upload Upload bandwidth of the node.
     */
    //public void initUpload(long upload);

    /**
     * Set current download value.
     * @param download The download bandwidth of the node.
     */
    //public void setDownload(long download);


    /**
     * Maximum download available.
     * @param download_max Maximum download.
     */
    //public void setDownloadMax(long download_max);

    /**
     * Initialize download resources in the node. This method is invoking by initUpload and the download is set as ten passiveDownload times the upload.
     * @param download The download bandwidth of the node.
     */
    //public void initDownload(long download);

    /**
     * Method used for upload fluctuation. It will be implemented.
     */
    public void fluctuationUpload();

    /**
     * Method used for download fluctuation. It will be implemented.
     */
    public void fluctuationDownload();

    /**
     * return bandwidth/transport pid.
     */
    public int getProtocolID();

    /**
     * return client uplayer pid.
     */
    public int getUplayerProtocolID();
    /**
     * return bandwidth/transport pid.
     */
    public void setProtocolID(int pid);

    /**
     * return client uplayer pid.
     */
    public void setUplayerProtocolID(int uplayer_pid);

    /**
     * Initilize the object with corresponding data structure.
     */
    public void bootstrap(long id, Node n, 
    		long bw, long seed, boolean has_bottleneck,
    		long renew_delay, long retry_delay, int retry_attempts,
    		boolean bwd_control,float max_bwd_reservation_percentage);
    /**
     * Set the minimum bandwidth factor.
     * @param factor This double will determines the minimal spare bandwidth of up and downlinks compared to maximum value. For example, if up link is equal to 100Mbps and this factor is equal to .15, minimum acceptable bandwidth for uplink connections will be .15*100Mbps. 
     */
    //public void setMBF(double factor);
    /**
     * Set the definition of second in event time units.
     * @param evnet time units that represent a second. for exmaple, if time is equal to 1000, it means that one second represents 1000 time units of PeerSim 
     */
    public void setTS(long ts);
    /**
     * Set hard limit flag.
     * @param ts flag that determines how bandwidth limit will be enforced
     * true means that the strict value is enforced
     * false value allows higher bandwidth values to be set
     */
    public void setHardLimitFlag(boolean flag);
    
}
