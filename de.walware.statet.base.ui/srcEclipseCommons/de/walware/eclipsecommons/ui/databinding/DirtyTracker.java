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

package de.walware.eclipsecommons.ui.databinding;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.list.IListChangeListener;
import org.eclipse.core.databinding.observable.list.ListChangeEvent;
import org.eclipse.core.databinding.observable.list.ListDiffEntry;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;


/**
 *
 */
public class DirtyTracker implements IValueChangeListener {
	
	
	private boolean fDirty;
	
	
	public DirtyTracker(DataBindingContext dbc) {

		for (Object obj : dbc.getBindings()) {
			track((Binding) obj, true);
		}
		dbc.getBindings().addListChangeListener(new IListChangeListener() {
			public void handleListChange(ListChangeEvent event) {
				for (ListDiffEntry diff : event.diff.getDifferences()) {
					track((Binding) diff.getElement(), diff.isAddition());
				}
			}
		});
	}
	
	private void track(Binding binding, boolean add) {
		
		IObservable obs = binding.getTarget();
		if (obs instanceof IObservableValue) {
			IObservableValue value = (IObservableValue) obs;
			if (add) {
				value.addValueChangeListener(this);
			}
			else {
				value.removeValueChangeListener(this);
			}
			return;
		}
		// add more, if required
	}
	
	public void handleValueChange(ValueChangeEvent event) {
		
		handleChange();
	}
	
	public void handleChange() {
		
		fDirty = true;
	}
	
	public void resetDirty() {
		
		fDirty = false;
	}
	
	public boolean isDirty() {
		
		return fDirty;
	}
}
