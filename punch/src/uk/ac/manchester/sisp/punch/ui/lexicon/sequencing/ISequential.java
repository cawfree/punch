package uk.ac.manchester.sisp.punch.ui.lexicon.sequencing;

import java.util.Comparator;

import uk.ac.manchester.sisp.punch.ui.color.global.ColorGlobal;
import uk.ac.manchester.sisp.punch.ui.core.IGroup;
import uk.ac.manchester.sisp.punch.ui.lexicon.ILexical;
import uk.ac.manchester.sisp.punch.ui.lexicon.courier.ILexiconCourier;
import uk.ac.manchester.sisp.punch.ui.lexicon.global.LexiconGlobal;
import uk.ac.manchester.sisp.ribbon.common.IBounds2;
import uk.ac.manchester.sisp.ribbon.common.IDim2;
import uk.ac.manchester.sisp.ribbon.common.IVec2;
import uk.ac.manchester.sisp.ribbon.io.ArrayStore;
import uk.ac.manchester.sisp.ribbon.opengl.vector.VectorPath;
import uk.ac.manchester.sisp.ribbon.opengl.vector.VectorPathContext;
import uk.ac.manchester.sisp.ribbon.opengl.vector.global.IPathDefinition;
import uk.ac.manchester.sisp.ribbon.opengl.vector.global.IVectorPathGroup;

public interface ISequential <U extends ILexical> extends IGroup<U>, ILexical { 
	
	/* Static Implementation Stub. */
	public static class Impl<U extends ILexical> extends IGroup.Impl<U> implements ISequential<U> {

		/* Define the default Serialization UID. */
		private static final long serialVersionUID = 1L;
		
		public Impl(final int pX, final int pY, final int pWidth, final int pHeight) {
			super(pX, pY, pWidth, pHeight);
		}
		
		/* Courier Dispatch Implementations. */
		@Override public <T> void onCourierDispatch(final ILexiconCourier<T> pLexiconCourier, final T pCourierPackage) { pLexiconCourier.onCourierTransit(this, pCourierPackage); }
		
		@Override
		public IVectorPathGroup[] getVectorPathGroups(final ArrayStore.Float pFloatStore, final VectorPathContext pVectorPathContext) {
			/* Allocate a simple shape for debugging. */
			return new IVectorPathGroup[]{ 
				/* Allocate a simple filled Rectangle. (Use IVectorPathGroup.Impl to force evaluation!) */
				new IVectorPathGroup.Impl(new VectorPath[]{ pVectorPathContext.onRectangle(pFloatStore, 0.0f, 0.0f, this.getWidth(), this.getHeight()).onCreatePath(pFloatStore) }, new IPathDefinition[]{ new IPathDefinition.Fill(ColorGlobal.RGBA_WHITE) })
			};
		}
		
		/* Look-and-feel constants. */
		@Override public IDim2.I            getMinima() { return new IDim2.I.Impl(LexiconGlobal.CODE_DIM_HEIGHT_UNIT, LexiconGlobal.CODE_DIM_HEIGHT_UNIT);        }
		@Override public int               getPadding() { return 18;                                                                                              }
		@Override public IBounds2.I         getMargin() { return new IBounds2.I.Impl(this.getPadding(), 0, this.getPadding(), 0);                                 }
		@Override public boolean    isScissorContents() { return true;                                                                                            }

		@SuppressWarnings("unchecked") @Override public Comparator<U> getComparator() { return (Comparator<U>)IVec2.COMPARATOR_HORIZONTAL; }
		
	}
	
}