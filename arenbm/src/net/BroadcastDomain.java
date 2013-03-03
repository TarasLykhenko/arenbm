/*
 * BradcastDomain.java        0.1 20110822
 *
 * Copyright 2011-2013 GuthembergSilvestre. 
 * All rights reserved.

 * Redistribution and use in source and binary 
 * forms, with or without modification, are permitted 
 * provided that the following conditions are met:
 * 
 *    1. Redistributions of source code must retain 
 *       the above copyright notice, this list of
 *       conditions and the following disclaimer.
 * 
 *    2. Redistributions in binary form must reproduce 
 *       the above copyright notice, this list of 
 *       conditions and the following disclaimer in 
 *       the documentation and/or other materials
 *       provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY GUTHEMBERGSILVESTRE 
 * ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, 
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A 
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT 
 * SHALL GuthembergSilvestre OR CONTRIBUTORS BE LIABLE 
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS 
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR 
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY 
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT 
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, 
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software 
 * and documentation are those of the authors and should 
 * not be interpreted as representing official policies, 
 * either expressed or implied, of Guthemberg Silvestre.
 * 
 */

package net;

import java.util.ArrayList;
import java.util.List;

import peersim.core.CommonState;
import peersim.core.Node;

/**
 * BroadcastDomain: encapsulates connections in the 
 * network.
 * 
 * Basically a Broadcast domain is composed of :
 * -> id
 * -> two connection lists (an upload and a download)
 * -> bandwidth maximum capacity
 *  
 * @version 0.1 20110822
 * @author Guthemberg Silvestre
 *
 */

public class BroadcastDomain {
    /**
     * HOST_BD_ID: host broadcast domain
     * identifier
     * 
     * This is the main broadcast domain, and it
     * can not be removed.
     * 
     */
    public static final 
    int HOST_BD_ID = 0;

	private long renew_delay;
	
	/*
	 * id: broadcast identifier
	 */
	private int id;

	/*
	 * net_capacity: is the maximum network capacity
	 * rate in bits per seconds
	 */
	private long net_capacity;

	/*
	 * uploads: list of upload connections
	 */
	private ConnectionList uploads;

	/*
	 * downloads: list of download connections
	 */
	private ConnectionList downloads;

	/*
	 * uploads: list of upload connections
	 */
	private ConnectionList changable_uploads;

	/*
	 * downloads: list of download connections
	 */
	private ConnectionList changable_downloads;

	/*
	 * address: identifiers that use this 
	 * broadcast domain
	 */
	private List<Long> address;

	/**
	 * tx: transmitted number of bits
	 * it is a state variable
	 */
	private long tx;

	/**
	 * tx: received number of bits
	 * it is a state variable
	 */
	private long rx;

	/**
	 * t_tx: transient transmitted 
	 * number of bits
	 * from active flows only
	 */
	private long t_tx;

	/**
	 * t_rx: transient received 
	 * number of bits
	 * from active flows only
	 */
	private long t_rx;
	
	/**
	 * accomplished_upload: finished 
	 * flows counter
	 */
	private int accomplished_upload;

	/**
	 * accomplished_download: finished 
	 * counter
	 */
	private int accomplished_download;

	/**
	 * failed_uploads: failed uploads 
	 * counter
	 */
	private int failed_uploads;
	/**
	 * failed_downloads: failed downloads 
	 * counter
	 */
	private int failed_downloads;
	/**
	 * failed_uploads: failed uploads 
	 * counter
	 */
	private int refused_uploads;
	/**
	 * failed_downloads: failed downloads 
	 * counter
	 */
	private int refused_downloads;

	private boolean has_bottleneck;
	
	/**
	 * BroadcastDomain: is a default constructor 
	 * 
	 * note: when a new broadcast domain is created, 
	 * the two connections lists (up and down) are 
	 * initialised
	 * 
	 * @param id broadcast identifier to be defined 
	 * by network controller
	 * @param net_capacity network capacity rate in 
	 * bits per second. this is the maximum bandwidth
	 * rate for both download and upload connections
	 */
	public BroadcastDomain(int id, long net_capacity, boolean has_bottleneck, long renew_delay) {
		this.net_capacity=net_capacity;
		this.id=id;
		this.downloads = new ConnectionList();
		this.uploads = new ConnectionList();
		this.address=new ArrayList<Long>();
		this.changable_downloads = new ConnectionList();
		this.changable_uploads = new ConnectionList();
		this.tx=0L;
		this.rx=0L;
		this.t_tx=0L;
		this.t_rx=0L;
		this.accomplished_upload=0;
		this.accomplished_download=0;
		this.failed_uploads=0;
		this.failed_downloads=0;
		this.refused_uploads=0;
		this.refused_downloads=0;
		this.has_bottleneck=has_bottleneck;
		this.renew_delay = renew_delay;
	}


	/**
	 * reset: resets broadcast domain status
	 */
	public void clear(){
		this.downloads.clean();
		this.uploads.clean();
		this.changable_downloads.clean();
		this.changable_uploads.clean();
		this.tx=0L;
		this.rx=0L;
		this.t_tx=0L;
		this.t_rx=0L;
		this.accomplished_upload=0;
		this.accomplished_download=0;
		this.failed_uploads=0;
		this.failed_downloads=0;
		this.refused_uploads=0;
		this.refused_downloads=0;
	}
	
	//common gets
	/**
	 * getId: gets the broadcast identifier
	 * 
	 * @return (integer) identifier
	 */
	public int getId() {
		return this.id;
	}
	
	/**
	 * getNetCapacity: gets the maximum network 
	 * capacity in bits per second
	 * 
	 * This maximum limit must be applied over
	 * both upload and download connections
	 * 
	 * @return maximum bandwidth rate in bits per 
	 * second
	 */
	public long getNetCapacity() {
		return this.net_capacity;
	}

	/**
	 * getConnection: gets a specific connection
	 * with id c_id
	 * 
	 * @return null if the connection does not
	 * exist
	 */
	public Connection getConnection(long c_id) {
		Connection c = this.downloads.getConnection(c_id);
		if(c!=null)
			return c;
		else
			return this.uploads.getConnection(c_id);
		//search in downloads
		/*for (int i = 0; i < this.downloads.getSize(); i++) {
			if(c_id == 
				this.downloads.getElement(i).getId())
				return this.downloads.getElement(i);
		}
		//search in uploads
		for (int i = 0; i < this.uploads.getSize(); i++) {
			if(c_id == 
				this.uploads.getElement(i).getId())
				return this.uploads.getElement(i);
		}
		return null;
		*/
	}

	/**
	 * getDownloads: gets the list of download 
	 * connections
	 * 
	 * @return download connections list
	 */
	public ConnectionList getDownloads() {
		return this.downloads;
	}

	public int getNumberOfDownloads(){
		return this.downloads.getSize();
	}
	/**
	 * getAddresses: gets the list of 
	 * node addresses
	 * 
	 * @return list of address
	 */
	public List<Long> getAddresses() {
		return this.address;
	}

	/**
	 * getUploads: gets the list of upload 
	 * connections
	 * 
	 * @return upload connections list
	 */
	public ConnectionList getUploads() {
		return this.uploads;
	}

	/**
	 * getUpload: gets a specific 
	 * upload
	 * 
	 * @param connection id
	 * 
	 * @return upload connection
	 */
	public Connection getUpload(long c_id) {
		return this.uploads.getConnection(c_id);
		/*Connection c = null;
		for (int i = 0; i < this.uploads.getSize(); i++) {
			c = this.uploads.getElement(i);
			if(c.getId()==c_id)
				return c;
		}
		return null;*/
	}
	/**
	 * getDownload: gets a specific 
	 * download connection
	 * 
	 * @param connection id
	 * 
	 * @return download connection
	 */
	public Connection getDownload(long c_id) {
		return this.downloads.getConnection(c_id);
		/*Connection c = null;
		for (int i = 0; i < this.downloads.getSize(); i++) {
			c = this.downloads.getElement(i);
			if(c.getId()==c_id)
				return c;
		}
		return null;*/
	}

	/**
	 * getDownloadConsuption: returns how many
	 * download bandwidth is being used 
	 * 
	 * @return the sum of all download bandwidths
	 */
	public long getDownloadConsuption() {
		long consumption=0;
		Connection c = null;
		//ConnectionList to_be_closed = new ConnectionList();
		//long now = CommonState.getTime();
		//boolean has_residual = false;
		for (int i = 0; i < this.downloads.getSize(); i++) {
			c = this.downloads.getElement(i);
			/*if(c.getEnd()<=now){
				//to_be_closed.addConnection(c);
				has_residual = true;
			} else*/
				consumption+=c.getBandwidth();
		}
		/*if(has_residual){
			for (int i = 0; i < to_be_closed.getSize(); i++) {
				c = to_be_closed.getElement(i);
				this.close(c);
			}
		}*/
		return consumption;
	}

	/**
	 * getUploadConsuption: returns how many
	 * upload bandwidth is being used 
	 * 
	 * @return the sum of all upload bandwidths
	 */
	public long getUploadConsuption() {
		long consumption=0;
		Connection c = null;
		/*ConnectionList to_be_closed = new ConnectionList();
		long now = CommonState.getTime();
		boolean has_residual = false;*/
		for (int i = 0; i < this.uploads.getSize(); i++) {
			c = this.uploads.getElement(i);
			/*if(c.getEnd()<=now){
				to_be_closed.addConnection(c);
				has_residual = true;
			} else*/
				consumption+=c.getBandwidth();
		}
		/*if(has_residual){
			for (int i = 0; i < to_be_closed.getSize(); i++) {
				c = to_be_closed.getElement(i);
				this.close(c);
			}
		}*/
		return consumption;
	}
	
	/**
	 * getIntraDownloadConsuption: returns how many
	 * download bandwidth is being used 
	 * 
	 * intra connected connections only. it means 
	 * connections using the same network layer
	 * 
	 * @return the sum of  download bandwidths
	 */
	public long getIntraDownloadConsuption() {
		long consumption=0;
		Connection c = null;
		for (int i = 0; i < this.downloads.getSize(); i++) {
			c = this.downloads.getElement(i);
			if(c.isIntraConnected())
				consumption+=c.getBandwidth();
		}
		return consumption;
	}

	/**
	 * getUploadConsuption: returns how many
	 * upload bandwidth is being used 
	 * 
	 * intra connected connections only. it means 
	 * connections using the same network layer
	 * 
	 * @return the sum of upload bandwidths
	 */
	public long getIntraUploadConsuption() {
		long consumption=0;
		Connection c = null;
		for (int i = 0; i < this.uploads.getSize(); i++) {
			c = this.uploads.getElement(i);
			if(c.isIntraConnected())
				consumption+=c.getBandwidth();
		}
		return consumption;
	}
	/**
	 * return the number of download contributors
	 * for a new connection
	 * 
	 * @return contributors counter
	 */
	public int getDownloadContibutors() {
		//1 here bellow means a new connection
		//we are actually computing here the
		//the amount of bandwidth for a new 
		long now = CommonState.getTime();
		int contributors=0;
		Connection c = null;
		//theoretical mean
		long t_mean = Math.round((double)this.getNetCapacity()/(double)this.downloads.getSize());
		for (int i = 0; i < this.downloads.getSize(); i++) {
			c = this.downloads.getElement(i);
			boolean oudated = (c.getUpdateTimestamp()<(now));
			boolean available = true;
			if ((c.getMinBandwidth()>=t_mean))
				available=false;
			if(oudated&&available)
				contributors++;
		}
		return contributors;
	}
	/**
	 * return the number of download contributors
	 * for a new connection
	 * 
	 * @return contributors counter
	 */
	public int getNumberModifiableDownloads() {
		//1 here bellow means a new connection
		//we are actually computing here the
		//the amount of bandwidth for a new 
		int contributors=0;
		Connection c = null;
		//theoretical mean
		long t_mean = Math.round((double)this.getNetCapacity()/(double)this.downloads.getSize());
		for (int i = 0; i < this.downloads.getSize(); i++) {
			c = this.downloads.getElement(i);
			if ((c.getMinBandwidth()<t_mean))
				contributors++;
		}
		return contributors;
	}
	/**
	 * return the number of download contributors
	 * for a new connection
	 * 
	 * @return contributors counter
	 */
	public int getNumberModifiableUploads() {
		//1 here bellow means a new connection
		//we are actually computing here the
		//the amount of bandwidth for a new 
		int contributors=0;
		Connection c = null;
		//theoretical mean
		long t_mean = Math.round((double)this.getNetCapacity()/(double)this.uploads.getSize());
		for (int i = 0; i < this.uploads.getSize(); i++) {
			c = this.uploads.getElement(i);
			if ((c.getMinBandwidth()<t_mean))
				contributors++;
		}
		return contributors;
	}
	/**
	 * return the number of download contributors
	 * for a new connection
	 * 
	 * @return contributors counter
	 */
	public ConnectionList getDownloadContibutors(long contribution) {
		//1 here bellow means a new connection
		//we are actually computing here the
		//the amount of bandwidth for a new 
		long now = CommonState.getTime();
		ConnectionList contributors= new ConnectionList();
		Connection c = null;
		for (int i = 0; i < this.downloads.getSize(); i++) {
			c = this.downloads.getElement(i);
			boolean oudated = (c.getUpdateTimestamp()<(now));
			boolean available = true;
			if (((c.getMinBandwidth())>(c.getBandwidth()-contribution)))
				available=false;
			if(oudated&&available)
				contributors.addConnection(c);
		}
		return contributors;
	}
	/**
	 * return the number of download contributors
	 * for a new connection
	 * 
	 * @return contributors counter
	 */
	public int getUploadContibutors() {
		//1 here bellow means a new connection
		//we are actually computing here the
		//the amount of bandwidth for a new 
		int contributors=0;
		long now = CommonState.getTime();
		Connection c = null;
		//theoretical mean
		long t_mean = Math.round((double)this.getNetCapacity()/(double)this.uploads.getSize());
		for (int i = 0; i < this.uploads.getSize(); i++) {
			c = this.uploads.getElement(i);
			boolean oudated = (c.getUpdateTimestamp()<(now));
			boolean available = true;
			if((c.getMinBandwidth()>=t_mean))
				available=false;
			if(oudated&&available)
				contributors++;
		}
		return contributors;
	}
	/**
	 * return the number of download contributors
	 * for a new connection
	 * 
	 * @return contributors counter
	 */
	/*public ConnectionList getUploadDonners() {
		//1 here bellow means a new connection
		//we are actually computing here the
		//the amount of bandwidth for a new 
		ConnectionList contributors=new ConnectionList();
		long now = CommonState.getTime();
		Connection c = null;
		boolean available=true,
			outdated = true;
		for (int i = 0; i < this.uploads.getSize(); i++) {
			c = this.uploads.getElement(i);
			outdated = (c.getUpdateTimestamp()<now);
			available = true;
			if(((c.getMinBandwidth())==(c.getBandwidth()))){
				available=false;
			}
			if(outdated&&available)
				contributors.addConnection(c);
		}
		return contributors;
	}
	public ConnectionList getDownloadDonners() {
		//1 here bellow means a new connection
		//we are actually computing here the
		//the amount of bandwidth for a new 
		ConnectionList contributors=new ConnectionList();
		long now = CommonState.getTime();
		Connection c = null;
		boolean outdated=true, available=true;
		for (int i = 0; i < this.downloads.getSize(); i++) {
			c = this.downloads.getElement(i);
			outdated = (c.getUpdateTimestamp()<now);
			available = true;
			if((c.getMinBandwidth())==(c.getBandwidth())){
				available=false;
			}
			if(outdated&&available)
				contributors.addConnection(c);
		}
		return contributors;
	}*/
	public long getDownloadDonnersReload() {
		//1 here bellow means a new connection
		//we are actually computing here the
		//the amount of bandwidth for a new 
		Connection c = null;
		long spare = this.getSpareDownload();
		if(spare>=0)
			return spare;
		//consider that spare is always negative
		//new connections
		//t_mean = Math.round(((double)(this.getNetCapacity()+spare))/((double)this.downloads.getSize()));
		long n_mean = Math.round(((double)(this.getNetCapacity()))/((double)this.downloads.getSize()));
//		long contrib=0L;	
		long new_bandwidth=0;
		for (int i = 0; i < this.downloads.getSize(); i++) {
			c = this.downloads.getElement(i);
			new_bandwidth=n_mean;
			//last connection
			if(i==(this.downloads.getSize()-1)){
				new_bandwidth=c.getBandwidth()+spare;
			}
			if(new_bandwidth>0){
				spare+=(c.getBandwidth()-new_bandwidth);
				c.updateConnection(new_bandwidth);
			}
		}
		return spare;
		
	}

	public long getDownloadDonnersReloadMin() {
		//1 here bellow means a new connection
		//we are actually computing here the
		//the amount of bandwidth for a new 
		Connection c = null;
		long spare = this.getSpareDownload();
		if(spare>=0)
			return spare;
		//consider that spare is always negative
		//new connections
		long contribution=0L;
		int n =this.downloads.getSize();
		this.downloads.sortMinBwd();this.downloads.reverse();
		long total = this.getNetCapacity();
		long t_mean = Math.round(((double)(total))/((double)n));
		long new_bandwidth=0L;
		for (int i = 0; i < this.downloads.getSize(); i++) {
			c = this.downloads.getElement(i);
			new_bandwidth=Math.max(c.getMinBandwidth(), t_mean);
			if((new_bandwidth<c.getBandwidth())){
				contribution=c.getBandwidth()-new_bandwidth;
				spare+=(contribution);
				c.updateConnection(new_bandwidth);
			}
			total-=c.getBandwidth();
			n--;
			t_mean = Math.round(((double)(total))/((double)n));
			if(spare>=0)
				return spare;
		}
		return spare;
	}
	
	public long getUploadDonnersReload() {
		//1 here bellow means a new connection
		//we are actually computing here the
		//the amount of bandwidth for a new 
		Connection c = null;
		long spare = this.getSpareUpload();
		if(spare>=0)
			return spare;
		//consider that spare is always negative
		//new connections
		//t_mean = Math.round(((double)(this.getNetCapacity()+spare))/((double)this.downloads.getSize()));
		long n_mean = Math.round(((double)(this.getNetCapacity()))/((double)this.uploads.getSize()));
//		long contrib=0L;	
		long new_bandwidth=0;
		for (int i = 0; i < this.uploads.getSize(); i++) {
			c = this.uploads.getElement(i);
			new_bandwidth=n_mean;
			//last connection
			if(i==(this.uploads.getSize()-1)){
				new_bandwidth=c.getBandwidth()+spare;
			}
			if(new_bandwidth>0){
				spare+=(c.getBandwidth()-new_bandwidth);
				c.updateConnection(new_bandwidth);
			}
		}
		return spare;	
	}
	
	public long getUploadDonnersReloadMin() {
		//1 here bellow means a new connection
		//we are actually computing here the
		//the amount of bandwidth for a new 
		Connection c = null;
		long spare = this.getSpareUpload();
		if(spare>=0)
			return spare;
		long contribution=0L;
		int n =this.uploads.getSize();
		this.uploads.sortMinBwd();this.uploads.reverse();
		long total = this.getNetCapacity();
		long t_mean = Math.round(((double)(total))/((double)n));
		long new_bandwidth=0L;
		for (int i = 0; i < this.uploads.getSize(); i++) {
			c = this.uploads.getElement(i);
			new_bandwidth=Math.max(c.getMinBandwidth(), t_mean);
			if((new_bandwidth<c.getBandwidth())){
				contribution=c.getBandwidth()-new_bandwidth;
				spare+=(contribution);
				c.updateConnection(new_bandwidth);
			}
			total-=c.getBandwidth();
			n--;
			t_mean = Math.round(((double)(total))/((double)n));
			if(spare>=0)
				return spare;
		}
		return spare;
	}
	
	/**
	 * getSoftDownload: returns how many
	 * download bandwidth is available for 
	 * fair sharing<br>
	 * <br>
	 * soft means bandwidth that can be taken
	 * into account (in other words, the sum will
	 * take into account connections
	 * with timestamp smaller than now 
	 * and bandwidth limits subtracted)<br>
	 * <br>
	 * @return the sum of  downloads bandwidth
	 * available
	 */
	
	public long getSoftDownload() {
		//we are actually computing here the
		//the amount of bandwidth for a new 
		long b_available=this.getNetCapacity();
		Connection c = null;
		long now = CommonState.getTime();
		/*ConnectionList to_be_closed = new ConnectionList();
		boolean has_residual = false;*/
		for (int i = 0; i < this.downloads.getSize(); i++) {
			c = this.downloads.getElement(i);
			/*if(c.getEnd()<=now){
				to_be_closed.addConnection(c);
				has_residual = true;
			} else {*/				
				if(c.getUpdateTimestamp()==(now))
					b_available-=c.getBandwidth();
				else
					b_available-=c.getMinBandwidth();
			//}
		}
		/*if(has_residual){
			for (int i = 0; i < to_be_closed.getSize(); i++) {
				c = to_be_closed.getElement(i);
				this.close(c);
			}
		}*/
		return b_available;
	}

	/**
	 * getSoftUpload: returns how many
	 * upload bandwidth is available for 
	 * fair sharing<br>
	 * <br>
	 * soft means bandwidth that can be taken
	 * into account (in other words, the sum will
	 * take into account connections
	 * with timestamp smaller than now 
	 * and bandwidth limits subtracted)<br>
	 * <br>
	 * @return the sum of soft uploads bandwidth
	 * 
	 */
	/*
	long consumption=0;
	Connection c = null;
	ConnectionList to_be_closed = new ConnectionList();
	long now = CommonState.getTime();
	boolean has_residual = false;
	for (int i = 0; i < this.uploads.getSize(); i++) {
		c = this.uploads.getElement(i);
		if(c.getEnd()<=now){
			to_be_closed.addConnection(c);
			has_residual = true;
		} else
			consumption+=this.uploads.getElement(i).getBandwidth();
	}
	if(has_residual){
		for (int i = 0; i < to_be_closed.getSize(); i++) {
			c = to_be_closed.getElement(i);
			this.close(c);
		}
	}
*/
	public long getModifiableUpload(){		
		long b_available=this.getNetCapacity();
		Connection c = null;
		for (int i = 0; i < this.uploads.getSize(); i++) {
			c = this.uploads.getElement(i);
			b_available-=c.getMinBandwidth();
		}
		return b_available;
	}
	
	public long getModifiableDownload(){		
		long b_available=this.getNetCapacity();
		Connection c = null;
		for (int i = 0; i < this.downloads.getSize(); i++) {
			c = this.downloads.getElement(i);
			b_available-=c.getMinBandwidth();
		}
		return b_available;
	}
	
	public long getSoftUpload() {
		long b_available=this.getNetCapacity();
		Connection c = null;
		long now = CommonState.getTime();
		/*ConnectionList to_be_closed = new ConnectionList();
		boolean has_residual = false;*/
		for (int i = 0; i < this.uploads.getSize(); i++) {
			c = this.uploads.getElement(i);
			/*if(c.getEnd()<=now){
				to_be_closed.addConnection(c);
				has_residual = true;
			} else {*/
				if(c.getUpdateTimestamp()==(now))
					b_available-=c.getBandwidth();
				else
					b_available-=c.getMinBandwidth();
			//}
		}
		/*if(has_residual){
			for (int i = 0; i < to_be_closed.getSize(); i++) {
				c = to_be_closed.getElement(i);
				this.close(c);
			}
		}*/
		return b_available;
	}
	/**
	 * getUnmodifiedUpload: returns how many
	 * upload bandwidth is unmodified<br>
	 * <br>
	 * it removes also the minimum bandwidth<br>
	 * <br>
	 * @return the sum of unmodified uploads bandwidth
	 * 
	 */
	public long getUnmodifiedUpload() {
		long ubwd=0L;
		Connection c = null;
		long now = CommonState.getTime();
		for (int i = 0; i < this.uploads.getSize(); i++) {
			c = this.uploads.getElement(i);
			if(c.getUpdateTimestamp()<(now))
				ubwd+=(c.getBandwidth()-c.getMinBandwidth());
		}
		return ubwd;
	}
	/**
	 * getUnmodifiedUpload: returns how many
	 * upload bandwidth is unmodified<br>
	 * <br>
	 * it removes also the minimum bandwidth<br>
	 * <br>
	 * @return the sum of unmodified uploads bandwidth
	 * 
	 */
	public long getUnmodifiedDownload() {
		long ubwd=0L;
		Connection c = null;
		long now = CommonState.getTime();
		for (int i = 0; i < this.downloads.getSize(); i++) {
			c = this.downloads.getElement(i);
			if(c.getUpdateTimestamp()<(now))
				ubwd+=(c.getBandwidth()-c.getMinBandwidth());
		}
		return ubwd;
	}
	/**
	 * getInterDownloadConsumption: returns
	 * the sum of download inter-prresim node 
	 * connections
	 * 
	 * @return the sum of  downloads bandwidth
	 */
	public long getInterDownloadConsumption() {
		long consumption=0L;
		Connection c = null;
		boolean inter=false;
		for (int i = 0; i < this.downloads.getSize(); i++) {
			c = this.downloads.getElement(i);
			inter = (!(c.isIntraConnected()));
			if(inter)
				consumption+=c.getMinBandwidth();
		}
		return consumption;
	}

	/**
	 * getInterUploadConsumption: returns
	 * the sum of upload inter-peersim node 
	 * connections
	 * 
	 * @return the sum of  uploads bandwidth
	 */
	public long getInterUploadConsumption() {
		long consumption=0L;
		Connection c = null;
		boolean inter=false;
		for (int i = 0; i < this.uploads.getSize(); i++) {
			c = this.uploads.getElement(i);
			inter = (!(c.isIntraConnected()));
			if(inter)
				consumption+=c.getMinBandwidth();
		}
		return consumption;
	}

	/**
	 * getTx: gets the sum of transmitted
	 * bits 
	 * 
	 * @return number of transmitted bits
	 */
	public long getTx(){
		return this.tx;
	}
	/**
	 * getRx: gets the sum of received
	 * bits 
	 * 
	 * @return number of received bits
	 */
	public long getRx(){
		return this.rx;
	}
	/**
	 * getTransientTx: transient transmitted 
	 * number of bits
	 * from active flows only
	 * 
	 * @return number of transient
	 * transmitted bits
	 */
	public long getTransientTx(){
		return this.t_tx;
	}
	/**
	 * getTransientRx: transient transmitted 
	 * number of bits
	 * from active flows only
	 * 
	 * @return number of transient
	 * received bits
	 */
	public long getTransientRx(){
		return this.t_rx;
	}
	
	/**
	 * getAccNUploads(): number of accomplished
	 * upload connections
	 * 
	 * @return number of finished flows
	 */
	public int getAccNUploads(){
		return this.accomplished_upload;
	}

	/**
	 * getAccNDownload(): number of accomplished
	 * download connections
	 * 
	 * @return number of finished flows
	 */
	public int getAccNDownloads(){
		return this.accomplished_download;
	}
	/**
	 * getFailedUploads: number of failed
	 * upload connections
	 * 
	 * @return number of failed flows
	 */
	public int getFailedUploads(){
		return this.failed_uploads;
	}
	public int getRefusedUploads(){
		return this.refused_uploads;
	}
	/**
	 * getFailedDownloads: number of failed
	 * download connections
	 * 
	 * @return number of failed download
	 * connections
	 */
	//public int getFailedDownloads(){
	//	return this.failed_downloads;
	//}
	//public int getRefsedDownloads(){
	//	return this.refused_downloads;
	//}

	//common add methods
	/**
	 * addAddress: adds a new address to
	 * this broadcast domain
	 * 
	 * @return true if it was added
	 * successfully
	 */
	protected boolean addAddresse(long id) {
		boolean new_address = (!(this.address.contains(Long.valueOf(id))));
		if(new_address){
			this.address.add(Long.valueOf(id));
			return true;
		}
		return false;
		
	}
	/**
	 * addDownload: adds a download connections to
	 * this broadcast domain
	 * 
	 * Before adding the new connection, it checks
	 * a) if it is really a new connection to this
	 * broadcast domain
	 * b) if this broadcast domain has available capacity
	 * for accepts this new connection
	 * 
	 * @return true if there is enough bandwidth
	 * available for this new connection
	 */
	public boolean addDownloadByCheck(Connection c) {
		boolean exists = (this.downloads.contains(c));
		if(exists)
			return false;
		long band_used = this.getDownloadConsuption();
		if((c.getBandwidth()+band_used)<=this.net_capacity){
			this.downloads.addConnection(c);
			return true;
		} else {
			return false;
		}
	}
	/**
	 * forceAddDownload: inserts a new connection
	 * to this broadcast domain without checking
	 * available resources
	 * 
	 * note that it is useful for procedures of
	 * connection creation
	 * 
	 * @return true if succeeds 
	 */
	public boolean addDownload(Connection c) {
		boolean exists = (this.downloads.contains(c));
		if(exists)
			return false;
		else{
			this.downloads.addConnection(c);
			return true;
		}
	}
	/**
	 * forceAddUpload: inserts a new connection
	 * to this broadcast domain without checking
	 * available resources
	 * 
	 * note that it is useful for procedures of
	 * connection creation
	 * 
	 * @return true if succeeds 
	 */
	public boolean addUpload(Connection c) {
		boolean exists = (this.uploads.contains(c));
		if(exists)
			return false;
		else{
			this.uploads.addConnection(c);
			return true;
		}
	}

	/**
	 * addUpload: adds a upload connections to
	 * this broadcast domain
	 * 
	 * @return true if there is enough bandwidth
	 * available for this new connection
	 */
	public boolean addUploadByCheck(Connection c) {
		boolean exists = (this.uploads.contains(c));
		if(exists)
			return false;
		long band_used = this.getUploadConsuption();
		if((c.getBandwidth()+band_used)<=this.net_capacity){
			this.uploads.addConnection(c);
			return true;
		} else {
			return false;
		}
	}
	//common remove methods
	/**
	 * removesAddress: removes a address to
	 * this broadcast domain
	 * 
	 * @return true if it was removed
	 * successfully
	 */
	protected boolean removeAddresse(long id) {
		return this.address.remove(Long.valueOf(id));		
	}
	/**
	 * removeDownload: removes a download connection from
	 * this broadcast domain
	 * 
	 * @param conn_id connection identifier
	 * 
	 * @return the removed connection or null if
	 * it does not exist 
	 */
	/*public Connection removeDownload(long conn_id) {
		Connection download=null;
		for (int i = 0; i < this.downloads.getSize(); i++) {
			download = this.downloads.getElement(i);
			if(download.getId()==conn_id)
				return download;
		}
		return null;
	}*/

	/**
	 * removeUpload: removes a upload connection from
	 * this broadcast domain
	 * 
	 * @param conn_id connection identifier
	 * 
	 * @return the removed connection or null if
	 * it does not exist 
	 */
	/*public Connection removeUpload(long conn_id) {
		Connection upload=null;
		for (int i = 0; i < this.uploads.getSize(); i++) {
			upload = this.uploads.getElement(i);
			if(upload.getId()==conn_id)
				return upload;
		}
		return null;
	}*/
	
	//overwriting important Object methods
    /**
     * Check if the connection element is the same of the one given or not.
     * @param ce Connection element to compare.
     * @return True if thery are the same, false otherwise.
     */
    public boolean equals(Object c) {
    	BroadcastDomain bd= (BroadcastDomain)c;
    	if(bd.getId()==this.getId())
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
    	return Integer.valueOf(this.getId()).hashCode();    	
    }



    /**
     * getSpareUpload: gets spare upload 
     * bandwidth
     * 
     * @return amount of spare bandwidth
     * in bits per second.
     */
    public long getSpareUpload() {
    	//get spare download
    	return this.net_capacity-this.getUploadConsuption();
    }
    
    /**
     * getSpareDownload: gets spare download 
     * bandwidth
     * 
     * @return amount of spare bandwidth
     * in bits per second.
     */
    public long getSpareDownload() {
    		return this.net_capacity-this.getDownloadConsuption();
    }
    /**
     * getNFinishedConnections: gets the total
     * number of finished connections
     * 
     * It sums up all types of connections, including 
     * failed ones
     * 
     * @return number of finished connections.
     */
    public int getNFinishedConnections() {
    	return this.failed_downloads+this.failed_uploads+
    	this.refused_downloads+this.refused_uploads+
    	this.accomplished_download+this.accomplished_upload;
    }

    public int getNAccomplishedUpConnections() {
    	return this.accomplished_upload;
    }
    public int getNFailedUpConnections() {
    	return this.failed_uploads;
    }
    public int getNRefusedUpConnections() {
    	return this.refused_uploads;
    }

    public int getNFailedDownConnections() {
    	return this.failed_downloads;
    }
    public int getNRefusedDownConnections() {
    	return this.refused_downloads;
    }
    
    public int getNAccomplishedDownConnections() {
    	return this.accomplished_download;
    }

    //common set methods
	/**
	 * setTx: sets the sum of transmitted
	 * bits 
	 * 
	 * @return tx number of transmitted 
	 * bits
	 */
	public void setTx(long tx){
		this.tx=tx;
	}
	/**
	 * setRx: sets the sum of received
	 * bits 
	 * 
	 * @param rx number of received bits
	 */
	public void setRx(long rx){
		this.rx=rx;
	}
	/**
	 * setTransientTx: sets transient 
	 * transmitted number of bits
	 * from active flows only
	 * 
	 * @return t_tx number of transient
	 * transmitted bits
	 */
	public void setTransientTx(long t_tx){
		this.t_tx=t_tx;
	}
	/**
	 * setTransientRx: transient transmitted 
	 * number of bits
	 * from active flows only
	 * 
	 * @param number of transient
	 * received bits
	 */
	public void setTransientRx(long t_rx){
		this.t_rx = t_rx;
	}
	
	/**
	 * setAccNUploads: sets number of 
	 * accomplished uploads connections
	 * 
	 * @param accomplished_connections 
	 * number of finished connections
	 */
	public void setAccNUploads(int accomplished_flows){
		this.accomplished_upload = accomplished_flows;
	}
	/**
	 * setAccNDownloads: sets number of 
	 * accomplished downloads connections
	 * 
	 * @param accomplished_connections 
	 * number of finished connections
	 */
	public void setAccNDownloads(int accomplished_flows){
		this.accomplished_download = accomplished_flows;
	}
	/**
	 * getFailedFlows: sets the number of 
	 * failed upload connections
	 * 
	 * @param flows number of failed 
	 * upload connections
	 */
	public void setFailedUploads(int n){
		this.failed_uploads =n;
	}
	public void setRefusedUploads(int n){
		this.refused_uploads =n;
	}
	/**
	 * getFailedDownloads: sets the number of 
	 * failed download connections
	 * 
	 * @param n number of failed connections
	 */
	//public void setFailedDownloads(int n){
	//	this.failed_downloads =n;
	//}
	//public void setRefusedDownloads(int n){
	//	this.refused_downloads =n;
	//}
	//update connection list methods
	public boolean uploadAvailable(){
		long now = CommonState.getTime();
		if((((now)-this.renew_delay)>this.getUploads().getUpdateTS()))
			return true;
		else
			return false;
	}

	public boolean downloadAvailable(){
		long now = CommonState.getTime();
		if((((now)-this.renew_delay)>this.downloads.getUpdateTS()))
			return true;
		else
			return false;
	}
	public void setUploadTS(){
		long now = CommonState.getTime();
		this.getUploads().setUpdateTS((now));
	}
	public void setDownloadTS(){
		long now = CommonState.getTime();
		this.downloads.setUpdateTS((now));
	}
	
	public ConnectionList getChangableUpload(){
		BroadcastDomain dst_bd=null;
		Node counterpart = null;
		NetworkController counterpart_transport = null;
		if((this.changable_uploads.getSize()==0)&&(this.uploadAvailable())){
			Connection c = null;
			long now = CommonState.getTime();
	    	//spare bandwidth
	    	//long spare_bandwidth = this.getSpareUpload();
			for (int i = 0; i < this.uploads.getSize(); i++) {
				c = this.uploads.getElement(i);
				counterpart = c.getReceiver();
				counterpart_transport = ((NetworkController) (counterpart.getProtocol(c.getNPId())));
				dst_bd = counterpart_transport.getBroadcastDomain(Long.valueOf(c.getDstId()));
				if(c.getUpdateTimestamp()<(now) && (c.getBandwidth()>c.getMinBandwidth())&&(dst_bd.downloadAvailable()))
					this.changable_uploads.addConnection(c);
			}
	        //sort connection numerically by bandwidth 
	    	//(increasing order)
			//this.changable_uploads.sort();
	    	//if bandwidth lacks, reverse it
	    	/*if (spare_bandwidth<0){ 
	    		this.changable_uploads.reverse();
	    	}*/
	    	this.setUploadTS();
		}
		return this.changable_uploads;
	}

	public ConnectionList getChangableDownload(){
		BroadcastDomain src_bd=null;
		Node counterpart = null;
		NetworkController counterpart_transport = null;
		if((this.changable_downloads.getSize()==0)&&(this.downloadAvailable())){
			Connection c = null;
			long now = CommonState.getTime();
	    	//spare bandwidth
	    	//long spare_bandwidth = this.getSpareDownload();
			for (int i = 0; i < this.downloads.getSize(); i++) {
				c = this.downloads.getElement(i);
				counterpart = c.getSender();
				counterpart_transport = ((NetworkController) (counterpart.getProtocol(c.getNPId())));
				src_bd = counterpart_transport.getBroadcastDomain(Long.valueOf(c.getSrcId()));
				if((c.getUpdateTimestamp()<(now)) && (c.getBandwidth()>c.getMinBandwidth())&&(src_bd.uploadAvailable()))
					this.changable_downloads.addConnection(c);
			}
	        //sort connection numerically by bandwidth 
	    	//(increasing order)
			//this.changable_downloads.sort();
	    	//if bandwidth lacks, reverse it
	    	/*if (spare_bandwidth<0){ 
	    		this.changable_downloads.reverse();
	    	}*/
	    	this.setDownloadTS();
		}
		return this.changable_downloads;
	}


	public long getChangableDownBwd(){
		Connection c = null;
		long ubwd=0L;
		for (int i = 0; i < this.changable_downloads.getSize(); i++) {
			c = this.changable_downloads.getElement(i);
			ubwd+=(c.getBandwidth()-c.getMinBandwidth());
		}
		return ubwd;
	}
	
	public long getChangableUpBwd(){
		Connection c = null;
		long ubwd=0L;
		for (int i = 0; i < this.changable_uploads.getSize(); i++) {
			c = this.changable_uploads.getElement(i);
			ubwd+=(c.getBandwidth()-c.getMinBandwidth());
		}
		return ubwd;
	}
	
	public boolean hasBottleneck(){
		return this.has_bottleneck;
	}
	
	public void setHasBottleneck(boolean has_bottleneck){
		this.has_bottleneck = has_bottleneck;
	}
	
	//miscellaneous 
	public int getChUpSize(){
		return this.changable_uploads.getSize();
	}
	public int getChDownSize(){
		return this.changable_downloads.getSize();
	}
	
	public void moveFirstUptoEnd(){
		this.uploads.moveFirsttoEnd();
	}
	public void moveFirstDowntoEnd(){
		this.downloads.moveFirsttoEnd();
	}
	
	public long doForroDownloads() {
		long spare = this.getSpareDownload();
		if(spare==0)
			return spare;
		//lets fair sharing 
		ConnectionList
			dancers=new ConnectionList();
		long total=0L;
		Connection c = null;
		long t_mean = 0L;
		//consider that spare is always positive
		//old connections have gone
		t_mean = Math.round(((double)(this.getNetCapacity()))/((double)this.downloads.getSize()));
			
		long new_bandwidth=0;
		int num_connections=0;
		
		//selecting connections that might be modified
		for (int i = 0; i < this.downloads.getSize(); i++) {
			c = this.downloads.getElement(i);
			if((t_mean>c.getMinBandwidth())){
				total+=c.getBandwidth();
				num_connections++;
				dancers.addConnection(c);
			}
		}
		t_mean = Math.round(((double)total+spare)/((double)num_connections));
		boolean inter =false;
    	Node counterpart=null, mynode=null;
        NetworkController 
        counterpart_network= null, mynet=null;
    	BroadcastDomain host=null, guest=null;
    	long spare_upband_src=0, spare_downband_dst=0,
    	spare_upband=0L,target_spare=0L,contribution=0L;
		//note that spare has negative value
		for (int i = 0; i < dancers.getSize(); i++) {
			c = dancers.getElement(i);
			//is it inter connected??
			inter = (!(c.isIntraConnected()));
			if(i==(dancers.getSize()-1)){
				target_spare = spare;
			} else {
				target_spare = t_mean-c.getBandwidth();
			}
			//counterpart source 
			counterpart = c.getSender();
			counterpart_network = ((NetworkController) 
					(counterpart.getProtocol(c.getNPId())));
			mynode = c.getReceiver();
			mynet = ((NetworkController) 
					(mynode.getProtocol(c.getNPId())));
			
			if(inter){
				if(counterpart_network.getBroadcastDomain(BroadcastDomain.HOST_BD_ID).hasBottleneck()){
					host = counterpart_network.getBroadcastDomain(
							BroadcastDomain.HOST_BD_ID
					);
					guest = counterpart_network.getBroadcastDomain(
							Long.valueOf(c.getSrcId())
					);
					spare_upband_src = 
						Math.min(host.getSpareUpload(),
								guest.getSpareUpload()
								);
				} else {
					guest = counterpart_network.getBroadcastDomain(
							Long.valueOf(c.getSrcId())
					);
					spare_upband_src = guest.getSpareUpload();
					
				}
				if(mynet.getBroadcastDomain(BroadcastDomain.HOST_BD_ID).hasBottleneck()){
					host = mynet.getBroadcastDomain(BroadcastDomain.HOST_BD_ID);
					guest = this;
					spare_downband_dst = 
						Math.min(
								host.getSpareDownload(),
								guest.getSpareDownload()
								);
					
				} else {
					guest = this;
					spare_downband_dst = guest.getSpareDownload();
				}
				spare_upband = Math.min(spare_upband_src,spare_downband_dst);
			} else {
				host = counterpart_network.getBroadcastDomain(Long.valueOf(c.getSrcId()));
				spare_upband = host.getSpareUpload();
			}
			
			contribution=Math.min(target_spare, spare_upband);
			new_bandwidth=c.getBandwidth()+contribution;
			num_connections--;
			total-=c.getBandwidth();
			if(new_bandwidth>c.getBandwidth()){
				spare-=(new_bandwidth-c.getBandwidth());
				c.updateConnection(new_bandwidth);
			}
			t_mean = Math.round(((double)total+spare)/((double)num_connections));
		}
		return spare;
	}
	
	public long doForroUploads() {
		long spare = this.getSpareUpload();
		if(spare==0)
			return spare;
		//1 here bellow means a new connection
		//we are actually computing here the
		//the amount of bandwidth for a new 
		ConnectionList
			dancers=new ConnectionList();
		long total=0L;
		Connection c = null;
		long t_mean = 0L;
		//consider that spare is always positive
		t_mean = Math.round(((double)(this.getNetCapacity()))/((double)this.uploads.getSize()));
			
		long new_bandwidth=0;
		int num_connections=0;
		for (int i = 0; i < this.uploads.getSize(); i++) {
			c = this.uploads.getElement(i);
			if((t_mean>c.getMinBandwidth())){
				total+=c.getBandwidth();
				num_connections++;
				dancers.addConnection(c);
			}
		}
		t_mean = Math.round(((double)total+spare)/((double)num_connections));
		boolean inter =false;
    	Node counterpart=null, mynode=null;
        NetworkController 
        counterpart_network= null, mynet=null;
    	BroadcastDomain host=null, guest=null;
    	long spare_downband_dst=0, spare_upband_src=0,
    	spare_downband=0L,target_spare=0L, contribution=0L;
		for (int i = 0; i < dancers.getSize(); i++) {
			c = dancers.getElement(i);
			inter = (!(c.isIntraConnected()));
			if(i==(dancers.getSize()-1)){
				target_spare = spare;
			} else {
				target_spare = t_mean-c.getBandwidth();
			}
			
			//counterpart source 
			counterpart = c.getReceiver();
			counterpart_network = ((NetworkController) 
					(counterpart.getProtocol(c.getNPId())));
			mynode = c.getSender();
			mynet = ((NetworkController) 
					(mynode.getProtocol(c.getNPId())));
			
			if(inter){
				if(counterpart_network.getBroadcastDomain(BroadcastDomain.HOST_BD_ID).hasBottleneck()){
					host = counterpart_network.getBroadcastDomain(BroadcastDomain.HOST_BD_ID);
					guest = counterpart_network.getBroadcastDomain(Long.valueOf(c.getDstId()));
					spare_downband_dst = 
						Math.min(host.getSpareDownload(),
							guest.getSpareDownload());
					
				} else {
					guest = counterpart_network.getBroadcastDomain(Long.valueOf(c.getDstId()));
					spare_downband_dst = guest.getSpareDownload();
					
				}
				if(mynet.getBroadcastDomain(BroadcastDomain.HOST_BD_ID).hasBottleneck()){
					host = mynet.getBroadcastDomain(BroadcastDomain.HOST_BD_ID);
					guest = this;
					spare_upband_src = 
						Math.min(
								host.getSpareUpload(),
								guest.getSpareUpload());
					
				}else {
					guest = this;
					spare_upband_src = guest.getSpareUpload();
					
				}
				spare_downband = Math.min(spare_upband_src,spare_downband_dst);
			} else {
				host = counterpart_network.getBroadcastDomain(Long.valueOf(c.getDstId()));
				spare_downband = host.getSpareDownload();
			}

			contribution=Math.min(target_spare, spare_downband);
			new_bandwidth = c.getBandwidth()+contribution;
			
			total-=c.getBandwidth();
			num_connections--;
			if(new_bandwidth>c.getBandwidth()){
				spare-=(new_bandwidth-c.getBandwidth());
				c.updateConnection(new_bandwidth);
			}
			t_mean = Math.round(((double)total+spare)/((double)num_connections));
		}
		return spare;
	}

	public boolean active(){
		return (this.downloads.getSize()>0 || this.uploads.getSize()>0);
	}
	
	public long getRevervedDownloadBwd(){
		return this.downloads.getReservedBwd();
	}

	public long getRevervedUploadBwd(){
		return this.uploads.getReservedBwd();
	}
	
	public void show(){
		Connection c = null;
		System.err.println("uploads in bd "+this.getId()+" :");
		for (int i = 0; i < this.uploads.getSize(); i++) {
			c=this.uploads.getElement(i);
			c.show();
		}
		System.err.println("downloads in bd "+this.getId()+" :");
		for (int i = 0; i < this.downloads.getSize(); i++) {
			c=this.downloads.getElement(i);
			c.show();
		}
	}

}
