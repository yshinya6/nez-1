package nez.infer;

import java.util.ArrayList;
import java.util.List;

public class Histogram {

	private List<Integer> dataCollections;
	private List<DataUnit> dataUnits;
	private String label; // a label of the target token
	private int tmpTokenFrequency;

	public Histogram(String label) {
		this.label = label;
		this.dataCollections = new ArrayList<Integer>();
		this.dataUnits = new ArrayList<DataUnit>();
		this.tmpTokenFrequency = 0;
	}

	public Histogram(String label, List<DataUnit> dataUnits) {
		this.label = label;
		this.dataUnits = dataUnits;
	}

	public final String getLabel() {
		return this.label;
	}

	public final void commit() {
		int chunkCount = this.dataCollections.get(tmpTokenFrequency) + 1;
		this.dataCollections.set(tmpTokenFrequency, chunkCount);
		tmpTokenFrequency = 0;
	}

	public void update() {
		this.tmpTokenFrequency++;
	}

	private final int width() {
		return this.dataUnits.size() - 1;
	}

	private final int wholeSize() {
		return this.dataUnits.size();
	}

	public void normalize() {
		this.newDataUnits();
		this.orderByTokenFrequency();
	}

	private final void newDataUnits() {
		this.dataUnits.add(new DataUnit(0, this.dataCollections.get(0)));
		for (int i = 1; i < this.dataCollections.size(); i++) {
			int chunkCount = this.dataCollections.get(i);
			if (chunkCount != 0) {
				this.dataUnits.add(new DataUnit(i, chunkCount));
			}
		}
	}

	private final void orderByTokenFrequency() {
		this.dataUnits.sort((unit1, unit2) -> {
			int subOfTokenFrequency = unit2.getTokenFrequency() - unit1.getTokenFrequency();
			int subOfChunkCount = unit2.getChunkCount() - unit1.getChunkCount();
			if (subOfChunkCount == 0) {
				return subOfChunkCount;
			} else {
				return subOfTokenFrequency;
			}
		});
	}

	private final int getChunkCountI(int idx) {
		return idx < this.width() ? this.dataUnits.get(idx).getTokenFrequency() : 0;
	}

	private final double getChunkCountF(int idx) {
		return idx < this.width() ? this.dataUnits.get(idx).getTokenFrequency() : 0;
	}

	public final double residualMass(int idx) {
		int rm = this.wholeSize();
		for (int i = idx; i >= 0; i--) {
			rm -= this.getChunkCountI(i);
		}
		return (double) rm / this.wholeSize();
	}

	public final double coverage() {
		double cov = 0.0;
		for (int i = 0; i < this.width(); i++) {
			cov += this.getChunkCountI(i);
		}
		return cov / this.wholeSize();
	}

	protected static double calcRelativeEntropy(Histogram h1, Histogram h2) {
		double relativeEntropy = 0.0;
		double f1, f2;
		for (int i = 0; i < h1.width(); i++) {
			f1 = h1.getChunkCountF(i);
			f2 = h2.getChunkCountF(i);
			relativeEntropy += (f1 / h1.wholeSize()) * Math.log(f1 / f2);
		}
		return relativeEntropy;
	}

	public static double calcSimilarity(Histogram h1, Histogram h2) {
		double sim = 0.0;
		Histogram ave = Histogram.average(h1, h2);
		sim = (Histogram.calcRelativeEntropy(h1, ave) / 2) + (Histogram.calcRelativeEntropy(h2, ave) / 2);
		return sim;
	}

	public static Histogram average(Histogram h1, Histogram h2) {
		List<DataUnit> newBody = new ArrayList<>();
		int[] sums = new int[Math.max(h1.width(), h2.width())];
		for (int i = 0; i < sums.length; i++) {
			sums[i] += h1.getChunkCountI(i);
			sums[i] += h2.getChunkCountI(i);
			newBody.add(new DataUnit(0, sums[i] / 2));
		}
		String label = String.format("AVE_%s_%s", h1.getLabel(), h2.getLabel());
		return new Histogram(label, newBody);
	}
}

class DataUnit {
	private final int tokenFrequency;
	private final int chunkCount;

	DataUnit(int tokenFrequency, int chunkCount) {
		this.tokenFrequency = tokenFrequency;
		this.chunkCount = chunkCount;
	}

	public int getTokenFrequency() {
		return tokenFrequency;
	}

	public int getChunkCount() {
		return chunkCount;
	}
}
