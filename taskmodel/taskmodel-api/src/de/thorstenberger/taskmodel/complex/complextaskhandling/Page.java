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
package de.thorstenberger.taskmodel.complex.complextaskhandling;

import java.util.List;


/**
 * @author Thorsten Berger
 *
 */
public interface Page {

	public static final int NOT_PROCESSED = 0;
	public static final int PARTLY_PROCESSED = 1;
	public static final int COMPLETELY_PROCESSED = 2;
		
	public int getNumber();
	
	public int getProcessStatus();
	
	public long getHash();
	
	public List<SubTasklet> getSubTasklets();
	
	public String getCategoryRefId();

//	public void setCategoryRefId( String id );
	
}
