/*

Copyright (C) 2006 Steffen Dienst

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
package junittask.activator;

import junittask.subtasklet.JUnitAddOnSubTaskletFactoryImpl;
import junittask.subtasklet.view.JUnitSubTaskViewFactory;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import de.thorstenberger.taskmodel.complex.addon.AddOnSubTaskletFactory;
import de.thorstenberger.taskmodel.view.AddonSubTaskViewFactory;

public class Activator implements BundleActivator{

	public void start(BundleContext context) throws Exception {
		context.registerService(
				AddOnSubTaskletFactory.class.getName(), 
				new JUnitAddOnSubTaskletFactoryImpl(),
				null);
		
		context.registerService(
				AddonSubTaskViewFactory.class.getName(), 
				new JUnitSubTaskViewFactory(),
				null);
		
	}

	public void stop(BundleContext context) throws Exception {
	}

}
