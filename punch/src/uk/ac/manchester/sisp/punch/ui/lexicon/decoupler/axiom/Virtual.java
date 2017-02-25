package uk.ac.manchester.sisp.punch.ui.lexicon.decoupler.axiom;

import uk.ac.manchester.sisp.punch.ui.lexicon.courier.ILexiconCourier;
import uk.ac.manchester.sisp.punch.ui.lexicon.functions.courier.IFunctionCourier;
import uk.ac.manchester.sisp.punch.ui.lexicon.parameter.contact.IContact;

public abstract class Virtual extends Axiom<IContact.Link> {  /** TODO: Likely abstract. **/
	
	/* Define the default Serialization UID. */
	private static final long serialVersionUID = 1L;

	public Virtual(final int pX, final int pY) { super(pX, pY); }
	
	/* Courier Dispatch Operations. */
	@Override public final <T> void onCourierDispatch(final IFunctionCourier<T> pFunctionCourier, final T pCourierPackage) { pFunctionCourier.onCourierTransit(this, pCourierPackage); }
	@Override public final <T> void onCourierDispatch(final  ILexiconCourier<T>  pLexiconCourier, final T pCourierPackage) {  pLexiconCourier.onCourierTransit(this, pCourierPackage); }
	
}