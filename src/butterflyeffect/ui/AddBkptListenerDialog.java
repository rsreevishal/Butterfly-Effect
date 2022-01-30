//$Id$
package butterflyeffect.ui;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class AddBkptListenerDialog extends TitleAreaDialog{
		private Text txtMessage, txtFileName;
		private String message;
		public static String filename;
		private TreeViewer treeViewer;
		public TreeViewer getTreeViewer() {
			return treeViewer;
		}

		public void setTreeViewer(TreeViewer treeViewer) {
			this.treeViewer = treeViewer;
		}

		public String getMessage() {
			return this.message;
		}
		
		public void setMessage(String message) {
			this.message = message;
		}
		
	    public AddBkptListenerDialog(Shell parentShell) {
	        super(parentShell);
	    }

	    @Override
	    public void create() {
	        super.create();
	        setTitle("Set message for the break point");
	        setMessage("Enter the message", IMessageProvider.INFORMATION);
	        if(filename != null) {
	        	txtFileName.setText(filename != null ? filename : "");
	        }
	    }

	    @Override
	    protected Control createDialogArea(Composite parent) {
	        Composite area = (Composite) super.createDialogArea(parent);
	        Composite container = new Composite(area, SWT.NONE);
	        container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	        GridLayout layout = new GridLayout(2, false);
	        container.setLayout(layout);
	        createFileName(container);
	        createMessage(container);
	        return area;
	    }

	    private void createMessage(Composite container) {
	    	Label lbtMessage = new Label(container, SWT.NONE);
	    	lbtMessage.setText("Message");
	    	GridData dataDescription = new GridData();
	    	dataDescription.grabExcessHorizontalSpace = true;
	    	dataDescription.horizontalAlignment = GridData.FILL;
	    	txtMessage = new Text(container, SWT.BORDER);
	    	txtMessage.setText(message != null ? message : "");
	    	txtMessage.setLayoutData(dataDescription);
	    }

	    private void createFileName(Composite container) {
	    	Label lbtFileName = new Label(container, SWT.NONE);
	    	lbtFileName.setText("Breakpoint file");
	    	GridData dataDescription = new GridData();
	    	dataDescription.grabExcessHorizontalSpace = true;
	    	dataDescription.horizontalAlignment = GridData.FILL;
	    	txtFileName = new Text(container, SWT.BORDER);
	    	txtFileName.setText(filename != null ? filename : "");
	    	txtFileName.setLayoutData(dataDescription);
	    	Button fileBtn = createButton(container, IDialogConstants.OPEN_ID, IDialogConstants.OPEN_LABEL, false);
	    	fileBtn.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetDefaultSelected(SelectionEvent arg0) {
					widgetSelected(arg0);
				}

				@Override
				public void widgetSelected(SelectionEvent arg0) {
					try {
						String[] FILTER_NAMES = { "Breakpoint files (*.bkpt)"};
						String[] FILTER_EXTS = { "*.bkpt"};
						FileDialog dlg = new FileDialog(treeViewer.getControl().getShell(), SWT.OPEN);
						dlg.setFilterNames(FILTER_NAMES);
						dlg.setFilterExtensions(FILTER_EXTS);
						filename = dlg.open();
						txtFileName.setText(filename != null ? filename : "");
				    } 
					catch (Exception e) {
				    	MessageDialog.openError(treeViewer.getControl().getShell(), "Error", "Unknown error occurred while saving the file.");
				    	e.printStackTrace();
				    }
				}
	    		
	    	});
	    }

	    @Override
	    protected boolean isResizable() {
	        return true;
	    }

	    private void saveInput() {
	        setMessage(txtMessage.getText());
	    }

	    @Override
	    protected void okPressed() {
	        saveInput();
	        super.okPressed();
	    }

		public String getFilename() {
			return filename;
		}

		public void setFilename(String filename) {
			this.filename = filename;
		}
}
