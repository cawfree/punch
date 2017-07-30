package uk.ac.manchester.sisp.punch.ui.lexicon.cascade;

import uk.ac.manchester.sisp.punch.ui.core.IIcon;
import uk.ac.manchester.sisp.punch.ui.lexicon.ILexical;
import uk.ac.manchester.sisp.punch.ui.lexicon.courier.ILexiconCourier;
import uk.ac.manchester.sisp.punch.ui.lexicon.data.global.DataGlobal;
import uk.ac.manchester.sisp.punch.ui.lexicon.data.global.EDataDirection;
import uk.ac.manchester.sisp.punch.ui.lexicon.data.global.IDataConduit;
import uk.ac.manchester.sisp.punch.ui.lexicon.global.LexiconGlobal;
import uk.ac.manchester.sisp.ribbon.common.IBounds2;
import uk.ac.manchester.sisp.ribbon.common.IDim2;
import uk.ac.manchester.sisp.ribbon.io.ArrayStore;
import uk.ac.manchester.sisp.ribbon.opengl.vector.VectorPath;
import uk.ac.manchester.sisp.ribbon.opengl.vector.VectorPathContext;
import uk.ac.manchester.sisp.ribbon.opengl.vector.global.IPathDefinition;
import uk.ac.manchester.sisp.ribbon.opengl.vector.global.IVectorPathGroup;
import uk.ac.manchester.sisp.ribbon.utils.DataUtils;

public final class Cascade extends IIcon.Impl implements ILexical, IDim2.I.W { 
	
	/* Static Definitions. */
	private static final IBounds2.F PATH_MARGIN = new IBounds2.F.Impl(0, 4, 0, 4);
	
	/* Static Definitions. */
	private static final float      PATH_OFFSET_STARVATION    =  4.0f;
	private static final float      PATH_STARVATION_INCREMENT =  8.0f;
	
	private static final VectorPathContext onBufferStipple(final VectorPathContext pVectorPathContext, final ArrayStore.Float pFloatStore, final int pStartX, final int pEndX, final float pIncrement) {
		/* Iterate across the specified X Range in discrete steps. */
		for(float lX = (pStartX + Cascade.PATH_OFFSET_STARVATION); lX < pEndX; lX += pIncrement) {
			/* Render a line segment. */
			pVectorPathContext.onRectangle(pFloatStore, lX, Cascade.PATH_MARGIN.getMinimumY(), (pIncrement / 2.0f), LexiconGlobal.CODE_DIM_HEIGHT_UNIT - (Cascade.PATH_MARGIN.getMinimumY() + Cascade.PATH_MARGIN.getMaximumY()));
		}
		/* Return the VectorPathContext. */
		return pVectorPathContext;
	}
	
	/* Define the default Serialization UID. */
	private static final long serialVersionUID = 1L;
	
	/* Member Variables. */
	private       int          mWidth;
	private final IDataConduit mSourceConduit;
	private final IDataConduit mSinkConduit;
	
	public Cascade(final int pX, final int pWidth, final IDataConduit pSourceConduit, final IDataConduit pSinkConduit) {
		/* Define a constant height for all Cascades. Here we'll initialize the dimensions as zero; these will be defined by the caller. */
		super(pX, 0);
		/* Initialize Member Variables. */
		this.mWidth         = pWidth;
		this.mSourceConduit = pSourceConduit;
		this.mSinkConduit   = pSinkConduit;
	}
	
	/* Generic Courier Dispatch. */
	@Override public final <T> void onCourierDispatch(final ILexiconCourier<T> pLexiconCourier, final T pCourierPackage) { pLexiconCourier.onCourierTransit(this, pCourierPackage); }
	
	@Override
	public final void setWidth(final int pWidth) {
		this.mWidth = pWidth;
	}
	
	@Override
	public final int getWidth() {
		return this.mWidth;
	}
	
	@Override public final void setHeight(final int pHeight) { /* Unsupported. */ }
	
	@Override
	public final int getHeight() { 
		return LexiconGlobal.CODE_DIM_HEIGHT_UNIT;
	}

	@Override
	public IVectorPathGroup[] getVectorPathGroups(final ArrayStore.Float pFloatStore, final VectorPathContext pVectorPathContext) { 
		/* Determine whether we're rendering a starved path. */
		if(this.isStarved()) { 
			/* Render a starved path. */
			return new IVectorPathGroup[]{ 
				/* Starvation. */
				new IVectorPathGroup.Impl(Cascade.onBufferStipple(pVectorPathContext, pFloatStore, 0, this.getWidth(), Cascade.PATH_STARVATION_INCREMENT).onCreatePath(pFloatStore), new IPathDefinition.Fill(this.getSinkConduit().getDataType(EDataDirection.SINK).getColor()))
			};
		}
		else if(this.isErroneous()) { 
			/* Return an error path. */
			return new IVectorPathGroup[]{ 
				/* Use a simple transparency. */
				new IVectorPathGroup.Impl(new VectorPath[]{ pVectorPathContext.onRectangle(pFloatStore, 0.0f, Cascade.PATH_MARGIN.getMinimumY(), this.getWidth(), this.getHeight() - (Cascade.PATH_MARGIN.getMinimumY() + Cascade.PATH_MARGIN.getMaximumY())).onCreatePath(pFloatStore) }, new IPathDefinition[]{ new IPathDefinition.Fill(new float[] { 1.0f, 0.0f, 0.0f, 0.3f }) }),
			};
		}
		else { 
			/* Render a standard path. */
			return new IVectorPathGroup[] { new IVectorPathGroup.Impl(new VectorPath[]{ pVectorPathContext.onRectangle(pFloatStore, 0.0f, Cascade.PATH_MARGIN.getMinimumY(), this.getWidth(), this.getHeight() - (Cascade.PATH_MARGIN.getMinimumY() + Cascade.PATH_MARGIN.getMaximumY())).onCreatePath(pFloatStore) }, new IPathDefinition[]{ new IPathDefinition.Fill(this.getSourceConduit().getDataType(EDataDirection.SOURCE).getColor()) }) };
		}
	}
	
	public final boolean isStarved() { 
		return DataUtils.isNull(this.getSourceConduit());
	}
	
	public final boolean isSuperflouous() { 
		return this.getSourceConduit().equals(this.getSinkConduit());
	}
	
	public final boolean isErroneous() { 
		return !DataGlobal.isCompatible(this.getSourceConduit().getDataType(EDataDirection.SOURCE), this.getSinkConduit().getDataType(EDataDirection.SINK));
	}
	
	public final IDataConduit getSourceConduit() { 
		return this.mSourceConduit;
	}
	
	public final IDataConduit getSinkConduit() { 
		return this.mSinkConduit;
	}
	
}