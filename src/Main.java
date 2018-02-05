
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
		final String bilkentXmlPath ="D:\\Y�ksek Lisans\\Tez\\Bilkent DATA\\BilCol2005\\1.xml";
		String operationSelect ="-1";
		System.out.println("GitHub Entegrasyonu OK!");
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
			System.out.println("Ba�l�klar� birle�tir              -> Press 8");
			System.out.println("Co-Occurence Matrix(ESK�)         -> Press 9");
			System.out.println("T�m Entry leri D��a Aktar         -> Press 10");
			System.out.println("Parametrik Entry D��a Aktar�m     -> Press 11");
			System.out.println("Co-Occurence Matrix (YEN� - F�LE) -> Press 12");
			System.out.println("Benzer Kullan�c� - Title Hesaplama-> Press 13");
			System.out.println("Kullan�c� - Title Hesaplama       -> Press 14");
			System.out.println("Jaccard Benzerli�ini Hesaplama    -> Press 15");
			System.out.println("Bilkent �rnek Veriyi �al��t�r     -> Press 16");
			operationSelect = scanIn.nextLine();
			if (operationSelect.equals("1")) {
				crawlerManager.createCrudeLinks(ham);
				
			} else if (operationSelect.equals("2")) {
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
			} else if (operationSelect.equals("3")) {
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
			} else if (operationSelect.equals("4")) {
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
			} else if (operationSelect.equals("5")) {
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
			} else if (operationSelect.equals("6")) {
				crawlerManager.getLinksFromMainPage(url);
			} else if (operationSelect.equals("7")) {
				crawlerManager.getDocumentWithjSoup(url, dir);
			} else if (operationSelect.equals("8")) {
				crawlerManager.findDuplicateTitlesAndMerge();
			} else if (operationSelect.equals("9")) {
				System.out.println("Hesaplanmas�n� istedi�iniz entry say�s�n� giriniz : ");
				int parameterForEntryCount = scanIn.nextInt();
				engineManager.coOccurenceMatrixWithEntryObjectAndReturnWindowSizeOLD(parameterForEntryCount);
			} else if (operationSelect.equals("10")) {
				entryManager.writeAllEntriesToDocument();
			} else if (operationSelect.equals("11")) {
				System.out.println("�ekilmesini istedi�iniz entry say�s�n� giriniz : ");
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
