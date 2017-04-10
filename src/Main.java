
import java.sql.Date;
import java.util.List;
import java.util.Scanner;

import model.Title;
import model.User;
import service.EngineManager;
import service.EngineManagerImpl;
import service.TitleManager;
import service.TitleManagerImpl;
import service.UserManager;
import service.UserManagerImpl;

public class Main {
//	static final String url ="https://eksisozluk.com/sitemap.xml";
	static final String url ="https://eksisozluk.com/";
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		EngineManager engineManager = new EngineManagerImpl();
		TitleManager titleManager = new TitleManagerImpl();
		UserManager userManager = new UserManagerImpl();
		String dir = "C:\\webharvest\\KAYDET\\";
		String ham = "C:\\webharvest\\HAM\\";
		String menuItemS ="-1";
		Scanner scanIn = new Scanner(System.in);		
		do {
			System.out.println("Çýk                               -> Press 0");
			System.out.println("Linkleri Çýkar    	  			  -> Press 1");
			System.out.println("Yeni Baþlýk Ekle                  -> Press 2");
			System.out.println("Tüm Baþlýklarý Listele            -> Press 3");
			System.out.println("Yeni Kullanýcý Ekle               -> Press 4");
			System.out.println("Tüm Kullanýcýlarý Listele         -> Press 5");
			System.out.println("Bugünün popüler linklerini kaydet -> Press 6");
			System.out.println("Local linkleri ayrýþtýr           -> Press 7");
			menuItemS = scanIn.nextLine();
			if (menuItemS.equals("1")) {
				engineManager.createCrudeLinks(ham);
				
			} else if (menuItemS.equals("2")) {
				Title title = new Title();
				System.out.println("Baþlýðý giriniz");
				title.setName(scanIn.nextLine());
				System.out.println("Tarihi Giriniz");
				String dateS = scanIn.nextLine();
				title.setDate(dateS);
				boolean isItOk = titleManager.addTitle(title);
				if(isItOk) {
					System.out.println("Ekleme baþarýlý!");
				} else {
					System.err.println("Ýþlem sýrasýnda kritik bir hata oluþtu!");
				}
			} else if (menuItemS.equals("3")) {
				List<Title> titleList = titleManager.getAllTitles();
				if(titleList != null && titleList.size() > 0) {					
					for (Title t : titleList) {
						System.out.println("ID : " + t.getId());
						System.out.println("Ýsim : "+t.getName());
						System.out.println("Tarih : "+t.getDate());
						if(t.getEntryList() != null && t.getEntryList().size() > 0) {							
							System.out.println("Girilen Entry Sayýsý : " + t.getEntryList().size());
						} else {
							System.out.println("Girilen Entry Sayýsý : " +0);
						}
						System.out.println("----------------------------");
					}
				} else {
					System.err.println("Hiç baþlýk bulunamadý!");
				}
			} else if (menuItemS.equals("4")) {
				User user = new User();
				System.out.println("Ýsim giriniz");
				user.setName(scanIn.nextLine());
				System.out.println("Soyisim giriniz");
				user.setSurname(scanIn.nextLine());
				System.out.println("Nickname giriniz");
				user.setNickname(scanIn.nextLine());
				boolean isItOk = userManager.addUser(user);
				if (isItOk) {
					System.out.println("Ekleme baþarýlý!");
				} else {
					System.err.println("Ýþlem sýrasýnda kritik bir hata oluþtu!");
				}
			} else if (menuItemS.equals("5")) {
				List<User> userList = userManager.getAllUsers();
				if (userList != null && userList.size() > 0) {
					for (User u : userList) {
						System.out.println("ID : " + u.getId());
						System.out.println("Ýsim : "+u.getName());
						System.out.println("Soyisim : "+u.getSurname());
						System.out.println("Nickname : "+u.getNickname());
						System.out.println("----------------------------");
					}
					
				} 
				
				else {
					System.err.println("Hiç kullanýcý bulunamadý!");
				}
			} else if (menuItemS.equals("6")) {
				engineManager.getLinksFromMainPage(url);
			} else if (menuItemS.equals("7")) {
				engineManager.getDocumentWithjSoup(url, dir);
			}
			
		} while(!menuItemS.equals("0"));

	}	
}
