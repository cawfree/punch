package uk.ac.manchester.sisp.punch.ui.update.easing.ui;

import uk.ac.manchester.sisp.punch.ui.update.EUICommand;
import uk.ac.manchester.sisp.punch.ui.update.UIUpdateDispatcher;
import uk.ac.manchester.sisp.punch.ui.update.UIUpdateEvent;
import uk.ac.manchester.sisp.ribbon.common.IDim2;
import uk.ac.manchester.sisp.ribbon.common.IVec2;
import uk.ac.manchester.sisp.ribbon.ui.easing.EasingGroup;
import uk.ac.manchester.sisp.ribbon.ui.easing.IEasingConfiguration;
import uk.ac.manchester.sisp.ribbon.utils.ResourceUtils;

public class UIEasingGroup extends EasingGroup<UIElementPacket> {

	/* Define position interpolation. */
	private static final void onInterpolatePosition(final IVec2.I.W pVec2, final float[] pResultsBuffer) {
		/* Update the Position. */
		pVec2.setX(Math.round(pResultsBuffer[UIElementPacket.INDEX_RESULT_X]));
		pVec2.setY(Math.round(pResultsBuffer[UIElementPacket.INDEX_RESULT_Y]));
	};
	
	/* Define position interpolation. */
	private static final void onInterpolateDimension(final IDim2.I.W pDim2, final float[] pResultsBuffer) {
		/* Update the Dimension. */
		pDim2.setWidth (Math.round(pResultsBuffer[UIElementPacket.INDEX_RESULT_W]));
		pDim2.setHeight(Math.round(pResultsBuffer[UIElementPacket.INDEX_RESULT_H]));
	};
	
	/* Member Variables. */
	private final UIUpdateDispatcher mUIUpdateDispatcher;
	
	public UIEasingGroup(final UIUpdateDispatcher pUIUpdateDispatcher, final IEasingConfiguration pEasingConfiguration, final float pObjectTimeSeconds) {
		super(pEasingConfiguration, pObjectTimeSeconds);
		/* Initialize the UIUpdateDispatcher. */
		this.mUIUpdateDispatcher = pUIUpdateDispatcher;
	}
	
	public final UIUpdateDispatcher getUIUpdateDispatcher() {
		return this.mUIUpdateDispatcher;
	}

	@Override
	public final void onEasingResult(final UIElementPacket pEasingPacket, final float pCurrentTimeSeconds, final float[] pResultsBuffer) {
		/* Synchronize along the UIElement. */
		synchronized(pEasingPacket.getUIElement()) {
			/* Interpolate the Position. */
			UIEasingGroup.onInterpolatePosition(pEasingPacket.getUIElement(), pResultsBuffer);
			/* Determine if the UIElementPacket is changing width. */
			if(pEasingPacket.isTransforming() && (pEasingPacket.getUIElement() instanceof IDim2.I.W)) { 
				/* Cast accordingly. */
				final IDim2.I.W lUIElement = (IDim2.I.W)pEasingPacket.getUIElement();
				/* Update the Dimensions of the Group. */
				UIEasingGroup.onInterpolateDimension(lUIElement, pResultsBuffer);
				/* Respond to the Result. */
				this.getUIUpdateDispatcher().onRibbonEvent(new UIUpdateEvent(ResourceUtils.getSystemTimeSeconds(), EUICommand.UPDATE, pEasingPacket.getUIElement()));
			}
		}
	}

}