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
			while((line = in.readLine()) != null){
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
	
}
