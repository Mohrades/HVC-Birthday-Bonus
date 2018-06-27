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
import dao.DAO;
import dao.queries.BirthDaysDAOJdbc;
import dao.queries.HVConsumerDAOJdbc;
import dao.queries.USSDServiceDAOJdbc;
import domain.models.HVConsumer;
import domain.models.USSDService;
import product.ProductProperties;
import tools.HappyBirthDayEventPublisher;

/*@Component("importHVConsumersTasklet")*/
public class ImportHVConsumersTasklet implements Tasklet {

	/*@Autowired*/
	private DAO dao;

	/*@Autowired*/
	private ProductProperties productProperties;

	public ImportHVConsumersTasklet() {
		
	}

	public DAO getDao() {
		return dao;
	}

	public void setDao(DAO dao) {
		this.dao = dao;
	}

	public ProductProperties getProductProperties() {
		return productProperties;
	}

	public void setProductProperties(ProductProperties productProperties) {
		this.productProperties = productProperties;
	}

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
			else if((new BirthDaysDAOJdbc(dao)).isBirthDayReported()) {
				// Sets stop flag
				chunkContext.getStepContext().getStepExecution().setTerminateOnly();
				stepContribution.setExitStatus(new ExitStatus("STOPPED", "Job should not be run right now."));
			}
			else {
				// set today HVC
				Connection connexion = null;
				PreparedStatement ps = null;
				ResultSet rs = null;

				HashSet <HVConsumer> allMSISDN_Today_Is_BIRTHDATE = new HashSet <HVConsumer>();

				try {
					Class.forName("oracle.jdbc.driver.OracleDriver"); // chargement du pilote JDBC
					// connexion = DriverManager.getConnection("jdbc:oracle:thin:@irmdb01.mtn.bj:1521:ISLDB1", "aplimanuser", "CI1617o#L#20"); // ouverture connexion
					connexion = DriverManager.getConnection("jdbc:oracle:thin:@ga-exa-scan.mtn.bj:1521/isldg", "aplimanuser", "CI1617o#L#20"); // ouverture connexion
					connexion.setAutoCommit(false); // d�but transaction
					connexion.setReadOnly(true); // en mode lecture seule

					// on lit la table MTNB.CRM_CUSTOMER_DATA [MSISDN, LASTNAME, FIRSTNAME, BIRTHDATE, PREFERREDLANGUAGE]
					// ps = connexion.prepareStatement("SELECT MSISDN,LASTNAME,FIRSTNAME,BIRTHDATE,PREFERREDLANGUAGE FROM MTNB.CRM_CUSTOMER_DATA WHERE (TO_CHAR(SYSDATE, 'MMDD') = TO_CHAR(TO_DATE(BIRTHDATE,'DY MON DD HH24:MI:SS YYYY'), 'MMDD'))");
					// ps = connexion.prepareStatement("SELECT COUNT(*) FROM MTNB.CRM_CUSTOMER_DATA Aa WHERE ((TO_CHAR(SYSDATE,'DDMM')= TO_CHAR(TO_DATE(BIRTHDATE,'DY MON DD HH24:MI:SS YYYY'),'DDMM')) AND (Aa.SYS_CREATED_DATE_TIME >= ALL (SELECT B.SYS_CREATED_DATE_TIME FROM MTNB.CRM_CUSTOMER_DATA B WHERE B.MSISDN = Aa.MSISDN)) AND (ROWNUM < 100000))");
					// ps = connexion.prepareStatement("SELECT * FROM MTNB.CRM_CUSTOMER_DATA Aa WHERE ((TO_CHAR(SYSDATE,'DDMM')= TO_CHAR(TO_DATE(BIRTHDATE,'DY MON DD HH24:MI:SS YYYY'),'DDMM')) AND (Aa.SYS_CREATED_DATE_TIME >= ALL (SELECT B.SYS_CREATED_DATE_TIME FROM MTNB.CRM_CUSTOMER_DATA B WHERE B.MSISDN = Aa.MSISDN)) AND (ROWNUM < 100000))");

					// Anciens propri�taires d'un MSISDN
					// ps = connexion.prepareStatement("SELECT MSISDN,LASTNAME,FIRSTNAME,BIRTHDATE,PREFERREDLANGUAGE FROM MTNB.CRM_CUSTOMER_DATA Aa WHERE ((TO_CHAR(SYSDATE,'DDMM')= TO_CHAR(TO_DATE(BIRTHDATE,'DY MON DD HH24:MI:SS YYYY'),'DDMM')) AND (Aa.SYS_CREATED_DATE_TIME < ANY (SELECT B.SYS_CREATED_DATE_TIME FROM MTNB.CRM_CUSTOMER_DATA B WHERE B.MSISDN = Aa.MSISDN)))");
					// La mise � jour la plus r�cente !!
					List<Map<String, Object>>  reporteds = (new BirthDaysDAOJdbc(dao)).getAllReportedBirthDays();

					if(reporteds.isEmpty()) {
						ps = connexion.prepareStatement("SELECT MSISDN,LASTNAME,FIRSTNAME,TO_DATE(BIRTHDATE,'DY MON DD HH24:MI:SS YYYY') BIRTH_DATE,PREFERREDLANGUAGE FROM MTNB.CRM_CUSTOMER_DATA Aa WHERE ((TO_CHAR(SYSDATE,'DDMM') = TO_CHAR(TO_DATE(BIRTHDATE,'DY MON DD HH24:MI:SS YYYY'),'DDMM')) AND (Aa.SYS_CREATED_DATE_TIME >= ALL (SELECT B.SYS_CREATED_DATE_TIME FROM MTNB.CRM_CUSTOMER_DATA B WHERE B.MSISDN = Aa.MSISDN)))");
					}
					else {
						SimpleDateFormat dateFormat = new SimpleDateFormat("ddMM");
						String birthdays = "'" + dateFormat.format(new Date()) + "'";

						for(Map<String, Object> reported : reporteds) {
							Set <Entry<String, Object> > entrees = reported.entrySet () ; // entrees est un ensemble de "paires"
							Iterator <Entry<String, Object> > iter = entrees.iterator() ; // it�rateur sur les paires
							// boucle sur les paires
							while (iter.hasNext()) {
								Map.Entry <String, Object> entree = (Entry<String, Object>)iter.next() ;  // paire courante

								String cle   = entree.getKey () ; // cl� de la paire courante
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
						int language = 1;

						try {
							String PREFERREDLANGUAGE = rs.getString("PREFERREDLANGUAGE").trim();
							language = PREFERREDLANGUAGE.equalsIgnoreCase("fr") ? 1 : PREFERREDLANGUAGE.equalsIgnoreCase("en") ? 2 : 1; // PREFERREDLANGUAGE = fr,en

						} catch(NumberFormatException ex) {

						} catch(Exception ex) {

						} catch(Throwable th) {

						}

						String identity = (((rs.getString("FIRSTNAME") == null) ? "" : rs.getString("FIRSTNAME").trim()) + " " + ((rs.getString("LASTNAME") == null) ? "" : rs.getString("LASTNAME").trim())).trim();
						allMSISDN_Today_Is_BIRTHDATE.add(new HVConsumer(0, (msisdn.length() == productProperties.getMsisdn_length()) ? productProperties.getMcc() + msisdn : msisdn, identity.isEmpty() ? msisdn : identity, 0, language, rs.getDate("BIRTH_DATE")));
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

				HashSet <HVConsumer> allHVC = new HashSet <HVConsumer>();

				try {
					Class.forName("oracle.jdbc.driver.OracleDriver"); // chargement du pilote JDBC
					connexion = DriverManager.getConnection("jdbc:oracle:thin:@ga-exa-scan.mtn.bj:1521/vmdg", "abutu", "kT60#bTh03#18"); // ouverture connexion
					connexion.setAutoCommit(false); // d�but transaction
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
						allHVC.add(new HVConsumer(0, (msisdn.length() == productProperties.getMsisdn_length()) ? productProperties.getMcc() + msisdn : msisdn, null, segment, 0, null));
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

				// publishers
				HashSet <HVConsumer> allMSISDN_Today_Is_BIRTHDATE_COPY = null;
				List<String> happy_birthday_bonus_event_listeners = productProperties.getHappy_birthday_bonus_event_listeners();
				if(happy_birthday_bonus_event_listeners != null) {
					allMSISDN_Today_Is_BIRTHDATE_COPY = new HashSet <HVConsumer>(allMSISDN_Today_Is_BIRTHDATE);
				}

				// croiser today_is_birthday and HVC
				allMSISDN_Today_Is_BIRTHDATE.retainAll(allHVC);

				for(HVConsumer hvc : allMSISDN_Today_Is_BIRTHDATE) {
					try {
						// store hvc
						new HVConsumerDAOJdbc(dao).saveOneHVConsumer(hvc);

					} catch(Throwable th) {

					}
				}

				// publish msisdn as a birthday susbcriber
				if(happy_birthday_bonus_event_listeners != null) {
					// croiser today_is_birthday and not HVC
					allMSISDN_Today_Is_BIRTHDATE_COPY.removeAll(allHVC);

					for(HVConsumer hvc : allMSISDN_Today_Is_BIRTHDATE_COPY) {
						try {
							for(String url : happy_birthday_bonus_event_listeners) {
								if((new HappyBirthDayEventPublisher()).notify(url, hvc.getValue(), hvc.getName(), hvc.getLanguage(), "eBA") == 0) ;
							}

						} catch(Throwable th) {

						}
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
