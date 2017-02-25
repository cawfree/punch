package uk.ac.manchester.sisp.punch.ui.lexicon.functions;

import uk.ac.manchester.sisp.punch.ui.lexicon.data.global.DataGlobal;
import uk.ac.manchester.sisp.punch.ui.lexicon.data.global.EDataDirection;
import uk.ac.manchester.sisp.punch.ui.lexicon.decoupler.axiom.Axiom;
import uk.ac.manchester.sisp.punch.ui.lexicon.functions.courier.IFunctionCourier;
import uk.ac.manchester.sisp.punch.ui.lexicon.parameter.contact.IContact;
import uk.ac.manchester.sisp.ribbon.utils.ResourceUtils;

/* A core axiomatic representation of Punch's two-input NAND gate. */
public final class Nand extends Axiom<IContact<?>> { 
	
	/* Define the default Serialization UID. */
	private static final long serialVersionUID = 1L;
	
	/* Member Variables. */
	private IContact<?> mA;
	private IContact<?> mB;
	private IContact<?> mResult;
	
	public Nand(final int pX, final int pY) {
		super(pX, pY);
		/* Initialize Member Variables. */
		this.mA      = new IContact.Impl(0, 0, ResourceUtils.getResource(this.getClass().getClassLoader(), "res/icon/contact/unary.svg"), DataGlobal.TYPE_DHARMA, EDataDirection.SINK);
		this.mB      = new IContact.Impl(0, 0, ResourceUtils.getResource(this.getClass().getClassLoader(), "res/icon/contact/unary.svg"), DataGlobal.TYPE_DHARMA, EDataDirection.SINK);
		this.mResult = new IContact.Impl(0, 0, ResourceUtils.getResource(this.getClass().getClassLoader(), "res/icon/contact/nand.svg"),  DataGlobal.TYPE_DHARMA, EDataDirection.SOURCE);
		/* Add the Inputs. */
		this.getUIElements().add(this.getA());
		this.getUIElements().add(this.getB());
		/* Add the Output. */
		this.getUIElements().add(this.getResult());
	}
	
	/* Courier Dispatch Operations. */
	@Override public final <T> void onCourierDispatch(final IFunctionCourier<T> pFunctionCourier, final T pCourierPackage) { pFunctionCourier.onCourierTransit(this, pCourierPackage); }
	
	/* Input Accessors. */
	public final IContact<?> getA()      { return this.mA;      }
	public final IContact<?> getB()      { return this.mB;      }
	public final IContact<?> getResult() { return this.mResult; }
	
}