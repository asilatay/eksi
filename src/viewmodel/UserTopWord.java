package viewmodel;

import java.util.ArrayList;
import java.util.List;

public class UserTopWord {

	private String userName;
	
	private List<String> wordList = new ArrayList<String>();

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public List<String> getWordList() {
		return wordList;
	}

	public void setWordList(List<String> wordList) {
		this.wordList = wordList;
	}

	@Override
	public String toString() {
		return "UserTopWord [userName=" + userName + ", wordList=" + wordList + "]";
	}

	
}
