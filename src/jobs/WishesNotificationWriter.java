package jobs;

import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.MessageSource;

import com.tools.SMPPConnector;

import connexions.AIRRequest;
import domain.models.HVConsumer;
import product.ProductProperties;
import util.AccountDetails;

public class WishesNotificationWriter implements ItemWriter<HVConsumer> {

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
	public void write(List<? extends HVConsumer> hvcs) {
		// TODO Auto-generated method stub

		try {
			Logger logger = LogManager.getLogger("logging.log4j.SubmitSMLogger");

			for(HVConsumer hvc : hvcs) {
				if(hvc != null) {
					try {
						AccountDetails accountDetails = (new AIRRequest(productProperties.getAir_hosts(), productProperties.getAir_io_sleep(), productProperties.getAir_io_timeout(), productProperties.getAir_io_threshold(), productProperties.getAir_preferred_host())).getAccountDetails(hvc.getValue());
						hvc.setLanguage((accountDetails == null) ? 1 : accountDetails.getLanguageIDCurrent());
						String message = i18n.getMessage("sms_birthday", new Object [] {hvc.getName()}, null, (hvc.getLanguage() == 2) ? Locale.ENGLISH : Locale.FRENCH);

						/*new SMPPConnector().submitSm("HVC", hvc.getValue(), message);*/
						new SMPPConnector().submitSm(productProperties.getSms_notifications_header(), hvc.getValue(), message);
						logger.log(Level.TRACE, "[" + hvc.getValue() + "] " + message);

					} catch(NullPointerException ex) {

					} catch(Throwable th) {

					}
				}
			}

		} catch(Throwable th) {

		}
	}

}
