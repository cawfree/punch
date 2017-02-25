package uk.ac.manchester.sisp.punch.ui.lexicon.decoupler.axiom;

import uk.ac.manchester.sisp.punch.ui.color.global.ColorGlobal;
import uk.ac.manchester.sisp.punch.ui.lexicon.IAtomic;
import uk.ac.manchester.sisp.punch.ui.lexicon.courier.ILexiconCourier;
import uk.ac.manchester.sisp.punch.ui.lexicon.data.global.EDataDirection;
import uk.ac.manchester.sisp.punch.ui.lexicon.decoupler.Decoupler;
import uk.ac.manchester.sisp.punch.ui.lexicon.functions.courier.IFunctionCourier;
import uk.ac.manchester.sisp.punch.ui.lexicon.parameter.contact.IContact;

/* An indivisible block of computation with respect to the caller. */
public abstract class Axiom <U extends IContact<?>> extends Decoupler<U> implements ILexiconCourier.Dispatch, IAtomic, IFunctionCourier.Dispatch { /** TODO: Abstract. **/
	
	/* Define the default Serialization UID. */
	private static final long serialVersionUID = 1L;
	
	/* Constructor. */
	public Axiom(final int pX, final int pY) { super(pX, pY); }
	
	/* Courier Dispatch Operations. */
	@Override public <T> void onCourierDispatch(final ILexiconCourier<T> pLexiconCourier, final T pCourierPackage) { pLexiconCourier.onCourierTransit(this, pCourierPackage); }
	
	/* Define that the Axiom supports both Input and Output Contacts. */
	@Override public final EDataDirection getDataDirection() { return EDataDirection.BIDIRECTIONAL;  }
	@Override public final float[]                getColor() { return    ColorGlobal.RGBA_FILL_CORE; }
}