package uk.ac.manchester.sisp.punch.ui;

import java.io.Serializable;

import uk.ac.manchester.sisp.punch.ui.courier.IUICourier;
import uk.ac.manchester.sisp.ribbon.common.IDim2;
import uk.ac.manchester.sisp.ribbon.common.IOpacity;
import uk.ac.manchester.sisp.ribbon.common.IVec2;
import uk.ac.manchester.sisp.ribbon.common.IVisible;

/** TODO: We must find some way to abstract the LexiconCourier. **/
public interface IUIElement extends Serializable, IVisible, IVec2.I.W, IDim2.I, IOpacity, IUICourier.Dispatch {
	/* Defines whether a UIElement is subject to render bounds within visibility tests. */
	public abstract boolean isScissorable();
}