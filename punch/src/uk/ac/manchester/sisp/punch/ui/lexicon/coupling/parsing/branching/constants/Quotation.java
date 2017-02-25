package uk.ac.manchester.sisp.punch.ui.lexicon.coupling.parsing.branching.constants;

import uk.ac.manchester.sisp.punch.ui.lexicon.compilation.Compilation;
import uk.ac.manchester.sisp.punch.ui.lexicon.coupling.parsing.GParser;
import uk.ac.manchester.sisp.punch.ui.lexicon.coupling.parsing.GState;
import uk.ac.manchester.sisp.punch.ui.lexicon.coupling.parsing.IDriver;
import uk.ac.manchester.sisp.punch.ui.lexicon.coupling.parsing.branching.IBranch;
import uk.ac.manchester.sisp.punch.ui.lexicon.parameter.contact.IContact;
import uk.ac.manchester.sisp.ribbon.common.IVec2;
import uk.ac.manchester.sisp.ribbon.utils.MathUtils;

public final class Quotation <T extends IDriver> implements IBranch<T> { 
	
	/* Member Variables. */
	private final IContact<?> mContact;
	private final IVec2.I     mOffset;
	
	public <U extends GState<T, ?, ?>> Quotation(final IContact<?> pContact, final IVec2.I pOffset, final GParser<T, U> pGParser, final U pGState, final Compilation pCompilation) { 
		/* Initialize Member Variables. */
		this.mContact = pContact;
		this.mOffset  = pOffset;
		/* Initialize the Branch. */
		this.next(pGParser, pGState, pCompilation);
	}

	@Override
	public final void logic() { 
		/* There's no pre-branch logic for an unconditional quotation. */
	}

	@Override
	public final boolean isAmbivalent() { 
		return true;
	}
	
	@Override
	public final <U extends GState<T, ?, ?>> void next(final GParser<T, U> pGParser, final U pGState, final Compilation pCompilation) {
		/* Move to the Contact's Location. */
		MathUtils.onSupplyOffset  (pCompilation, this.getOffset());
			/* Quote the Contact. */
			pGParser.onQuoteConduit(this.getContact(), pGState, pCompilation);
		/* Withdraw from the Contact's Location. */
		MathUtils.onWithdrawOffset(pCompilation, this.getOffset());
	}

	@Override
	public final boolean isEnabled() { 
		/* A Quotation is always active. */
		return true;
	}
	
	private final IContact<?> getContact() { 
		return this.mContact;
	}
	
	private final IVec2.I getOffset() { 
		return this.mOffset;
	}
	
}