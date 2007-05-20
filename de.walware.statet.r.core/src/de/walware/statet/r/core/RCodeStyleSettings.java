/*******************************************************************************
 * Copyright (c) 2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core;

import java.util.Map;
import java.util.concurrent.locks.Lock;

import de.walware.eclipsecommons.preferences.AbstractPreferencesModelObject;
import de.walware.eclipsecommons.preferences.IPreferenceAccess;
import de.walware.eclipsecommons.preferences.Preference;
import de.walware.eclipsecommons.preferences.Preference.BooleanPref;
import de.walware.eclipsecommons.preferences.Preference.EnumPref;
import de.walware.eclipsecommons.preferences.Preference.IntPref;


/**
 * Settings for style of R code.
 */
public class RCodeStyleSettings extends AbstractPreferencesModelObject {
	
	public static final String CONTEXT_ID = "r.codestyle"; //$NON-NLS-1$

	public static enum IndentationType {
		TAB, SPACES,
	}
	
	public static final IntPref PREF_TAB_SIZE = new IntPref(
			RCorePreferenceNodes.CAT_R_CODESTYLE_QUALIFIER, "tab.size"); //$NON-NLS-1$
	public static final String PROP_TAB_SIZE = "tabSize"; //$NON-NLS-1$
	
	public static final EnumPref<IndentationType> PREF_INDENT_DEFAULT_TYPE = new EnumPref<IndentationType>(
			RCorePreferenceNodes.CAT_R_CODESTYLE_QUALIFIER, "indent.default.type", IndentationType.class); //$NON-NLS-1$
	public static final String PROP_INDENT_DEFAULT_TYPE = "indentDefaultType"; //$NON-NLS-1$
	
	public static final IntPref PREF_INDENT_SPACES_COUNT = new IntPref(
			RCorePreferenceNodes.CAT_R_CODESTYLE_QUALIFIER, "indent.spaces.count"); //$NON-NLS-1$
	public static final String PROP_INDENT_SPACES_COUNT = "indentSpacesCount"; //$NON-NLS-1$

	public static final BooleanPref PREF_REPLACE_TABS_WITH_SPACES = new BooleanPref(
			RCorePreferenceNodes.CAT_R_CODESTYLE_QUALIFIER, "indent.replace_tabs.enable"); //$NON-NLS-1$
	public static final String PROP_REPLACE_TABS_WITH_SPACES = "replaceOtherTabsWithSpaces"; //$NON-NLS-1$

	public static final IntPref PREF_INDENT_BLOCK_DEPTH = new IntPref(
			RCorePreferenceNodes.CAT_R_CODESTYLE_QUALIFIER, "indent.block.depth"); //$NON-NLS-1$
	public static final String PROP_INDENT_BLOCK_DEPTH = "indentBlockDepth"; //$NON-NLS-1$
	
	public static final IntPref PREF_INDENT_WRAPPED_COMMAND_DEPTH = new IntPref(
			RCorePreferenceNodes.CAT_R_CODESTYLE_QUALIFIER, "indent.wrapped_command.depth"); //$NON-NLS-1$
	public static final String PROP_INDENT_WRAPPED_COMMAND_DEPTH = "indentWrappedCommandDepth"; //$NON-NLS-1$
	
	
	private boolean fEditMode;
	
	private int fTabSize;
	private IndentationType fIndentDefaultType;
	private int fIndentSpacesCount;
	private int fIndentBlockDepth;
	private int fIndentWrappedCommandDepth;
	private boolean fReplaceOtherTabsWithSpaces;

	
	/**
	 * Creates an instance with default settings.
	 */
	public RCodeStyleSettings(boolean editMode) {
		super(!editMode);
		fEditMode = editMode;
		loadDefaults();
		resetDirty();
	}
	
	/**
	 * Creates an instance with default settings.
	 */
	public RCodeStyleSettings() {
		this(false);
	}
	
	
	@Override
	public String[] getNodeQualifiers() {
		return new String[] { RCorePreferenceNodes.CAT_R_CODESTYLE_QUALIFIER };
	}
	
	@Override
	public synchronized void loadDefaults() {
		setTabSize(4);
		setIndentDefaultType(IndentationType.TAB);
		setIndentSpacesCount(4);
		setIndentBlockDepth(1);
		setIndentWrappedCommandDepth(2);
		setReplaceOtherTabsWithSpaces(false);
	}
	
	@Override
	public synchronized void load(IPreferenceAccess prefs) {
		setTabSize(prefs.getPreferenceValue(PREF_TAB_SIZE));
		setIndentDefaultType(prefs.getPreferenceValue(PREF_INDENT_DEFAULT_TYPE));
		setIndentSpacesCount(prefs.getPreferenceValue(PREF_INDENT_SPACES_COUNT));
		setReplaceOtherTabsWithSpaces(prefs.getPreferenceValue(PREF_REPLACE_TABS_WITH_SPACES));
		setIndentBlockDepth(prefs.getPreferenceValue(PREF_INDENT_BLOCK_DEPTH));
		setIndentWrappedCommandDepth(prefs.getPreferenceValue(PREF_INDENT_WRAPPED_COMMAND_DEPTH));
	}

	public void load(RCodeStyleSettings source) {
		Lock sourceLock = source.getReadLock();
		Lock writeLock = getWriteLock();
		try {
			sourceLock.lock();
			writeLock.lock();
			
			setTabSize(source.fTabSize);
			setIndentDefaultType(source.fIndentDefaultType);
			setIndentSpacesCount(source.fIndentSpacesCount);
			setReplaceOtherTabsWithSpaces(source.fReplaceOtherTabsWithSpaces);
			setIndentBlockDepth(source.fIndentBlockDepth);
			setIndentWrappedCommandDepth(source.fIndentWrappedCommandDepth);
		}
		finally {
			sourceLock.unlock();
			writeLock.unlock();
		}
	}

	@Override
	public synchronized Map<Preference, Object> deliverToPreferencesMap(Map<Preference, Object> map) {
		map.put(PREF_TAB_SIZE, getTabSize());
		map.put(PREF_INDENT_DEFAULT_TYPE, getIndentDefaultType());
		map.put(PREF_INDENT_SPACES_COUNT, getIndentSpacesCount());
		map.put(PREF_REPLACE_TABS_WITH_SPACES, getReplaceOtherTabsWithSpaces());
		map.put(PREF_INDENT_BLOCK_DEPTH, getIndentBlockDepth());
		map.put(PREF_INDENT_WRAPPED_COMMAND_DEPTH, getIndentWrappedCommandDepth());
		return map;
	}

	
/*-- Properties --------------------------------------------------------------*/
	
	public void setTabSize(int size) {
		int oldValue = fTabSize;
		fTabSize = size;
		firePropertyChange(PROP_TAB_SIZE, oldValue, size);
	}
	public int getTabSize() {
		return fTabSize;
	}

	public void setIndentDefaultType(IndentationType type) {
		IndentationType oldValue = fIndentDefaultType;
		fIndentDefaultType = type;
		firePropertyChange(PROP_INDENT_DEFAULT_TYPE, oldValue, type);
	}
	public IndentationType getIndentDefaultType() {
		return fIndentDefaultType;
	}

	public void setIndentSpacesCount(int count) {
		int oldValue = fIndentSpacesCount;
		fIndentSpacesCount = count;
		firePropertyChange(PROP_INDENT_SPACES_COUNT, oldValue, count);
	}
	public int getIndentSpacesCount() {
		return fIndentSpacesCount;
	}

	public void setIndentBlockDepth(int depth) {
		int oldValue = fIndentBlockDepth;
		fIndentBlockDepth = depth;
		firePropertyChange(PROP_INDENT_BLOCK_DEPTH, oldValue, depth);
	}
	public int getIndentBlockDepth() {
		return fIndentBlockDepth;
	}

	public void setIndentWrappedCommandDepth(int depth) {
		int oldValue = fIndentWrappedCommandDepth;
		fIndentWrappedCommandDepth = depth;
		firePropertyChange(PROP_INDENT_WRAPPED_COMMAND_DEPTH, oldValue, depth);
	}
	public int getIndentWrappedCommandDepth() {
		return fIndentWrappedCommandDepth;
	}
	
	public void setReplaceOtherTabsWithSpaces(boolean enable) {
		boolean oldValue = fReplaceOtherTabsWithSpaces;
		fReplaceOtherTabsWithSpaces = enable;
		firePropertyChange(PROP_REPLACE_TABS_WITH_SPACES, oldValue, getReplaceOtherTabsWithSpaces());
	}
	public boolean getReplaceOtherTabsWithSpaces() {
		if (fEditMode) {
			return fReplaceOtherTabsWithSpaces;
		}
		return (fIndentDefaultType == IndentationType.SPACES) && fReplaceOtherTabsWithSpaces;
	}

}