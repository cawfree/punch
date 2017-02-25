package uk.ac.manchester.sisp.punch.ui.core;

import java.io.File;

import uk.ac.manchester.sisp.punch.ui.IUIElement;
import uk.ac.manchester.sisp.punch.ui.courier.IUICourier;
import uk.ac.manchester.sisp.ribbon.common.IColorRGBA;
import uk.ac.manchester.sisp.ribbon.common.IDim2;
import uk.ac.manchester.sisp.ribbon.ui.global.UIGlobal;

public interface ISVGIcon extends IUIElement, IDim2.I {
	
	/* Static Base Implementation. */
	public static abstract class Impl implements ISVGIcon {

		/* Define the default Serialization UID. */
		private static final long serialVersionUID = 1L;
		
		/* Member Variables. */
		private       int     mX;
		private       int     mY;
		private final File    mFile;
		
		public Impl(final int pX, final int pY, final File pFile) {
			/* Initialize Member Variables. */
			this.mX       = pX;
			this.mY       = pY;
			/* Fetch the corresponding FilePath. */
			this.mFile = pFile;
		}
		
		@Override public <T> void onCourierDispatch(final IUICourier<T> pUICourier, final T pCourierPackage) { pUICourier.onCourierTransit(this, pCourierPackage); }
		
		@Override 
		public boolean isVisible() { 
			return true;
		}
		
		@Override 
		public void setX(final int pX) { 
			this.mX = pX;
		}
		
		@Override 
		public int getX() { 
			return this.mX;
		}

		@Override 
		public void setY(final int pY) { 
			this.mY = pY;
		}
		
		@Override 
		public int  getY() { 
			return this.mY;
		}
		
		public final File getFile() { 
			return this.mFile;
		}
		
		@Override
		public float getOpacity() {
			return UIGlobal.UI_UNITY;
		}
		
		@Override
		public boolean isScissorable() {
			return true;
		}
		
	};
	
	public static interface IFlat extends ISVGIcon, IColorRGBA { 
		
	};
	
	/* Defines a 'flat' method of rendering coloured fills. */
	public static class Flat extends ISVGIcon.Impl implements ISVGIcon.IFlat { 

		/* Define the default Serialization UID. */
		private static final long serialVersionUID = 1L;
		
		/* Member Variables. */
		private float[]   mColor;
		private final int mWidth;
		private final int mHeight;
		
		public Flat(int pX, int pY, final int pWidth, final int pHeight, File pFile, float[] pColor) {
			super(pX, pY, pFile);
			/* Initialize Member Variables. */
			this.mWidth  = pWidth;
			this.mHeight = pHeight;
			this.mColor  = pColor;
		}

		@Override public int getWidth()  { return this.mWidth;  }
		@Override public int getHeight() { return this.mHeight; }

		@Override public final <T> void onCourierDispatch(final IUICourier<T> pUICourier, final T pCourierPackage) { 
			pUICourier.onCourierTransit(this, pCourierPackage);
		}
		
		@Override
		public float[] getColor() {
			return this.mColor;
		}
		
	};
	
	/** TODO: Abstract to an interface. **/
	/* Returns the location of the SVG File resource. */
	public abstract File getFile();
	
	
	
}