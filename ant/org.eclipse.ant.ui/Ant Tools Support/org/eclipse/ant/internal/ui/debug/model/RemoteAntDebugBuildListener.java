/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ant.internal.ui.debug.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import org.eclipse.ant.internal.ui.launchConfigurations.RemoteAntBuildListener;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IProcess;

public class RemoteAntDebugBuildListener extends RemoteAntBuildListener {
	
	// sockets to communicate with the remote Ant debug build logger
	private Socket fRequestSocket;
	private PrintWriter fRequestWriter;
	private BufferedReader fRequestReader;
	
	private int fRequestPort= -1;
	
	private AntDebugTarget fTarget;
	
	/**
	 * Reader thread that processes request responses from the remote Ant debug build logger
	 */
	private class ReaderThread extends Thread {
		public ReaderThread() {
			super("Ant Request Response Reader Thread"); //$NON-NLS-1$
			setDaemon(true);
		}

		public void run(){
			try { 
				String message= null; 
				while (fRequestReader != null) { 
					if ((message= fRequestReader.readLine()) != null) {
						receiveMessage(message);
					}
				} 
			} catch (Exception e) {
				RemoteAntDebugBuildListener.this.shutDown();
			}
		}
	}	
	
	public RemoteAntDebugBuildListener(ILaunch launch) {
		super(launch);
	}
	
	protected void receiveMessage(String message) {
		if (fDebug) {
			System.out.println(message);
		}
		if (message.startsWith(DebugMessageIds.BUILD_STARTED)) {
			IProcess process= getProcess();
			fTarget= new AntDebugTarget(fLaunch, process, this);
			fLaunch.addDebugTarget(fTarget);
			
			connectRequest();
			
			fTarget.buildStarted();
		} else if (message.startsWith(DebugMessageIds.BUILD_FINISHED)) {
			fTarget.terminated();
		} else if (message.startsWith(DebugMessageIds.TARGET_STARTED)){

		} else if (message.startsWith(DebugMessageIds.TARGET_FINISHED)) {
			
		} else if (message.startsWith(DebugMessageIds.TASK_STARTED)){
			
		} else if (message.startsWith(DebugMessageIds.TASK_FINISHED)){
			
		} else if (message.startsWith(DebugMessageIds.SUSPENDED)){
			if (message.endsWith(DebugMessageIds.CLIENT_REQUEST)) {
				fTarget.suspended(DebugEvent.CLIENT_REQUEST);
			} else if (message.endsWith(DebugMessageIds.STEP)) {
				fTarget.suspended(DebugEvent.STEP_END);
			} else if (message.indexOf(DebugMessageIds.BREAKPOINT) >= 0) {
				fTarget.breakpointHit(message);
			}
		} else if (message.startsWith(DebugMessageIds.RESUMED)){
			if (message.endsWith(DebugMessageIds.STEP)) {
				((AntThread)fTarget.getThreads()[0]).setStepping(true);
				fTarget.resumed(DebugEvent.STEP_OVER);
			} else if (message.endsWith(DebugMessageIds.CLIENT_REQUEST)) {
				fTarget.resumed(DebugEvent.CLIENT_REQUEST);
			}
		} else if (message.startsWith(DebugMessageIds.TERMINATED)){
			fTarget.terminated();
		} else if (message.startsWith(DebugMessageIds.STACK)){
			try {
				AntStackFrame frame= (AntStackFrame) fTarget.getThreads()[0].getTopStackFrame();
				frame.init(message);
			} catch (DebugException de) {
				
			}
		} else {
			super.receiveMessage(message);
		}
	}
	
	/**
	 * 
	 */
	private void connectRequest() {
		try {
			fRequestSocket = new Socket("localhost", fRequestPort); //$NON-NLS-1$
			fRequestWriter = new PrintWriter(fRequestSocket.getOutputStream(), true);
			fRequestReader = new BufferedReader(new InputStreamReader(fRequestSocket.getInputStream()));
			
			ReaderThread readerThread= new ReaderThread();
			readerThread.start();
		} catch (UnknownHostException e) {
			//TODO
			//fTarget.abort("Unable to connect to PDA VM", e);
			
		} catch (IOException e) {
			//fTarget.abort("Unable to connect to PDA VM", e);
			//TODO
		}
	}

	/**
	 * Start listening to an Ant build. Start a server connection that
	 * the RemoteAntBuildLogger can connect to.
	 * 
	 * @param port The port number to create the server connection on
	 */
	public synchronized void startListening(int eventPort, int requestPort) {
		super.startListening(eventPort);
		fRequestPort= requestPort;
	}
	
	/**
	 * Sends a request to the Ant build
	 * 
	 * @param request debug command
	 */
	protected void sendRequest(String request) {
		if (fRequestSocket == null) {
			return;
		}
		synchronized (fRequestSocket) {
			fRequestWriter.println(request);
		}		
	}
	
	protected synchronized void shutDown() {
		if (fDebug) {
			System.out.println("shutdown " + fRequestPort); //$NON-NLS-1$
		}
		fLaunch= null;
		DebugPlugin.getDefault().getLaunchManager().removeLaunchListener(this);
		try {
			if (fRequestReader != null) {
				fRequestReader.close();
				fRequestReader= null;
			}
		} catch(IOException e) {
		}	
		if (fRequestWriter != null) {
			fRequestWriter.close();
			fRequestWriter= null;
		}
		try{
			if(fRequestSocket != null) {
				fRequestSocket.close();
				fRequestSocket= null;
			}
		} catch(IOException e) {
		}
		super.shutDown();
	}
}