package org.eclipse.debug.internal.ui.launchConfigurations;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;

/**
 * Deletes the selected launch configuration(s).
 */
public class DeleteLaunchConfigurationAction extends AbstractLaunchConfigurationAction {

	/**
	 * Action identifier for IDebugView#getAction(String)
	 */
	public static final String ID_DELETE_ACTION = DebugUIPlugin.getUniqueIdentifier() + ".ID_DELETE_ACTION";
	
	/**
	 * Constructs an action to delete launch configuration(s) 
	 */
	public DeleteLaunchConfigurationAction(Viewer viewer) {
		super("Dele&te", viewer);
	}

	/**
	 * @see AbstractLaunchConfigurationAction#performAction()
	 */
	protected void performAction() {
		IStructuredSelection selection = getStructuredSelection();

		// Make the user confirm the deletion
		String dialogMessage = selection.size() > 1 ? LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.Do_you_wish_to_delete_the_selected_launch_configurations__1") : LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.Do_you_wish_to_delete_the_selected_launch_configuration__2"); //$NON-NLS-1$ //$NON-NLS-2$
		boolean ok = MessageDialog.openQuestion(getShell(), LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.Confirm_Launch_Configuration_Deletion_3"), dialogMessage); //$NON-NLS-1$
		if (!ok) {
			return;
		}

		getViewer().getControl().setRedraw(false);
		Iterator iterator = selection.iterator();
		while (iterator.hasNext()) {
			ILaunchConfiguration configuration = (ILaunchConfiguration)iterator.next();
			try {
				configuration.delete();
			} catch (CoreException e) {
				errorDialog(e);
			}
		}
		getViewer().getControl().setRedraw(true);
	}

	/**
	 * @see org.eclipse.ui.actions.SelectionListenerAction#updateSelection(org.eclipse.jface.viewers.IStructuredSelection)
	 */
	protected boolean updateSelection(IStructuredSelection selection) {
		if (selection.isEmpty()) {
			return false;
		}
		Iterator items = selection.iterator();
		while (items.hasNext()) {
			if (!(items.next() instanceof ILaunchConfiguration)) {
				return false;
			}
		}
		return true;
	}

}
