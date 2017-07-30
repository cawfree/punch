package uk.ac.manchester.sisp.punch.ui.lexicon.parameter;

import java.io.File;

import uk.ac.manchester.sisp.punch.ui.core.ISVGIcon;
import uk.ac.manchester.sisp.punch.ui.courier.IUICourier;
import uk.ac.manchester.sisp.punch.ui.lexicon.ILexical;
import uk.ac.manchester.sisp.ribbon.ui.global.UIGlobal;

public interface IParameter extends ILexical, ISVGIcon.IFlat { 
	
	/* Static Declarations. */
	public static final int DIM = 40;
	
	/* Default Implementation Stub. */
	public static abstract class Impl implements IParameter { 
		
		/* Define the default Serialization UID. */
		private static final long serialVersionUID = 1L;
		
		/* Member Variables. */
		private       int     mX;
		private       int     mY;
		private final File    mFile;
		
		public Impl(final int pX, final int pY, final File pFile) {
			/* Initialize Member Variables. */
			this.mX    = pX;
			this.mY    = pY;
			this.mFile = pFile;
		}
		
		/* Generic Courier Dispatch. */
		@Override public final <U> void onCourierDispatch(final IUICourier<U> pCourier, final U pCourierPackage) { pCourier.onCourierTransit(this, pCourierPackage); }
		
		/* Wrapper Configurations. */
		@Override public int     getWidth()         { return IParameter.DIM;    }
		@Override public int     getHeight()        { return IParameter.DIM;    }
		@Override public boolean isVisible()        { return true;              }
		@Override public void    setX(final int pX) { this.mX = pX;             }
		@Override public int     getX()             { return this.mX;           }
		@Override public void    setY(final int pY) { this.mY = pY;             }
		@Override public int     getY()             { return this.mY;           }
		@Override public float   getOpacity()       { return UIGlobal.UI_UNITY; }
		          public File    getFile()          { return this.mFile;        }
		@Override public boolean isScissorable()    { return true;              }
		
	};
	
}