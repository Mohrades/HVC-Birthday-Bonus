package jobs;

import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.MessageSource;
import connexions.AIRRequest;
import domain.models.HVC;
import product.ProductProperties;
import tools.SMPPConnector;
import util.AccountDetails;

public class WishesNotificationWriter implements ItemWriter<HVC> {

	private MessageSource i18n;
	private ProductProperties productProperties;

	public WishesNotificationWriter() {

	}

	public MessageSource getI18n() {
		return i18n;
	}

	public void setI18n(MessageSource i18n) {
		this.i18n = i18n;
	}

	public ProductProperties getProductProperties() {
		return productProperties;
	}

	public void setProductProperties(ProductProperties productProperties) {
		this.productProperties = productProperties;
	}

	@Override
	public void write(List<? extends HVC> hvcs) {
		// TODO Auto-generated method stub

		try {
			Logger logger = LogManager.getLogger("logging.log4j.SubmitSMLogger");

			for(HVC hvc : hvcs) {
				if(hvc != null) {
					try {
						AccountDetails accountDetails = (new AIRRequest(productProperties.getAir_hosts(), productProperties.getAir_io_sleep(), productProperties.getAir_io_timeout(), productProperties.getAir_io_threshold())).getAccountDetails(hvc.getValue());
						hvc.setLanguage((accountDetails == null) ? 1 : accountDetails.getLanguageIDCurrent());

						/*new SMPPConnector().submitSm("HVC", hvc.getValue(), i18n.getMessage("sms_birthday", new Object [] {hvc.getName()}, null, (hvc.getLanguage() == 2) ? Locale.ENGLISH : Locale.FRENCH));*/
						new SMPPConnector().submitSm(productProperties.getSms_notifications_header(), hvc.getValue(), i18n.getMessage("sms_birthday", new Object [] {hvc.getName()}, null, (hvc.getLanguage() == 2) ? Locale.ENGLISH : Locale.FRENCH));
						logger.log(Level.TRACE, "[" + hvc.getValue() + "] " + i18n.getMessage("sms_birthday", new Object [] {hvc.getName()}, null, (hvc.getLanguage() == 2) ? Locale.ENGLISH : Locale.FRENCH));

					} catch(NullPointerException ex) {

					} catch(Throwable th) {

					}					
				}
			}

		} catch(Throwable th) {

		}
	}

}
