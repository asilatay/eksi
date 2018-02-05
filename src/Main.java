
import java.util.List;
import java.util.Scanner;

import model.Title;
import model.User;
import service.CrawlerManager;
import service.CrawlerManagerImpl;
import service.EngineManager;
import service.EngineManagerImpl;
import service.EntryManager;
import service.EntryManagerImpl;
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
		
		CrawlerManager crawlerManager = new CrawlerManagerImpl();
		
		EntryManager entryManager = new EntryManagerImpl();
		
		String dir = "C:\\webharvest\\KAYDET\\";
		String ham = "C:\\webharvest\\HAM\\";
		final String fileReadingPath = "C:\\Users\\ASIL\\git\\eksiGit\\entries.txt";
		final String bilkentXmlPath ="D:\\Yüksek Lisans\\Tez\\Bilkent DATA\\BilCol2005\\1.xml";
		String operationSelect ="-1";
		System.out.println("GitHub Entegrasyonu OK!");
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
			System.out.println("Baþlýklarý birleþtir              -> Press 8");
			System.out.println("Co-Occurence Matrix(ESKÝ)         -> Press 9");
			System.out.println("Tüm Entry leri Dýþa Aktar         -> Press 10");
			System.out.println("Parametrik Entry Dýþa Aktarým     -> Press 11");
			System.out.println("Co-Occurence Matrix (YENÝ - FÝLE) -> Press 12");
			System.out.println("Benzer Kullanýcý - Title Hesaplama-> Press 13");
			System.out.println("Kullanýcý - Title Hesaplama       -> Press 14");
			System.out.println("Jaccard Benzerliðini Hesaplama    -> Press 15");
			System.out.println("Bilkent Örnek Veriyi Çalýþtýr     -> Press 16");
			operationSelect = scanIn.nextLine();
			if (operationSelect.equals("1")) {
				crawlerManager.createCrudeLinks(ham);
				
			} else if (operationSelect.equals("2")) {
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
			} else if (operationSelect.equals("3")) {
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
			} else if (operationSelect.equals("4")) {
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
			} else if (operationSelect.equals("5")) {
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
			} else if (operationSelect.equals("6")) {
				crawlerManager.getLinksFromMainPage(url);
			} else if (operationSelect.equals("7")) {
				crawlerManager.getDocumentWithjSoup(url, dir);
			} else if (operationSelect.equals("8")) {
				crawlerManager.findDuplicateTitlesAndMerge();
			} else if (operationSelect.equals("9")) {
				System.out.println("Hesaplanmasýný istediðiniz entry sayýsýný giriniz : ");
				int parameterForEntryCount = scanIn.nextInt();
				engineManager.coOccurenceMatrixWithEntryObjectAndReturnWindowSizeOLD(parameterForEntryCount);
			} else if (operationSelect.equals("10")) {
				entryManager.writeAllEntriesToDocument();
			} else if (operationSelect.equals("11")) {
				System.out.println("Çekilmesini istediðiniz entry sayýsýný giriniz : ");
				int parameterForEntryCount = scanIn.nextInt();
				entryManager.writeSpecificEntryCountToDocument(parameterForEntryCount);
			} else if (operationSelect.equals("12")) {
				engineManager.createCoOccurenceMatrix(fileReadingPath);
			} else if (operationSelect.equals("13")) {
				entryManager.getSimilarUsersThatWriteTheSameTitle();
			} else if (operationSelect.equals("14")) {
				entryManager.getTitleCountOfUsers();
			} else if (operationSelect.equals("15")) {
				engineManager.calculateJaccardSimilarityAndSave();
			} else if (operationSelect.equals("16")) {
				engineManager.runBilkentData(bilkentXmlPath);
			}
			
		} while(!operationSelect.equals("0"));
		
		scanIn.close();

	}	
}
