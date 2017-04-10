package model;
public class Entry {
	
	private int id;
	
	private String description;
	
	private String date;
	
	private User user;
	
	private Title title;
	
	private String entryLink;
	
	private int titleId;
	
	private int userId;
	
	public Entry(int id, String description, String date, User user, Title title, String entryLink, int titleId, int userId) {
		super();
		this.id = id;
		this.description = description;
		this.date = date;
		this.user = user;
		this.title = title;
		this.entryLink = entryLink;
		this.titleId = titleId;
		this.userId = userId;
	}
	
	public Entry()  {
		
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Title getTitle() {
		return title;
	}

	public void setTitle(Title title) {
		this.title = title;
	}

	public String getEntryLink() {
		return entryLink;
	}

	public void setEntryLink(String entryLink) {
		this.entryLink = entryLink;
	}

	public int getTitleId() {
		return titleId;
	}

	public void setTitleId(int titleId) {
		this.titleId = titleId;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

}
