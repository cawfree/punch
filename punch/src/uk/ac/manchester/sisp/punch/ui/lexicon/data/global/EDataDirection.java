package uk.ac.manchester.sisp.punch.ui.lexicon.data.global;

import uk.ac.manchester.sisp.ribbon.common.IInverse;

public enum EDataDirection implements IInverse<EDataDirection> {
	/* Defines the direction of data flow. */
	SINK { @Override public final EDataDirection getInverse() { return EDataDirection.SOURCE; } }, SOURCE { @Override public final EDataDirection getInverse() { return EDataDirection.SINK; } }, BIDIRECTIONAL { @Override public final EDataDirection getInverse() { return EDataDirection.BIDIRECTIONAL; } };
}