package service;

import java.util.List;

public interface EngineManager {

	
	void coOccurenceMatrixWithEntryObjectAndReturnWindowSizeOLD(int parameterForEntryCount);
	
	List<String> splittedEntryDescription(List<String>retList,String  entryDescription);
	
	void createCoOccurenceMatrix(String readTextPath);
	
}
