package model;

import java.util.ArrayList;

public class UserEntryFrequency {
	
	private User user1;
	
	private int user1Id;
	
	private User user2;
	
	private int user2Id;
	
	private ArrayList<Title> titleList;
	
	private ArrayList<Integer> titleIdList;

	public User getUser1() {
		return user1;
	}

	public void setUser1(User user1) {
		this.user1 = user1;
	}

	public User getUser2() {
		return user2;
	}

	public void setUser2(User user2) {
		this.user2 = user2;
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

	public int getUser1Id() {
		return user1Id;
	}

	public void setUser1Id(int user1Id) {
		this.user1Id = user1Id;
	}

	public int getUser2Id() {
		return user2Id;
	}

	public void setUser2Id(int user2Id) {
		this.user2Id = user2Id;
	}
	

	
}
