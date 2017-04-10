package service;

import java.util.List;

import model.User;

public interface UserManager {
	
	List<User> getAllUsers();
	
	User getUserById(int id);
	
	boolean addUser(User user);
	
	User getUserByUsername(String username);
	
}
