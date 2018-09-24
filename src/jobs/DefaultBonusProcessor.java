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
	
	let�s look at the filtering rules for item processors:
		If  the  process  method  returns  null,  Spring  Batch  filters  out  the  item  and  it won�t go to the item writer.
		Filtering is different from skipping.
		An exception thrown by an item processor results in a skip (if you configured the skip strategy accordingly).
	
	The basic contract for filtering is clear, but we must point out the distinction between filtering and skipping:
		Filtering means that Spring Batch shouldn�t write a given record. For example, the item writer can�t handle a record.
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
			/**
			 *
			On peut s'assurer qu'un objet est bien une instance d'une classe donn�e en recourant � l'op�rateur instanceOf.
			Par exemple, l'expression p instanceOf Point vaudra true si p est (exactement) de type Point.
			Mais ce test ne reponds pas � nos probl�matiques ici. Et donc, nous n'utilisons pas cette m�thode.

			Les conversions explicites de r�f�rences:
			Nous avons largement insist� sur la compatibilit� qui existe entre r�f�rence � un objet d�un type donn� et r�f�rence � un objet d'un type ascendant.
			Comme on peut s'y attendre, la compatibilit� n'a pas lieu dans le sens inverse. Consid�rons cet exemple, fond� sur nos classes Point et Pointcol habituelles :
			class Point { ..... }
			class Pointcol extends Point { ..... }
			   .....
			Pointcol pc ;
			pc = new Point (...) ;    // erreur de compilation
			Si l'affectation �tait l�gale, un simple appel tel que pc.colore(...) conduirait � attribuer une
			couleur � un objet de type Point, ce qui poserait quelques probl�mes � l'ex�cution...
			Mais consid�rons cette situation :
			Point p ;
			Pointcol pc1 = new Pointcol(...), pc2 ;
			   .....
			p = pc1 ;   // p contient la r�f�rence � un objet de type Pointcol
			   .....
			pc2 = p ;   // refus� en compilation
			L'affectation pc2 = p est tout naturellement refus�e. Cependant, nous sommes certains que p contient bien ici la r�f�rence � un objet de type Pointcol.
			En fait, nous pouvons forcer le compilateur � r�aliser la conversion correspondante en utilisant l'op�rateur de cast d�j� rencontr� pour les types primitifs. Ici, nous �crirons simplement :
			pc2 = (Pointcol) p ;   // accept� en compilation
			Toutefois, lors de l'ex�cution, Java s'assurera que p contient bien une r�f�rence � un objet de type Pointcol (ou d�riv�) afin de ne pas compromettre la bonne ex�cution du programme.
			Dans le cas contraire, on obtiendra une exception ClassCastException qui, si elle n'est pas trait�e, conduira � un arr�t de l'ex�cution.

			!! CECI EST JUSTE UNE INFORMATION A SE RAPPELER

			 */

			/**
			 *
			Determines if the class or interface represented by this Class object is either the same as, or is a superclass or superinterface of, the class or interface represented by the specified Class parameter. It returns true if so; otherwise it returns false.
			If this Class object represents a primitive type, this method returns true if the specified Class parameter is exactly this Class object; otherwise it returns false.
			Specifically, this method tests whether the type represented by the specified Class parameter can be converted to the type represented by this Class object via an identity conversion or via a widening reference conversion.

			 */
			/*if(ex instanceof AirAvailabilityException) throw (AirAvailabilityException)ex;*/
			try {
				AirAvailabilityException exceptionClass = (AirAvailabilityException)ex;
				throw exceptionClass;

			} catch(AirAvailabilityException exception) {
				throw exception;

			} catch(NullPointerException exception) {

			} catch(ClassCastException exception) {

			} catch(Exception exception) {

			} catch(Throwable th) {

			}

		} catch(Throwable th) {
			/**
			 *
			On peut s'assurer qu'un objet est bien une instance d'une classe donn�e en recourant � l'op�rateur instanceOf.
			Par exemple, l'expression p instanceOf Point vaudra true si p est (exactement) de type Point.
			Mais ce test ne reponds pas � nos probl�matiques ici. Et donc, nous n'utilisons pas cette m�thode.

			Les conversions explicites de r�f�rences:
			Nous avons largement insist� sur la compatibilit� qui existe entre r�f�rence � un objet d�un type donn� et r�f�rence � un objet d'un type ascendant.
			Comme on peut s'y attendre, la compatibilit� n'a pas lieu dans le sens inverse. Consid�rons cet exemple, fond� sur nos classes Point et Pointcol habituelles :
			class Point { ..... }
			class Pointcol extends Point { ..... }
			   .....
			Pointcol pc ;
			pc = new Point (...) ;    // erreur de compilation
			Si l'affectation �tait l�gale, un simple appel tel que pc.colore(...) conduirait � attribuer une
			couleur � un objet de type Point, ce qui poserait quelques probl�mes � l'ex�cution...
			Mais consid�rons cette situation :
			Point p ;
			Pointcol pc1 = new Pointcol(...), pc2 ;
			   .....
			p = pc1 ;   // p contient la r�f�rence � un objet de type Pointcol
			   .....
			pc2 = p ;   // refus� en compilation
			L'affectation pc2 = p est tout naturellement refus�e. Cependant, nous sommes certains que p contient bien ici la r�f�rence � un objet de type Pointcol.
			En fait, nous pouvons forcer le compilateur � r�aliser la conversion correspondante en utilisant l'op�rateur de cast d�j� rencontr� pour les types primitifs. Ici, nous �crirons simplement :
			pc2 = (Pointcol) p ;   // accept� en compilation
			Toutefois, lors de l'ex�cution, Java s'assurera que p contient bien une r�f�rence � un objet de type Pointcol (ou d�riv�) afin de ne pas compromettre la bonne ex�cution du programme.
			Dans le cas contraire, on obtiendra une exception ClassCastException qui, si elle n'est pas trait�e, conduira � un arr�t de l'ex�cution.

			!! CECI EST JUSTE UNE INFORMATION A SE RAPPELER

			 */

			/**
			 *
			Determines if the class or interface represented by this Class object is either the same as, or is a superclass or superinterface of, the class or interface represented by the specified Class parameter. It returns true if so; otherwise it returns false.
			If this Class object represents a primitive type, this method returns true if the specified Class parameter is exactly this Class object; otherwise it returns false.
			Specifically, this method tests whether the type represented by the specified Class parameter can be converted to the type represented by this Class object via an identity conversion or via a widening reference conversion.

			 */
			/*if(th instanceof AirAvailabilityException) throw (AirAvailabilityException)th;*/
			try {
				AirAvailabilityException exceptionClass = (AirAvailabilityException)th;
				throw exceptionClass;

			} catch(AirAvailabilityException exception) {
				throw exception;

			} catch(NullPointerException exception) {

			} catch(ClassCastException exception) {

			} catch(Exception exception) {

			} catch(Throwable throwable) {

			}
		}

		return null;
	}

}
