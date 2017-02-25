package uk.ac.manchester.sisp.punch.ui.distribution;

import java.util.List;

import uk.ac.manchester.sisp.punch.ui.IUIElement;
import uk.ac.manchester.sisp.punch.ui.update.UIUpdateDispatcher;

public interface IDistributable {
	/* Forces a Distributable to re-distribute it's internal contents, representing the completed distribution within an EasingPacket. Call within a synchronized context. */
	public abstract IDistribution onFetchDistribution(final List<IUIElement> pHierarchy, final UIUpdateDispatcher pUIUpdateDispatcher);
}