/*=============================================================================#
 # Copyright (c) 2007-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.base.internal.core;

import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.jsch.core.IJSchService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;
import org.osgi.util.tracker.ServiceTracker;

import de.walware.ecommons.IDisposable;

import de.walware.statet.base.core.StatetCore;


/**
 * The activator class controls the plug-in life cycle
 */
public final class BaseCorePlugin extends Plugin {
	
	
	/** The shared instance. */
	private static BaseCorePlugin gPlugin;
	
	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static BaseCorePlugin getDefault() {
		return gPlugin;
	}
	
	public static void logError(final int code, final String message, final Throwable e) {
		getDefault().getLog().log(new Status(IStatus.ERROR, StatetCore.PLUGIN_ID, code, message, e));
	}
	
	
	private boolean fStarted;
	
	private final CopyOnWriteArraySet<IDisposable> fStopListeners = new CopyOnWriteArraySet<IDisposable>();
	
	private ServiceTracker fSshTracker;
	private SshSessionManager fSshSessions;
	
	
	/**
	 * The constructor
	 */
	public BaseCorePlugin() {
		gPlugin = this;
	}
	
	
	@Override
	public void start(final BundleContext context) throws Exception {
		super.start(context);
		
		fSshTracker = new ServiceTracker(context, "org.eclipse.jsch.core.IJSchService", null); //$NON-NLS-1$
		fSshTracker.open();
		
		fStarted = true;
	}
	
	@Override
	public void stop(final BundleContext context) throws Exception {
		try {
			synchronized (this) {
				fStarted = false;
				
				fSshSessions = null;
			}
			
			try {
				for (final IDisposable listener : fStopListeners) {
					listener.dispose();
				}
			}
			finally {
				fStopListeners.clear();
			}
			
			if (fSshTracker != null) {
				fSshTracker.close();
				fSshTracker = null;
			}
		}
		finally {
			gPlugin = null;
			super.stop(context);
		}
	}
	
	
	public void addStoppingListener(final IDisposable listener) {
		fStopListeners.add(listener);
	}
	
	public void removeStoppingListener(final IDisposable listener) {
		fStopListeners.remove(listener);
	}
	
	
	private static final Version REQ_JSCH_VERSION = new Version(0, 1, 49);
	
	public IJSchService getJSchService() throws CoreException {
		// E-3.5 IJSchService declarative?
		IJSchService.class.getName();
		
		{	// Check if JSch is compatible/up-to-date
			final Bundle bundle = Platform.getBundle("com.jcraft.jsch"); //$NON-NLS-1$
			if (bundle != null && bundle.getVersion().compareTo(REQ_JSCH_VERSION) < 0) {
				throw new CoreException(new Status(IStatus.ERROR, StatetCore.PLUGIN_ID, 0,
						"The installed version of the Java Secure Channel Library 'JSch' by JCraft is outdated.\n" +
						"Please install version " + REQ_JSCH_VERSION + " or newer, " +
						"for example from the update-site of WalWare.de.", null ));
			}
		}
		
		return (IJSchService) fSshTracker.getService();
	}
	
	public synchronized SshSessionManager getSshSessionManager() {
		if (fSshSessions == null) {
			if (!fStarted) {
				throw new IllegalStateException("Plug-in is not started.");
			}
			fSshSessions = new SshSessionManager();
			addStoppingListener(fSshSessions);
		}
		return fSshSessions;
	}
	
}
