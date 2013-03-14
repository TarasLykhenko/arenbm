/**
 * 
 */
package net;


/**
 * @author guthemberg
 *
 */
public class DelayMatrix {

	protected final int order;
	protected final long[][] eedelays;
	
	/**
	 * 
	 */
	public DelayMatrix(int order) {
		
        this.order = order;
        this.eedelays = new long[this.order][this.order];
        for (int i = 0; i < this.order; i++)
            for (int j = 0; j < this.order; j++)
                    this.eedelays[i][j] = 0L;    
    }
	
	/**
	 * 
	 */
	public DelayMatrix(int order, long eedelay) {
		
        this.order = order;
        this.eedelays = new long[this.order][this.order];
        for (int i = 0; i < this.order; i++)
            for (int j = 0; j < this.order; j++)
                    this.eedelays[i][j] = eedelay;    
    }
	
	public long getDelay(int src_index, int dst_index){
		return this.eedelays[src_index][dst_index];
	}

	public long setDelay(int src_index, int dst_index, long eedelay){
		return this.eedelays[src_index][dst_index] = eedelay;
	}

}
