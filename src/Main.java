
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
			System.out.println("��k                               -> Press 0");
			System.out.println("Linkleri ��kar    	  			  -> Press 1");
			System.out.println("Yeni Ba�l�k Ekle                  -> Press 2");
			System.out.println("T�m Ba�l�klar� Listele            -> Press 3");
			System.out.println("Yeni Kullan�c� Ekle               -> Press 4");
			System.out.println("T�m Kullan�c�lar� Listele         -> Press 5");
			System.out.println("Bug�n�n pop�ler linklerini kaydet -> Press 6");
			System.out.println("Local linkleri ayr��t�r           -> Press 7");
			menuItemS = scanIn.nextLine();
			if (menuItemS.equals("1")) {
				engineManager.createCrudeLinks(ham);
				
			} else if (menuItemS.equals("2")) {
				Title title = new Title();
				System.out.println("Ba�l��� giriniz");
				title.setName(scanIn.nextLine());
				System.out.println("Tarihi Giriniz");
				String dateS = scanIn.nextLine();
				title.setDate(dateS);
				boolean isItOk = titleManager.addTitle(title);
				if(isItOk) {
					System.out.println("Ekleme ba�ar�l�!");
				} else {
					System.err.println("��lem s�ras�nda kritik bir hata olu�tu!");
				}
			} else if (menuItemS.equals("3")) {
				List<Title> titleList = titleManager.getAllTitles();
				if(titleList != null && titleList.size() > 0) {					
					for (Title t : titleList) {
						System.out.println("ID : " + t.getId());
						System.out.println("�sim : "+t.getName());
						System.out.println("Tarih : "+t.getDate());
						if(t.getEntryList() != null && t.getEntryList().size() > 0) {							
							System.out.println("Girilen Entry Say�s� : " + t.getEntryList().size());
						} else {
							System.out.println("Girilen Entry Say�s� : " +0);
						}
						System.out.println("----------------------------");
					}
				} else {
					System.err.println("Hi� ba�l�k bulunamad�!");
				}
			} else if (menuItemS.equals("4")) {
				User user = new User();
				System.out.println("�sim giriniz");
				user.setName(scanIn.nextLine());
				System.out.println("Soyisim giriniz");
				user.setSurname(scanIn.nextLine());
				System.out.println("Nickname giriniz");
				user.setNickname(scanIn.nextLine());
				boolean isItOk = userManager.addUser(user);
				if (isItOk) {
					System.out.println("Ekleme ba�ar�l�!");
				} else {
					System.err.println("��lem s�ras�nda kritik bir hata olu�tu!");
				}
			} else if (menuItemS.equals("5")) {
				List<User> userList = userManager.getAllUsers();
				if (userList != null && userList.size() > 0) {
					for (User u : userList) {
						System.out.println("ID : " + u.getId());
						System.out.println("�sim : "+u.getName());
						System.out.println("Soyisim : "+u.getSurname());
						System.out.println("Nickname : "+u.getNickname());
						System.out.println("----------------------------");
					}
					
				} 
				
				else {
					System.err.println("Hi� kullan�c� bulunamad�!");
				}
			} else if (menuItemS.equals("6")) {
				engineManager.getLinksFromMainPage(url);
			} else if (menuItemS.equals("7")) {
				engineManager.getDocumentWithjSoup(url, dir);
			}
			
		} while(!menuItemS.equals("0"));

	}	
}
