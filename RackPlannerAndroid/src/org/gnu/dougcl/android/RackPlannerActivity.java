package org.gnu.dougcl.android;

import android.app.Activity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;

public class RackPlannerActivity extends Activity {
	private RackPlanner frame;
	
	private static final int CANCEL = 0;
	private static final int ADD_MODULE =1;
	private static final int DELETE_MODULE=2;
	private static final int COPY_MODULE=3;
	private static final int OPEN = 10;
	private static final int SAVE = 11;
	private static final int SAVE_AS = 12;
	private static final int SAVE_AS_JPG = 13;
	private static final int EDIT_PROPERTIES =14;
	private View contextView;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        frame = new RackPlanner(this);
        setContentView(frame);
        frame.setId(1);
        registerForContextMenu(frame);
    }
    
    @Override  
    public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo) {  
    	
    	super.onCreateContextMenu(menu, v, menuInfo);  

    	if (v instanceof RackRow){
        	contextView = v;
    		//menu.setHeaderTitle("Module Menu");  
        	//menu.add(0, CANCEL, 0, "Cancel"); 
    		menu.add(0, ADD_MODULE, 0, "Add Module"); 
    		if (frame.moduleSelected()){
    			menu.add(0, COPY_MODULE, 0, "Copy Selected Module");
    			menu.add(0, DELETE_MODULE, 0, "Delete Selected Module");
    		}

    	} else if (menu.size() == 0){
    		//menu.setHeaderTitle("Rack Menu");
    		//menu.add(0, CANCEL, 0, "Cancel"); 
    		menu.add(0, OPEN, 0, "Open");  
    		menu.add(0, SAVE, 0, "Save"); 
    		menu.add(0, SAVE_AS, 0, "Save As"); 
    		menu.add(0, SAVE_AS_JPG, 0, "Save As JPG"); 
    		menu.add(0, EDIT_PROPERTIES, 0, "Edit Rack Properties"); 
    	}
    }  
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        	case ADD_MODULE:
        		System.out.println("Add selected");
        		return true;
        	case COPY_MODULE:
        		if (contextView instanceof RackRow){
        			Module m = frame.getSelectedModule().getCopy();
        			int row = contextView.getId();
        			int pos = (int)(-1.0 * frame.getRack().getHP() * frame.getRack().getLeftMargin()/frame.getRack().getScaledWidth()) + 1;
        			frame.getRack().addModule(m, row, pos);
        			frame.setSelectedModule(m);
        		}	
        		return true;
            case DELETE_MODULE:
            	frame.deleteSelectedModule();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }
}