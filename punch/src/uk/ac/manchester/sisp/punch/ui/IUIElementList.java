package uk.ac.manchester.sisp.punch.ui;

import java.util.List;

public interface IUIElementList <T extends IUIElement> {
	/* Returns the associated List of UIElements. */
	public abstract List<T> getUIElements();
}