package model;

import java.math.BigDecimal;

public class PMIValueIndexes {
	private int index1;
	
	private int index2;
	
	private BigDecimal pmiValue;
	
	public PMIValueIndexes() {
		
	}
	
	public PMIValueIndexes(int index1, int index2, BigDecimal pmiValue) {
		this.index1 = index1;
		this.index2 = index2;
		this.pmiValue = pmiValue;
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

	public BigDecimal getPmiValue() {
		return pmiValue;
	}

	public void setPmiValue(BigDecimal pmiValue) {
		this.pmiValue = pmiValue;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + index1;
		result = prime * result + index2;
		result = prime * result + ((pmiValue == null) ? 0 : pmiValue.hashCode());
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
		PMIValueIndexes other = (PMIValueIndexes) obj;
		if (index1 != other.index1)
			return false;
		if (index2 != other.index2)
			return false;
		if (pmiValue == null) {
			if (other.pmiValue != null)
				return false;
		} else if (!pmiValue.equals(other.pmiValue))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "PMIValueIndexes [index1=" + index1 + ", index2=" + index2 + ", pmiValue=" + pmiValue + "]";
	}

	

}