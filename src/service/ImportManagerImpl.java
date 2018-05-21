package service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class ImportManagerImpl implements ImportManager {

	@Override
	public List<String> readFromTxt(String readTextPath) {
		try {
			
			// Directory i�inde ne kadar dosya varsa bunlar�n path ini bir listeye doldurur
			List<Path> filesInDirectory = new ArrayList<Path>();
			
			try (Stream<Path> paths = Files.walk(Paths.get(readTextPath))) 
			{
				paths.filter(Files::isRegularFile).forEach(filesInDirectory::add);
				
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("Veri okunurken problem olu�tu");
			}
			
			int fileCount = filesInDirectory.size();
			System.out.println("Sistemdeki toplam dosya say�s� -> " + fileCount);
			
			List<String> wordList = new ArrayList<String>();
			for (Path p : filesInDirectory) {
				
				BufferedReader in = new BufferedReader(new FileReader(p.toString()));
				String line;
				while ((line = in.readLine()) != null) {
					wordList.add(line);
				}
				in.close();
			}
			
			System.out.println("Dosyalar okundu, memory e al�nd�");
			
			return wordList;
			
		} catch (Exception e) {
			System.err.println("TXT dosyas� okunurken kritik bir hata olu�tu.");
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
			System.out.println("TXT den Bilkent Verisi okuma i�lemi tamamland�. Operasyonel s�rece ge�iliyor. Veri B�y�kl��� : " + wordList.size());
			return wordList;
		} catch (Exception e) {
			System.err.println("TXT dosyas� okunurken kritik bir hata olu�tu.");
			e.printStackTrace();
			return null;
		}
	}
	
}
