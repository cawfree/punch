package uk.ac.manchester.sisp.punch;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import uk.ac.manchester.sisp.punch.ui.IUIElement;
import uk.ac.manchester.sisp.punch.ui.core.Field;
import uk.ac.manchester.sisp.punch.ui.core.IGroup;
import uk.ac.manchester.sisp.punch.ui.core.IIcon;
import uk.ac.manchester.sisp.punch.ui.core.ISVGIcon;
import uk.ac.manchester.sisp.punch.ui.core.context.IContext;
import uk.ac.manchester.sisp.punch.ui.courier.IUICourier;
import uk.ac.manchester.sisp.punch.ui.key.UIKeyEvent;
import uk.ac.manchester.sisp.punch.ui.update.EUICommand;
import uk.ac.manchester.sisp.ribbon.common.IBounds2;
import uk.ac.manchester.sisp.ribbon.common.IDim2;
import uk.ac.manchester.sisp.ribbon.common.IScale;
import uk.ac.manchester.sisp.ribbon.common.IVec2;
import uk.ac.manchester.sisp.ribbon.font.IFont;
import uk.ac.manchester.sisp.ribbon.font.global.FontGlobal;
import uk.ac.manchester.sisp.ribbon.image.svg.SVGDecoder;
import uk.ac.manchester.sisp.ribbon.image.svg.SVGImage;
import uk.ac.manchester.sisp.ribbon.io.ArrayStore;
import uk.ac.manchester.sisp.ribbon.io.EEntryMode;
import uk.ac.manchester.sisp.ribbon.opengl.GLContext;
import uk.ac.manchester.sisp.ribbon.opengl.IGL;
import uk.ac.manchester.sisp.ribbon.opengl.IGLES20;
import uk.ac.manchester.sisp.ribbon.opengl.IGLRunnable;
import uk.ac.manchester.sisp.ribbon.opengl.IScreenParameters;
import uk.ac.manchester.sisp.ribbon.opengl.buffers.GLBuffer;
import uk.ac.manchester.sisp.ribbon.opengl.buffers.GLBufferPackage;
import uk.ac.manchester.sisp.ribbon.opengl.matrix.GLMatrix;
import uk.ac.manchester.sisp.ribbon.opengl.program.constants.GLVectorProgram;
import uk.ac.manchester.sisp.ribbon.opengl.text.GLTextRenderer;
import uk.ac.manchester.sisp.ribbon.opengl.vector.VectorPathContext;
import uk.ac.manchester.sisp.ribbon.opengl.vector.global.IPathDefinition;
import uk.ac.manchester.sisp.ribbon.opengl.vector.global.IVectorPathGroup;
import uk.ac.manchester.sisp.ribbon.ui.global.UIGlobal;
import uk.ac.manchester.sisp.ribbon.utils.DataUtils;
import uk.ac.manchester.sisp.ribbon.utils.GLUtils;
import uk.ac.manchester.sisp.ribbon.utils.MathUtils;

/** TODO: Buffer a set of block dimensions to improve buffer usage? **/
/** TODO: We create a new set of vertices for every SVGIcon. Terrible approach! Refactor. **/
public final class PunchView extends GLContext implements IUICourier<EUICommand> {
	
	/* Define the VGBuffer. (Enables the persistence of mixed vertex data between frames.) */
	private static final class VGBufferPackage extends GLBufferPackage<GLBuffer.XY_UV> implements IPathDefinition.Group {
		/* Factory Implementation. */
		public static final VGBufferPackage onFactoryCreate(final ArrayStore.Float pFloatStore, final VectorPathContext pVectorPathContext, final ArrayStore.Integer pIntegerStore, final IVectorPathGroup[] pVectorPathGroups) {
			/* Allocate a list to hold the triangulatable set of PathDefinitions. */
			final List<IPathDefinition> lPathDefinitions  = new ArrayList<IPathDefinition>();
			
			/** TODO: Reconcile why. **/
			if(pVectorPathGroups.length == 0) {
				return new VGBufferPackage(DataUtils.delegateNative(new float[]{}), new int[]{}, new IPathDefinition[]{});
			}
			
			/* Iterate through the VectorPathGroups. */
			for(int i = 0; i < pVectorPathGroups.length; i++) {
				/* Fetch the current VectorPathGroup. */
				final IVectorPathGroup lVectorPathGroup = pVectorPathGroups[i];
				/* Iterate through the PathDefinitions. */
				for(int j = 0; j < lVectorPathGroup.getPathDefinitions().length; j++) {
					/* Fetch the PathDefinition. */
					final IPathDefinition lPathDefinition = lVectorPathGroup.getPathDefinitions()[j];
					/* The PathDefinition is triangulatable. */
					if(lPathDefinition.isTriangulatable()) {
						/* Buffer the PathDefinition. */
						lPathDefinitions.add(lPathDefinition);
						/* Iterate through each VectorPath. */
						for(int k = 0; k < lVectorPathGroup.getVectorPaths().length; k++) {
							/* Have the current PathDefinition buffer the VectorPath. */
							lPathDefinition.onTriangulate(pFloatStore, pVectorPathContext, lVectorPathGroup.getVectorPaths()[k]);
						}
						/* Update the current offset of the ArrayStore as the index for this VectorPathGroup. */
						pIntegerStore.store(new int[]{ (pFloatStore.getNumberOfElements() >> 2) });
					}
				}
			}
			/* Instantiate the VGBufferPackage. Initialize using a SubscriptionCount of 1 . */
			return new VGBufferPackage(DataUtils.delegateNative(pFloatStore.onProduceArray()), pIntegerStore.onProduceArray(), lPathDefinitions.toArray(new IPathDefinition[lPathDefinitions.size()]));
		};
		/* Member Variables. */
		private final IPathDefinition[] mPathDefinitions;
		/* Local Constructor. Initializes to a SubscriptionCount of 1. */
		private VGBufferPackage(final ByteBuffer pByteBuffer, final int[] pIndices, final IPathDefinition[] pPathDefinitions) {
			super(new GLBuffer.XY_UV(pByteBuffer, IGLES20.GL_ARRAY_BUFFER, IGLES20.GL_STATIC_DRAW), pIndices);
			/* Initialize Member Variables. */
			this.mPathDefinitions   = pPathDefinitions;
		}
		/* Getter and setter implementations. */
		@Override public final IPathDefinition[] getPathDefinitions()                               { return this.mPathDefinitions;                 }
	};
	
	/* Member Variables. */
	private final PunchModel         mPunchModel;
	private final GLVectorProgram    mGLVectorProgram;
	private final ArrayStore.Float   mFloatStore;
	private final ArrayStore.Integer mIntegerStore;
	private final VectorPathContext  mVectorPathContext;
	
	/* Buffer Mappings. */
	private final Map<IUIElement, VGBufferPackage> mVertexMap;
	private final Map<File,       GLTextRenderer>  mFontMap;
	
	/* Render Allocations. */
	private final    IVec2.F.W           mRenderOffset;
	private final     IScale.W           mRenderScale;
	private final LinkedList<Float>      mOpacityStack;
	private final LinkedList<IBounds2.I> mScissorStack;
	
	public PunchView(final PunchModel pPunchModel) throws IOException {
		super();
		/* Initialize Member Variables. */
		this.mPunchModel          = pPunchModel;
		this.mGLVectorProgram     = new GLVectorProgram();
		this.mVectorPathContext   = new VectorPathContext();
		this.mFloatStore          = new ArrayStore.Float();
		this.mIntegerStore        = new ArrayStore.Integer();
		/* Schedule for execution upon the next graphics iteration. */
		this.invokeLater(new IGLRunnable() { @Override public final void run(final IGLES20 pGLES20, final GLContext pGLContext) {
			/* Supply an active OpenGL context to GL dependencies. */
			pGLContext.onHandleDelegates(EEntryMode.SUPPLY, pGLES20, getGLVectorProgram().getVertexShader(), getGLVectorProgram().getFragmentShader(), getGLVectorProgram());
		} });
		/* Initialize Buffer Mappings. */
		this.mVertexMap     = new HashMap<IUIElement, VGBufferPackage>(0);
		this.mFontMap       = new HashMap<File,       GLTextRenderer> (0); 
		/* Initialize the contents of the FontMap. (This is an extremely rudimentary approach.) */
		for(final Entry<File, IFont> lEntry : this.getPunchModel().getFontMap().entrySet()) {
			/* Buffer the corresponding GLTextRenderer. */
			this.getFontMap().put(lEntry.getKey(), new GLTextRenderer(UIKeyEvent.CHARSET_SIMPLE_ENGLISH, lEntry.getValue(), this, this.getFloatStore(), this.getVectorPathContext()));
		}
		/* Initialize Render Allocations. */
		this.mRenderOffset  = new    IVec2.F.Impl();
		this.mRenderScale   = new     IScale.Impl(UIGlobal.UI_UNITY);
		this.mOpacityStack  = new LinkedList<Float>();
		this.mScissorStack  = new LinkedList<IBounds2.I>();
	}
	
	@Override public final void onRenderFrame(final IGLES20 pGLES20, final float pCurrentTimeSeconds) {
		/* Maintain a local reference to ourself. */
		final GLContext lGLContext  = this; /** TODO: Refactor to resupply context via parameters.... **/
		/* Define the UIRenderCourier. */
		final IUICourier<IUICourier<?>> lUIRenderCourier = new IUICourier<IUICourier<?>>() {
			/* Render a Field. */
			@Override public final void onCourierTransit(final Field pField, final IUICourier<?> pCourierPackage) {
				/* Fetch the Field's Font. */
				final IFont          lFont           = getPunchModel().getFontMap().get(pField.getFont());
				/* Fetch the corresponding GLTextRenderer within the FontMap. */
				final GLTextRenderer lGLTextRenderer = getFontMap().get(pField.getFont());
				/* Calculate the FontScale. */
				float lFontScale = lFont.getFontScale(getDotsPerInch(), pField.getPointSize());
				/* Calculate the Descent. */
				float lDescent   = (float)Math.ceil(lFont.getDescent() * lFontScale);
				/* Force Pixel-Perfect Rendering. */
				getGLVectorProgram().onSupplyPixelPerfect(pGLES20, true);
				/* Supply the Font Color. */
				getGLVectorProgram().onSupplyColor(pGLES20, pField.getColor());
				/* Supply the FontScale. */
				getGLVectorProgram().onSupplyScale(pGLES20, lFontScale, (-1.0f * lFontScale)); 
				/** TODO: Abstract this calculation, it's too dispersed around the application. **/
				final float lLineHeight = Math.round(FontGlobal.onCalculateLineHeight(lFont, lFont.getFontScale(getDotsPerInch(), pField.getPointSize()), UIKeyEvent.CHARSET_SIMPLE_ENGLISH));
				/* Offset the Model Matrix by the text height to account for co-ordinate specification differences. */
				GLMatrix.translateM(getModelMatrix(), 0.0f, lLineHeight + lDescent, 0.0f);
				/* Render the ApplicationTitle using the DiagramTextRenderer. */
				lGLTextRenderer.onRenderText(pGLES20, lGLContext, getGLVectorProgram(), pField.getText(), lFontScale);
				/* Supply a Uniform RenderScale for future iterations. */
				getGLVectorProgram().onSupplyScale(pGLES20, UIGlobal.UI_UNITY, UIGlobal.UI_UNITY);
				/* Disengage Pixel-Perfect Rendering. */
				getGLVectorProgram().onSupplyPixelPerfect(pGLES20, false);
			}
			/* Render a IGroup. */
			@Override public final <T extends IUIElement> void onCourierTransit(final IGroup<T> pGroup, final IUICourier<?> pCourierPackage) { /** TODO: Specify specific scissorbounds, like a margin! This will allow us to scissor a certain region of a group. **/
				/* Synchronize along the Group. */
				synchronized(pGroup) {
					/* Determine if the Group is Scissors it's contents. */
					final boolean lIsScissor = pGroup.isScissorContents();
					/* Determine whether to implement scissoring about the contents. */
					if(lIsScissor) {
						/* Calculate the ScaledWidth and ScaledHeight. */
						final float lScaledWidth  =  pGroup.getWidth() * getRenderScale().getScale();
						final float lScaledHeight = pGroup.getHeight() * getRenderScale().getScale();
						/* Calculate the render bounds of the current Group. (In OpenGL space!) */
						final IBounds2.I.W lRenderBounds   = new IBounds2.I.Impl((int)Math.floor(getRenderOffset().getX()), (int)Math.floor(getScreenHeight() - (lScaledHeight + getRenderOffset().getY())), (int)Math.floor(getRenderOffset().getX() + lScaledWidth), (int)Math.ceil(getScreenHeight() - getRenderOffset().getY()));
						/* Peek at the existing bounds. */
						final IBounds2.I   lExistingBounds = getScissorStack().peek();
						/* Perform tests against the bounds. */
						lRenderBounds.setMinimumX(Math.max(lRenderBounds.getMinimumX(), lExistingBounds.getMinimumX()));
						lRenderBounds.setMaximumX(Math.min(lRenderBounds.getMaximumX(), lExistingBounds.getMaximumX()));
						/* Test inverse OpenGL co-ordinates. (RibbonGL's axis is the inverse of OpenGL's.) */
						lRenderBounds.setMinimumY(Math.max(lRenderBounds.getMinimumY(), lExistingBounds.getMinimumY()));
						lRenderBounds.setMaximumY(Math.min(lRenderBounds.getMaximumY(), lExistingBounds.getMaximumY()));
						/* Buffer the RenderBounds. */
						getScissorStack().push(lRenderBounds);
						/* Write the bounds. */
						onDelegateScissor(pGLES20);
					}
					/* First, render the IGroup as if it were an IIcon. */
					this.onCourierTransit((IIcon)pGroup, pCourierPackage);
					/* Fetch a safe clone of the IGroup's UIElements. */
					final List<T> lUIElements = pGroup.getUIElements();
					/* Iterate across the UIElements. */
					for(int i = 0; i < lUIElements.size(); i++) {
						/* Render the UIElement. */
						onRenderUIElement(pGLES20, lUIElements.get(i), this);
					}
					/* Determine if we Scissor the Group. */
					if(lIsScissor) {
						/* Withdraw the RenderBounds. */
						getScissorStack().pop();
						/* Restore the previous Scissor. */
						onDelegateScissor(pGLES20);
					}
				}
			}
			/* Render an ISVGIcon. */
			@Override public final void onCourierTransit(final ISVGIcon pSVGIcon, final IUICourier<?> pCourierPackage) {
				/* Allocate a reference to the VGBufferPackage. */
				final VGBufferPackage lSVGBufferPackage = getVertexMap().get(pSVGIcon);
				/* Ensure the package isn't null. */
				if(DataUtils.isNotNull(lSVGBufferPackage)) {
					/* Bind to the GLBuffer. */
					lSVGBufferPackage.getGLBuffer().bind(pGLES20, getGLVectorProgram());
					/* Allocate the BufferOffset. */
					int lBufferOffset = 0;
					/* Iterate through the IIcon's colours. */
					for(int i = 0; i < lSVGBufferPackage.getPathDefinitions().length; i++) {
						/* Supply the colour. */
						getGLVectorProgram().onSupplyColor(pGLES20, lSVGBufferPackage.getPathDefinitions()[i].getColor());
						/* Render the triangulated region. */
						pGLES20.glDrawArrays(IGL.GL_TRIANGLES, lBufferOffset, lSVGBufferPackage.getIndices()[i] - lBufferOffset);
						/* Update the Buffer */
						lBufferOffset = lSVGBufferPackage.getIndices()[i];
					}
				}
			}
			@Override public final void onCourierTransit(final ISVGIcon.IFlat pFlatIcon, final IUICourier<?> pCourierPackage) {
				/* Allocate a reference to the VGBufferPackage. */
				final VGBufferPackage lSVGBufferPackage = getVertexMap().get(pFlatIcon);
				/* Ensure the package isn't null. */
				if(DataUtils.isNotNull(lSVGBufferPackage)) {
					/* Bind to the GLBuffer. */
					lSVGBufferPackage.getGLBuffer().bind(pGLES20, getGLVectorProgram());
					/* Supply the colour. */
					getGLVectorProgram().onSupplyColor(pGLES20, pFlatIcon.getColor());
					/* Draw the entire path as a whole. */
					pGLES20.glDrawArrays(IGL.GL_TRIANGLES, 0, lSVGBufferPackage.getIndices()[lSVGBufferPackage.getIndices().length - 1]);
				}
			}
			/* Handle an IIcon. */
			@Override public final void onCourierTransit(final IIcon pIcon, final IUICourier<?> pCourierPackage) {
				/* Fetch the IIcon's VGBufferPackage. */
				final VGBufferPackage lVGBufferPackage = getVertexMap().get(pIcon);
				/* Ensure the package isn't null. */
				if(DataUtils.isNotNull(lVGBufferPackage)) {
					/* Bind to the GLBuffer. */
					lVGBufferPackage.getGLBuffer().bind(pGLES20, getGLVectorProgram());
					/* Allocate the BufferOffset. */
					int lBufferOffset = 0;
					/* Iterate through the IIcon's colours. */
					for(int i = 0; i < lVGBufferPackage.getPathDefinitions().length; i++) {
						/* Supply the colour. */
						getGLVectorProgram().onSupplyColor(pGLES20, lVGBufferPackage.getPathDefinitions()[i].getColor());
						/* Render the triangulated region. */
						pGLES20.glDrawArrays(IGL.GL_TRIANGLES, lBufferOffset, lVGBufferPackage.getIndices()[i] - lBufferOffset );
						/* Update the Buffer */
						lBufferOffset = lVGBufferPackage.getIndices()[i];
					}
				}
			}
		};
		/* Initialize the ScissorStack. */
		this.getScissorStack().push(new IBounds2.I.Impl(0, 0, this.getScreenWidth(), this.getScreenHeight())); // (X, Y, X + W, Y + H) ~~
		/* Synchronize along the Contexts. */
		synchronized(this.getPunchModel().getContexts()) {
			/* Iterate the Contexts. */
			for(final IContext lContext : this.getPunchModel().getContexts()) {
				/* Synchronize along the Context. */
				synchronized(lContext) {
					/* Re-initialize the RenderOffset. */
					MathUtils.onWithdrawOffset(this.getRenderOffset(), this.getRenderOffset());
					/* Allocate the ScaleBuffer. */
					this.getRenderScale().setScale(lContext.getScale());
					/* Configure the Projection matrix as an Orthographic projection. */
					GLMatrix.setMatrixOrthographic(this.getProjectionMatrix(), 0, this.getScreenWidth(), this.getScreenHeight(), 0, Float.MIN_VALUE, Float.MAX_VALUE);
					/* Set the Model Matrix as an Identity. */
					GLMatrix.setIdentityM(this.getModelMatrix());
					/* Set the View Matrix as an Identity. */
					GLMatrix.setIdentityM(this.getViewMatrix());
					/* Reset the GLVectorProgram's scale. */
					GLUtils.onReinitializeScale(pGLES20, this.getGLVectorProgram());
					/* Bind to the GLVectorProgram. */
					this.getGLVectorProgram().bind(pGLES20);
					/* Supply the Resolution. */
					this.getGLVectorProgram().onSupplyResolution(pGLES20, this);
					/* Initialize the Opacity. */
					this.getGLVectorProgram().onSupplyOpacity(pGLES20, UIGlobal.UI_UNITY);
					/* Disengage Pixel-Perfect Rendering. */
					this.getGLVectorProgram().onSupplyPixelPerfect(pGLES20, false);
					/* Update the ViewMatrix. */
					//GLMatrix.translateM(this.getViewMatrix(), -1.0f * this.getPunchModel().getX(),     -1.0f * this.getPunchModel().getY(),     0.0f);
					GLMatrix.scaleM(    this.getViewMatrix(),         lContext.getScale(),         lContext.getScale(), UIGlobal.UI_UNITY);
					/* Update the GLVectorProgram's World Matrices. */
					GLUtils.onUpdateWorldMatrices(pGLES20, this.getGLVectorProgram(), this);
					/* Initialize the ShadeMix. */
					this.getGLVectorProgram().onSupplyShadeMix(pGLES20, 0.0f);
					/* Delegate the ScissorStack onto the GPU. */
					this.onDelegateScissor(pGLES20);
					/* Render the Context. */
					this.onRenderUIElement(pGLES20, lContext, lUIRenderCourier);
				}
			}
		}
		/* Remove the initial bounds resting upon the ScissorStack. */
		this.getScissorStack().pop();
	}
	
	private final boolean onDelegateScissor(final IGLES20 pGLES20) {
		/* Fetch the current ScissorBounds. */
		final IBounds2.I lScissorBounds = this.getScissorStack().peek();
		/* Sometimes ScissorBounds can be null. We need to find out why! */
		if(DataUtils.isNull(lScissorBounds)) {
			/* Inform the developer. */
			System.err.println("Detected bad scissor.!");
			/* Assert that there was an error. */
			return false;
		}
		else {
			/* Delegate the ScissorBounds. */
			pGLES20.glScissor(lScissorBounds.getMinimumX(), lScissorBounds.getMinimumY(), (lScissorBounds.getMaximumX() - lScissorBounds.getMinimumX()), (lScissorBounds.getMaximumY() - lScissorBounds.getMinimumY()));
			/* Assert we successfully delegated the Scissor. */
			return true;
		}
	}
	
	public final void onRenderUIElement(final IGLES20 pGLES20, final IUIElement pUIElement, final IUICourier<IUICourier<?>> pUICourier) {
		/* Buffer the resulting Opacity. */
		this.getOpacityStack().push(this.getOpacityStack().isEmpty() ? pUIElement.getOpacity() : pUIElement.getOpacity() * this.getOpacityStack().peek());
		/* Supply the Opacity. */
		this.getGLVectorProgram().onSupplyOpacity(pGLES20, this.getOpacityStack().peek());
		/* Supply the RenderOffset. */
		MathUtils.onSupplyOffset(this.getRenderOffset(), pUIElement.getX() * this.getRenderScale().getScale(), pUIElement.getY() * this.getRenderScale().getScale());
		/* Determine whether the UIElement can be drawn. */
		/** TODO: IScissorable instead of this bad solution **/
		if(pUIElement.isVisible() && ((!pUIElement.isScissorable()) || ((this.isOnScreen(pUIElement))))) { /** TODO: And if encapsulated within the RenderBounds!!! **/
			/* Prepare the matrices for a render operation within the bounds of the UIElement. */
			this.onPrepareRender(pGLES20, pUIElement);
			/* Render the UIElement using the UICourier. */
			pUIElement.onCourierDispatch(pUICourier, this);
			/* Withdraw the local matrix modifcations. */
			this.onWithdrawRender(pUIElement);
		}
		/* Withdraw the RenderOffset. */
		MathUtils.onWithdrawOffset(this.getRenderOffset(), pUIElement.getX() * this.getRenderScale().getScale(), pUIElement.getY() * this.getRenderScale().getScale());
		/* Withdraw the Opacity. */
		this.getOpacityStack().pop();
	}
	
	@Override public void onCourierTransit(final Field pField, final EUICommand pUICommand) { 
		/** TODO: Handle dynamic constuction and destruction of IFonts here. **/
	}
	
	@Override public final <T extends IUIElement>  void onCourierTransit(final IGroup<T> pGroup, final EUICommand pUICommand) {
		/* Handle the UICommand conditionally. */
		switch(pUICommand) {
			case CREATE  : 
			case DESTROY : 
				/* Attain exclusive access to the IGroup. */
				synchronized(pGroup) {
					/* Fetch a safe clone of the IGroup's UIElements. */
					final List<T> lUIElements = pGroup.getUIElements();
					/* Dispatch to all UIElements within the group. */
					for(final IUIElement lUIElement : lUIElements) {
						/* Dispatch the UICommand. */
						lUIElement.onCourierDispatch(this, pUICommand);
					}
				}
			break;
			case UPDATE  : /* This command only applies to the IGroup itself. */ break;
		}
		/* Finally, handle the IGroup as if it were a standard IIcon. */
		this.onCourierTransit((IIcon)pGroup, pUICommand);
	}
	
	@Override public final void onCourierTransit(final IIcon pIcon, final EUICommand pUICommand) {
		/* Handle the UICommand. */
		switch(pUICommand) {
			case CREATE  :
			case UPDATE  :
				/* Fetch the IIcon's VectorPathGroups. */
				final IVectorPathGroup[] lVectorPathGroups = pIcon.getVectorPathGroups(this.getFloatStore(), this.getVectorPathContext());
				/* Encapsulate the groups as a VGBufferPackage. */
				final VGBufferPackage   lSVGBufferPackage = VGBufferPackage.onFactoryCreate(this.getFloatStore(), this.getVectorPathContext(), this.getIntegerStore(), lVectorPathGroups);
				/* Export operation on the next graphics iteration. */
				this.invokeLater(new IGLRunnable() { @Override public final void run(final IGLES20 pGLES20, final GLContext pGLContext) {
					/* Determine if the VertexMap contains an existing entry. */
					if(getVertexMap().containsKey(pIcon)) {
						/* Remove the existing entry and unload it. */
						pGLContext.onHandleDelegates(EEntryMode.WITHDRAW, pGLES20, getVertexMap().remove(pIcon).getGLBuffer());
					}
					/* Deploy the generated GLBufferPackage. */
					pGLContext.onHandleDelegates(EEntryMode.SUPPLY, pGLES20, lSVGBufferPackage.getGLBuffer());
					/* Push the GLBufferPackage onto the VertexMap. */
					getVertexMap().put(pIcon, lSVGBufferPackage);
				} });
			break;
			case DESTROY : 
				/* Export operation on the next graphics iteration. */
				this.invokeLater(new IGLRunnable() { @Override public final void run(final IGLES20 pGLES20, final GLContext pGLContext) {
					/* Determine if the VertexMap contains an existing entry. */
					if(getVertexMap().containsKey(pIcon)) {
						/* Remove the existing entry and unload it. */
						pGLContext.onHandleDelegates(EEntryMode.WITHDRAW, pGLES20, getVertexMap().remove(pIcon).getGLBuffer());
					}
				} });
			break;
		}
	}
	
	/* We synchronize on this method to ensure sequential interaction with the SVGMap. */
	@Override public final void onCourierTransit(final ISVGIcon pSVGIcon, final EUICommand pUICommand) {
		/* Allocate an SVGDecoder. */
		final SVGDecoder lSVGDecoder = new SVGDecoder();
		/* Process the UICommand. */
		switch(pUICommand) {
			case CREATE : 
				/* Allocate a reference to the VGBufferPackage. */
				final VGBufferPackage lSVGBufferPackage;
				/* Attempt to read the SVG Image. */
				try {
					/* Decode the SVGImage using the SVGDecoder. */
					final SVGImage lSVGImage  = lSVGDecoder.createFromFile(pSVGIcon.getFile(), this.getFloatStore(), this.getVectorPathContext(), this);
					/* Fetch the VectorPathGroups. */ 
					final IVectorPathGroup[]    lVectorPathGroups = lSVGImage.getVectorPathGroups(this.getFloatStore(), this.getVectorPathContext());
					/* Create the VGBufferPackage. */
					lSVGBufferPackage = VGBufferPackage.onFactoryCreate(this.getFloatStore(), this.getVectorPathContext(), this.getIntegerStore(), lVectorPathGroups);
				}
				catch(final IOException pIOException) {
					/* Throw a RuntimeException. */ /** TODO: Handle gracefully? We should never get here. **/
					throw new RuntimeException(pIOException);
				}
				/* Schedule for execution on the next graphics thread. */
				this.invokeLater(new IGLRunnable() { @Override public final void run(final IGLES20 pGLES20, final GLContext pGLContext) {
					/* Deploy the VGBufferPackage. */
					pGLContext.onHandleDelegates(EEntryMode.SUPPLY, pGLES20, lSVGBufferPackage.getGLBuffer());
					/* Place the VGBufferPackage into the SVGMap. */
					getVertexMap().put(pSVGIcon, lSVGBufferPackage);
				} });
			break;
			case DESTROY : 
				/* Schedule for execution on the next graphics thread. */
				this.invokeLater(new IGLRunnable() { @Override public final void run(final IGLES20 pGLES20, final GLContext pGLContext) {
					/* Look up the existing VGBufferPackage. */
					final VGBufferPackage lSVGBufferPackage = getVertexMap().get(pSVGIcon);
					/* Ensure it actually exists. */
					if(DataUtils.isNotNull(lSVGBufferPackage)) { /** TODO: This only really happens during initialization, I suppose (see Menu constructor). Should implement some query isAlive on invokeAndWait type test during initialization... Unsafe until then. Though we are synchronized. Hm. **/
						/* Delete the VGBufferPackage. */
						pGLContext.onHandleDelegates(EEntryMode.WITHDRAW, pGLES20, lSVGBufferPackage.getGLBuffer());
						/* Remove the entry from the SVGMap. */
						getVertexMap().remove(pSVGIcon);
					}
				} });
			break;
			default : /* Refuse to handle other cases. */ break;
		}
	}

	@Override public final void onCourierTransit(final ISVGIcon.IFlat pFlatIcon, final EUICommand pUICommand) {
		/* Handle as a generic ISVGIcon. */
		this.onCourierTransit((ISVGIcon)pFlatIcon, pUICommand);
	}
	
	// (Defunct test, pure screen co-ordinates.)
	// return (((this.getRenderOffset().getX()) < this.getScreenWidth()) && (((this.getRenderOffset().getX() + (pUIElement.getWidth()) * this.getRenderScale().getScale())) > 0.0f)) && (((this.getRenderOffset().getY()) < this.getScreenHeight()) && (((this.getRenderOffset().getY()) + (pUIElement.getHeight() * this.getRenderScale().getScale())) > 0.0f));
	private final boolean isOnScreen(final IUIElement pUIElement) {
		/* Calculate the ScaledWidth and ScaledHeight of the UIElement. */
		final float lScaledWidth  = this.getRenderScale().getScale() * pUIElement.getWidth();
		final float lScaledHeight = this.getRenderScale().getScale() * pUIElement.getHeight();
		/* Peek the ScissorBounds. */
		final IBounds2.I lScissorBounds = this.getScissorStack().peek();
		/* Test broad intersection with the bounding box. */ /** TODO: Abstract to a generic test; this is inf **/
		return ((this.getRenderOffset().getX() + lScaledWidth) > lScissorBounds.getMinimumX()) && (this.getRenderOffset().getX() < lScissorBounds.getMaximumX()) && ((getScreenHeight() - (lScaledHeight + getRenderOffset().getY())) < lScissorBounds.getMaximumY()) && ((getScreenHeight() - getRenderOffset().getY()) > lScissorBounds.getMinimumY());
	}
	
	private final <T extends IVec2.I & IDim2> void onPrepareRender(final IGLES20 pGLES20, final T pT) {
		/* Push the ModelMatrix onto the Matrix Stack. */
		this.getMatrixStack().push(this.getModelMatrix().clone());
		/* Translate the Model Matrix. */
		GLMatrix.translateM(getModelMatrix(), pT.getX(), pT.getY(), 0.0f);
		/* Supply the UniformProvider with the updated Model Matrix. */
		this.getGLVectorProgram().onSupplyModelMatrix(pGLES20, this);
	}
	
	private final <T extends IVec2 & IDim2> void onWithdrawRender(final T pT) {
		/* Pop the ModelMatrix off the Matrix Stack and restore the implementation. */
		System.arraycopy(getMatrixStack().pop(), 0, getModelMatrix(), 0, getModelMatrix().length);
	}

	@Override
	public final void onScreenParametersChanged(final IScreenParameters pScreenParameters) {
		/* Deploy the event to the PunchModel. */
		this.getPunchModel().onScreenParametersChanged(pScreenParameters);
	}

	@Override public final float getDotsPerInch() {
		return this.getPunchModel().getDotsPerInch();
	}
	
	@Override
	public final int getScreenWidth() {
		return this.getPunchModel().getScreenWidth();
	}

	@Override
	public final int getScreenHeight() {
		return this.getPunchModel().getScreenHeight();
	}
	
	public final PunchModel getPunchModel() {
		return this.mPunchModel;
	}
	
	private final GLVectorProgram getGLVectorProgram() {
		return this.mGLVectorProgram;
	}
	
	private final ArrayStore.Float getFloatStore() {
		return this.mFloatStore;
	}
	
	private final ArrayStore.Integer getIntegerStore() {
		return this.mIntegerStore;
	}
	
	private final VectorPathContext getVectorPathContext() {
		return this.mVectorPathContext;
	}
	
	private final Map<File, GLTextRenderer> getFontMap() {
		return this.mFontMap;
	}
	
	private final Map<IUIElement, VGBufferPackage> getVertexMap() {
		return this.mVertexMap;
	}
	
	private final IVec2.F.W getRenderOffset() {
		return this.mRenderOffset;
	}
	
	private final LinkedList<IBounds2.I> getScissorStack() {
		return this.mScissorStack;
	}
	
	private final IScale.W getRenderScale() {
		return this.mRenderScale;
	}
	
	private final LinkedList<Float> getOpacityStack() {
		return this.mOpacityStack;
	}
	
}