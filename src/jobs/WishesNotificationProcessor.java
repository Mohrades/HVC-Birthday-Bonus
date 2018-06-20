package jobs;

import org.springframework.batch.item.ItemProcessor;
import domain.models.HVConsumer;

public class WishesNotificationProcessor implements ItemProcessor<HVConsumer, HVConsumer> {

	public WishesNotificationProcessor() {
		
	}

	@Override
	public HVConsumer process(HVConsumer hvc) {
		// TODO Auto-generated method stub

		try {
			return hvc;

		} catch(Throwable th) {

		}

		return null;
	}

}
