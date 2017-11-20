package service;

import java.util.List;
import java.util.Map;

import model.User;
import repository.UserRepository;
import repository.UserRepositoryImpl;

public class UserManagerImpl implements UserManager{
	
	UserRepository userRepository = new UserRepositoryImpl();
	
	@Override
	public List<User> getAllUsers() {
		return userRepository.getAllUsers();
	}
	
	@Override
	public User getUserById(int id) {
		return userRepository.getUserById(id);
	}
	
	@Override
	public boolean addUser(User user) {
		return userRepository.addUser(user);
	}
	
	@Override
	public User getUserByUsername(String username) {
		return userRepository.getUserByUsername(username);
	}
	
	@Override
	public  Map<Integer, String> getIdUserNameMap(List<Integer> idList) {
		return userRepository.getIdUserNameMap(idList);
	}

}
