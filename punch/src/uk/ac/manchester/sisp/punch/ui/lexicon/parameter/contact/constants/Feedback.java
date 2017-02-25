package uk.ac.manchester.sisp.punch.ui.lexicon.parameter.contact.constants;

import uk.ac.manchester.sisp.punch.ui.lexicon.ILexical;
import uk.ac.manchester.sisp.punch.ui.lexicon.courier.ILexiconCourier;
import uk.ac.manchester.sisp.punch.ui.lexicon.data.global.EDataDirection;
import uk.ac.manchester.sisp.punch.ui.lexicon.parameter.contact.IContact;
import uk.ac.manchester.sisp.ribbon.utils.ResourceUtils;

public final class Feedback extends IContact.Dynamic implements ILexical { 
	
	/* Define the default Serialization UID. */
	private static final long serialVersionUID = 1L;
	
	public Feedback(final int pX, final int pY) {
		super(pX, pY, ResourceUtils.getResource(Iteration.class.getClassLoader(), "res/icon/contact/feedback.svg"), EDataDirection.SOURCE);
	}
	
	/* Generic Courier Dispatch. */
	@Override public <T> void onCourierDispatch(final ILexiconCourier<T> pLexiconCourier, final T pCourierPackage) { pLexiconCourier.onCourierTransit(this, pCourierPackage); }
	
}