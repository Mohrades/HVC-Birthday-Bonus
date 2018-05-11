package jobs;

import java.util.List;
import java.util.Locale;

import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import domain.models.HVC;
import product.ProductProperties;
import tools.SMPPConnector;

@Component("wishesNotificationWriter")
public class WishesNotificationWriter implements ItemWriter<HVC> {

	@Autowired
	private MessageSource i18n;

	@Autowired
	private ProductProperties productProperties;

	@Override
	public void write(List<? extends HVC> hvcs) {
		// TODO Auto-generated method stub

		try {
			for(HVC hvc : hvcs) {
				if(hvc != null) {
					try {
						/*new SMPPConnector().submitSm("HVC", hvc.getValue().substring((productProperties.getMcc() + "").length()), i18n.getMessage("sms_birthday", new Object [] {hvc.getName()}, null, (hvc.getLanguage() == 2) ? Locale.ENGLISH : Locale.FRENCH));*/
						new SMPPConnector().submitSm("HVC", hvc.getValue().substring((productProperties.getMcc() + "").length()), i18n.getMessage("sms_birthday", new Object [] {hvc.getName()}, null, (hvc.getLanguage() == 2) ? Locale.ENGLISH : Locale.FRENCH));

					} catch(NullPointerException ex) {

					} catch(Throwable th) {

					}					
				}
			}

		} catch(Throwable th) {

		}
	}

}
