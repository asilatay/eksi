package repository;

import java.util.List;

import model.Title;

public interface TitleRepository {

	List<Title> getAllTitles();
	
	Title getTitleById(int id);
	
	boolean addTitle(Title title);
	
	Title getLastSavedTitle();
	
	void removeTitleWithId(int titleId);

	List<Integer> getTitleIdList();
}
