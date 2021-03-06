package dao.queries;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import dao.DAO;
import dao.mapping.MSISDNRowMapper;
import domain.models.MSISDN;

public class JdbcMSISDNDao {

	private DAO dao;

	public JdbcMSISDNDao(DAO dao) {
		this.dao = dao;
	}

	public JdbcTemplate getJdbcTemplate() {
		return dao.getJdbcTemplate();
	}

	public void saveOneMSISDN(MSISDN msisdn, String tableName) {
		try{			
			if(msisdn.getId() == 0) {
				getJdbcTemplate().update("INSERT INTO " + tableName + " (MSISDN) VALUES('" + msisdn.getValue() + "')");			
			}
			else if(msisdn.getId() > 0) {
				getJdbcTemplate().update("UPDATE " + tableName + " SET MSISDN = '" + msisdn.getValue() + "' WHERE ID = " + msisdn.getId());
			}

		} catch(EmptyResultDataAccessException emptyEx) {

		} catch(Throwable th) {

		}
	}

	public MSISDN getOneMSISDN(int id, String tableName) {
		List<MSISDN> staffs = getJdbcTemplate().query("SELECT ID,MSISDN FROM " + tableName + " WHERE ID = " + id, new MSISDNRowMapper());
		return staffs.isEmpty() ? null : staffs.get(0);
	}

	public MSISDN getOneMSISDN(String msisdn, String tableName) {
		List<MSISDN> staffs = getJdbcTemplate().query("SELECT ID,MSISDN FROM " + tableName + " WHERE ((BIRTH_DATE = '" + (new SimpleDateFormat("dd-MMM-yy", Locale.ENGLISH)).format(new Date()) + "') AND (MSISDN = '" + msisdn + "'))", new MSISDNRowMapper());
		return staffs.isEmpty() ? null : staffs.get(0);
	}

	public void deleteOneMSISDN(int id, String tableName) {
		getJdbcTemplate().update("DELETE FROM " + tableName + " WHERE ID = " + id);
	}

	public void deleteOneMSISDN(String msisdn, String tableName) {
		getJdbcTemplate().update("DELETE FROM " + tableName + " WHERE ((BIRTH_DATE = '" + (new SimpleDateFormat("dd-MMM-yy", Locale.ENGLISH)).format(new Date()) + "') AND (MSISDN = '" + msisdn + "'))");
	}

}
