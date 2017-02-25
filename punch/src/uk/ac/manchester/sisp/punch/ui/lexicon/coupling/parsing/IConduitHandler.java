package uk.ac.manchester.sisp.punch.ui.lexicon.coupling.parsing;

import uk.ac.manchester.sisp.punch.ui.lexicon.compilation.Compilation;
import uk.ac.manchester.sisp.punch.ui.lexicon.data.IDataType;
import uk.ac.manchester.sisp.punch.ui.lexicon.data.global.IDataConduit;

public interface IConduitHandler<T extends IDriver, U extends GState<T, ? ,?>> { 
	
	/* Unary Handlers. */
	public abstract void   onTrash(final Integer pIndex, final T pDriver, final U pGState, final Compilation pCompilation);
	public abstract void   onAlloc(final Integer pIndex, final T pDriver, final U pGState, final Compilation pCompilation);
	
	/* Starvation Handler; Indicates the DataConduit and ExpectedType. */
	public abstract void onStarved(final IDataConduit pDataConduit, final IDataType<?> pExpectedType, final U pGState, final Compilation pCompilation);
	
	/* Binary Handlers. */
	public abstract void onWrite(final T pDriver, final T            pDefunct, final U pGState, final Compilation pCompilation);
	public abstract void onCarry(final T pDriver, final IDataConduit    pSink, final U pGState, final Compilation pCompilation);
	public abstract void onError(final T pDriver, final IDataConduit    pSink, final U pGState, final Compilation pCompilation);

}