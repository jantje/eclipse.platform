package org.eclipse.ui.externaltools.internal.ant.launchConfigurations;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.ui.externaltools.internal.model.VariableContextManager;
import org.eclipse.ui.externaltools.launchConfigurations.ExternalToolsRefreshTab;
import org.eclipse.ui.externaltools.model.IExternalToolConstants;
import org.eclipse.ui.externaltools.model.ToolUtil;
import org.eclipse.ui.externaltools.variable.ExpandVariableContext;


public class AntTabGroup extends AbstractLaunchConfigurationTabGroup {

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTabGroup#createTabs(org.eclipse.debug.ui.ILaunchConfigurationDialog, java.lang.String)
	 */
	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
		ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[] {
			new AntMainTab(),
			new ExternalToolsRefreshTab(),
			new AntTargetsTab(),
			new AntClasspathTab(),
			new AntPropertiesTab(),
			new CommonTab()
		};
		setTabs(tabs);
	}
	
	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTabGroup#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		super.setDefaults(configuration);
		// set default name for script
		VariableContextManager manager = VariableContextManager.getDefault();
		ExpandVariableContext context = manager.getVariableContext();
		IResource resource = context.getSelectedResource();
		if (resource != null && resource instanceof IFile) {
			IFile file = (IFile)resource;
			String extension = file.getFileExtension();
			if (extension != null && extension.equalsIgnoreCase("xml")) {
				StringBuffer buffer = new StringBuffer(file.getProject().getName());
				buffer.append(" "); //$NON-NLS-1$
				buffer.append(file.getName());
				String name = buffer.toString().trim();
				name= DebugPlugin.getDefault().getLaunchManager().generateUniqueLaunchConfigurationNameFrom(name);
				configuration.rename(name);
				
				StringBuffer buf = new StringBuffer();
				ToolUtil.buildVariableTag(IExternalToolConstants.VAR_WORKSPACE_LOC, file.getFullPath().toString(), buf);
				String text= buf.toString();
				if (text != null) {
					configuration.setAttribute(IExternalToolConstants.ATTR_LOCATION, text);
				}
			}		
		}
	}	
}
