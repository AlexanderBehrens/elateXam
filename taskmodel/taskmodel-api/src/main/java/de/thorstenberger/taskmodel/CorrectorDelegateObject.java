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
package de.thorstenberger.taskmodel;


/**
 * @author Thorsten Berger
 *
 */
public interface CorrectorDelegateObject extends DelegateObject{

	public String getCorrectorLogin();

	public String getCorrectorUserName();

	// do we need this?
	public TaskManager getTaskManager();

	public TaskDef getTaskDef() throws TaskApiException;

	// redundant
//	public long getTaskId();

	/**
	 * Determines wether the corrector is allowed to correct and see the Tasklets of all users.
	 * If not, the corrector may only correct assigned Tasklets.
	 * @return
	 */
	public boolean isPrivileged();

  /**
   * Get all non suspended tutors. This method returns an empty array if {@link #isPrivileged()} returns false!
   * 
   * @return
   */
  String[] getAllCorrectorNames();

}
