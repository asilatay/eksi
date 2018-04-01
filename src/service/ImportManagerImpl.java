package service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class ImportManagerImpl implements ImportManager {

	@Override
	public List<String> readFromTxt(String readTextPath) {
		try {
			BufferedReader in = new BufferedReader(new FileReader("entries.txt"));
			String line;
			List<String> wordList = new ArrayList<String>();
			while ((line = in.readLine()) != null) {
				wordList.add(line);
			}
			in.close();
			return wordList;
		} catch (Exception e) {
			System.err.println("TXT dosyasý okunurken kritik bir hata oluþtu.");
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	public List<String> readBilkentDataTxt(String readTxtPath) {
		try {
			BufferedReader in = new BufferedReader(new FileReader(readTxtPath));
			String line;
			List<String> wordList = new ArrayList<String>();
			while((line = in.readLine()) != null){
				if (! line.contains("<DOC>") && ! line.contains("<TITLE>")  && ! line.contains("<DOCID>") 
						&& ! line.contains("<SOURCE>") && ! line.contains("<DATE>") && ! line.contains("<TEXT>") && ! line.contains("</TEXT>")
						&& ! line.contains("<ALL>") && ! line.contains("</ALL>") && ! line.contains("<doc>") && ! line.contains("</doc>")) {	
					wordList.add(line);
				}
			}
			in.close();
			System.out.println("TXT den Bilkent Verisi okuma iþlemi tamamlandý. Operasyonel sürece geçiliyor. Veri Büyüklüðü : " + wordList.size());
			return wordList;
		} catch (Exception e) {
			System.err.println("TXT dosyasý okunurken kritik bir hata oluþtu.");
			e.printStackTrace();
			return null;
		}
	}
	
}
