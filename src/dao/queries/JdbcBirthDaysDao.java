package dao.queries;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import dao.DAO;

public class JdbcBirthDaysDao {

	private DAO dao;

	public JdbcBirthDaysDao(DAO dao) {
		this.dao = dao;
	}

	public JdbcTemplate getJdbcTemplate() {
		return dao.getJdbcTemplate();
	}

	public List<Map<String,Object>> getAllReportedBirthDays() {
		try {
			return getJdbcTemplate().queryForList("SELECT BIRTH_DATE FROM HVC_BIRTHDAY_BONUS_DATES_EBA WHERE (REPORTED_TO = '" + (new SimpleDateFormat("dd-MMM-yy")).format(new Date()) + "')");

		} catch(EmptyResultDataAccessException empty) {
			
		} catch(Exception ex) {
			
		} catch(Throwable th) {
			
		}

		return new LinkedList<Map<String, Object>>();
	}

	public boolean isBirthDayReported() {
		try {
			List<Map<String, Object>> result = getJdbcTemplate().queryForList("SELECT BIRTH_DATE FROM HVC_BIRTHDAY_BONUS_DATES_EBA Aa WHERE (Aa.BIRTH_DATE = TO_DATE('" + (new SimpleDateFormat("dd-MMM-yy")).format(new Date()) + "', 'DD-MON-YY')) AND ((Aa.REPORTED_TO IS NULL) OR (Aa.REPORTED_TO != Aa.BIRTH_DATE))");
			return result.isEmpty() ? false : true ;

		} catch(EmptyResultDataAccessException empty) {
			
		} catch(Exception ex) {
			
		} catch(Throwable th) {
			
		}

		return true;
	}

}
