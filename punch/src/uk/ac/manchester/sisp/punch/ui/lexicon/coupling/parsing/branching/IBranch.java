package uk.ac.manchester.sisp.punch.ui.lexicon.coupling.parsing.branching;

import uk.ac.manchester.sisp.punch.ui.lexicon.compilation.Compilation;
import uk.ac.manchester.sisp.punch.ui.lexicon.coupling.parsing.GParser;
import uk.ac.manchester.sisp.punch.ui.lexicon.coupling.parsing.GState;
import uk.ac.manchester.sisp.punch.ui.lexicon.coupling.parsing.IDriver;
import uk.ac.manchester.sisp.ribbon.common.IEnabled;

public interface IBranch <T extends IDriver> extends IEnabled { 
	
	public abstract void    logic();
	public abstract boolean isAmbivalent();
	public abstract <U extends GState<T, ?, ?>> void next(final GParser<T, U> pGParser, final U pGState, final Compilation pCompilation);
	
}