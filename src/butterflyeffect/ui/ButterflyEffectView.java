package butterflyeffect.ui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Iterator;

import javax.annotation.PostConstruct;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.core.BinaryType;
import org.eclipse.jdt.internal.core.JarPackageFragmentRoot;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.JavaUIMessages;
import org.eclipse.jdt.internal.ui.dialogs.OpenTypeSelectionDialog;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.ITextEditor;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import butterflyeffect.listeners.CustomMessageBkptListener;
import butterflyeffect.model.ButterflyEffect;
import butterflyeffect.model.Model;

// to get active file in workbench
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.part.FileEditorInput;

public class ButterflyEffectView extends ViewPart {
	protected TreeViewer treeViewer;
	protected Text text;
	protected ButterflyEffectLabelProvider labelProvider;

	protected Action atLeatThreeItems;
	protected Action addAction, removeAction, editAction, saveAction,
			openAction, displayAction, traceBkptAction, closeTraceBkptAction,
			buildAction;
	protected ViewerFilter atLeastThreeFilter;
	protected ButterflyEffectContentProvider contentProvider;
	protected ButterflyEffect root;
	private boolean isExpanded;

	public ButterflyEffectView() {
	}

	protected IPath getActiveFile() throws NullPointerException {
		try {
			IWorkbench wb = PlatformUI.getWorkbench();
			IWorkbenchWindow window = wb.getActiveWorkbenchWindow();
			IWorkbenchPage page = window.getActivePage();
			IEditorPart editor = page.getActiveEditor();
			IEditorInput input = editor.getEditorInput();
			IPath path = ((FileEditorInput) input).getPath();
			if (path == null) {
				throw new NullPointerException();
			}
			return path.makeRelativeTo(ResourcesPlugin.getWorkspace().getRoot().getLocation());
		} catch (Exception e) {
			MessageDialog.openError(treeViewer.getControl().getShell(), "Error", "There is no active file opened in the editor.");
			throw new NullPointerException();
		}
	}

	@PostConstruct
	public void createPartControl(Composite parent) {
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.verticalSpacing = 2;
		layout.marginWidth = 0;
		layout.marginHeight = 2;
		parent.setLayout(layout);
		text = new Text(parent, SWT.READ_ONLY | SWT.SINGLE | SWT.BORDER);
		// layout the text field above the treeviewer
		GridData layoutData = new GridData();
		layoutData.grabExcessHorizontalSpace = true;
		layoutData.horizontalAlignment = GridData.FILL;
		text.setLayoutData(layoutData);

		// Create the tree viewer as a child of the composite parent
		treeViewer = new TreeViewer(parent);
		contentProvider = new ButterflyEffectContentProvider();
		treeViewer.setContentProvider(contentProvider);
		labelProvider = new ButterflyEffectLabelProvider();
		ColumnViewerToolTipSupport.enableFor(treeViewer, ToolTip.NO_RECREATE);
		treeViewer.setLabelProvider(labelProvider);
		treeViewer.setUseHashlookup(true);

		// layout the tree viewer below the text field
		layoutData = new GridData();
		layoutData.grabExcessHorizontalSpace = true;
		layoutData.grabExcessVerticalSpace = true;
		layoutData.horizontalAlignment = GridData.FILL;
		layoutData.verticalAlignment = GridData.FILL;
		treeViewer.getControl().setLayoutData(layoutData);

		// Create menu, toolbars, filters, sorters.
		createFiltersAndSorters();
		createActions();
		createMenus();
		createToolbar();
		hookListeners();

		treeViewer.setInput(getInitalInput());
		treeViewer.expandAll();
	}

	protected void createFiltersAndSorters() {
		atLeastThreeFilter = new ThreeItemFilter();
	}

	protected void gotoFile(String filePath, int Line) {
		IEditorPart openEditor;
		final IFile inputFile = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(Path.fromOSString(filePath));
		if (inputFile != null) {
			IWorkbenchPage page1 = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			try {
				openEditor = IDE.openEditor(page1, inputFile);
				if (openEditor instanceof ITextEditor) {
					ITextEditor textEditor = (ITextEditor) openEditor;
					IDocument document = textEditor.getDocumentProvider().getDocument(textEditor.getEditorInput());
					textEditor.selectAndReveal(document.getLineOffset(Line - 1), document.getLineLength(Line - 1));
				}
			} catch (PartInitException e1) {
				MessageDialog.openError(treeViewer.getControl().getShell(), "Error", "Can't able to locate the file.");
				e1.printStackTrace();
			} catch (BadLocationException e2) {
				MessageDialog.openError(treeViewer.getControl().getShell(), "Error", "Can't able to locate the file.");
				e2.printStackTrace();
			}
		}
	}

	protected void hookListeners() {
		treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				try {
					if (event.getSelection().isEmpty()) {
						text.setText("");
						return;
					}
					if (event.getSelection() instanceof IStructuredSelection) {
						IStructuredSelection selection = (IStructuredSelection) event.getSelection();
						StringBuffer toShow = new StringBuffer();
						for (Iterator<Model> iterator = selection.iterator(); iterator.hasNext();) {
							Object domain = (Model) iterator.next();
							String value = labelProvider.getText(domain);
							toShow.append(value);
							toShow.append(", ");
						}
						// remove the trailing comma space pair
						if (toShow.length() > 0) {
							toShow.setLength(toShow.length() - 2);
						}
						text.setText(toShow.toString());
					}
				} catch (Exception e) {
					MessageDialog.openError(treeViewer.getControl().getShell(), "Error", "Unknown error occurred while selecting the effect.");
					e.printStackTrace();
				}
			}
		});
		treeViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				try {
					if (event.getSelection().isEmpty()) {
						text.setText("");
						return;
					}
					if (event.getSelection() instanceof IStructuredSelection) {
						IStructuredSelection selection = (IStructuredSelection) event.getSelection();
						ButterflyEffect domain = (ButterflyEffect) selection.getFirstElement();
						String value = labelProvider.getText(domain);
						text.setText(value);
						gotoFile(Paths.get(ResourcesPlugin.getWorkspace().getRoot().getLocation().toString(), domain.getName()).toString(), domain.getLine());
					}
				} catch (Exception e) {
					MessageDialog.openError(treeViewer.getControl().getShell(), "Error", "Unknown error occurred while opening the effect.");
					e.printStackTrace();
				}
			}
		});
	}

	protected void createActions() {
		try {
			atLeatThreeItems = new Action("Effects With At Least Three Causes") {
				public void run() {
					updateFilter(atLeatThreeItems);
				}
			};
			atLeatThreeItems.setChecked(false);

			addAction = new Action("Add") {
				public void run() {
					addEffect();
				}
			};
			addAction.setToolTipText("Add effect");
			addAction.setImageDescriptor(createImageDescriptor("add.png"));

			removeAction = new Action("Delete") {
				public void run() {
					removeSelected();
				}
			};
			removeAction.setToolTipText("Delete effect");
			removeAction.setImageDescriptor(createImageDescriptor("delete.png"));

			editAction = new Action("Edit") {
				public void run() {
					editSelected();
				}
			};
			editAction.setToolTipText("Edit effect");
			editAction.setImageDescriptor(createImageDescriptor("edit.png"));

			saveAction = new Action("Save") {
				public void run() {
					saveEffects();
				}
			};
			saveAction.setToolTipText("Save effects");
			saveAction.setImageDescriptor(createImageDescriptor("save.png"));

			openAction = new Action("Open") {
				public void run() {
					openEffects();
				}
			};
			openAction.setToolTipText("Open effects");
			openAction.setImageDescriptor(createImageDescriptor("open.png"));

			displayAction = new Action("Expand/Collapse") {
				public void run() {
					if (isExpanded) {
						treeViewer.collapseAll();
						isExpanded = false;
					} else {
						treeViewer.expandAll();
						isExpanded = true;
					}
				}
			};
			displayAction.setToolTipText("Expand/Collapse effects");
			displayAction.setImageDescriptor(createImageDescriptor("arrow_inout.png"));

			traceBkptAction = new Action("Trace Bkpt") {
				public void run() {
					AddBkptListenerDialog dialog = new AddBkptListenerDialog(treeViewer.getControl().getShell());
					dialog.setTreeViewer(treeViewer);
					dialog.create();
					if (dialog.open() == Window.OK) {
						CustomMessageBkptListener instance = CustomMessageBkptListener.getInstance();
						instance.setMessage(dialog.getMessage());
						instance.setFilename(dialog.getFilename());
						JDIDebugModel.removeJavaBreakpointListener(instance);
						JDIDebugModel.addJavaBreakpointListener(instance);
					}
				}
			};
			traceBkptAction.setToolTipText("Trace the break points with custom message");
			traceBkptAction.setImageDescriptor(createImageDescriptor("eye.png"));

			closeTraceBkptAction = new Action("Stop Trace Bkpt") {
				public void run() {
					JDIDebugModel.removeJavaBreakpointListener(CustomMessageBkptListener.getInstance());
					MessageDialog.openInformation(treeViewer.getControl().getShell(), "Success", "Tracing stopped!");
				}
			};
			closeTraceBkptAction.setToolTipText("Stop tracing break points");
			closeTraceBkptAction.setImageDescriptor(createImageDescriptor("no_eye.png"));

			buildAction = new Action("Update jar File") {
				public void run() {
					SelectionDialog dialog = new OpenTypeSelectionDialog(JavaPlugin.getActiveWorkbenchShell(), true, PlatformUI.getWorkbench().getProgressService(), null, IJavaSearchConstants.TYPE);
					dialog.setTitle(JavaUIMessages.OpenTypeAction_dialogTitle);
					dialog.setMessage(JavaUIMessages.OpenTypeAction_dialogMessage);
					int result = dialog.open();
					if (result != 0)
						return;
					Object[] types = dialog.getResult();
					if (types == null || types.length == 0)
						return;
					if (types.length == 1) {
						IJavaElement element = (IJavaElement) types[0];
						if (element instanceof BinaryType) {
							try {
								String fileName = ((BinaryType) element).getClassFile().getElementName();
								String jarFile = element.getPath().toOSString();
								String packageName = ((BinaryType) element).getPackageFragment().getElementName();
								ElementTreeSelectionDialog dialogProject = new ElementTreeSelectionDialog(JavaPlugin.getActiveWorkbenchShell(), new WorkbenchLabelProvider(), new WorkbenchContentProvider());
								dialogProject.setInput(ResourcesPlugin.getWorkspace().getRoot());
								dialogProject.setAllowMultiple(false);
								if (dialogProject.open() == Window.OK) {
									Runtime rt = Runtime.getRuntime();
									IResource resource = (IResource) dialogProject.getFirstResult();
									String projectPath = resource.getLocation().toOSString();
									packageName = packageName.replace(".", "/");
									String completePath = String.format("%s/%s", packageName, fileName);
									String cmd = String.format("jar -uvf %s %s", jarFile, completePath);
									Process pr = rt.exec(cmd, new String[] {}, new File(String.format("%s/bin/", projectPath)));
									BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
					                String output= "",line=null;
					                while((line=input.readLine()) != null) {
					                    output += line + "\n";
					                }
									int retVal = pr.waitFor();
									if(retVal != 0) {
										MessageDialog.openError(JavaPlugin.getActiveWorkbenchShell(), "Error", "Couldn't able to update the JAR");
									} else {
										MessageDialog.openInformation(JavaPlugin.getActiveWorkbenchShell(), "Success", output);
									}
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						} else {
							MessageDialog.openError(JavaPlugin.getActiveWorkbenchShell(), "Error", "Open file with JAR path.");
						}
						return;
					}
				}
			};
			buildAction.setToolTipText("Update jar file in the build path");
			buildAction.setImageDescriptor(createImageDescriptor("wrench.png"));
		} catch (Exception e) {
			MessageDialog.openError(treeViewer.getControl().getShell(), "Error", "Unknown error occurred while creating the actions.");
			e.printStackTrace();
		}
	}

	private ImageDescriptor createImageDescriptor(String image) {
		Bundle bundle = FrameworkUtil.getBundle(ButterflyEffectView.class);
		URL url = FileLocator.find(bundle, new Path("icons/" + image), null);
		return ImageDescriptor.createFromURL(url);
	}

	protected void addEffect() {
		try {
			ButterflyEffect receivingEffect;
			if (treeViewer.getSelection().isEmpty()) {
				receivingEffect = root;
			} else {
				IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
				Model selectedDomainObject = (Model) selection.getFirstElement();
				if (!(selectedDomainObject instanceof ButterflyEffect)) {
					receivingEffect = selectedDomainObject.getParent();
				} else {
					receivingEffect = (ButterflyEffect) selectedDomainObject;
				}
			}
			AddEffectDialog dialog = new AddEffectDialog(treeViewer.getControl().getShell());
			String activeFile = getActiveFile().toString();
			dialog.setFileName(activeFile != null ? activeFile : "Nil");
			dialog.create();
			if (dialog.open() == Window.OK) {
				ButterflyEffect container = new ButterflyEffect(activeFile);
				container.setLine(dialog.getLineNumber());
				container.setDescription(dialog.getDescription());
				container.addListener(contentProvider);
				receivingEffect.add(container);
			}
		} catch (IllegalArgumentException e) {
			MessageDialog.openError(treeViewer.getControl().getShell(), "Error", "Make sure to select the effect.");
		} catch (NullPointerException e) {
			MessageDialog.openError(treeViewer.getControl().getShell(), "Error", "Make sure to open a file to add the effect.");
		} catch (Exception e) {
			MessageDialog.openError(treeViewer.getControl().getShell(), "Error", "Make sure to give valid input.");
			e.printStackTrace();
		}
	}

	protected void editSelected() {
		try {
			ButterflyEffect receivingEffect;
			if (treeViewer.getSelection().isEmpty()) {
				receivingEffect = root;
			} else {
				IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
				Model selectedDomainObject = (Model) selection.getFirstElement();
				if (!(selectedDomainObject instanceof ButterflyEffect)) {
					receivingEffect = selectedDomainObject.getParent();
				} else {
					receivingEffect = (ButterflyEffect) selectedDomainObject;
				}
			}
			AddEffectDialog dialog = new AddEffectDialog(treeViewer.getControl().getShell());
			dialog.setFileName(receivingEffect.getName());
			dialog.setLineNumber(receivingEffect.getLine());
			dialog.setDescription(receivingEffect.getDescription());
			dialog.create();
			if (dialog.open() == Window.OK) {
				receivingEffect.setName(dialog.getFileName());
				receivingEffect.setLine(dialog.getLineNumber());
				receivingEffect.setDescription(dialog.getDescription());
			}
		} catch (IllegalArgumentException e) {
			MessageDialog.openError(treeViewer.getControl().getShell(), "Error", "Make sure to select the effect.");
		} catch (NullPointerException e) {
			MessageDialog.openError(treeViewer.getControl().getShell(), "Error", "Make sure to open a file to edit the effect.");
		} catch (Exception e) {
			MessageDialog.openError(treeViewer.getControl().getShell(), "Error", "Make sure to give valid input.");
			e.printStackTrace();
		}
	}

	protected void saveEffects() {
		try {
			JSONObject json = ButterflyEffect.toJSON(root);
			String[] FILTER_NAMES = { "JSON Files (*.json)" };
			String[] FILTER_EXTS = { "*.json" };
			FileDialog dlg = new FileDialog(treeViewer.getControl().getShell(), SWT.SAVE);
			dlg.setFilterNames(FILTER_NAMES);
			dlg.setFilterExtensions(FILTER_EXTS);
			String fn = dlg.open();
			FileWriter myWriter = new FileWriter(fn);
			myWriter.write(json.toJSONString());
			myWriter.close();
		} catch (IOException e) {
			MessageDialog.openError(treeViewer.getControl().getShell(), "Error", "An error occurred while saving the file.");
			e.printStackTrace();
		} catch (Exception e) {
			MessageDialog.openError(treeViewer.getControl().getShell(), "Error", "Unknown error occurred while saving the file.");
			e.printStackTrace();
		}
	}

	protected void openEffects() {
		try {
			String[] FILTER_NAMES = { "JSON Files (*.json)" };
			String[] FILTER_EXTS = { "*.json" };
			FileDialog dlg = new FileDialog(treeViewer.getControl().getShell(), SWT.OPEN);
			dlg.setFilterNames(FILTER_NAMES);
			dlg.setFilterExtensions(FILTER_EXTS);
			String fn = dlg.open();
			FileReader fr = new FileReader(fn);
			BufferedReader br = new BufferedReader(fr);
			String jsonString = "", line = "";
			while ((line = br.readLine()) != null) {
				jsonString += line;
			}
			JSONObject json = (JSONObject) new JSONParser().parse(jsonString);
			root = ButterflyEffect.fromJSON(json);
			treeViewer.setInput(root);
			br.close();
			fr.close();
		} catch (IOException e) {
			MessageDialog.openError(treeViewer.getControl().getShell(), "Error", "An error occurred while reading the file. Make sure th file exists");
			e.printStackTrace();
		} catch (ParseException e) {
			MessageDialog.openError(treeViewer.getControl().getShell(), "Error", "An error occurred while reading the json. Check your JSON file.");
			e.printStackTrace();
		} catch (Exception e) {
			MessageDialog.openError(treeViewer.getControl().getShell(), "Error", "Unknown error occurred while saving the file.");
			e.printStackTrace();
		}
	}

	protected void removeSelected() {
		try {
			if (treeViewer.getSelection().isEmpty()) {
				return;
			}
			IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
			treeViewer.getTree().setRedraw(false);
			for (Iterator<Model> iterator = selection.iterator(); iterator.hasNext();) {
				Model model = (Model) iterator.next();
				model.removeListener(contentProvider);
				ButterflyEffect parent = model.getParent();
				parent.remove(model);
			}
			treeViewer.getTree().setRedraw(true);
		} catch (IllegalArgumentException e) {
			MessageDialog.openError(treeViewer.getControl().getShell(), "Error", "Make sure to select the effect.");
		} catch (Exception e) {
			MessageDialog.openError(treeViewer.getControl().getShell(), "Error", "Unknown error occurred while removing the effect.");
			e.printStackTrace();
		}
	}

	protected void createMenus() {
		try {
			IMenuManager rootMenuManager = getViewSite().getActionBars().getMenuManager();
			rootMenuManager.setRemoveAllWhenShown(true);
			rootMenuManager.addMenuListener(new IMenuListener() {
				public void menuAboutToShow(IMenuManager mgr) {
					fillMenu(mgr);
				}
			});
			fillMenu(rootMenuManager);
		} catch (Exception e) {
			MessageDialog.openError(treeViewer.getControl().getShell(), "Error", "Unknown error occurred while creating the menu.");
		}
	}

	protected void fillMenu(IMenuManager rootMenuManager) {
		try {
			IMenuManager filterSubmenu = new MenuManager("Filters");
			rootMenuManager.add(filterSubmenu);
			filterSubmenu.add(atLeatThreeItems);
		} catch (Exception e) {
			MessageDialog.openError(treeViewer.getControl().getShell(), "Error", "Unknown error occurred while filling the menu.");
			e.printStackTrace();
		}
	}

	protected void updateFilter(Action action) {
		try {
			if (action == atLeatThreeItems) {
				if (action.isChecked()) {
					treeViewer.addFilter(atLeastThreeFilter);
				} else {
					treeViewer.removeFilter(atLeastThreeFilter);
				}
			}
		} catch (Exception e) {
			MessageDialog.openError(treeViewer.getControl().getShell(), "Error", "Unknown error occurred while updating the filter.");
			e.printStackTrace();
		}
	}

	protected void createToolbar() {
		try {
			IToolBarManager toolbarManager = getViewSite().getActionBars().getToolBarManager();
			toolbarManager.add(addAction);
			toolbarManager.add(editAction);
			toolbarManager.add(removeAction);
			toolbarManager.add(saveAction);
			toolbarManager.add(openAction);
			toolbarManager.add(displayAction);
			toolbarManager.add(traceBkptAction);
			toolbarManager.add(closeTraceBkptAction);
			toolbarManager.add(buildAction);
		} catch (Exception e) {
			MessageDialog.openError(treeViewer.getControl().getShell(), "Error", "Unknown error occurred while creating the toolbar.");
			e.printStackTrace();
		}
	}

	public ButterflyEffect getInitalInput() {
		root = new ButterflyEffect();
		return root;
	}

	@Focus
	public void setFocus() {
		treeViewer.getControl().setFocus();
	}
}
