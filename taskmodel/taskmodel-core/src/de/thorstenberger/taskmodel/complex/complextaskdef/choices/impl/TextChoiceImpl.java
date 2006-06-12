/*

Copyright (C) 2006 Thorsten Berger

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/
/**
 * 
 */
package de.thorstenberger.taskmodel.complex.complextaskdef.choices.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.thorstenberger.taskmodel.complex.complextaskdef.Choice;
import de.thorstenberger.taskmodel.complex.complextaskdef.SubTaskDef;
import de.thorstenberger.taskmodel.complex.complextaskdef.subtaskdefs.impl.TextSubTaskDefImpl;
import de.thorstenberger.taskmodel.complex.jaxb.TextSubTaskDef;

/**
 * @author Thorsten Berger
 *
 */
public class TextChoiceImpl implements Choice {

	private de.thorstenberger.taskmodel.complex.jaxb.ComplexTaskDefType.CategoryType.TextTaskBlockType.Choice choice;

	/**
	 * @param choice
	 */
	public TextChoiceImpl(de.thorstenberger.taskmodel.complex.jaxb.ComplexTaskDefType.CategoryType.TextTaskBlockType.Choice choice) {
		// TODO Auto-generated constructor stub
		this.choice = choice;
	}

	/* (non-Javadoc)
	 * @see de.thorstenberger.taskmodel.complex.complextaskdef.Choice#getSubTaskDefs()
	 */
	public List<SubTaskDef> getSubTaskDefs() {
		List<SubTaskDef> ret = new ArrayList<SubTaskDef>();
		Iterator it = choice.getTextSubTaskDef().iterator();
		while( it.hasNext() ){
			ret.add( new TextSubTaskDefImpl( (TextSubTaskDef) it.next() ) );
		}
		return ret;
	}

}
