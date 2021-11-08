package butterflyeffect.ui;

import java.util.Iterator;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

import butterflyeffect.model.ButterflyEffect;
import butterflyeffect.model.DeltaEvent;
import butterflyeffect.model.IDeltaListener;
import butterflyeffect.model.Model;

public class ButterflyEffectContentProvider implements ITreeContentProvider, IDeltaListener {
	private static Object[] EMPTY_ARRAY = new Object[0];
	protected TreeViewer viewer;

	public void dispose() {}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = (TreeViewer)viewer;
		if(oldInput != null) {
			removeListenerFrom((ButterflyEffect)oldInput);
		}
		if(newInput != null) {
			addListenerTo((ButterflyEffect)newInput);
		}
	}
	
	protected void removeListenerFrom(ButterflyEffect bEffect) {
		bEffect.removeListener(this);
		for (Iterator<ButterflyEffect> iterator = bEffect.getEffects().iterator(); iterator.hasNext();) {
			ButterflyEffect effect = (ButterflyEffect) iterator.next();
			removeListenerFrom(effect);
		}
	}
	
	protected void addListenerTo(ButterflyEffect bEffect) {
		bEffect.addListener(this);
		for (Iterator<ButterflyEffect> iterator = bEffect.getEffects().iterator(); iterator.hasNext();) {
			ButterflyEffect effect = (ButterflyEffect) iterator.next();
			addListenerTo(effect);
		}
	}
	
	public Object[] getChildren(Object parentElement) {
		if(parentElement instanceof ButterflyEffect) {
			ButterflyEffect effect = (ButterflyEffect)parentElement;
			return effect.getEffects().toArray();
		}
		return EMPTY_ARRAY;
	}

	public Object getParent(Object element) {
		if(element instanceof Model) {
			return ((Model)element).getParent();
		}
		return null;
	}

	public boolean hasChildren(Object element) {
		return getChildren(element).length > 0;
	}

	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	public void add(DeltaEvent event) {
		Object effect = ((Model)event.receiver()).getParent();
		viewer.refresh(effect, false);
	}

	public void remove(DeltaEvent event) {
		add(event);
	}

}
