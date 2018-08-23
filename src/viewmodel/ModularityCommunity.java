package viewmodel;

public class ModularityCommunity {

	private String modularityCommunityName;
	
	private String overlappingCommunityName;
	
	private int count;

	public String getModularityCommunityName() {
		return modularityCommunityName;
	}

	public void setModularityCommunityName(String modularityCommunityName) {
		this.modularityCommunityName = modularityCommunityName;
	}

	public String getOverlappingCommunityName() {
		return overlappingCommunityName;
	}

	public void setOverlappingCommunityName(String overlappingCommunityName) {
		this.overlappingCommunityName = overlappingCommunityName;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	@Override
	public String toString() {
		return "ModularityCommunity [modularityCommunityName=" + modularityCommunityName + ", overlappingCommunityName="
				+ overlappingCommunityName + ", count=" + count + "]";
	}
	
}