/*
 *  eXist Open Source Native XML Database
 *  Copyright (C) 2001-03 Wolfgang M. Meier
 *  wolfgang@exist-db.org
 *  http://exist.sourceforge.net
 *  
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *  
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *  
 *  $Id$
 */
package org.exist.xquery;

import org.exist.dom.QName;
import org.exist.xquery.util.ExpressionDumper;
import org.exist.xquery.value.BooleanValue;
import org.exist.xquery.value.Item;
import org.exist.xquery.value.Sequence;
import org.exist.xquery.value.SequenceIterator;
import org.exist.xquery.value.Type;

/**
 * Represents a quantified expression: "some ... in ... satisfies", 
 * "every ... in ... satisfies".
 * 
 * @author Wolfgang Meier (wolfgang@exist-db.org)
 */
public class QuantifiedExpression extends BindingExpression {
	
	public final static int SOME = 0;
	public final static int EVERY = 1;
	
	private int mode = SOME;
	
	/**
	 * @param context
	 */
	public QuantifiedExpression(XQueryContext context, int mode) {
		super(context);
		this.mode = mode;
	}

    /* (non-Javadoc)
     * @see org.exist.xquery.BindingExpression#analyze(org.exist.xquery.Expression, int, org.exist.xquery.OrderSpec[])
     */
    public void analyze(Expression parent, int flags, OrderSpec orderBy[]) throws XPathException {
        LocalVariable mark = context.markLocalVariables();
		context.declareVariable(new LocalVariable(QName.parse(context, varName, null)));
		
		inputSequence.analyze(this, flags);
		returnExpr.analyze(this, flags);
		
		context.popLocalVariables(mark);
    }
    
	public Sequence eval(Sequence contextSequence, Item contextItem, Sequence resultSequence) throws XPathException {
        if (contextItem != null)
            contextSequence = contextItem.toSequence();
		LocalVariable mark = context.markLocalVariables();
		LocalVariable var = new LocalVariable(QName.parse(context, varName, null));
		context.declareVariable(var);
		Sequence inSeq = inputSequence.eval(contextSequence);
		Sequence satisfiesSeq;
		boolean found = false;
		if ( mode == EVERY )
			found = true;
		for(SequenceIterator i = inSeq.iterate(); i.hasNext(); ) {
			contextItem = i.nextItem();
			var.setValue(contextItem.toSequence());
            var.checkType();
			satisfiesSeq = returnExpr.eval(contextSequence);
			found = satisfiesSeq.effectiveBooleanValue();
			if((mode == SOME && found) || (mode == EVERY && !found))
				break;
		}
		context.popLocalVariables(mark);
		return found ? BooleanValue.TRUE : BooleanValue.FALSE;
	}

	/* (non-Javadoc)
     * @see org.exist.xquery.Expression#dump(org.exist.xquery.util.ExpressionDumper)
     */
    public void dump(ExpressionDumper dumper) {
    	dumper.display(mode == SOME ? "some" : "every");
        dumper.display(" $").display(varName).display(" in");
        dumper.startIndent();
        inputSequence.dump(dumper);
        dumper.endIndent().nl();
        dumper.display("satisfies");
        dumper.startIndent();
        returnExpr.dump(dumper);
        dumper.endIndent();
    }
    
	/* (non-Javadoc)
	 * @see org.exist.xquery.Expression#returnsType()
	 */
	public int returnsType() {
		return Type.BOOLEAN;
	}
	
	/* (non-Javadoc)
	 * @see org.exist.xquery.AbstractExpression#getDependencies()
	 */
	public int getDependencies() {
		return Dependency.CONTEXT_ITEM | Dependency.CONTEXT_SET;
	}

}
