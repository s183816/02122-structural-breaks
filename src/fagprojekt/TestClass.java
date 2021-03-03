package fagprojekt;
// example usage

import java.util.Arrays;

public class TestClass {
	public static void main(String[] args) {
		TimeSeries ts = new TimeSeries("./1Breaks_1K.json");
		Algorithm alg = new Algorithm(ts,1);
		Solution bestSolution = alg.findBreakPoints(null);
		System.out.println(Arrays.toString(bestSolution.returnBreakpoints()));
	}
}