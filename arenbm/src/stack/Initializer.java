package stack;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import net.NetworkController;

import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDSimulator;
import stack.App;
import stack.Transport;
/**
 * 
 */
import stat.Generator;

/**
 * @author guthemberg
 *
 */
public class Initializer implements Control {

    private static final String PAR_PROT = "application";
    private static final String PAR_NET = "network";
    private static final String PAR_LOG = "logfile";
    private static final String PAR_SERVER_SET_SIZE = "server_set_size";
    private static final String PAR_BWD = "bwd";
    private static final String PAR_TS = "ts";
    private static final String PAR_TRANSPORT = "transport";
    private static final String PAR_SEED = "seed";
    private static final String PAR_UPDATE_TIME = "update_time";
    private static final String PAR_PARALLEL = "parallel_degree";
    private static final String PAR_MONITORING_CALL_DELAY = "monitoring_call_delay";
    private static final String PAR_MIN = "min";
    private static final String PAR_MAX = "max";
    private static final String PAR_EXP = "exp";
    int parallel_degree;
    double exp;
    int application;
    int server_set_size;
    int network;
    int transport;
    long bwd;
    long ts;
    long seed;
    long update_time;
    long min;
    long max;
    long monitoring_call_delay;
    String logfile;
	/**
	 * 
	 */
	public Initializer(String prefix) {
        this.application = Configuration.getPid(prefix + "." + PAR_PROT);
        this.network = Configuration.getPid(prefix + "." + PAR_NET);
        this.transport = Configuration.getPid(prefix + "." + PAR_TRANSPORT);
        this.logfile = Configuration.getString(prefix + "." + PAR_LOG);
        this.exp = Configuration.getDouble(prefix + "." + PAR_EXP);
        this.min = Configuration.getLong(prefix + "." + PAR_MIN);
        this.max = Configuration.getLong(prefix + "." + PAR_MAX);
        this.server_set_size= Configuration.getInt(prefix + "." + PAR_SERVER_SET_SIZE);
        this.bwd= Configuration.getLong(prefix + "." + PAR_BWD);
        this.ts= Configuration.getLong(prefix + "." + PAR_TS);
        this.seed= Configuration.getLong(prefix + "." + PAR_SEED);
        this.update_time= Configuration.getLong(prefix + "." + PAR_UPDATE_TIME);
        this.parallel_degree = Configuration.getInt(prefix + "." + PAR_PARALLEL);
        this.monitoring_call_delay= Configuration.getLong(prefix + "." + PAR_MONITORING_CALL_DELAY);
        this.log("ts,avg_bwd,msgs,sys_time,mem_usage,mean_size",false);
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
            app.bootstrap(this.bwd, this.ts, this.transport,servers);
            Transport transp= (Transport) node.getProtocol(this.transport);
            transp.bootstrap(this.bwd, this.ts, this.application, this.transport,this.network,this.update_time, node);
            NetworkController network = (NetworkController) node.getProtocol(this.network);
            network.bootstrap(node.getID(), node, this.bwd,
            		this.seed,false,0L,
            		2L,3,
            		false, 0.8f);
        	network.addLocalAddress(node.getID(), network.getBroadcastDomain(0));
//            network.bootstrap(node.getID(), node, this.bwd,
//            		this.seed,false,0L,
//            		2,3,
//            		false, .8);
		}
		for (int i = servers.length; i < Network.size(); i++) {
            Node node = Network.get(i);
            App app= (App) node.getProtocol(this.application);
        	for (int j = 0; j < this.parallel_degree; j++) {
                app.fetchData(node);
        	}
		}
		//generate update message to node 0
		EDSimulator.add(this.update_time, Message.createUpdateMessage(), Network.get(0), this.transport);
		// TODO Auto-generated method stub
		return false;
	}

}
