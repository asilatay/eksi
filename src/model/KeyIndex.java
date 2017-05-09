package model;

public class KeyIndex {

	public Object row;
	
	public Object column;

	public KeyIndex(Object row, Object column){
		this.row = row;
		this.column = column;
	}
	
	public Object getRow() {
		return row;
	}

	public void setRow(Object row) {
		this.row = row;
	}

	public Object getColumn() {
		return column;
	}

	public void setColumn(Object column) {
		this.column = column;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((column == null) ? 0 : column.hashCode());
		result = prime * result + ((row == null) ? 0 : row.hashCode());
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
		KeyIndex other = (KeyIndex) obj;
		if (column == null) {
			if (other.column != null)
				return false;
		} else if (!column.equals(other.column))
			return false;
		if (row == null) {
			if (other.row != null)
				return false;
		} else if (!row.equals(other.row))
			return false;
		return true;
	}	
}
