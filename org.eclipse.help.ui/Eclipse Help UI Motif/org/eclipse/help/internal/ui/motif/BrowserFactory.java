package org.eclipse.help.internal.ui.motif;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import org.eclipse.swt.widgets.Composite;
import org.eclipse.help.internal.ui.*;

/**
 * BrowserFactory
 */
public class BrowserFactory implements IBrowserFactory {
	/**
	 * BrowserFactory constructor.
	 */
	public BrowserFactory() {
		super();
	}
	/**
	 * Creates a browser control instance
	 */
	public IBrowser createBrowser(Composite parent) {
		return new WebBrowser(parent);
	}
}
