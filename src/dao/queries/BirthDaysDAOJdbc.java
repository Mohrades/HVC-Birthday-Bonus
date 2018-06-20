package dao.queries;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import dao.DAO;

public class BirthDaysDAOJdbc {

	private DAO dao;

	public BirthDaysDAOJdbc(DAO dao) {
		this.dao = dao;
	}

	public JdbcTemplate getJdbcTemplate() {
		return dao.getJdbcTemplate();
	}

	public Map<String, Object> getOneReportedBirthDay() {
		try {
			return getJdbcTemplate().queryForMap("SELECT BIRTH_DATE FROM HVC_BIRTHDAY_BONUS_DATES_EBA WHERE (REPORTED_TO = '" + (new SimpleDateFormat("dd-MMM-yy")).format(new Date()) + "')");

		} catch(EmptyResultDataAccessException empty) {
			
		} catch(Exception ex) {
			
		} catch(Throwable th) {
			
		}

		return new HashMap<String, Object>();
		
	}

	public List<Map<String, Object>> getAllReportedBirthDays() {
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
			Map<String, Object> result = getJdbcTemplate().queryForMap("SELECT ID,BIRTH_DATE,REPORTED_TO FROM HVC_BIRTHDAY_BONUS_DATES_EBA WHERE ((BIRTH_DATE = '" + (new SimpleDateFormat("dd-MMM-yy")).format(new Date()) + "') AND ((REPORTED_TO IS NULL) OR (REPORTED_TO != BIRTH_DATE)))");
			return result.isEmpty() ? false : true;

		} catch(EmptyResultDataAccessException empty) {
			
		} catch(Exception ex) {
			
		} catch(Throwable th) {
			
		}

		return false;
	}

}
