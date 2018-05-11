package jobs;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import domain.models.HVC;

@Component("wishesNotificationProcessor")
public class WishesNotificationProcessor implements ItemProcessor<HVC, HVC> {

	@Override
	public HVC process(HVC hvc) {
		// TODO Auto-generated method stub

		try {
			return hvc;

		} catch(Throwable th) {

		}

		return null;
	}

}
