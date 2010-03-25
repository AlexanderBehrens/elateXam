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
package de.thorstenberger.taskmodel.view.correction;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

import de.thorstenberger.taskmodel.CorrectorDelegateObject;
import de.thorstenberger.taskmodel.ManualCorrection;
import de.thorstenberger.taskmodel.TaskModelViewDelegate;
import de.thorstenberger.taskmodel.TaskStatistics;
import de.thorstenberger.taskmodel.Tasklet;

/**
 * @author Thorsten Berger
 *
 */
public class TutorCorrectionOverviewAction extends Action {

	/* (non-Javadoc)
	 * @see org.apache.struts.action.Action#execute(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		   ActionMessages errors = new ActionMessages();

			long id;
			try {
				id = Long.parseLong( request.getParameter( "taskId" ) );
			} catch (NumberFormatException e) {
				errors.add( ActionMessages.GLOBAL_MESSAGE, new ActionMessage( "invalid.parameter" ) );
				saveErrors( request, errors );
				return mapping.findForward( "error" );
			}
			
			CorrectorDelegateObject delegateObject = (CorrectorDelegateObject)TaskModelViewDelegate.getDelegateObject( request.getSession().getId(), id );

			if( delegateObject == null ){
				errors.add( ActionMessages.GLOBAL_MESSAGE, new ActionMessage( "no.session" ) );
				saveErrors( request, errors );
				return mapping.findForward( "error" );
			}
			request.setAttribute( "ReturnURL", delegateObject.getReturnURL() );

			TaskStatistics taskStatistics = delegateObject.getTaskManager().getTaskletContainer().calculateStatistics( id );
			TutorSolutionsInfoVO tsivo = new TutorSolutionsInfoVO();
			
			tsivo.setTaskId( id );
			tsivo.setCount( taskStatistics.getNumOfSolutions() );
			tsivo.setCorrectedCount( taskStatistics.getNumOfCorrectedSolutions() );
			tsivo.setAssignedCount( taskStatistics.getNumOfAssignedSolutions() );
			if( taskStatistics.getNumOfSolutions() == 0 )
				tsivo.setCorrectedCountPercent( NumberFormat.getPercentInstance().format( 0 ) );
			else tsivo.setCorrectedCountPercent( NumberFormat.getPercentInstance().format(
					(double) taskStatistics.getNumOfCorrectedSolutions() / 
					(double) taskStatistics.getNumOfSolutions() ) );
			
			List<Tasklet> assignedTasklets = delegateObject.getTaskManager().getTaskletContainer().getTaskletsAssignedToCorrector( id, delegateObject.getCorrectorLogin() );
			List<TaskletInfoVO> assignedUncorrectedTasklets = new ArrayList<TaskletInfoVO>();
			List<TaskletInfoVO> assignedCorrectedTasklets = new ArrayList<TaskletInfoVO>();
			
			
			// for the list we need the same number of corrections (columns) in every row, so
			// we expand every row that has too few columns
			int maxManualCorrections = 0;
			boolean expandSome = false;
			
			for( Tasklet tasklet : assignedTasklets ){
				TaskletInfoVO tivo = getTIVO( tasklet );
				if( tivo.getCorrections().size() > maxManualCorrections ){
					maxManualCorrections = tivo.getCorrections().size();
					expandSome = true;
				}
				if( tasklet.hasOrPassedStatus( Tasklet.Status.CORRECTED ) )
					assignedCorrectedTasklets.add( tivo );
				else
					assignedUncorrectedTasklets.add( tivo );
			}
			if( expandSome ){
				for( TaskletInfoVO tivo : assignedCorrectedTasklets ){
					for( int i = tivo.getCorrections().size(); i < maxManualCorrections; i++ )
						tivo.getCorrections().add( new TaskletInfoVO.ManualCorrectionVO( null, null ) );
				}
				for( TaskletInfoVO tivo : assignedUncorrectedTasklets ){
					for( int i = tivo.getCorrections().size(); i < maxManualCorrections; i++ )
						tivo.getCorrections().add( new TaskletInfoVO.ManualCorrectionVO( null, null ) );
				}
				
			}
			
			CorrectorsForm cform = (CorrectorsForm) form;
			cform.setAvailableCorrectors(delegateObject.getAllCorrectorNames());
			
			tsivo.setAssignedCorrectedTasklets( assignedCorrectedTasklets );
			tsivo.setAssignedUncorrectedTasklets( assignedUncorrectedTasklets );
			tsivo.setPrivileged( delegateObject.isPrivileged() );
			
			request.setAttribute( "Solutions", tsivo );
			
			return mapping.findForward( "success" );
		
	}

	public static TaskletInfoVO getTIVO( Tasklet tasklet ){
		
		TaskletInfoVO tivo = new TaskletInfoVO();
		tivo.setTaskId( tasklet.getTaskId() );
		tivo.setLogin( tasklet.getUserId() );
		
		
		// set points
		List<TaskletInfoVO.ManualCorrectionVO> taskletCorrections = new LinkedList<TaskletInfoVO.ManualCorrectionVO>();
		if( tasklet.getTaskletCorrection().isCorrected() ){
			if( tasklet.getTaskletCorrection().isAutoCorrected() )
				tivo.setAutoCorrectionPoints( tasklet.getTaskletCorrection().getAutoCorrectionPoints() );
			else{
				for( ManualCorrection mc : tasklet.getTaskletCorrection().getManualCorrections() ){
					taskletCorrections.add( new TaskletInfoVO.ManualCorrectionVO( mc.getCorrector(), mc.getPoints() ) );
				}
			}
		}
		tivo.setCorrections( taskletCorrections );
		
		tivo.setStatus( tasklet.getStatus().getValue() );
		tivo.setCorrectorLogin( tasklet.getTaskletCorrection().getCorrector() );
		tivo.setCorrectorHistory( tasklet.getTaskletCorrection().getCorrectorHistory() );
		tivo.setFlags( tasklet.getFlags() );
		tivo.setCorrigible( tasklet.hasOrPassedStatus( Tasklet.Status.SOLVED ) );
		tivo.setViewable( tasklet.hasOrPassedStatus( Tasklet.Status.INPROGRESS ));

		return tivo;
		
	}
	
	
}
