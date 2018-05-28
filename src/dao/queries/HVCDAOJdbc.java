package dao.queries;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import dao.DAO;
import dao.mapping.HVCRowMapper;
import domain.models.HVC;

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
				// Tout autre caractère suivi d'un antislash est pris littéralement. Du coup, pour inclure un caractère antislash, écrivez deux antislashs (\\).
				// De plus, un guillemet simple peut être inclus dans une chaîne d'échappement en écrivant \', en plus de la façon normale ''.
				getJdbcTemplate().update("INSERT INTO HVC_BIRTHDAY_BONUS_MSISDN_EBA (MSISDN,NAME,SEGMENT,LANGUAGE,BIRTH_DATE) VALUES('" + hvc.getValue() + "','" + hvc.getName().replace("'", "''") + "'," + hvc.getSegment() + "," + hvc.getLanguage() + ",'" + (new SimpleDateFormat("dd-MMM-yy")).format(new Date()) + "')");
				// getJdbcTemplate().update("INSERT INTO HVC_BIRTHDAY_BONUS_MSISDN_EBA (MSISDN,NAME,SEGMENT,LANGUAGE,BIRTH_DATE) VALUES('" + hvc.getValue() + "','" + hvc.getName().replace("'", "\'") + "'," + hvc.getSegment() + "," + hvc.getLanguage() + ",'" + (new SimpleDateFormat("dd-MMM-yy")).format(new Date()) + "')");
			}
			else if(hvc.getId() > 0) {
				if((hvc.getBonus() > 0) && (hvc.getBonus_expires_in() != null)) {
					return getJdbcTemplate().update("UPDATE HVC_BIRTHDAY_BONUS_MSISDN_EBA SET BONUS = " + hvc.getBonus() + ", BONUS_EXPIRES_IN = TIMESTAMP '" + (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(hvc.getBonus_expires_in()) + "', LAST_UPDATE_TIME = TIMESTAMP '" + (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(new Date()) + "' WHERE ((ID = " + hvc.getId() + ") AND (BONUS IS NULL))");
				}
				else return -1;
			}
			else {
				return -1;
			}

		} catch(EmptyResultDataAccessException emptyEx) {
			return -1;

		} catch(Throwable th) {
			return -1;
		}

		return 0;
	}
	
	public int locking(HVC hvc, boolean locked) {
		try {
			if(locked) {
				return getJdbcTemplate().update("UPDATE HVC_BIRTHDAY_BONUS_MSISDN_EBA SET LOCKED = 1 WHERE ((ID = " + hvc.getId() + ") AND (BONUS IS NULL) AND (LOCKED = 0))");
			}
			else {
				return getJdbcTemplate().update("UPDATE HVC_BIRTHDAY_BONUS_MSISDN_EBA SET LOCKED = 0 WHERE (ID = " + hvc.getId() + ")");
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
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yy");
		Date now = new Date();

		if(expires == 0) {
			hvcs = getJdbcTemplate().query("SELECT ID,MSISDN,NAME,SEGMENT,LANGUAGE,BIRTH_DATE,BONUS,BONUS_EXPIRES_IN,LAST_UPDATE_TIME FROM HVC_BIRTHDAY_BONUS_MSISDN_EBA WHERE ((BIRTH_DATE = '" + dateFormat.format(now) + "') AND (MSISDN = '" + msisdn + "'))", new HVCRowMapper());
		}
		else {
			Date yesterday = new Date();
			yesterday.setDate(yesterday.getDate() - 1);

			hvcs = getJdbcTemplate().query("SELECT ID,MSISDN,NAME,SEGMENT,LANGUAGE,BIRTH_DATE,BONUS,BONUS_EXPIRES_IN,LAST_UPDATE_TIME FROM HVC_BIRTHDAY_BONUS_MSISDN_EBA WHERE (((BIRTH_DATE = '" + dateFormat.format(yesterday) + "') AND (MSISDN = '" + msisdn + "')) OR ((BIRTH_DATE = '" + dateFormat.format(now) + "') AND (MSISDN = '" + msisdn + "'))) ORDER BY BIRTH_DATE DESC", new HVCRowMapper());
		}

		if( hvcs.isEmpty()) return null;
		else {
			// (id = 0) ==> HVC in 2 days consecutive
			// (id > 0) ==> HVC birth date is today
			// (id < 0) ==> HVC birth date is yesterday

			HVC hvc = hvcs.get(0);

			if(hvcs.size() == 1) {
				if(dateFormat.format(now).equals(dateFormat.format(hvc.getBirth_date())));
				else hvc.setId(-hvc.getId());
			}
			else hvc.setId(0);

			return hvc;
		}
	}

	public void deleteOneHVC(int id) {
		getJdbcTemplate().update("DELETE FROM HVC_BIRTHDAY_BONUS_MSISDN_EBA WHERE ID = " + id);
	}

	public void deleteOneHVC(String msisdn) {
		getJdbcTemplate().update("DELETE FROM HVC_BIRTHDAY_BONUS_MSISDN_EBA WHERE ((BIRTH_DATE = '" + (new SimpleDateFormat("dd-MMM-yy")).format(new Date()) + "') AND (MSISDN = '" + msisdn + "'))");
	}
}
