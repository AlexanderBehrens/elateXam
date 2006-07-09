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
package de.thorstenberger.taskmodel.view;

/**
 * @author Thorsten Berger
 *
 */
public class SubTaskletInfoVO {

	private String virtualSubTaskletNumber;
	private String hint;
	private float reachablePoints;
	private String problem;
	private String renderedHTML;
	
	
	/**
	 * @return Returns the hint.
	 */
	public String getHint() {
		return hint;
	}
	/**
	 * @param hint The hint to set.
	 */
	public void setHint(String hint) {
		this.hint = hint;
	}
	/**
	 * @return Returns the problem.
	 */
	public String getProblem() {
		return problem;
	}
	/**
	 * @param problem The problem to set.
	 */
	public void setProblem(String problem) {
		this.problem = problem;
	}
	/**
	 * @return Returns the reachablePoints.
	 */
	public float getReachablePoints() {
		return reachablePoints;
	}
	/**
	 * @param reachablePoints The reachablePoints to set.
	 */
	public void setReachablePoints(float reachablePoints) {
		this.reachablePoints = reachablePoints;
	}
	/**
	 * @return Returns the renderedHTML.
	 */
	public String getRenderedHTML() {
		return renderedHTML;
	}
	/**
	 * @param renderedHTML The renderedHTML to set.
	 */
	public void setRenderedHTML(String renderedHTML) {
		this.renderedHTML = renderedHTML;
	}
	/**
	 * @return Returns the virtualSubTaskletNumber.
	 */
	public String getVirtualSubTaskletNumber() {
		return virtualSubTaskletNumber;
	}
	/**
	 * @param virtualSubTaskletNumber The virtualSubTaskletNumber to set.
	 */
	public void setVirtualSubTaskletNumber(String virtualSubTaskletNumber) {
		this.virtualSubTaskletNumber = virtualSubTaskletNumber;
	}
	
	

}
