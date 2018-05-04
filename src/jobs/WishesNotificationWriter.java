package jobs;

import java.util.List;

import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import domain.models.HVC;

@Component("wishesNotificationWriter")
public class WishesNotificationWriter implements ItemWriter<HVC> {

	@Override
	public void write(List<? extends HVC> arg0) {
		// TODO Auto-generated method stub

	}

}
