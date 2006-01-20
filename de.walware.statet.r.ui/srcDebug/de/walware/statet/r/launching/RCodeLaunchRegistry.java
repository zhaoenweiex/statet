/*******************************************************************************
 * Copyright (c) 2005 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.launching;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.debug.ui.DebugUITools;

import de.walware.eclipsecommon.preferences.PreferencesUtil;
import de.walware.eclipsecommon.preferences.Preference.StringPref;
import de.walware.statet.base.IStatetStatusConstants;
import de.walware.statet.r.internal.debug.RDebugPreferenceConstants;
import de.walware.statet.r.internal.debug.connector.RConsoleConnector;
import de.walware.statet.r.ui.RUiPlugin;

public class RCodeLaunchRegistry implements IPreferenceChangeListener {


	public static final StringPref PREF_R_CONNECTOR = new StringPref(RDebugPreferenceConstants.CAT_RCONNECTOR_QUALIFIER, "rconnector.id");

	public static void initializeDefaultValues(IScopeContext context) {
		
		PreferencesUtil.setPrefValue(context, PREF_R_CONNECTOR, RConsoleConnector.ID);
	}


	public static boolean isConfigured() {

		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IConfigurationElement[] elements = registry.getConfigurationElementsFor(RUiPlugin.ID, EXTENSION_POINT);
		return (elements != null && elements.length > 0);
	}
	
	public static void runRFileViaSource(IFile file) throws CoreException {
		
		String path = file.getLocation().toString();
		IRCodeLaunchConnector connector = getDefault().getConnector();
		if (!DebugUITools.saveBeforeLaunch())
			return;
		
		String cmd = "source(\""+path+"\");";
		connector.submit(new String[] { cmd });
	}

	public static void runRCodeDirect(String[] code) throws CoreException {
		
		IRCodeLaunchConnector connector = getDefault().getConnector();
		
		connector.submit(code);
	}
	
	public static void gotoRConsole() throws CoreException {
		
		IRCodeLaunchConnector connector = getDefault().getConnector();
		
		connector.gotoConsole();
	}

	
	static public final String EXTENSION_POINT = "rCodeLaunchConnector";
	private static final String ATT_ID = "id";
	private static final String ATT_NAME = "name";
	private static final String ATT_DESCRIPTION = "description";
	private static final String ATT_CLASS = "class";
	
	private static RCodeLaunchRegistry fgRegistry;
	
	private static synchronized RCodeLaunchRegistry getDefault() throws CoreException {
		
		if (fgRegistry == null)
			new RCodeLaunchRegistry();
		return fgRegistry;
	}

	
/* Config *********************************************************************/
	
	public static class ConnectorConfig {
		
		public final String fId;
		public final String fName;
		public final String fDescription;
		
		public ConnectorConfig(IConfigurationElement element) {

			fId = element.getAttribute(ATT_ID);;
			fName = element.getAttribute(ATT_NAME);
			fDescription = element.getAttribute(ATT_DESCRIPTION);
		}
	}
	
	public static ConnectorConfig[] getAvailableConnectors() {
		
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IConfigurationElement[] elements = registry.getConfigurationElementsFor(RUiPlugin.ID, EXTENSION_POINT);
		ConnectorConfig[] configs = new ConnectorConfig[elements.length];
		for (int i = 0; i < elements.length; i++) {
			configs[i] = new ConnectorConfig(elements[i]);
		}
		
		return configs;
	}
	
	
/* Instance *******************************************************************/	

	private IRCodeLaunchConnector fConnector;
	
	private RCodeLaunchRegistry() throws CoreException {
		
		fgRegistry = this;
		loadExtensions();
		
		new InstanceScope().getNode(PREF_R_CONNECTOR.getQualifier()).addPreferenceChangeListener(this);
	}
	
	private void loadExtensions() throws CoreException {
		
		fConnector = null;
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IConfigurationElement[] elements = registry.getConfigurationElementsFor(RUiPlugin.ID, EXTENSION_POINT);
		
		String id = PreferencesUtil.getInstancePrefs().getPreferenceValue(PREF_R_CONNECTOR);

		for (int i = 0; i < elements.length; i++) {
			if (id.equals(elements[i].getAttribute(ATT_ID))) {
				try {
					fConnector = (IRCodeLaunchConnector) elements[i].createExecutableExtension(ATT_CLASS);
					return;
				} catch (Exception e) {
					throw new CoreException(new Status(
							IStatus.ERROR,
							RUiPlugin.ID,
							IStatetStatusConstants.LAUNCHCONFIG_ERROR,
							"Error loading R Launch Connector '"+elements[i].getAttribute(ATT_NAME)+"'", e.getCause()
							));
				}
			}
		}
	}
	
	private IRCodeLaunchConnector getConnector() throws CoreException {
		
		if (fConnector == null)
			throw new CoreException(new Status(IStatus.ERROR, RUiPlugin.ID, IStatus.OK, "No R Launch Connector configured.", null));
		
		return fConnector;
	}

	public void preferenceChange(PreferenceChangeEvent event) {
		
		try {
			loadExtensions();
		} catch (CoreException e) {
			fConnector = null;
		}
	}
	
}