/*******************************************************************************
 * Copyright (c) 2012 Udayan Kumar.
 * All rights reserved. 
 * 
 * This file is part of iTrust application for android.
 * iTrust is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version. 
 * 
 * iTrust is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with iTrust.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Udayan Kumar - initial API and implementation
 ******************************************************************************/
package uf.edu;

import org.acra.ACRA;
import org.acra.annotation.*;

import android.app.Application;

//this class is added to enable automatic crash reports being sent to google docs. 
@ReportsCrashes(formKey = "dGN5SF8yV3F3dm5rLWc3ZGpLMlVnRGc6MQ") 
public class iTrustApplication extends Application {
    @Override
    public void onCreate() {
        // The following line triggers the initialization of ACRA
        ACRA.init(this);
        super.onCreate();
    }


}
