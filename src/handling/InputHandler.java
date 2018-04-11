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
import dao.JdbcOperations.RollBackDAOJdbc;
import dao.JdbcOperations.USSDRequestDAOJdbc;
import dao.JdbcOperations.USSDServiceDAOJdbc;
import dao.domain.model.HVC;
import dao.domain.model.RollBack;
import dao.domain.model.USSDRequest;
import dao.domain.model.USSDService;
import filter.MSISDNValidator;
import product.ProductProperties;
import product.USSDMenu;
import tools.SMPPConnector;
import util.BalanceAndDate;
import util.DedicatedAccount;
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
		int[] offer_id = new int[] {Integer.parseInt(productProperties.getOffer_id().get(0)), Integer.parseInt(productProperties.getOffer_id().get(1)), Integer.parseInt(productProperties.getOffer_id().get(2)), Integer.parseInt(productProperties.getOffer_id().get(3))};
		HashSet<OfferInformation> offers = new AIRRequest().getOffers(ussd.getMsisdn(), new int[][] {{offer_id[0], offer_id[0]}, {offer_id[1], offer_id[1]}, {offer_id[2], offer_id[2]}, {offer_id[3], offer_id[3]}}, false, null, false);

		if((offers == null) || offers.size() == 0) {
			endStep(dao, ussd, modele, productProperties, i18n.getMessage("status.failed", null, null, null), null, null, null, null);
		}
		else {
			BalanceAndDate balance = new AIRRequest().getBalanceAndDate(ussd.getMsisdn(), (int) productProperties.getData_da());

			if(balance == null) {
				balance = new AIRRequest().getBalanceAndDate(ussd.getMsisdn(), (int) productProperties.getVoice_da());

				if(balance == null) {
					endStep(dao, ussd, modele, productProperties, i18n.getMessage("status.failed", null, null, null), null, null, null, null);
				}
				else {
					endStep(dao, ussd, modele, productProperties, i18n.getMessage("voice.status", new Object [] {balance.getAccountValue()/(10*100), (new SimpleDateFormat("dd/MM/yyyy 'a' HH:mm")).format(balance.getExpiryDate())}, null, null), ussd.getMsisdn(), null, null, "HVC");
				}
			}
			else {
				if((balance.getAccountValue()/(10*100)) >= 1024) {
					endStep(dao, ussd, modele, productProperties, i18n.getMessage("data.status", new Object [] {new Formatter().format("%.2f", ((double)(balance.getAccountValue()/(10*100)))/1024), "Go", (new SimpleDateFormat("dd/MM/yyyy 'a' HH:mm")).format(balance.getExpiryDate())}, null, null), ussd.getMsisdn(), null, null, "HVC");
				}
				else {
					endStep(dao, ussd, modele, productProperties, i18n.getMessage("data.status", new Object [] {balance.getAccountValue()/(10*100), "Mo", (new SimpleDateFormat("dd/MM/yyyy 'a' HH:mm")).format(balance.getExpiryDate())}, null, null), ussd.getMsisdn(), null, null, "HVC");
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	public int doActions(DAO dao, HVC hvc, int offer, int da, long volume) {
		try {
			if(new HVCDAOJdbc(dao).saveOneHVC(hvc) > 0) {
				Date expires = new Date();
				expires.setDate(expires.getDate() + 1);
				expires.setSeconds(59);expires.setMinutes(59);expires.setHours(23);

				HashSet<BalanceAndDate> balances = new HashSet<BalanceAndDate>();
				if(da == 0) balances.add(new BalanceAndDate(da, volume, expires));
				else balances.add(new DedicatedAccount(da, volume, expires));

				// update Anumber Balance
				if(new AIRRequest().updateBalanceAndDate(hvc.getValue(), balances, "HVC", (hvc.getSegment() + ""), "eBA")) {
					// update Anumber Offer
					if(new AIRRequest().updateOffer(hvc.getValue(), offer, null, expires, null, "eBA")) {
						return 0;
					}
					// rollback
					else {
						balances.clear();
						if(da == 0) balances.add(new BalanceAndDate(da, -volume, null));
						else balances.add(new DedicatedAccount(da, -volume, null));

						if(new AIRRequest().updateBalanceAndDate(hvc.getValue(), balances, "HVC", (hvc.getSegment() + ""), "eBA"));
						else new RollBackDAOJdbc(dao).saveOneRollBack(new RollBack(0, -1, hvc.getSegment(), hvc.getValue(), hvc.getValue(), null));
					}
				}
				// rollback
				else {
					new RollBackDAOJdbc(dao).saveOneRollBack(new RollBack(0, 1, hvc.getSegment(), hvc.getValue(), hvc.getValue(), null));
				}
			}
			else {
				return 1;
			}

		} catch(Throwable th) {

		}

		return -1;
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
		HVC hvc = new HVCDAOJdbc(dao).getOneHVC(ussd.getMsisdn());

		int choice = Integer.parseInt(inputs.get(1));
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
		
		int result = doActions(dao, hvc, offer, da, volume);

		if(result == 0) {
			Date expires = new Date();
			expires.setDate(expires.getDate() + 1);
			expires.setSeconds(59);expires.setMinutes(59);expires.setHours(23);

			if(choice == 2) {
				volume = volume / (10*100);

				if(volume >= 1024) {
					endStep(dao, ussd, modele, productProperties, i18n.getMessage("sms.data.bonus", new Object [] {new Formatter().format("%.2f", ((double)volume)/1024), "Go", (new SimpleDateFormat("dd/MM/yyyy 'a' HH:mm")).format(expires)}, null, null), ussd.getMsisdn(), null, null, "HVC");
				}
				else {
					endStep(dao, ussd, modele, productProperties, i18n.getMessage("sms.data.bonus", new Object [] {volume, "Mo", (new SimpleDateFormat("dd/MM/yyyy 'a' HH:mm")).format(expires)}, null, null), ussd.getMsisdn(), null, null, "HVC");
				}
			}
			else {
				endStep(dao, ussd, modele, productProperties, i18n.getMessage("sms.voice.bonus", new Object [] {volume/(10*100), (new SimpleDateFormat("dd/MM/yyyy 'a' HH:mm")).format(expires)}, null, null), ussd.getMsisdn(), null, null, "HVC");
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
