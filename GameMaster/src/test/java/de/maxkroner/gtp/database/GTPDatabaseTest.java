package de.maxkroner.gtp.database;

import org.junit.Test;

import de.maxkroner.gtp.database.GTPDatabase;
import de.maxkroner.values.Values;

import static org.junit.Assert.fail;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;


public class GTPDatabaseTest {
	private GTPDatabase db = new GTPDatabase("gmgtptest");
	
	@Test
	public void createDatabaseTables() throws SQLException{
		//GIVEN
		db.resetDatabase();

		//THEN
		assertThat(db.getRowCount("SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'IMAGES'"), is(1));
		assertThat(db.getRowCount("SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'WORDS'"), is(1));
		assertThat(db.getRowCount("SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'LISTS'"), is(1));
		assertThat(db.getRowCount("SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'LISTNESTING'"), is(1));
		assertThat(db.getRowCount("SELECT COUNT(*) FROM images"), is(0));
		assertThat(db.getRowCount("SELECT COUNT(*) FROM words"), is(0));
		assertThat(db.getRowCount("SELECT COUNT(*) FROM lists"), is(0));
		assertThat(db.getRowCount("SELECT COUNT(*) FROM listnesting"), is(0));
	}
	
	@Test
	public void addList() throws SQLException{
		//GIVEN
		db.resetDatabase();
		
		//WHEN
		long list_id_1 = db.addList("list_1", "german", "description1", 1);
		long list_id_2 = db.addList("list_2", "english", "description2", 2);
		ResultSet rs = db.getResultSetFromQuery("SELECT * FROM lists");
		
		//THEN
		rs.next();
		assertThat(rs.getLong(rs.findColumn("LIST_ID")), is(list_id_1));
		rs.next();
		assertThat(rs.getLong(rs.findColumn("LIST_ID")), is(list_id_2));
		assertThat(rs.getString(rs.findColumn("NAME")), is("list_2"));
		assertThat(rs.getString(rs.findColumn("LANGUAGE")), is("english"));
		assertThat(rs.getString(rs.findColumn("DESCRIPTION")), is("description2"));
		assertThat(rs.getInt(rs.findColumn("GUILD_ID")), is(2));
		assertThat(rs.next(), is(false));
	}
	
	@Test
	public void addWordWithUrlsToList() throws SQLException{
		//GIVEN
		db.resetDatabase();
		
		//WHEN
		long list_id_1 = db.addList("list_1", "german", "description1", 1);
		long list_id_2 = db.addList("list_2", "english", "description2", 1);
		ArrayList<String> urls = new ArrayList<String>();
		urls.add("url1");
		urls.add("url2");
		String word1 = "word1";
		String word2 = "word2";
		
		
		long word_id_1= db.addWordWithUrlsToList(1, "list_1", word1, urls);
		urls.add("url3");
		long word_id_2 = db.addWordWithUrlsToList(1, "list_2", word2, urls);
		
		ResultSet rs_words = db.getResultSetFromQuery("SELECT * FROM words");
		ResultSet rs_1 = db.getResultSetFromQuery("SELECT * FROM images WHERE word_id=" + word_id_1);
		ResultSet rs_2 = db.getResultSetFromQuery("SELECT * FROM images WHERE word_id=" + word_id_2);
		
		//THEN
		rs_words.next();
		assertThat(rs_words.getLong(rs_words.findColumn("WORD_ID")), is(word_id_1));
		assertThat(rs_words.getString(rs_words.findColumn("NAME")), is("word1"));
		assertThat(rs_words.getLong(rs_words.findColumn("LIST_ID")), is(list_id_1));
		
		rs_words.next();
		assertThat(rs_words.getLong(rs_words.findColumn("WORD_ID")), is(word_id_2));
		assertThat(rs_words.getString(rs_words.findColumn("NAME")), is("word2"));
		assertThat(rs_words.getLong(rs_words.findColumn("LIST_ID")), is(list_id_2));
		
		assertThat(rs_words.next(), is(false));
		
		rs_1.next();
		assertThat(rs_1.getString(rs_1.findColumn("URL")), is("url1"));
		rs_1.next();
		assertThat(rs_1.getString(rs_1.findColumn("URL")), is("url2"));
		assertThat(rs_1.next(), is(false));
		rs_2.next();
		assertThat(rs_2.getString(rs_1.findColumn("URL")), is("url1"));
		rs_2.next();
		assertThat(rs_2.getString(rs_1.findColumn("URL")), is("url2"));
		rs_2.next();
		assertThat(rs_2.getString(rs_1.findColumn("URL")), is("url3"));
		assertThat(rs_2.next(), is(false));
	}
	
	@Test
	public void listNestingIntegrityCheck(){
		//GIVEN
		db.resetDatabase();
		
		//WHEN	
		try {
			long list_id_1 = db.addList("list_1", "german", "description", 1);
			long list_id_2 = db.addList("list_2", "german", "description", 1);
			db.addListNesting(list_id_1, list_id_2);
			db.addListNesting(list_id_2, list_id_1);
			fail("SQLException expected!");
		} catch (SQLException e) {
		    assertEquals(Values.MESSAGE_SQL_ERROR_NESTING, e.getCause().getMessage());
		}	
	}
	
	
	

}
