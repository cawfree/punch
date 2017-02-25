package uk.ac.manchester.sisp.punch.ui.lexicon.simulation.branching;

import uk.ac.manchester.sisp.punch.ui.lexicon.compilation.Compilation;
import uk.ac.manchester.sisp.punch.ui.lexicon.coupling.parsing.GParser;
import uk.ac.manchester.sisp.punch.ui.lexicon.coupling.parsing.GState;
import uk.ac.manchester.sisp.punch.ui.lexicon.coupling.parsing.branching.IBranch;
import uk.ac.manchester.sisp.punch.ui.lexicon.data.global.EDataDirection;
import uk.ac.manchester.sisp.punch.ui.lexicon.data.global.IValue;
import uk.ac.manchester.sisp.punch.ui.lexicon.parameter.contact.constants.Iteration;
import uk.ac.manchester.sisp.punch.ui.lexicon.simulation.SDriver;
import uk.ac.manchester.sisp.ribbon.common.IVec2;
import uk.ac.manchester.sisp.ribbon.utils.DataUtils;
import uk.ac.manchester.sisp.ribbon.utils.MathUtils;

public final class CyclicBranch implements IBranch<SDriver>, IValue<Boolean> {
	
	/* Member Variables. */
	private final Iteration mIteration;
	private final IVec2.I   mOffset;
	private final Boolean   mValue;
	private       int       mCount;
	
	public <U extends GState<SDriver, ?, ?>> CyclicBranch(final Iteration pIteration, final Boolean pValue, final IVec2.I pLocalOffset, final GParser<SDriver, U> pGParser, final U pGState, final Compilation pCompilation) { 
		/* Initialize Member Variables. */
		this.mIteration = pIteration;
		this.mValue     = pValue;
		this.mOffset    = pLocalOffset;
		this.mCount     = 0;
		/* Initialize the ArrayBranch by buffering the Count. There's no driver we wish to trash at this point since we've entered a new scope. */
		this.onBufferCount(pGParser, pGState, pCompilation, false);
	}

	@Override
	public final boolean isAmbivalent() { 
		return false;
	}

	@Override
	public final void logic() {
		/* Increment the Count. */
		this.mCount++;
	}

	@Override
	public final boolean isEnabled() { 
		/* The driving value defines whether the CyclicBranch is enabled. */
		return (boolean)this.getValue();
	}

	@Override
	public final <U extends GState<SDriver, ?, ?>> void next(final GParser<SDriver, U> pGParser, final U pGState, final Compilation pCompilation) {
		/* Buffer the Count. Ensure we trash the old driver. */
		this.onBufferCount(pGParser, pGState, pCompilation, true);
	}
	
	private final <U extends GState<SDriver, ?, ?>> void onBufferCount(final GParser<SDriver, U> pGParser, final U pGState, final Compilation pCompilation, final boolean pIsTrashDriver) { 
		/* Move to the Contact's Location. */
		MathUtils.onSupplyOffset  (pCompilation, this.getOffset());
			/* Determine whether to trash the Driver. */
			if(pIsTrashDriver) { 
				/* Trash the Driver at the Index. */
				pGParser.onTrash(pCompilation.getAbsoluteIndex(), pGState, pCompilation);
			}
			/* Write the Count. */
			final SDriver lSDriver = new SDriver(this.getIteration(), this.getIteration().getDataType(EDataDirection.SOURCE), DataUtils.getCachedInteger(this.getCount()));
			/* Write the Driver to the Stack. */
			pGParser.getStateStack().peek().getDriverMap().put(pCompilation.getAbsoluteIndex(), lSDriver);
		/* Withdraw from the Contact's Location. */
		MathUtils.onWithdrawOffset(pCompilation, this.getOffset());
	}
	
	private final Iteration getIteration() { 
		return this.mIteration;
	}
	
	private final IVec2.I getOffset() { 
		return this.mOffset;
	}

	@Override
	public final Boolean getValue() {
		return this.mValue;
	}
	
	private final int getCount() { 
		return this.mCount;
	}
	
}