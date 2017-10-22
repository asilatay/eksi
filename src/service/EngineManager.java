package service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import model.PMIValueIndexes;

public interface EngineManager {

	
	void coOccurenceMatrixWithEntryObjectAndReturnWindowSizeOLD(int parameterForEntryCount);
	
	List<String> splittedEntryDescription(List<String>retList,String  entryDescription);
	
	void createCoOccurenceMatrix(String readTextPath);
	
	void createVectors (Map<PMIValueIndexes, BigDecimal> filledMatrix);
	
}
