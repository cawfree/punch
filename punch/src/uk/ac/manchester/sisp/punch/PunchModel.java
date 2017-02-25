package uk.ac.manchester.sisp.punch;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.manchester.sisp.punch.ui.core.context.IContext;
import uk.ac.manchester.sisp.punch.ui.key.UIKeyDispatcher;
import uk.ac.manchester.sisp.punch.ui.update.UIUpdateDispatcher;
import uk.ac.manchester.sisp.ribbon.font.IFont;
import uk.ac.manchester.sisp.ribbon.font.truetype.TrueTypeDecoder;
import uk.ac.manchester.sisp.ribbon.opengl.IScreenParameters;
import uk.ac.manchester.sisp.ribbon.opengl.IScreenParametersListener;
import uk.ac.manchester.sisp.ribbon.ui.pointer.UIPointerDispatcher;
import uk.ac.manchester.sisp.ribbon.ui.pointer.UIPointerEvent;
import uk.ac.manchester.sisp.ribbon.ui.time.UIElapsedFilter;
import uk.ac.manchester.sisp.ribbon.ui.time.UITimeDispatcher;
import uk.ac.manchester.sisp.ribbon.utils.ResourceUtils;

/* Represents the abstract application independently of graphics API. */
public final class PunchModel implements IScreenParametersListener { /** TODO: Screen Listener or something of that ilk **/
	
	/* Core File References. */
	public static final File FILE_FONT_REGULAR = ResourceUtils.getResource(PunchModel.class.getClassLoader(), "res/fonts/PixelOperator.ttf");
	public static final File FILE_FONT_BOLD    = ResourceUtils.getResource(PunchModel.class.getClassLoader(), "res/fonts/PixelOperator-Bold.ttf");
	public static final File FILE_FONT_UI      = ResourceUtils.getResource(PunchModel.class.getClassLoader(), "res/fonts/VeraMono-Bold.ttf");
	public static final File FILE_FONT_CODE    = ResourceUtils.getResource(PunchModel.class.getClassLoader(), "res/fonts/ProggyClean-Regular.ttf");
	
	/* Member Variables. */
	private final float   mDotsPerInch;
	private       int     mScreenWidth;
	private       int     mScreenHeight;
	private       boolean mUnsavedChanges;
	
	/* UI Services. */
	private final UITimeDispatcher             mUITimeDispatcher;
	private final UIPointerDispatcher          mUIPointerDispatcher;
	private final UIKeyDispatcher              mUIKeyDispatcher;
	private final UIUpdateDispatcher           mUIUpdateDispatcher;
	private final UIElapsedFilter              mUIDeferFilter;
	private final List<UIPointerEvent>         mDeferList;
	
	/* Fonts Mapping. */
	private final Map<File, IFont> mFontMap;
	
	/* Active Applications Contexts. */
	private final List<IContext> mContexts;
	
	protected PunchModel(final float pDotsPerInch) throws IOException {
		/* Fetch the CurrentTimeSeconds. */
		final float lCurrentTimeSeconds = ResourceUtils.getSystemTimeSeconds();
		/* Initialize Member Variables. */
		this.mDotsPerInch         = pDotsPerInch;
		this.mScreenWidth         = 0;
		this.mScreenHeight        = 0;
		this.mUnsavedChanges      = false;
		/* Initialize Member Variables. */
		this.mUITimeDispatcher    = new UITimeDispatcher(true);
		this.mUIPointerDispatcher = new UIPointerDispatcher(ResourceUtils.getSystemTimeSeconds(), true);
		this.mUIKeyDispatcher     = new UIKeyDispatcher(true);
		this.mUIUpdateDispatcher  = new UIUpdateDispatcher(true);
		this.mUIDeferFilter       = new UIElapsedFilter(lCurrentTimeSeconds, 0.0f);
		this.mFontMap             = new HashMap<File, IFont>();
		this.mDeferList           = Collections.synchronizedList(new ArrayList<UIPointerEvent>());
		/* Allocate the Contexts. */
		this.mContexts            = new ArrayList<IContext>(0);
		/* Allocate a TrueTypeDecoder. */
		final TrueTypeDecoder lTrueTypeDecoder = new TrueTypeDecoder();
		/* Initialize the Fonts. */ 
		this.getFontMap().put(PunchModel.FILE_FONT_REGULAR, lTrueTypeDecoder.onCreateFromFile(PunchModel.FILE_FONT_REGULAR));
		this.getFontMap().put(PunchModel.FILE_FONT_BOLD,    lTrueTypeDecoder.onCreateFromFile(PunchModel.FILE_FONT_BOLD));
		this.getFontMap().put(PunchModel.FILE_FONT_UI,      lTrueTypeDecoder.onCreateFromFile(PunchModel.FILE_FONT_UI));
		this.getFontMap().put(PunchModel.FILE_FONT_CODE,    lTrueTypeDecoder.onCreateFromFile(PunchModel.FILE_FONT_CODE));
	}
	
	public final float getDotsPerInch() {
		return this.mDotsPerInch;
	}
	
	private final void setScreenWidth(final int pScreenWidth) {
		this.mScreenWidth = pScreenWidth;
	}
	
	public final int getScreenWidth() {
		return this.mScreenWidth;
	}
	
	private final void setScreenHeight(final int pScreenHeight) {
		this.mScreenHeight = pScreenHeight;
	}
	
	public final int getScreenHeight() {
		return this.mScreenHeight;
	}
	
	public final UIElapsedFilter getUIDeferFilter() {
		return this.mUIDeferFilter;
	}
	
	protected final void setUnsavedChanges(final boolean pIsUnsavedChanges) {
		this.mUnsavedChanges = pIsUnsavedChanges;
	}
	
	public final boolean isUnsavedChanges() {
		return this.mUnsavedChanges;
	}
	
	public final UITimeDispatcher getUISecondsDispatcher() {
		return this.mUITimeDispatcher;
	}
	
	public final UIPointerDispatcher getUIPointerDispatcher() {
		return this.mUIPointerDispatcher;
	}
	
	public final UIKeyDispatcher getUIKeyDispatcher() {
		return this.mUIKeyDispatcher;
	}
	
	public final UIUpdateDispatcher getUIUpdateDispatcher() {
		return this.mUIUpdateDispatcher;
	}
	
	public final Map<File, IFont> getFontMap() {
		return this.mFontMap;
	}
	
//	public final IFont getFont() {
//		return this.mFont;
//	}
//	
//	public final IFont getBoldFont() {
//		return this.mBoldFont;
//	}
//	
//	public final IFont getUIFont() {
//		return this.mUIFont;
//	}
//	
//	public final IFont getCodeFont() {
//		return this.mCodeFont;
//	}
	
	/** TODO: How to protect? **/
	public final List<IContext> getContexts() {
		return this.mContexts;
	}
	
	public final List<UIPointerEvent> getDeferList() {
		return this.mDeferList;
	}

	@Override
	public final void onScreenParametersChanged(final IScreenParameters pScreenParameters) {
		/* Update the ScreenWidth and ScreenHeight. */
		this.setScreenWidth(pScreenParameters.getScreenWidth());
		this.setScreenHeight(pScreenParameters.getScreenHeight());
		/* Synchronize along the Contexts. */
		synchronized(this.getContexts()) {
			/* Iterate the Contexts. */
			for(final IContext lContext : this.getContexts()) {
				/* Supply the ScreenParameters. */
				lContext.onScreenParametersChanged(pScreenParameters);
			}
		}
	}
	
}