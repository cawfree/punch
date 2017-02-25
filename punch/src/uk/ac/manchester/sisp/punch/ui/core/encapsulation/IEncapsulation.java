package uk.ac.manchester.sisp.punch.ui.core.encapsulation;

import java.util.List;

import uk.ac.manchester.sisp.punch.PunchModel;
import uk.ac.manchester.sisp.punch.exception.PunchException;
import uk.ac.manchester.sisp.punch.ui.IUIElement;
import uk.ac.manchester.sisp.punch.ui.core.IGroup;
import uk.ac.manchester.sisp.punch.ui.core.context.IContext;
import uk.ac.manchester.sisp.punch.ui.distribution.global.DistributionGlobal;
import uk.ac.manchester.sisp.ribbon.common.IEnabled;
import uk.ac.manchester.sisp.ribbon.io.EEntryMode;
import uk.ac.manchester.sisp.ribbon.ui.easing.IEasingConfiguration;

/* Define the type of response for an encapsulation. */
public interface IEncapsulation extends IEnabled { /** TODO: Really, this is a kind of EnclosureResponse. **/
	
	/* Construction Constant; Disable Construction. */
	public static final IEncapsulation ENCAPSULATION_DISABLED = new IEncapsulation() {
		/* Assert that Construction won't be supported. */
		@Override public final <T extends IUIElement> void onEncapsulate(final EEntryMode pEntryMode, final List<IUIElement> pHierarchy, final List<T> pUIElements, final IUIElement pUIElement) { }
		/* This method should not be called on a disabled Construction. */
		@Override public final <T extends IUIElement> void onPostEncapsulation(final EEntryMode pEntryMode, final IContext pContext, final List<IUIElement> pHierarchy, final IGroup<T> pEnclosure, final IUIElement pUIElement, final PunchModel pPunchModel) {
			/* Throw a core exception. */
			throw new PunchException("Construction Response Not Supported!");
		}
		/* Assert that this Construction is not supported. */
		@Override public final boolean isEnabled() { return false; }
		/* Define a null reference Enclosure. */
		@Override public final IGroup<?> onDefineEnclosure(final IGroup<?> pSuggestion, final List<IUIElement> pHierarchy) { return null; }
	};
//	
//	/* Construction Constant; Default Construction. */
//	public static final IEncapsulation ENCAPSULATION_ENABLED = new Simple();
//	
//	/* Executes supported encapsulation in a direct manner. (No distribution.) */
//	public static final IEncapsulation ENCAPSULATION_RAW     = new IEncapsulation() {
//		/* Handle the encapsulation. */
//		@SuppressWarnings("unchecked") @Override public final <T extends IUIElement> void onEncapsulate(final EEntryMode pEntryMode, final List<IUIElement> pHierarchy, final List<T> pUIElements, final IUIElement pUIElement) {
//			/* Process the EntryMode. */
//			switch(pEntryMode) {
//				case SUPPLY   : pUIElements.add((T)pUIElement); break;
//				case WITHDRAW : pUIElements.remove(pUIElement); break;
//			}
//		}
//		/* Generic Overrides. */
//		@Override public final boolean isEnabled() { return true; }
//		@Override public final <T extends IUIElement> void onPostEncapsulation(final EEntryMode pEntryMode, final IContext pContext, final List<IUIElement> pHierarchy, final IGroup<T> pEnclosure, final IUIElement pUIElement, final PunchModel pPunchModel) { /* Do nothing. */ }
//		@Override public final IEasingConfiguration getEasingConfiguration() { return IEasingConfiguration.CONFIGURATION_NONE; } 
//		
//	};
	
	/* Define the default Encapsulation. */
	public static abstract class Simple implements IEncapsulation {
		/* Performs the Construction in a simple manner. */
		@SuppressWarnings("unchecked") @Override public <T extends IUIElement> void onEncapsulate(final EEntryMode pEntryMode, final List<IUIElement> pHierarchy, final List<T> pUIElements, final IUIElement pUIElement) { 
			/* Process the EntryMode. */
			switch(pEntryMode) {
				case SUPPLY   : 
					/* Add the UIElement. */
					pUIElements.add((T)pUIElement);
				break;
				case WITHDRAW :
					/* Remove the UIElement. */
					pUIElements.remove(pUIElement);
				break;
			}
		}
		/* Perform a simple response; re-distribute the hierarchy. */
		@Override public <T extends IUIElement> void onPostEncapsulation(final EEntryMode pEntryMode, final IContext pContext, final List<IUIElement> pHierarchy, final IGroup<T> pEnclosure, final IUIElement pUIElement, final PunchModel pPunchModel) {
			/* Implement an immediate distribution. */
			DistributionGlobal.onDistributeHierarchy(pPunchModel, pHierarchy, pEnclosure, IEasingConfiguration.CONFIGURATION_SMOOTH);
		}
		/* Assert that this Construction is active. */
		@Override public final boolean isEnabled() { return true; }
	}
	
	/* Define an Encapsulation that Redirects Containment to another Enclosure. */
	public static abstract class Redirection extends IEncapsulation.Simple {
		/* Returns a reference to the Enclosure which manages the redirection. (This container must be the parent of the container being routed to. */
		public abstract IGroup<?> getManagingEnclosure();
		/* Returns a reference to the Enclosure which  */
		public abstract IGroup<?> getTargetEnclosure();
		/* Define that we'll insert into the Internals. */
		@Override public final IGroup<?> onDefineEnclosure(final IGroup<?> pSuggestion, final List<IUIElement> pHierarchy) { 
			/* Fetch the target Enclosure. */
			final IGroup<?> lTargetEnclosure   = this.getTargetEnclosure();
			/* Fetch the managing Enclosure. */
			final IGroup<?> lManagingEnclosure = this.getManagingEnclosure();
			/* Determine if the Internals isn't present in the Hierarchy. */
			if(!pHierarchy.contains(lTargetEnclosure)) {
				/* Calculate the insertion index. */
				final int i = pHierarchy.indexOf(lManagingEnclosure) + 1; /** TODO: Later build up so we can specify complex hierarchical constraints. **/
				/* Insert the Suggestion into the Hierarchy. */
				pHierarchy.add(i, lTargetEnclosure);
				/* Iterate the subsequent indices. */
				for(int j = pHierarchy.size() - 1; j > i; j--) {
					/* Remove the jth (following) entries. */
					pHierarchy.remove(j);
				}
			}
			/* Force the TargetEnclosure to be the source of the Encapsulation. */
			return lTargetEnclosure;
		}
		/* Handle the Post-Encapsulation update. */
		@Override public <T extends IUIElement> void onPostEncapsulation(final EEntryMode pEntryMode, final IContext pContext, final List<IUIElement> pHierarchy, final IGroup<T> pEnclosure, final IUIElement pUIElement, final PunchModel pPunchModel) {
			/* Distribute the Hierarchy. */
			DistributionGlobal.onDistributeHierarchy(pPunchModel, pHierarchy, pEnclosure, IEasingConfiguration.CONFIGURATION_SMOOTH);
		}
		
	}
	
//	new IEncapsulation.Simple() {
//		/* Define that we'll insert into the Internals. */
//		@Override public final IGroup<?> onDefineEnclosure(final IGroup<?> pSuggestion, final List<IUIElement> pHierarchy) {
//			/* Determine if the Internals isn't present in the Hierarchy. */
//			if(!pHierarchy.contains(pArray.getInternals())) {
//				/* Calculate the insertion index. */
//				final int i = pHierarchy.indexOf(pArray) + 1;
//				/* Insert the Suggestion into the Hierarchy. */
//				pHierarchy.add(i, pArray.getInternals());
//				/* Iterate the subsequent indices. */
//				for(int j = pHierarchy.size() - 1; j > i; j--) {
//					/* Remove the jth (following) entries. */
//					pHierarchy.remove(j);
//				}
//			}
//			/* Force the Internals to be the source of the Encapsulation. */
//			return pArray.getInternals();
//		}
//		/* Handle the Post-Encapsulation update. */
//		@Override public final <T extends IUIElement> void onPostEncapsulation(final EEntryMode pEntryMode, final IContext pContext, final List<IUIElement> pHierarchy, final IGroup<T> pEnclosure, final IUIElement pUIElement, final PunchModel pPunchModel) {
//			/* Distribute the Hierarchy. */
//			DistributionGlobal.onDistributeHierarchy(pPunchModel, pHierarchy, pEnclosure, IEasingConfiguration.CONFIGURATION_SMOOTH);
//		}
//	}
	
	
	
//	/* Construction Constant; Animates the UIElement being dropped from the container, then deletes the instance altogether. */
//	public static final IEncapsulation ENCAPSULATION_DROP = new IEncapsulation() {
//		/* Performs the Construction in a simple manner. */
//		@Override public final <T extends IUIElement> void onEncapsulate(final EEntryMode pEntryMode, final List<IUIElement> pHierarchy, final List<T> pUIElements, final IUIElement pUIElement) { 
//			/* Perform default Construction. */
//			IEncapsulation.ENCAPSULATION_ENABLED.onEncapsulate(pEntryMode, pHierarchy, pUIElements, pUIElement);
//		}
//		/* Perform a simple response; re-distribute the hierarchy. */
//		@Override public final <T extends IUIElement> void onPostEncapsulation(final EEntryMode pEntryMode, final IContext pContext, final List<IUIElement> pHierarchy, final IGroup<T> pEnclosure, final IUIElement pUIElement, final PunchModel pPunchModel) {
//			/* Define the UIElementPacket. */
//			final UIElementPacket lUIElementPacket = new UIElementPacket(pUIElement);
//			/* Update the Co-ordinates of the UIElementPacket. */
//			MathUtils.onSupplyOffset(lUIElementPacket, 0, lUIElementPacket.getHeight());
//			/* Define the UIEasingGroup. */
//			final UIEasingGroup   lUIEasingGroup   = new UIEasingGroup(pPunchModel.getUIUpdateDispatcher(), this.getEasingConfiguration(), ResourceUtils.getSystemTimeSeconds()) {
//				/* Implement custom destruction. */
//				@Override public void dispose() {
//					/* Synchronize along the Enclosure. */
//					synchronized(pEnclosure) {
//						/* Remove the UIElement from the Enclosure. */
//						pEnclosure.getUIElements().remove(pUIElement);
//					}
//					/* Queue the UIElement for destruction. */
//					pPunchModel.getUIUpdateDispatcher().onRibbonEvent(new UIUpdateEvent(ResourceUtils.getSystemTimeSeconds(), EUICommand.DESTROY, pUIElement));
//				}
//			};
//			/* Add the UIElementPacket to the UIEasingGroup. */
//			lUIEasingGroup.getEasingPackets().add(lUIElementPacket);
//			/* Export the UIEasingGroup. */
//			pPunchModel.getUISecondsDispatcher().getEventFilters().add(lUIEasingGroup);
////			/* Defer user interaction. */
////			pPunchModel.getUIDeferFilter().onRegisterCheckpoint(ResourceUtils.getSystemTimeSeconds(), lUIEasingGroup.getEasingConfiguration().getDurationSeconds());
//		}
//		/* Assert that this Construction is active. */
//		@Override public final boolean isEnabled() { return IEncapsulation.ENCAPSULATION_ENABLED.isEnabled(); }
//		/* Define the EasingConfiguration. */
//		@Override public final IEasingConfiguration getEasingConfiguration() { return IEasingConfiguration.CONFIGURATION_DROP; }
//	};
	
	/* Allow implementors to specify the Enclosure. The Group must be either be null in a disabled Encapsulation, or present within the Hierarchy when enabled.*/
	public abstract                        IGroup<?> onDefineEnclosure(final IGroup<?> pSuggestion, final List<IUIElement> pHierarchy);
	/* Defines the core mechanic of the encapsulation routine. Called within a synchronized Context along the owning Group. Must modify UIElements in a fast and simple manner. */
	public abstract <T extends IUIElement> void      onEncapsulate(final EEntryMode pEntryMode, final List<IUIElement> pHierarchy, final List<T> pUIElements, final IUIElement pUIElement);
	/* Allows the Construction routine to respond to the encapsulation. Called within a synchronized Context along the owning Group. */
	public abstract <T extends IUIElement> void      onPostEncapsulation(final EEntryMode pEntryMode, final IContext pContext, final List<IUIElement> pHierarchy, final IGroup<T> pEnclosure, final IUIElement pUIElement, final PunchModel pPunchModel);
//	/* Defines the recommended EasingConfiguration to use for Encapsulation-related Distributions. */
//	public abstract IEasingConfiguration getEasingConfiguration();
}