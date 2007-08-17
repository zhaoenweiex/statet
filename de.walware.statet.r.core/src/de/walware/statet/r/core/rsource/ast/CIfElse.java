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

package de.walware.statet.r.core.rsource.ast;



/**
 *
 */
public class CIfElse extends RAstNode {
	
	boolean fWithElse = false;
	
	int fCondOpenOffset = Integer.MIN_VALUE;
	final Expression fCondExpr = new Expression();
	int fCondCloseOffset = Integer.MIN_VALUE;
	final Expression fThenExpr = new Expression();
	int fElseOffset = Integer.MIN_VALUE;
	final Expression fElseExpr = new Expression();
	
	
	@Override
	public final NodeType getNodeType() {
		return NodeType.C_IF;
	}

	public final boolean hasElse() {
		return fWithElse;
	}
	
	@Override
	public final boolean hasChildren() {
		return true;
	}
		
	@Override
	public final int getChildCount() {
		return fWithElse ? 3 : 2;
	}
	
	@Override
	public final RAstNode getChild(int index) {
		switch (index) {
		case 0:
			return fCondExpr.node;
		case 1:
			return fThenExpr.node;
		case 2:
			if (fWithElse) {
				return fElseExpr.node;
			}
		default:
			throw new IndexOutOfBoundsException();
		}
	}
	
	@Override
	public final RAstNode[] getChildren() {
		if (fWithElse) {
			return new RAstNode[] { fCondExpr.node, fThenExpr.node, fElseExpr.node };
		}
		else {
			return new RAstNode[] { fCondExpr.node, fThenExpr.node };
		}
	}
	
	@Override
	public final int getIndex(RAstNode child) {
		if (fCondExpr.node == child) {
			return 0;
		}
		if (fThenExpr.node == child) {
			return 1;
		}
		if (fElseExpr.node == child) {
			return 2;
		}
		return -1;
	}
	
	public final int getCondOpenOffset() {
		return fCondOpenOffset;
	}
	
	public final RAstNode getCondChild() {
		return fCondExpr.node;
	}
	
	public final int getCondCloseOffset() {
		return fCondCloseOffset;
	}
	
	public final RAstNode getThenChild() {
		return fThenExpr.node;
	}
	
	public final int getElseOffset() {
		return fElseOffset;
	}
	
	public final RAstNode getElseChild() {
		return fElseExpr.node;
	}
	

	@Override
	final Expression getExpr(RAstNode child) {
		if (fThenExpr.node == child) {
			return fThenExpr;
		}
		if (fElseExpr.node == child) {
			return fElseExpr;
		}
		if (fCondExpr.node == child) {
			return fCondExpr;
		}
		return null;
	}
	
	@Override
	final Expression getLeftExpr() {
		return fThenExpr;
	}
	
	@Override
	final Expression getRightExpr() {
		if (fWithElse) {
			return fElseExpr;
		}
		return fThenExpr;
	}
	
	@Override
	public final boolean equalsSingle(RAstNode element) {
		return (element.getNodeType() == NodeType.C_IF);
	}
	
	@Override
	public final void accept(RAstVisitor visitor) {
		visitor.visit(this);
	}
	
	@Override
	public final void acceptInChildren(RAstVisitor visitor) {
		fCondExpr.node.accept(visitor);
		fThenExpr.node.accept(visitor);
		if (fWithElse) {
			fElseExpr.node.accept(visitor);
		}
	}
	
	
	@Override
	final void updateStopOffset() {
		if (fWithElse && fElseExpr.node != null) {
			fStopOffset = fElseExpr.node.fStopOffset;
		}
		else if (fWithElse && fElseOffset >= 0) {
			fStopOffset = fElseOffset+4;
		}
		else if (fThenExpr.node != null) {
			fStopOffset = fThenExpr.node.fStopOffset;
		}
		else if (fCondCloseOffset >= 0) {
			fStopOffset = fCondCloseOffset+1;
		}
		else if (fCondExpr.node != null) {
			fStopOffset = fCondExpr.node.fStopOffset;
		}
		else if (fCondOpenOffset != 0) {
			fStopOffset = fCondOpenOffset+1;
		}
		else {
			fStopOffset = fStartOffset+2;
		}
	}

}
