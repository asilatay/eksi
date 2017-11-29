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
	
	//Arraylerden ayný cellde 999999 olan veriler temizlendikten sonra sadece arr1 de kalan 999999 un tüm sayýya oraný
	private String numberOf999999Index1;
	//Arraylerden ayný cellde 999999 olan veriler temizlendikten sonra sadece arr2 de kalan 999999 un tüm sayýya oraný
	private String numberOf999999Index2;

	public String getNumberOf999999Index1() {
		return numberOf999999Index1;
	}

	public void setNumberOf999999Index1(String numberOf999999Index1) {
		this.numberOf999999Index1 = numberOf999999Index1;
	}

	public String getNumberOf999999Index2() {
		return numberOf999999Index2;
	}

	public void setNumberOf999999Index2(String numberOf999999Index2) {
		this.numberOf999999Index2 = numberOf999999Index2;
	}

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
