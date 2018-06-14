package dao.queries;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;

import dao.DAO;

public class BIRTHDAY_REPORT_TO_DATE_DAOJdbc {

	private DAO dao;

	public BIRTHDAY_REPORT_TO_DATE_DAOJdbc(DAO dao) {
		this.dao = dao;
	}

	public JdbcTemplate getJdbcTemplate() {
		return dao.getJdbcTemplate();
	}

	public Map<String, Object> getOneReportedBirthDay() {
		return getJdbcTemplate().queryForMap("SELECT BIRTH_DATE FROM HVC_BIRTHDAY_BONUS_DATES_EBA WHERE (REPORTED_TO = '" + (new SimpleDateFormat("dd-MMM-yy")).format(new Date()) + "')");
	}

	public List<Map<String, Object>> getAllReportedBirthDays() {
		return getJdbcTemplate().queryForList("SELECT BIRTH_DATE FROM HVC_BIRTHDAY_BONUS_DATES_EBA WHERE (REPORTED_TO = '" + (new SimpleDateFormat("dd-MMM-yy")).format(new Date()) + "')");
	}

	public boolean isBirthDayReported() {
		Map<String, Object> result = getJdbcTemplate().queryForMap("SELECT ID,BIRTH_DATE,REPORTED_TO FROM HVC_BIRTHDAY_BONUS_DATES_EBA WHERE (BIRTH_DATE = '" + (new SimpleDateFormat("dd-MMM-yy")).format(new Date()) + "')");

		return result.isEmpty() ? false : true;
	}
}
