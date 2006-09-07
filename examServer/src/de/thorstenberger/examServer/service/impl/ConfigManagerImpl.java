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
package de.thorstenberger.examServer.service.impl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.Validator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.thorstenberger.examServer.dao.xml.jaxb.Config;
import de.thorstenberger.examServer.dao.xml.jaxb.ObjectFactory;
import de.thorstenberger.examServer.service.ConfigManager;
import de.thorstenberger.examServer.service.ExamServerManager;

/**
 * @author Thorsten Berger
 *
 */
public class ConfigManagerImpl implements ConfigManager {

	private ExamServerManager examServerManager;
	private JAXBContext jc;
	private Config config;
	private File configFile;

	private Log log = LogFactory.getLog( ConfigManagerImpl.class );
	
	/**
	 * 
	 */
	public ConfigManagerImpl( ExamServerManager examServerManager ) {

		this.examServerManager = examServerManager;
	
		try { // JAXBException

			jc = JAXBContext.newInstance( "de.thorstenberger.examServer.dao.xml.jaxb" );

			configFile = new File( examServerManager.getRepositoryFile().getAbsolutePath() + File.separatorChar + ExamServerManager.SYSTEM + File.separatorChar + "config.xml" );

			if( !configFile.exists() ){
				ObjectFactory oF = new ObjectFactory();
				config = oF.createConfig();
				config.setStudentsLoginEnabled( false );
				config.setLoadJVMOnStartup( false );
				save();
				return;			    
			}


			// wenn vorhanden, dann auslesen
			Unmarshaller unmarshaller;
			unmarshaller = jc.createUnmarshaller();
			unmarshaller.setValidating( true );
			BufferedInputStream bis = new BufferedInputStream( new FileInputStream( configFile ) );
			config = (Config) unmarshaller.unmarshal( bis );


		} catch (JAXBException e) {
			throw new RuntimeException( e );
		}catch (IOException e1){
			throw new RuntimeException( e1 );
		}

		
	}
	
	private void save(){
		try {
			
			Marshaller marshaller = jc.createMarshaller();
			Validator validator = jc.createValidator();
			validator.validate( config );
			marshaller.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, new Boolean(true) );
			BufferedOutputStream bos = new BufferedOutputStream( new FileOutputStream( this.configFile ) );
			marshaller.marshal( config, bos );
			
			bos.close();
			
		} catch (JAXBException e) {
			throw new RuntimeException( e );
		} catch( IOException e1 ){
			throw new RuntimeException( e1 );
		}
		
	}


	/* (non-Javadoc)
	 * @see de.thorstenberger.examServer.service.ConfigManager#getTitle()
	 */
	public String getTitle() {
		return config.getTitle();
	}

	/* (non-Javadoc)
	 * @see de.thorstenberger.examServer.service.ConfigManager#isLoadJVMOnStartup()
	 */
	public boolean isLoadJVMOnStartup() {
		return config.isLoadJVMOnStartup();
	}

	/* (non-Javadoc)
	 * @see de.thorstenberger.examServer.service.ConfigManager#setLoadJVMOnStartup(boolean)
	 */
	public void setLoadJVMOnStartup(boolean value) {
		config.setLoadJVMOnStartup( value );
		save();
	}

	/* (non-Javadoc)
	 * @see de.thorstenberger.examServer.service.ConfigManager#setTitle(java.lang.String)
	 */
	public void setTitle(String title) {
		config.setTitle( title );
		save();
	}

	/* (non-Javadoc)
	 * @see de.thorstenberger.examServer.service.ConfigManager#getRemoteUserMangerURL()
	 */
	public String getRemoteUserManagerURL() {
		return config.getRemoteUserManagerURL();
	}

	/* (non-Javadoc)
	 * @see de.thorstenberger.examServer.service.ConfigManager#setRemoteUserManagerURL()
	 */
	public void setRemoteUserManagerURL( String url ) {
		config.setRemoteUserManagerURL( url );
		save();
	}

	/* (non-Javadoc)
	 * @see de.thorstenberger.examServer.service.ConfigManager#isStudentsLoginEnabled()
	 */
	public boolean isStudentsLoginEnabled() {
		return config.isStudentsLoginEnabled();
	}

	/* (non-Javadoc)
	 * @see de.thorstenberger.examServer.service.ConfigManager#setStudentsLoginEnabled(boolean)
	 */
	public void setStudentsLoginEnabled(boolean value) {
		config.setStudentsLoginEnabled( value );
		save();		
	}
	
	

}
