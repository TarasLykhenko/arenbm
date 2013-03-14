package stack;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;
import stack.App;
import stack.Net;
/**
 * 
 */
import stat.Generator;

/**
 * @author guthemberg
 *
 */
public class Initializer implements Control {

    private static final String PAR_PROT = "protocol";
    private static final String PAR_NET = "network";
    private static final String PAR_LOG = "logfile";
    private static final String PAR_SERVER_SET_SIZE = "server_set_size";
    private static final String PAR_PARALELL_DEGREE = "parallel_degree";
    private static final String PAR_BWD = "bwd";
    private static final String PAR_MIN = "min";
    private static final String PAR_MAX = "max";
    private static final String PAR_TS = "ts";
    private static final String PAR_EXP = "exp";
    double exp;
    int protocol;
    int server_set_size;
    int network;
    long parallel_degree;
    long bwd;
    long ts;
    String logfile;
    long min;
    long max;
	/**
	 * 
	 */
	public Initializer(String prefix) {
        this.protocol = Configuration.getPid(prefix + "." + PAR_PROT);
        this.network = Configuration.getPid(prefix + "." + PAR_NET);
        this.logfile = Configuration.getString(prefix + "." + PAR_LOG);
        this.exp = Configuration.getDouble(prefix + "." + PAR_EXP);
        this.min= Configuration.getLong(prefix + "." + PAR_MIN);
        this.max=Configuration.getLong(prefix + "." + PAR_MAX);
        this.bwd= Configuration.getLong(prefix + "." + PAR_BWD);
        this.server_set_size= Configuration.getInt(prefix + "." + PAR_SERVER_SET_SIZE);
        this.parallel_degree= Configuration.getInt(prefix + "." + PAR_PARALELL_DEGREE);
        this.ts= Configuration.getLong(prefix + "." + PAR_TS);
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
            App app= (App) node.getProtocol(this.protocol);
            app.bootstrap(servers, this.network, this.bwd, this.ts);
            Net net= (Net) node.getProtocol(this.network);
            net.bootstrap(this.protocol, this.bwd, this.ts);
		}
		for (int i = servers.length; i < Network.size(); i++) {
            Node dst = Network.get(i);
            App app= (App) dst.getProtocol(this.protocol);	
            for (int j = 0; j < this.parallel_degree; j++) {
            	app.fetch(dst);
			}
		}
		// TODO Auto-generated method stub
		return false;
	}

}
