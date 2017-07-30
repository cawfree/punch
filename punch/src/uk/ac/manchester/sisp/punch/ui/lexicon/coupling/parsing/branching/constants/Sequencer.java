package uk.ac.manchester.sisp.punch.ui.lexicon.coupling.parsing.branching.constants;

import uk.ac.manchester.sisp.punch.ui.lexicon.compilation.Compilation;
import uk.ac.manchester.sisp.punch.ui.lexicon.coupling.parsing.GParser;
import uk.ac.manchester.sisp.punch.ui.lexicon.coupling.parsing.GState;
import uk.ac.manchester.sisp.punch.ui.lexicon.coupling.parsing.IDriver;
import uk.ac.manchester.sisp.punch.ui.lexicon.coupling.parsing.branching.IBranch;

public final class Sequencer implements IBranch<IDriver> { 
	
	/* Member Variables. */
	private final GState<?, ?, ?> mGState;
	
	public Sequencer(final GState<?, ?, ?> pGState) { 
		/* Initialize Member Variables. */
		this.mGState = pGState;
	}

	@Override
	public final boolean isAmbivalent() { 
		return false;
	}

	@Override
	public final boolean isEnabled() {
		/* Enable the Branch whilst there are further cases to iterate through. */
		return this.getGState().getIndex() < this.getGState().getContents().size();
	}

	@Override
	public final void logic() {
		/* Update the Index to the next case. */
		this.getGState().setIndex(this.getGState().getIndex() + 1);
	}

	@Override
	public final <U extends GState<IDriver, ?, ?>> void next(final GParser<IDriver, U> pGParser, final U pGState, final Compilation pCompilation) { 
		/* Trash the previous drivers. */
		pGParser.onTrashAll(pCompilation);
	}
	
	private final GState<?, ?, ?> getGState() { 
		return this.mGState;
	}

	
}