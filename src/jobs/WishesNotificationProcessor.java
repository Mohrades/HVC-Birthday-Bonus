package jobs;

import java.util.Locale;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import domain.models.HVC;
import product.ProductProperties;
import tools.SMPPConnector;

@Component("wishesNotificationProcessor")
public class WishesNotificationProcessor implements ItemProcessor<HVC, HVC> {

	@Autowired
	private MessageSource i18n;

	@Autowired
	private ProductProperties productProperties;

	@Override
	public HVC process(HVC hvc) {
		// TODO Auto-generated method stub

		try {
			/*new SMPPConnector().submitSm("HVC", hvc.getValue().substring((productProperties.getMcc() + "").length()), i18n.getMessage("sms_birthday", new Object [] {hvc.getName()}, null, (hvc.getLanguage() == 2) ? Locale.UK : null));*/
			new SMPPConnector().submitSm("HVC", hvc.getValue().substring((productProperties.getMcc() + "").length()), i18n.getMessage("sms_birthday", new Object [] {hvc.getName()}, null, (hvc.getLanguage() == 2) ? Locale.ENGLISH : null));

		} catch(Throwable th) {

		}

		return null;
	}

}
