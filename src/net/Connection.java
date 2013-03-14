package net;

import peersim.core.Node;
import peersim.core.CommonState;

/**
 * Reproduce the bandwidth connection element.<p>
 * Each transmission (e.g., chunk transfer) is made up by one or more connectione elements,
 * collected in the connection list structure.<p>
 * These elements cointains several information on the current transmission.
 *
 * @author Alessandro Russo
 * @version $Revision: 0.02$
 */
public class Connection {

    /**
     * Source/Sender node.
     */
    private Node src;
    /**
     * Destination/Receiver node.
     */
    private Node dst;
    /**
     * Bandwidth used.
     */
    private long bandwidth;


    /**
     * Minimum bandwidth required.
     */
    private long min_band;

    /**
     * bits to be transmitted.
     */
    private long load;
    /**
     * transmitted bits.
     */
    private double precise_tb;
    /**
     * Start time for bandwidth usage.
     */
    private long start_time;
    /**
     * End time for bandwidth usage.
     */
    private long end_time;
    /**
     * Connection identifier.
     */
    private long id;
    /**
     * Check whether the node has pending bandwidth to upload or not.
     */
    private boolean check;

    /**
     * A timestamp for update procedures.
     */
    private long update_timestamp;
    private long ts;

 	
 	private int n_pid;
 	private long src_id;
 	private long dst_id;
 	private long eedelay;
 	private long deadline;
 	private Object content;
 	
 	/**
 	 * defines how bandwidth limit is 
 	 * interpreted
 	 * 
 	 * if it is true, bandwidth limit is 
 	 * strictly enforced, neither lower
 	 * nor higher values are allowed
 	 */
 	private boolean hard_limit_flag;
 	
    /**
     * Constructor method.
     * @param sender Sender node, i.e. the current node.
     * @param receiver Receiver node.
     * @param band Bandwidth used.
     * @param start Start time for this connection.
     * @param end End time for this connection.
     * @param txid connection identifier.
     */
    public Connection(Node sender, Node receiver, 
    		long band, long start, long end, long id, 
    		long min_band, long ts, long load, 
    		int n_pid, long src_id, long dst_id, 
    		long eedelay,Object content, 
    		long deadline, boolean hard_limit_flag) {
    	this.hard_limit_flag=hard_limit_flag;
        this.n_pid = n_pid;
        this.src = sender;
        this.dst = receiver;
        this.ts=ts;
        if(band>=0)
        	this.bandwidth=(band);
        else
        	this.bandwidth=0L;
        this.start_time = start;
        this.end_time = end;
        this.id = id;
        this.check = false;
        this.update_timestamp=(CommonState.getTime()+eedelay);
        //this.update_timestamp=(CommonState.getTime());
        this.precise_tb=0.0d;
        if(min_band>0)
        	this.min_band=min_band;
        else
        	this.min_band=0L;
        this.deadline = deadline;
		this.load = load;
		this.src_id = src_id;
		this.dst_id = dst_id;
		this.eedelay = eedelay;
		this.content = content;
        //System.out.println("conn load: "+(band*((end-start)/1000)));
    }
    
    public Object getContent(){
    	return this.content;
    }
    public long getEEDelay(){
    	return this.eedelay;
    }
    public long getSrcId(){
    	return this.src_id;
    }

    public long getDstId(){
    	return this.dst_id;
    }

    public int getNPId(){
    	return this.n_pid;
    }
    
    /**
     * Get the sender node.
     * @return Node sender.
     */
    public Node getSender() {
        return this.src;
    }

    /**
     * Receiver node.
     * @return Node receiver.
     */
    public Node getReceiver() {
        return this.dst;
    }

    public boolean getHardLimitFlag(){
    	return this.hard_limit_flag;
    }
    /**
     * Set the bandwidth used in this connection element.
     * 
     * note that this checks the limit before setting
     * new value
     * 
     * @param band new bandwidth value
     * 
     * @return true if the new value is greater or
     * equal to the minimum limit
     */
    public boolean setBandwidth(long band) {
        if((this.hard_limit_flag)&&(this.min_band>0)) {
        	return false;
        } else if((band>=this.min_band)){
        	this.bandwidth = band;
        	return true;
        }
        return false;
    }
    
    /**
     * update some common connection attributes.
     */
    public long updateConnection(long band) {
    	long now = CommonState.getTime();
    	if(band==this.getBandwidth()){
        	this.updateTb();
    	} else {
        	this.updateTb();
            if(this.setBandwidth(band)){
        		long c_end = this.getEnd(); 
                long delay_to_end = Math.round((((double)this.load - this.precise_tb)/(double)this.bandwidth)*((double)this.ts));
                //this (+1) make sure that there will not be inconsistencies 
                //caused by Math.round
                //DEPRECATED CODE
                //if(delay_to_end==0)
                //	delay_to_end=1;
                
                long n_end = ((now)+delay_to_end);
                this.setEndtime(n_end);
                if((c_end!=n_end))
                	NetKernel.addModifiedConnection(c_end, this);
            }
            this.update_timestamp = (now);
    	}
        return this.end_time;
    }

    /**
     * Returns timestamp of the latest update.
     */
    public long getUpdateTimestamp() {
        return this.update_timestamp;
    }

    /**
     * set update time state to now (Common State simulation time).
     */
    public void setUpdateTimestamp() {
        this.update_timestamp = (CommonState.getTime());
    }

    /**
     * Get the bandwidth used in this connection element.
     * @return bandwidth used.
     */
    public long getBandwidth() {
        return this.bandwidth;
    }

    /**
     * getMinBandwidth(): get minimum bandwidth value
     * 
     * @return minimum bandwidth required.
     */
    public long getMinBandwidth() {
        return this.min_band;
    }

    /**
     * Get the start time for the connection element.
     * @return start time.
     */
    public long getStart() {
        return this.start_time;
    }

    /**
     * Get the end time for this connection.
     * @return End time for this connection.
     */
    public long getEnd() {
        return this.end_time;
    }

    /**
     *	getId: gets connection identifier
     *
     *  @return connection identifier.
     */
    public long getId() {
        return this.id;
    }

    /**
     * Set the start time for the connection element.
     * @param value Start time
     */
    public void setStarttime(long value) {
        this.start_time = value;
    }

    /**
     * Set the end time for the connection element.
     * @param value End time
     */
    public void setEndtime(long value) {
        this.end_time = value;
    }

    /**
     * Set check value, i.e. there is a pending bandwidth that will be used in the next future.
     */
    public void setCheck() {
        this.check = !check;
    }

    /**
     * Get the check value for checking whether a pending upload exists or not.
     * @return True if there is a pending upload of bandwidth, false otherwise.
     */
    public boolean getCheck() {
        return this.check;
    }


    /**
     * Check if the connection element is the same of the one given or not.
     * @param ce Connection element to compare.
     * @return True if thery are the same, false otherwise.
     */
    public boolean equals(Object c) {
    	Connection connection = (Connection)c;
    	if(connection.getId()==this.getId())
    		return true;
    	else
    		return false;
    	
    }

    /**
     * hashCode: is this object identifier.
     * 
     * @return hash code of the connection id.
     */
    public int  hashCode(){
    	return Long.valueOf(this.getId()).hashCode();    	
    }

    /**
     * Printable versione of connection element.
     * @return String containing information on current connection element.
     */
    public String toString() {
        return "| Src " + this.getSrcId() + " | Dst " + this.getDstId() + " | TxID  " + this.id +
                " | Bwd " + this.bandwidth + " | Start " + this.start_time + " | End " + this.end_time + " | min_bwd "
                + this.min_band + " | deadline "
                + this.deadline + " |" + this.content;
    }

    /**
     * Prints the valuse of the connection element.
     * @return String containing the values of current connection element.
     */
    public String getValues() {
        return this.src.getIndex() + "\t" + this.dst.getIndex() + "\t" + this.id +
                "\t" + this.bandwidth + "\t\t" + this.start_time + "\t" + this.end_time + "; ";
    }

    /**
     * Print the labels of the current connection element.
     * @return String with the labels of connection element.
     */
    public String getLabels() {
        return "SRC\tDEST\tTxID\tBandwidth\tStart\tEnd\t";
    }
    
    
    public long getLoad(){
    	return this.load;
    }
    
    public long updateTb() {
    	long now = CommonState.getTime();
    	//init load
    	double asize = 0.0d;
    	if(this.end_time<=now){
			this.precise_tb = (double)this.load;
		} else if (now>this.update_timestamp) {
    		asize=(
    				((double)this.bandwidth)*((
    						((double)(now-this.update_timestamp))/((double)this.ts)))
    						);
    		//rounding latest bits, increasing by 10th part, smoothly 
    		if ((this.precise_tb+asize) > ((double)this.load)) {
    			//it means that there exist
    			//a tiny, but acceptable
    			//imprecision, for transfers
    			//bigger than 1MB
    			//System.err.println("WARNING: unexpected behaviour in net.Connection. (cid, now, start, end, this.tb, asize,this.load,diff,jid,tix): ("+this.id+","+now+","+this.start_time+","+this.end_time+","+this.precise_tb+","+ asize+","+this.load+","+((this.precise_tb+asize) - this.load)+")");
    			this.precise_tb += ((((double)this.load)-this.precise_tb)/2);
    		}else{
    			this.precise_tb += asize;
    		}
    		
    	}
		this.update_timestamp=now;
    	return Math.round(this.precise_tb);
    }
    
    /**
     * getTb: gets transmitted bits.
     * 
     * @return current number of transmitted bits.
     */
    public long getTb() {
    	return Math.round(this.precise_tb);
    }

    /**
     * isIntraConnected: checks if
     * dst and src PeerSim nodes are
     * equal.
     * 
     * @return true if they are equal.
     */
    public boolean isIntraConnected() {
    	if(this.dst.getID()==this.src.getID())
    		return true;
    	else
    		return false;
    }
    public void show(){
    	System.err.println(this.id+","+this.end_time+","+this.bandwidth);
    }
    
    public long getDeadline(){
    	return this.deadline;
    }

    public void setDeadline(long deadline){
    	if(this.deadline>0)
    		if(deadline>0)
    			this.deadline=deadline;
    }
    
    
    public void setMinBandwidth(long min_bwd){
    	if(min_bwd==this.min_band || min_bwd<0)
    		return;
    	this.min_band=min_bwd;
    }
}
