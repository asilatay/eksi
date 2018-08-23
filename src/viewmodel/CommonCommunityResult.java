package viewmodel;

import java.util.ArrayList;
import java.util.List;

public class CommonCommunityResult {

	private String userName1;
	
	private String userName2;
	
	List<String> commonCommunitiesCN = new ArrayList<String>();
	
	List<String> commonCommunitesWAN = new ArrayList<String>();

	public String getUserName1() {
		return userName1;
	}

	public void setUserName1(String userName1) {
		this.userName1 = userName1;
	}

	public String getUserName2() {
		return userName2;
	}

	public void setUserName2(String userName2) {
		this.userName2 = userName2;
	}

	public List<String> getCommonCommunitiesCN() {
		return commonCommunitiesCN;
	}

	public void setCommonCommunitiesCN(List<String> commonCommunitiesCN) {
		this.commonCommunitiesCN = commonCommunitiesCN;
	}

	public List<String> getCommonCommunitesWAN() {
		return commonCommunitesWAN;
	}

	public void setCommonCommunitesWAN(List<String> commonCommunitesWAN) {
		this.commonCommunitesWAN = commonCommunitesWAN;
	}

	@Override
	public String toString() {
		return "CommonCommunityResult [userName1=" + userName1 + ", userName2=" + userName2 + ", commonCommunitiesCN="
				+ commonCommunitiesCN + ", commonCommunitesWAN=" + commonCommunitesWAN + "]";
	}
	
	
}
