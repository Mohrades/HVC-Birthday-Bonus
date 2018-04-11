package jobs;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import dao.domain.model.HVC;
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
			new SMPPConnector().submitSm("HVC", hvc.getValue().substring((productProperties.getMcc() + "").length()), i18n.getMessage("sms_birthday", new Object [] {hvc.getName()}, null, null));

		} catch(Throwable th) {

		}

		return null;
	}

}
