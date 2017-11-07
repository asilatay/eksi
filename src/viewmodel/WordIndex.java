package viewmodel;

import java.math.BigDecimal;

/**
 * 
 * @author ASIL
 * Bu sýnýf bir indexte bulunan kelimeyi ve bu kelimenin tüm corpus ta kaç defa kullanýldýðýný göstermek için oluþturulmuþ bir sýnýftýr
 */
public class WordIndex {
	
	private int index;
	
	private String word;
	
	private BigDecimal frequency; //Bir kelime corpusta kaç defa görülmüþ.
	
	public WordIndex() {
		
	}

	public WordIndex (int index, String word, BigDecimal frequency) {
		this.index = index;
		this.word = word;
		this.frequency = frequency;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public BigDecimal getFrequency() {
		return frequency;
	}

	public void setFrequency(BigDecimal frequency) {
		this.frequency = frequency;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((frequency == null) ? 0 : frequency.hashCode());
		result = prime * result + index;
		result = prime * result + ((word == null) ? 0 : word.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WordIndex other = (WordIndex) obj;
		if (frequency == null) {
			if (other.frequency != null)
				return false;
		} else if (!frequency.equals(other.frequency))
			return false;
		if (index != other.index)
			return false;
		if (word == null) {
			if (other.word != null)
				return false;
		} else if (!word.equals(other.word))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "WordIndex [index=" + index + ", word=" + word + ", frequency=" + frequency + "]";
	}


	
}
