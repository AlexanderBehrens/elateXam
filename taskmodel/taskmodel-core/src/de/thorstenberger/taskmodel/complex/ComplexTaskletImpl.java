/*

Copyright (C) 2005 Thorsten Berger

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
package de.thorstenberger.taskmodel.complex;

import java.io.File;
import java.util.List;

import de.thorstenberger.taskmodel.Annotation;
import de.thorstenberger.taskmodel.TaskApiException;
import de.thorstenberger.taskmodel.TaskFactory;
import de.thorstenberger.taskmodel.TaskModelPersistenceException;
import de.thorstenberger.taskmodel.Tasklet;
import de.thorstenberger.taskmodel.TaskletCorrection;
import de.thorstenberger.taskmodel.complex.complextaskdef.ComplexTaskDefRoot;
import de.thorstenberger.taskmodel.complex.complextaskhandling.ComplexTaskHandlingDAO;
import de.thorstenberger.taskmodel.complex.complextaskhandling.ComplexTaskHandlingRoot;
import de.thorstenberger.taskmodel.complex.complextaskhandling.CorrectionSubmitData;
import de.thorstenberger.taskmodel.complex.complextaskhandling.SubTasklet;
import de.thorstenberger.taskmodel.complex.complextaskhandling.SubmitData;
import de.thorstenberger.taskmodel.complex.complextaskhandling.Try;
import de.thorstenberger.taskmodel.impl.AbstractTasklet;

/**
 * @author Thorsten Berger
 *
 * TODO / FIXME : all lifecycle methods should move to the container for thread safety reasons
 */
public class ComplexTaskletImpl extends AbstractTasklet implements
		ComplexTasklet {

	
	private TaskDef_Complex complexTaskDef;
	private ComplexTaskDefRoot complexTaskDefRoot;
	private ComplexTaskHandlingRoot complexTaskHandlingRoot;
	private ComplexTaskHandlingDAO complexTaskHandlingDAO;
	private File xmlComplexTaskHandlingFile;
	private ComplexTaskBuilder complexTaskBuilder;
	
	/**
	 * @param taskFactory
	 * @param userId
	 * @param taskId
	 * @param status
	 * @param taskletCorrection
	 */
	public ComplexTaskletImpl(TaskFactory taskFactory, ComplexTaskBuilder complexTaskBuilder, String userId,
			long taskId, Tasklet.Status status, List<String> flags, TaskletCorrection taskletCorrection, TaskDef_Complex complexTaskDef, ComplexTaskHandlingDAO complexTaskHandlingDAO, File xmlComplexTaskHandlingFile ) {
		
		super(taskFactory, userId, taskId, status, flags, taskletCorrection );
		
		this.complexTaskDef = complexTaskDef;
		this.complexTaskDefRoot = complexTaskDef.getComplexTaskDefRoot();
		this.complexTaskHandlingDAO = complexTaskHandlingDAO;
		this.xmlComplexTaskHandlingFile = xmlComplexTaskHandlingFile;
		this.complexTaskHandlingRoot = complexTaskHandlingDAO.getComplexTaskHandlingRoot( xmlComplexTaskHandlingFile, complexTaskDefRoot );
		this.complexTaskBuilder = complexTaskBuilder;
		
		update();
		
	}

	/* (non-Javadoc)
	 * @see de.thorstenberger.taskmodel.complex.ComplexTasklet#updateStatus()
	 */
	public synchronized void update() {
			
		if( getStatus() == Status.INPROGRESS ){
			
			if( complexTaskDefRoot.hasTimeRestriction() ){
				long deadline = getActiveTry().getStartTime() + complexTaskDefRoot.getTimeInMinutesWithKindnessExtensionTime() * 60 * 1000;
				if( System.currentTimeMillis() > deadline ){
					submit();
					return;
				}
			}
			
			if( !complexTaskDef.isActive() )
				submit();
			
		}
			    
			

	}

	/* (non-Javadoc)
	 * @see de.thorstenberger.taskmodel.complex.ComplexTasklet#canStartNewTry()
	 */
	public synchronized boolean canStartNewTry() {
		if( canContinueTry() )
			return false;
		
		return ( complexTaskHandlingRoot.getNumberOfTries() < complexTaskDefRoot.getTries() ) &&
																			complexTaskDef.isActive();
	}

	/* (non-Javadoc)
	 * @see de.thorstenberger.taskmodel.complex.ComplexTasklet#startNewTry(int)
	 */
	public synchronized void startNewTry(int tryNo) throws IllegalStateException {
		// Student davon abhalten, versehentlich einen neuen Versuch zu starten
		// falls noch einer in Bearbeitung
		if( canContinueTry() ){
			continueLastTry();
			return;
		}
		
		// Status-Checks
		if( !complexTaskDef.isActive() )
			throw new IllegalStateException( TaskHandlingConstants.NOT_ACTIVE );
		// Anzahl bereits abgesendeter Versuche:
		if( complexTaskHandlingRoot.getNumberOfTries() >= complexTaskDefRoot.getTries() )
			throw new IllegalStateException( TaskHandlingConstants.TRIES_SPENT );
		// Sicherheitsabfrage, dass auch wirklich ein neuer Versuch gestartet werden soll
		if( tryNo <= complexTaskHandlingRoot.getNumberOfTries() )
			throw new IllegalStateException( TaskHandlingConstants.CANNOT_RESTART_SPENT_TRY );
		
		Try newTry = 
			complexTaskBuilder.generateTry( complexTaskDefRoot, System.currentTimeMillis() );

		complexTaskHandlingRoot.addTry( newTry );
		
		// ans Ende, sonst wird auch bei Exceptions, die beim Zusammenstellen der Aufgaben auftreten,
		// ein inkonsistenter Zustand gespeichert
		super.getTaskletCorrection().reset();
		setStatus( Status.INPROGRESS );		
		
		// und schlie�lich speichern
		try {
			save();
		} catch (TaskApiException e) {
			// TODO change interface to be able to throw TaskApiExceptions from this method
			throw new TaskModelPersistenceException( e );
		}
		
		// TODO TRY zur�ckgeben

	}

	/* (non-Javadoc)
	 * @see de.thorstenberger.taskmodel.complex.ComplexTasklet#canContinueTry()
	 */
	public synchronized boolean canContinueTry() {
		if( getStatus() == Status.INPROGRESS )
			return true;
		else
			return false;
	}

	/* (non-Javadoc)
	 * @see de.thorstenberger.taskmodel.complex.ComplexTasklet#continueLastTry()
	 */
	public synchronized void continueLastTry() throws IllegalStateException {
		if( !canContinueTry() )
			throw new IllegalStateException( TaskHandlingConstants.CANNOT_CONTINUE_TIME_EXCEEDED );
		
		
		// TODO TRY zur�ckgeben
		// ok, mehr ist erstmal nicht zu tun
	}

	/* (non-Javadoc)
	 * @see de.thorstenberger.taskmodel.complex.ComplexTasklet#canSavePage(int, long)
	 */
	public synchronized void canSavePage(int pageNo, long hashcode)
			throws IllegalStateException {
		
		if( !canContinueTry() )
			throw new IllegalStateException( TaskHandlingConstants.CANNOT_SAVE_TIME_EXCEEDED );
		Try activeTry = getActiveTry();
		if( hashcode != activeTry.getPage( pageNo ).getHash() )
			throw new IllegalStateException( TaskHandlingConstants.SUBMIT_DATA_CORRUPTED );
		
	}

	/* (non-Javadoc)
	 * @see de.thorstenberger.taskmodel.complex.ComplexTasklet#savePage(int, de.thorstenberger.taskmodel.complex.oldtaskhandling.SubmitData[], long)
	 */
	public synchronized void savePage(int pageNo, List<SubmitData> submitData, long hashcode)
			throws IllegalStateException {
		
		if( !canContinueTry() )
			throw new IllegalStateException( TaskHandlingConstants.CANNOT_SAVE_TIME_EXCEEDED );
		Try activeTry = getActiveTry();
		if( hashcode != activeTry.getPage( pageNo ).getHash() )
			throw new IllegalStateException( TaskHandlingConstants.SUBMIT_DATA_CORRUPTED );
		
//		SubTask[] subtasks = getSubTasks( pageNo );
		List<SubTasklet> subTasklets = activeTry.getPage( pageNo ).getSubTasklets();
		
		int i = 0;
		for( SubTasklet subTasklet : subTasklets )
			subTasklet.doSave( submitData.get( i++ ) );
		
		try {
			save();
		} catch (TaskApiException e) {
			throw new TaskModelPersistenceException( e );
		}

	}

	/* (non-Javadoc)
	 * @see de.thorstenberger.taskmodel.complex.ComplexTasklet#submit()
	 */
	public synchronized void submit() throws IllegalStateException {
		// falls nicht in Bearbeitung, dann einfach abbrechen,
		// d.h. wenn die Bearbeitung vorher schon automatisch wegen Zeit�berschreitung
		// gestoppt (und korrigiert) wurde, wird dies dem Student mitgeteilt
		if( !canContinueTry() )
			throw new IllegalStateException( TaskHandlingConstants.TIME_EXCEEDED_AUTO_SUBMIT_MADE );
		
		// automatische Vor-Korrektur durchf�hren
		
		boolean allCorrected = true;
		float points = 0;
		
		List<de.thorstenberger.taskmodel.complex.complextaskhandling.Page> pages = getActiveTry().getPages();
		
		for( de.thorstenberger.taskmodel.complex.complextaskhandling.Page page : pages ){
			
			List<SubTasklet> subTasklets = page.getSubTasklets();
			
			for( SubTasklet subTasklet : subTasklets ){
		
				subTasklet.doAutoCorrection();
				if( !subTasklet.isCorrected() )
					allCorrected = false;
				else
					points += subTasklet.getPoints();
				
			}
		}
		
		if( allCorrected ){
			getTaskletCorrection().setPoints( points );
			setStatus( Status.CORRECTED );
		}else
			setStatus( Status.SOLVED );
		
		
		try {
			save();
		} catch (TaskApiException e) {
			throw new TaskModelPersistenceException( e );
		}

	}

	/* (non-Javadoc)
	 * @see de.thorstenberger.taskmodel.complex.ComplexTasklet#doManualCorrection(de.thorstenberger.taskmodel.complex.oldtaskhandling.SubTask, de.thorstenberger.taskmodel.complex.oldtaskhandling.CorrectionSubmitData)
	 */
	public synchronized void doManualCorrection(SubTasklet actualSubtasklet,
			CorrectionSubmitData csd) throws IllegalStateException {

		if( canContinueTry() )
            throw new IllegalStateException( TaskHandlingConstants.CANNOT_CORRECT_TASK_IN_PROGRESS );
        if( getStatus() == Status.INITIALIZED )
            throw new IllegalStateException( TaskHandlingConstants.CANNOT_CORRECT_TASK_NOT_SOLVED );
        
        if( actualSubtasklet != null && csd != null ){
        	
	        actualSubtasklet.doManualCorrection( csd );
	
	        
			boolean allCorrected = true;
			float points = 0;
			
			List<de.thorstenberger.taskmodel.complex.complextaskhandling.Page> pages = getSolutionOfLatestTry().getPages();
			
			for( de.thorstenberger.taskmodel.complex.complextaskhandling.Page page : pages ){
				
				List<SubTasklet> subTasklets = page.getSubTasklets();
				
				for( SubTasklet subTasklet : subTasklets ){
			
					if( !subTasklet.isCorrected() )
						allCorrected = false;
					else
						points += subTasklet.getPoints();
					
				}
			}
			
			if( allCorrected ){
				getTaskletCorrection().setPoints( points );
				setStatus( Status.CORRECTED );
			}
			
        }
        
        if( csd != null ){
        	if( csd.getAnnotation() != null && csd.getAnnotation().trim().length() > 0 ){
        		getTaskletCorrection().setCorrectorAnnotation( csd.getAnnotation() );
        		addFlag( Tasklet.FLAG_HAS_CORRECTOR_ANNOTATION );
        	}else{
        		getTaskletCorrection().setCorrectorAnnotation( null );        		
        		removeFlag( Tasklet.FLAG_HAS_CORRECTOR_ANNOTATION );
        	}
        }
		
        try {
			save();
		} catch (TaskApiException e) {
			throw new TaskModelPersistenceException( e );
		}


	}

	/* (non-Javadoc)
	 * @see de.thorstenberger.taskmodel.complex.ComplexTasklet#save()
	 */
	protected synchronized void save() throws TaskApiException{
		// save the tasklet itself
		super.save();
		
		// make the DOM persistent
		complexTaskHandlingDAO.save( complexTaskHandlingRoot, xmlComplexTaskHandlingFile );
	}

	
	/* (non-Javadoc)
	 * @see de.thorstenberger.taskmodel.complex.ComplexTasklet#getActiveTry()
	 */
	public synchronized de.thorstenberger.taskmodel.complex.complextaskhandling.Try getActiveTry() throws IllegalStateException {
		
		if( getStatus() != Status.INPROGRESS )
			throw new IllegalStateException( TaskHandlingConstants.NOT_IN_PROGRESS );
		
		return complexTaskHandlingRoot.getRecentTry();
	}


	/* (non-Javadoc)
	 * @see de.thorstenberger.taskmodel.complex.ComplexTasklet#getSolutionOfLatestTry()
	 */
	public synchronized de.thorstenberger.taskmodel.complex.complextaskhandling.Try getSolutionOfLatestTry() throws IllegalStateException {
		// TODO
//		if( !getStatus().equals( CORRECTED ) && !getStatus().equals( SOLVED ) )
//			throw new IllegalStateException( TaskHandlingConstants.SHOW_CORRECTION_NOT_POSSIBLE );
		
	    
	    return complexTaskHandlingRoot.getRecentTry();

	}

	/**
	 * @return Returns the complexTaskDefRoot.
	 */
	public ComplexTaskDefRoot getComplexTaskDefRoot() {
		return complexTaskDefRoot;
	}

	/**
	 * @return Returns the complexTaskHandlingRoot.
	 */
	public ComplexTaskHandlingRoot getComplexTaskHandlingRoot() {
		return complexTaskHandlingRoot;
	}

	/* (non-Javadoc)
	 * @see de.thorstenberger.taskmodel.complex.ComplexTasklet#studentAnnotatesCorrection(java.lang.String)
	 */
	public boolean studentAnnotatesCorrection(String annotation) throws IllegalStateException {
		
		boolean change = false;
		
		if( !hasOrPassedStatus( Status.CORRECTED ) )
			throw new IllegalStateException( TaskHandlingConstants.STUDENT_CAN_ONLY_ANNOTATE_CORRECTED_TRY );
		
		if( getTaskletCorrection().getStudentAnnotations().size() > 0 && !getTaskletCorrection().getStudentAnnotations().get( 0 ).isAcknowledged() ){
			getTaskletCorrection().getStudentAnnotations().remove( 0 );
			change = true;
		}
		
		if( annotation != null && annotation.trim().length() > 0 ){
			getTaskletCorrection().addStudentAnnotation( annotation );
			setStatus( Status.ANNOTATED );
			addFlag( Tasklet.FLAG_HAS_STUDENT_ANNOTATION );
			change = true;
		}

		try {
			save();
		} catch (TaskApiException e) {
			throw new TaskModelPersistenceException( e );
		}
		
		return change;
		
	}

	/* (non-Javadoc)
	 * @see de.thorstenberger.taskmodel.complex.ComplexTasklet#acknowledgeStudentAnnotation()
	 */
	public void acknowledgeStudentAnnotation() throws IllegalStateException {
		
		if( !hasOrPassedStatus( Status.ANNOTATED ) )
			throw new IllegalStateException( TaskHandlingConstants.CORRECTOR_CAN_ONLY_ACKNOWLEDGE_IF_ANNOTATED );
		
		for( Annotation anno : getTaskletCorrection().getStudentAnnotations() ){
			if( !anno.isAcknowledged() )
				anno.setAcknowledged( true );
		}
		
		setStatus( Status.ANNOTATION_ACKNOWLEDGED );
		
		try {
			save();
		} catch (TaskApiException e) {
			throw new TaskModelPersistenceException( e );
		}
		
	}
	
	

}
