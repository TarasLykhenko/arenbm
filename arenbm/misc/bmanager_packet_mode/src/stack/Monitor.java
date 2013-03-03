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
    private static final String PAR_PROT = "protocol";
    private static final String PAR_NET = "network";
    private static final String PAR_LOG = "logfile";
    private static final String PAR_SERVER_SET_SIZE = "server_set_size";
    private static final String PAR_TS = "ts";
    private static final String PAR_MONITORING_CALL_DELAY = "monitoring_call_delay";
    int server_set_size;
    int protocol;
    int network;
    long monitoring_call_delay;
    long ts;
    long last_sys_time=0L;
    String logfile;

	/**
	 * 
	 */
	public Monitor(String prefix) {
        this.protocol = Configuration.getPid(prefix + "." + PAR_PROT);
        this.network = Configuration.getPid(prefix + "." + PAR_NET);
        this.logfile = Configuration.getString(prefix + "." + PAR_LOG);
        this.server_set_size= Configuration.getInt(prefix + "." + PAR_SERVER_SET_SIZE);
        this.ts= Configuration.getLong(prefix + "." + PAR_TS);
        this.monitoring_call_delay= Configuration.getLong(prefix + "." + PAR_MONITORING_CALL_DELAY);
        this.last_sys_time=0L;
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
		int qsize=0;
//		long durations=0;
		long downloaded=0;
		for (int i = 0; i < Network.size(); i++) {
			if(((Net)Network.get(i).getProtocol(this.network)).getQueueSize()>0)
				((Net)Network.get(i).getProtocol(this.network)).retry(this.network);
			qsize+=((Net)Network.get(i).getProtocol(this.network)).getQueueSize();
			cmsg+=((App)Network.get(i).getProtocol(this.protocol)).getComputedMessages();
			((App)Network.get(i).getProtocol(this.protocol)).resetComputedMessages();
//			durations+=((App)Network.get(i).getProtocol(this.protocol)).getDuration();
//			((App)Network.get(i).getProtocol(this.protocol)).resetDuration();
			downloaded+=((Net)Network.get(i).getProtocol(this.network)).getTx();
			((Net)Network.get(i).getProtocol(this.network)).resetTx();
		}
		double avg_bwd=0.0;//in Mbps
		//if(duration>0.0){
		avg_bwd =(((((double)downloaded/((double)(this.monitoring_call_delay)/(double)this.ts))))/(1000000.0))/((double)this.server_set_size);
		//}
		String avg_bwd_str = String.format(Locale.US,"%.2f", avg_bwd);
		long sys_now =System.currentTimeMillis();
		if(this.last_sys_time==0)
			this.last_sys_time=sys_now;
		String mem_usage_str = String.format(Locale.US,"%.2f", 
				((((double)Runtime.getRuntime().totalMemory())-((double)Runtime.getRuntime().freeMemory()))));
		this.log(CommonState.getTime()+","+avg_bwd_str+","+cmsg+","+(sys_now-this.last_sys_time)+","+mem_usage_str+","+Generator.meanLong(), true);
		last_sys_time=sys_now;
		// TODO Auto-generated method stub

//		
//		this.log(CommonState.getTime()+","+rx+","+tx, true);
//		long now = CommonState.getTime();
//		double mean=0.0;
//		if(durations>0)
//			mean =((((double)downloaded/((double)durations/(double)this.ts)))/(double)this.server_set_size)/((double)this.monitoring_call_delay/(double)this.ts);
//		String mean_str = String.format("%.2f", mean);
//		System.out.println("["+now+"]queue size: "+qsize+"; msg size: "+
//				msize+"; cmsgs: "+cmsg+"; downloaded: "+
//				downloaded+"; durations: "+durations+
//				"; mean: "+mean_str);
//		// TODO Auto-generated method stub
		return false;
	}

	
}
