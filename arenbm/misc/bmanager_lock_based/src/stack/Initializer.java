package stack;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;
import stat.Generator;
import stack.App;
import stack.Net;
/**
 * 
 */

/**
 * @author guthemberg
 *
 */
public class Initializer implements Control {

    private static final String PAR_PROT = "application";
    private static final String PAR_NET = "network";
    private static final String PAR_LOG = "logfile";
    private static final String PAR_SERVER_SET_SIZE = "server_ser_size";
    private static final String PAR_BWD = "bwd";
    private static final String PAR_TS = "ts";
    private static final String PAR_MIN = "min";
    private static final String PAR_MAX = "max";
    private static final String PAR_PARALLEL = "parallel_degree";
    private static final String PAR_MONITORING_CALL_DELAY = "monitoring_call_delay";
    private static final String PAR_EXP = "exp";

    long monitoring_call_delay;
    double exp;
    int parallel_degree;
    int application;
    int server_set_size;
    int network;
    long bwd;
    long ts;
    long min;
    long max;
    String logfile;
	/**
	 * 
	 */
	public Initializer(String prefix) {
        this.exp = Configuration.getDouble(prefix + "." + PAR_EXP);
        this.application = Configuration.getPid(prefix + "." + PAR_PROT);
        this.network = Configuration.getPid(prefix + "." + PAR_NET);
        this.logfile = Configuration.getString(prefix + "." + PAR_LOG);
        this.server_set_size= Configuration.getInt(prefix + "." + PAR_SERVER_SET_SIZE);
        this.bwd= Configuration.getLong(prefix + "." + PAR_BWD);
        this.ts= Configuration.getLong(prefix + "." + PAR_TS);
        this.min = Configuration.getLong(prefix + "." + PAR_MIN);
        System.out.println("min: "+this.min);
        this.max = Configuration.getLong(prefix + "." + PAR_MAX);
        System.out.println("max: "+this.max);
        this.parallel_degree = Configuration.getInt(prefix + "." + PAR_PARALLEL);
        this.monitoring_call_delay= Configuration.getLong(prefix + "." + PAR_MONITORING_CALL_DELAY);
        //deletefile(logfile);
        this.log("ts,avg_bwd,msgs,sys_time,mem_usage,mean_size",false);
		// TODO Auto-generated constructor stub
	}
	
//	private static void deletefile(String file){
//		File f1 = new File(file);
//		f1.delete();
//	}
	public void log(String str,boolean append){
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
		Generator.bootstrapt(this.min, this.max, this.exp);
		Node[] servers = new Node[this.server_set_size];
		for (int i = 0; i < this.server_set_size; i++) {
            servers[i] = Network.get(i);			
		}
		for (int i = 0; i < Network.size(); i++) {
            Node node = Network.get(i);
            App app= (App) node.getProtocol(this.application);	
            app.bootstrap(this.bwd, this.ts, 
            		this.network,servers);
            Net net= (Net) node.getProtocol(this.network);
            net.bootstrap(this.bwd, this.ts, this.application);
		}
		for (int i = servers.length; i < Network.size(); i++) {
            Node node = Network.get(i);
            App app= (App) node.getProtocol(this.application);	
            //long shift = Generator.getRandomIndex((int)this.monitoring_call_delay);
        	//long shift = app.getRandomIndex((int)(10*this.ts));
        	//long shift = 0L;
        	for (int j = 0; j < this.parallel_degree; j++) {
                app.fetchData(node);
			}
		}
		// TODO Auto-generated method stub
		return false;
	}

}
