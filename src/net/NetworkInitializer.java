/*
 * NAPA-WINE project
 * www.napa-wine.eu
 */
package net;

import peersim.config.*;
import peersim.core.*;

/**
 * Initialize the Bandwidth Aware protocol.<p>
 * This protocol provides a network layer where peers have
 * different resources in term of both up-/down-load bandwidth.<p>
 * You have to provide the CDF of the bandwidth, using the CDF distribution setter.
 * It uses the methods defined in {@link NetworkSkeleton}.
 *
 * @author Alessandro Russo
 * @version $Revision: 0.02$
 */
public class NetworkInitializer implements Control {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    private static final String PAR_UPLAYER = "uplayer";//protocol id of uplayer client layer
    private static final String PAR_PROT = "protocol";//associated protocol
    private static final String PAR_TS = "ts";//1 second represents in event time unit
    private static final String PAR_HARD_LIMIT_FLAG = "hard_limit_flag";//1 second represents in event time unit
    private static final String PAR_SEED = "seed";
    // ------------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------------
    /**Protocol Identifier */
    private final int uplayer_pid;
    /**Protocol Identifier */
    private final int pid;
    /**Active upload*/
    /**second as a quantity of event time units*/
    private long ts;
    private boolean hard_limit_flag;
    private long seed;

    // 
    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------
    /**
     * Creates a new instance and reads parameters from the config file.
     */
    public NetworkInitializer(String prefix) {
        System.err.print("Init Bandwidth: ");
        //System.err.print(prefix + "." + PAR_UPLAYER);
        uplayer_pid = Configuration.getPid(prefix + "." + PAR_UPLAYER);
        pid = Configuration.getPid(prefix + "." + PAR_PROT);
        this.ts = Configuration.getLong(prefix + "." + PAR_TS);
        this.hard_limit_flag = Configuration.getBoolean(prefix + "." + PAR_HARD_LIMIT_FLAG,false);
        this.seed = Configuration.getLong(prefix + "." + PAR_SEED,NetworkController.DEFAULT_SEED);
        System.err.print("Ts: "+this.ts);
        System.err.println("Pid "+pid);
    }

    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------
    /**
     * Initialize peers' fields and the source's resources.
     * @return Always return false.
     */
    public boolean execute() {
        for (int i = 0; i < Network.size(); i++) {
            //retrieve node instance
            Node aNode = Network.get(i);
            //retrieve protocol skeleton
            NetworkSkeleton bwa = (NetworkSkeleton) aNode.getProtocol(pid);
            bwa.setProtocolID(pid);
            bwa.setUplayerProtocolID(this.uplayer_pid);
            //initilize the data structures in the node
            bwa.bootstrap(aNode.getID(), aNode, 
            		NetworkController.DEFAULT_NETWORK_CAPACITY, 
            		this.seed,false,0L,2000L,3,
            		false,0.8f);
            bwa.setTS(this.ts);
            bwa.setHardLimitFlag(this.hard_limit_flag);
            /*if (i == Network.size() - 1 && this.srcup != -1) {//set source bandwidth                
                bwa.initUpload(srcup);                
            } */           
        }
        NetKernel.bootstrap();
        return false;
    }
}
