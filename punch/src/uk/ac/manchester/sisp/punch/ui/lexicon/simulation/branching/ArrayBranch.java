package uk.ac.manchester.sisp.punch.ui.lexicon.simulation.branching;

import java.util.Collections;
import java.util.List;

import uk.ac.manchester.sisp.punch.ui.lexicon.compilation.Compilation;
import uk.ac.manchester.sisp.punch.ui.lexicon.coupling.parsing.GParser;
import uk.ac.manchester.sisp.punch.ui.lexicon.coupling.parsing.GState;
import uk.ac.manchester.sisp.punch.ui.lexicon.coupling.parsing.branching.IBranch;
import uk.ac.manchester.sisp.punch.ui.lexicon.data.global.EDataDirection;
import uk.ac.manchester.sisp.punch.ui.lexicon.data.global.IValue;
import uk.ac.manchester.sisp.punch.ui.lexicon.parameter.contact.constants.Iteration;
import uk.ac.manchester.sisp.punch.ui.lexicon.simulation.SDriver;
import uk.ac.manchester.sisp.ribbon.common.IVec2;
import uk.ac.manchester.sisp.ribbon.utils.MathUtils;

public final class ArrayBranch implements IBranch<SDriver>, IValue<List<?>> {
	
	/* Member Variables. */
	private final Iteration mIteration;
	private final List<?>   mValue;
	private final IVec2.I   mOffset;
	private       int       mIndex;
	
	public <U extends GState<SDriver, ?, ?>> ArrayBranch(final Iteration pIteration, final List<?> pValue, final IVec2.I pLocalOffset, final GParser<SDriver, U> pGParser, final U pGState, final Compilation pCompilation) { 
		/* Initialize Member Variables. */
		this.mIteration = pIteration;
		this.mValue     = Collections.unmodifiableList(pValue);
		this.mOffset    = pLocalOffset;
		this.mIndex     = 0;
		/* Initialize the ArrayBranch by indexing the Array. We don't wish to trash the driver since we've entered a new scope. */
		this.onIndexArray(pGParser, pGState, pCompilation, false);
	}

	@Override
	public final boolean isAmbivalent() { 
		return false;
	}

	@Override
	public final boolean isEnabled() { 
		/* The Branch is only enabled whilst we may iterate the Array. */
		return (this.getIndex() < this.getValue().size());
	}

	@Override
	public final void logic() {
		/* Increment the Offset. */
		this.mIndex++;
	}

	@Override
	public final <U extends GState<SDriver, ?, ?>> void next(final GParser<SDriver, U> pGParser, final U pGState, final Compilation pCompilation) {
		/* Index the Array. Assure we trash the old driver. */
		this.onIndexArray(pGParser, pGState, pCompilation, true);
	}
	
	private final <U extends GState<SDriver, ?, ?>> void onIndexArray(final GParser<SDriver, U> pGParser, final U pGState, final Compilation pCompilation, final boolean pIsTrashDriver) { 
		/* Move to the Contact's Location. */
		MathUtils.onSupplyOffset  (pCompilation, this.getOffset());
			/* Determine whether we wish to trash the old driver. */
			if(pIsTrashDriver) { 
				/* Trash the Driver at the Index. */
				pGParser.onTrash(pCompilation.getAbsoluteIndex(), pGState, pCompilation);
			}
			/* Allocate a new SDriver which has fetched the Array Data at the current Offset. */
			final SDriver lSDriver = new SDriver(this.getIteration(), this.getIteration().getDataType(EDataDirection.SOURCE), this.getValue().get(this.getIndex()));
			/* Write the Driver to the Stack. */
			pGParser.getStateStack().peek().getDriverMap().put(pCompilation.getAbsoluteIndex(), lSDriver);
		/* Withdraw from the Contact's Location. */
		MathUtils.onWithdrawOffset(pCompilation, this.getOffset());
	}
	
	public final Iteration getIteration() { 
		return this.mIteration;
	}

	@Override
	public final List<?> getValue() { 
		return this.mValue;
	}
	
	private final IVec2.I getOffset() { 
		return this.mOffset;
	}
	
	public final int getIndex() { 
		return this.mIndex;
	}
	
}