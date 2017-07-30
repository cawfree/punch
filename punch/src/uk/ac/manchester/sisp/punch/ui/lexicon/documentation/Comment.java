package uk.ac.manchester.sisp.punch.ui.lexicon.documentation;

import java.io.File;

import uk.ac.manchester.sisp.punch.PunchModel;
import uk.ac.manchester.sisp.punch.ui.core.Field;
import uk.ac.manchester.sisp.punch.ui.lexicon.ILexical;
import uk.ac.manchester.sisp.punch.ui.lexicon.courier.ILexiconCourier;

public final class Comment extends Field implements ILexical {

	/* Define the default Serialization UID. */
	private static final long serialVersionUID = 1L;
	
	public Comment(final int pX, final int pY, final File pFont, final String pText, final float pPointSize, final float[] pColor, final PunchModel pPunchModel) {
		super(pX, pY, pFont, pText, pPointSize, pColor, pPunchModel);
	}

	@Override
	public final boolean isMultiline() {
		return true;
	}

	@Override public final <T> void onCourierDispatch(final ILexiconCourier<T> pCourier, final T pCourierPackage) { pCourier.onCourierTransit(this, pCourierPackage); }

}