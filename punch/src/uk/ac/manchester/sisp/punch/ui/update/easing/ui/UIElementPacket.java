package uk.ac.manchester.sisp.punch.ui.update.easing.ui;

import uk.ac.manchester.sisp.punch.ui.IUIElement;
import uk.ac.manchester.sisp.ribbon.common.IDim2;
import uk.ac.manchester.sisp.ribbon.common.IVec2;
import uk.ac.manchester.sisp.ribbon.ui.easing.EasingPacket;

/** TODO: This is goddamn terrible! We have UIElements which can't even change dimensions, but we calculate them anyway. We expose functionality to change parameters of a UIElement that do not even exist. There must be a nicer pattern, but as of yet, I'm too bewildered to figure that one out for myself. **/
public class UIElementPacket extends EasingPacket implements IVec2.I.W, IDim2.I.W {
	
	/* Define the default Serialization UID. */
	private static final long serialVersionUID = 1L;
	
	/* Initial Declarations. */
	public static final int INDEX_INITIAL_X = 0;
	public static final int INDEX_INITIAL_Y = 1;
	public static final int INDEX_INITIAL_W = 2;
	public static final int INDEX_INITIAL_H = 3;
	
	/* Result Declarations. */
	protected static final int INDEX_RESULT_X = 0;
	protected static final int INDEX_RESULT_Y = 1;
	protected static final int INDEX_RESULT_W = 2;
	protected static final int INDEX_RESULT_H = 3;
	
	/* Terminal Declarations. */
	private static final int INDEX_TERMINAL_X = 4;
	private static final int INDEX_TERMINAL_Y = 5;
	private static final int INDEX_TERMINAL_W = 6;
	private static final int INDEX_TERMINAL_H = 7;
	
	/* Member Variables. */
	private final IUIElement mUIElement;
	
	public UIElementPacket(final IUIElement pUIElement) {
		/* Here, we allocate interpolation variables in all dimensions for the UIElement. */ 
		super(new float[]{ pUIElement.getX(), pUIElement.getY(), pUIElement.getWidth(), pUIElement.getHeight(), pUIElement.getX(), pUIElement.getY(), pUIElement.getWidth(), pUIElement.getHeight() });
		/* Initialize Member Variables. */
		this.mUIElement = pUIElement;
	}
	
	public final IUIElement getUIElement() {
		return this.mUIElement;
	}
	
	/* Setters. */
	@Override public void setWidth(final int pWidth)   { this.getEasingData()[INDEX_TERMINAL_W] = (float)pWidth;  }
	@Override public void setHeight(final int pHeight) { this.getEasingData()[INDEX_TERMINAL_H] = (float)pHeight; }
	@Override public void setX(final int pX)           { this.getEasingData()[INDEX_TERMINAL_X] = (float)pX;      }
	@Override public void setY(final int pY)           { this.getEasingData()[INDEX_TERMINAL_Y] = (float)pY;      }
	
	/* Getters. */
	@Override public int getX()      { return Math.round(this.getEasingData()[INDEX_TERMINAL_X]); }
	@Override public int getY()      { return Math.round(this.getEasingData()[INDEX_TERMINAL_Y]); }
	@Override public int getWidth()  { return Math.round(this.getEasingData()[INDEX_TERMINAL_W]); }
	@Override public int getHeight() { return Math.round(this.getEasingData()[INDEX_TERMINAL_H]); }
	
	/* Checks whether the UIElementPacket interpolates the Dimensions of the UIElement. */
	public final boolean isTransforming() {
		/* Define whether the UIElementPacket is shape changing. */
		return this.getEasingData()[UIElementPacket.INDEX_INITIAL_W] != this.getWidth() || this.getEasingData()[UIElementPacket.INDEX_INITIAL_H] != this.getHeight();
	}
	
}