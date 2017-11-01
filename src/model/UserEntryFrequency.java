package model;

import java.util.ArrayList;

public class UserEntryFrequency {
	
	private User user;
	
	private int userId;
	
	private ArrayList<Title> titleList;
	
	private ArrayList<Integer> titleIdList;

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public ArrayList<Title> getTitleList() {
		return titleList;
	}

	public void setTitleList(ArrayList<Title> titleList) {
		this.titleList = titleList;
	}

	public ArrayList<Integer> getTitleIdList() {
		return titleIdList;
	}

	public void setTitleIdList(ArrayList<Integer> titleIdList) {
		this.titleIdList = titleIdList;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	
}
