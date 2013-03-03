package net;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;

/**
 * This class implements a collection of connection elements.<p>
 * Such a collection reflects all connections in the current node with its neighbors.
 *
 * @author Alessandro Russo
 * @version $Revision: 0.02$
 *
 */
public class ConnectionList {

	
    /**
     * This array cointains all the connection element in this protocol instance.<p>
     * Such an array is increased and reduced in runtime to collect all the connections of the node.
     */
    private ArrayList<Connection> connection_list;
    
    private Hashtable<Long, Connection> connection_table;
    /**
     * update_time_stamp: in order to control updates.
     */
    long update_time_stamp;
    
    long reserved_bwd;
    
    /**
     * Constructor method to initialize the list of connection elements.
     */
    public ConnectionList() {
        this.connection_list = new ArrayList<Connection>();
        this.connection_table = new Hashtable<Long, Connection>();
        this.update_time_stamp=-1;
        this.reserved_bwd=0L;
    }
    
    /**
     * clear: cleans the current list, and
     * resets the update timestamp.
     */
    public void clean(){
    	this.connection_list.clear();
    	this.connection_table.clear();
        this.update_time_stamp=-1;
        this.reserved_bwd=0L;
    }
    

    /**
     * Get the number of elements in the list of connections.
     * @return The number of elements in the list.
     */
    public int getSize() {
        return this.connection_list.size();
    }

    /**
     * Gets the bandwidth that is reserved.
     * @return bandwidth in bits per second.
     */
    public long getReservedBwd() {
        return this.reserved_bwd;
    }
    
    public void moveFirsttoEnd(){
    	Connection c = this.connection_list.get(0);
    	this.connection_list.remove(0);
    	this.connection_list.add(c);
    }

    /**
     * Checks whether the list is empty or not.
     * @return True if the list is empty, false otherwise.
     */
    public boolean isEmpty() {
        return (this.getSize() == 0);
    }

    /**
     * Checks whether the there is a connection here.
     * 
     * @return True if the connection has already been
     * added.
     */
    public boolean contains(Connection c) {
    	return this.connection_list.contains(c);
    }

    /**
     * sums all bandwidths.
     * 
     * @return sum of bandwidths.
     */
    public long sumBandwidth() {
    	long sum=0;
    	Connection c = null;
    	for (int i = 0; i < connection_list.size(); i++) {
			c=connection_list.get(i);
			sum+=c.getBandwidth();
		}
    	return sum;
    }

    /**
     * Checks whether the there is a connection here.
     * 
     * @return True if the connection has already been
     * added.
     */
    public boolean contains(long id) {
    	return connection_table.containsKey(Long.valueOf(id));
    	/*Connection c=null;
    	for (int i = 0; i < connection_list.size(); i++) {
			c=connection_list.get(i);
			if(c.getId()==id)
				return true;
		}
    	return false;*/
    }

    /**
     * This methos is used to add a connection element to the current list of connections.<p>
     * Connection elemenets are sorted first for start time and then for end time.
     *
     * @param ce Connection element to be added with all paramenters.
     */
    public void addConnection(Connection ce) {
    	if(ce.getDeadline()>0)
    		this.reserved_bwd+=ce.getMinBandwidth();
    	this.connection_table.put(Long.valueOf(ce.getId()), ce);
    	this.connection_list.add(ce);
    }


    /**
     * Remove a connection element from the list of elements.
     * @param ce ConnectionElement to be removed. The criteria used to identify the ConnectionElement to remove is the
     *      start time, the end time and the node.
     * @return connection element just removed, null if no elements with this criteria was found.
     */
    /*public Connection remConnection(long conn_id) {
    	Connection c=null;
    	for (int i = 0; i < this.connection_list.size(); i++) {
			if(conn_id==this.connection_list.get(i).getId()){
				c=this.connection_list.remove(i);
				return c;
			}
				
		}
    	return null;
    }*/

    /**
     * Remove a connection element from the list of elements.
     * @param ce ConnectionElement to be removed. The criteria used to identify the ConnectionElement to remove is the
     *      start time, the end time and the node.
     * @return true if it succeeds.
     */
    public boolean remConnection(Connection ce) {
    	Connection c_removed = this.connection_table.remove(Long.valueOf(ce.getId()));
    	if(c_removed!=null){
        	if(ce.getDeadline()>0)
        		this.reserved_bwd-=ce.getMinBandwidth();
        	return this.connection_list.remove(ce);    		
    	}
    	return false;
    }
    /**
     * Remove a connection element from the list of elements.
     * @param ce ConnectionElement to be removed. The criteria used to identify the ConnectionElement to remove is the
     *      start time, the end time and the node.
     * @return true if it succeeds.
     */
    /*public Connection remConnection(int i) {
    	return this.connection_list.remove(i);
    }*/


    /**
     * Returns the i-th element in the list of connections.
     * @param i Position in the list of the element.
     * @return BandwidthConnectionElement in the i-th posisition, null otherwise.
     */
    public Connection getElement(int i) {
    	try {
    		return this.connection_list.get(i);			
		} catch (IndexOutOfBoundsException e) {
			return null;
		}        
    }
    
    public long getUpdateTS(){
    	return this.update_time_stamp;
    }

    public void setUpdateTS(long update_time_stamp){
    	this.update_time_stamp=update_time_stamp;
    }

    /**
     * Gives a printable version of the connection list.
     * @return String containing all the elements in the connection list.
     */
    public String getAll() {
        String result = "";
        Connection ce = null;
        for (int i = 0; i < this.getSize(); i++) {
            ce = this.connection_list.get(i);
            if (i == 0) {
                result += ce.getLabels() + "\n";
            }
            result += ce.getValues() + "\n";
        }
        return result;
    }

    /**
     * This method is used to sample and get the bandwidth usage in the given time.<p>
     * It looks for all connection with (startTime <= time <= endTime), and return the bandwidth usage within these times.
     * @param time Time to sample.
     * @return Bandwidth usage.
     */
    public long getBandwidthUsage(long time) {
        long band_use = 0;
        for (int i = 0; i < this.getSize(); i++) {
            Connection bce = this.getElement(i);
            if (bce.getStart() <= time && bce.getEnd() <= time) {
                band_use += bce.getBandwidth();
            }
        }
        return band_use;
    }

    class BandwithComapator implements
	Comparator<Connection> {

		public int compare(Connection conn1,
				Connection conn2) {
			// TODO Auto-generated method stub
			Long banwidth1 = Long.valueOf(conn1.getBandwidth());
			Long banwidth2 = Long.valueOf(conn2.getBandwidth());
			return banwidth1.compareTo(banwidth2);
		}
	
	}

    class MinBandwithComapator implements
	Comparator<Connection> {

		public int compare(Connection conn1,
				Connection conn2) {
			// TODO Auto-generated method stub
			Long banwidth1 = Long.valueOf(conn1.getMinBandwidth());
			Long banwidth2 = Long.valueOf(conn2.getMinBandwidth());
			return banwidth1.compareTo(banwidth2);
		}
	
	}
    
    public void go(){
		System.out.println("bands before:");
    	for (int i = 0; i < this.connection_list.size(); i++) {
			System.out.println(this.connection_list.get(i).getBandwidth());
		}
    	BandwithComapator mycompatator = new BandwithComapator();
    	Collections.sort(this.connection_list, mycompatator);
    	Collections.reverse(this.connection_list);
		System.out.println("bands after:");
    	for (int i = 0; i < this.connection_list.size(); i++) {
			System.out.println(this.connection_list.get(i).getBandwidth());
		}
    }

    public void sort(){
    	BandwithComapator mycompatator = new BandwithComapator();
    	Collections.sort(this.connection_list, mycompatator);
    }

    public void sortMinBwd(){
    	MinBandwithComapator mycompatator = new MinBandwithComapator();
    	Collections.sort(this.connection_list, mycompatator);
    }
    
    public void reverse(){
    	Collections.reverse(this.connection_list);    	
    }
    
    public Connection getConnection(long c_id){
    	return this.connection_table.get(Long.valueOf(c_id));
    }
    
}
