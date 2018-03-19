package service;

import java.util.List;

public interface ImportManager {

	List<String> readFromTxt(String readTextPath);
	
	List<String> readBilkentDataTxt(String readTxtPath);

}
