package jobs;

import java.util.Date;
import java.util.HashSet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import connexions.AIRRequest;
import dao.DAO;
import dao.JdbcOperations.HVCDAOJdbc;
import dao.JdbcOperations.RollBackDAOJdbc;
import dao.domain.model.HVC;
import dao.domain.model.RollBack;
import product.ProductProperties;
import util.BalanceAndDate;
import util.DedicatedAccount;

@Component("defaultBonusProcessor")
public class DefaultBonusProcessor implements ItemProcessor<HVC, HVC> {

	@Autowired
	private DAO dao;

	@Autowired
	private ProductProperties productProperties;

	@SuppressWarnings("deprecation")
	@Override
	public HVC process(HVC hvc) {
		// TODO Auto-generated method stub

		try {
			int offer = Integer.parseInt(productProperties.getOffer_id().get(hvc.getSegment() - 1));
			int da = productProperties.getVoice_da();
			long voice_volume = Long.parseLong(productProperties.getVoice_volume().get(hvc.getSegment() - 1));

			hvc.setBonus(1);

			if(new HVCDAOJdbc(dao).saveOneHVC(hvc) > 0) {
				Date expires = new Date();
				expires.setDate(expires.getDate() + 1);
				expires.setSeconds(59);expires.setMinutes(59);expires.setHours(23);

				HashSet<BalanceAndDate> balances = new HashSet<BalanceAndDate>();
				if(da == 0) balances.add(new BalanceAndDate(da, voice_volume, expires));
				else balances.add(new DedicatedAccount(da, voice_volume, expires));

				// update Anumber Balance
				if(new AIRRequest().updateBalanceAndDate(hvc.getValue(), balances, "HVC", (hvc.getSegment() + ""), "eBA")) {
					// update Anumber Offer
					if(new AIRRequest().updateOffer(hvc.getValue(), offer, null, expires, null, "eBA")) {
						return hvc;
					}
					// rollback
					else {
						balances.clear();
						if(da == 0) balances.add(new BalanceAndDate(da, -voice_volume, null));
						else balances.add(new DedicatedAccount(da, -voice_volume, null));

						if(new AIRRequest().updateBalanceAndDate(hvc.getValue(), balances, "HVC", (hvc.getSegment() + ""), "eBA"));
						else new RollBackDAOJdbc(dao).saveOneRollBack(new RollBack(0, -1, hvc.getSegment(), hvc.getValue(), hvc.getValue(), null));
					}
				}
				// rollback
				else {
					new RollBackDAOJdbc(dao).saveOneRollBack(new RollBack(0, 1, hvc.getSegment(), hvc.getValue(), hvc.getValue(), null));
				}
			}

		} catch(Throwable th) {
			
		}

		return null;
	}

}
