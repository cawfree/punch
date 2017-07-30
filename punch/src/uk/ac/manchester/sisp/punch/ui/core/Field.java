package uk.ac.manchester.sisp.punch.ui.core;

import java.io.File;
import java.util.List;

import uk.ac.manchester.sisp.punch.PunchModel;
import uk.ac.manchester.sisp.punch.ui.IUIElement;
import uk.ac.manchester.sisp.punch.ui.collision.RayCastAdapter;
import uk.ac.manchester.sisp.punch.ui.core.context.IContext;
import uk.ac.manchester.sisp.punch.ui.courier.IUICourier;
import uk.ac.manchester.sisp.punch.ui.distribution.global.DistributionGlobal;
import uk.ac.manchester.sisp.punch.ui.key.UIKeyDispatcher;
import uk.ac.manchester.sisp.punch.ui.key.UIKeyEvent;
import uk.ac.manchester.sisp.ribbon.common.IDim2;
import uk.ac.manchester.sisp.ribbon.common.IVec2;
import uk.ac.manchester.sisp.ribbon.event.filter.IEventsFilter;
import uk.ac.manchester.sisp.ribbon.font.IFont;
import uk.ac.manchester.sisp.ribbon.font.global.FontGlobal;
import uk.ac.manchester.sisp.ribbon.ui.easing.IEasingConfiguration;
import uk.ac.manchester.sisp.ribbon.ui.global.UIGlobal;
import uk.ac.manchester.sisp.ribbon.ui.pointer.EPointerAction;
import uk.ac.manchester.sisp.ribbon.ui.pointer.UIPointerDispatcher;
import uk.ac.manchester.sisp.ribbon.ui.pointer.UIPointerEvent;
import uk.ac.manchester.sisp.ribbon.utils.MathUtils;

public class Field implements IUIElement {
	
	/* Define the default Serialization UID. */
	private static final long serialVersionUID = 1L;
	
//	public static final int onCalculateNumberOfLines(final String pText) {
//		/* Allocate the number of lines. (All text will be at least one!) */
//		int lNumLines = 1;
//		/* Count the number of newline instances in the String. */
//		for(int i = 0; i < pText.length(); i++) {
//			/* Update the NumLines for all instances of NewLines found. */
//			lNumLines += DataUtils.booleanToInt(pText.charAt(i) == '\r');
//		}
//		/* Return the NumLines. */
//		return lNumLines;
//	}

	public static final IDim2.I.W onStringDimensions(final IDim2.I.W pCallback, final String pText, final IFont pFont, final float pPointSize, final float pDotsPerInch) {
		/* Initialize the Callback. */
		pCallback.setWidth (0);
		pCallback.setHeight(0);
		/* Split the String into different Lines. */
		final String[] lLines = pText.split("\r");
		/* Iterate the Lines. */
		for(int i = 0; i < lLines.length; i++) {
			/* Calculate the Width. */
			pCallback.setWidth(Math.max(pCallback.getWidth(), Math.round(FontGlobal.onCalculateLineWidth(pFont,  pFont.getFontScale(pDotsPerInch, pPointSize), lLines[i]))));
		}
		/* Set the LineHeight. */
		pCallback.setHeight((lLines.length) * Math.round(FontGlobal.onCalculateLineHeight(pFont, pFont.getFontScale(pDotsPerInch, pPointSize), UIKeyEvent.CHARSET_SIMPLE_ENGLISH)));
		/* Return the Callback. */
		return pCallback;
	}
	
	/* Co-ordinates the series of calls to support typed-text updates to a Field. */
	public static final void onListen(final IContext pContext, final List<IUIElement> pHierarchy, final IGroup<?> pEnclosure, final Field pField, final PunchModel pPunchModel) {
		/* Calculate the absolute co-ordinates of the Field. */
		final IVec2.I   lAbsoluteCoords = RayCastAdapter.onFetchAbsoluteCoordinates(new IVec2.I.Impl(), pHierarchy, pField); /** TODO: How to maintain with delta? **/
		/* Allocate the LifeTime buffer. Initialize the task as alive ('true'). */
		final boolean[] lLifeTime       = new boolean[] { true };
		/* Allocate the UIPointerFilter. */
		final IEventsFilter<UIPointerEvent, UIPointerDispatcher> lUIPointerFilter = new IEventsFilter<UIPointerEvent, UIPointerDispatcher>() {
			/* Handle Pointer Events. */
			@Override public boolean onHandleEvent(final UIPointerEvent pUIPointerEvent, final UIPointerDispatcher pEventsDispatcher) {
				/* Synchronize along the LifeTime. */
				synchronized(lLifeTime) {
					/* Determine whether the user has clicked, and we're still alive. */
					if(lLifeTime[0] && pUIPointerEvent.getPointerAction().equals(EPointerAction.POINTER_PRESSED)) {
						/* Define the PointerCoords. (The mouse position is offset by the camera location.) */
						final IVec2.I lPointerCoords  = new IVec2.I.Impl((pUIPointerEvent.getX()), (pUIPointerEvent.getY()));
						/* Calculate the XPosition and YPosition. */
						final float   lXPosition      = (lAbsoluteCoords.getX() * pContext.getScale());
						final float   lYPosition      = (lAbsoluteCoords.getY() * pContext.getScale());
						/* Determine whether we're intersecting. */
						final boolean lIsIntersecting = MathUtils.isIntersecting(lPointerCoords.getX(), lPointerCoords.getY(), lXPosition, lYPosition, lXPosition + (pField.getWidth() * pContext.getScale()), lYPosition + (pField.getHeight() * pContext.getScale()));
						/* Update the lifetime. (We must only allow the Field's interaction to persist whilst the user has clicked within the field!) */
						              lLifeTime[0]   &= lIsIntersecting;
					}
				}
				/* Assert that the filter lasts as long as the LifeTime. */
				return lLifeTime[0];
			}
			/* Implement disposal operations. */
			@Override public void dispose() { }
		};
		/* Allocate the UIKeyFilter. */
		final IEventsFilter<UIKeyEvent, UIKeyDispatcher> lUIKeyFilter = new IEventsFilter<UIKeyEvent, UIKeyDispatcher>() {
			/* Handle Key Events. */
			@Override public final boolean onHandleEvent(final UIKeyEvent pUIKeyEvent, final UIKeyDispatcher pEventsDispatcher) {
				/* Allocate references to the TextString and Dimension. */
				      String  lTextString = null;
				      IDim2.I lDim2       = null;
				/* Synchronize along the LifeTime. */
				synchronized(lLifeTime) {
					/* Ensure we're still alive. */
					if(lLifeTime[0]) {
						/* Allocate the TextString. */
						lTextString = pField.getText();
						/* Determine if the character is in the alphabet. */
						if(UIKeyEvent.isAlphabet(pUIKeyEvent.getKeyChar()) || UIKeyEvent.isPunctuation(pUIKeyEvent.getKeyChar())) {
							/* Determine if we're handling a carriage return in a non-multiline field. */
							if(!(pUIKeyEvent.getKeyChar() == '\r' && !pField.isMultiline())) {
								/* Update the TextString. */
								lTextString = (lTextString + pUIKeyEvent.getKeyChar());
							}
						}
						/* Determine if we're deleting characters. */
						if(UIKeyEvent.isBackSpace(pUIKeyEvent.getKeyCode()) && (lTextString.length() > 0)) {
							/* Remove the character. */
							lTextString = lTextString.substring(0, lTextString.length() - 1);
						}
						/* Fetch the Font. */
						final IFont lFont = pPunchModel.getFontMap().get(pField.getFont());
						/* Calculate the Field's dimensions. */
						            lDim2 = Field.onStringDimensions(new IDim2.I.Impl(), lTextString, lFont, pField.getPointSize(), pPunchModel.getDotsPerInch());
						/* Assign the TextString to the field. */
						pField.setText(lTextString);
						/* Update the Field's width. */
						pField.setWidth (lDim2.getWidth());
						pField.setHeight(lDim2.getHeight());
						/* Immediately re-distribute the hierarchy. */
						DistributionGlobal.onDistributeHierarchy(pPunchModel, pHierarchy, pEnclosure, IEasingConfiguration.CONFIGURATION_NONE);
					}
				}
				/* Assert that the filter lasts as long as the LifeTime. */
				return lLifeTime[0];
			} 
			/* Implement disposal operations. */
			@Override public final void dispose() { }
		};
		/* Launch the UIPointerFilter and the UIKeyFilter. */
		pPunchModel.getUIKeyDispatcher().getEventFilters().add(lUIKeyFilter);
		pPunchModel.getUIPointerDispatcher().getEventFilters().add(lUIPointerFilter);
	}
	
	/* Member Variables. */
	private int     mX;
	private int     mY;
	private File    mFont;
	private String  mText;
	private float   mPointSize;
	private float[] mColor;
	private int     mWidth;
	private int     mHeight;
	
	public Field(final int pX, final int pY, final File pFont, final String pText, final float pPointSize, final float[] pColor, final PunchModel pPunchModel) {
		/* Initialize Member Variables. */
		this.mX         = pX;
		this.mY         = pY;
		this.mFont      = pFont;
		this.mText      = pText;
		this.mPointSize = pPointSize;
		this.mColor     = pColor;
		/* Fetch the corresponding Font. */
		final IFont lFont = pPunchModel.getFontMap().get(this.getFont());
		/* Assign the Dimensions. */
		this.mWidth  = Field.onStringDimensions(new IDim2.I.Impl(),                    this.getText(), lFont, this.getPointSize(), pPunchModel.getDotsPerInch()).getWidth();
		this.mHeight = Field.onStringDimensions(new IDim2.I.Impl(), UIKeyEvent.CHARSET_SIMPLE_ENGLISH, lFont, this.getPointSize(), pPunchModel.getDotsPerInch()).getHeight();
	}
	
	@Override public final <T> void onCourierDispatch(final IUICourier<T> pUICourier, final T pCourierPackage) { pUICourier.onCourierTransit(this, pCourierPackage);          }
	
	@Override public final boolean isVisible() {
		return true;
	}

	@Override public final void setX(final int pX) {
		this.mX = pX;
	}

	@Override public final void setY(final int pY) {
		this.mY = pY;
	}

	@Override public final int getX() {
		return this.mX;
	}

	@Override public final int getY() {
		return this.mY;
	}
	
	public final void setWidth(final int pWidth) {
		this.mWidth = pWidth;
	}
	
	@Override public final int getWidth() {
		return this.mWidth;
	}
	
	private final void setHeight(final int pHeight) {
		this.mHeight = pHeight;
	}

	@Override public final int getHeight() {
		return this.mHeight;
	}
	
	public final File getFont() {
		return this.mFont;
	}
	
	public final void setText(final String pText) {
		this.mText = pText;
	}
	
	public final String getText() {
		return this.mText;
	}
	
	public final float getPointSize() {
		return this.mPointSize;
	}
	
	public final void setColor(final float[] pColor) {
		this.mColor = pColor;
	}
	
	public final float[] getColor() {
		return this.mColor;
	}

	@Override
	public float getOpacity() {
		return UIGlobal.UI_UNITY;
	}
	
	/* Defines whether the Field supports multiline entry. */
	public boolean isMultiline() { return false; }

	@Override
	public boolean isScissorable() {
		return true;
	}
	
}