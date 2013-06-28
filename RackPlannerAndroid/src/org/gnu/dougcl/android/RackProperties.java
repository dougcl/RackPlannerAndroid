package org.gnu.dougcl.android;

import java.lang.reflect.Field;
import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class RackProperties {

	//Begin XML properties (these names must match the tag names in the rack xml file)
	private double scale; //zoom factor.
	private String modulesPath; //location of module zips
	private String imagesPath; //location of rack background image
	private String rackImageFilename; //the name of the background image file
	private int rackHP;//the number of horizontal positions corresponding to the bg image
	private int rows; //the number of rack rows in this rack
	private int cols;//the number of rack repeats horizontally
	//End XML Properties

	public double getScale(){
		return this.scale;
	}

	public void setScale(double scale){
		this.scale = scale;
	}

	public String getModulesPath(){
		return this.modulesPath;
	}

	public void setModulesPath(String modulesPath){
		this.modulesPath = modulesPath;
	}

	public String getImagesPath(){
		return this.imagesPath;
	}

	public void setImagesPath(String imagesPath){
		this.imagesPath = imagesPath;
	}

	public String getRackImageFilename(){
		return this.rackImageFilename;
	}

	public void setRackImageFilename(String rackImageFilename){
		this.rackImageFilename = rackImageFilename;
	}

	public int getRackHP(){
		return this.rackHP;
	}

	public void setRackHP(int rackHP){
		this.rackHP = rackHP;
	}

	public int getRows(){
		return this.rows;
	}

	public void setRows(int rows){
		this.rows = rows;
	}

	public int getCols(){
		return this.cols;
	}

	public void setCols(int cols){
		this.cols = cols;
	}


	public RackProperties(File file) throws Exception {
		super();

		try {
	    	  Field fld;
	    	  String val;
	    	  DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	    	  DocumentBuilder db = dbf.newDocumentBuilder();
	    	  Document doc = db.parse(file);

	    	  doc.getDocumentElement().normalize();
	    	  NodeList properties = doc.getElementsByTagName("properties");
	    	  if (properties == null) return;
	    	  properties = properties.item(0).getChildNodes();
	    	  //Loop through all the children of "properties" and use reflection to set the property values.
	    	  for (int i = 0; i < properties.getLength(); i++){
	    		  if (properties.item(i).getNodeType() == Node.ELEMENT_NODE) {
	    			  try {
		    			  fld = this.getClass().getDeclaredField(properties.item(i).getNodeName());
		    			  val = properties.item(i).getFirstChild().getNodeValue();
		    			  //Only integers, doubles and strings are allowed thus far. 
		    			  //Easy to add more, just copy the int.class code here to double.class, etc.
		    			  if (fld.getType().equals(int.class)){
		    				  fld.set(this, Integer.valueOf(val));
		    			  } else if (fld.getType().equals(double.class)){
		    				  fld.set(this, Double.valueOf(val));
		    			  } else {
		    				  fld.set(this, val);
		    			  }
	    			  } catch (NoSuchFieldException e1){
	    				  //Do not attempt to deal with unknown properties.
	    			  }
	    		  }  
	    	  }	    				  
	    } catch (Exception e) {
	    	throw e;
	    	/*
	  		String msg = "Rack Planner has a problem with the rack xml file: " + file + "\n" +
				"\n" +
				e.getMessage();
	  			JOptionPane.showMessageDialog(parent, msg, "Rack XML Problem", JOptionPane.ERROR_MESSAGE);
	  			System.exit(0);
	  		*/
	    }	
	}
}