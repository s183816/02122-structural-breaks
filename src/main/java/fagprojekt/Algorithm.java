package fagprojekt;
import java.util.Random;
// Johan
public class Algorithm {
	private TimeSeries timeSeries;
	private Random rand;
	double alpha = 0.25;
	private double pm;
	// sat som i artiklen
	private double pb = 0.6;
	private double puc = 0.3;
	private double popc = 0.3;
	private double pmu = 0.4;
	private int T;
	private int M = 5;
	private final int max_iter = 500;
	private int k0;
	
	public Algorithm(TimeSeries timeSeries, int k0) {
		this.timeSeries = timeSeries;
		this.T = timeSeries.getT();
		this.k0=k0;
		this.rand = new Random();
	}
	
	public Solution findBreakPoints(Populations population) {
		Populations X = new Populations(M,k0,T,timeSeries,alpha);
		if (population != null) {
			X = population;
		}
		int iter = 0;
		// implementer stopkriterie
		while (iter < max_iter) {
			Solution Xi = select(X);
			Solution Xj = select(X);
			Solution C = new Solution(T);
			double random = rand.nextDouble();
			if (random < pmu) {
				C = mutate(Xi);
			}
			else if (random >= pmu && random <= 1.0-popc) {
				C = uniformCrossover(Xi,Xj);
			}
			else {
				C = onePointCrossover(Xi,Xj);
			}
			C.calculateFit(alpha, timeSeries);
			int minIndex = X.leastFitIndex();
			random = rand.nextDouble();
			if (random < C.getFit()/(C.getFit() + UserDefinedFunctions.fit(X.getIndex(minIndex),alpha,timeSeries))) {
				X.removeIndex(minIndex);
				X.add(C);
			}
			iter++;
		}
		return X.fittestString();
	}
	
	public Solution uniformCrossover(Solution X, Solution Y) {
		Solution C = new Solution(T);
		for (int i = 0; i < T; i++) {
			if (rand.nextDouble()<0.5) {
				// genbrugt kode; ikke effektivt; den s�ger igennem det samme flere gange
				// finder ud af om X(i) er breakpoint og hvis den er s� s�tter den det relevante v�rdi af B
				String B = X.getBreakpoint(i);
				if (B!="*") {
					C.setBreakpoint(i, B);
				}
			}
			else {
				String B = Y.getBreakpoint(i);
				if (B!="*") {
					C.setBreakpoint(i, B);
				}
			}
		}
		return C;
	}
	
	public Solution mutate(Solution X) {
		Solution C = new Solution(T);
		pm = (double) 2*X.numberOfBreakpoints()/T;
		for (int i = 0; i < T; i++) {	
			double r = rand.nextDouble();
			if (r<pm) {
				r = rand.nextDouble();
				if (r<pb) {
					C.setBreakpoint(i, UserDefinedFunctions.newB());
				}
			}
			else {
				String B = X.getBreakpoint(i);
				if (B!="*") {
					C.setBreakpoint(i, B);
				}
			}
		}
		return C;
	}
	
	public Solution select(Populations pop) {
		double S1 = pop.sumOfSquaredFitnesses();
		double r = rand.nextDouble()*(S1-1)+1;
		double val = UserDefinedFunctions.fit(pop.getIndex(0),alpha,timeSeries);
		double h = val*val ;
		int i = 0;
		while (h <= r) {
			i+=1;
			val = UserDefinedFunctions.fit(pop.getIndex(i),alpha,timeSeries);
			h+= val*val;
		}
		return pop.getIndex(i);
	}
		
	public Solution onePointCrossover(Solution X,Solution Y) {
		int k = rand.nextInt(T);
		Solution C = new Solution(T);
		for (int i = 0; i < T; i++) {
			if (i<k) {
				String B = X.getBreakpoint(i);
				if (B!="*") {
					C.setBreakpoint(i, B);
				}
			}
			else {
				String B = Y.getBreakpoint(i);
				if (B!="*") {
					C.setBreakpoint(i, B);
				}
			}
		}
		return C;
	}
}
