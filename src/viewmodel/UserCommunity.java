package viewmodel;

import java.util.ArrayList;
import java.util.List;

public class UserCommunity {

	private String userName;
	
	private List<String> communityList = new ArrayList<String>();

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public List<String> getCommunityList() {
		return communityList;
	}

	public void setCommunityList(List<String> communityList) {
		this.communityList = communityList;
	}

	@Override
	public String toString() {
		return "UserCommunity [userName=" + userName + ", communityList=" + communityList + "]";
	}
	
}
