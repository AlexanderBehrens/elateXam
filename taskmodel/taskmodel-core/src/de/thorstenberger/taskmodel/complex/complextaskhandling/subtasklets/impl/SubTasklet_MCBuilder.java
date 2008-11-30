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
package de.thorstenberger.taskmodel.complex.complextaskhandling.subtasklets.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import javax.xml.bind.JAXBException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.thorstenberger.taskmodel.complex.RandomUtil;
import de.thorstenberger.taskmodel.complex.complextaskhandling.subtasklets.SubTasklet_MC;
import de.thorstenberger.taskmodel.complex.jaxb.ComplexTaskHandlingType;
import de.thorstenberger.taskmodel.complex.jaxb.McSubTaskDefType;
import de.thorstenberger.taskmodel.complex.jaxb.ObjectFactory;
import de.thorstenberger.taskmodel.complex.jaxb.McSubTaskDefType.CorrectType;
import de.thorstenberger.taskmodel.complex.jaxb.McSubTaskDefType.IncorrectType;

/**
 * @author Thorsten Berger
 *
 */
public class SubTasklet_MCBuilder {

	Log log = LogFactory.getLog( SubTasklet_MCBuilder.class );
	private static Random r = new Random();
	private ObjectFactory taskHandlingobjectFactory = new ObjectFactory();
	
	/**
	 * M�gliche Antworten zu MC-Aufgaben hinzuf�gen.
	 * @param newMcSubTask
	 * @param mcSubTaskDef
	 */
	void constructAnswersForMCSubTask( ComplexTaskHandlingType.TryType.PageType.McSubTask newMcSubTask,
			McSubTaskDefType mcSubTaskDef) throws JAXBException{

		List correctAnswers = mcSubTaskDef.getCorrect();
		List incorrectAnswers = mcSubTaskDef.getIncorrect();
				
		// sicherheitshalber pr�fen, d�rfte aber schon durch XML-Schema ausgeschlossen sein
		if( correctAnswers.size() == 0 || mcSubTaskDef.getDisplayedAnswers() == 0 ){
			log.warn( "Aufgabe " + mcSubTaskDef.getId() + " wird keine Antworten enthalten!" );
			return;
		}

		int numOfCorrectAnswers;
		// nach Kategorie die Anzahl der richtigen Antworten festlegen
		if( mcSubTaskDef.getCategory().equals( SubTasklet_MC.CAT_SINGLESELECT ) ){
			numOfCorrectAnswers = 1;
		}else{
			
			int wantedMinCorrect = mcSubTaskDef.isSetMinCorrectAnswers() ?
										mcSubTaskDef.getMinCorrectAnswers() : 1;
			
			// mindestens so viele richtige Antworten
			// das kommt daher, dass zu wenig falsche Fragen da sein k�nnten, um die gew�nschte
			// Anzahl angezeigter Antworten zu erzeugen
			int minCorr = Math.max( mcSubTaskDef.getDisplayedAnswers() - incorrectAnswers.size(), wantedMinCorrect );
			// und sicherheitshalber beschr�nken
			minCorr = Math.min( minCorr, correctAnswers.size() );
			minCorr = Math.min( minCorr, mcSubTaskDef.getDisplayedAnswers() );
			
			// maximal so viele richtige Antworten
			int maxCorr = Math.min( correctAnswers.size(), mcSubTaskDef.getDisplayedAnswers() );
			if( mcSubTaskDef.isSetMaxCorrectAnswers() )
				maxCorr = Math.min( maxCorr, mcSubTaskDef.getMaxCorrectAnswers() );
			
			// TODO Algorithmus verifizieren
			if( maxCorr < minCorr )
				maxCorr = minCorr;
			
			// ok, jetzt zuf�llig mit den berechneten Grenzen festlegen
			numOfCorrectAnswers = r.nextInt( maxCorr - minCorr + 1 ) + minCorr;
			
		}


		// Anzahl der insg. anzuzeigenden Antworten
		int numOfAnswers = Math.min( numOfCorrectAnswers + incorrectAnswers.size() , mcSubTaskDef.getDisplayedAnswers() );		

		// Array der ausgew�hlten Antworten		
		ComplexTaskHandlingType.TryType.PageType.McSubTaskType.AnswerType[] toInsert =
			new ComplexTaskHandlingType.TryType.PageType.McSubTaskType.AnswerType[ numOfAnswers ];
		
		// Auswahlreihenfolge der Antworten als zuf�llige Permutation
		int[] correctAnswersOrder = RandomUtil.getPermutation( correctAnswers.size() );
		int[] incorrectAnswersOrder = RandomUtil.getPermutation( incorrectAnswers.size() );
		
		if( mcSubTaskDef.isPreserveOrderOfAnswers() ){
			int[] tmpOrder = RandomUtil.getPermutation( correctAnswers.size() );
			correctAnswersOrder = new int[ numOfCorrectAnswers ];
			System.arraycopy( tmpOrder, 0, correctAnswersOrder, 0, numOfCorrectAnswers );
			Arrays.sort( correctAnswersOrder );
			
			tmpOrder = RandomUtil.getPermutation( incorrectAnswers.size() );
			incorrectAnswersOrder = new int[ numOfAnswers - numOfCorrectAnswers ];
			System.arraycopy( tmpOrder, 0, incorrectAnswersOrder, 0, numOfAnswers - numOfCorrectAnswers );
			Arrays.sort( incorrectAnswersOrder );
		}
		
		int[] insertOrder = RandomUtil.getPermutation( toInsert.length );
		if( mcSubTaskDef.isPreserveOrderOfAnswers() ){
			Arrays.sort( insertOrder );
		}
			
		int insertIndex = 0;
		
		// korrekte einf�gen
		for( int i=0; i<numOfCorrectAnswers; i++)
			toInsert[ insertOrder[ insertIndex++ ] ] = createAnswerType( 
					(McSubTaskDefType.CorrectType)
							correctAnswers.get( correctAnswersOrder[i] ) );

		// falsche einf�gen
		for( int i=0; i < ( numOfAnswers - numOfCorrectAnswers ); i++ )
			toInsert[ insertOrder[ insertIndex++ ] ] = createAnswerType(
					(McSubTaskDefType.IncorrectType)
							incorrectAnswers.get( incorrectAnswersOrder[i] ) );
		
		
		// und jetzt die Antworten in XML entspr. einf�gen
		List answers = newMcSubTask.getAnswer();
		for( int i=0; i<toInsert.length; i++)
			answers.add( toInsert[i] );
		
	}
	
	void constructPreviewAnswersForMCSubTask( ComplexTaskHandlingType.TryType.PageType.McSubTask newMcSubTask,
			McSubTaskDefType mcSubTaskDef) throws JAXBException{
		
		List<CorrectType> correctAnswers = (List<CorrectType>)mcSubTaskDef.getCorrect();
		List<IncorrectType> incorrectAnswers = (List<IncorrectType>)mcSubTaskDef.getIncorrect();
		
		// sicherheitshalber pr�fen, d�rfte aber schon durch XML-Schema ausgeschlossen sein
		if( correctAnswers.size() == 0 || mcSubTaskDef.getDisplayedAnswers() == 0 ){
			log.warn( "Aufgabe " + mcSubTaskDef.getId() + " enth�lt keine Antworten" );
			return;
		}
		System.out.println( mcSubTaskDef.getId() );
		
		List answers = newMcSubTask.getAnswer();
		for( CorrectType ct : correctAnswers )
			answers.add( createAnswerType( ct ) );
		for( IncorrectType ict : incorrectAnswers )
			answers.add( createAnswerType( ict ) );
		
	}
	
	
	/**
	 * neuen AnswerType aus korrekter Antwort erstellen
	 * @param correct
	 * @return
	 */
	private ComplexTaskHandlingType.TryType.PageType.McSubTaskType.AnswerType createAnswerType(
						McSubTaskDefType.CorrectType correct ) throws JAXBException{
		
		ComplexTaskHandlingType.TryType.PageType.McSubTaskType.AnswerType ret;
		
		ret = taskHandlingobjectFactory.createComplexTaskHandlingTypeTryTypePageTypeMcSubTaskTypeAnswerType();

		ret.setRefId( correct.getId() );
		ret.setSelected( false );
		
		return ret;
		
	}

	/**
	 * neuen AnswerType aus inkorrekten Antworten erstellen
	 * @param incorrect
	 * @return
	 */
	private ComplexTaskHandlingType.TryType.PageType.McSubTaskType.AnswerType createAnswerType(
			McSubTaskDefType.IncorrectType incorrect ) throws JAXBException{

		ComplexTaskHandlingType.TryType.PageType.McSubTaskType.AnswerType ret;
		
		ret = taskHandlingobjectFactory.createComplexTaskHandlingTypeTryTypePageTypeMcSubTaskTypeAnswerType();
		
		ret.setRefId( incorrect.getId() );
		ret.setSelected( false );

		return ret;

	}


}
