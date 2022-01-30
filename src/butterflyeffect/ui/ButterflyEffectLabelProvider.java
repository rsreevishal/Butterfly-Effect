package butterflyeffect.ui;

import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import butterflyeffect.model.ButterflyEffect;

public class ButterflyEffectLabelProvider extends ColumnLabelProvider {	
	private Map imageCache = new HashMap(11);
	
	private ImageDescriptor createImageDescriptor(String image) {
        Bundle bundle = FrameworkUtil.getBundle(ButterflyEffectLabelProvider.class);
        URL url = FileLocator.find(bundle, new Path("icons/" + image), null);
        return ImageDescriptor.createFromURL(url);
    }
	
	public Image getImage(Object element) {
		ImageDescriptor descriptor = null;
		if (element instanceof ButterflyEffect) {
			descriptor = createImageDescriptor("butterfly.png");
		} else {
			throw unknownElement(element);
		}

		//obtain the cached image corresponding to the descriptor
		Image image = (Image)imageCache.get(descriptor);
		if (image == null) {
			image = descriptor.createImage();
			imageCache.put(descriptor, image);
		}
		return image;
	}

	public String getText(Object element) {
		if (element instanceof ButterflyEffect) {
			if(((ButterflyEffect)element).getName() == null) {
				return "Effect";
			} else {
				String name = ((ButterflyEffect)element).getName();
				return String.format("%s (%d) - %s", name.substring(name.lastIndexOf('/') + 1), ((ButterflyEffect)element).getLine(), ((ButterflyEffect)element).getDescription());
			}
		} else {
			throw unknownElement(element);
		}
	}

	public void dispose() {
		for (Iterator i = imageCache.values().iterator(); i.hasNext();) {
			((Image) i.next()).dispose();
		}
		imageCache.clear();
	}

	protected RuntimeException unknownElement(Object element) {
		return new RuntimeException("Unknown type of element in tree of type " + element.getClass().getName());
	}
	  
	@Override
	public String getToolTipText(Object element) {
	    return ((ButterflyEffect)element).getDescription();
	}

	@Override
	public Point getToolTipShift(Object object) {
	    return new Point(5, 5);
	}

	@Override
	public int getToolTipDisplayDelayTime(Object object) {
	    return 100; // msec
	}

	@Override
	public int getToolTipTimeDisplayed(Object object) {
	    return 10000; // msec
	}

}
