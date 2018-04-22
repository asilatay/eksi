package service;

import java.util.List;

import model.Title;
import repository.TitleRepository;
import repository.TitleRepositoryImpl;

public class TitleManagerImpl implements TitleManager{
	
	TitleRepository titleRepository =  new TitleRepositoryImpl();
	
	@Override
	public List<Title> getAllTitles() {
		return titleRepository.getAllTitles();
	}
	
	@Override
	public Title getTitleById(int id) {
		return titleRepository.getTitleById(id);
	}
	
	@Override
	public boolean addTitle(Title title) {
		return titleRepository.addTitle(title);
	}
	
	@Override
	public Title getLastSavedTitle() {
		return titleRepository.getLastSavedTitle();
	}
	
	@Override
	public void removeTitleWithId(int titleId) {
		titleRepository.removeTitleWithId(titleId);
	}
	
	@Override
	public List<Integer> getTitleIdList() {
		return titleRepository.getTitleIdList();
	}

}
