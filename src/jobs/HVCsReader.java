package jobs;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.batch.item.database.JdbcCursorItemReader;

@SuppressWarnings("rawtypes")
public class HVCsReader extends JdbcCursorItemReader {

	public HVCsReader(int type) {
		if(type == 0) {
			setSql("SELECT ID,MSISDN,NAME,SEGMENT,LANGUAGE,BIRTH_DATE,BONUS,BONUS_EXPIRES_IN,LAST_UPDATE_TIME FROM HVC_BIRTHDAY_BONUS_MSISDN_EBA WHERE (BIRTH_DATE = '" + (new SimpleDateFormat("dd-MMM-yy")).format(new Date()) + "')");
		}
		else if(type == 1) {
			setSql("SELECT ID,MSISDN,NAME,SEGMENT,LANGUAGE,BIRTH_DATE,BONUS,BONUS_EXPIRES_IN,LAST_UPDATE_TIME FROM HVC_BIRTHDAY_BONUS_MSISDN_EBA WHERE ((BIRTH_DATE = '" + (new SimpleDateFormat("dd-MMM-yy")).format(new Date()) + "') AND (BONUS IS NOT NULL))");
		}
		else if(type == 2) {
			setSql("SELECT ID,MSISDN,NAME,SEGMENT,LANGUAGE,BIRTH_DATE,BONUS,BONUS_EXPIRES_IN,LAST_UPDATE_TIME FROM HVC_BIRTHDAY_BONUS_MSISDN_EBA WHERE ((BIRTH_DATE = '" + (new SimpleDateFormat("dd-MMM-yy")).format(new Date()) + "') AND (BONUS IS NULL) AND (LOCKED = 0))");
		}
	}

}
