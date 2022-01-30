//$Id$
package butterflyeffect.listeners;

import org.eclipse.core.resources.IMarker;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.internal.core.BreakpointManager;
import org.eclipse.debug.internal.core.XMLMemento;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.importexport.breakpoints.IImportExportConstants;
import org.eclipse.debug.ui.actions.ImportBreakpointsOperation;
import org.eclipse.jdt.core.dom.Message;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaBreakpointListener;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaType;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

@SuppressWarnings("restriction")
public class CustomMessageBkptListener implements IJavaBreakpointListener {
	private String message;
	private String filename;
	private static CustomMessageBkptListener instance = null;
	private XMLMemento memento;
	private CustomMessageBkptListener() {

	}

	public static CustomMessageBkptListener getInstance() {
		if (instance == null) {
			instance = new CustomMessageBkptListener();
			return instance;
		}
		return instance;
	}

	public void addingBreakpoint(IJavaDebugTarget target, IJavaBreakpoint breakpoint) {
	}

	public void breakpointHasCompilationErrors(IJavaLineBreakpoint breakpoint, Message[] errors) {
	}

	public void breakpointHasRuntimeException(IJavaLineBreakpoint breakpoint, DebugException exception) {
	}

	public boolean checkWSExist(String wkSet, String value) {
		if(value.length() > 1) {
			String[] values = wkSet.split(IImportExportConstants.DELIMITER);
			for(String val: values) {
				if(val.trim().equals(value)) {
					return true;
				}
			}
		}
		return false;
	}
	public int breakpointHit(IJavaThread thread, IJavaBreakpoint breakpoint) {
		try {
			ImportBreakpointsOperation bkptOpr = new ImportBreakpointsOperation(filename, true, true);
			int debuggedLineNumber = thread.getTopStackFrame().getLineNumber();
			IMarker marker = breakpoint.getMarker();
			String debuggedClassPath = marker.getResource().getFullPath().toPortableString();
			XMLMemento[] nodes = memento.getChildren(IImportExportConstants.IE_NODE_BREAKPOINT);
			for (int i = 0; i < nodes.length; i++) {
				XMLMemento child = nodes[i].getChild(IImportExportConstants.IE_NODE_MARKER);
				XMLMemento resource = nodes[i].getChild(IImportExportConstants.IE_NODE_RESOURCE);
				String nodeType = child.getString(IImportExportConstants.IE_NODE_TYPE);
				int lineNumber = child.getInteger(IMarker.LINE_NUMBER);
				if(nodeType.equals(marker.getType()) && debuggedClassPath.equals(resource.getString("path")) && lineNumber == debuggedLineNumber) {
					nodes[i].putBoolean("tested", true);
					//String finalMessage = nodes[i].getString("cmessage") != null ? nodes[i].getString("cmessage") + IImportExportConstants.DELIMITER + message : IImportExportConstants.DELIMITER + message;
					XMLMemento[] grandChild = child.getChildren(IImportExportConstants.IE_NODE_ATTRIB);
					for(int j=0; j<grandChild.length; j++ ) {
						if(grandChild[j].getString(IImportExportConstants.IE_NODE_NAME).equals(IInternalDebugUIConstants.WORKING_SET_NAME)) {
							if(grandChild[j].getString("value") != null) {
								if(!checkWSExist(grandChild[j].getString("value"), message)) {
									String finalMessage = grandChild[j].getString("value") + IImportExportConstants.DELIMITER + message;
									grandChild[j].putString("value", finalMessage);
									break;
								}
							}
						}
					}
					//nodes[i].putString("cmessage", finalMessage);
					try (Writer outWriter = new OutputStreamWriter(new FileOutputStream(filename), StandardCharsets.UTF_8)) {
						memento.save(outWriter);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return IJavaBreakpointListener.DONT_SUSPEND;
	}

	public void breakpointInstalled(IJavaDebugTarget target, IJavaBreakpoint breakpoint) {
	}

	public void breakpointRemoved(IJavaDebugTarget target, IJavaBreakpoint breakpoint) {
	}

	public int installingBreakpoint(IJavaDebugTarget target, IJavaBreakpoint breakpoint, IJavaType type) {
		return 4;
	}

	public String getMessage() {
		return this.message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
		try (Reader reader = new InputStreamReader(new FileInputStream(filename), StandardCharsets.UTF_8)) {
			memento = XMLMemento.createReadRoot(reader);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
