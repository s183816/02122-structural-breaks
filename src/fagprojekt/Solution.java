package fagprojekt;

import java.util.LinkedList;
import java.util.Map;

public class Solution {
	private LinkedList<Map<Integer, String>> solution;
	private double fit;
	private int T;
	private boolean fitComputable;
	
	public Solution(LinkedList<Map<Integer, String>> solution, int T) {
		this.solution = solution;
		calculateFit();
	}
	
	// initially empty, fitnesss function should not be computed
	public Solution(int T) {
		this.fitComputable = false;
		this.T = T;
		solution = new LinkedList<Map<Integer, String>>();
	}
	
	public void set(int b, String B) {
		
	}
	
	// TO-DO: implementation of fitness function
	public void calculateFit() {
		this.fitComputable = true;
		this.fit = 0.0;
	}
	
	public double getFit() {
		if (!fitComputable) {
			// �ndr exceptiotypen
			throw new IllegalArgumentException();
		}
		return fit;
	}
	
	
	
}
