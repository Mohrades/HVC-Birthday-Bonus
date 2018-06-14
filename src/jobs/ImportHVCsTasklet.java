package jobs;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import connexions.AIRRequest;
import dao.DAO;
import dao.queries.BIRTHDAY_REPORT_TO_DATE_DAOJdbc;
import dao.queries.HVCDAOJdbc;
import dao.queries.USSDServiceDAOJdbc;
import domain.models.HVC;
import domain.models.USSDService;
import product.ProductProperties;
import util.AccountDetails;

@Component("importHVCsTasklet")
public class ImportHVCsTasklet implements Tasklet {

	@Autowired
	private DAO dao;

	@Autowired
	private ProductProperties productProperties;

	@Override
	public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) {
		// TODO Auto-generated method stub

		try {
			USSDService service = new USSDServiceDAOJdbc(dao).getOneUSSDService(productProperties.getSc());
			Date now = new Date();

			/*The first way to stop execution is to throw an exception. This works all the time, unless you configured the job to skip some exceptions in a chunk-oriented step!*/
			// Stopping a job from a tasklet : Setting the stop flag in a tasklet is straightforward;
			if((service == null) || (((service.getStart_date() != null) && (now.before(service.getStart_date()))) || ((service.getStop_date() != null) && (now.after(service.getStop_date()))))) {
				// Sets stop flag
				chunkContext.getStepContext().getStepExecution().setTerminateOnly();
				stepContribution.setExitStatus(new ExitStatus("STOPPED", "Job should not be run right now."));
			}
			/*else if((productProperties.getBirth_dates_excluded() != null) && productProperties.getBirth_dates_excluded().contains(((new SimpleDateFormat("dd-MMM")).format(new Date())).toUpperCase())) {
				// Sets stop flag
				chunkContext.getStepContext().getStepExecution().setTerminateOnly();
				stepContribution.setExitStatus(new ExitStatus("STOPPED", "Job should not be run right now."));
			}*/
			else if((new BIRTHDAY_REPORT_TO_DATE_DAOJdbc(dao)).isBirthDayReported()) {
				// Sets stop flag
				chunkContext.getStepContext().getStepExecution().setTerminateOnly();
				stepContribution.setExitStatus(new ExitStatus("STOPPED", "Job should not be run right now."));
			}
			else {
				// set today HVC
				Connection connexion = null;
				PreparedStatement ps = null;
				ResultSet rs = null;

				HashSet <HVC> allMSISDN_Today_Is_BIRTHDATE = new HashSet <HVC>();

				try {
					Class.forName("oracle.jdbc.driver.OracleDriver"); // chargement du pilote JDBC
					// connexion = DriverManager.getConnection("jdbc:oracle:thin:@irmdb01.mtn.bj:1521:ISLDB1", "aplimanuser", "CI1617o#L#20"); // ouverture connexion
					connexion = DriverManager.getConnection("jdbc:oracle:thin:@ga-exa-scan.mtn.bj:1521/isldg", "aplimanuser", "CI1617o#L#20"); // ouverture connexion
					connexion.setAutoCommit(false); // début transaction
					connexion.setReadOnly(true); // en mode lecture seule

					// on lit la table MTNB.CRM_CUSTOMER_DATA [MSISDN, LASTNAME, FIRSTNAME, BIRTHDATE, PREFERREDLANGUAGE]
					// ps = connexion.prepareStatement("SELECT MSISDN,LASTNAME,FIRSTNAME,BIRTHDATE,PREFERREDLANGUAGE FROM MTNB.CRM_CUSTOMER_DATA WHERE (TO_CHAR(SYSDATE, 'MMDD') = TO_CHAR(TO_DATE(BIRTHDATE,'DY MON DD HH24:MI:SS YYYY'), 'MMDD'))");
					// ps = connexion.prepareStatement("SELECT COUNT(*) FROM MTNB.CRM_CUSTOMER_DATA Aa WHERE ((TO_CHAR(SYSDATE,'DDMM')= TO_CHAR(TO_DATE(BIRTHDATE,'DY MON DD HH24:MI:SS YYYY'),'DDMM')) AND (Aa.SYS_CREATED_DATE_TIME >= ALL (SELECT B.SYS_CREATED_DATE_TIME FROM MTNB.CRM_CUSTOMER_DATA B WHERE B.MSISDN = Aa.MSISDN)) AND (ROWNUM < 100000))");
					// ps = connexion.prepareStatement("SELECT * FROM MTNB.CRM_CUSTOMER_DATA Aa WHERE ((TO_CHAR(SYSDATE,'DDMM')= TO_CHAR(TO_DATE(BIRTHDATE,'DY MON DD HH24:MI:SS YYYY'),'DDMM')) AND (Aa.SYS_CREATED_DATE_TIME >= ALL (SELECT B.SYS_CREATED_DATE_TIME FROM MTNB.CRM_CUSTOMER_DATA B WHERE B.MSISDN = Aa.MSISDN)) AND (ROWNUM < 100000))");

					// Anciens propriétaires d'un MSISDN
					// ps = connexion.prepareStatement("SELECT MSISDN,LASTNAME,FIRSTNAME,BIRTHDATE,PREFERREDLANGUAGE FROM MTNB.CRM_CUSTOMER_DATA Aa WHERE ((TO_CHAR(SYSDATE,'DDMM')= TO_CHAR(TO_DATE(BIRTHDATE,'DY MON DD HH24:MI:SS YYYY'),'DDMM')) AND (Aa.SYS_CREATED_DATE_TIME < ANY (SELECT B.SYS_CREATED_DATE_TIME FROM MTNB.CRM_CUSTOMER_DATA B WHERE B.MSISDN = Aa.MSISDN)))");
					// La mise à jour la plus récente !!
					List<Map<String, Object>>  reporteds = (new BIRTHDAY_REPORT_TO_DATE_DAOJdbc(dao)).getAllReportedBirthDays();

					if(reporteds.isEmpty()) {
						ps = connexion.prepareStatement("SELECT MSISDN,LASTNAME,FIRSTNAME,TO_DATE(BIRTHDATE,'DY MON DD HH24:MI:SS YYYY') BIRTH_DATE,PREFERREDLANGUAGE FROM MTNB.CRM_CUSTOMER_DATA Aa WHERE ((TO_CHAR(SYSDATE,'DDMM') = TO_CHAR(TO_DATE(BIRTHDATE,'DY MON DD HH24:MI:SS YYYY'),'DDMM')) AND (Aa.SYS_CREATED_DATE_TIME >= ALL (SELECT B.SYS_CREATED_DATE_TIME FROM MTNB.CRM_CUSTOMER_DATA B WHERE B.MSISDN = Aa.MSISDN)))");
					}
					else {
						SimpleDateFormat dateFormat = new SimpleDateFormat("ddMM");
						String birthdays = "'" + dateFormat.format(new Date()) + "'";

						for(Map<String, Object> reported : reporteds) {
							Set <Entry<String, Object> > entrees = reported.entrySet () ; // entrees est un ensemble de "paires"
							Iterator <Entry<String, Object> > iter = entrees.iterator() ; // itérateur sur les paires
							// boucle sur les paires
							while (iter.hasNext()) {
								Map.Entry <String, Object> entree = (Entry<String, Object>)iter.next() ;  // paire courante

								String cle   = entree.getKey () ; // clé de la paire courante
								if(cle.equals("BIRTH_DATE")) {
									if(entree.getValue() != null) {
										Date valeur = (Date) entree.getValue() ; // valeur de la paire courante
										birthdays += ", '" + dateFormat.format(valeur) + "'";
									}
								}
							}
						}

						ps = connexion.prepareStatement("SELECT MSISDN,LASTNAME,FIRSTNAME,TO_DATE(BIRTHDATE,'DY MON DD HH24:MI:SS YYYY') BIRTH_DATE,PREFERREDLANGUAGE FROM MTNB.CRM_CUSTOMER_DATA Aa WHERE ((TO_CHAR(TO_DATE(BIRTHDATE,'DY MON DD HH24:MI:SS YYYY'),'DDMM') IN (" + birthdays + ")) AND (Aa.SYS_CREATED_DATE_TIME >= ALL (SELECT B.SYS_CREATED_DATE_TIME FROM MTNB.CRM_CUSTOMER_DATA B WHERE B.MSISDN = Aa.MSISDN)))");
					}

					rs = ps.executeQuery();
					// Liste des elements
					while (rs.next()) {
						String msisdn = rs.getString("MSISDN").trim();
						String PREFERREDLANGUAGE = rs.getString("PREFERREDLANGUAGE").trim();
						int language = PREFERREDLANGUAGE.equalsIgnoreCase("fr") ? 1 : PREFERREDLANGUAGE.equalsIgnoreCase("en") ? 2 : 1; // PREFERREDLANGUAGE = fr,en
						String identity = (((rs.getString("FIRSTNAME") == null) ? "" : rs.getString("FIRSTNAME").trim()) + " " + ((rs.getString("LASTNAME") == null) ? "" : rs.getString("LASTNAME").trim())).trim();
						allMSISDN_Today_Is_BIRTHDATE.add(new HVC(0, (msisdn.length() == productProperties.getMsisdn_length()) ? productProperties.getMcc() + msisdn : msisdn, identity.isEmpty() ? msisdn : identity, 0, language, rs.getDate("BIRTH_DATE")));
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
					// ps = connexion.prepareStatement("SELECT MSISDN,CUSTOMER_SEGMENT FROM PRICEPLAN.VALUE_BAND_LIST WHERE ((CUSTOMER_SEGMENT = 'GOLD_P100') OR (CUSTOMER_SEGMENT = 'PREMIUM_P100') OR (CUSTOMER_SEGMENT = 'PLATINUM_P100') OR (CUSTOMER_SEGMENT = 'DIAMOND_P100'))");
					// ps = connexion.prepareStatement("SELECT MSISDN,UPPER(TRIM(CUSTOMER_SEGMENT)) FROM PRICEPLAN.VALUE_BAND_LIST WHERE " + productProperties.getCustomer_segment_filter());
					// ps = connexion.prepareStatement("SELECT MSISDN,CUSTOMER_SEGMENT FROM PRICEPLAN.VALUE_BAND_LIST WHERE " + productProperties.getCustomer_segment_filter());
					ps = connexion.prepareStatement(productProperties.getCustomer_segment_filter());
					rs = ps.executeQuery();
					// Liste des elements
					while (rs.next()) {
						String msisdn = rs.getString("MSISDN").trim();

						String CUSTOMER_SEGMENT = rs.getString("CUSTOMER_SEGMENT").trim();
						CUSTOMER_SEGMENT = CUSTOMER_SEGMENT.toUpperCase();

						int segment = productProperties.getCustomer_segment_list().contains(CUSTOMER_SEGMENT) ? (productProperties.getCustomer_segment_list().indexOf(CUSTOMER_SEGMENT) + 1) : 0;
						// allHVC.add(new HVC(0, (msisdn.length() == productProperties.getMsisdn_length()) ? productProperties.getMcc() + msisdn : msisdn, null, CUSTOMER_SEGMENT.equalsIgnoreCase("BRONZE_P100") ? 1 : CUSTOMER_SEGMENT.equalsIgnoreCase("SILVER_P100") ? 2 : CUSTOMER_SEGMENT.equalsIgnoreCase("GOLD_P100") ? 3 : CUSTOMER_SEGMENT.equalsIgnoreCase("PREMIUM_P100") ? 4 : CUSTOMER_SEGMENT.equalsIgnoreCase("PLATINUM_P100") ? 5 : CUSTOMER_SEGMENT.equalsIgnoreCase("DIAMOND_P100") ? 6 : CUSTOMER_SEGMENT.equalsIgnoreCase("IVOIRE_P100") ? 7 : CUSTOMER_SEGMENT.equalsIgnoreCase("GOLD_P100") ? 8 : 0, 0, null));
						allHVC.add(new HVC(0, (msisdn.length() == productProperties.getMsisdn_length()) ? productProperties.getMcc() + msisdn : msisdn, null, segment, 0, null));
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
						AccountDetails accountDetails = (new AIRRequest(productProperties.getAir_hosts(), productProperties.getAir_io_sleep(), productProperties.getAir_io_timeout(), productProperties.getAir_io_threshold())).getAccountDetails(hvc.getValue());
						if(accountDetails != null) hvc.setLanguage(accountDetails.getLanguageIDCurrent());
						// store hvc
						new HVCDAOJdbc(dao).saveOneHVC(hvc);

					} catch(Throwable th) {

					}
				}

				stepContribution.setExitStatus(ExitStatus.COMPLETED);
				return RepeatStatus.FINISHED;
			}

		} catch(Throwable th) {

		}

		return null;
	}

}
