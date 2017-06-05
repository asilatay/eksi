package model;

public class KeyIndex {

	public Object row;
	
	public Object column;
	
	public String rowWord;
	
	public String columnWord;

	public KeyIndex(Object row, Object column, String rowWord, String columnWord){
		this.row = row;
		this.column = column;
		this.rowWord = rowWord;
		this.columnWord = columnWord;
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

	public String getRowWord() {
		return rowWord;
	}

	public void setRowWord(String rowWord) {
		this.rowWord = rowWord;
	}

	public String getColumnWord() {
		return columnWord;
	}

	public void setColumnWord(String columnWord) {
		this.columnWord = columnWord;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((column == null) ? 0 : column.hashCode());
		result = prime * result + ((columnWord == null) ? 0 : columnWord.hashCode());
		result = prime * result + ((row == null) ? 0 : row.hashCode());
		result = prime * result + ((rowWord == null) ? 0 : rowWord.hashCode());
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
		if (columnWord == null) {
			if (other.columnWord != null)
				return false;
		} else if (!columnWord.equals(other.columnWord))
			return false;
		if (row == null) {
			if (other.row != null)
				return false;
		} else if (!row.equals(other.row))
			return false;
		if (rowWord == null) {
			if (other.rowWord != null)
				return false;
		} else if (!rowWord.equals(other.rowWord))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "KeyIndex [row=" + row + ", column=" + column + ", rowWord=" + rowWord + ", columnWord=" + columnWord
				+ "]";
	}	
	
}
