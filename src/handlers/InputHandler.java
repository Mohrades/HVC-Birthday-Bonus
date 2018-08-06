package handlers;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.MessageSource;

import com.google.common.base.Splitter;

import connexions.AIRRequest;
import dao.DAO;
import dao.queries.JdbcHVConsumerDao;
import dao.queries.JdbcUSSDRequestDao;
import dao.queries.JdbcUSSDServiceDao;
import domain.models.HVConsumer;
import domain.models.USSDRequest;
import domain.models.USSDService;
import exceptions.AirAvailabilityException;
import filter.MSISDNValidator;
import product.ProductActions;
import product.ProductProperties;
import product.USSDMenu;
import tools.SMPPConnector;
import util.AccountDetails;
import util.BalanceAndDate;
import util.OfferInformation;

public class InputHandler {

	public InputHandler() {

	}

	@SuppressWarnings("deprecation")
	public void handle(MessageSource i18n, ProductProperties productProperties, Map<String, String> parameters, Map<String, Object> modele, HttpServletRequest request, DAO dao) {
		USSDRequest ussd = null;
		HVConsumer hvc = null;
		int language = 1;

		try {
			if(productProperties.getAir_preferred_host() != -1) {
				AccountDetails accountDetails = (new AIRRequest(productProperties.getAir_hosts(), productProperties.getAir_io_sleep(), productProperties.getAir_io_timeout(), productProperties.getAir_io_threshold(), productProperties.getAir_preferred_host())).getAccountDetails(parameters.get("msisdn"));
				language = (accountDetails == null) ? 1 : accountDetails.getLanguageIDCurrent();
			}
			else {
				throw new AirAvailabilityException();
			}

			long sessionId = Long.parseLong(parameters.get("sessionid"));
			ussd = new JdbcUSSDRequestDao(dao).getOneUSSD(sessionId, parameters.get("msisdn"));

			if(ussd == null) {
				ussd = new USSDRequest(0, sessionId, parameters.get("msisdn"), parameters.get("input").trim(), 1, null);

				USSDService service = new JdbcUSSDServiceDao(dao).getOneUSSDService(productProperties.getSc());
				Date now = new Date();

				if((service == null) || (((service.getStart_date() != null) && (now.before(service.getStart_date()))) || ((service.getStop_date() != null) && (now.after(service.getStop_date()))))) {
					modele.put("next", false);
					modele.put("message", i18n.getMessage("service.unavailable", null, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH));
					return;
				}
				else {
					// check msisdn is alive hvc (yesterday and today)
					hvc = new JdbcHVConsumerDao(dao).getOneHVConsumer(ussd.getMsisdn(), -1);

					if(hvc == null) {
						modele.put("next", false);
						modele.put("message", i18n.getMessage("service.disabled", null, null, (language == 2) ? Locale.ENGLISH : Locale.FRENCH));
						return;
					}
					else {
						hvc.setLanguage(language);

						// logging conflicting
						if(hvc.getId() == 0) {
							modele.put("next", false);
							modele.put("message", i18n.getMessage("service.internal.error", null, null, (hvc.getLanguage() == 2) ? Locale.ENGLISH : Locale.FRENCH));
							return;
						}
						else {
							/*if((now.getHours() >= 12) || (hvc.getId() < 0)) {*/
							/*if((hvc.getBonus() > 0) || ((now.getHours() >= 12) || (hvc.getId() < 0))) {*/
							if((hvc.getBonus() > 0) || (hvc.getId() < 0)) {
								// envoie SMS de statut
								statut(i18n, productProperties, dao, hvc, ussd, modele);
								return;
							}
						}
					}
				}
			}
			else {
				// check msisdn is alive hvc (only today)
				hvc = new JdbcHVConsumerDao(dao).getOneHVConsumer(ussd.getMsisdn(), 0);
				hvc.setLanguage(language);

				ussd.setStep(ussd.getStep() + 1);
				ussd.setInput((ussd.getInput() + "*" + parameters.get("input").trim()).trim());
			}

			// USSD Flow Status
			Map<String, Object> flowStatus = new USSDFlow().validate(ussd, hvc, (new USSDMenu()).getContent(productProperties.getSc()), productProperties, i18n);

			// -1 : exit with error (delete state from ussd table; message)
			if(((Integer)flowStatus.get("status")) == -1) {
				endStep(dao, ussd, modele, productProperties, (String)flowStatus.get("message"), null, null, null, null);
			}

			// 0  : successful (delete state from ussd table; actions and message)
			else if(((Integer)flowStatus.get("status")) == 0) {
				// logging
				Logger logger = LogManager.getLogger("logging.log4j.ProcessingLogger");
				logger.trace("[" + productProperties.getSc() + "] " + "[USSD] " + "[" + parameters.get("msisdn") + "] " + "[" + ussd.getInput() + "]");


				// short code
				String short_code = productProperties.getSc() + "";

				if(ussd.getInput().equals(short_code + "*3")) {
					// envoie SMS de statut
					statut(i18n, productProperties, dao, hvc, ussd, modele);
				}
				else if((ussd.getInput().startsWith(short_code + "*1*")) || (ussd.getInput().startsWith(short_code + "*2*"))) {
					if(ussd.getInput().endsWith("*1")) {
						Date expires = new Date();
						expires.setSeconds(00);expires.setMinutes(00);expires.setHours(12);

						/*if((new Date().before(expires)) && ((new MSISDNValidator()).isFiltered(dao, productProperties, ussd.getMsisdn(), "A"))) {*/
						if((new MSISDNValidator()).isFiltered(dao, productProperties, ussd.getMsisdn(), "A")) {
							List<String> inputs = Splitter.onPattern("[*]").trimResults().omitEmptyStrings().splitToList(ussd.getInput());

							if(inputs.size() == 3) {
								setBonus(dao, hvc, ussd, i18n, productProperties, modele, inputs);
							}
							else {
								endStep(dao, ussd, modele, productProperties, i18n.getMessage("request.unavailable", null, null, (hvc.getLanguage() == 2) ? Locale.ENGLISH : Locale.FRENCH), null, null, null, null);
							}
						}
						else endStep(dao, ussd, modele, productProperties, i18n.getMessage("menu.disabled", null, null, (hvc.getLanguage() == 2) ? Locale.ENGLISH : Locale.FRENCH), null, null, null, null);						
					}
					else if(ussd.getInput().endsWith("*2")) {
						endStep(dao, ussd, modele, productProperties, i18n.getMessage("service.internal.error", null, null, (hvc.getLanguage() == 2) ? Locale.ENGLISH : Locale.FRENCH), null, null, null, null);
					}
				}
				else {
					endStep(dao, ussd, modele, productProperties, i18n.getMessage("service.internal.error", null, null, (hvc.getLanguage() == 2) ? Locale.ENGLISH : Locale.FRENCH), null, null, null, null);
				}
			}

			// 1  : flow continues (save state; message)
			else if(((Integer)flowStatus.get("status")) == 1) {
				if((new MSISDNValidator()).isFiltered(dao, productProperties, ussd.getMsisdn(), "A")) {
					nextStep(dao, ussd, false, (String)flowStatus.get("message"), modele, productProperties);
				}
				else {
					// envoie SMS de statut
					statut(i18n, productProperties, dao, hvc, ussd, modele);
				}
			}

			// this case should not occur
			else {
				endStep(dao, ussd, modele, productProperties, i18n.getMessage("service.internal.error", null, null, (hvc.getLanguage() == 2) ? Locale.ENGLISH : Locale.FRENCH), null, null, null, null);
			}

		} catch(NullPointerException ex) {
			endStep(dao, ussd, modele, productProperties, i18n.getMessage("service.internal.error", null, null, Locale.FRENCH), null, null, null, null);

		} catch(AirAvailabilityException ex) {
			endStep(dao, ussd, modele, productProperties, i18n.getMessage("service.internal.error", null, null, Locale.FRENCH), null, null, null, null);

		} catch(Throwable th) {
			endStep(dao, ussd, modele, productProperties, i18n.getMessage("service.internal.error", null, null, Locale.FRENCH), null, null, null, null);
		}
	}

	public void statut(MessageSource i18n, ProductProperties productProperties, DAO dao, HVConsumer hvc, USSDRequest ussd, Map<String, Object> modele) {
		/*HVC hvc = new HVCDAOJdbc(dao).getOneHVC(ussd.getMsisdn(), -1);*/

		if((hvc == null) || (hvc.getBonus() <= 0)) {
			endStep(dao, ussd, modele, productProperties, i18n.getMessage("status.failed", null, null, Locale.FRENCH), null, null, null, null);
			return;
		}

		int offer = Integer.parseInt(productProperties.getOffer_id().get(hvc.getSegment() - 1));
		HashSet<OfferInformation> offers = (offer > 0) ? (new AIRRequest(productProperties.getAir_hosts(), productProperties.getAir_io_sleep(), productProperties.getAir_io_timeout(), productProperties.getAir_io_threshold(), productProperties.getAir_preferred_host())).getOffers(ussd.getMsisdn(), new int[][]{{offer,offer}}, false, null, false) : null;

		if((offer > 0) && ((offers == null) || offers.size() == 0)) {
			endStep(dao, ussd, modele, productProperties, i18n.getMessage("status.failed", null, null, (hvc.getLanguage() == 2) ? Locale.ENGLISH : Locale.FRENCH), null, null, null, null);
		}
		else {
			BalanceAndDate balance = (hvc.getBonus() == 2) ? (new AIRRequest(productProperties.getAir_hosts(), productProperties.getAir_io_sleep(), productProperties.getAir_io_timeout(), productProperties.getAir_io_threshold(), productProperties.getAir_preferred_host())).getBalanceAndDate(ussd.getMsisdn(), productProperties.getData_da()) : (hvc.getBonus() == 1) ? (new AIRRequest(productProperties.getAir_hosts(), productProperties.getAir_io_sleep(), productProperties.getAir_io_timeout(), productProperties.getAir_io_threshold(), productProperties.getAir_preferred_host())).getBalanceAndDate(ussd.getMsisdn(), productProperties.getVoice_da()) : null;

			if(balance == null) {
				endStep(dao, ussd, modele, productProperties, i18n.getMessage("status.failed", null, null, (hvc.getLanguage() == 2) ? Locale.ENGLISH : Locale.FRENCH), null, null, null, null);
			}
			else {
				if(hvc.getBonus() == 2) {
					long volume = (long) (((double)balance.getAccountValue()) / ((Double.parseDouble(productProperties.getData_volume_rate().get(hvc.getSegment() - 1)))*1024*1024*100));
					if(volume >= 1024) {
						endStep(dao, ussd, modele, productProperties, i18n.getMessage("data.status", new Object [] {new Formatter().format("%.2f", ((double)volume/1024)), (hvc.getLanguage() == 2) ? "GB" : "Go", (hvc.getLanguage() == 2) ? (new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm")).format(balance.getExpiryDate()) : (new SimpleDateFormat("dd/MM/yyyy 'a' HH:mm")).format(balance.getExpiryDate())}, null, (hvc.getLanguage() == 2) ? Locale.ENGLISH : Locale.FRENCH), null, null, null, null);
					}
					else {
						endStep(dao, ussd, modele, productProperties, i18n.getMessage("data.status", new Object [] {volume, (hvc.getLanguage() == 2) ? "MB" : "Mo", (hvc.getLanguage() == 2) ? (new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm")).format(balance.getExpiryDate()) : (new SimpleDateFormat("dd/MM/yyyy 'a' HH:mm")).format(balance.getExpiryDate())}, null, (hvc.getLanguage() == 2) ? Locale.ENGLISH : Locale.FRENCH), null, null, null, null);
					}
				}
				else {
					long volume = (long) (((double)balance.getAccountValue()) / (Double.parseDouble(productProperties.getVoice_volume_rate().get(hvc.getSegment() - 1))));
					volume = volume/100;
					endStep(dao, ussd, modele, productProperties, i18n.getMessage("voice.status", new Object [] {volume/60, volume%60, (new SimpleDateFormat("dd/MM/yyyy 'a' HH:mm")).format(balance.getExpiryDate())}, null, (hvc.getLanguage() == 2) ? Locale.ENGLISH : Locale.FRENCH), null, null, null, null);					
				}
			}
		}
	}

	public void endStep(DAO dao, USSDRequest ussd, Map<String, Object> modele, ProductProperties productProperties, String messageA, String Anumber, String messageB, String Bnumber, String senderName) {
		if((ussd != null) && (ussd.getId() > 0)) {
			new JdbcUSSDRequestDao(dao).deleteOneUSSD(ussd.getId());
		}

		modele.put("next", false);
		modele.put("message", messageA);

		if(senderName != null) {
			Logger logger = LogManager.getLogger("logging.log4j.SubmitSMLogger");

			if(Anumber != null) {
				// if(Anumber.startsWith(productProperties.getMcc() + "")) Anumber = Anumber.substring((productProperties.getMcc() + "").length());
				new SMPPConnector().submitSm(senderName, Anumber, messageA);
				logger.trace("[" + Anumber + "] " + messageA);
			}
			if(Bnumber != null) {
				// if(Bnumber.startsWith(productProperties.getMcc() + "")) Bnumber = Bnumber.substring((productProperties.getMcc() + "").length());
				new SMPPConnector().submitSm(senderName, Bnumber, messageB);
				logger.log(Level.TRACE, "[" + Bnumber + "] " + messageB);
			}
		}
	}

	public void nextStep(DAO dao, USSDRequest ussd, boolean reset, String message, Map<String, Object> modele, ProductProperties productProperties) {
		if(reset) {
			ussd.setStep(1);
			ussd.setInput(productProperties.getSc() + "");
		}
		else {
			//
		}

		new JdbcUSSDRequestDao(dao).saveOneUSSD(ussd);

		modele.put("next", true);
		modele.put("message", message);
	}

	@SuppressWarnings("deprecation")
	public void setBonus(DAO dao, HVConsumer hvc, USSDRequest ussd, MessageSource i18n, ProductProperties productProperties, Map<String, Object> modele, List<String> inputs) throws AirAvailabilityException {
		/*HVC hvc = new HVCDAOJdbc(dao).getOneHVC(ussd.getMsisdn(), 0);*/

		int choice = Integer.parseInt(inputs.get(1));
		// set bonus choice (data or voice)
		hvc.setBonus(choice);

		int offer = Integer.parseInt(productProperties.getOffer_id().get(hvc.getSegment() - 1));
		int da;
		long volume;

		if(choice == 2) {
			da = productProperties.getData_da();
			volume = Long.parseLong(productProperties.getData_volume().get(hvc.getSegment() - 1));
		}
		else {
			da = productProperties.getVoice_da();
			volume = Long.parseLong(productProperties.getVoice_volume().get(hvc.getSegment() - 1));
		}

		int result = (new ProductActions(productProperties)).doActions(dao, hvc, offer, da, volume, productProperties.getAccumulator_id());

		if(result == 0) {
			Date expires = new Date();
			expires.setDate(expires.getDate() + 1);
			expires.setSeconds(59);expires.setMinutes(59);expires.setHours(23);

			if(choice == 2) {
				volume = (long) (((double)volume) / ((Double.parseDouble(productProperties.getData_volume_rate().get(hvc.getSegment() - 1)))*1024*1024*100));

				if(volume >= 1024) {
					endStep(dao, ussd, modele, productProperties, i18n.getMessage("sms.data.bonus", new Object [] {new Formatter().format("%.2f", ((double)volume)/1024), "Go", (new SimpleDateFormat("dd/MM/yyyy 'a' HH:mm")).format(expires)}, null, (hvc.getLanguage() == 2) ? Locale.ENGLISH : Locale.FRENCH), ussd.getMsisdn(), null, null, productProperties.getSms_notifications_header());
				}
				else {
					endStep(dao, ussd, modele, productProperties, i18n.getMessage("sms.data.bonus", new Object [] {volume, "Mo", (new SimpleDateFormat("dd/MM/yyyy 'a' HH:mm")).format(expires)}, null, (hvc.getLanguage() == 2) ? Locale.ENGLISH : Locale.FRENCH), ussd.getMsisdn(), null, null, productProperties.getSms_notifications_header());
				}
			}
			else {
				volume = (long) (((double)volume) / (Double.parseDouble(productProperties.getVoice_volume_rate().get(hvc.getSegment() - 1))));
				endStep(dao, ussd, modele, productProperties, i18n.getMessage("sms.voice.bonus", new Object [] {volume/(60*100), (new SimpleDateFormat("dd/MM/yyyy 'a' HH:mm")).format(expires)}, null, (hvc.getLanguage() == 2) ? Locale.ENGLISH : Locale.FRENCH), ussd.getMsisdn(), null, null, productProperties.getSms_notifications_header());
			}
		}
		else if(result == 1) {
			// endStep(dao, ussd, modele, productProperties, i18n.getMessage("bonus.choice.done", null, null, (hvc.getLanguage() == 2) ? Locale.ENGLISH : Locale.FRENCH), null, null, null, null);
			endStep(dao, ussd, modele, productProperties, i18n.getMessage("service.internal.error", null, null, (hvc.getLanguage() == 2) ? Locale.ENGLISH : Locale.FRENCH), null, null, null, null);
		}
		else {
			endStep(dao, ussd, modele, productProperties, i18n.getMessage("service.internal.error", null, null, (hvc.getLanguage() == 2) ? Locale.ENGLISH : Locale.FRENCH), null, null, null, null);
		}
	}

}
