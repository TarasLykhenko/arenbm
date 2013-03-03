package stat;

public class Generator {
	private static cern.jet.random.engine.RandomEngine generator;
	private static cern.jet.random.Uniform uniform;
	static ParetoDistrib pgen;
	static long total;
	static long hits;
	public static void bootstrapt(long min,long max,double exponent){
		Generator.total=0L;
		Generator.hits=0L;
		Generator.generator = new cern.jet.random.engine.MersenneTwister(1024);
		Generator.uniform= new cern.jet.random.Uniform(Generator.generator);
		Generator.pgen=new ParetoDistrib(min, max, exponent);
	}

	public static int getRandomIndex(int n){
		int dst_idx = 0;
		if(n>1){
			//VS use of colt random number generator return int in [from,to] inclusive so use of n-1
			dst_idx = Generator.uniform.nextIntFromTo(0, n-1);/*Math.abs(this.rgen.nextInt())%n;*/
		}
		return dst_idx;
	}

	public static long nextLong() {
		long next_long = Generator.pgen.nextLong(Generator.generator);
		Generator.total+=next_long;
		Generator.hits++;
		return next_long;
	}

	public static long meanLong() {
		return Generator.total/Generator.hits;
	}

}
