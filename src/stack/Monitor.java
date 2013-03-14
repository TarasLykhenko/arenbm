package stack;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import stat.Generator;

/**
 * 
 */

/**
 * @author guthemberg
 *
 */
public class Monitor implements Control {
    private static final String PAR_APPLICATION = "protocol";
    private static final String PAR_TRANSPORT = "transport";
    private static final String PAR_LOG = "logfile";
    private static final String PAR_TS= "ts";
    private static final String PAR_SERVER_SET_SIZE = "server_set_size";
    private static final String PAR_MONITORING_CALL_DELAY = "monitoring_call_delay";
    long monitoring_call_delay;
    int application;
    int transport;
    String logfile;
    long ts;
    int server_set_size;
    long last_sys_time=0L;
    long last_tx=0L;

	/**
	 * 
	 */
	public Monitor(String prefix) {
        this.application = Configuration.getPid(prefix + "." + PAR_APPLICATION);
        this.transport = Configuration.getPid(prefix + "." + PAR_TRANSPORT);
        this.logfile = Configuration.getString(prefix + "." + PAR_LOG);
        this.ts= Configuration.getLong(prefix + "." + PAR_TS);
        this.server_set_size= Configuration.getInt(prefix + "." + PAR_SERVER_SET_SIZE);
        this.monitoring_call_delay= Configuration.getLong(prefix + "." + PAR_MONITORING_CALL_DELAY);
        this.last_sys_time=0L;
        this.last_tx=0L;
		// TODO Auto-generated constructor stub
	}

	public void log(String str, boolean append){
		try {
			FileWriter outFile = new FileWriter(this.logfile,append);
			PrintWriter out = new PrintWriter(outFile);
			out.println(str);
			out.close();
		} catch (IOException e){
			e.printStackTrace();
		}
	}
	/* (non-Javadoc)
	 * @see peersim.core.Control#execute()
	 */
	@Override
	public boolean execute() {
		int cmsg=0;
		int active_transfers=0;
		long downloaded=0;
		long tx=0;
		for (int i = 0; i < Network.size(); i++) {
			active_transfers+=((Transport)Network.get(i).getProtocol(this.transport)).getActiveFlows();
			cmsg+=((App)Network.get(i).getProtocol(this.application)).getComputedMessages();
			((App)Network.get(i).getProtocol(this.application)).resetComputedMessages();
			if(i<this.server_set_size){
				downloaded+=((Transport)Network.get(i).getProtocol(this.transport)).getTx();
				tx+=((App)Network.get(i).getProtocol(this.application)).getTx();
			}
		}
		double avg_bwd=0.0;//in Mbps
		avg_bwd =(((((double)downloaded/((double)(this.monitoring_call_delay)/(double)this.ts))))/(1000000.0))/((double)this.server_set_size);
		
		String avg_bwd_str = String.format(Locale.US,"%.2f", avg_bwd);
		long sys_now =System.currentTimeMillis();
		if(this.last_sys_time==0)
			this.last_sys_time=sys_now;
		String mem_usage_str = String.format(Locale.US,"%.2f", 
				((((double)Runtime.getRuntime().totalMemory())-((double)Runtime.getRuntime().freeMemory()))));
		this.log(CommonState.getTime()+","+avg_bwd_str+","+cmsg+","+(sys_now-this.last_sys_time)+","+mem_usage_str+","+Generator.meanLong(), true);
		last_sys_time=sys_now;
		return false;
	}

	
}
