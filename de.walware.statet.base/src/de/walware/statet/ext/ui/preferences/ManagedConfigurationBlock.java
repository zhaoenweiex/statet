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

package de.walware.statet.ext.ui.preferences;


import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;
import org.eclipse.ui.preferences.IWorkingCopyManager;
import org.osgi.service.prefs.BackingStoreException;

import de.walware.eclipsecommon.preferences.IPreferenceAccess;
import de.walware.eclipsecommon.preferences.Preference;
import de.walware.eclipsecommon.ui.preferences.AbstractConfigurationBlock;
import de.walware.statet.base.StatetPlugin;
import de.walware.statet.base.core.CoreUtility;


public class ManagedConfigurationBlock extends AbstractConfigurationBlock 
		implements IPreferenceAccess {

	
	protected class PreferenceManager {
		
		private IScopeContext[] fLookupOrder;
		protected final Preference[] fPreferenceKeys;
		
		private final IWorkbenchPreferenceContainer fContainer;
		/** Manager for a working copy of the preferences */
		private final IWorkingCopyManager fManager;
		/** Map saving the project settings, if disabled */
		private Map<Preference, Object> fDisabledProjectSettings;
		
		
		PreferenceManager(IWorkbenchPreferenceContainer container, Preference[] keys) {
			
			fContainer = container;
			fManager = fContainer.getWorkingCopyManager();
			fPreferenceKeys = keys;
			
			fPreferenceManager = this;
			
			if (fProject != null) {
				fLookupOrder = new IScopeContext[] {
						new ProjectScope(fProject),
						new InstanceScope(),
						new DefaultScope()
				};
			} else {
				fLookupOrder = new IScopeContext[] {
						new InstanceScope(),
						new DefaultScope()
				};
			}
			
			testIfOptionsComplete();

			// init disabled settings, if required
			if (fProject == null || hasProjectSpecificSettings(fProject)) {
				fDisabledProjectSettings = null;
			} else {
				saveDisabledProjectSettings();
			}
		
		}

		
/* Managing methods ***********************************************************/
		
		/**
		 * Checks, if project specific options exists
		 * 
		 * @param project to look up
		 * @return
		 */
		boolean hasProjectSpecificSettings(IProject project) {
			
			IScopeContext projectContext = new ProjectScope(project);
			for (Preference<Object> key : fPreferenceKeys) {
				if (getStoredValue(key, projectContext, true) != null)
					return true;
			}
			return false;
		}	

		void setUseProjectSpecificSettings(boolean enable) {
			
			boolean hasProjectSpecificOption = (fDisabledProjectSettings == null);
			
			if (enable != hasProjectSpecificOption) {
				if (enable) {
					loadDisabledProjectSettings();
				} else {
					saveDisabledProjectSettings();
				}
			}
		}
		
		private void saveDisabledProjectSettings() {

			fDisabledProjectSettings = new IdentityHashMap<Preference, Object>();
			for (Preference<Object> key : fPreferenceKeys) {
				fDisabledProjectSettings.put(key, getValue(key));
				setStoredValue(key, null); // clear project settings
			}
			
		}
		
		private void loadDisabledProjectSettings() {

			for (Preference<Object> key : fPreferenceKeys) {
				// Copy values from saved disabled settings to working store
				setValue(key, fDisabledProjectSettings.get(key));
			}
			fDisabledProjectSettings = null;
		}

		boolean processChanges(boolean saveStore) {

			List<Preference> changedOptions = new ArrayList<Preference>();
			boolean needsBuild = getChanges(changedOptions);
			if (changedOptions.isEmpty()) {
				return true;
			}
			
			boolean doBuild = false;
			if (needsBuild) {
				String[] strings = getFullBuildDialogStrings(fProject == null);
				if (strings != null) {
					MessageDialog dialog = new MessageDialog(getShell(), 
							strings[0], null, strings[1],	
							MessageDialog.QUESTION, new String[] { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL, IDialogConstants.CANCEL_LABEL }, 2);
					int res = dialog.open();
					if (res == 0) {
						doBuild = true;
					} 
					else if (res != 1) {
						return false; // cancel pressed
					}
				}
			}
			if (saveStore) {
				try {
					fManager.applyChanges();
				} catch (BackingStoreException e) {
					StatetPlugin.logUnexpectedError(e);
					return false;
				}
				if (doBuild) {
					CoreUtility.getBuildJob(fProject).schedule();
				}
			} else
				if (doBuild) {
					fContainer.registerUpdateJob(CoreUtility.getBuildJob(fProject));
				}
			return true;
		}
		
		/**
		 * 
		 * @param currContext
		 * @param changedSettings
		 * @return true, if rebuild is required.
		 */
		private boolean getChanges(List<Preference> changedSettings) {
			
			IScopeContext currContext = fLookupOrder[0];
			boolean needsBuild = false;
			for (Preference<Object> key : fPreferenceKeys) {
				String oldVal = getStoredValue(key, currContext, false);
				String val = getStoredValue(key, currContext, true);
				if (val == null) {
					if (oldVal != null) {
						changedSettings.add(key);
						needsBuild |= !oldVal.equals(getStoredValue(key, true));
					}
				} else if (!val.equals(oldVal)) {
					changedSettings.add(key);
					needsBuild |= oldVal != null || !val.equals(getStoredValue(key, true));
				}
			}
			return needsBuild;
		}
		
		
		void loadDefaults() {

			DefaultScope defaultScope = new DefaultScope();
			for (Preference<Object> key : fPreferenceKeys) {
				String defValue = getStoredValue(key, defaultScope, false);
				setValue(key, defValue);
			}

		}
		
		// DEBUG
		private void testIfOptionsComplete() {
			
			for (Preference<Object> key : fPreferenceKeys) {
				if (getStoredValue(key, false) == null) {
					System.out.println("preference option missing: " + key + " (" + this.getClass().getName() +')');  //$NON-NLS-1$//$NON-NLS-2$
				}
			}
		}

		private IEclipsePreferences getNode(IScopeContext context, String qualifier, boolean useWorkingCopy) {
			
			IEclipsePreferences node = context.getNode(qualifier);
			if (useWorkingCopy) {
				return fManager.getWorkingCopy(node);
			}
			return node;
		}

		private String getStoredValue(Preference<Object> key, IScopeContext context, boolean useWorkingCopy) {
			
			return getNode(context, key.getQualifier(), useWorkingCopy).get(key.getKey(), null);
		}
		
		private String getStoredValue(Preference<Object> key, boolean ignoreTopScope) {
			
			for (int i = ignoreTopScope ? 1 : 0; i < fLookupOrder.length; i++) {
				String value = getStoredValue(key, fLookupOrder[i], true);
				if (value != null) {
					return value;
				}
			}
			return null;
		}
		
		private void setStoredValue(Preference<Object> key, String value) {
			
			if (value != null) {
				getNode(fLookupOrder[0], key.getQualifier(), true).put(key.getKey(), value);
			} else {
				getNode(fLookupOrder[0], key.getQualifier(), true).remove(key.getKey());
			}
		}
		
		
		private <T> void setValue(Preference<T> key, T value) {
			
			IEclipsePreferences node = getNode(fLookupOrder[0], key.getQualifier(), true);
			if (value == null)
				node.remove(key.getKey());
			
			switch (key.getType()) {
			case BOOLEAN:
				node.putBoolean(key.getKey(), (Boolean) value);
				break;
			case INT:
				node.putInt(key.getKey(), (Integer) value);
				break;
			case LONG:
				node.putLong(key.getKey(), (Long) value);
				break;
			case DOUBLE:
				node.putDouble(key.getKey(), (Double) value);
				break;
			case FLOAT:
				node.putFloat(key.getKey(), (Float) value);
				break;
			default:
				node.put(key.getKey(), (String) value);
				break;
			}
		}
		
		@SuppressWarnings("unchecked")
		private <T> T getValue(Preference<T> key) {
			
			IEclipsePreferences node = null;
			int lookupIndex = 0;
			for (; lookupIndex < fLookupOrder.length; lookupIndex++) {
				IEclipsePreferences nodeToCheck = getNode(fLookupOrder[lookupIndex], key.getQualifier(), true);
				if (nodeToCheck.get(key.getKey(), null) != null) {
					node = nodeToCheck;
					break;
				}
			}
			if (node == null)
				return null;

			switch (key.getType()) {
			case BOOLEAN:
				return (T) Boolean.valueOf(node.getBoolean(key.getKey(), IPreferenceStore.BOOLEAN_DEFAULT_DEFAULT));
			case INT:
				return (T) Integer.valueOf(node.getInt(key.getKey(), IPreferenceStore.INT_DEFAULT_DEFAULT));
			case LONG:
				return (T) Long.valueOf(node.getLong(key.getKey(), IPreferenceStore.LONG_DEFAULT_DEFAULT));
			case DOUBLE:
				return (T) Double.valueOf(node.getDouble(key.getKey(), IPreferenceStore.DOUBLE_DEFAULT_DEFAULT));
			case FLOAT:
				return (T) Float.valueOf(node.getFloat(key.getKey(), IPreferenceStore.FLOAT_DEFAULT_DEFAULT));
			default:
				return (T) node.get(key.getKey(), null);
			}
		}
	}
	
	
	protected IProject fProject;
	protected PreferenceManager fPreferenceManager;
	

	public ManagedConfigurationBlock(IProject project) {

		super();
		
		fProject = project;
	}

	public ManagedConfigurationBlock() {
		this(null);
	}

	protected void setupPreferenceManager(IWorkbenchPreferenceContainer container, Preference[] keys) {
		
		new PreferenceManager(container, keys);
	}
	
	
	@Override
	public void performApply() {
		
		if (fPreferenceManager != null)
			fPreferenceManager.processChanges(true);
	}
	
	public boolean performOk() {
		
		if (fPreferenceManager != null)
			return fPreferenceManager.processChanges(false);
		return false;
	}

	public void performDefaults() {
		
		if (fPreferenceManager != null) {
			fPreferenceManager.loadDefaults();
			updateControls();
		}
	}

	
/* */

	/**
	 * Checks, if project specific options exists
	 * 
	 * @param project to look up
	 * @return
	 */
	public boolean hasProjectSpecificOptions(IProject project) {
		
		if (project != null && fPreferenceManager != null)
			return fPreferenceManager.hasProjectSpecificSettings(project);
		return false;
	}
	
	public void setUseProjectSpecificSettings(boolean enable) {
		
		super.setUseProjectSpecificSettings(enable);
		
		if (fProject != null && fPreferenceManager != null)
			fPreferenceManager.setUseProjectSpecificSettings(enable);
	}
	
	protected void updateControls() {
		
	}

	
	/* Access preference values ***************************************************/
	
	/**
	 * Returns the value for the specified preference.
	 * 
	 * @param key preference key
	 * @return value of the preference
	 */
	public <T> T getPreferenceValue(Preference<T> key) {

		assert (fPreferenceManager != null);
		
		if (fPreferenceManager.fDisabledProjectSettings != null)
			return (T) fPreferenceManager.fDisabledProjectSettings.get(key);
		return (T) fPreferenceManager.getValue(key);
	}
	
	public IEclipsePreferences[] getPreferenceNodes(String nodeQualifier) {
		
		assert (fPreferenceManager != null);
		
		IEclipsePreferences[] nodes = new IEclipsePreferences[fPreferenceManager.fLookupOrder.length - 1];
		for (int i = 0; i < nodes.length; i++) {
			nodes[i] = fPreferenceManager.getNode(fPreferenceManager.fLookupOrder[i], nodeQualifier, true);
		}
		return nodes;
	}
	
	public IScopeContext[] getPreferenceContexts() {
		
		assert (fPreferenceManager != null);
		return fPreferenceManager.fLookupOrder;
	}
	
	/**
	 * Sets a preference value in the default store.
	 * 
	 * @param key preference key
	 * @param value new value 
	 * @return old value
	 */
	public <T> T setPrefValue(Preference<T> key, T value) {
		
		assert (fPreferenceManager != null);
		
		if (fPreferenceManager.fDisabledProjectSettings != null)
			return (T) fPreferenceManager.fDisabledProjectSettings.put(key, value);
		T oldValue = (T) getPreferenceValue(key);
		fPreferenceManager.setValue(key, value);
		return oldValue;
	}
	
	public void setPrefValues(Map<Preference, Object> map) {
		
		for (Preference<Object> unit : map.keySet()) {
			setPrefValue(unit, map.get(unit));
		}
	}
		
	
	/**
	 * Changes requires full build, this method should be overwritten
	 * and return the Strings for the dialog.
	 * 
	 * @param workspaceSettings true, if settings for workspace; false, if settings for project.
	 * @return
	 */
	protected String[] getFullBuildDialogStrings(boolean workspaceSettings) {
		
		return null;
	}
}