package viewmodel;

import java.math.BigDecimal;

public class MostSimilarWord {
	
	private String originWord;
	
	private String otherWord;
	
	private BigDecimal similarityRate;

	public String getOriginWord() {
		return originWord;
	}

	public void setOriginWord(String originWord) {
		this.originWord = originWord;
	}

	public String getOtherWord() {
		return otherWord;
	}

	public void setOtherWord(String otherWord) {
		this.otherWord = otherWord;
	}

	public BigDecimal getSimilarityRate() {
		return similarityRate;
	}

	public void setSimilarityRate(BigDecimal similarityRate) {
		this.similarityRate = similarityRate;
	}

	@Override
	public String toString() {
		return "MostSimilarWord [originWord=" + originWord + ", otherWord=" + otherWord + ", similarityRate="
				+ similarityRate + "]";
	}
	
	

}
