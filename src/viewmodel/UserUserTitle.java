package viewmodel;

import java.math.BigDecimal;

import model.User;

public class UserUserTitle {
	
	private User user1;
	
	private String userName1;
	
	private int user1Id;
	
	private User user2;
	
	private String userName2;
	
	private int user2Id;
	
	private int countOfSimilarTitle;
	
	private BigDecimal jaccardSimilarity;

	public BigDecimal getJaccardSimilarity() {
		return jaccardSimilarity;
	}

	public void setJaccardSimilarity(BigDecimal jaccardSimilarity) {
		this.jaccardSimilarity = jaccardSimilarity;
	}

	public User getUser1() {
		return user1;
	}

	public void setUser1(User user1) {
		this.user1 = user1;
	}

	public int getUser1Id() {
		return user1Id;
	}

	public void setUser1Id(int user1Id) {
		this.user1Id = user1Id;
	}

	public User getUser2() {
		return user2;
	}

	public void setUser2(User user2) {
		this.user2 = user2;
	}

	public int getUser2Id() {
		return user2Id;
	}

	public void setUser2Id(int user2Id) {
		this.user2Id = user2Id;
	}

	public int getCountOfSimilarTitle() {
		return countOfSimilarTitle;
	}

	public void setCountOfSimilarTitle(int countOfSimilarTitle) {
		this.countOfSimilarTitle = countOfSimilarTitle;
	}

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
	
	
}
