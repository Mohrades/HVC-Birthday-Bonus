package jobs;

import org.springframework.batch.item.ItemProcessor;
import dao.DAO;
import domain.models.HVC;
import exceptions.AirAvailabilityException;
import product.ProductActions;
import product.ProductProperties;

public class DefaultBonusProcessor implements ItemProcessor<HVC, HVC> {

	private DAO dao;

	private ProductProperties productProperties;

	public DefaultBonusProcessor() {
		
	}

	public DAO getDao() {
		return dao;
	}

	public void setDao(DAO dao) {
		this.dao = dao;
	}

	public ProductProperties getProductProperties() {
		return productProperties;
	}

	public void setProductProperties(ProductProperties productProperties) {
		this.productProperties = productProperties;
	}

	@Override
	public HVC process(HVC hvc) throws AirAvailabilityException {
		// TODO Auto-generated method stub

		try {
			int offer = Integer.parseInt(productProperties.getOffer_id().get(hvc.getSegment() - 1));
			int da = productProperties.getVoice_da();
			long voice_volume = Long.parseLong(productProperties.getVoice_volume().get(hvc.getSegment() - 1));

			// set bonus choice (data or voice) : in this case equals voice
			hvc.setBonus(1);

			if((new ProductActions(productProperties)).doActions(dao, hvc, offer, da, voice_volume, productProperties.getAccumulator_id()) == 0) {
				return hvc;
			}

			/*Date expires = new Date();
			expires.setDate(expires.getDate() + 1);
			expires.setSeconds(59);expires.setMinutes(59);expires.setHours(23);
			// set bonus expiry date
			hvc.setBonus_expires_in(expires);

			if(new HVCDAOJdbc(dao).saveOneHVC(hvc) > 0) {
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
			}*/

		} catch(AirAvailabilityException ex) {
			throw ex;
			
		} catch(Throwable th) {
			
		}

		return null;
	}

}
