/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ant.internal.ui.antsupport.inputhandler;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.input.DefaultInputHandler;
import org.apache.tools.ant.input.InputRequest;
import org.eclipse.ant.internal.ui.antsupport.RemoteAntMessages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class SWTInputHandler extends DefaultInputHandler {
	
	private Text fText;
	private Text fErrorMessageText;
	private Button fOkButton;
	private Shell fDialog;
	private FontMetrics fFontMetrics;
	private InputRequest fRequest;
	
	
	/* (non-Javadoc)
	 * @see org.apache.tools.ant.input.InputHandler#handleInput(org.apache.tools.ant.input.InputRequest)
	 */
	public void handleInput(InputRequest request) throws BuildException {
		if (System.getProperty("eclipse.ant.noInput") != null) { //$NON-NLS-1$
			throw new BuildException(RemoteAntMessages.getString("SWTInputHandler.0")); //$NON-NLS-1$
		}
		fRequest= request;
		BuildException[] problem= new BuildException[1];
		Runnable runnable= getHandleInputRunnable(problem);
		Display.getDefault().syncExec(runnable);
		if (problem[0] != null) {
			throw problem[0];
		}
	}
	
	protected Runnable getHandleInputRunnable(final BuildException[] problem) {
		return new Runnable() {
			public void run() {
				String prompt = getPrompt(fRequest);
		       	String title= RemoteAntMessages.getString("SWTInputHandler.1"); //$NON-NLS-1$
		       	boolean[] result = new boolean[1];
				open(title, prompt, result);
		
				if (!result[0]) {
					problem[0]= new BuildException(RemoteAntMessages.getString("SWTInputHandler.2")); //$NON-NLS-1$
				}
			}
		};
	}
	
	private void open(String title, String prompt, boolean[] result) {
		Display display= Display.getDefault();
		
		fDialog = new Shell(display, SWT.DIALOG_TRIM
				| SWT.APPLICATION_MODAL | SWT.RESIZE);
		initializeDialogUnits(fDialog);
		fDialog.setLayout(new GridLayout());
		
		GridData gd= new GridData(SWT.FILL);
		gd.horizontalSpan= 2;
		fDialog.setLayoutData(gd);
		fDialog.setText(title);
		Label label= new Label(fDialog, SWT.WRAP);
		label.setText(prompt);
		GridData data = new GridData(GridData.GRAB_HORIZONTAL
                | GridData.GRAB_VERTICAL | GridData.HORIZONTAL_ALIGN_FILL
                | GridData.VERTICAL_ALIGN_CENTER);
        
        data.widthHint = convertHorizontalDLUsToPixels(300);
        label.setLayoutData(data);
        label.setFont(fDialog.getFont());
        
        fText = new Text(fDialog, SWT.SINGLE | SWT.BORDER);
        fText.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
                | GridData.HORIZONTAL_ALIGN_FILL));
        fText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                validateInput();
            }
        });
		
		fErrorMessageText = new Text(fDialog, SWT.READ_ONLY);
        fErrorMessageText.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
                | GridData.HORIZONTAL_ALIGN_FILL));
        fErrorMessageText.setBackground(fErrorMessageText.getDisplay()
                .getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
        
		
		createButtonBar(fDialog, result);
		fDialog.pack();
		fDialog.open();

		while (!fDialog.isDisposed()) {
			if (!display.readAndDispatch()) display.sleep();
		}
		display.dispose();
	}

    protected void setErrorMessage(String errorMessage) {
        fErrorMessageText.setText(errorMessage == null ? "" : errorMessage); //$NON-NLS-1$
        fOkButton.setEnabled(errorMessage == null);
        fErrorMessageText.getParent().update();
    }
    
    protected void validateInput() {
        String errorMessage = null;
        fRequest.setInput(fText.getText());
        if (!fRequest.isInputValid()) {
        	errorMessage= RemoteAntMessages.getString("SWTInputHandler.3"); //$NON-NLS-1$
       }
       
        setErrorMessage(errorMessage); 
    }
    
    protected Control createButtonBar(Composite parent, boolean[] result) {
        Composite composite = new Composite(parent, SWT.NONE);
        // create a layout with spacing and margins appropriate for the font size.
        GridLayout layout = new GridLayout();
        layout.numColumns = 2; 
        layout.makeColumnsEqualWidth = true;
        composite.setLayout(layout);
        GridData data = new GridData(GridData.HORIZONTAL_ALIGN_END
                | GridData.VERTICAL_ALIGN_CENTER);
        composite.setLayoutData(data);
        composite.setFont(parent.getFont());
        // Add the buttons to the button bar.
        createButtonsForButtonBar(composite, result);
        return composite;
    }
    
    protected void createButtonsForButtonBar(Composite parent, final boolean[] result) {
    	fOkButton = new Button(parent, SWT.PUSH);
		fOkButton.setText(RemoteAntMessages.getString("SWTInputHandler.4")); //$NON-NLS-1$
		setButtonLayoutData(fOkButton);
		
		Button cancel = new Button(parent, SWT.PUSH);
		cancel.setText(RemoteAntMessages.getString("SWTInputHandler.5")); //$NON-NLS-1$
		Listener listener = new Listener() {
			public void handleEvent(Event event) {
				result[0] = event.widget == fOkButton;
				fDialog.close();
			}
		};
		setButtonLayoutData(cancel);
		fOkButton.addListener(SWT.Selection, listener);
		fDialog.setDefaultButton(fOkButton);
		cancel.addListener(SWT.Selection, listener);
        //do this here because setting the text will set enablement on the ok
        // button
        fText.setFocus();
        //TODO default value from the input request which appears to not be currently possible with the 
        //Ant implementation
        //if (value != null) {
          //  text.setText(value);
           // text.selectAll();
        //}
    }
    
    private void setButtonLayoutData(Button button) {
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		int widthHint = convertHorizontalDLUsToPixels(61);
		Point minSize = button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		data.widthHint = Math.max(widthHint, minSize.x);
		button.setLayoutData(data);
	}

	private int convertHorizontalDLUsToPixels(int dlus) {
        // round to the nearest pixel
        return (fFontMetrics.getAverageCharWidth() * dlus + 4 / 2)
                / 4;
	}

	protected void initializeDialogUnits(Control control) {
        // Compute and store a font metric
        GC gc = new GC(control);
        gc.setFont(control.getFont());
        fFontMetrics = gc.getFontMetrics();
        gc.dispose();
    }
}
