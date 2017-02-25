package uk.ac.manchester.sisp.punch.ui.core.context.constants;

import java.util.ArrayList;
import java.util.List;

import uk.ac.manchester.sisp.punch.PunchModel;
import uk.ac.manchester.sisp.punch.ui.IUIElement;
import uk.ac.manchester.sisp.punch.ui.collision.RayCastManager;
import uk.ac.manchester.sisp.punch.ui.color.global.ColorGlobal;
import uk.ac.manchester.sisp.punch.ui.core.Field;
import uk.ac.manchester.sisp.punch.ui.core.IGroup;
import uk.ac.manchester.sisp.punch.ui.core.context.IContext;
import uk.ac.manchester.sisp.punch.ui.core.search.SearchBox;
import uk.ac.manchester.sisp.punch.ui.distribution.IDistribution;
import uk.ac.manchester.sisp.punch.ui.distribution.constants.LinearDistribution;
import uk.ac.manchester.sisp.punch.ui.distribution.global.DistributionGlobal;
import uk.ac.manchester.sisp.punch.ui.drag.global.DragGlobal;
import uk.ac.manchester.sisp.punch.ui.lexicon.ILexical;
import uk.ac.manchester.sisp.punch.ui.lexicon.coupling.Coupling;
import uk.ac.manchester.sisp.punch.ui.lexicon.data.global.DataGlobal;
import uk.ac.manchester.sisp.punch.ui.lexicon.data.global.EDataDirection;
import uk.ac.manchester.sisp.punch.ui.lexicon.decoupler.axiom.Invocation;
import uk.ac.manchester.sisp.punch.ui.lexicon.diagram.Diagram;
import uk.ac.manchester.sisp.punch.ui.lexicon.documentation.Comment;
import uk.ac.manchester.sisp.punch.ui.lexicon.functions.Elapsed;
import uk.ac.manchester.sisp.punch.ui.lexicon.functions.Nand;
import uk.ac.manchester.sisp.punch.ui.lexicon.functions.Print;
import uk.ac.manchester.sisp.punch.ui.lexicon.global.LexiconGlobal;
import uk.ac.manchester.sisp.punch.ui.lexicon.literal.array.Array;
import uk.ac.manchester.sisp.punch.ui.lexicon.literal.constants.Dharma;
import uk.ac.manchester.sisp.punch.ui.lexicon.parameter.contact.IContact;
import uk.ac.manchester.sisp.punch.ui.lexicon.parameter.contact.constants.Feedback;
import uk.ac.manchester.sisp.punch.ui.lexicon.parameter.contact.constants.Iteration;
import uk.ac.manchester.sisp.punch.ui.lexicon.parameter.contact.constants.Selector;
import uk.ac.manchester.sisp.punch.ui.lexicon.parameter.contact.constants.Tunnel;
import uk.ac.manchester.sisp.punch.ui.lexicon.sequencing.ISequential;
import uk.ac.manchester.sisp.punch.ui.update.EUICommand;
import uk.ac.manchester.sisp.punch.ui.update.UIUpdateDispatcher;
import uk.ac.manchester.sisp.punch.ui.update.UIUpdateEvent;
import uk.ac.manchester.sisp.punch.ui.update.easing.ui.UIEasingGroup;
import uk.ac.manchester.sisp.punch.ui.update.easing.ui.UIElementPacket;
import uk.ac.manchester.sisp.ribbon.common.IBounds2;
import uk.ac.manchester.sisp.ribbon.common.IVec2;
import uk.ac.manchester.sisp.ribbon.io.ArrayStore;
import uk.ac.manchester.sisp.ribbon.opengl.IScreenParameters;
import uk.ac.manchester.sisp.ribbon.opengl.vector.VectorPath;
import uk.ac.manchester.sisp.ribbon.opengl.vector.VectorPathContext;
import uk.ac.manchester.sisp.ribbon.opengl.vector.global.IPathDefinition;
import uk.ac.manchester.sisp.ribbon.opengl.vector.global.IVectorPathGroup;
import uk.ac.manchester.sisp.ribbon.ui.easing.IEasingConfiguration;
import uk.ac.manchester.sisp.ribbon.ui.easing.global.EEasingAlgorithm;
import uk.ac.manchester.sisp.ribbon.ui.pointer.EPointerAction;
import uk.ac.manchester.sisp.ribbon.ui.pointer.UIPointerEvent;
import uk.ac.manchester.sisp.ribbon.utils.DataUtils;
import uk.ac.manchester.sisp.ribbon.utils.MathUtils;
import uk.ac.manchester.sisp.ribbon.utils.ResourceUtils;

public final class MenuContext extends IContext.Impl {
	
	/* Define default Serialization UID. */
	private static final long serialVersionUID = 1L;
	
	/* Static Constants. */
	private static final int DIM_WIDTH_MENU = 300; 
	
	private static final Coupling<ILexical, ISequential<ILexical>> onEncodeNotGate(final PunchModel pPunchModel) {
		/* Allocate a Sequential Coupling. */
		final Coupling<ILexical, ISequential<ILexical>> lCoupling   = new Coupling<ILexical, ISequential<ILexical>>(0, 0, new ISequential.Impl<ILexical>(0,0,0,0));
		/* Fetch the Sequence. */
		final ISequential<ILexical>                     lSequential = lCoupling.getInternals().get(0);
		/* Allocate a Nand. */
		final Nand    lNandGate = new Nand(0, 0);
		/* Distribute the Nand. */
		DistributionGlobal.onDistributeHierarchy(pPunchModel, null, lNandGate, IEasingConfiguration.CONFIGURATION_NONE);
		/* Add the Nand to the Coupling. */
		lSequential.getUIElements().add(lNandGate);
		/* Reposition the Nand's Output. */
		lNandGate.getResult().setY(0);
		/* Align the Outputs. */
		lNandGate.getA().setY(0);
		lNandGate.getB().setY(0);
		/* Create the SinkTerminal. Point it towards the remaining undriven input of the Nand. */
		final IContact<IContact.Link> lSinkTerminal   = new IContact.Impl(0, 0,        ResourceUtils.getResource(MenuContext.class.getClassLoader(), "res/icon/contact/unary.svg"), DataGlobal.TYPE_DHARMA, EDataDirection.SINK);
		/* Place the Sinking Input along the SinkDecoupler. */
		lCoupling.getSinkDecoupler().getUIElements().add(lSinkTerminal);
		/* Allocate the SourceTerminal. */
		final IContact<IContact.Link> lSourceTerminal = new IContact.Impl(0, 0, ResourceUtils.getResource(MenuContext.class.getClassLoader(),          "res/icon/contact/not.svg"), DataGlobal.TYPE_DHARMA, EDataDirection.SOURCE);
		/* Place the Sourcing Output along the SourceDecoupler. */
		lCoupling.getSourceDecoupler().getUIElements().add(lSourceTerminal);
		/* Open it up. */
		lCoupling.onChangeState(pPunchModel, null, IEasingConfiguration.CONFIGURATION_NONE);
		/* Return the Coupling. */
		return lCoupling;
	}
	
	private static final Coupling<ILexical, ISequential<ILexical>> onEncodeAndGate(final PunchModel pPunchModel) {
		/* Allocate a Sequential Coupling. */
		final Coupling<ILexical, ISequential<ILexical>> lCoupling   = new Coupling<ILexical, ISequential<ILexical>>(0, 0, new ISequential.Impl<ILexical>(0,0,0,0));
		/* Fetch the Sequence. */
		final ISequential<ILexical>                     lSequential = lCoupling.getInternals().get(0);
		/* Allocate a Nand. */
		final Nand         lNandGate = new Nand(0, 0);
		/* Distribute the Nand. */
		DistributionGlobal.onDistributeHierarchy(pPunchModel, null, lNandGate, IEasingConfiguration.CONFIGURATION_NONE);
		/* Set the Output of the Nand to overwrite. */
		lNandGate.getResult().setY(0);
		/* Allocate a Call to the NotGate. */
		final Invocation lNotGate  = MenuContext.onEncodeNotGate(pPunchModel).onCreateInvocation();
		/* Distribute the NotGate. */
		DistributionGlobal.onDistributeHierarchy(pPunchModel, null, lNotGate, IEasingConfiguration.CONFIGURATION_NONE);
		/* Set the Output of the NotGate to overwrite. */
		lNotGate.getUIElements().get(1).setY(0);
		/* Set the Not Gate to the right of the Nand Gate. */
		lNotGate.setX(lNandGate.getX() + lNandGate.getWidth());
		/* Add the Nand and NotGate to the Coupling. */
		lSequential.getUIElements().add(lNandGate);
		lSequential.getUIElements().add(lNotGate);
		/* Allocate the two Sinking Inputs to the AndGate. */
		final IContact<IContact.Link> lA   = new IContact.Impl(0,                                  0, ResourceUtils.getResource(MenuContext.class.getClassLoader(), "res/icon/contact/unary.svg"), DataGlobal.TYPE_DHARMA, EDataDirection.SINK);
		final IContact<IContact.Link> lB   = new IContact.Impl(0, LexiconGlobal.CODE_DIM_HEIGHT_UNIT, ResourceUtils.getResource(MenuContext.class.getClassLoader(), "res/icon/contact/unary.svg"), DataGlobal.TYPE_DHARMA, EDataDirection.SINK);
		/* Add the inputs to the Coupling's SinkDecoupler. */
		lCoupling.getSinkDecoupler().getUIElements().add(lA);
		lCoupling.getSinkDecoupler().getUIElements().add(lB);
		/* Allocate the Sourcing Output of the AndGate. */
		final IContact<IContact.Link> lC   = new IContact.Impl(0,                                  0, ResourceUtils.getResource(MenuContext.class.getClassLoader(),  "res/icon/contact/and.svg"),  DataGlobal.TYPE_DHARMA, EDataDirection.SOURCE);
		/* Add the Output to the AndGate's SourceDecoupler. */
		lCoupling.getSourceDecoupler().getUIElements().add(lC);
		/* Open it up. */
		lCoupling.onChangeState(pPunchModel, null, IEasingConfiguration.CONFIGURATION_NONE);
		/* Return the Coupling. */
		return lCoupling;
	}
	
	/* Codes an Or Gate. */
	private static final Coupling<ILexical, ISequential<ILexical>> onEncodeOrGate(final PunchModel pPunchModel) { 
		/* Allocate a Sequential Coupling. */
		final Coupling<ILexical, ISequential<ILexical>> lCoupling   = new Coupling<ILexical, ISequential<ILexical>>(0, 0, new ISequential.Impl<ILexical>(0,0,0,0));
		/* Fetch the Sequence. */
		final ISequential<ILexical>                     lSequential = lCoupling.getInternals().get(0);
		/* Create a Not Gate. */
		final Coupling<ILexical, ISequential<ILexical>> lNotGate = MenuContext.onEncodeNotGate(pPunchModel);
		/* Create an And Gate. */
		final Coupling<ILexical, ISequential<ILexical>> lAndGate = MenuContext.onEncodeAndGate(pPunchModel);
		/* Allocate three NotGates. */
		final Invocation lNotGate0 = lNotGate.onCreateInvocation();
		final Invocation lNotGate1 = lNotGate.onCreateInvocation();
		final Invocation lNotGate2 = lNotGate.onCreateInvocation();
		/* Allocate an And Gate. */
		final Invocation lAndGate0 = lAndGate.onCreateInvocation();
		/* Distribute the Gates. */
		DistributionGlobal.onDistributeHierarchy(pPunchModel, null, lNotGate0, IEasingConfiguration.CONFIGURATION_NONE);
		DistributionGlobal.onDistributeHierarchy(pPunchModel, null, lNotGate1, IEasingConfiguration.CONFIGURATION_NONE);
		DistributionGlobal.onDistributeHierarchy(pPunchModel, null, lNotGate2, IEasingConfiguration.CONFIGURATION_NONE);
		DistributionGlobal.onDistributeHierarchy(pPunchModel, null, lAndGate0, IEasingConfiguration.CONFIGURATION_NONE);
		/* Organize the ISequential of the gates. */
		lNotGate1.setX(lNotGate0.getX() + lNotGate0.getWidth());
		lAndGate0.setX(lNotGate0.getX() + lNotGate0.getWidth());
		lNotGate2.setX(lAndGate0.getX() + lAndGate0.getWidth());
		/* Add the Gates to the Coupling. */
		lSequential.getUIElements().add(lNotGate0);
		lSequential.getUIElements().add(lNotGate1);
		lSequential.getUIElements().add(lNotGate2);
		lSequential.getUIElements().add(lAndGate0);
		/* Reposition the output of the first NotGate. */
		lNotGate0.getUIElements().get(1).setY(0);
		/* Reposition the input of the second NotGate. */
		lNotGate1.getUIElements().get(0).setY(LexiconGlobal.CODE_DIM_HEIGHT_UNIT);
		/* Reposition the output of the AndGate. */
		lAndGate0.getUIElements().get(2).setY(0);
		/* Reposition the output of the final NotGate. */
		lNotGate2.getUIElements().get(1).setY(0);
		/* Allocate the two Sinking Inputs to the OrGate. */
		final IContact<IContact.Link> lA   = new IContact.Impl(0,                                  0, ResourceUtils.getResource(MenuContext.class.getClassLoader(), "res/icon/contact/unary.svg"), DataGlobal.TYPE_DHARMA, EDataDirection.SINK);
		final IContact<IContact.Link> lB   = new IContact.Impl(0, LexiconGlobal.CODE_DIM_HEIGHT_UNIT, ResourceUtils.getResource(MenuContext.class.getClassLoader(), "res/icon/contact/unary.svg"), DataGlobal.TYPE_DHARMA, EDataDirection.SINK);
		/* Add the inputs to the Coupling's SinkDecoupler. */
		lCoupling.getSinkDecoupler().getUIElements().add(lA);
		lCoupling.getSinkDecoupler().getUIElements().add(lB);
		/* Allocate the Sourcing Output of the OrGate. */
		final IContact<IContact.Link> lC   = new IContact.Impl(0,                                  0, ResourceUtils.getResource(MenuContext.class.getClassLoader(),  "res/icon/contact/or.svg"),   DataGlobal.TYPE_DHARMA, EDataDirection.SOURCE);
		/* Add the Output to the AndGate's SourceDecoupler. */
		lCoupling.getSourceDecoupler().getUIElements().add(lC);
		/* Open it up. */
		lCoupling.onChangeState(pPunchModel, null, IEasingConfiguration.CONFIGURATION_NONE);
		/* Return the Coupling. */
		return lCoupling;
	}
	
	/* Codes an Nor Gate. */
	private static final Coupling<ILexical, ISequential<ILexical>> onEncodeNorGate(final PunchModel pPunchModel) { 
		/* Allocate a Sequential Coupling. */
		final Coupling<ILexical, ISequential<ILexical>> lCoupling   = new Coupling<ILexical, ISequential<ILexical>>(0, 0, new ISequential.Impl<ILexical>(0,0,0,0));
		/* Fetch the Sequence. */
		final ISequential<ILexical>                     lSequential = lCoupling.getInternals().get(0);
		/* Create an Or Gate and fetch the Invocation. */
		final Invocation lOrGate   = MenuContext.onEncodeOrGate(pPunchModel).onCreateInvocation();
		/* Create a Not Gate and fetch the Invocation. */
		final Invocation lNotGate  = MenuContext.onEncodeNotGate(pPunchModel).onCreateInvocation();
		/* Position the NotGate after the OrGate. */
		lNotGate.setX(lOrGate.getX() + lOrGate.getWidth());
		/* Add the Gates to the Coupling's Internals. */
		lSequential.getUIElements().add(lOrGate);
		lSequential.getUIElements().add(lNotGate);
		/* Allocate the two Sinking Inputs to the NorGate. */
		final IContact<IContact.Link> lA   = new IContact.Impl(0,                                  0, ResourceUtils.getResource(MenuContext.class.getClassLoader(), "res/icon/contact/unary.svg"), DataGlobal.TYPE_DHARMA, EDataDirection.SINK);
		final IContact<IContact.Link> lB   = new IContact.Impl(0, LexiconGlobal.CODE_DIM_HEIGHT_UNIT, ResourceUtils.getResource(MenuContext.class.getClassLoader(), "res/icon/contact/unary.svg"), DataGlobal.TYPE_DHARMA, EDataDirection.SINK);
		/* Add the inputs to the Coupling's SinkDecoupler. */
		lCoupling.getSinkDecoupler().getUIElements().add(lA);
		lCoupling.getSinkDecoupler().getUIElements().add(lB);
		/* Allocate the Sourcing Output of the OrGate. */
		final IContact<IContact.Link> lC   = new IContact.Impl(0,                                  0, ResourceUtils.getResource(MenuContext.class.getClassLoader(),  "res/icon/contact/nor.svg"),  DataGlobal.TYPE_DHARMA, EDataDirection.SOURCE);
		/* Add the Output to the AndGate's SourceDecoupler. */
		lCoupling.getSourceDecoupler().getUIElements().add(lC);
		/* Open it up. */
		lCoupling.onChangeState(pPunchModel, null, IEasingConfiguration.CONFIGURATION_NONE);
		/* Return the Coupling. */
		return lCoupling;
	}
	
	/* Codes an XOR Gate. */
	private static final Coupling<ILexical, ISequential<ILexical>> onEncodeXorGate(final PunchModel pPunchModel) { 
		/* Allocate a Sequential Coupling. */
		final Coupling<ILexical, ISequential<ILexical>> lCoupling   = new Coupling<ILexical, ISequential<ILexical>>(0, 0, new ISequential.Impl<ILexical>(0,0,0,0));
		/* Fetch the Sequence. */
		final ISequential<ILexical>                     lSequential = lCoupling.getInternals().get(0);
		/* Allocate a the NotGate and AndGate Source. */
		final Coupling<ILexical, ISequential<ILexical>> lNotGate = MenuContext.onEncodeNotGate(pPunchModel);
		final Coupling<ILexical, ISequential<ILexical>> lAndGate = MenuContext.onEncodeAndGate(pPunchModel);
		/* Allocate the NotGates. */
		final Invocation lNotGate0 = lNotGate.onCreateInvocation();
		final Invocation lNotGate1 = lNotGate.onCreateInvocation();
		final Invocation lNotGate2 = lNotGate.onCreateInvocation();
		final Invocation lNotGate3 = lNotGate.onCreateInvocation();
		final Invocation lNotGate4 = lNotGate.onCreateInvocation();
		/* Allocate the AndGates. */
		final Invocation lAndGate0 = lAndGate.onCreateInvocation();
		final Invocation lAndGate1 = lAndGate.onCreateInvocation();
		final Invocation lAndGate2 = lAndGate.onCreateInvocation();
		/* Distribute the NotGates. */
		DistributionGlobal.onDistributeHierarchy(pPunchModel, null, lNotGate0, IEasingConfiguration.CONFIGURATION_NONE);
		DistributionGlobal.onDistributeHierarchy(pPunchModel, null, lNotGate1, IEasingConfiguration.CONFIGURATION_NONE);
		DistributionGlobal.onDistributeHierarchy(pPunchModel, null, lNotGate2, IEasingConfiguration.CONFIGURATION_NONE);
		DistributionGlobal.onDistributeHierarchy(pPunchModel, null, lNotGate3, IEasingConfiguration.CONFIGURATION_NONE);
		DistributionGlobal.onDistributeHierarchy(pPunchModel, null, lNotGate4, IEasingConfiguration.CONFIGURATION_NONE);
		/* Distribute the AndGates. */
		DistributionGlobal.onDistributeHierarchy(pPunchModel, null, lAndGate0, IEasingConfiguration.CONFIGURATION_NONE);
		DistributionGlobal.onDistributeHierarchy(pPunchModel, null, lAndGate1, IEasingConfiguration.CONFIGURATION_NONE);
		DistributionGlobal.onDistributeHierarchy(pPunchModel, null, lAndGate2, IEasingConfiguration.CONFIGURATION_NONE);
		/* Align NotGate0. */
		lNotGate0.getUIElements().get(1).setY(LexiconGlobal.CODE_DIM_HEIGHT_UNIT * 2);
		lNotGate1.setX(lNotGate0.getX() + lNotGate0.getWidth());
		lNotGate1.setY(LexiconGlobal.CODE_DIM_HEIGHT_UNIT);
		lNotGate1.getUIElements().get(1).setY(LexiconGlobal.CODE_DIM_HEIGHT_UNIT * 3);
		/* Align NotGate1. */
		lAndGate0.setX(lNotGate1.getX() + lNotGate1.getWidth());
		lAndGate0.getUIElements().get(0).setY(0);
		lAndGate0.getUIElements().get(1).setY(LexiconGlobal.CODE_DIM_HEIGHT_UNIT * 4);
		lAndGate0.getUIElements().get(2).setY(0);
		/* Align AndGate0. */
		lAndGate1.setX(lAndGate0.getX() + lAndGate0.getWidth());
		lAndGate1.setY(LexiconGlobal.CODE_DIM_HEIGHT_UNIT);
		lAndGate1.getUIElements().get(2).setY(0);
		/* Align AndGate1. */
		lNotGate2.setX(lAndGate1.getX() + lAndGate1.getWidth());
		lNotGate2.getUIElements().get(1).setY(0);
		/* Align NotGate2. */
		lNotGate3.setX(lNotGate2.getX() + lNotGate2.getWidth());
		lNotGate3.setY(LexiconGlobal.CODE_DIM_HEIGHT_UNIT);
		lNotGate3.getUIElements().get(1).setY(0);
		/* Align NotGate3. */
		lAndGate2.setX(lNotGate3.getX() + lNotGate3.getWidth());
		lAndGate2.getUIElements().get(2).setY(0);
		/* Align AndGate2. */
		lNotGate4.setX(lAndGate2.getX() + lAndGate2.getWidth());
		lNotGate4.getUIElements().get(1).setY(0);
		/* Add the Gates to the Coupling's Internals. */
		lSequential.getUIElements().add(lNotGate0);
		lSequential.getUIElements().add(lNotGate1);
		lSequential.getUIElements().add(lAndGate0);
		lSequential.getUIElements().add(lAndGate1);
		lSequential.getUIElements().add(lNotGate2);
		lSequential.getUIElements().add(lNotGate3);
		lSequential.getUIElements().add(lAndGate2);
		lSequential.getUIElements().add(lNotGate4);
		/* Allocate the two Sinking Inputs to the XorGate. */
		final IContact<IContact.Link> lA   = new IContact.Impl(0,                                  0, ResourceUtils.getResource(MenuContext.class.getClassLoader(), "res/icon/contact/unary.svg"), DataGlobal.TYPE_DHARMA, EDataDirection.SINK);
		final IContact<IContact.Link> lB   = new IContact.Impl(0, LexiconGlobal.CODE_DIM_HEIGHT_UNIT, ResourceUtils.getResource(MenuContext.class.getClassLoader(), "res/icon/contact/unary.svg"), DataGlobal.TYPE_DHARMA, EDataDirection.SINK);
		/* Add the inputs to the Coupling's SinkDecoupler. */
		lCoupling.getSinkDecoupler().getUIElements().add(lA);
		lCoupling.getSinkDecoupler().getUIElements().add(lB);
		/* Allocate the Sourcing Output of the XorGate. */
		final IContact<IContact.Link> lC   = new IContact.Impl(0,                                  0, ResourceUtils.getResource(MenuContext.class.getClassLoader(),  "res/icon/contact/xor.svg"),   DataGlobal.TYPE_DHARMA, EDataDirection.SOURCE);
		/* Add the Output to the AndGate's SourceDecoupler. */
		lCoupling.getSourceDecoupler().getUIElements().add(lC);
		/* Open it up. */
		lCoupling.onChangeState(pPunchModel, null, IEasingConfiguration.CONFIGURATION_NONE);
		/* Return the Coupling. */
		return lCoupling;
	}
	
	/* Codes a Half Adder. */
	private static final Coupling<ILexical, ISequential<ILexical>> onEncodeHalfAdder(final PunchModel pPunchModel) { 
		/* Allocate a Sequential Coupling. */
		final Coupling<ILexical, ISequential<ILexical>> lCoupling   = new Coupling<ILexical, ISequential<ILexical>>(0, 0, new ISequential.Impl<ILexical>(0,0,0,0));
		/* Fetch the Sequence. */
		final ISequential<ILexical>                     lSequential = lCoupling.getInternals().get(0);
		/* Encode an XOR Gate and an AND gate. */
		final Coupling<ILexical, ISequential<ILexical>> lXorGate = MenuContext.onEncodeXorGate(pPunchModel);
		final Coupling<ILexical, ISequential<ILexical>> lAndGate = MenuContext.onEncodeAndGate(pPunchModel);
		/* Make some Invocations of the XOR and AndGate. */
		final Invocation lXorGate0 = lXorGate.onCreateInvocation();
		final Invocation lAndGate0 = lAndGate.onCreateInvocation();
		/* Distribute the Gates. */
		DistributionGlobal.onDistributeHierarchy(pPunchModel, null, lXorGate0, IEasingConfiguration.CONFIGURATION_NONE);
		DistributionGlobal.onDistributeHierarchy(pPunchModel, null, lAndGate0, IEasingConfiguration.CONFIGURATION_NONE);
		/* Add the XorGate. */
		lSequential.getUIElements().add(lXorGate0);
		/* Offset the AndGate. */
		lAndGate0.setX(lXorGate0.getX() + lXorGate.getWidth());
		lSequential.getUIElements().add(lAndGate0);
		/* Reposition the output of the AndGate. */
		lAndGate0.getUIElements().get(2).setY(0);
		
		/* Allocate the two Sinking Inputs to the XorGate. */
		final IContact<IContact.Link> lA     = new IContact.Impl(0,                                       0, ResourceUtils.getResource(MenuContext.class.getClassLoader(), "res/icon/contact/unary.svg"), DataGlobal.TYPE_DHARMA, EDataDirection.SINK);
		final IContact<IContact.Link> lB     = new IContact.Impl(0,      LexiconGlobal.CODE_DIM_HEIGHT_UNIT, ResourceUtils.getResource(MenuContext.class.getClassLoader(), "res/icon/contact/unary.svg"), DataGlobal.TYPE_DHARMA, EDataDirection.SINK);
		/* Add the inputs to the Coupling's SinkDecoupler. */
		lCoupling.getSinkDecoupler().getUIElements().add(lA);
		lCoupling.getSinkDecoupler().getUIElements().add(lB);
		/* Allocate the Sigma (Summation) and Carry Output. */
		final IContact<IContact.Link> lSigma = new IContact.Impl(0, LexiconGlobal.CODE_DIM_HEIGHT_UNIT << 1, ResourceUtils.getResource(MenuContext.class.getClassLoader(), "res/icon/contact/sigma.svg"), DataGlobal.TYPE_DHARMA, EDataDirection.SOURCE);
		final IContact<IContact.Link> lCarry = new IContact.Impl(0,                                       0, ResourceUtils.getResource(MenuContext.class.getClassLoader(), "res/icon/contact/carry.svg"), DataGlobal.TYPE_DHARMA, EDataDirection.SOURCE);
		/* Add the outputs to the Coupling's SourceDecoupler. */
		lCoupling.getSourceDecoupler().getUIElements().add(lCarry);
		lCoupling.getSourceDecoupler().getUIElements().add(lSigma);
		/* Open it up. */
		lCoupling.onChangeState(pPunchModel, null, IEasingConfiguration.CONFIGURATION_NONE);
		/* Return the Coupling. */
		return lCoupling;
	}
	
	/* MenuItem Definition. */
	private static abstract class MenuItem extends Field {

		/* Define default Serialization UID. */
		private static final long serialVersionUID = 1L;
		
		public MenuItem(final int pX, final int pY, final String pText, final float[] pColor, final PunchModel pPunchModel) {
			super(pX, pY, PunchModel.FILE_FONT_UI, pText, 8.0f, pColor, pPunchModel);
		}
		
		public abstract ILexical onCreateItem();
		
	};
	
	/* Member Variables. */
	private final IGroup<IUIElement> mItemList;
	private final SearchBox          mSearchBox;
	
	public MenuContext(final boolean pIsEnabled, final boolean pIsVisible, final PunchModel pPunchModel) {
		super(pIsEnabled, pIsVisible);
		/* Initialize Member Variables. */
		this.mItemList = new IGroup.Impl<IUIElement>(0, 0, MenuContext.DIM_WIDTH_MENU, MenuContext.DIM_WIDTH_MENU) {
			/* Define default Serialization UID. */
			private static final long serialVersionUID = 1L;
			/* Define the VectorPathGroups. */
			@Override public IVectorPathGroup[] getVectorPathGroups(final ArrayStore.Float pFloatStore, final VectorPathContext pVectorPathContext) {
				/* Allocate a simple shape for debugging. */
				return new IVectorPathGroup[]{ 
					/* Allocate a simple filled Rectangle. (Use IVectorPathGroup.Impl to force evaluation!) */
					new IVectorPathGroup.Impl(new VectorPath[]{ pVectorPathContext.onRectangle(pFloatStore, 0.0f, 0.0f, this.getWidth(), this.getHeight()).onCreatePath(pFloatStore) }, new IPathDefinition[]{ new IPathDefinition.Fill(new float[] { 0.0f, 0.0f, 0.0f, 0.4f }) })
				};
			}
			/* Define the Bounds. */
			@Override public final IBounds2.I getMargin() {
				return new IBounds2.I.Impl(10, 5, 10, 5);
			}
			/* Define the Padding. */
			@Override public int getPadding() { return 2; }
			/* Define the distribution. */
			@Override public final IDistribution onFetchDistribution(final List<IUIElement> pHierarchy, final UIUpdateDispatcher pUIUpdateDispatcher) {
				/* Fetch a copy of the ItemList. */
				final List<IUIElement> lMenuItems = new ArrayList<IUIElement>(this.getUIElements());
				/* Use a vertical LinearDistribution. */
				return new LinearDistribution<IUIElement>(lMenuItems) { @Override public final boolean isHorizontal() { return false; } };
			}
		};
		
		/* Define Menu Items. */
		this.getItemList().getUIElements().add(new MenuItem(0, 0,           "Sequence", DataGlobal.TYPE_VOID.getColor(), pPunchModel) { private static final long serialVersionUID = 1L; @Override public ILexical onCreateItem() { return new Coupling<ILexical, ISequential<ILexical>>(0,0, new ISequential.Impl<ILexical>(0,0,0,0)); } });
		this.getItemList().getUIElements().add(new MenuItem(0, 0,          "Selective", DataGlobal.TYPE_VOID.getColor(), pPunchModel) { private static final long serialVersionUID = 1L; @Override public ILexical onCreateItem() { return new Coupling<ILexical, ISequential<ILexical>>(0,0, new ISequential.Impl<ILexical>(0,0,0,0), new ISequential.Impl<ILexical>(0,0,0,0)); } });
		this.getItemList().getUIElements().add(new MenuItem(0, 0,             "Dharma", ColorGlobal.RGBA_DATA_BOOLEAN, pPunchModel) { private static final long serialVersionUID = 1L; @Override public ILexical onCreateItem() { return new Dharma(0, 0, false, pPunchModel);                                                                                             } });
		this.getItemList().getUIElements().add(new MenuItem(0, 0,             "Tunnel", DataGlobal.TYPE_VOID.getColor(), pPunchModel) { private static final long serialVersionUID = 1L; @Override public ILexical onCreateItem() { return new Tunnel(0,0);                                                                                            } });
		
		
		
		this.getItemList().getUIElements().add(new MenuItem(0, 0,            "Comment", ColorGlobal.RGBA_WHITE, pPunchModel) { private static final long serialVersionUID = 1L; @Override public ILexical onCreateItem() { return new Comment(0, 0, PunchModel.FILE_FONT_REGULAR, "Comment", 12.0f, ColorGlobal.RGBA_BLACK, pPunchModel);                                                                                            } });
		
		
		this.getItemList().getUIElements().add(new MenuItem(0, 0,              "Array", ColorGlobal.RGBA_BLACK, pPunchModel) { private static final long serialVersionUID = 1L; @Override public ILexical onCreateItem() { return new Array(0, 0);                                                                                            } });
//		this.getItemList().getUIElements().add(new MenuItem(0, 0,        "ArrayElement", ColorGlobal.RGBA_DATA_BOOLEAN, pPunchModel) { private static final long serialVersionUID = 1L; @Override public ILexical onCreateItem() { 
//			final ArrayElement lArrayToByte = new ArrayElement(0, 0);
//			DistributionGlobal.onDistributeHierarchy(pPunchModel, null, lArrayToByte, IEasingConfiguration.CONFIGURATION_NONE);
//			return lArrayToByte;
//		} });
		
//		this.getItemList().getUIElements().add(new MenuItem(0, 0,           "Selective", ColorGlobal.RGBA_DATA_BOOLEAN, pPunchModel) { private static final long serialVersionUID = 1L; @Override public ILexical onCreateItem() { 
//			final Selective lSelective = new Selective(0, 0);
//			DistributionGlobal.onDistributeHierarchy(pPunchModel, null, lSelective, IEasingConfiguration.CONFIGURATION_NONE);
//			return lSelective;
//		} });
		
		this.getItemList().getUIElements().add(new MenuItem(0, 0,           "Iteration", ColorGlobal.RGBA_BLACK, pPunchModel) { private static final long serialVersionUID = 1L; @Override public ILexical onCreateItem() { 
			return new Iteration(0, 0);
		} });
		
		this.getItemList().getUIElements().add(new MenuItem(0, 0,           "Feedback", ColorGlobal.RGBA_BLACK, pPunchModel) { private static final long serialVersionUID = 1L; @Override public ILexical onCreateItem() { 
			return new Feedback(0, 0);
		} });
		
		this.getItemList().getUIElements().add(new MenuItem(0, 0,           "Selector", ColorGlobal.RGBA_BLACK, pPunchModel) { private static final long serialVersionUID = 1L; @Override public ILexical onCreateItem() { 
			return new Selector(0, 0);
		} });
		
		this.getItemList().getUIElements().add(new MenuItem(0, 0,           "Print", ColorGlobal.RGBA_DATA_BOOLEAN, pPunchModel) { private static final long serialVersionUID = 1L; @Override public ILexical onCreateItem() { 
			final Print lPrintBoolean = new Print(0, 0);
			DistributionGlobal.onDistributeHierarchy(pPunchModel, null, lPrintBoolean, IEasingConfiguration.CONFIGURATION_NONE);
			return lPrintBoolean;
		} });
		
		this.getItemList().getUIElements().add(new MenuItem(0, 0,           "Elapsed", ColorGlobal.RGBA_DATA_BOOLEAN, pPunchModel) { private static final long serialVersionUID = 1L; @Override public ILexical onCreateItem() { 
			final Elapsed lPrintBoolean = new Elapsed(0, 0);
			DistributionGlobal.onDistributeHierarchy(pPunchModel, null, lPrintBoolean, IEasingConfiguration.CONFIGURATION_NONE);
			return lPrintBoolean;
		} });
		
		this.getItemList().getUIElements().add(new MenuItem(0, 0,           "NotGate", ColorGlobal.RGBA_DATA_BOOLEAN, pPunchModel) { private static final long serialVersionUID = 1L; @Override public ILexical onCreateItem() { 
			final Coupling<ILexical, ISequential<ILexical>> lCoupling =  MenuContext.onEncodeNotGate(pPunchModel);
			final Invocation lX = lCoupling.onCreateInvocation();
			DistributionGlobal.onDistributeHierarchy(pPunchModel, null, lX, IEasingConfiguration.CONFIGURATION_NONE);
			return lX;
		} });
		
		this.getItemList().getUIElements().add(new MenuItem(0, 0,           "AndGate", ColorGlobal.RGBA_DATA_BOOLEAN, pPunchModel) { private static final long serialVersionUID = 1L; @Override public ILexical onCreateItem() { 
			final Coupling<ILexical, ISequential<ILexical>> lCoupling =  MenuContext.onEncodeAndGate(pPunchModel);
			final Invocation lX = lCoupling.onCreateInvocation();
			DistributionGlobal.onDistributeHierarchy(pPunchModel, null, lX, IEasingConfiguration.CONFIGURATION_NONE);
			return lCoupling;
		} });

		this.getItemList().getUIElements().add(new MenuItem(0, 0,           "OrGate", ColorGlobal.RGBA_DATA_BOOLEAN, pPunchModel) { private static final long serialVersionUID = 1L; @Override public ILexical onCreateItem() { 
			final Coupling<ILexical, ISequential<ILexical>> lCoupling =  MenuContext.onEncodeOrGate(pPunchModel);
			final Invocation lX = lCoupling.onCreateInvocation();
			DistributionGlobal.onDistributeHierarchy(pPunchModel, null, lX, IEasingConfiguration.CONFIGURATION_NONE);
			return lX;
		} });
		
		this.getItemList().getUIElements().add(new MenuItem(0, 0,           "Nand", ColorGlobal.RGBA_DATA_BOOLEAN, pPunchModel) { private static final long serialVersionUID = 1L; @Override public ILexical onCreateItem() { 
			final Nand lNandGate = new Nand(0, 0);
			DistributionGlobal.onDistributeHierarchy(pPunchModel, null, lNandGate, IEasingConfiguration.CONFIGURATION_NONE);
			return lNandGate;
		} });

		this.getItemList().getUIElements().add(new MenuItem(0, 0,           "NorGate", ColorGlobal.RGBA_DATA_BOOLEAN, pPunchModel) { private static final long serialVersionUID = 1L; @Override public ILexical onCreateItem() { 
			final Coupling<ILexical, ISequential<ILexical>> lCoupling =  MenuContext.onEncodeNorGate(pPunchModel);
			final Invocation lX = lCoupling.onCreateInvocation();
			DistributionGlobal.onDistributeHierarchy(pPunchModel, null, lX, IEasingConfiguration.CONFIGURATION_NONE);
			return lX;
		} });

		this.getItemList().getUIElements().add(new MenuItem(0, 0,           "XorGate", ColorGlobal.RGBA_DATA_BOOLEAN, pPunchModel) { private static final long serialVersionUID = 1L; @Override public ILexical onCreateItem() { 
			final Coupling<ILexical, ISequential<ILexical>> lCoupling =  MenuContext.onEncodeXorGate(pPunchModel);
			final Invocation lX = lCoupling.onCreateInvocation();
			DistributionGlobal.onDistributeHierarchy(pPunchModel, null, lX, IEasingConfiguration.CONFIGURATION_NONE);
			return lX;
		} });
		


		this.getItemList().getUIElements().add(new MenuItem(0, 0,           "Half Adder", ColorGlobal.RGBA_DATA_BOOLEAN, pPunchModel) { private static final long serialVersionUID = 1L; @Override public ILexical onCreateItem() { 
			final Coupling<ILexical, ISequential<ILexical>> lCoupling =  MenuContext.onEncodeHalfAdder(pPunchModel);
			final Invocation lX = lCoupling.onCreateInvocation();
			DistributionGlobal.onDistributeHierarchy(pPunchModel, null, lX, IEasingConfiguration.CONFIGURATION_NONE);
			return lX;
		} });
		
//		this.getItemList().getUIElements().add(new MenuItem(0, 0,           "Full Adder", ColorGlobal.RGBA_DATA_BOOLEAN, pPunchModel) { private static final long serialVersionUID = 1L; @Override public ILexical onCreateItem() { 
//			final Coupling<?,?> lCoupling =  MenuContext.onEncodeFullAdder(pPunchModel);
//			final Axiom.Invocation lX = lCoupling.onCreateInvocation();
//			DistributionGlobal.onDistributeHierarchy(pPunchModel, null, lX, IEasingConfiguration.CONFIGURATION_NONE);
//			return lCoupling;
//		} });
		
//		this.getItemList().getUIElements().add(new MenuItem(0, 0,           "Operator", ColorGlobal.RGBA_DATA_BOOLEAN, pPunchModel) { private static final long serialVersionUID = 1L; @Override public ILexical onCreateItem() { 
//			return new Operator(0, 0);
//		} });
		
		/* Redistribute the ItemList. */
		DistributionGlobal.onDistributeHierarchy(pPunchModel, null, this.getItemList(), IEasingConfiguration.CONFIGURATION_NONE);
		/* Add the ItemList. */
		this.getUIElements().add(this.getItemList());
		/* Instantiate the SearchBox. */
		this.mSearchBox = new SearchBox(0, 0, 90, pPunchModel);
		/* Add the SearchBox. */
		this.getUIElements().add(this.getSearchBox());
	}

	@Override
	public final boolean onHandleEvent(final UIPointerEvent pUIPointerEvent, final List<IUIElement> pHierarchy, final PunchModel pPunchModel) {
		boolean b = false;
		/* Determine if the user is dragging. */
		if(pUIPointerEvent.getPointerAction().equals(EPointerAction.POINTER_DRAGGED)) {
			/* Fetch the RecentCollision. */
			final IUIElement lRecentCollision = RayCastManager.onFetchRecentCollision(pHierarchy);
			/* Determine if the user is dragging along a MenuItem. */
			if(lRecentCollision instanceof MenuItem) {
				b= true;
				/* Cast accordingly. */
				final MenuItem lMenuItem = (MenuItem)lRecentCollision;
				/* Queue the Lexical for dragging along the Context. */
				final ILexical lLexical  = lMenuItem.onCreateItem();
				/* Allocate a graphical implementation for the Lexical. */
				pPunchModel.getUIUpdateDispatcher().onRibbonEvent(new UIUpdateEvent(ResourceUtils.getSystemTimeSeconds(), EUICommand.CREATE, lLexical));
				/* Allocate a reference to the Diagram. */ /** TODO: How to enforce? **/
				final Diagram  lDiagram;
				/* Synchronize along the Contexts. */
				synchronized(pPunchModel.getContexts()) {
					/* Fetch the Diagram. */
					lDiagram = (Diagram)DataUtils.getFirstElementOf(pPunchModel.getContexts());
				}
				/* Synchronize along the Context. */
				synchronized(lDiagram) {
					/* Add the Lexical. */
					lDiagram.getUIElements().add(lLexical);
				}
				/* Allocate a simulated hierarchy. */
				final List<IUIElement> lHierarchy = new ArrayList<IUIElement>(2);
				/* Scale the UIPointerEvent to the Diagram. */
				UIPointerEvent.onTransformPointer(pUIPointerEvent, 0, 0, lDiagram.getScale());
				/* Update the Lexical's location. (Have it rest centrally.) */ /** TODO: This doesn't compensate for offset or scale. **/
				MathUtils.setPosition(lLexical, (pUIPointerEvent.getX()), (pUIPointerEvent.getY()));
				/* Calculate the difference between the position of the MenuContext and the Diagram. */
				final IVec2.I          lOffset    = new IVec2.I.Impl((lDiagram.getX() - this.getX()) + (Math.round((lLexical.getWidth() / 2))), (lDiagram.getY() - this.getY()) + (Math.round((lLexical.getHeight() / 2))));
				/* Withdraw the offset from the Lexical. */
				MathUtils.onWithdrawOffset(lLexical, lOffset);
				/* Add a reference to the Diagram. */
				lHierarchy.add(lDiagram);
				/* Add a reference to the Lexical. */
				lHierarchy.add(lLexical);
				/* Queue the UIElement for dragging along the Diagram, using the simulated Hierarchy. */
				DragGlobal.onEncapsulationDrag(lDiagram, lHierarchy, pPunchModel, pUIPointerEvent, lLexical, false);
			}
		}
		if(pUIPointerEvent.getPointerAction().equals(EPointerAction.POINTER_RELEASE) && pHierarchy.contains(this.getSearchBox())) {
			/* Do a funky expansion. */
			final UIEasingGroup   lUIEasingGroup   = new UIEasingGroup(pPunchModel.getUIUpdateDispatcher(), new IEasingConfiguration.Impl(EEasingAlgorithm.EXPONENTIAL_EASE_OUT, 0.6f), ResourceUtils.getSystemTimeSeconds());
			/* Allocate a UIElementPacket. */
			final UIElementPacket lUIElementPacket = new UIElementPacket(this.getSearchBox());
			/* Define the Expansion. */
			final int             lExpansion       = 100;
			/* Update the UIElementPacket. */
			lUIElementPacket.setX    (lUIElementPacket.getX()     - lExpansion);
			lUIElementPacket.setWidth(lUIElementPacket.getWidth() + lExpansion);
			/* Add the UIElementPacket to the UIEasingGroup. */
			lUIEasingGroup.getEasingPackets().add(lUIElementPacket);
			/* Remove the text. */
			this.getSearchBox().getField().setText("");
			/* Reset the ObjectTimeSeconds. */
			lUIEasingGroup.setObjectTimeSeconds(ResourceUtils.getSystemTimeSeconds());
			/* Export the UIEasingGroup. */
			pPunchModel.getUISecondsDispatcher().getEventFilters().add(lUIEasingGroup);
		}
		/* Only consume the event if it lies within the confines of the Context. */
		return b;
	}

	@Override public final void onScreenParametersChanged(final IScreenParameters pScreenParameters) {
		/* Implement standard procedures. */
		super.onScreenParametersChanged(pScreenParameters);
		/* Reposition the SearchBox. */
		this.getSearchBox().setX(pScreenParameters.getScreenWidth() - (this.getSearchBox().getWidth() + 10));
		this.getSearchBox().setY(10);
	}
	
	private final IGroup<IUIElement> getItemList() {
		return this.mItemList;
	}
	
	private final SearchBox getSearchBox() {
		return this.mSearchBox;
	}

}