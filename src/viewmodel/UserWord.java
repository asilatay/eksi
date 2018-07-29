package viewmodel;

public class UserWord {
	
	private String userName;
	
	private String word;
	
	private Integer priority;
	
	private Integer count;

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public Integer getPriority() {
		return priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}

	@Override
	public String toString() {
		return "UserWord [userName=" + userName + ", word=" + word + ", priority=" + priority + ", count=" + count
				+ "]";
	}
	

}
