/**
 * 
 */
package stat;

import peersim.config.Configuration;
import cern.jet.random.engine.RandomEngine;

/**
 * @author vsimon
 *
 */
public class ParetoDistrib{

	// ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
	private static final String PAR_MIN = "min_val";
	private static final String PAR_MAX = "max_val";
	private static final String PAR_EXPONENT = "exponent";
	
	// ------------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------------
	
	private final double exponent;
	private final long min;
	private final long max;
	public ParetoDistrib(long min, long max, double exponent)
	{
		this.exponent = exponent;
		this.min = min;
		this.max = max;
	}

	public long nextLong(RandomEngine rgen) {
		double gen = Math.floor(min/Math.pow(rgen.nextDouble(), 1/exponent));
		return gen> max ?max:(long)gen;
	}
	public double nextDouble(RandomEngine rgen) {
		double gen = ((float)min)/Math.pow(rgen.nextDouble(), 1/exponent);
		return gen> max ?max:gen;
	}

}
