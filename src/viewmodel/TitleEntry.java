package viewmodel;

import java.util.ArrayList;

import model.Entry;

/**
 * 
 * @author ASIL
 * Bu sýnýf bir title ve entry bilgisini bir arada tutan bir ara modeldir.
 */
public class TitleEntry {
	
	private String titleName;
	
	private int titleId;
	
	private String entryDescription;

	public String getTitleName() {
		return titleName;
	}

	public void setTitleName(String titleName) {
		this.titleName = titleName;
	}

	public int getTitleId() {
		return titleId;
	}

	public void setTitleId(int titleId) {
		this.titleId = titleId;
	}

	public String getEntryDescription() {
		return entryDescription;
	}

	public void setEntryDescription(String entryDescription) {
		this.entryDescription = entryDescription;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((entryDescription == null) ? 0 : entryDescription.hashCode());
		result = prime * result + titleId;
		result = prime * result + ((titleName == null) ? 0 : titleName.hashCode());
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
		TitleEntry other = (TitleEntry) obj;
		if (entryDescription == null) {
			if (other.entryDescription != null)
				return false;
		} else if (!entryDescription.equals(other.entryDescription))
			return false;
		if (titleId != other.titleId)
			return false;
		if (titleName == null) {
			if (other.titleName != null)
				return false;
		} else if (!titleName.equals(other.titleName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "TitleEntry [titleName=" + titleName + ", titleId=" + titleId + ", entryDescription=" + entryDescription
				+ "]";
	}

	
	
}
