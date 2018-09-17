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
	/**
	 * 
	Filtering items : It is actually very easy to tell Spring Batch not to continue processing an item.  To do so, instead of the ItemProcessor returning an item, it returns null.
	
	let’s look at the filtering rules for item processors:
		If  the  process  method  returns  null,  Spring  Batch  filters  out  the  item  and  it won’t go to the item writer.
		Filtering is different from skipping.
		An exception thrown by an item processor results in a skip (if you configured the skip strategy accordingly).
	
	The basic contract for filtering is clear, but we must point out the distinction between filtering and skipping:
		Filtering means that Spring Batch shouldn’t write a given record. For example, the item writer can’t handle a record.
		Skipping  means that a given record is invalid. For example, the format of a phone number is invalid.
	*/
	/* Filtering items : It is actually very easy to tell Spring Batch not to continue processing an item.  To do so, instead of the ItemProcessor returning an item, it returns null.*/
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
			if(ex instanceof AirAvailabilityException) throw (AirAvailabilityException)ex;

		} catch(Throwable th) {
			if(th instanceof AirAvailabilityException) throw (AirAvailabilityException)th;
		}

		return null;
	}

}
