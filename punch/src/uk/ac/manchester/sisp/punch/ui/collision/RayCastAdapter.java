package uk.ac.manchester.sisp.punch.ui.collision;

import java.util.ArrayList;
import java.util.List;

import uk.ac.manchester.sisp.punch.ui.IUIElement;
import uk.ac.manchester.sisp.punch.ui.core.Field;
import uk.ac.manchester.sisp.punch.ui.core.IGroup;
import uk.ac.manchester.sisp.punch.ui.core.IIcon;
import uk.ac.manchester.sisp.punch.ui.core.ISVGIcon;
import uk.ac.manchester.sisp.punch.ui.core.context.IContext;
import uk.ac.manchester.sisp.punch.ui.courier.IUICourier;
import uk.ac.manchester.sisp.ribbon.common.IVec2;
import uk.ac.manchester.sisp.ribbon.utils.DataUtils;

public class RayCastAdapter extends RayCastManager<IUIElement> implements IUICourier<IVec2.I> { 
	
	public static final <T extends IVec2.I> IVec2.I.W onFetchAbsoluteCoordinates(final IVec2.I.W pResultsBuffer, final List<T> pHierarchy, final T pT) {
		/* Allocate a reference to hold each nested UIElement. */
		T lT = pT;
		/* Initialize the ResultsBuffer. */
		pResultsBuffer.setX(lT.getX());
		pResultsBuffer.setY(lT.getY());
		/* Iterate through the rest of the hierarchy and re-assign the UIElement. */
		while(DataUtils.isNotNull(lT = RayCastAdapter.onFetchParent(pHierarchy, lT))) {
			/* Offset the ResultsBuffer. */
			pResultsBuffer.setX(pResultsBuffer.getX() + lT.getX());
			pResultsBuffer.setY(pResultsBuffer.getY() + lT.getY());
		}
		/* Return the co-ordinates. */
		return pResultsBuffer;
	}
	
	/* Default constructor. */
	public RayCastAdapter() {
		/* Allocate an internal List. */
		super(new ArrayList<IUIElement>()); 
	}
	
	public final List<IUIElement> onTransmitRay(final IVec2.I pVec2, final IUIElement pEntryPoint) {
		/* Clear the results from the previous collision. */
		this.getCollisionResults().clear();
		/* Update the RayHierarchyTransform for this new List. */
		this.onRayHierarchyTransform(pVec2);
		/* Attempt to buffer the collisions of the UIPointerEvent from the EntryPoint. */
		pEntryPoint.onCourierDispatch(this, pVec2);
		/* Return the CollisionResults. */
		return this.getCollisionResults();
	}

	@Override public final void onCourierTransit(final IIcon pIcon, final IVec2.I pCourierPackage) {
		/* Determine intersection with the IIcon. */
		if(this.isIntersectingBounds(pIcon)) {
			/* Buffer the collision. */
			this.getCollisionResults().add(pIcon);
		}
	}

	@Override public void onCourierTransit(final ISVGIcon pSVGIcon, final IVec2.I pCourierPackage) {
		/* Determine intersection with the ISVGIcon. */
		if(this.isIntersectingBounds(pSVGIcon)) {
			/* Buffer the collision. */
			this.getCollisionResults().add(pSVGIcon);
		}
	}

	@Override public final void onCourierTransit(final Field pField, final IVec2.I pCourierPackage) {
		/* Determine intersection with the Field. */
		if(this.isIntersectingBounds(pField)) {
			/* Buffer the collision. */
			this.getCollisionResults().add(pField);
		}
	}
	
	@Override public final <T extends IUIElement>  void onCourierTransit(final IGroup<T> pGroup, final IVec2.I pCourierPackage) {
		/* Determine intersection with the IGroup. */
		if(this.isIntersectingBounds(pGroup) || (pGroup instanceof IContext)) { /** TODO: Smarter! Surely IScissorable works here? **/
			/* Buffer the collision. */
			this.getCollisionResults().add(pGroup);
			/* Update the ray hierarchy. */
			this.onRayHierarchyTransform(pCourierPackage);
			/* Attain synchronized access to the IGroup. */
			synchronized(pGroup) {
				/* Fetch a safe copy of the IGroup's UIElements. */
				final List<T> lUIElements = pGroup.getUIElements();
				/* Iterate across the IGroup's UIElements whilst further collisions have not been found. Allow non-vessels to terminate upon nested collisions. */
				for(int i = lUIElements.size() -1; i >= 0 && (this.isRecentCollision(pGroup)); i--) {
					/* Determine if there are further intersections with the current UIElement. */
					lUIElements.get(i).onCourierDispatch(this, pCourierPackage);
				}
			}
		}
	}

	@Override public final void onCourierTransit(final ISVGIcon.IFlat pFlatIcon, final IVec2.I pCourierPackage) {
		/* Handle as a generic ISVGIcon. */
		this.onCourierTransit((ISVGIcon)pFlatIcon, pCourierPackage);
	}
	
}