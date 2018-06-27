package jobs;

import org.springframework.batch.item.ItemProcessor;
import dao.DAO;
import domain.models.HVConsumer;
import exceptions.AirAvailabilityException;
import product.ProductActions;
import product.ProductProperties;

public class DefaultBonusProcessor implements ItemProcessor<HVConsumer, HVConsumer> {

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
	public HVConsumer process(HVConsumer hvc) throws AirAvailabilityException {
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

		} catch(AirAvailabilityException ex) {
			throw ex;
			
		} catch(Exception ex) {
			if(ex instanceof AirAvailabilityException) throw ex;

		} catch(Throwable th) {
			if(th instanceof AirAvailabilityException) throw th;
		}

		return null;
	}

}
