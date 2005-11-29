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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

import de.thorstenberger.taskmodel.CorrectorDelegateObject;
import de.thorstenberger.taskmodel.TaskApiException;
import de.thorstenberger.taskmodel.TaskModelViewDelegate;
import de.thorstenberger.taskmodel.complex.ComplexTasklet;
import de.thorstenberger.taskmodel.complex.complextaskhandling.SubTasklet;

/**
 * @author Thorsten Berger
 *
 */
public class AssignTaskletAction extends Action {

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
		
		String new_corrector = request.getParameter( "new_corrector" );
		if( new_corrector == null || new_corrector.length() == 0 ){
			
			try {
				delegateObject.getTaskManager().getTaskletContainer().assignRandomTaskletToCorrector( id, delegateObject.getCorrectorLogin() );
			} catch (TaskApiException e) {
				errors.add( ActionMessages.GLOBAL_MESSAGE, new ActionMessage( e.getMessage() ) );
				saveErrors( request, errors );
				return mapping.findForward( "success" );
			}
			
		}else if( !new_corrector.equals( "___no_selection" ) ){
			
			String userId = request.getParameter( "userId" );
			ComplexTasklet tasklet = (ComplexTasklet)delegateObject.getTaskManager().getTaskletContainer().getTasklet( id, userId );

			if( !delegateObject.isPrivileged() ){
				if( !delegateObject.getCorrectorLogin().equals( tasklet.getTaskletCorrection().getCorrector() ) ){
					errors.add( ActionMessages.GLOBAL_MESSAGE, new ActionMessage( "Unprivileged user: you need to have the Tasklet assigned to execute this action." ) );
					saveErrors( request, errors );
					return mapping.findForward( "error" );
				}
			}
			
			tasklet.assignToCorrector( new_corrector );
			
			if( delegateObject.isPrivileged() )
				return mapping.findForward( "successToCorrection" );
			
		}else{
			
			if( delegateObject.isPrivileged() )
				return mapping.findForward( "successToCorrection" );
			
		}
		
		
		return mapping.findForward( "success" );
	}

	

}
