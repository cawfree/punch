package uk.ac.manchester.sisp.punch.ui.lexicon.functions;

import uk.ac.manchester.sisp.punch.ui.lexicon.data.global.EDataDirection;
import uk.ac.manchester.sisp.punch.ui.lexicon.decoupler.axiom.Axiom;
import uk.ac.manchester.sisp.punch.ui.lexicon.functions.courier.IFunctionCourier;
import uk.ac.manchester.sisp.punch.ui.lexicon.parameter.contact.IContact;
import uk.ac.manchester.sisp.ribbon.utils.ResourceUtils;

/* A core axiomatic representation of Punch's Dharma print function. */
public final class Print extends Axiom<IContact<?>> { 
	
	/* Define the default Serialization UID. */
	private static final long serialVersionUID = 1L;
	
	/* Member Variables. */
	private IContact.Dynamic mA;
	
	public Print(final int pX, final int pY) {
		super(pX, pY);
		/* Initialize Member Variables. */
		this.mA = new IContact.Dynamic(pY, pY, ResourceUtils.getResource(Print.class.getClassLoader(), "res/icon/contact/print.svg"), EDataDirection.SINK);
		/* Add the Input. */
		this.getUIElements().add(this.getA());
	}
	
	/* Courier Dispatch Operations. */
	@Override public final <T> void onCourierDispatch(final IFunctionCourier<T> pFunctionCourier, final T pCourierPackage) { pFunctionCourier.onCourierTransit(this, pCourierPackage); }
	
	/* Input Accessors. */
	public final IContact.Dynamic getA() { return this.mA; }
	
}