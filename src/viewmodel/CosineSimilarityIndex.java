package viewmodel;

import java.math.BigDecimal;

public class CosineSimilarityIndex {
	
	private double cosineSimilarity;
	
	private int index1;
	
	private int index2;
	
	private BigDecimal index1Total = BigDecimal.ZERO;
	
	private BigDecimal index2Total = BigDecimal.ZERO;
	
	private double[] index1Array;
	
	private double[] index2Array;

	public double[] getIndex1Array() {
		return index1Array;
	}

	public void setIndex1Array(double[] index1Array) {
		this.index1Array = index1Array;
	}

	public double[] getIndex2Array() {
		return index2Array;
	}

	public void setIndex2Array(double[] index2Array) {
		this.index2Array = index2Array;
	}

	public double getCosineSimilarity() {
		return cosineSimilarity;
	}

	public void setCosineSimilarity(double cosineSimilarity) {
		this.cosineSimilarity = cosineSimilarity;
	}

	public int getIndex1() {
		return index1;
	}

	public void setIndex1(int index1) {
		this.index1 = index1;
	}

	public int getIndex2() {
		return index2;
	}

	public void setIndex2(int index2) {
		this.index2 = index2;
	}

	public BigDecimal getIndex1Total() {
		return index1Total;
	}

	public void setIndex1Total(BigDecimal index1Total) {
		this.index1Total = index1Total;
	}

	public BigDecimal getIndex2Total() {
		return index2Total;
	}

	public void setIndex2Total(BigDecimal index2Total) {
		this.index2Total = index2Total;
	}
	
	

}
