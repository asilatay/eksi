package viewmodel;

import java.util.ArrayList;
import java.util.List;

public class WordCommunity {

	private String word;
	
	private List<String> communityList = new ArrayList<String>();

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public List<String> getCommunityList() {
		return communityList;
	}

	public void setCommunityList(List<String> communityList) {
		this.communityList = communityList;
	}

	@Override
	public String toString() {
		return "WordCommunity [word=" + word + ", communityList=" + communityList + "]";
	}

}
