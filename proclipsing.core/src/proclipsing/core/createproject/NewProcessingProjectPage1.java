package proclipsing.core.createproject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import proclipsing.os.OSHelperManager;

public class NewProcessingProjectPage1 extends WizardPage {

	public static String PAGE_NAME                 = "New Processing Project";
	public static String PAGE_TITLE                = "Processing";
	public static String PROJECT_NAME_LABEL        = "Project Name";
	public static String PROCESSING_PATH_LABEL     = "Processing Path";
	public static String DIR_SEARCH_BUTTON_LABEL   = "Browse...";
	public static String IMPORT_LIBRARIES_LABEL    = "Select Libraries to Import";
	public static int    PROJECT_NAME_MAXSIZE      = 150;
	public static int    PATH_TEXT_WIDTH_HINT      = 350;
	
	private Text project_name_text;
	private Text processing_path_text;
	private CheckboxTableViewer libraries_viewer;
	private Button appButton;
	
	protected NewProcessingProjectPage1() {
	    super(PAGE_NAME, PAGE_TITLE, null);
	}

	public void createControl(Composite parent) { 
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		 
		Label label = new Label(composite, SWT.NONE);
		label.setText(PROJECT_NAME_LABEL);
		label.setLayoutData(new GridData());
		 
		project_name_text = new Text(composite, SWT.NONE | SWT.BORDER);
		project_name_text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		project_name_text.addModifyListener(new ModifyListener() {
		    public void modifyText(ModifyEvent e) {
		        // calling this forces isPageComplete() to get called
		        setPageComplete(true);
            }
		});
		
		drawProcessingFinder(composite);
		drawAppOption(composite);
        drawLibrarySelector(composite);
		setControl(composite);
	}
	
	private void drawProcessingFinder(Composite parent) {
        
        Label processingPathLabel = new Label(parent, SWT.NONE);
        processingPathLabel.setText(PROCESSING_PATH_LABEL);
        
        final Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        composite.setLayout(layout);
        
        processing_path_text = new Text(composite, SWT.NONE | SWT.BORDER );
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.widthHint = PATH_TEXT_WIDTH_HINT;
        processing_path_text.setLayoutData(gd);
        processing_path_text.setText(getProjectConfiguration().getProcessingPath());
        processing_path_text.addModifyListener(new ModifyListener() {
		    public void modifyText(ModifyEvent e) {
		        // get & show the discovered libraries based on what's been typed in
		        File processingPath = new File(processing_path_text.getText());
		        showDiscoveredLibraries(processingPath);
		        setSelectedLibraries();         
		        
		        //  calling this forces isPageComplete() to get called
		        setPageComplete(true);
            }
		});
        Button button = new Button(composite, SWT.PUSH);
        button.setText(DIR_SEARCH_BUTTON_LABEL);
        button.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
            	FileDialog dialog = 
                    new FileDialog(composite.getShell());
                processing_path_text.setText(dialog.open());
            }
        });
    }

    public void drawAppOption(Composite parent){
	    Composite appOption = new Composite(parent, SWT.NONE);
	    RowLayout rowLayout = new RowLayout(SWT.VERTICAL);
	    appOption.setLayout(rowLayout);
	    appOption.setLayoutData(
	            new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
	    
		Button appletButton = new Button(appOption, SWT.RADIO);
		appButton = new Button(appOption, SWT.RADIO);

		appButton.setText("Application");
		appletButton.setText("Applet");
		
		appButton.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent arg0) {}

			public void widgetSelected(SelectionEvent arg0) {
	            setPageComplete(true);
			}			
		});
		
		appletButton.setSelection(true);
	}
	
	public void drawLibrarySelector(Composite parent) {
        // group surrounds the box w/ a thin line
		Group projectsGroup = new Group(parent, SWT.NONE);
        projectsGroup.setText(IMPORT_LIBRARIES_LABEL);
        GridData gdProjects = new GridData(GridData.FILL_BOTH);
        //gdProjects.horizontalSpan = 2;
        projectsGroup.setLayoutData(gdProjects);
        projectsGroup.setLayout(new GridLayout(1, false));
        
        // main table to hold the library entries
        Table librariesTable = new Table(projectsGroup ,SWT.CHECK | SWT.BORDER | SWT.V_SCROLL);
        librariesTable.setHeaderVisible(false);

        TableColumn col1 = new TableColumn(librariesTable, SWT.NONE);
        col1.setWidth(200);
        //col1.setText("Processing Library");

        TableLayout tableLayout = new TableLayout();
        librariesTable.setLayout(tableLayout);

        GridData viewerData = new GridData(GridData.FILL_BOTH);
        viewerData.horizontalSpan = 2;
        viewerData.heightHint = 200;

        // jface component to deal w/ data in table and checkboxes
        libraries_viewer = new CheckboxTableViewer(librariesTable);
        libraries_viewer.getControl().setLayoutData(viewerData);
        libraries_viewer.setContentProvider(new SelectedLibrariesContentProvider());
        libraries_viewer.setLabelProvider(new SelectedLibrariesLabelProvider());
        showDiscoveredLibraries(new File(getProjectConfiguration().getProcessingPath()));
        libraries_viewer.addCheckStateListener(new ICheckStateListener() {
            public void checkStateChanged(CheckStateChangedEvent event) {
                saveConfiguration();
            }
        });
	}
	
	
	private void setLibriesViewerInput(String[] allLibraryIdentifiers) {
	    libraries_viewer.setInput(allLibraryIdentifiers);
    }

    public ArrayList<String> getSelectedLibraries() {
	    ArrayList<String> libs = new ArrayList<String>();
	    for (Object element : libraries_viewer.getCheckedElements()) {
	       libs.add((String) element);
	    }
	    return libs;
	}
	
	/**
	 * invoked any time setPageComplete(true) is called
	 * This then calls saveConfiguration()
	 * So the best thing to do is after any changes by the user,
	 * call setPageComplete(true) to have this run and check the changes
	 * and then save the changes if everything looks ok
	 * 
	 * @see org.eclipse.jface.wizard.WizardPage#isPageComplete()
	 */
    public boolean isPageComplete() {
    	
        setErrorMessage(null);
        
        // project name is ok, what about processing path?
        //File processingPath = new File(processing_path_text.getText());
        //showDiscoveredLibraries(processingPath);
        //setSelectedLibraries();         
        
        String projName = project_name_text.getText();
        char[] cs = projName.toCharArray();
        
        // no project name
        if (cs.length < 1) return false;
        
        // project name too long!
        if (cs.length > PROJECT_NAME_MAXSIZE) {
            setErrorMessage("Project name limit reached!");
            return false;
        }
        
        // invalid project name
        for (int i = 0; i < cs.length; i++) {
            if (cs[i] == ' ' || cs[i] == '_') {
                continue;
            }
            if (!Character.isLetterOrDigit(cs[i])) {
                setErrorMessage("Invalid project name.");
                return false;
            }
        }       
        
        // project already exists
        IProject proj = 
        	ResourcesPlugin.getWorkspace().getRoot().getProject(projName);
        if (proj.exists()) {
            setErrorMessage("Project with name " + projName + " already exists.");
            return false;
        }     
        
        File processingPath = new File(processing_path_text.getText());
        if (!processingPath.exists()) {
        	setErrorMessage("Processing path (" + 
        			processing_path_text.getText() + ") does not exist.");
        	return false;
        }
        
        // final check for core.jar
        File core = new File(processingPath, 
                OSHelperManager.getHelper().getCorePath() + "core.jar");
        if (!core.exists()) {
            setErrorMessage(core.getAbsolutePath() + " does not contain the processing libs.");
            return false;
        }
           
        saveConfiguration();
        return true;
        
    }
    
    private void showDiscoveredLibraries(File processingPath) {
        File librariesDir = new File(processingPath,
                OSHelperManager.getHelper().getLibraryPath());
        List<String> libraries = new ArrayList<String>();
        if (librariesDir.exists()) { 
            String[] files = librariesDir.list();
            for (String file : files) {
                if ((new File(librariesDir, file)).isDirectory())
                    libraries.add(file);
            }
        }
        setLibriesViewerInput(
                libraries.toArray(new String[libraries.size()]));
    }
    
    private void setSelectedLibraries() {
        List<String> selectedLibs = getProjectConfiguration().getSelectedLibraries();
        if (selectedLibs != null)
            libraries_viewer.setCheckedElements(selectedLibs.toArray());
        libraries_viewer.refresh();
    }

    private void saveConfiguration() {
    	getProjectConfiguration().setSelectedLibraries(getSelectedLibraries());
    	getProjectConfiguration().setProjectName(project_name_text.getText());
    	getProjectConfiguration().setApp(appButton.getSelection());
    	getProjectConfiguration().setProcessingPath(processing_path_text.getText());
    }
    
    private ProjectConfiguration getProjectConfiguration() {
        return ((NewProcessingProjectWizard) getWizard()).getProjectConfiguration();
    }
    
    
    
    /* INNER CLASSES */
    
    
    class SelectedLibrariesContentProvider implements IStructuredContentProvider {
        public Object[] getElements(Object input) {
            if (input instanceof String[]) {
                return (String[]) input;
            }
            return null;
        }
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
        public void dispose() {
        }
    }

    class SelectedLibrariesLabelProvider extends LabelProvider implements ITableLabelProvider {
        public String getColumnText(Object element, int columnIndex) {
            String selectedLib = (String) element;
            if (columnIndex == 0) return selectedLib;
            else return "";
        }
        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }
    }   	

}
