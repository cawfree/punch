package uk.ac.manchester.sisp.punch.ui.lexicon.simulation;

import uk.ac.manchester.sisp.punch.ui.lexicon.coupling.parsing.IDriver;
import uk.ac.manchester.sisp.punch.ui.lexicon.data.IDataType;
import uk.ac.manchester.sisp.punch.ui.lexicon.data.global.IDataConduit;
import uk.ac.manchester.sisp.punch.ui.lexicon.data.global.IValue;

public final class SDriver extends IDriver.Impl implements IValue<Object> {
	
	/* Member Variables. */
	private final Object mValue;
	
	public SDriver(final IDataConduit pDataConduit, final IDataType<?> pDataType, final Object pValue) {
		super(pDataConduit, pDataType);
		/* Initialize Member Variables. */
		this.mValue = pValue;
	}

	@Override
	public final Object getValue() { 
		/* Return the associated Value. */
		return this.mValue;
	}
	
}