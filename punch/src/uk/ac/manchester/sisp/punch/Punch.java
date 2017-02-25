package uk.ac.manchester.sisp.punch;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.media.nativewindow.WindowClosingProtocol.WindowClosingMode;
import javax.media.opengl.GLAnimatorControl;
import javax.media.opengl.GLAnimatorControl.UncaughtExceptionHandler;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLProfile;
import javax.swing.JOptionPane;

import uk.ac.manchester.sisp.punch.global.PunchGlobal;
import uk.ac.manchester.sisp.punch.ui.IUIElement;
import uk.ac.manchester.sisp.punch.ui.core.context.IContext;
import uk.ac.manchester.sisp.punch.ui.core.context.constants.MenuContext;
import uk.ac.manchester.sisp.punch.ui.core.context.constants.UIContext;
import uk.ac.manchester.sisp.punch.ui.distribution.global.DistributionGlobal;
import uk.ac.manchester.sisp.punch.ui.key.UIKeyEvent;
import uk.ac.manchester.sisp.punch.ui.lexicon.compilation.Compilation;
import uk.ac.manchester.sisp.punch.ui.lexicon.coupling.Coupling;
import uk.ac.manchester.sisp.punch.ui.lexicon.diagram.Diagram;
import uk.ac.manchester.sisp.punch.ui.lexicon.simulation.Simulator;
import uk.ac.manchester.sisp.punch.ui.update.EUICommand;
import uk.ac.manchester.sisp.punch.ui.update.UIUpdateEvent;
import uk.ac.manchester.sisp.ribbon.ui.easing.IEasingConfiguration;
import uk.ac.manchester.sisp.ribbon.utils.DataUtils;
import uk.ac.manchester.sisp.ribbon.utils.ResourceUtils;

import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.util.FPSAnimator;

public final class Punch {
	
	public static final void main(final String[] pArgs) {
		/* Declare the Animator. */
		final FPSAnimator lAnimator   = new FPSAnimator(120);
		try {
			/* State that we wish to use OpenGLES 2.0 for software graphics. */
			final GLCapabilities lGLCapabilities = new GLCapabilities(GLProfile.getGL2ES2());
			/* Disable multisampling.*/
			lGLCapabilities.setSampleBuffers(true);
			/* Set the number of samples. */
			lGLCapabilities.setNumSamples(16);
			/* Prevent screen tearing. */
			lGLCapabilities.setDoubleBuffered(true);
//			/* Assert an invisible background.  */
//			lGLCapabilities.setBackgroundOpaque(false);
			/* Create a new window based on these capabilities. */
			final GLWindow lGLWindow = GLWindow.create(lGLCapabilities);
			/* Set the title of the Window. */
			lGLWindow.setTitle(PunchGlobal.APPLICATION_TITLE);
			/* Allow buffers to be automatically swapped. */
			lGLWindow.setAutoSwapBufferMode(true);
			/* Define a default size. */
			lGLWindow.setSize(700, 400);
//			/* Remove the title bar. */
//			lGLWindow.setUndecorated(true);
			/* Define close behaviour. */
			lGLWindow.setDefaultCloseOperation(WindowClosingMode.DISPOSE_ON_CLOSE);
			/* Allow the mouse pointer to be visible. */
			lGLWindow.setPointerVisible(true);
//			/* Set the application as fullscreen. */
//			lGLWindow.setFullscreen(true);
			/* Point the Animator towards the GLWindow. */
			lAnimator.add(lGLWindow);
			/* Graphical daemon thread control. */
			lGLWindow.setWindowDestroyNotifyAction(new Runnable() { @Override public void run() { lAnimator.stop(); } });
			/* Initialize the Punch Model Data. */
			final PunchModel      lPunchModel      = new PunchModel(96);
			/* Initialize the PunchView. */
			final PunchView       lPunchView       = new PunchView(lPunchModel);
			/* Initialize the PunchController. */
			final PunchController lPunchController = new PunchController(lPunchModel, lPunchView);
			/* Configure render event handling. */
			final DesktopGLES20 lDesktopGLES20 = new DesktopGLES20() {
				public final void display(final GLAutoDrawable pGLAutoDrawable) { super.display(pGLAutoDrawable); lPunchView.onDisplay(this); lPunchController.onDisplay(this);      }
				public final void dispose(final GLAutoDrawable pGLAutoDrawable) { super.dispose(pGLAutoDrawable); lPunchView.onDispose(this); lPunchController.onDispose(this);      }
				public final void init(final GLAutoDrawable pGLAutoDrawable)    { super.init(pGLAutoDrawable); lPunchView.onInitialize(this); lPunchController.onInitialize(this);   }
				public final void reshape(final GLAutoDrawable pGLAutoDrawable, final int pX, final int pY, final int pWidth, final int pHeight) { super.reshape(pGLAutoDrawable, pX, pY, pWidth, pHeight); lPunchView.onResized(this, pX, pY, pWidth, pHeight); lPunchController.onResized(this, pX, pY, pWidth, pHeight); }
			};
			/* Configure the Renderer. */
			lGLWindow.addGLEventListener(lDesktopGLES20);
			/* Configure mouse event routing. */
			lGLWindow.addMouseListener(lPunchController);
			
			/* Allocate a pointer to the file location. */
			final File lFilePath = new File("/home/alexander/diagram.p");
			
			lGLWindow.addKeyListener(new KeyListener(){
				@Override public final void keyPressed(final KeyEvent pKeyEvent)  {
					/* Copy and Paste? */
					if(pKeyEvent.getKeyCode() == 67 && pKeyEvent.isControlDown()) {
//						try {
//							/* Allocate the ObjectInputStream. */
//							final ObjectInputStream lObjectInputStream = new ObjectInputStream(new FileInputStream(lFilePath));
//							/* Write the Diagram. */
//							Diagram d = (Diagram)lObjectInputStream.readObject();
//							
//							lPunchModel.getUIUpdateDispatcher().onRibbonEvent(new UIUpdateEvent(0, EUICommand.CREATE, d.getUIElements().toArray(new IUIElement[d.getUIElements().size()])));
//							
//							DataUtils.getFirstElementOf(lPunchModel.getContexts()).getUIElements().addAll(d.getUIElements());
//							
//							/* Close the ObjectOutputStream. */
//							lObjectInputStream.close();
//						}
//						catch (final IOException | ClassNotFoundException pIOException) {
//							System.err.println("Created a new Diagram."+" ("+pIOException.getMessage()+")");
//						}
					}
					/* Determine if the user is pressing CTRL+S. (Simple save). */
					if(pKeyEvent.getKeyCode() == 83 && pKeyEvent.isControlDown()) {
						try {
							/* Synchronize along the Contexts. */
							synchronized(lPunchModel.getContexts()) {
								/* Make sure the diagram isn't deferring input. */
								if(lPunchModel.getUIDeferFilter().isElapsed(ResourceUtils.getSystemTimeSeconds())) {
									/* Fetch the Diagram. Simplistically, resides at index 0. */
									final IContext        lCodeContext        = DataUtils.getFirstElementOf(lPunchModel.getContexts());
									/* Allocate the ObjectOutputStream. */
									final ObjectOutputStream lObjectOutputStream = new ObjectOutputStream(new FileOutputStream(lFilePath));
									/* Write the Diagram. */
									lObjectOutputStream.writeObject(lCodeContext);
									/* Flush the Stream. */
									lObjectOutputStream.flush();
									/* Close the ObjectOutputStream. */
									lObjectOutputStream.close();
								}
							}
						}
						catch (final IOException pIOException) {
							pIOException.printStackTrace();
						}
					}
					/* If the user wishes to compile... */
					if(pKeyEvent.getKeyCode() == UIKeyEvent.KEY_CODE_SPACE && pKeyEvent.isControlDown() && lPunchModel.getUIDeferFilter().isElapsed(ResourceUtils.getSystemTimeSeconds())) {
						/* Synchronize along the Contexts. */
						synchronized(lPunchModel.getContexts()) {
							/* Fetch the Diagram. Simplistically, resides at index 0. */
							final IContext lCodeContext = DataUtils.getFirstElementOf(lPunchModel.getContexts());
							/* Synchronize along the CodeContext. */
							synchronized(lCodeContext) { 
								
								
//								final Cascader lCascader = new Cascader();
//								
//								/** TODO: Simulate! **/
								/* Fetch the sole Coupling. (Simplistically assumes a single top-level component.) */
								final Coupling<?, ?> lCoupling    = (Coupling<?, ?>)DataUtils.getFirstElementOf(lCodeContext.getUIElements());
								/* Instantiate the Compilation using a default hierarchy. */
								final Compilation    lCompilation = new Compilation();
//								
//								lCoupling.onCourierDispatch(lCompilation, lGParser);
								
								
								/* Allocate a simulation. */
								final Simulator      lSimulation  = new Simulator();
								/* Attempt a compilation. */
								try {
									/* Execute the Compiler. */
									lCoupling.onCourierDispatch(lCompilation, lSimulation);
								}
								catch(final Exception pException) {
									pException.printStackTrace();
//									/* Assert that the Simulation failed. */
//									System.err.println("Simulation Failed. ("+pException.getMessage()+")");
								};
							}
						}
					}
					/* If the escape key has been pressed... */
					if(pKeyEvent.getKeyCode() == KeyEvent.VK_ESCAPE) {
						/* Stop the animator. */
						lAnimator.stop();
						/* Kill the PunchView. */
						lPunchView.onDispose(lDesktopGLES20);
					}
					else {
						/* Deploy to the PunchController. */
						lPunchController.keyPressed(pKeyEvent);
					}
				}
				@Override public final void keyReleased(final KeyEvent pKeyEvent) { lPunchController.keyReleased(pKeyEvent); }
			});
			/* Register an UncaughtExceptionHandler to provide the user with debugging information. */
			lAnimator.setUncaughtExceptionHandler(new UncaughtExceptionHandler() { @Override public final void uncaughtException(final GLAnimatorControl pGLAnimatorControl, final GLAutoDrawable pGLAutoDrawable, final Throwable pThrowable) {
				/* Make sure the window is invisible. */
				lGLWindow.setVisible(false);
				/* Stop the animator! */
				lAnimator.stop();
				/* Display an apology. */
				Punch.onApologize(PunchGlobal.APPLICATION_TITLE, pThrowable);
			} });
			
			/* Allocate the Contexts. */
			Diagram lCodeContext = null;//  = new  Diagram(true, true, lPunchModel);
			final MenuContext lMenuContext  = new MenuContext(true, true, lPunchModel);
			final UIContext lUIContext  = new UIContext(true, true, lPunchModel);
			
			
//			System.out.println("ici");
//			final Array lArray = new Array(0, 0);
//			lArray.getInternals().getUIElements().add(new Dharma(0,0, true, lPunchModel));
//			lArray.getInternals().getUIElements().add(new Dharma(1,0, true, lPunchModel));
//			lArray.getInternals().getUIElements().add(new Dharma(2,0, false, lPunchModel));
//			lArray.getInternals().getUIElements().add(new Dharma(3,0, true, lPunchModel));
//			
//			for(final Object x : lArray.getValue()) {
//				System.out.println(x);
//			}
			
			/* Determine if there's a saved Diagram. */
			if(lFilePath.exists()) {
				try {
					/* Allocate the ObjectInputStream. */
					final ObjectInputStream lObjectInputStream = new ObjectInputStream(new FileInputStream(lFilePath));
					/* Write the Diagram. */
					lCodeContext = (Diagram)lObjectInputStream.readObject();
//					lCodeContext.setX(-300);
//					lCodeContext.setScale(2.0f);
					/* Close the ObjectOutputStream. */
					lObjectInputStream.close();
				}
				catch (final IOException pIOException) {
					System.err.println("Created a new Diagram."+" ("+pIOException.getMessage()+")");
					/* Allocate a new default Diagram. */
					lCodeContext = new  Diagram(true, true, lPunchModel);
				}
			}
			else {
				/* Allocate a new default Diagram. */
				lCodeContext = new  Diagram(true, true, lPunchModel);
			}
			
			/* Distribute the Contexts. */
			DistributionGlobal.onDistributeHierarchy(lPunchModel, null,    lUIContext, IEasingConfiguration.CONFIGURATION_NONE);
			DistributionGlobal.onDistributeHierarchy(lPunchModel, null,  lCodeContext, IEasingConfiguration.CONFIGURATION_NONE);
			DistributionGlobal.onDistributeHierarchy(lPunchModel, null,  lMenuContext, IEasingConfiguration.CONFIGURATION_NONE);
			
//			/* Allocate a Selective. */
//			final Selective lSelective = new Selective(0, 0);
//			
//			/* Allocate a couple of Booleans. */
//			final Dharma    lDharma0   = new Dharma(0, 0,  true, lPunchModel);
//			final Dharma    lDharma1   = new Dharma(0, 0, false, lPunchModel);
//			final Dharma    lDharma2   = new Dharma(0, 0, false, lPunchModel);
//			final Dharma    lDharma3   = new Dharma(0, 0, false, lPunchModel);
//			final Dharma    lDharma4   = new Dharma(0, 0, false, lPunchModel);
//			//final Array     lArray     = new Array(0, 0);
//			
//			/* Fetch the 'true' case. */
//			lSelective.getInternals().get(0).getUIElements().add(lDharma0);
//			lSelective.getInternals().get(1).getUIElements().add(lDharma1);
//			lSelective.getInternals().get(1).getUIElements().add(lDharma2);
//			lSelective.getInternals().get(1).getUIElements().add(lDharma3);
//			lSelective.getInternals().get(1).getUIElements().add(lDharma4);
//			//lSelective.getInternals().get(2).getUIElements().add(lArray);
//			/* Open the Selective. */
//			lSelective.onChangeState(lPunchModel, null, IEasingConfiguration.CONFIGURATION_NONE);
//			
//			/* Add the Selective to the CodeDiagram. */
//			lCodeContext.getUIElements().add(lSelective);

			/* Fabricate a Hierarchy. */
			final List<IUIElement> lHierarchy = new ArrayList<IUIElement>();
			/* Buffer the CodeContext onto the Hierarchy. */
			lHierarchy.add(lCodeContext);
			/* Synchronize along the Contexts. */
			synchronized(lPunchModel.getContexts()) {
				/* Supply the Contexts. */
				lPunchModel.getContexts().add(lCodeContext);
				lPunchModel.getContexts().add(lMenuContext);
				lPunchModel.getContexts().add(lUIContext);
				/* Iterate the Contexts. */
				for(final IContext lContext : lPunchModel.getContexts()) {
					/* Load the Context. */
					lPunchModel.getUIUpdateDispatcher().onRibbonEvent(new UIUpdateEvent(ResourceUtils.getSystemTimeSeconds(), EUICommand.CREATE, lContext));
				}
			}
			
			/* Begin rendering. */
			lAnimator.start();
			/* Make the GLWindow visible. */
			lGLWindow.setVisible(true);
			/* Wait on the Animator. */
			lAnimator.getThread().join();
			/* Destroy the Window. */
			lGLWindow.destroy();
		}
		catch(final Throwable pThrowable) {
			/* Print the fatal error. A user should never see this. */
			pThrowable.printStackTrace();
		}
	}
	
	private static final void onApologize(final String pTitle, final Throwable pThrowable) {
		/** TODO: Attempt to save a backup of the PunchModel. **/
		/* Declare the Apology. */
		final StringBuilder lApology = new StringBuilder("We're sorry you experienced this error. "+PunchGlobal.APPLICATION_TITLE+" is still in pre-alpha, so occasional technical glitches are inevitable!\nPlease tweet your feedback to the "+PunchGlobal.APPLICATION_TITLE+" Team at @Cawfree. We aim to fix these problems as soon as we can.\n");
		/* Append the name of the Exception. */
		lApology.append("\nException: " + pThrowable);
		/* Determine if there was a known cause. */
		if(DataUtils.isNotNull(pThrowable.getCause())) {
			/* Write the Java Exception. */
			lApology.append("\n"+"Caused by: "+pThrowable.getCause().toString());
			/* Write the start of the StackTrace. */
			for(int i = 0; i < pThrowable.getCause().getStackTrace().length; i++) {
				/* Fetch the Cause's StackTraceElement. */
				final StackTraceElement lStackTraceElement = pThrowable.getCause().getStackTrace()[i];
				/* Print the location of the error. */
				lApology.append("\n" + "\t "+ lStackTraceElement.toString());
			}
		}
		/* Apologies to the user that the program messed up and allow the program to terminate after the Daemon finishes. */
		JOptionPane.showMessageDialog(null, lApology.toString(), pTitle+" : Fatal Error", JOptionPane.ERROR_MESSAGE);
	}
	
}