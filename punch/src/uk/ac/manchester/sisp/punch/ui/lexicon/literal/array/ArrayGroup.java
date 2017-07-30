package uk.ac.manchester.sisp.punch.ui.lexicon.literal.array;

import java.util.ArrayList;
import java.util.List;

import uk.ac.manchester.sisp.punch.ui.IUIElement;
import uk.ac.manchester.sisp.punch.ui.color.global.ColorGlobal;
import uk.ac.manchester.sisp.punch.ui.distribution.IDistribution;
import uk.ac.manchester.sisp.punch.ui.distribution.constants.LinearDistribution;
import uk.ac.manchester.sisp.punch.ui.lexicon.courier.ILexiconCourier;
import uk.ac.manchester.sisp.punch.ui.lexicon.literal.ILiteral;
import uk.ac.manchester.sisp.punch.ui.lexicon.sequencing.ISequential;
import uk.ac.manchester.sisp.punch.ui.update.UIUpdateDispatcher;
import uk.ac.manchester.sisp.ribbon.common.IBounds2;
import uk.ac.manchester.sisp.ribbon.io.ArrayStore;
import uk.ac.manchester.sisp.ribbon.opengl.vector.VectorPath;
import uk.ac.manchester.sisp.ribbon.opengl.vector.VectorPathContext;
import uk.ac.manchester.sisp.ribbon.opengl.vector.global.IPathDefinition;
import uk.ac.manchester.sisp.ribbon.opengl.vector.global.IVectorPathGroup;

public final class ArrayGroup extends ISequential.Impl<ILiteral<?>> implements ILexiconCourier.Dispatch {
	
	/* Define the default Serialization UID. */
	private static final long serialVersionUID = 1L;

	public ArrayGroup(final int pX, final int pY, final int pWidth, final int pHeight) {
		super(pX, pY, pWidth, pHeight);
	}
	
	@Override
	public IVectorPathGroup[] getVectorPathGroups(final ArrayStore.Float pFloatStore, final VectorPathContext pVectorPathContext) {
		/* Allocate a simple shape for debugging. */
		return new IVectorPathGroup[]{ 
			/* Allocate a simple filled Rectangle. (Use IVectorPathGroup.Impl to force evaluation!) */
			new IVectorPathGroup.Impl(new VectorPath[]{ pVectorPathContext.onRectangle(pFloatStore, 0.0f, 0.0f, this.getWidth(), this.getHeight()).onCreatePath(pFloatStore) }, new IPathDefinition[]{ new IPathDefinition.Fill(ColorGlobal.RGBA_WHITE) })
		};
	}

	/* Courier Dispatch Implementations. */
	@Override public <T> void onCourierDispatch(final ILexiconCourier<T> pLexiconCourier, final T pCourierPackage) { pLexiconCourier.onCourierTransit(this, pCourierPackage); }
	
	
	/* Define custom padding. */
	@Override public final int getPadding() {
		/* Define some tight padding. */
		return 2;
	}
	
	/* Define the Margin. */
	@Override public final IBounds2.I getMargin() {
		/* Use a tight padding to further compact the Array. */
		return new IBounds2.I.Impl(this.getPadding(), 0, this.getPadding(), 0);
	}
	
	/* Define a custom Distribution. */
	@Override public final IDistribution onFetchDistribution(final List<IUIElement> pHierarchy, final UIUpdateDispatcher pUIUpdateDispatcher) {
		/* Return a LinearDistribution. */
		return new LinearDistribution<ILiteral<?>>(new ArrayList<ILiteral<?>>(this.getUIElements()));
	}

}