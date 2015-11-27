package nez.infer;

import java.util.ArrayList;
import java.util.List;

public class Histogram {
	// a variable 'data' is a list of number of chunk that has the specific
	// tokens.
	// This list's index corresponds to the number of occurrences of the token
	// in a chunk.
	List<Integer> data;
	String label; // a label of the target token
	int tokenCount;

	public Histogram(String label) {
		this.label = label;
		this.data = new ArrayList<Integer>();
		this.tokenCount = 0;
	}

	public void commit() {
		int occurence = this.data.get(tokenCount) + 1;
		this.data.set(tokenCount, occurence);
		tokenCount = 0;
	}

	public void update() {
		this.tokenCount++;
	}

	protected int width() {
		return this.data.size() - 1;
	}

	protected int wholeSize() {
		return this.data.size();
	}

	public void normalize() {
		// this.orderByTokenFrequency();
	}

	protected int getTokenFrequencyI(int idx) {
		// return idx < this.width() ? this.data.get(idx).tokenFrequency : 0;
		return 0;
	}

	protected double getTokenFrequencyF(int idx) {
		// return idx < this.width() ? this.data.get(idx).tokenFrequency : 0;
		return 0.0;
	}

	public double residualMass(int idx) {
		int rm = this.wholeSize();
		for (int i = idx; i >= 0; i--) {
			rm -= this.getTokenFrequencyI(i);
		}
		return (double) rm / this.wholeSize();
	}

	public double coverage() {
		double cov = 0;
		for (int i = 0; i < this.width(); i++) {
			cov += this.getTokenFrequencyI(i);
		}
		return cov / this.wholeSize();
	}

	static protected double calcKLD(Histogram h1, Histogram h2) {
		double kld = 0;
		double v1, v2;
		for (int i = 0; i < h1.width(); i++) {
			v1 = h1.getTokenFrequencyF(i);
			v2 = h2.getTokenFrequencyF(i);
			kld += (v1 / h1.wholeSize()) * Math.log(v1 / v2);
		}
		return kld;
	}

	static public double calcSimilarity(Histogram h1, Histogram h2) {
		double sim = 0;
		Histogram ave = Histogram.average(h1, h2);
		sim = (Histogram.calcKLD(h1, ave) / 2) + (Histogram.calcKLD(h2, ave) / 2);
		return sim;
	}

	static public Histogram average(Histogram h1, Histogram h2) {
		// List<Bar> newBody = new ArrayList<>();
		int[] sums = new int[Math.max(h1.width(), h2.width())];
		for (int i = 0; i < sums.length; i++) {
			sums[i] += h1.getTokenFrequencyI(i);
			sums[i] += h2.getTokenFrequencyI(i);
			// newBody.add(new Bar(0, sums[i]));
		}
		// return new AverageHistogram(null, newBody, Math.max(h1.wholeSize(),
		// h2.wholeSize()), false);
		return null;
	}
}
