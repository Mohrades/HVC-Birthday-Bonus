package dao.JdbcOperations;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import dao.DAO;
import dao.domain.model.HVC;
import dao.domain.model.HVCRowMapper;

public class HVCDAOJdbc {

	private DAO dao;

	public HVCDAOJdbc(DAO dao) {
		this.dao = dao;
	}

	public JdbcTemplate getJdbcTemplate() {
		return dao.getJdbcTemplate();
	}

	public int saveOneHVC(HVC hvc) {
		try {
			if(hvc.getId() == 0) {
				// Tout autre caract�re suivi d'un antislash est pris litt�ralement. Du coup, pour inclure un caract�re antislash, �crivez deux antislashs (\\).
				// De plus, un guillemet simple peut �tre inclus dans une cha�ne d'�chappement en �crivant \', en plus de la fa�on normale ''.
				getJdbcTemplate().update("INSERT INTO HVC_BIRTHDAY_BONUS_MSISDN_EBA (MSISDN,NAME,SEGMENT,LANGUAGE,BIRTH_DATE) VALUES('" + hvc.getValue() + "','" + hvc.getName().replace("'", "''") + "'," + hvc.getSegment() + "," + hvc.getLanguage() + ",'" + (new SimpleDateFormat("dd-MMM-yy")).format(new Date()) + "')");
				// getJdbcTemplate().update("INSERT INTO HVC_BIRTHDAY_BONUS_MSISDN_EBA (MSISDN,NAME,SEGMENT,LANGUAGE,BIRTH_DATE) VALUES('" + hvc.getValue() + "','" + hvc.getName().replace("'", "\'") + "'," + hvc.getSegment() + "," + hvc.getLanguage() + ",'" + (new SimpleDateFormat("dd-MMM-yy")).format(new Date()) + "')");
			}
			else if(hvc.getId() > 0) {
				return getJdbcTemplate().update("UPDATE HVC_BIRTHDAY_BONUS_MSISDN_EBA SET BONUS = " + hvc.getBonus() + ", BONUS_EXPIRES_IN = TIMESTAMP '" + (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(hvc.getBonus_expires_in()) + "', LAST_UPDATE_TIME = TIMESTAMP '" + (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(new Date()) + "' WHERE ((ID = " + hvc.getId() + ") AND (BONUS = NULL))");
			}

		} catch(EmptyResultDataAccessException emptyEx) {

		} catch(Throwable th) {

		}

		return 0;
	}

	public HVC getOneHVC(int id) {
		List<HVC> hvcs = getJdbcTemplate().query("SELECT ID,MSISDN,NAME,SEGMENT,LANGUAGE,BIRTH_DATE,BONUS,BONUS_EXPIRES_IN,LAST_UPDATE_TIME FROM HVC_BIRTHDAY_BONUS_MSISDN_EBA WHERE ID = " + id, new HVCRowMapper());
		return hvcs.isEmpty() ? null : hvcs.get(0);
	}

	@SuppressWarnings("deprecation")
	public HVC getOneHVC(String msisdn, int expires) {
		List<HVC> hvcs = null;

		if(expires == 0) {
			hvcs = getJdbcTemplate().query("SELECT ID,MSISDN,NAME,SEGMENT,LANGUAGE,BIRTH_DATE,BONUS,BONUS_EXPIRES_IN,LAST_UPDATE_TIME FROM HVC_BIRTHDAY_BONUS_MSISDN_EBA WHERE ((BIRTH_DATE = '" + (new SimpleDateFormat("dd-MMM-yy")).format(new Date()) + "') AND (MSISDN = '" + msisdn + "'))", new HVCRowMapper());
		}
		else {
			Date now = new Date();
			Date yesterday = new Date();
			yesterday.setDate(yesterday.getDate() - 1);

			hvcs = getJdbcTemplate().query("SELECT ID,MSISDN,NAME,SEGMENT,LANGUAGE,BIRTH_DATE,BONUS,BONUS_EXPIRES_IN,LAST_UPDATE_TIME FROM HVC_BIRTHDAY_BONUS_MSISDN_EBA WHERE (((BIRTH_DATE = '" + (new SimpleDateFormat("dd-MMM-yy")).format(yesterday) + "') AND (MSISDN = '" + msisdn + "')) OR ((BIRTH_DATE = '" + (new SimpleDateFormat("dd-MMM-yy")).format(now) + "') AND (MSISDN = '" + msisdn + "'))) ORDER BY BIRTH_DATE DESC", new HVCRowMapper());
		}

		return hvcs.isEmpty() ? null : hvcs.get(0);
	}

	public void deleteOneHVC(int id) {
		getJdbcTemplate().update("DELETE FROM HVC_BIRTHDAY_BONUS_MSISDN_EBA WHERE ID = " + id);
	}

	public void deleteOneHVC(String msisdn) {
		getJdbcTemplate().update("DELETE FROM HVC_BIRTHDAY_BONUS_MSISDN_EBA WHERE ((BIRTH_DATE = '" + (new SimpleDateFormat("dd-MMM-yy")).format(new Date()) + "') AND (MSISDN = '" + msisdn + "'))");
	}
}
