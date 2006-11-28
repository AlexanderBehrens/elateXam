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
package de.thorstenberger.taskmodel.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;

import de.thorstenberger.taskmodel.MethodNotSupportedException;
import de.thorstenberger.taskmodel.ReportBuilder;
import de.thorstenberger.taskmodel.TaskApiException;
import de.thorstenberger.taskmodel.TaskDef;
import de.thorstenberger.taskmodel.TaskFactory;
import de.thorstenberger.taskmodel.TaskManager;
import de.thorstenberger.taskmodel.Tasklet;
import de.thorstenberger.taskmodel.UserInfo;
import de.thorstenberger.taskmodel.complex.ComplexTasklet;
import de.thorstenberger.taskmodel.complex.TaskDef_Complex;
import de.thorstenberger.taskmodel.complex.complextaskdef.Block;
import de.thorstenberger.taskmodel.complex.complextaskdef.Category;
import de.thorstenberger.taskmodel.complex.complextaskdef.Choice;
import de.thorstenberger.taskmodel.complex.complextaskdef.SubTaskDef;
import de.thorstenberger.taskmodel.complex.complextaskdef.SubTaskDefOrChoice;
import de.thorstenberger.taskmodel.complex.complextaskhandling.Page;
import de.thorstenberger.taskmodel.complex.complextaskhandling.SubTasklet;
import de.thorstenberger.taskmodel.complex.complextaskhandling.Try;
import de.thorstenberger.taskmodel.complex.complextaskhandling.subtasklets.SubTasklet_MC;

/**
 * @author Thorsten Berger
 *
 */
public class ReportBuilderImpl implements ReportBuilder {

	private TaskManager taskManager;
	private TaskFactory taskFactory;
	
	Log log = LogFactory.getLog( ReportBuilderImpl.class );
	
	private static DateFormat df = DateFormat.getDateTimeInstance( );
	
	/**
	 * @param taskletContainer
	 */
	public ReportBuilderImpl( TaskManager taskManager, TaskFactory taskFactory ) {
		this.taskManager = taskManager;
		this.taskFactory = taskFactory;
	}

	/* (non-Javadoc)
	 * @see de.thorstenberger.taskmodel.ReportBuilder#createExcelBinary(long, java.io.OutputStream)
	 */
	public void createExcelBinary(long taskId, OutputStream out) throws TaskApiException, MethodNotSupportedException{

		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet();
		TaskDef taskDef = taskManager.getTaskDef( taskId );
		
		short r = 0;
		short c = 0;

		HSSFRow row = sheet.createRow( r++ );
			row.createCell( c++ ).setCellValue( "Login" ); row.createCell( c++ ).setCellValue( "Vorname" );
			row.createCell( c++ ).setCellValue( "Name" ); row.createCell( c++ ).setCellValue( "Status" );
			row.createCell( c++ ).setCellValue( "Punkte" ); row.createCell( c++ ).setCellValue( "Korrektor" );
			row.createCell( c++ ).setCellValue( "Korrektoren-History" );
			
			// add some more columns, if complex task; e.g. points per category
			if( taskDef instanceof TaskDef_Complex ){
				
				row.createCell( c++ ).setCellValue( "Startzeit" );
				
				TaskDef_Complex ctd = (TaskDef_Complex) taskDef;
				List<Category> categories = ctd.getComplexTaskDefRoot().getCategoriesList();
				for( Category category : categories )
					row.createCell( c++ ).setCellValue( category.getTitle() + " (" + category.getId() + ")" );
							
			}

			
		List<Tasklet> tasklets = taskManager.getTaskletContainer().getTasklets( taskId );
		
		for( Tasklet tasklet : tasklets ){

			if( tasklet.getStatus() == Tasklet.Status.INITIALIZED )
				continue;
			
			row = sheet.createRow( r++ );
			c = 0;
			
			c = createUserInfoColumns( tasklet, c, wb, row );
			row.createCell( c++ ).setCellValue( tasklet.getStatus().toString() );
			row.createCell( c++ ).setCellValue( tasklet.getTaskletCorrection().getPoints() != null ? "" + tasklet.getTaskletCorrection().getPoints() : "-" );
			row.createCell( c++ ).setCellValue( tasklet.getTaskletCorrection().getCorrector() != null ? tasklet.getTaskletCorrection().getCorrector() : "-" );
			row.createCell( c++ ).setCellValue( tasklet.getTaskletCorrection().getCorrectorHistory().toString() );

			// add the additional cols for complex tasks
			if( taskDef instanceof TaskDef_Complex ){
				
				TaskDef_Complex ctd = (TaskDef_Complex) taskDef;
				ComplexTasklet ct = (ComplexTasklet)tasklet;
				row.createCell( c++ ).setCellValue( getStringFromMillis( ct.getSolutionOfLatestTry().getStartTime() ) );
				
				List<Category> categories = ctd.getComplexTaskDefRoot().getCategoriesList();
				
				// points per category
				Map<String, Float> pointsInCatMap = new Hashtable<String, Float>();
				
				// init every category with 0 points
				for( Category category : categories )
					pointsInCatMap.put( category.getId(), 0f );
				
				Try studentsLastTry;
				    
				try {
					studentsLastTry = ct.getSolutionOfLatestTry();
				} catch (IllegalStateException e) {
					log.error( "Error generating excel stream: ", e );
					throw new TaskApiException( e );
				}
					
				List<Page> pages = studentsLastTry.getPages();

				for( Page page : pages ){
					Float points = pointsInCatMap.get( page.getCategoryRefId() );
					
					// continue if marked as uncorrected earlier
					if( points == null )
						continue;
					
					boolean markCategoryAsUncorrected = false;
					
					List<SubTasklet> subTasklets = page.getSubTasklets();
					for( SubTasklet subTasklet : subTasklets ){
						try {
							points += subTasklet.getPoints();
						} catch (IllegalStateException e) {
							markCategoryAsUncorrected = true;
							break;
						}
					}
					
					if( markCategoryAsUncorrected )
						pointsInCatMap.remove( page.getCategoryRefId() );
					else
						pointsInCatMap.put( page.getCategoryRefId(), points );
					
				}
				
				for( Category category : categories ){
					Float points = pointsInCatMap.get( category.getId() );
					row.createCell( c++ ).setCellValue( points != null ? "" + points : "-" );
				}
				
			}
			
		}
		
		try {
			wb.write( out );
			out.flush();
		} catch (IOException e) {
			log.error( "Error writing excel stream!", e );
			throw new TaskApiException( e );
		}finally{
			try {
				out.close();
			} catch (IOException e) {
				throw new TaskApiException( e );
			}			
		}
			
	}
	
	private short createUserInfoColumns( Tasklet tasklet, short c, HSSFWorkbook wb, HSSFRow row ){
		UserInfo userInfo = taskFactory.getUserInfo( tasklet.getUserId() );
		
		String login;
		String firstName;
		String name;
		boolean notfound;
		
		if( userInfo != null ){
			login = userInfo.getLogin();
			firstName = userInfo.getFirstName();
			name = userInfo.getName();
			notfound = false;				
		}else{
			login = tasklet.getUserId();
			firstName = "?";
			name = "?";
			notfound = true;
		}
		
		
		if( notfound ){
			HSSFCellStyle cs2 = wb.createCellStyle();
			HSSFFont font2 = wb.createFont(); font2.setColor( HSSFColor.RED.index );
			cs2.setFont( font2 );		
			HSSFCell cell2 = row.createCell( c++ );
			cell2.setCellStyle( cs2 );
			cell2.setCellValue( login );
		}else{
			row.createCell( c++ ).setCellValue( login );
		}		
		
		row.createCell( c++ ).setCellValue( firstName );
		row.createCell( c++ ).setCellValue( name );
		
		return c;
	}	
	

	/* (non-Javadoc)
	 * @see de.thorstenberger.taskmodel.ReportBuilder#createExcelAnalysisForBlock(de.thorstenberger.taskmodel.complex.complextaskdef.Block, java.io.OutputStream)
	 */
	public void createExcelAnalysisForBlock(long taskId, String categoryId, int blockIndex, OutputStream out) throws TaskApiException, MethodNotSupportedException {
		
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet();
		TaskDef_Complex taskDef;
		try {
			taskDef = (TaskDef_Complex)taskManager.getTaskDef( taskId );
		} catch (ClassCastException e) {
			throw new MethodNotSupportedException( "Only analysis of complexTasks supported (for now)!" );
		}
		Category category = taskDef.getComplexTaskDefRoot().getCategories().get( categoryId );
		Block block = category.getBlock( blockIndex );
		
		short r = 0;
		short c = 0;
		
		HSSFRow row = sheet.createRow( r++ );
		row.createCell( c++ ).setCellValue( "Login" );
		row.createCell( c++ ).setCellValue( "Vorname" );
		row.createCell( c++ ).setCellValue( "Name" );

		if( !block.getType().equals( "mc" ) )
			throw new MethodNotSupportedException( "Only MC analysis supported (for now)!" );
		
		List<SubTaskDefOrChoice> stdocs = block.getSubTaskDefOrChoiceList();
		List<SubTaskDef> stds = new ArrayList<SubTaskDef>();
		// put all SubTaskDefs into stds
		for( SubTaskDefOrChoice stdoc : stdocs ){
			if( stdoc instanceof SubTaskDef ){
				stds.add( (SubTaskDef)stdoc );
				row.createCell( c++ ).setCellValue( ((SubTaskDef)stdoc).getId() );				
			}else{
				Choice choice = (Choice)stdoc;
				for( SubTaskDef std : choice.getSubTaskDefs() ){
					stds.add( std );
					row.createCell( c++ ).setCellValue( std.getId() );
				}
			}
		}
		
		
		List<Tasklet> tasklets = taskManager.getTaskletContainer().getTasklets( taskId );
		
		for( Tasklet tasklet : tasklets ){
			
			if( tasklet.getStatus() == Tasklet.Status.INITIALIZED )
				continue;
			
			if( !(tasklet instanceof ComplexTasklet ) )
				throw new MethodNotSupportedException( "Only analysis of complexTasklets supported (for now)!" );
				
			row = sheet.createRow( r++ );
			c = 0;

			ComplexTasklet ct = (ComplexTasklet)tasklet;
			Try actualTry = ct.getSolutionOfLatestTry();

			c = createUserInfoColumns( tasklet, c, wb, row );
			
			for( SubTaskDef std : stds ){
				
				SubTasklet_MC mcst = (SubTasklet_MC)actualTry.lookupSubTasklet( std );
				if( mcst == null ){
					row.createCell( c++ ).setCellValue( "[n/a]" );
					continue;
				}
				
				StringBuilder sb = new StringBuilder();
				List<SubTasklet_MC.Answer> answers = mcst.getAnswers();
				boolean first = true;
				for( SubTasklet_MC.Answer answer : answers ){
					if( answer.isSelected() ){
						if( !first )
							sb.append( ";" );
						sb.append( answer.getId() );
						first = false;
					}
				}	
				row.createCell( c++ ).setCellValue( sb.toString() );
				
			}
			
		}
		
		try {
			wb.write( out );
			out.flush();
		} catch (IOException e) {
			log.error( "Error writing excel stream!", e );
			throw new TaskApiException( e );
		}finally{
			try {
				out.close();
			} catch (IOException e) {
				throw new TaskApiException( e );
			}			
		}
		
	}

	private static String getStringFromMillis( long timestamp ){
		return df.format( new Date( timestamp ) );
	}
		
}

