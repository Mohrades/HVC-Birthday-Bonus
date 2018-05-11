package jobs;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import connexions.AIRRequest;
import domain.models.HVC;
import product.ProductProperties;
import tools.SMPPConnector;
import util.AccountDetails;

@Component("defaultBonusWriter")
public class DefaultBonusWriter implements ItemWriter<HVC> {

	@Autowired
	private MessageSource i18n;

	@Autowired
	private ProductProperties productProperties;

	@Override
	public void write(List<? extends HVC> hvcs) {
		// TODO Auto-generated method stub

		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy 'a' HH:mm");

			for(HVC hvc : hvcs) {
				if(hvc != null) {
					try {
						AccountDetails accountDetails = new AIRRequest().getAccountDetails(hvc.getValue());
						hvc.setLanguage((accountDetails == null) ? 1 : accountDetails.getLanguageIDCurrent());

						long volume = (long) (((double)Long.parseLong(productProperties.getVoice_volume().get(hvc.getSegment() - 1))) / (Double.parseDouble(productProperties.getVoice_volume_rate().get(hvc.getSegment() - 1))));
						new SMPPConnector().submitSm("HVC", hvc.getValue().substring((productProperties.getMcc() + "").length()), i18n.getMessage("sms.voice.bonus", new Object [] {volume/(60*100), dateFormat.format(hvc.getBonus_expires_in())}, null, (hvc.getLanguage() == 2) ? Locale.ENGLISH : Locale.FRENCH));

					} catch(NullPointerException ex) {

					} catch(Throwable th) {

					}					
				}
			}

		} catch(Throwable th) {

		}
	}

}
