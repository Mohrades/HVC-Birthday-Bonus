package jobs;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.MessageSource;
import connexions.AIRRequest;
import domain.models.HVConsumer;
import product.ProductProperties;
import tools.SMPPConnector;
import util.AccountDetails;

public class DefaultBonusWriter implements ItemWriter<HVConsumer> {

	private MessageSource i18n;

	private ProductProperties productProperties;

	public DefaultBonusWriter() {

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
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy 'a' HH:mm");
			Logger logger = LogManager.getLogger("logging.log4j.SubmitSMLogger");

			for(HVConsumer hvc : hvcs) {
				if(hvc != null) {
					try {
						AccountDetails accountDetails = (new AIRRequest(productProperties.getAir_hosts(), productProperties.getAir_io_sleep(), productProperties.getAir_io_timeout(), productProperties.getAir_io_threshold(), productProperties.getAir_preferred_host())).getAccountDetails(hvc.getValue());
						hvc.setLanguage((accountDetails == null) ? 1 : accountDetails.getLanguageIDCurrent());

						long volume = (long) (((double)Long.parseLong(productProperties.getVoice_volume().get(hvc.getSegment() - 1))) / (Double.parseDouble(productProperties.getVoice_volume_rate().get(hvc.getSegment() - 1))));
						String message = i18n.getMessage("sms.voice.bonus", new Object [] {volume/(60*100), dateFormat.format(hvc.getBonus_expires_in())}, null, (hvc.getLanguage() == 2) ? Locale.ENGLISH : Locale.FRENCH);

						new SMPPConnector().submitSm(productProperties.getSms_notifications_header(), hvc.getValue(), message);
						logger.trace("[" + hvc.getValue() + "] " + message);

					} catch(NullPointerException ex) {

					} catch(Throwable th) {

					}
				}
			}

		} catch(Throwable th) {

		}
	}

}
