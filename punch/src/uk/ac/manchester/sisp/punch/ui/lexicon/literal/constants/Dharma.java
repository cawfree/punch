package uk.ac.manchester.sisp.punch.ui.lexicon.literal.constants;

import uk.ac.manchester.sisp.punch.PunchModel;
import uk.ac.manchester.sisp.punch.ui.lexicon.courier.ILexiconCourier;
import uk.ac.manchester.sisp.punch.ui.lexicon.data.IDataType;
import uk.ac.manchester.sisp.punch.ui.lexicon.data.global.DataGlobal;
import uk.ac.manchester.sisp.punch.ui.lexicon.data.global.EDataDirection;
import uk.ac.manchester.sisp.punch.ui.lexicon.literal.ILiteral;

public final class Dharma extends ILiteral.Input<Boolean> { 
	
	/* Define the default Serialization UID. */
	private static final long serialVersionUID = 1L;

	/* Static Declarations. */
	private static final String FIELD_VALUE_TRUE  = "T";
	private static final String FIELD_VALUE_FALSE = "F";
	
	/* Determines the appropriate String data to use to represent true or false values. */
	private static final String onConvertValue(final boolean pValue) {
		/* Select the staticized constant based on the value. */
		return (pValue ? Dharma.FIELD_VALUE_TRUE : Dharma.FIELD_VALUE_FALSE);
	}
	
	public Dharma(final int pX, final int pY, final boolean pValue, final PunchModel pPunchModel) {
		super(pX, pY, Dharma.onConvertValue(pValue), pPunchModel);
	}
	
	/* Generic Courier Dispatch. */
	@Override public final <U> void onCourierDispatch(final ILexiconCourier<U> pLexiconCourier, final U pCourierPackage) { pLexiconCourier.onCourierTransit(this, pCourierPackage); }

	@Override
	public final IDataType<Boolean> getDataType(final EDataDirection pDataDirection) { 
		/* A Dharma will remain this type regardless of the DataDirection. */
		return DataGlobal.TYPE_DHARMA;
	}
	
	/* Setter. */
	public final void setValue(final boolean pValue) {
		/* Update the Field. */
		this.getField().setText(Dharma.onConvertValue(pValue));
	}
	
	/* Data getter and setters. */
	@Override public final Boolean getValue() { 
		/* Return the Field's value. */
		return this.getField().getText().equals(Dharma.FIELD_VALUE_TRUE);
	}
	
}