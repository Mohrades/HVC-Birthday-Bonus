package jobs;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import connexions.AIRRequest;
import dao.DAO;
import dao.JdbcOperations.HVCDAOJdbc;
import dao.domain.model.HVC;
import product.ProductProperties;
import util.AccountDetails;

@Component("importHVCTasklet")
public class ImportHVCTasklet implements Tasklet {

	@Autowired
	private DAO dao;

	@Autowired
	private ProductProperties productProperties;

	@Override
	public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) {
		// TODO Auto-generated method stub

		try {
			// set today HVC
			Connection connexion = null;
			PreparedStatement ps = null;
			ResultSet rs = null;
			
			HashSet <HVC> allMSISDN_Today_Is_BIRTHDATE = new HashSet <HVC>();

			try {
				Class.forName("oracle.jdbc.driver.OracleDriver"); // chargement du pilote JDBC
				connexion = DriverManager.getConnection("jdbc:oracle:thin:@irmdb01.mtn.bj:1521:ISLDB1", "aplimanuser", "CI1617o#L#20"); // ouverture connexion
				connexion.setAutoCommit(false); // début transaction
				connexion.setReadOnly(true); // en mode lecture seule

				// on lit la table MTNB.CRM_CUSTOMER_DATA [MSISDN, LASTNAME, FIRSTNAME, BIRTHDATE, PREFERREDLANGUAGE]
				// ps = connexion.prepareStatement("SELECT MSISDN,LASTNAME,FIRSTNAME,BIRTHDATE,PREFERREDLANGUAGE FROM MTNB.CRM_CUSTOMER_DATA WHERE (TO_CHAR(SYSDATE, 'MMDD') = TO_CHAR(TO_DATE(BIRTHDATE,'DY MON DD HH24:MI:SS YYYY'), 'MMDD'))");
				// ps = connexion.prepareStatement("SELECT COUNT(*) FROM MTNB.CRM_CUSTOMER_DATA Aa WHERE ((TO_CHAR(SYSDATE,'DDMM')= TO_CHAR(TO_DATE(BIRTHDATE,'DY MON DD HH24:MI:SS YYYY'),'DDMM')) AND (Aa.SYS_CREATED_DATE_TIME >= ALL (SELECT B.SYS_CREATED_DATE_TIME FROM MTNB.CRM_CUSTOMER_DATA B WHERE B.MSISDN = Aa.MSISDN)) AND (ROWNUM < 100000))");
				// ps = connexion.prepareStatement("SELECT * FROM MTNB.CRM_CUSTOMER_DATA Aa WHERE ((TO_CHAR(SYSDATE,'DDMM')= TO_CHAR(TO_DATE(BIRTHDATE,'DY MON DD HH24:MI:SS YYYY'),'DDMM')) AND (Aa.SYS_CREATED_DATE_TIME >= ALL (SELECT B.SYS_CREATED_DATE_TIME FROM MTNB.CRM_CUSTOMER_DATA B WHERE B.MSISDN = Aa.MSISDN)) AND (ROWNUM < 100000))");

				// Anciens propriétaires d'un MSISDN
				// ps = connexion.prepareStatement("SELECT MSISDN,LASTNAME,FIRSTNAME,BIRTHDATE,PREFERREDLANGUAGE FROM MTNB.CRM_CUSTOMER_DATA Aa WHERE ((TO_CHAR(SYSDATE,'DDMM')= TO_CHAR(TO_DATE(BIRTHDATE,'DY MON DD HH24:MI:SS YYYY'),'DDMM')) AND (Aa.SYS_CREATED_DATE_TIME < ANY (SELECT B.SYS_CREATED_DATE_TIME FROM MTNB.CRM_CUSTOMER_DATA B WHERE B.MSISDN = Aa.MSISDN)))");
				// La mise à jour la plus récente !!
				ps = connexion.prepareStatement("SELECT MSISDN,LASTNAME,FIRSTNAME,TO_DATE(BIRTHDATE,'DY MON DD HH24:MI:SS YYYY') BIRTH_DATE,PREFERREDLANGUAGE FROM MTNB.CRM_CUSTOMER_DATA Aa WHERE ((TO_CHAR(SYSDATE,'DDMM')= TO_CHAR(TO_DATE(BIRTHDATE,'DY MON DD HH24:MI:SS YYYY'),'DDMM')) AND (Aa.SYS_CREATED_DATE_TIME >= ALL (SELECT B.SYS_CREATED_DATE_TIME FROM MTNB.CRM_CUSTOMER_DATA B WHERE B.MSISDN = Aa.MSISDN)))");

				rs = ps.executeQuery();
				// Liste des elements
				while (rs.next()) {
					String msisdn = rs.getString("MSISDN").trim();
					String PREFERREDLANGUAGE = rs.getString("PREFERREDLANGUAGE").trim();
					int language = PREFERREDLANGUAGE.equalsIgnoreCase("fr") ? 1 : PREFERREDLANGUAGE.equalsIgnoreCase("en") ? 2 : 1; // PREFERREDLANGUAGE = fr,en
					allMSISDN_Today_Is_BIRTHDATE.add(new HVC(0, (msisdn.length() == productProperties.getMsisdn_length()) ? productProperties.getMcc() + msisdn : msisdn, rs.getString("FIRSTNAME").trim() + " " + rs.getString("LASTNAME").trim(), 0, language, rs.getDate("BIRTH_DATE")));
				}

				connexion.commit(); // commit transaction

			} catch (ClassNotFoundException|SQLException ex) {
				// on traite l'exception

			} finally {
				// fermer la connexion
				if (connexion != null) {
					try {
						connexion.close();

					} catch (SQLException ex) {
						// traiter l'exception
					}
				}
			}

			HashSet <HVC> allHVC = new HashSet <HVC>();

			try {
				Class.forName("oracle.jdbc.driver.OracleDriver"); // chargement du pilote JDBC
				connexion = DriverManager.getConnection("jdbc:oracle:thin:@ga-exa-scan.mtn.bj:1521/vmdg", "abutu", "kT60#bTh03#18"); // ouverture connexion
				connexion.setAutoCommit(false); // début transaction
				connexion.setReadOnly(true); // en mode lecture seule

				// on lit la table PRICEPLAN.VALUE_BAND_LIST [MSISDN, CUSTOMER_SEGMENT]
				// ps = connexion.prepareStatement("SELECT MSISDN,CUSTOMER_SEGMENT FROM PRICEPLAN.VALUE_BAND_LIST WHERE (((CUSTOMER_SEGMENT = 'GOLD_P100') OR (CUSTOMER_SEGMENT = 'PREMIUM_P100') OR (CUSTOMER_SEGMENT = 'PLATINUM_P100') OR (CUSTOMER_SEGMENT = 'DIAMOND_P100')) AND (ROWNUM < 10000))");
				ps = connexion.prepareStatement("SELECT MSISDN,CUSTOMER_SEGMENT FROM PRICEPLAN.VALUE_BAND_LIST WHERE ((CUSTOMER_SEGMENT = 'GOLD_P100') OR (CUSTOMER_SEGMENT = 'PREMIUM_P100') OR (CUSTOMER_SEGMENT = 'PLATINUM_P100') OR (CUSTOMER_SEGMENT = 'DIAMOND_P100'))");
				rs = ps.executeQuery();
				// Liste des elements
				while (rs.next()) {
					String msisdn = rs.getString("MSISDN").trim();
					String CUSTOMER_SEGMENT = rs.getString("CUSTOMER_SEGMENT").trim();
					allHVC.add(new HVC(0, (msisdn.length() == productProperties.getMsisdn_length()) ? productProperties.getMcc() + msisdn : msisdn, null, CUSTOMER_SEGMENT.equalsIgnoreCase("BRONZE_P100") ? 1 : CUSTOMER_SEGMENT.equalsIgnoreCase("SILVER_P100") ? 2 : CUSTOMER_SEGMENT.equalsIgnoreCase("GOLD_P100") ? 3 : CUSTOMER_SEGMENT.equalsIgnoreCase("PREMIUM_P100") ? 4 : CUSTOMER_SEGMENT.equalsIgnoreCase("PLATINUM_P100") ? 5 : CUSTOMER_SEGMENT.equalsIgnoreCase("DIAMOND_P100") ? 6 : CUSTOMER_SEGMENT.equalsIgnoreCase("IVOIRE_P100") ? 7 : CUSTOMER_SEGMENT.equalsIgnoreCase("GOLD_P100") ? 8 : 0, 0, null));
				}

				connexion.commit(); // commit transaction

			} catch (ClassNotFoundException|SQLException ex) {
				// on traite l'exception

			} finally {
				// fermer la connexion
				if (connexion != null) {
					try {
						connexion.close();

					} catch (SQLException ex) {
						// traiter l'exception
					}
				}
			}

			// croiser today_is_birthday and segment
			allMSISDN_Today_Is_BIRTHDATE.retainAll(allHVC);

			for(HVC hvc : allMSISDN_Today_Is_BIRTHDATE) {
				try {
					AccountDetails accountDetails = new AIRRequest().getAccountDetails(hvc.getValue());
					if(accountDetails != null) hvc.setLanguage(accountDetails.getLanguageIDCurrent());
					// store hvc
					new HVCDAOJdbc(dao).saveOneHVC(hvc);

				} catch(Throwable th) {

				}
			}

			stepContribution.setExitStatus(ExitStatus.COMPLETED);
			return RepeatStatus.FINISHED;

		} catch(Throwable th) {

		}

		return null;
	}

}
