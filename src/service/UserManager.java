package service;

import java.util.List;
import java.util.Map;

import model.User;

public interface UserManager {
	
	List<User> getAllUsers();
	
	User getUserById(int id);
	
	boolean addUser(User user);
	
	User getUserByUsername(String username);

	Map<Integer, String> getIdUserNameMap(List<Integer> idList);

	List<Integer> getUserIdList();
	
}
