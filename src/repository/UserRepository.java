package repository;

import java.util.List;

import model.User;

public interface UserRepository {
	List<User> getAllUsers();
	
	User getUserById(int id);
	
	boolean addUser(User user);
	
	User getUserByUsername(String username);
}
