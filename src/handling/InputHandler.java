package handling;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.context.MessageSource;

import com.google.common.base.Splitter;

import connexions.AIRRequest;
import dao.DAO;
import dao.JdbcOperations.HVCDAOJdbc;
import dao.JdbcOperations.USSDRequestDAOJdbc;
import dao.JdbcOperations.USSDServiceDAOJdbc;
import dao.domain.model.HVC;
import dao.domain.model.USSDRequest;
import dao.domain.model.USSDService;
import filter.MSISDNValidator;
import product.ProductActions;
import product.ProductProperties;
import product.USSDMenu;
import tools.SMPPConnector;
import util.BalanceAndDate;
import util.OfferInformation;

public class InputHandler {

	public InputHandler() {

	}

	@SuppressWarnings("deprecation")
	public void handle(MessageSource i18n, ProductProperties productProperties, Map<String, String> parameters, Map<String, Object> modele, HttpServletRequest request, DAO dao) {
		USSDRequest ussd = null;

		try {
			long sessionId = Long.parseLong(parameters.get("sessionid"));
			ussd = new USSDRequestDAOJdbc(dao).getOneUSSD(sessionId, parameters.get("msisdn"));

			if(ussd == null) {
				USSDService service = new USSDServiceDAOJdbc(dao).getOneUSSDService(productProperties.getSc());
				Date now = new Date();

				if(((service.getStart_date() != null) && (now.before(service.getStart_date()))) || ((service.getStop_date() != null) && (now.after(service.getStop_date())))) {
					modele.put("next", false);
					modele.put("message", i18n.getMessage("service.unavailable", null, null, null));
					return;
				}

				ussd = new USSDRequest(0, sessionId, parameters.get("msisdn"), parameters.get("input").trim(), 1, null);
			}
			else {
				ussd.setStep(ussd.getStep() + 1);
				ussd.setInput((ussd.getInput() + "*" + parameters.get("input").trim()).trim());
			}

			// USSD Flow Status
			Map<String, Object> flowStatus = new USSDFlow().validate(ussd, (new USSDMenu()).getContent(productProperties.getSc()), productProperties, i18n);

			// -1 : exit with error (delete state from ussd table; message)
			if(((Integer)flowStatus.get("status")) == -1) {
				endStep(dao, ussd, modele, productProperties, (String)flowStatus.get("message"), null, null, null, null);
			}

			// 0  : successful (delete state from ussd table; actions and message)
			else if(((Integer)flowStatus.get("status")) == 0) {
				String short_code = productProperties.getSc() + "";

				if(ussd.getInput().equals(short_code + "*3")) {
					// envoie SMS de statut
					statut(i18n, productProperties, dao, ussd, modele);
				}
				else if(ussd.getInput().endsWith(short_code + "*1")) {
					Date expires = new Date();
					expires.setSeconds(00);expires.setMinutes(00);expires.setHours(12);

					if((new Date().before(expires)) && ((new MSISDNValidator()).isFiltered(dao, productProperties, ussd.getMsisdn(), "A"))) {
						List<String> inputs = Splitter.onPattern("[*]").trimResults().omitEmptyStrings().splitToList(ussd.getInput());

						if(inputs.size() == 3) {
							setBonus(dao, ussd, i18n, productProperties, modele, inputs);
						}
						else {
							endStep(dao, ussd, modele, productProperties, i18n.getMessage("request.unavailable", null, null, null), null, null, null, null);
						}
					}
					else endStep(dao, ussd, modele, productProperties, i18n.getMessage("menu.disabled", null, null, null), null, null, null, null);
				}
				else if(ussd.getInput().endsWith(short_code + "*2")) {
					endStep(dao, ussd, modele, productProperties, i18n.getMessage("service.internal.error", null, null, null), null, null, null, null);
				}
				else {
					endStep(dao, ussd, modele, productProperties, i18n.getMessage("service.internal.error", null, null, null), null, null, null, null);
				}
			}

			// 1  : flow continues (save state; message)
			else if(((Integer)flowStatus.get("status")) == 1) {
				if((new MSISDNValidator()).isFiltered(dao, productProperties, ussd.getMsisdn(), "A")) nextStep(dao, ussd, false, (String)flowStatus.get("message"), modele, productProperties);
				else {
					// envoie SMS de statut
					statut(i18n, productProperties, dao, ussd, modele);
				}
			}

			// this case should not occur
			else {
				endStep(dao, ussd, modele, productProperties, i18n.getMessage("service.internal.error", null, null, null), null, null, null, null);
			}

		} catch(NullPointerException ex) {
			endStep(dao, ussd, modele, productProperties, i18n.getMessage("service.internal.error", null, null, null), null, null, null, null);

		} catch(Throwable th) {
			endStep(dao, ussd, modele, productProperties, i18n.getMessage("service.internal.error", null, null, null), null, null, null, null);
		}
	}

	public void statut(MessageSource i18n, ProductProperties productProperties, DAO dao, USSDRequest ussd, Map<String, Object> modele) {
		HVC hvc = new HVCDAOJdbc(dao).getOneHVC(ussd.getMsisdn(), -1);

		if(hvc == null) {
			endStep(dao, ussd, modele, productProperties, i18n.getMessage("status.failed", null, null, null), null, null, null, null);
			return;
		}

		int[][] offer_id = new int[productProperties.getOffer_id().size()][2];
		for(int i = 0; i<productProperties.getOffer_id().size(); i++) {
			int offer = Integer.parseInt(productProperties.getOffer_id().get(i));
			offer_id[i][0] = offer;
			offer_id[i][1] = offer;
		}

		HashSet<OfferInformation> offers = new AIRRequest().getOffers(ussd.getMsisdn(), offer_id, false, null, false);

		if((offers == null) || offers.size() == 0) {
			endStep(dao, ussd, modele, productProperties, i18n.getMessage("status.failed", null, null, null), null, null, null, null);
		}
		else {
			BalanceAndDate balance = (hvc.getBonus() == 2) ? new AIRRequest().getBalanceAndDate(ussd.getMsisdn(), (int) productProperties.getData_da()) : (hvc.getBonus() == 1) ? new AIRRequest().getBalanceAndDate(ussd.getMsisdn(), (int) productProperties.getVoice_da()) : null;

			if(balance == null) {
				endStep(dao, ussd, modele, productProperties, i18n.getMessage("status.failed", null, null, null), null, null, null, null);
			}
			else {
				if(hvc.getBonus() == 2) {
					long volume = (long) (((double)balance.getAccountValue()) / ((Double.parseDouble(productProperties.getData_volume_rate().get(hvc.getSegment() - 1)))*1024*1024*100));
					if(volume >= 1024) {
						endStep(dao, ussd, modele, productProperties, i18n.getMessage("data.status", new Object [] {new Formatter().format("%.2f", volume/1024), "Go", (new SimpleDateFormat("dd/MM/yyyy 'a' HH:mm")).format(balance.getExpiryDate())}, null, null), ussd.getMsisdn(), null, null, "HVC");
					}
					else {
						endStep(dao, ussd, modele, productProperties, i18n.getMessage("data.status", new Object [] {volume, "Mo", (new SimpleDateFormat("dd/MM/yyyy 'a' HH:mm")).format(balance.getExpiryDate())}, null, null), ussd.getMsisdn(), null, null, "HVC");
					}					
				}
				else {
					long volume = (long) (((double)balance.getAccountValue()) / (Double.parseDouble(productProperties.getVoice_volume_rate().get(hvc.getSegment() - 1))));
					endStep(dao, ussd, modele, productProperties, i18n.getMessage("voice.status", new Object [] {volume/(60*100), (new SimpleDateFormat("dd/MM/yyyy 'a' HH:mm")).format(balance.getExpiryDate())}, null, null), ussd.getMsisdn(), null, null, "HVC");					
				}
			}
		}
	}

	public void endStep(DAO dao, USSDRequest ussd, Map<String, Object> modele, ProductProperties productProperties, String messageA, String Anumber, String messageB, String Bnumber, String senderName) {
		if((ussd != null) && (ussd.getId() > 0)) {
			new USSDRequestDAOJdbc(dao).deleteOneUSSD(ussd.getId());
		}

		modele.put("next", false);
		modele.put("message", messageA);

		if(senderName != null) {
			if(Anumber != null) {
				if(Anumber.startsWith(productProperties.getMcc() + "")) Anumber = Anumber.substring((productProperties.getMcc() + "").length());
				new SMPPConnector().submitSm(senderName, Anumber, messageA);
			}
			if(Bnumber != null) {
				if(Bnumber.startsWith(productProperties.getMcc() + "")) Bnumber = Bnumber.substring((productProperties.getMcc() + "").length());
				new SMPPConnector().submitSm(senderName, Bnumber, messageB);
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

		new USSDRequestDAOJdbc(dao).saveOneUSSD(ussd);

		modele.put("next", true);
		modele.put("message", message);
	}

	@SuppressWarnings("deprecation")
	public void setBonus(DAO dao, USSDRequest ussd, MessageSource i18n, ProductProperties productProperties, Map<String, Object> modele, List<String> inputs) {
		HVC hvc = new HVCDAOJdbc(dao).getOneHVC(ussd.getMsisdn(), 0);

		int choice = Integer.parseInt(inputs.get(1));
		// set bonus choice (data or voice)
		hvc.setBonus(choice);

		int offer = Integer.parseInt(productProperties.getOffer_id().get(hvc.getSegment() - 1));
		int da;
		long volume;
		if(choice == 2) {
			offer = 
			da = productProperties.getData_da();
			volume = Long.parseLong(productProperties.getData_volume().get(hvc.getSegment() - 1));
		}
		else {
			offer = Integer.parseInt(productProperties.getOffer_id().get(hvc.getSegment() - 1));
			da = productProperties.getVoice_da();
			volume = Long.parseLong(productProperties.getVoice_volume().get(hvc.getSegment() - 1));			
		}
		
		int result = (new ProductActions()).doActions(dao, hvc, offer, da, volume);

		if(result == 0) {
			Date expires = new Date();
			expires.setDate(expires.getDate() + 1);
			expires.setSeconds(59);expires.setMinutes(59);expires.setHours(23);

			if(choice == 2) {
				volume = (long) (((double)volume) / ((Double.parseDouble(productProperties.getData_volume_rate().get(hvc.getSegment() - 1)))*1024*1024*100));

				if(volume >= 1024) {
					endStep(dao, ussd, modele, productProperties, i18n.getMessage("sms.data.bonus", new Object [] {new Formatter().format("%.2f", ((double)volume)/1024), "Go", (new SimpleDateFormat("dd/MM/yyyy 'a' HH:mm")).format(expires)}, null, null), ussd.getMsisdn(), null, null, "HVC");
				}
				else {
					endStep(dao, ussd, modele, productProperties, i18n.getMessage("sms.data.bonus", new Object [] {volume, "Mo", (new SimpleDateFormat("dd/MM/yyyy 'a' HH:mm")).format(expires)}, null, null), ussd.getMsisdn(), null, null, "HVC");
				}
			}
			else {
				volume = (long) (((double)volume) / (Double.parseDouble(productProperties.getVoice_volume_rate().get(hvc.getSegment() - 1))));
				endStep(dao, ussd, modele, productProperties, i18n.getMessage("sms.voice.bonus", new Object [] {volume/(60*100), (new SimpleDateFormat("dd/MM/yyyy 'a' HH:mm")).format(expires)}, null, null), ussd.getMsisdn(), null, null, "HVC");
			}
		}
		else if(result == 1) {
			endStep(dao, ussd, modele, productProperties, i18n.getMessage("bonus.choice.done", null, null, null), null, null, null, null);
		}
		else {
			endStep(dao, ussd, modele, productProperties, i18n.getMessage("service.internal.error", null, null, null), null, null, null, null);
		}
	}

}
