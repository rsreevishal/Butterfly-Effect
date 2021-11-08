//$Id$
package butterflyeffect.ui;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class AddEffectDialog extends TitleAreaDialog{
	 	private Text txtFileName;
	    private Text txtLineNumber;
	    private Text txtDescription;

	    private String fileName;
	    private String description;
	    public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		private int lineNumber;

	    public AddEffectDialog(Shell parentShell) {
	        super(parentShell);
	    }

	    @Override
	    public void create() {
	        super.create();
	        setTitle("Add/Edit dialog box");
	        setMessage("Enter the details", IMessageProvider.INFORMATION);
	    }

	    @Override
	    protected Control createDialogArea(Composite parent) {
	        Composite area = (Composite) super.createDialogArea(parent);
	        Composite container = new Composite(area, SWT.NONE);
	        container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	        GridLayout layout = new GridLayout(2, false);
	        container.setLayout(layout);

	        createFileName(container);
	        createLineNumber(container);
	        createDescription(container);
	        return area;
	    }

	    private void createFileName(Composite container) {
	        Label lbtFileName = new Label(container, SWT.NONE);
	        lbtFileName.setText("File Name");

	        GridData dataFileName = new GridData();
	        dataFileName.grabExcessHorizontalSpace = true;
	        dataFileName.horizontalAlignment = GridData.FILL;
	        txtFileName = new Text(container, SWT.BORDER);
	        txtFileName.setText(fileName);
	        txtFileName.setEditable(false);
	        txtFileName.setLayoutData(dataFileName);
	    }

	    private void createLineNumber(Composite container) {
	        Label lbtLineNumber = new Label(container, SWT.NONE);
	        lbtLineNumber.setText("Line Number");

	        GridData dataLineNumber = new GridData();
	        dataLineNumber.grabExcessHorizontalSpace = true;
	        dataLineNumber.horizontalAlignment = GridData.FILL;
	        txtLineNumber = new Text(container, SWT.BORDER);
	        txtLineNumber.setText(lineNumber + "");
	        txtLineNumber.setLayoutData(dataLineNumber);
	    }

	    private void createDescription(Composite container) {
	    	Label lbtDescription = new Label(container, SWT.NONE);
	    	lbtDescription.setText("Description");
	    	GridData dataDescription = new GridData();
	    	dataDescription.grabExcessHorizontalSpace = true;
	    	dataDescription.horizontalAlignment = GridData.FILL;
	    	txtDescription = new Text(container, SWT.BORDER);
	    	txtDescription.setText(description != null ? description : "");
	    	txtDescription.setLayoutData(dataDescription);
	    }


	    @Override
	    protected boolean isResizable() {
	        return true;
	    }

	    private void saveInput() {
	        fileName = txtFileName.getText();
	        lineNumber = Integer.parseInt(txtLineNumber.getText());
	        description = txtDescription.getText();
	    }

	    @Override
	    protected void okPressed() {
	        saveInput();
	        super.okPressed();
	    }

	    public String getFileName() {
	        return fileName;
	    }

	    public int getLineNumber() {
	        return lineNumber;
	    }
	    
	    public void setFileName(String name) {
	    	fileName = name;
	    }
	    
	    public void setLineNumber(int line) {
	    	lineNumber = line;
	    }
}
