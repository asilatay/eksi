package viewmodel;

import java.util.ArrayList;
import java.util.List;

public class ModularityOverlappingCommunityData {

	private String word;
	
	private String modularityCommunityName;
	
	private List<String> overlappingCommunitiesList = new ArrayList<String>();
	
	private Integer id;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public String getModularityCommunityName() {
		return modularityCommunityName;
	}

	public void setModularityCommunityName(String modularityCommunityName) {
		this.modularityCommunityName = modularityCommunityName;
	}

	public List<String> getOverlappingCommunitiesList() {
		return overlappingCommunitiesList;
	}

	public void setOverlappingCommunitiesList(List<String> overlappingCommunitiesList) {
		this.overlappingCommunitiesList = overlappingCommunitiesList;
	}

	@Override
	public String toString() {
		return "ModularityOverlappingCommunityData [word=" + word + ", modularityCommunityName="
				+ modularityCommunityName + ", overlappingCommunitiesList=" + overlappingCommunitiesList + ", id=" + id
				+ "]";
	}

	
}