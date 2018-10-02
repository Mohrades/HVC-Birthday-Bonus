package product;

import java.util.Date;
import java.util.HashSet;

import connexions.AIRRequest;
import dao.DAO;
import dao.queries.JdbcHVConsumerDao;
import dao.queries.JdbcRollBackDao;
import domain.models.HVConsumer;
import domain.models.RollBack;
import exceptions.AirAvailabilityException;
import util.AccumulatorInformation;
import util.BalanceAndDate;
import util.DedicatedAccount;

public class ProductActions {

	private ProductProperties productProperties;

	public ProductActions(ProductProperties productProperties) {
		this.productProperties = productProperties;
	}

	@SuppressWarnings("deprecation")
	public int doActions(DAO dao, HVConsumer hvc, int offer, int da, long volume, int accumulator) throws AirAvailabilityException {
		AIRRequest request = new AIRRequest(productProperties.getAir_hosts(), productProperties.getAir_io_sleep(), productProperties.getAir_io_timeout(), productProperties.getAir_io_threshold(), productProperties.getAir_preferred_host());

		int responseCode = -1;

		// attempts
		int retry = 0;

		while(productProperties.getAir_preferred_host() == -1) {
			if(retry >= 3) throw new AirAvailabilityException();

			productProperties.setAir_preferred_host((byte) (new AIRRequest(productProperties.getAir_hosts(), productProperties.getAir_io_sleep(), productProperties.getAir_io_timeout(), productProperties.getAir_io_threshold(), productProperties.getAir_preferred_host())).testConnection(productProperties.getAir_test_connection_msisdn(), productProperties.getAir_preferred_host()));
			retry++;
		}

		retry = 0;

		try {
			Date expires = new Date();
			expires.setDate(expires.getDate() + productProperties.getDa_expires_in_days());
			expires.setSeconds(59); expires.setMinutes(59); expires.setHours(23);
			// set bonus expiry date
			hvc.setBonus_expires_in(expires);

			if(new JdbcHVConsumerDao(dao).locking(hvc, true) > 0) {
				responseCode = 1;

				HashSet<BalanceAndDate> balances = new HashSet<BalanceAndDate>();
				if(da == 0) balances.add(new BalanceAndDate(da, volume, null));
				else balances.add(new DedicatedAccount(da, volume, expires));

				// update Anumber Balance
				if(request.updateBalanceAndDate(hvc.getValue(), balances, "HVC", (hvc.getSegment() + ""), "eBA")) {
					// update Anumber Offer
					if((offer == 0) || (request.updateOffer(hvc.getValue(), offer, null, expires, null, "eBA"))) {
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

						if(new JdbcHVConsumerDao(dao).saveOneHVConsumer(hvc) > 0) {
							responseCode = 0;
						}
						else {
							responseCode = -1;
							new JdbcRollBackDao(dao).saveOneRollBack(new RollBack(0, 3, hvc.getSegment(), hvc.getValue(), hvc.getValue(), null));
						}
					}
					// rollback
					else {
						if(request.isSuccessfully()) {
							balances.clear();
							if(da == 0) balances.add(new BalanceAndDate(da, -volume, null));
							else balances.add(new DedicatedAccount(da, -volume, expires));

							if(request.updateBalanceAndDate(hvc.getValue(), balances, "HVC", (hvc.getSegment() + ""), "eBA"));
							else {
								if(request.isSuccessfully()) new JdbcRollBackDao(dao).saveOneRollBack(new RollBack(0, 1, hvc.getSegment(), hvc.getValue(), hvc.getValue(), null));
								else new JdbcRollBackDao(dao).saveOneRollBack(new RollBack(0, -1, hvc.getSegment(), hvc.getValue(), hvc.getValue(), null));
							}
						}
						else {
							new JdbcRollBackDao(dao).saveOneRollBack(new RollBack(0, -2, hvc.getSegment(), hvc.getValue(), hvc.getValue(), null));
						}
					}
				}
				// rollback
				else {
					if(request.isSuccessfully()) new JdbcRollBackDao(dao).saveOneRollBack(new RollBack(0, 1, hvc.getSegment(), hvc.getValue(), hvc.getValue(), null));
					else new JdbcRollBackDao(dao).saveOneRollBack(new RollBack(0, -1, hvc.getSegment(), hvc.getValue(), hvc.getValue(), null));
				}
			}

		} catch(Throwable th) {

		} finally {
			if(responseCode >= 0) {
				// unlock
				(new JdbcHVConsumerDao(dao)).locking(hvc, false);

				if(request.isWaitingForResponse()) {
					if(request.isSuccessfully());
					else {
						productProperties.setAir_preferred_host((byte) (new AIRRequest(productProperties.getAir_hosts(), productProperties.getAir_io_sleep(), productProperties.getAir_io_timeout(), productProperties.getAir_io_threshold(), productProperties.getAir_preferred_host())).testConnection(productProperties.getAir_test_connection_msisdn(), productProperties.getAir_preferred_host()));
						throw new AirAvailabilityException();
					}
				}
			}
		}

		return responseCode;
	}

}
