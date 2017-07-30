package uk.ac.manchester.sisp.punch.ui.lexicon.decoupler.directional;

import java.util.List;

import uk.ac.manchester.sisp.punch.ui.IUIElement;
import uk.ac.manchester.sisp.punch.ui.distribution.IDistribution;
import uk.ac.manchester.sisp.punch.ui.lexicon.ILexical;
import uk.ac.manchester.sisp.punch.ui.lexicon.courier.ILexiconCourier;
import uk.ac.manchester.sisp.punch.ui.lexicon.data.global.EDataDirection;
import uk.ac.manchester.sisp.punch.ui.lexicon.decoupler.Decoupler;
import uk.ac.manchester.sisp.punch.ui.lexicon.parameter.contact.IContact;
import uk.ac.manchester.sisp.punch.ui.update.UIUpdateDispatcher;

public final class SinkDecoupler extends Decoupler<IContact<IContact.Link>> implements ILexical {

	/* Define the default Serialization UID. */
	private static final long serialVersionUID = 1L;
	
	public SinkDecoupler(final int pX, final int pY) {
		super(pX, pY);
	}
	
	/* Courier Dispatch Operations. */
	@Override public final <T> void onCourierDispatch(final ILexiconCourier<T> pLexiconCourier, final T pCourierPackage) { pLexiconCourier.onCourierTransit(this, pCourierPackage); }

	@Override public final EDataDirection getDataDirection() { return EDataDirection.SINK; }
	
	@Override
	public final IDistribution onFetchDistribution(final List<IUIElement> pHierarchy, final UIUpdateDispatcher pUIUpdateDispatcher) {
		/* Directional Decouplers depend upon a Separator's distribution. */
		return null;
	}
	
}