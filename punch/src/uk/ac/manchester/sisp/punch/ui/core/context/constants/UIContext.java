package uk.ac.manchester.sisp.punch.ui.core.context.constants;

import java.util.List;

import uk.ac.manchester.sisp.punch.PunchModel;
import uk.ac.manchester.sisp.punch.ui.IUIElement;
import uk.ac.manchester.sisp.punch.ui.color.global.ColorGlobal;
import uk.ac.manchester.sisp.punch.ui.core.Field;
import uk.ac.manchester.sisp.punch.ui.core.context.IContext;
import uk.ac.manchester.sisp.ribbon.opengl.IScreenParameters;
import uk.ac.manchester.sisp.ribbon.ui.pointer.EPointerIndex;
import uk.ac.manchester.sisp.ribbon.ui.pointer.UIPointerEvent;

public final class UIContext extends IContext.Impl {

	/* Define the default Serialization UID. */
	private static final long serialVersionUID = 1L;
	
	/* Member Variables. */
	private final Field mField;
	
	public UIContext(final boolean pIsEnabled, final boolean pIsVisible, final PunchModel pPunchModel) {
		super(pIsEnabled, pIsVisible);
		/* Initialize Member Variables. */
		this.mField = new Field(4, 0, PunchModel.FILE_FONT_BOLD, "[Development Mode] [<3]", 12.0f, ColorGlobal.RGBA_RED, pPunchModel);
		/* Buffer the UIElements. */
		this.getUIElements().add(this.getField());
	}

	@Override
	public final void onScreenParametersChanged(final IScreenParameters pScreenParameters) {
		/* Handle as normal. */
		super.onScreenParametersChanged(pScreenParameters);
		/* Reposition the Field. */
		this.getField().setX((pScreenParameters.getScreenWidth()  - this.getField().getWidth()) / 2);
		this.getField().setY(pScreenParameters.getScreenHeight() - this.getField().getHeight() - 10);
	}

	@Override
	public final boolean onHandleEvent(final UIPointerEvent pUIPointerEvent, final List<IUIElement> pHierarchy, final PunchModel pPunchModel) {
		/* Determine if the user right-clicked. */
		if(pUIPointerEvent.getPointerIndex().equals(EPointerIndex.RIGHT)) {
			/* Process the PointerAction. */
			switch(pUIPointerEvent.getPointerAction()) {
				case POINTER_RELEASE : 
					
				break;
				default : /* Ignore other event cases. */ break;
			}
		}
		/* Assert that we wish for the event to delegate. */
		return false;
	}
	
	private final Field getField() {
		return this.mField;
	}
	
}