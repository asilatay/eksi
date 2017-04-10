package model;
import java.util.List;

public class Title {
	
	private int id;
	
	private String name;
	
	private String date;
	
	private List<Entry> entryList;
	
	private String link;
	
	public Title(){
		
	}

	public Title(int id, String name, String date, List<Entry> entryList, String link) {
		super();
		this.id = id;
		this.name = name;
		this.date = date;
		this.entryList = entryList;
		this.link = link;
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

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public List<Entry> getEntryList() {
		return entryList;
	}

	public void setEntryList(List<Entry> entryList) {
		this.entryList = entryList;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}
	
	

}
