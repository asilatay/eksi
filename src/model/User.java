package model;

public class User {
	
	private int id;
	
	private String name;
	
	private String surname;
	
	private String nickname;
	
	public User() {
		
	}

	public User(int id, String name, String surname, String nickname) {
		super();
		this.id = id;
		this.name = name;
		this.surname = surname;
		this.nickname = nickname;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

}
