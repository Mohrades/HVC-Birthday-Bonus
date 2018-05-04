package product;

import java.util.Date;
import java.util.HashSet;

import connexions.AIRRequest;
import dao.DAO;
import dao.queries.HVCDAOJdbc;
import dao.queries.RollBackDAOJdbc;
import domain.models.HVC;
import domain.models.RollBack;
import util.BalanceAndDate;
import util.DedicatedAccount;

public class ProductActions {
	
	public ProductActions() {
		
	}
	
	@SuppressWarnings("deprecation")
	public int doActions(DAO dao, HVC hvc, int offer, int da, long volume) {
		try {
			Date expires = new Date();
			expires.setDate(expires.getDate() + 1);
			expires.setSeconds(59);expires.setMinutes(59);expires.setHours(23);
			// set bonus expiry date
			hvc.setBonus_expires_in(expires);

			if(new HVCDAOJdbc(dao).saveOneHVC(hvc) > 0) {
				HashSet<BalanceAndDate> balances = new HashSet<BalanceAndDate>();
				if(da == 0) balances.add(new BalanceAndDate(da, volume, expires));
				else balances.add(new DedicatedAccount(da, volume, expires));

				// update Anumber Balance
				if(new AIRRequest().updateBalanceAndDate(hvc.getValue(), balances, "HVC", (hvc.getSegment() + ""), "eBA")) {
					// update Anumber Offer
					if(new AIRRequest().updateOffer(hvc.getValue(), offer, null, expires, null, "eBA")) {
						return 0;
					}
					// rollback
					else {
						balances.clear();
						if(da == 0) balances.add(new BalanceAndDate(da, -volume, null));
						else balances.add(new DedicatedAccount(da, -volume, null));

						if(new AIRRequest().updateBalanceAndDate(hvc.getValue(), balances, "HVC", (hvc.getSegment() + ""), "eBA"));
						else new RollBackDAOJdbc(dao).saveOneRollBack(new RollBack(0, -1, hvc.getSegment(), hvc.getValue(), hvc.getValue(), null));
					}
				}
				// rollback
				else {
					new RollBackDAOJdbc(dao).saveOneRollBack(new RollBack(0, 1, hvc.getSegment(), hvc.getValue(), hvc.getValue(), null));
				}
			}
			else {
				return 1;
			}

		} catch(Throwable th) {

		}

		return -1;
	}

}
