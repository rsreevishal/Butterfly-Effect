package butterflyeffect.ui;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import butterflyeffect.model.ButterflyEffect;

public class ThreeItemFilter extends ViewerFilter {
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		return parentElement instanceof ButterflyEffect && ((ButterflyEffect)parentElement).size() >= 3;
	}
}
