package viewmodel;

import java.util.ArrayList;

import model.Entry;

/**
 * 
 * @author ASIL
 * Bu sýnýf bir kullanýcýnýn girdiði entry leri tutan ara sýnýftýr
 */
public class UserEntry {
	
	private String username;
	
	private int userId;
	
	private String entryDescription;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEntryDescription() {
		return entryDescription;
	}

	public void setEntryDescription(String entryDescription) {
		this.entryDescription = entryDescription;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((entryDescription == null) ? 0 : entryDescription.hashCode());
		result = prime * result + userId;
		result = prime * result + ((username == null) ? 0 : username.hashCode());
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
		UserEntry other = (UserEntry) obj;
		if (entryDescription == null) {
			if (other.entryDescription != null)
				return false;
		} else if (!entryDescription.equals(other.entryDescription))
			return false;
		if (userId != other.userId)
			return false;
		if (username == null) {
			if (other.username != null)
				return false;
		} else if (!username.equals(other.username))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "UserEntry [username=" + username + ", userId=" + userId + ", entryDescription=" + entryDescription
				+ "]";
	}

}
