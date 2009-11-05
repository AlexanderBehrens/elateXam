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
package de.thorstenberger.examServer.dao.xml;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.Validator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.thorstenberger.examServer.dao.TaskHandlingDao;
import de.thorstenberger.examServer.dao.xml.jaxb.ObjectFactory;
import de.thorstenberger.examServer.dao.xml.jaxb.TaskHandling;
import de.thorstenberger.examServer.dao.xml.jaxb.TaskHandlingType.TaskletType;
import de.thorstenberger.examServer.dao.xml.jaxb.TaskHandlingType.TaskletType.AutoCorrectionType;
import de.thorstenberger.examServer.dao.xml.jaxb.TaskHandlingType.TaskletType.CorrectorAnnotationType;
import de.thorstenberger.examServer.dao.xml.jaxb.TaskHandlingType.TaskletType.CorrectorHistoryType;
import de.thorstenberger.examServer.dao.xml.jaxb.TaskHandlingType.TaskletType.ManualCorrectionType;
import de.thorstenberger.examServer.dao.xml.jaxb.TaskHandlingType.TaskletType.StudentAnnotationType;
import de.thorstenberger.examServer.model.CorrectorTaskletAnnotationVO;
import de.thorstenberger.examServer.model.StudentTaskletAnnotationVO;
import de.thorstenberger.examServer.model.TaskletVO;
import de.thorstenberger.examServer.model.TaskletVO.ManualCorrectionsVO;
import de.thorstenberger.examServer.service.ExamServerManager;
import de.thorstenberger.taskmodel.TaskModelPersistenceException;

/**
 * @author Thorsten Berger
 *
 */
public class TaskHandlingDaoImpl implements TaskHandlingDao {

	private ExamServerManager examServerManager;
	private JAXBContext jc;
	private TaskHandling taskHandling;
	
	private String taskHandlingFilePath;
	private File taskHandlingFile;
	
	private ObjectFactory objectFactory;

	private Log log = LogFactory.getLog( TaskHandlingDaoImpl.class );
	
	/**
	 * 
	 */
	public TaskHandlingDaoImpl( ExamServerManager examServerManager ) {

		this.examServerManager = examServerManager;
		
		objectFactory = new ObjectFactory();
		
		try { // JAXBException

			jc = JAXBContext.newInstance( "de.thorstenberger.examServer.dao.xml.jaxb" );

			taskHandlingFilePath = examServerManager.getRepositoryFile().getAbsolutePath() + File.separatorChar + ExamServerManager.SYSTEM + File.separatorChar + "taskhandling.xml";
			taskHandlingFile = new File( taskHandlingFilePath );

			if( !taskHandlingFile.exists() ){
				taskHandling = objectFactory.createTaskHandling();
				taskHandling.setIdCount( 0 );
				save();
				return;			    
			}


			// wenn vorhanden, dann auslesen
			Unmarshaller unmarshaller;
			unmarshaller = jc.createUnmarshaller();
			unmarshaller.setValidating( true );
			BufferedInputStream bis = new BufferedInputStream( new FileInputStream( taskHandlingFile ) );
			taskHandling = (TaskHandling) unmarshaller.unmarshal( bis );


		} catch (JAXBException e) {
			throw new RuntimeException( e );
		}catch (IOException e1){
			throw new RuntimeException( e1 );
		}
		
	}
	

	private synchronized void save(){
		try {
			
			Marshaller marshaller = jc.createMarshaller();
			Validator validator = jc.createValidator();
			validator.validate( taskHandling );
			marshaller.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, new Boolean(true) );
			
			File backup = new File( taskHandlingFilePath + ".bak" );
			taskHandlingFile.renameTo( backup );
			taskHandlingFile = new File( taskHandlingFilePath );
			
			BufferedOutputStream bos = new BufferedOutputStream( new FileOutputStream( this.taskHandlingFile ) );
			marshaller.marshal( taskHandling, bos );
			
			bos.close();
			
		} catch (JAXBException e) {
			throw new RuntimeException( e );
		} catch( IOException e1 ){
			throw new RuntimeException( e1 );
		}
		
	}

	/* (non-Javadoc)
	 * @see de.thorstenberger.examServer.dao.TaskHandlingDao#getTasklet(long, java.lang.String)
	 */
	public TaskletVO getTasklet(long taskId, String login) {

		TaskletType tt = null;
		
		synchronized ( taskHandling.getTasklet() ) {
		
			Iterator it = taskHandling.getTasklet().iterator();
			while( it.hasNext() ){
				TaskletType taskletType = (TaskletType)it.next();
				
				if( taskletType.getTaskDefId() == taskId && taskletType.getLogin().equals( login ) ){
					tt = taskletType;
					break;
				}
			}
		}
		
		if( tt != null ){
			return instantiateTaskletVO( tt );
		}
		
		return null;
		
	}
	
	
	private TaskletVO instantiateTaskletVO( TaskletType tt ){
		TaskletVO ret = new TaskletVO();
		ret.setTaskDefId( tt.getTaskDefId() ); //
		ret.setStatus( tt.getStatus() ); //
		ret.setLogin( tt.getLogin() ); //
		ret.setId( tt.getId() ); //
		ret.setCorrectorLogin( tt.getAssignedCorrector() ); //
		
		// set corrections (points)
		ret.setAutoCorrectionPoints( tt.isSetAutoCorrection() ? tt.getAutoCorrection().getPoints() : null );
		List<ManualCorrectionsVO> mvos = new LinkedList<ManualCorrectionsVO>();
		List<ManualCorrectionType> mcs = tt.getManualCorrection();
		for( ManualCorrectionType mc : mcs )
			mvos.add( ret.new ManualCorrectionsVO( mc.getCorrector(), mc.getPoints() ) );
		ret.setManualCorrections( mvos );
				
		// annotations
		List<CorrectorAnnotationType> cas = tt.getCorrectorAnnotation();
		List<CorrectorTaskletAnnotationVO> correctorAnnotationVOs = new LinkedList<CorrectorTaskletAnnotationVO>();
		for( CorrectorAnnotationType ca : cas )
			correctorAnnotationVOs.add( new CorrectorTaskletAnnotationVO( ca.getCorrector(), ca.getValue() ) );
		ret.setCorrectorAnnotations( correctorAnnotationVOs );
		
		List<StudentAnnotationType> sas = tt.getStudentAnnotation();
		List<StudentTaskletAnnotationVO> studentAnnotationVOs = new LinkedList<StudentTaskletAnnotationVO>();
		for( StudentAnnotationType sa : sas )
			studentAnnotationVOs.add( new StudentTaskletAnnotationVO( sa.getValue(), sa.getDate(), sa.isAcknowledged() ) );
		ret.setStudentAnnotations( studentAnnotationVOs );
		
		// flags
		List<String> fs = tt.getFlag();
		List<String> flags = new LinkedList<String>();
		for( String f : fs )
			flags.add( f );
		ret.setFlags( flags );
		
		// correctors history
		List<CorrectorHistoryType> ch = tt.getCorrectorHistory();
		List<String> correctors = new LinkedList<String>();
		for( CorrectorHistoryType c : ch )
			correctors.add( c.getCorrector() );
		ret.setCorrectorHistory( correctors );
		
		return ret;
		
	}
	
	

	/* (non-Javadoc)
	 * @see de.thorstenberger.examServer.dao.TaskHandlingDao#saveTasklet(de.thorstenberger.examServer.model.TaskletVO)
	 */
	public void saveTasklet(TaskletVO taskletVO) {
		
		TaskletType taskletType = null;
		
		synchronized ( taskHandling.getTasklet() ) {
			
			Iterator it = taskHandling.getTasklet().iterator();
			while( it.hasNext() ){
				TaskletType tempTaskletType = (TaskletType)it.next();
				
				if( tempTaskletType.getTaskDefId() == taskletVO.getTaskDefId() && tempTaskletType.getLogin().equals( taskletVO.getLogin() ) ){
					taskletType = tempTaskletType;
					break;
				}
			}
			
		}
		
		if( taskletType == null ){
			try {
				taskletType = objectFactory.createTaskHandlingTypeTaskletType();
				// FIXME handle overflow
				synchronized ( taskHandling ) {
					taskHandling.setIdCount( taskHandling.getIdCount() + 1 );
				}
				taskletType.setId( taskHandling.getIdCount() );
				taskletType.setTaskDefId( taskletVO.getTaskDefId() );
				taskletType.setLogin( taskletVO.getLogin() );

				synchronized ( taskHandling.getTasklet() ) {
					taskHandling.getTasklet().add( taskletType );
				}
				
			} catch (JAXBException e) {
				throw new RuntimeException( e );
			}
		}
		
		synchronized ( taskletType ) {

			taskletType.setStatus( taskletVO.getStatus() );
			taskletType.setAssignedCorrector( taskletVO.getCorrectorLogin() );
			
			// set corrections (points)
			if( taskletType.isSetAutoCorrection() ){
				if( taskletVO.getAutoCorrectionPoints() == null )
					taskletType.setAutoCorrection( null );
				else
					taskletType.getAutoCorrection().setPoints( taskletVO.getAutoCorrectionPoints() );
			}else{
				if( taskletVO.getAutoCorrectionPoints() != null ){
					AutoCorrectionType act;
					try {
						act = objectFactory.createTaskHandlingTypeTaskletTypeAutoCorrectionType();
					} catch (JAXBException e) {
						throw new TaskModelPersistenceException( e );
					}
					act.setPoints( taskletVO.getAutoCorrectionPoints() );
					taskletType.setAutoCorrection( act );
				}
			}
			if( taskletType.isSetManualCorrection() )
				taskletType.getManualCorrection().clear();
			if( taskletVO.getManualCorrections() != null ){
				for( ManualCorrectionsVO mcvo : taskletVO.getManualCorrections() ){
					ManualCorrectionType mct;
					try {
						mct = objectFactory.createTaskHandlingTypeTaskletTypeManualCorrectionType();
					} catch (JAXBException e) {
						throw new TaskModelPersistenceException( e );
					}
					mct.setCorrector( mcvo.getCorrector() );
					mct.setPoints( mcvo.getPoints() );
					taskletType.getManualCorrection().add( mct );
				}
			}
			
			// the annotations
			taskletType.getCorrectorAnnotation().clear();
			for( CorrectorTaskletAnnotationVO tavo : taskletVO.getCorrectorAnnotations() ){
				CorrectorAnnotationType cat;
				try {
					cat = objectFactory.createTaskHandlingTypeTaskletTypeCorrectorAnnotationType();
				} catch (JAXBException e) {
					throw new RuntimeException( e );
				}
				cat.setValue( tavo.getText() == null ? "" : tavo.getText() );
				cat.setCorrector( tavo.getCorrector() );
				taskletType.getCorrectorAnnotation().add( cat );
			}
			taskletType.getStudentAnnotation().clear();
			for( StudentTaskletAnnotationVO tavo : taskletVO.getStudentAnnotations() ){
				StudentAnnotationType sat;
				try {
					sat = objectFactory.createTaskHandlingTypeTaskletTypeStudentAnnotationType();
				} catch (JAXBException e) {
					throw new RuntimeException( e );
				}
				sat.setDate( tavo.getDate() );
				sat.setValue( tavo.getText() );
				sat.setAcknowledged( tavo.isAcknowledged() );
				taskletType.getStudentAnnotation().add( sat );
			}
			
			// flags
			taskletType.getFlag().clear();
			taskletType.getFlag().addAll( taskletVO.getFlags() );
			
			// correctors history
			taskletType.getCorrectorHistory().clear();
			
			if( taskletVO.getCorrectorHistory() != null && taskletVO.getCorrectorHistory().size() > 0 ){
				List<String> ch = taskletVO.getCorrectorHistory();
				for( String c : ch ){
					CorrectorHistoryType cht;
					try {
						cht = objectFactory.createTaskHandlingTypeTaskletTypeCorrectorHistoryType();
					} catch (JAXBException e) {
						throw new RuntimeException( e );
					}
					cht.setCorrector( c );
					taskletType.getCorrectorHistory().add( cht );
				}
			}

		}
		
		save();
		
	}


	/* (non-Javadoc)
	 * @see de.thorstenberger.examServer.dao.TaskHandlingDao#getTasklets(java.lang.String)
	 */
	public List<TaskletVO> getTasklets(String login) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see de.thorstenberger.examServer.dao.TaskHandlingDao#getTasklets(long)
	 */
	public List<TaskletVO> getTasklets(long taskId) {
		
		List<TaskletType> matchingTaskletTypes = new ArrayList<TaskletType>();

		synchronized ( taskHandling.getTasklet() ) {

			Iterator it = taskHandling.getTasklet().iterator();
			while( it.hasNext() ){
				TaskletType taskletType = (TaskletType)it.next();
				
				if( taskletType.getTaskDefId() == taskId )
					matchingTaskletTypes.add( taskletType );
				
			}
		}

		List<TaskletVO> ret = new ArrayList<TaskletVO>();

		for( TaskletType taskletType : matchingTaskletTypes )
			ret.add( instantiateTaskletVO( taskletType ) );
		
		return ret;
	}

	/* (non-Javadoc)
	 * @see de.thorstenberger.examServer.dao.TaskHandlingDao#getUserIdsOfAvailableTasklets(long)
	 */
	public List<String> getUserIdsOfAvailableTasklets(long taskId) {
		List<String> ret = new ArrayList<String>();
		
		synchronized ( taskHandling.getTasklet() ) {
			Iterator it = taskHandling.getTasklet().iterator();
			while( it.hasNext() ){
				TaskletType taskletType = (TaskletType)it.next();
				if( taskletType.getTaskDefId() == taskId )
					ret.add( taskletType.getLogin() );
			}
		}
		
		return ret;
	}
	
	public List<String> getUserIdsOfTaskletsAssignedToCorrector( long taskId, String correctorId ){
		if( correctorId == null )
			throw new NullPointerException();
		
		List<String> ret = new ArrayList<String>();
		synchronized ( taskHandling.getTasklet() ) {
			
			Iterator it = taskHandling.getTasklet().iterator();
			while( it.hasNext() ){
				TaskletType taskletType = (TaskletType)it.next();
				if( taskletType.getTaskDefId() == taskId && correctorId.equals( taskletType.getAssignedCorrector() ) )
					ret.add( taskletType.getLogin() );
			}
			
		}
		return ret;
	}


	/* (non-Javadoc)
	 * @see de.thorstenberger.examServer.dao.TaskHandlingDao#getTasklets()
	 */
	public List<TaskletVO> getTasklets() {
		throw new UnsupportedOperationException();
	}

}
