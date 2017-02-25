package uk.ac.manchester.sisp.punch.ui.lexicon.simulation.branching;

import uk.ac.manchester.sisp.punch.ui.lexicon.compilation.Compilation;
import uk.ac.manchester.sisp.punch.ui.lexicon.coupling.parsing.GParser;
import uk.ac.manchester.sisp.punch.ui.lexicon.coupling.parsing.GState;
import uk.ac.manchester.sisp.punch.ui.lexicon.coupling.parsing.branching.IBranch;
import uk.ac.manchester.sisp.punch.ui.lexicon.data.global.IValue;
import uk.ac.manchester.sisp.punch.ui.lexicon.simulation.SDriver;
import uk.ac.manchester.sisp.ribbon.common.IVec2;
import uk.ac.manchester.sisp.ribbon.utils.DataUtils;
import uk.ac.manchester.sisp.ribbon.utils.MathUtils;

public final class SelectBranch implements IBranch<SDriver>, IValue.W<Boolean> {
	
	/* Member Variables. */
	private final IVec2.I   mOffset;
	private       Boolean   mValue;
	
	public <U extends GState<SDriver, ?, ?>> SelectBranch(final IVec2.I pLocalOffset, final GParser<SDriver, U> pGParser, final U pGState, final Compilation pCompilation) { 
		/* Initialize Member Variables. */
		this.mOffset = pLocalOffset;
		this.mValue  = null;
		/* Initialize the ArrayBranch. */
		this.next(pGParser, pGState, pCompilation);
	}

	@Override
	public final boolean isAmbivalent() { 
		return true;
	}

	@Override
	public final void logic() { 
		/* Do nothing. */
	}

	@Override
	public final boolean isEnabled() { 
		/* This branch is always active. */
		return true;
	}

	@Override
	public final <U extends GState<SDriver, ?, ?>> void next(final GParser<SDriver, U> pGParser, final U pGState, final Compilation pCompilation) {
		/* Move to the Contact's Location. */
		MathUtils.onSupplyOffset  (pCompilation, this.getOffset());
			/* Fetch the Driver. */
			final SDriver lSDriver = pGParser.onFetchDriver(pCompilation.getAbsoluteIndex());
			/* Fetch the Boolean. */
			this.setValue((Boolean)lSDriver.getValue());
			/* Update the execution Index. */
			pGState.setIndex(DataUtils.booleanToInt(this.getValue()));
		/* Withdraw from the Contact's Location. */
		MathUtils.onWithdrawOffset(pCompilation, this.getOffset());
	}
	
	private final IVec2.I getOffset() { 
		return this.mOffset;
	}
	
	@Override
	public final void setValue(final Boolean pValue) { 
		this.mValue = pValue;
	}

	@Override
	public final Boolean getValue() {
		return this.mValue;
	}
	
}