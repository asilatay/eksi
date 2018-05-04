package repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import model.Entry;
import viewmodel.TitleEntry;
import viewmodel.UserEntry;
import viewmodel.UserTitle;
import viewmodel.UserUserTitle;

public class EntryRepositoryImpl implements EntryRepository{
	String connectionIP ="jdbc:mysql://localhost/eksi";
	String username ="root";
	String pass = "pass";
	String db = connectionIP;
	String myDriver = "com.mysql.jdbc.Driver";
	
	TitleRepository titleRepository  = new TitleRepositoryImpl();
	
	UserRepository userRepository = new UserRepositoryImpl();

	@Override
	public List<Entry> getAllEntries() {
		try {
			String tableName = "entry";
			Class.forName(myDriver);
			Connection conn = DriverManager.getConnection(db, username, pass);
			String query = "SELECT * FROM " +tableName;
			Statement st = conn.createStatement();        
			ResultSet rs = st.executeQuery(query); 
			List<Entry> entryList = new ArrayList<Entry>();
			while (rs.next()) {
				Entry currentEntry = new Entry();
				int s_id = rs.getInt("ID");
				currentEntry.setId(s_id);
				String s_description = rs.getString("description");
				currentEntry.setDescription(s_description);
				String s_date = rs.getString("date");
				currentEntry.setDate(s_date);
				int fk_titleId = rs.getInt("fk_title_id");
				currentEntry.setTitleId(fk_titleId);
				currentEntry.setTitle(titleRepository.getTitleById(fk_titleId));
				int fk_userId = rs.getInt("fk_user_id");
				currentEntry.setUserId(fk_userId);
				currentEntry.setUser(userRepository.getUserById(fk_userId));
				entryList.add(currentEntry);

			}
			st.close();
			conn.close();
			return entryList;
			
		} catch (Exception e) {
			System.err.println("Database Connection Error ! ENTRY TABLE");
	        System.err.println(e.getMessage());        
	        return null;
		}
	}
	
	@Override
	public boolean addEntry(Entry entry) {
		try {
			String tableName = "entry";
			Class.forName(myDriver);
			Connection conn = DriverManager.getConnection(db, username, pass);
			String q1 = "SELECT MAX(ID) +1 AS id FROM " +tableName;
			Statement st1 = conn.createStatement();	          
	        ResultSet rs = st1.executeQuery(q1);
	        int id = 0;
	        while (rs.next()){
	        	id = rs.getInt("id");
	        }
	        st1.close();
	        
			String query = "INSERT INTO "+tableName +" (id,description,date,fk_title_id,fk_user_id,entry_link)VALUES(";
			query += id +",'" +entry.getDescription()+"','"+entry.getDate()+"',"+entry.getTitleId()+","+entry.getUserId()+",'"+entry.getEntryLink()+"');";
			Statement st = conn.createStatement();
			st.executeUpdate(query);
			st.close();        
			conn.close();
			return true;
			
		} catch (Exception e) {
			System.err.println("Database Connection Error ! ENTRY TABLE");
	        System.err.println(e.getMessage()); 
	        return false;
		}
	}
	
	@Override
	public int getEntryWithEntryLink(String entryUrl) {
		try {
		String tableName = "entry";
		Class.forName(myDriver);
		Connection conn = DriverManager.getConnection(db, username, pass);
		String query = "SELECT * FROM " +tableName+ " WHERE entry_link='"+entryUrl+"'";
		Statement st = conn.createStatement();        
		ResultSet rs = st.executeQuery(query); 
		Entry currentEntry = new Entry();
		int id = 0;
		while (rs.next()) {
			id = rs.getInt("ID");
			currentEntry.setId(id);
			}
			st.close();
			conn.close();
			return id;

		} catch (Exception e) {
			System.err.println("Database Connection Error ! ENTRY TABLE");
			System.err.println(e.getMessage());
			return 0;
		}
	}
	
	@Override
	public List<Entry> getEntriesWithTitleId(int titleId) {
		try {			
			String tableName = "entry";
			Class.forName(myDriver);
			Connection conn = DriverManager.getConnection(db, username, pass);
			String query = "SELECT * FROM " +tableName+ " WHERE fk_title_id=" +titleId;
			Statement st = conn.createStatement();        
			ResultSet rs = st.executeQuery(query); 
			List<Entry> entryList = new ArrayList<Entry>();
			while (rs.next()) {
				Entry currentEntry = new Entry();
				int s_id = rs.getInt("ID");
				currentEntry.setId(s_id);
				String s_description = rs.getString("description");
				currentEntry.setDescription(s_description);
				String s_date = rs.getString("date");
				currentEntry.setDate(s_date);
				String s_Link = rs.getString("entry_link");
				currentEntry.setEntryLink(s_Link);
				entryList.add(currentEntry);
			}
			st.close();
			conn.close();
			return entryList;
			
		} catch (Exception e) {
			System.err.println("Database Connection Error ! ENTRY TABLE");
			System.err.println(e.getMessage());
			return null;
		}
	}
	
	@Override
	public void updateEntryTitle(int entryId, int newTitleId) {
		try {			
			String tableName = "entry";
			Class.forName(myDriver);
			Connection conn = DriverManager.getConnection(db, username, pass);
			String query = "UPDATE "+tableName +" SET fk_title_id =" + newTitleId + " WHERE id ="+entryId;
			Statement st = conn.createStatement();
			st.executeUpdate(query);
			st.close();        
			conn.close();
		} catch (Exception e) {
			System.err.println("Database Connection Error ! ENTRY TABLE");
			System.err.println(e.getMessage());
		}
	}
	
	@Override
	public List<Entry> getAllEntriesOrderByDate() {
		try {			
			String tableName = "entry";
			Class.forName(myDriver);
			Connection conn = DriverManager.getConnection(db, username, pass);
			String query = "SELECT * FROM " +tableName+ " ORDER BY date ASC";
			Statement st = conn.createStatement();        
			ResultSet rs = st.executeQuery(query); 
			List<Entry> entryList = new ArrayList<Entry>();
			while (rs.next()) {
				Entry currentEntry = new Entry();
				int s_id = rs.getInt("ID");
				currentEntry.setId(s_id);
				String s_description = rs.getString("description");
				currentEntry.setDescription(s_description);
				String s_date = rs.getString("date");
				currentEntry.setDate(s_date);
				String s_Link = rs.getString("entry_link");
				currentEntry.setEntryLink(s_Link);
				entryList.add(currentEntry);
			}
			st.close();
			conn.close();
			return entryList;
			
		} catch (Exception e) {
			System.err.println("Database Connection Error ! ENTRY TABLE");
			System.err.println(e.getMessage());
			return null;
		}
	}
	
	@Override
	public List<Entry> getAllEntriesOrderByDateWithLimit(int limit) {
		try {			
			String tableName = "entry";
			Class.forName(myDriver);
			Connection conn = DriverManager.getConnection(db, username, pass);
			String query = "SELECT * FROM " +tableName+ " ORDER BY date ASC LIMIT " +limit;
			Statement st = conn.createStatement();        
			ResultSet rs = st.executeQuery(query); 
			List<Entry> entryList = new ArrayList<Entry>();
			while (rs.next()) {
				Entry currentEntry = new Entry();
				int s_id = rs.getInt("ID");
				currentEntry.setId(s_id);
				String s_description = rs.getString("description");
				currentEntry.setDescription(s_description);
				String s_date = rs.getString("date");
				currentEntry.setDate(s_date);
				String s_Link = rs.getString("entry_link");
				currentEntry.setEntryLink(s_Link);
				entryList.add(currentEntry);
			}
			st.close();
			conn.close();
			return entryList;
			
		} catch (Exception e) {
			System.err.println("Database Connection Error ! ENTRY TABLE");
			System.err.println(e.getMessage());
			return null;
		}
	}

	@Override
	public List<Entry> getAllEntriesWithOnlyForeignKeys() {
		try {
			String tableName = "entry";
			Class.forName(myDriver);
			Connection conn = DriverManager.getConnection(db, username, pass);
			String query = "SELECT * FROM " +tableName;
			Statement st = conn.createStatement();        
			ResultSet rs = st.executeQuery(query); 
			List<Entry> entryList = new ArrayList<Entry>();
			while (rs.next()) {
				Entry currentEntry = new Entry();
				int s_id = rs.getInt("ID");
				currentEntry.setId(s_id);
				String s_description = rs.getString("description");
				currentEntry.setDescription(s_description);
				String s_date = rs.getString("date");
				currentEntry.setDate(s_date);
				int fk_titleId = rs.getInt("fk_title_id");
				currentEntry.setTitleId(fk_titleId);
				int fk_userId = rs.getInt("fk_user_id");
				currentEntry.setUserId(fk_userId);
				entryList.add(currentEntry);
			}
			st.close();
			conn.close();
			return entryList;
			
		} catch (Exception e) {
			System.err.println("Database Connection Error ! ENTRY TABLE");
	        System.err.println(e.getMessage());        
	        return null;
		}
	}
	
	@Override
	public List<UserUserTitle> getSimilarUsersForTitles() {
		try {			
			Class.forName(myDriver);
			Connection conn = DriverManager.getConnection(db, username, pass);
			String query = "select count(distinct t.id) as ortaksayi, e1.fk_user_id as userId1, e2.fk_user_id as userId2 from title t"
					+ " join entry e1 on e1.fk_title_id=t.id"
					+ " join entry e2 on e2.fk_title_id=t.id"
					+ " where e1.fk_user_id != e2.fk_user_id and e1.id !=  e2.id"
					+ " group by e1.fk_user_id, e2.fk_user_id"
					+ " order by ortaksayi desc limit 80000;";
			Statement st = conn.createStatement();        
			ResultSet rs = st.executeQuery(query);
			List<UserUserTitle> writeableList = new ArrayList<UserUserTitle>();
			while (rs.next()) {
				int totalCount = rs.getInt("ortaksayi");
				int user1Id = rs.getInt("userId1");
				int user2Id = rs.getInt("userId2");
				UserUserTitle newUserUserTitle = new UserUserTitle();
				newUserUserTitle.setUser1Id(user1Id);
				newUserUserTitle.setUser2Id(user2Id);
				newUserUserTitle.setCountOfSimilarTitle(totalCount);
				writeableList.add(newUserUserTitle);
				
			}
			return writeableList;			
		} catch (Exception e) {
			System.err.println("Database Connection Error ! ENTRY TABLE");
	        System.err.println(e.getMessage());        
	        return null;
		}
	}
	
	@Override
	public List<UserTitle> getTitleCountOfUsers() {
		try {			
			Class.forName(myDriver);
			Connection conn = DriverManager.getConnection(db, username, pass);
			String query = "select distinct u1.nickname as nickname, count(distinct t1.id) as titlesayi from entry e1"
					+ " join title t1 on t1.id = e1.fk_title_id"
					+ " join user u1 ON u1.id = e1.fk_user_id"
					+ " group by u1.id"
					+ " order by titlesayi desc;";
			Statement st = conn.createStatement();        
			ResultSet rs = st.executeQuery(query);
			List<UserTitle> writeableList = new ArrayList<UserTitle>();
			while (rs.next()) {
				String username = rs.getString("nickname");
				int totalTitleCount = rs.getInt("titlesayi");
				UserTitle newUserTitle = new UserTitle();
				newUserTitle.setUsername(username);
				newUserTitle.setCountOfTitleThatWrote(totalTitleCount);
				writeableList.add(newUserTitle);
			}
			return writeableList;
		} catch (Exception e) {
			System.err.println("Database Connection Error ! ENTRY TABLE");
	        System.err.println(e.getMessage());        
	        return null;
		}
	}
	
	@Override
	public List<UserEntry> getUserEntryList(Set<Integer> userIdList) {
		try {			
			Class.forName(myDriver);
			Connection conn = DriverManager.getConnection(db, username, pass);
			String query = "SELECT u.id AS userId, u.nickname AS username, e.description AS description FROM entry e" + 
					" JOIN user u ON u.id = e.fk_user_id" + 
					" where u.id IN (";
			
			
			for (Integer ids : userIdList) {
				query += ids + ",";
			}
			
			query = query.substring(0, query.length()-1);
			
			query += ");";
			
			Statement st = conn.createStatement();        
			ResultSet rs = st.executeQuery(query);
			
			List<UserEntry> userEntryList = new ArrayList<UserEntry>();
			while (rs.next()) {
				UserEntry userEntry = new UserEntry();
				
				String username = rs.getString("username");
				int userId = rs.getInt("userId");
				String entryDescription = rs.getString("description");
				
				userEntry.setUserId(userId);
				userEntry.setUsername(username);
				userEntry.setEntryDescription(entryDescription);
				
				userEntryList.add(userEntry);
			}
			
			return userEntryList;
			
		} catch (Exception e) {
			System.err.println("Database Connection Error ! ENTRY TABLE");
	        System.err.println(e.getMessage());        
	        return null;
		}
	}
	
	@Override
	public List<TitleEntry> getEntriesByTitleIdList(List<Integer> splittedIdList) {
		try {			
			Class.forName(myDriver);
			Connection conn = DriverManager.getConnection(db, username, pass);
			String query = "SELECT t.id AS titleId, t.name AS titleName, e.description AS entryDescription FROM entry e" + 
					" JOIN title t ON t.id = e.fk_title_id" + 
					" WHERE t.id IN (";
			
			
			for (Integer ids : splittedIdList) {
				query += ids + ",";
			}
			
			query = query.substring(0, query.length()-1);
			
			query += ");";
			
			Statement st = conn.createStatement();        
			ResultSet rs = st.executeQuery(query);
			
			List<TitleEntry> titleEntryList = new ArrayList<TitleEntry>();
			while (rs.next()) {
				TitleEntry titleEntry = new TitleEntry();
				
				String titleName = rs.getString("titleName");
				
				int titleId = rs.getInt("titleId");
				
				String entryDescription = rs.getString("entryDescription");
				
				titleEntry.setTitleId(titleId);
				titleEntry.setTitleName(titleName);
				titleEntry.setEntryDescription(entryDescription);
				
				titleEntryList.add(titleEntry);
			}
			
			return titleEntryList;
			
		} catch (Exception e) {
			System.err.println("Database Connection Error ! ENTRY TABLE");
	        System.err.println(e.getMessage());        
	        return null;
		}
	}
	
	@Override
	public void saveToWrongWordTable(String origin, String correctValue) {
    	try {
			String tableName = "wrong_vocabs";
			Class.forName(myDriver);
			Connection conn = DriverManager.getConnection(db, username, pass);
//			String q1 = "SELECT MAX(ID) +1 AS id FROM " +tableName;
//			Statement st1 = conn.createStatement();	          
//	        ResultSet rs = st1.executeQuery(q1);
//	        int id = 0;
//	        while (rs.next()){
//	        	id = rs.getInt("id");
//	        }
//	        st1.close();
	        
			String query = "INSERT INTO "+tableName +" (origin,correctly)VALUES('";
			query += origin +"','" + correctValue + "');";
			Statement st = conn.createStatement();
			st.executeUpdate(query);
			st.close();        
			conn.close();
			
		} catch (Exception e) {
			System.err.println("Database Connection Error ! WRONG VOCABS TABLE");
	        System.err.println(e.getMessage()); 
		}
    }
	
	@Override
	public Map<String, String> getWrongCorrectWordMap() {
    	try {
   		
			String tableName = "wrong_vocabs";
			Class.forName(myDriver);
			Connection conn = DriverManager.getConnection(db, username, pass);
	        
			String query = "SELECT * FROM " +tableName;
			
			Statement st = conn.createStatement();        
			ResultSet rs = st.executeQuery(query);
			
			Map<String, String> map = new HashMap<String, String>();
			
			while (rs.next()) {
				String origin = rs.getString("origin");
				String correctly = rs.getString("correctly");
				
				if (! map.containsKey(origin)) {
					map.put(origin, correctly);
				}

			}
			st.close();
			conn.close();
			return map;
			
		} catch (Exception e) {
			System.err.println("Database Connection Error ! WRONG VOCABS TABLE");
	        System.err.println(e.getMessage()); 
	        return null;
		}
	}
		
}
