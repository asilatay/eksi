package viewmodel;

/**
 * 
 * @author ASIL
 * Bu sýnýf bir kullanýcýnýn kaç tane title a yazdýðýný göstermek için oluþturulmuþ bir sýnýftýr
 */
public class UserTitle {
	
	private String username;
	
	private int countOfTitleThatWrote;
	
	private int userId;

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public int getCountOfTitleThatWrote() {
		return countOfTitleThatWrote;
	}

	public void setCountOfTitleThatWrote(int countOfTitleThatWrote) {
		this.countOfTitleThatWrote = countOfTitleThatWrote;
	}

}
