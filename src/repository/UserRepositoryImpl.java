package repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.User;

public class UserRepositoryImpl implements UserRepository{
	private static String connectionIP ="jdbc:mysql://localhost/eksi";
	private static String username ="root";
	private static String pass = "pass";
	private static String db = connectionIP;
	private static String myDriver = "com.mysql.jdbc.Driver";
	
	@Override
	public List<User> getAllUsers() {
		try {
			String tableName = "user";
			Class.forName(myDriver);
			Connection conn = DriverManager.getConnection(db, username, pass);
			String query = "SELECT * FROM " +tableName;
			Statement st = conn.createStatement();        
			ResultSet rs = st.executeQuery(query); 
			List<User> userList = new ArrayList<User>();
	        while (rs.next()) {
	        	User currentUser = new User();
	            int s_id = rs.getInt("ID");
	            currentUser.setId(s_id);
	            String s_name = rs.getString("name");
	            currentUser.setName(s_name);
	            String s_surname = rs.getString("surname");
	            currentUser.setSurname(s_surname);
	            String s_nickname = rs.getString("nickname");
	            currentUser.setNickname(s_nickname);
	            userList.add(currentUser);
	        }
	        st.close();
	        conn.close();
	        return userList;
			
		} catch (Exception e) {
			  System.err.println("Database Connection Error ! USER TABLE");
	          System.err.println(e.getMessage());        
	          return null;
		}
	}
		
	
	@Override
	public User getUserById(int id) {
		try {			
			String tableName = "user";
			Class.forName(myDriver);
			Connection conn = DriverManager.getConnection(db, username, pass);
			String query = "SELECT * FROM " +tableName + " WHERE ID=" + id;
			Statement st = conn.createStatement();        
			ResultSet rs = st.executeQuery(query);
			while (rs.next()) {
				User currentUser = new User();
				int s_id = rs.getInt("ID");
	            currentUser.setId(s_id);
	            String s_name = rs.getString("name");
	            currentUser.setName(s_name);
	            String s_surname = rs.getString("surname");
	            currentUser.setSurname(s_surname);
	            String s_nickname = rs.getString("nickname");
	            currentUser.setNickname(s_nickname);
	            st.close();
				conn.close();
	            return (User)currentUser;
			}
			return null;
			
		} catch (Exception e) {
			System.err.println("Database Connection Error ! USER TABLE");
	        System.err.println(e.getMessage());        
	        return null;
		}
	}
	
	@Override
	public boolean addUser(User user) {
		try {
			String tableName = "user";
			Class.forName(myDriver);
			Connection conn = DriverManager.getConnection(db, username, pass);
			String q1 = "SELECT MAX(ID) +1 AS id FROM " +tableName;
			Statement st1 = conn.createStatement();	          
	        ResultSet rs = st1.executeQuery(q1);
	        int id = 1;
	        while (rs.next()){
	        	if(rs != null) {	        		
	        		id = rs.getInt("id");
	        	}
	        	if(id == 0) {
	        		id = 1;
	        	}
	        }
	        st1.close();	        
			String query = "INSERT INTO "+tableName +" (id,name,surname,nickname)VALUES(";
			query += +id+",'"+user.getName()+"','"+user.getSurname()+"','"+user.getNickname()+"')";
			Statement st = conn.createStatement();
			st.executeUpdate(query);
			st.close();        
			conn.close();
			return true;
			
		} catch (Exception e) {
			System.err.println("Database Connection Error ! USER TABLE");
	        System.err.println(e.getMessage());
			return false;
		}
	}
	
	@Override
	public User getUserByUsername(String usernameParameter) {
		try {
			String tableName = "user";
			Class.forName(myDriver);
			Connection conn = DriverManager.getConnection(db, username, pass);
			String query = "SELECT * FROM " + tableName + " WHERE nickname='" + usernameParameter + "'";
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery(query);
			while (rs.next()) {
				User currentUser = new User();
				int s_id = rs.getInt("ID");
				currentUser.setId(s_id);
				String s_name = rs.getString("name");
				currentUser.setName(s_name);
				String s_surname = rs.getString("surname");
				currentUser.setSurname(s_surname);
				String s_nickname = rs.getString("nickname");
				currentUser.setNickname(s_nickname);
				st.close();
				conn.close();
				return (User)currentUser;
			}
			return null;

		} catch (Exception e) {
			System.err.println("Database Connection Error ! USER TABLE");
			System.err.println(e.getMessage());
			return null;
		}
	}
	
	@Override
	public Map<Integer, String> getIdUserNameMap(List<Integer> idList) {
		try {			
			String tableName = "user";
			Class.forName(myDriver);
			Connection conn = DriverManager.getConnection(db, username, pass);
			String query = "SELECT ID as id, nickname AS nickname FROM " + tableName + " WHERE id IN(" ;
			int count = 1;
			for (Integer i : idList) {
				if (count == idList.size()) {					
					query += i ;
				} else {
					query += i +",";
				}
				count++;
			}
			query += ");";
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery(query);
			Map<Integer, String> idUserNameMap = new HashMap<Integer, String>();
			while (rs.next()) {
				String userName = rs.getString("nickname");
				int idOfUser = rs.getInt("ID");
				if (! idUserNameMap.containsKey(idOfUser)) {					
					idUserNameMap.put(idOfUser, userName);
				}
			}
			return idUserNameMap;
			
		} catch (Exception e) {
			System.err.println("Database Connection Error ! USER TABLE");
			System.err.println(e.getMessage());
			return null;
		}
	}
	
	@Override
	public List<Integer> getUserIdList() {
		try {
			String tableName = "user";
			Class.forName(myDriver);
			
			Connection conn = DriverManager.getConnection(db, username, pass);
			
			String query = "SELECT id as ID FROM " + tableName;
			
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery(query);
			
			List<Integer> userIdList = new ArrayList<Integer>();
			while (rs.next()) {
				int s_id = rs.getInt("ID");
				userIdList.add(s_id);
			}
			
			st.close();
			conn.close();
			
			return userIdList;

		} catch (Exception e) {
			System.err.println("Database Connection Error ! USER TABLE");
			System.err.println(e.getMessage());
			return null;
		}
	}
}
