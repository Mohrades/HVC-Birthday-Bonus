package product;

import java.util.Date;
import java.util.HashSet;

import connexions.AIRRequest;
import dao.DAO;
import dao.queries.HVCDAOJdbc;
import dao.queries.RollBackDAOJdbc;
import domain.models.HVC;
import domain.models.RollBack;
import exceptions.AirAvailabilityException;
import util.AccumulatorInformation;
import util.BalanceAndDate;
import util.DedicatedAccount;

public class ProductActions {
	ProductProperties productProperties;
	
	public ProductActions(ProductProperties productProperties) {
		this.productProperties = productProperties;
	}
	
	@SuppressWarnings("deprecation")
	public int doActions(DAO dao, HVC hvc, int offer, int da, long volume, int accumulator) throws AirAvailabilityException {
		AIRRequest request = new AIRRequest(productProperties.getAir_hosts(), productProperties.getAir_io_sleep(), productProperties.getAir_io_timeout(), productProperties.getAir_io_threshold());

		int responseCode = -1;

		if((request.getBalanceAndDate(hvc.getValue(), 0)) != null) {
			try {
				Date expires = new Date();
				expires.setDate(expires.getDate() + 1);
				expires.setSeconds(59);expires.setMinutes(59);expires.setHours(23);
				// set bonus expiry date
				hvc.setBonus_expires_in(expires);

				if(new HVCDAOJdbc(dao).locking(hvc, true) > 0) {
					responseCode = 1;

					HashSet<BalanceAndDate> balances = new HashSet<BalanceAndDate>();
					if(da == 0) balances.add(new BalanceAndDate(da, volume, expires));
					else balances.add(new DedicatedAccount(da, volume, expires));

					// update Anumber Balance
					if(request.updateBalanceAndDate(hvc.getValue(), balances, "HVC", (hvc.getSegment() + ""), "eBA")) {
						// update Anumber Offer
						if(request.updateOffer(hvc.getValue(), offer, null, expires, null, "eBA")) {
							// reset accumulator
					        HashSet<AccumulatorInformation> accumulatorInformationList = new HashSet<AccumulatorInformation>();
					        AccumulatorInformation accumulatorInformation = new AccumulatorInformation(accumulator, 0, null, null);
					        accumulatorInformation.setAccumulatorValueRelative(false);
					        accumulatorInformationList.add(accumulatorInformation);

					        // don't waiting for the response : set waitingForResponse false
					        request.setWaitingForResponse(false);
					        request.updateAccumulators(hvc.getValue(), accumulatorInformationList, "eBA");
					        // release waiting for the response : set waitingForResponse true
					        request.setWaitingForResponse(true); request.setSuccessfully(true);

							if(new HVCDAOJdbc(dao).saveOneHVC(hvc) > 0) {
								responseCode = 0;
							}
						}
						// rollback
						else {
							balances.clear();
							if(da == 0) balances.add(new BalanceAndDate(da, -volume, null));
							else balances.add(new DedicatedAccount(da, -volume, null));

							if(request.updateBalanceAndDate(hvc.getValue(), balances, "HVC", (hvc.getSegment() + ""), "eBA"));
							else {
								if(request.isSuccessfully()) new RollBackDAOJdbc(dao).saveOneRollBack(new RollBack(0, 1, hvc.getSegment(), hvc.getValue(), hvc.getValue(), null));
								else new RollBackDAOJdbc(dao).saveOneRollBack(new RollBack(0, -1, hvc.getSegment(), hvc.getValue(), hvc.getValue(), null));
							}
						}
					}
					// rollback
					else {
						if(request.isSuccessfully()) new RollBackDAOJdbc(dao).saveOneRollBack(new RollBack(0, 1, hvc.getSegment(), hvc.getValue(), hvc.getValue(), null));
						else new RollBackDAOJdbc(dao).saveOneRollBack(new RollBack(0, -1, hvc.getSegment(), hvc.getValue(), hvc.getValue(), null));
					}
				}

			} catch(Throwable th) {

			} finally {
				if(responseCode >= 0) {
					// unlock
					(new HVCDAOJdbc(dao)).locking(hvc, false);

					if(request.isWaitingForResponse()) {
						if(request.isSuccessfully());
						else throw new AirAvailabilityException();
					}
				}
			}
		}
		else {
			if(request.isWaitingForResponse()) {
				if(request.isSuccessfully());
				else throw new AirAvailabilityException();				
			}
		}

		return responseCode;
	}

}
