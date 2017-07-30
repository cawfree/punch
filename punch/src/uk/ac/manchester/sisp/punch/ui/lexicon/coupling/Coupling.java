package uk.ac.manchester.sisp.punch.ui.lexicon.coupling;

import java.util.ArrayList;
import java.util.List;

import uk.ac.manchester.sisp.punch.PunchModel;
import uk.ac.manchester.sisp.punch.global.PunchGlobal;
import uk.ac.manchester.sisp.punch.ui.IUIElement;
import uk.ac.manchester.sisp.punch.ui.IUIPadding;
import uk.ac.manchester.sisp.punch.ui.collision.RayCastManager;
import uk.ac.manchester.sisp.punch.ui.core.IGroup;
import uk.ac.manchester.sisp.punch.ui.core.context.IContext;
import uk.ac.manchester.sisp.punch.ui.distribution.IDistribution;
import uk.ac.manchester.sisp.punch.ui.distribution.global.DistributionGlobal;
import uk.ac.manchester.sisp.punch.ui.lexicon.IAtomic;
import uk.ac.manchester.sisp.punch.ui.lexicon.ILexical;
import uk.ac.manchester.sisp.punch.ui.lexicon.cascade.generation.Cascader;
import uk.ac.manchester.sisp.punch.ui.lexicon.compilation.Compilation;
import uk.ac.manchester.sisp.punch.ui.lexicon.coupling.mapping.Elaborator;
import uk.ac.manchester.sisp.punch.ui.lexicon.coupling.mapping.Formalizer;
import uk.ac.manchester.sisp.punch.ui.lexicon.coupling.mapping.Packetizer;
import uk.ac.manchester.sisp.punch.ui.lexicon.coupling.mapping.Segmenter;
import uk.ac.manchester.sisp.punch.ui.lexicon.courier.ILexiconCourier;
import uk.ac.manchester.sisp.punch.ui.lexicon.decoupler.axiom.Invocation;
import uk.ac.manchester.sisp.punch.ui.lexicon.decoupler.directional.SinkDecoupler;
import uk.ac.manchester.sisp.punch.ui.lexicon.decoupler.directional.SourceDecoupler;
import uk.ac.manchester.sisp.punch.ui.lexicon.global.LexiconGlobal;
import uk.ac.manchester.sisp.punch.ui.lexicon.parameter.contact.IContact;
import uk.ac.manchester.sisp.punch.ui.lexicon.referencing.IInvocatable;
import uk.ac.manchester.sisp.punch.ui.lexicon.referencing.IReferential;
import uk.ac.manchester.sisp.punch.ui.update.UIUpdateDispatcher;
import uk.ac.manchester.sisp.punch.ui.update.easing.ui.UIEasingGroup;
import uk.ac.manchester.sisp.punch.ui.update.easing.ui.UIElementPacket;
import uk.ac.manchester.sisp.ribbon.common.IBounds2;
import uk.ac.manchester.sisp.ribbon.common.IDim2;
import uk.ac.manchester.sisp.ribbon.ui.easing.IEasingConfiguration;
import uk.ac.manchester.sisp.ribbon.utils.DataUtils;
import uk.ac.manchester.sisp.ribbon.utils.MathUtils;

public class Coupling <U extends ILexical, T extends ILexical & IGroup<U>> extends IGroup.Impl<ILexical> implements ILexical, IAtomic, IReferential<Coupling<U, T>>, IInvocatable<Invocation> { 

	/* Define the default Serialization UID. */
	private static final long serialVersionUID = 1L;
	
	/* Static Declarations. */
	private static final int  WIDTH_OVERLAP    = 1;
	
	/* Member Variables. */
	private final SinkDecoupler   mSinkDecoupler;
	private final SourceDecoupler mSourceDecoupler;
	private final List<T>         mInternals;
	
	/** TODO: How to avoid SafeVarargs? **/
	@SafeVarargs public Coupling(final int pX, final int pY, final T ... pInternals) {
		super(pX, pY, DataUtils.JAVA_NULL_INDEX, LexiconGlobal.CODE_DIM_HEIGHT_UNIT);
		/* Initialize Member Variables. */
		this.mSinkDecoupler     = new SinkDecoupler(0, 0);
		this.mSourceDecoupler   = new SourceDecoupler((this.getSinkDecoupler().getWidth() - this.getOverlap()), 0);
		/* Allocate the Internals. */
		this.mInternals         = new ArrayList<T>(pInternals.length);
		/* Calculate the Separator's InitialWidth. */
		final int lInitialWidth = this.getSourceDecoupler().getX() + this.getSourceDecoupler().getWidth();
		/* Iterate the Internals. */
		for(final T lT : pInternals) {
			/* Initialize the Internals. */
			MathUtils.setPosition(lT, this.getInternalX(), 0);
			/* Set the Height of the Internals. */
			lT.setHeight(this.getHeight());
			/* Add the T to the Internals. */
			this.getInternals().add(lT);
			/* Add the Internals to the UIElements. */
			this.getUIElements().add(lT);
		}
		/* Add the SinkDecoupler to the UIElements. */
		this.getUIElements().add(this.getSinkDecoupler());
		/* Add the SourceDecoupler to the UIElements. */
		this.getUIElements().add(this.getSourceDecoupler());
		/* Define the InitialWidth of the Separator. */
		this.setWidth(lInitialWidth);
	}
	
	/* Returns the XPosition for all Internals. */
	public final int getInternalX() { 
		/* All Internals should be processed from this position. */
		return this.getSinkDecoupler().getX() + this.getSinkDecoupler().getWidth();
	}
	
	/* Courier Dispatch Implementations. */
	@Override public <V> void onCourierDispatch(final ILexiconCourier<V> pLexiconCourier, final V pCourierPackage) { pLexiconCourier.onCourierTransit(this, pCourierPackage); }
	
	/* Opens a closed Coupling, and vice-versa. Callers must ensure they're sychronized. */
	public final void onChangeState(final PunchModel pPunchModel, final List<IUIElement> pHierarchy, final IEasingConfiguration pEasingConfiguration) {
		/* Calculate the position of the SourceDecoupler using a pixel offset to signify a change in state that can be interpreted during open/closed analysis. */
		final int lXPosition = this.isClosed() ? (this.getSourceDecoupler().getX() + this.getStep()) : ((this.getSourceDecoupler().getX()) - this.getStep());
		/* Update the position of the SourceDecoupler. */
		this.getSourceDecoupler().setX(lXPosition);
		/* Use an interpolated distribution. */
		DistributionGlobal.onDistributeHierarchy(pPunchModel, pHierarchy, this, pEasingConfiguration);
	}

	@Override
	public synchronized final IDistribution onFetchDistribution(final List<IUIElement> pHierarchy, final UIUpdateDispatcher pUIUpdateDispatcher) {
		/* Determine if we're handling the Outermost Coupling; the top-level definition. */
		if(this.isDeclaration(pHierarchy)) { 
			/* Define a custom mapping. */
			return new IDistribution() { 
				/* Define the Distributable elements contained by the Coupling. */
				@Override public final void onSupplyDistributables(final List<IUIElement> pUIElements) { 
					/* Recursively buffer all references, beginning with the Coupling. */
					PunchGlobal.onAccumulateRecursive(pUIElements, getReference());
					/* Remove the Coupling's parent reference. The Distributor will take care of that for us. */
					pUIElements.remove(getReference());
				}
				/* Orchestrate the Distribution. */
				@Override public final void onDistributeElements( final UIEasingGroup pUIEasingGroup, final IUIPadding pUIPadding, final IBounds2.I.W pResultingBounds) {
					/* Allocate the Coupling's base UIElementPacket. */
					final UIElementPacket lUIElementPacket = new UIElementPacket(getReference());
					/* Add the UIElementPacket to the UIEasingGroup. */
					pUIEasingGroup.getEasingPackets().add(lUIElementPacket);
					/* Allocate the Compilation. */
					final Compilation lCompilation = new Compilation();
					/* Packetize the AbsoluteMap. */
					getReference().onCourierDispatch(lCompilation, new Packetizer(pUIEasingGroup));
					/* Segment the AbsoluteMap. */
					getReference().onCourierDispatch(lCompilation, new  Segmenter(pUIEasingGroup));
					/* Formalize the AbsoluteMap. */
					getReference().onCourierDispatch(lCompilation, new Formalizer(pUIEasingGroup));
					/* Execute elaboration. */
					getReference().onCourierDispatch(lCompilation, new Elaborator(pUIEasingGroup));
					/* Determine whether we're allowed to generate Cascades. */
					if(getReference().isGenerateCascades()) { 
						/* Generate the Cascades. */
						getReference().onCourierDispatch(lCompilation, new Cascader(pUIEasingGroup));
					}
					/* Remove the Coupling's Mapping; it's the Distribution that asserts the Packetization of the parent. */
					pUIEasingGroup.getEasingPackets().remove(lUIElementPacket);
					/* Use the Coupling's Mapping to predict the resulting bounds. */
					MathUtils.setBounds(pResultingBounds, 0, 0, lUIElementPacket.getWidth(), lUIElementPacket.getHeight());
				}
				/* Define that this is an anchored distribution. */
				@Override public final boolean isAnchored() { return true; }
				/* Define custom disposal. */
				@Override public final void       dispose() { }
			};
		}
		else {
			/* Don't distribute. Keep searching the hierarchy. */
			return null;
		}
	}
	
	public boolean isDeclaration(final List<IUIElement> pHierarchy) { /** TODO: Work out, or optimize. **/
		/* Fetch the Parent. */
		final IUIElement lParent = DataUtils.isNull(pHierarchy) ? null : RayCastManager.onFetchParent(pHierarchy, this);
		/* Check whether the parent of the Coupling is a type of IConcurrency. */ /** TODO: This should be Liason, abstract when ready. whole architecture approach. **/
		return DataUtils.isNull(lParent) || (lParent instanceof IContext);
	}
	
	/* Returns the Internals which possesses the current focus of the user. */
	public T getActiveInternals() { /** TODO: Assure external synchronization! **/
		/* Synchronize upon ourself. */
		synchronized(this) {
			/* Fetch the Internals. */
			final List<T> lInternals = this.getInternals();
			/* Define the Index and the MinimumWidth. */
			      int lIndex        = Integer.MAX_VALUE;
			      int lMinimumWidth = Integer.MAX_VALUE;
			/* Iterate the Internals. */
			for(int i = 0; i < lInternals.size(); i++) {
				/* Calculate the Width. */
				final int lWidth = Math.abs(lInternals.get(i).getX() + (DataUtils.booleanToInt(lInternals.get(i).getX() < 0) * (lInternals.get(i).getWidth() >> 2))- this.getInternalX());
				/* Determine if it's Smaller than the MinimumWidth. */
				if(lWidth < lMinimumWidth) { 
					/* Assign the MinimumWidth. */
					lMinimumWidth = lWidth;
					/* Update the Index. */
					lIndex        = i;
				}
			}
			/* Return the Internals. */
			return lInternals.get(lIndex);
		}
	}
	
	@Override
	public final int getPadding() { 
		return IUIPadding.PADDING_NULL.getPadding(); 
	}
	
	@Override
	public final IDim2.I getMinima() {
		return new IDim2.I.Impl((LexiconGlobal.CODE_DIM_HEIGHT_UNIT << 1) - this.getOverlap(), LexiconGlobal.CODE_DIM_HEIGHT_UNIT);
	}
	
	/* Defines whether the Separator is closed. This is represented using a very minor positioning scheme with respect to the SourceDecoupler. */
	public boolean isClosed() {
		/* Calculate the distance between the SinkDecoupler and SourceDecoupler. */
		final int     lDistance   = this.getSourceDecoupler().getX() - (this.getSinkDecoupler().getX() + this.getSinkDecoupler().getWidth());
		/* Calculate the Width of the internal Group. */
		final int     lWidth      = this.getInternalWidth();
		/* Determine whether  the Group is being obscured. (This is asserted when a Group has been inserted into a much smaller open Coupling) */
		final boolean lIsObscured = (lDistance < lWidth - this.getOverlap());
		/* Define whether the Coupling is closed. (We reposition the Overlap in certain circumstances to define opening/closing.) */
		return (lDistance == (-1 * this.getOverlap())) || (!(lDistance == 0 || lDistance == lWidth || lIsObscured));
	}
	
	/* Determines whether to render within the dimensions of the Coupling. */
	@Override public final boolean isScissorContents() { 
		/* Ensure that the only case when we don't scissor is when the Coupling is completely open. */
		return true;//this.isClosed() || this.getSourceDecoupler().getX() < this.getInternalX() + this.getInternalWidth();
	}
	
	/* Returns the Width of the largest Internals; this defines the total width. */
	public final int getInternalWidth() { 
		/* Fetch the Internals. */
		final List<T> lInternals = this.getInternals();
		/* Define the MaximumWidth; initialize to the MIN_VALUE. (We can be certain a Coupling will always contain at least a single Internals entry.) */
		      int     lMaximumWidth  = Integer.MIN_VALUE;
		/* Iterate the Internals. */
		for(final T lT : lInternals) {
			/* Update the MaximumWidth. */
			lMaximumWidth = Math.max(lMaximumWidth, lT.getWidth());
		}
		/* Return the MaximumWidth. */
		return lMaximumWidth;
	}
	
	/* Defines the amount by which to overlap the SinkDecoupler and SourceDecoupler when the Coupling is closed. */
	public int getOverlap() {
		return Coupling.WIDTH_OVERLAP;
	}
	
	/* Defines the size of the offset to apply to the SourceDecoupler when performing a transition. */
	protected int getStep() {
		return Coupling.WIDTH_OVERLAP;
	}
	
	public final SinkDecoupler getSinkDecoupler() {
		return this.mSinkDecoupler;
	}
	
	public final SourceDecoupler getSourceDecoupler() {
		return this.mSourceDecoupler;
	}
	
	/* Callers must be synchronized! Returns the graphical elements which lie between the SinkDecoupler and SourceDecoupler. */
	public final List<T> getInternals() {
		/* Return the Internals. */
		return this.mInternals;
	}

	@Override
	public final Coupling<U, T> getReference() {
		return this;
	}
	
	/* Defines whether the Coupling can be used for Cascade generation. */
	protected boolean isGenerateCascades() {
		/* By default, a Coupling should be allowed to specify Cascades. */
		return true;
	}
	
	/** TODO: Can't invoke an array! **/
	/** TODO: Optimize this method, tighten re-use. Out of cleanliness, if anything. **/
	@Override
	public final Invocation onCreateInvocation() {
		/* Allocate the Invocation. */
		final Invocation lInvocation  = new Invocation(0, 0, this.getReference());
		/* Next, synchronize along the SinkDecoupler. */
		synchronized(this.getSinkDecoupler()) {
			/* Fetch the SinkDecoupler's UIElements. */
			final List<IContact<IContact.Link>> lUIElements = this.getSinkDecoupler().getUIElements();
			/* Iterate the UIElements. */
			for(final IContact<IContact.Link> lContact : lUIElements) {
				/* Create a corresponding link for the Contact. */
				final IContact.Link lLink = lContact.onCreateInvocation();
				/* Add the Link to the Invocation's UIElements. */
				lInvocation.getUIElements().add(lLink);
			}
		}
		/* Synchronize along the SourceDecoupler. */
		synchronized(this.getSourceDecoupler()) {
			/* Fetch the SinkDecoupler's UIElements. */
			final List<IContact<IContact.Link>> lUIElements = this.getSourceDecoupler().getUIElements();
			/* Iterate the UIElements. */
			for(final IContact<IContact.Link> lContact : lUIElements) {
				/* Create a corresponding link for the Contact. */
				final IContact.Link lLink = lContact.onCreateInvocation();
				/* Add the Link to the Invocation's UIElements. */
				lInvocation.getUIElements().add(lLink);
			}
		}
		/* Finally, return the created Invocation. */
		return lInvocation;
	}

}