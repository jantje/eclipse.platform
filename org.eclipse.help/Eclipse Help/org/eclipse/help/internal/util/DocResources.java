package org.eclipse.help.internal.util;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import java.util.*;
import org.eclipse.help.internal.HelpSystem;
import org.eclipse.help.internal.server.PluginURL;
import java.net.*;

/**
 * Uses a resource bundle to load images and strings from
 * a property file in a documentation plugin
 */
public class DocResources {

	static HashMap resourceBundleTable = new HashMap();
	static ArrayList pluginsWithoutResources = new ArrayList();
	static HashMap propertiesTable = new HashMap();

	/**
	 * Resources constructort.
	 */
	public DocResources() {
		super();
	}
	/**
	 * Returns a string from a property file.
	 * It uses 'name' as a the key to retrieve from a doc.properties file.
	 * this is used for translation of all manifest files (including F1)
	 */
	public static String getPluginString(String pluginID, String name) {
	
		// check plugins without resources 
		if (pluginsWithoutResources.contains(pluginID))
			return name;
	
		// check cache
		Properties properties = (Properties) propertiesTable.get(pluginID);
	
		if (properties == null) {
			// load local properties
			PluginURL propertiesURL = null;
			try {
				propertiesURL =
					new PluginURL(pluginID + "/doc.properties", "lang=" + Locale.getDefault().toString());
				Properties localProp = new Properties();
				localProp.load(propertiesURL.openStream()); //throws
				properties = localProp;
				propertiesTable.put(pluginID, properties);
			} catch (Throwable ex) {
			}
		}
		
		if(properties==null){
			// cache properties
			pluginsWithoutResources.add(pluginID);
			return name;
		}
		
		String value = properties.getProperty(name);
		if (value != null)
			return value;
	
		return name;
	}
	
}
