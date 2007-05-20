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

package de.walware.statet.base.core.preferences;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;

import de.walware.statet.base.internal.core.Messages;


public class SettingsChangeNotifier implements ISchedulingRule {

	
	public static interface ChangeListener {

		public void settingsChanged(Set<String> contexts);
	}
	
	public static interface ManageListener {

		public void beforeSettingsChangeNotification(Set<String> contexts);
		
		public void afterSettingsChangeNotification(Set<String> contexts);
	}
	
	
	private class NotifyJob extends Job {
		
		private String fSource;
		private Set<String> fChangeContexts = new HashSet<String>();
		
		public NotifyJob(String source) {
			super(Messages.SettingsChangeNotifier_Job_title);
			setPriority(Job.SHORT);
			setRule(SettingsChangeNotifier.this);
			fSource = source;
		}
		
		public void addContexts(String[] contexts) {
			fChangeContexts.addAll(Arrays.asList(contexts));
		}
		
		@Override
		protected IStatus run(IProgressMonitor monitor) {
			Object[] managers;
			Object[] listeners;
			synchronized (SettingsChangeNotifier.this) {
				managers = fManagers.getListeners();
				listeners = fListeners.getListeners();
				fPendingJobs.remove(fSource);
			}
			monitor.beginTask(Messages.SettingsChangeNotifier_Task_name, managers.length*5+listeners.length*5);
			for (Object obj : managers) {
				((ManageListener) obj).beforeSettingsChangeNotification(fChangeContexts);
				monitor.worked(3);
			}
			for (Object obj : listeners) {
				((ChangeListener) obj).settingsChanged(fChangeContexts);
				monitor.worked(5);
			}
			for (Object obj : managers) {
				((ManageListener) obj).afterSettingsChangeNotification(fChangeContexts);
				monitor.worked(2);
			}
			return Status.OK_STATUS;
		}
	}

	private ListenerList fManagers = new ListenerList();
	private ListenerList fListeners = new ListenerList();
	private Map<String, NotifyJob> fPendingJobs = new HashMap<String, NotifyJob>();
	
	
	public Job getNotifyJob(String source, String[] changeContext) {
		if (source == null) {
			source = "direct"; //$NON-NLS-1$
		}
		synchronized (SettingsChangeNotifier.this) {
			NotifyJob job = fPendingJobs.get(source);
			if (job != null) {
				job.addContexts(changeContext);
				return null;
			}
			job = new NotifyJob(source);
			fPendingJobs.put(source, job);
			job.addContexts(changeContext);
			return job;
		}
	}
	
	public boolean contains(ISchedulingRule rule) {
		return (rule == this);
	}
	public boolean isConflicting(ISchedulingRule rule) {
		return (rule == this);
	}

	public void addChangeListener(ChangeListener listener) {
		fListeners.add(listener);
	}
	
	public void removeChangeListener(ChangeListener listener) {
		fListeners.remove(listener);
	}
	
	public void addManageListener(ManageListener listener) {
		fManagers.add(listener);
	}
	
	public void removeManageListener(ManageListener listener) {
		fManagers.remove(listener);
	}
	
	public void dispose() {
		fManagers.clear();
		fListeners.clear();
	}
	
}