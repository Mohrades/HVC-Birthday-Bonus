package jobs;

import java.text.SimpleDateFormat;
import java.util.List;

import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import dao.domain.model.HVC;
import product.ProductProperties;
import tools.SMPPConnector;

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
				try {
					new SMPPConnector().submitSm("HVC", hvc.getValue().substring((productProperties.getMcc() + "").length()), i18n.getMessage("sms.voice.bonus", new Object [] {Long.parseLong(productProperties.getVoice_volume().get(hvc.getSegment() - 1)), dateFormat.format(hvc.getBonus_expires_in())}, null, null));

				} catch(NullPointerException ex) {

				} catch(Throwable th) {

				}
			}

		} catch(Throwable th) {

		}
	}

}
