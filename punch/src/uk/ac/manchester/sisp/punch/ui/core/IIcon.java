package uk.ac.manchester.sisp.punch.ui.core;

import uk.ac.manchester.sisp.punch.ui.IUIElement;
import uk.ac.manchester.sisp.punch.ui.courier.IUICourier;
import uk.ac.manchester.sisp.ribbon.image.IVectorImage;
import uk.ac.manchester.sisp.ribbon.ui.global.UIGlobal;

public interface IIcon extends IUIElement, IVectorImage {
	
	/* Static Class Implementation. */
	public static abstract class Impl implements IIcon { 

		/* Define the default Serialization UID. */
		private static final long serialVersionUID = 1L;
		
		/* Member Variables. */
		private int     mX;
		private int     mY;
		
		public Impl(final int pX, final int pY) {
			/* Initialize Member Variables. */
			this.mX       = pX;
			this.mY       = pY;
		}
		
		@Override public <T> void onCourierDispatch(final IUICourier<T> pUICourier, final T pCourierPackage) { pUICourier.onCourierTransit(this, pCourierPackage);          }
		
		@Override public boolean isVisible() {
			return true;
		}
		
		@Override public void dispose() { }

		@Override public final void setX(final int pX) { this.mX = pX;             }
		@Override public final void setY(final int pY) { this.mY = pY;             }
		@Override public final int  getX()             { return this.mX;           }
		@Override public final int  getY()             { return this.mY;           }
		@Override public float      getOpacity()       { return UIGlobal.UI_UNITY; }
		
		@Override
		public boolean isScissorable() {
			return true;
		}
		
	};
	
//	@Override
//	public IVectorPathGroup[] getVectorPathGroups(final ArrayStore.Float pFloatStore, final VectorPathContext pVectorPathContext) {
//		/* Allocate a simple shape for debugging. */
//		return new IVectorPathGroup[]{ 
//			/* Allocate a simple filled Rectangle. (Use IVectorPathGroup.Impl to force evaluation!) */
//			new IVectorPathGroup.Impl(new VectorPath[]{ pVectorPathContext.onRectangle(pFloatStore, 0.0f, 0.0f, this.getWidth(), this.getHeight()).onCreatePath(pFloatStore) }, new IPathDefinition[]{ new IPathDefinition.Fill(ColorGlobal.RGBA_RED) })
//		};
//	}
	
}