package uk.ac.manchester.sisp.punch.ui.lexicon.functions;

import uk.ac.manchester.sisp.punch.ui.lexicon.data.global.DataGlobal;
import uk.ac.manchester.sisp.punch.ui.lexicon.data.global.EDataDirection;
import uk.ac.manchester.sisp.punch.ui.lexicon.decoupler.axiom.Axiom;
import uk.ac.manchester.sisp.punch.ui.lexicon.functions.courier.IFunctionCourier;
import uk.ac.manchester.sisp.punch.ui.lexicon.parameter.contact.IContact;
import uk.ac.manchester.sisp.ribbon.utils.ResourceUtils;

/* A core axiomatic representation of Punch's two-input NAND gate. */
public final class Elapsed extends Axiom<IContact<?>> { 
	
	/* Define the default Serialization UID. */
	private static final long serialVersionUID = 1L;
	
	/* Member Variables. */
	private IContact<?> mTime;
	
	public Elapsed(final int pX, final int pY) {
		super(pX, pY);
		/* Initialize Member Variables. */
		this.mTime = new IContact.Impl(0, 0, ResourceUtils.getResource(this.getClass().getClassLoader(), "res/icon/contact/time.svg"),  DataGlobal.TYPE_F32, EDataDirection.SOURCE);
		/* Add the Output. */
		this.getUIElements().add(this.getResult());
	}
	
	/* Courier Dispatch Operations. */
	@Override public final <T> void onCourierDispatch(final IFunctionCourier<T> pFunctionCourier, final T pCourierPackage) { pFunctionCourier.onCourierTransit(this, pCourierPackage); }
	
	/* Input Accessors. */
	public final IContact<?> getResult() { return this.mTime; }
	
}