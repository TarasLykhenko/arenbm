/**
 * 
 */
package net;

/**
 * @author guthemberg
 *
 */
public class ConnectionInfo {
	Connection c;
	long c_time;

	/**
	 * 
	 */
	public ConnectionInfo() {
		// TODO Auto-generated constructor stub
	}
		
	public ConnectionInfo(long c_time, Connection c){
		this.c = c;
		this.c_time = c_time;
	}
	
	public long getCurrentTime(){
		return this.c_time;
	}
	
	public Connection getConnection(){
		return this.c;
	}
	public void setConnection(Connection c){
		this.c = c;
	}

}
