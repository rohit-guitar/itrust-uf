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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import android.content.Context;
import android.os.Environment;

public class zip
{
	Context con;
	
	public zip(Context con) {
		this.con = con;
	}

	public void zipFolder(String Filename)
	{
		try
		{	String files[] = con.getResources().getStringArray(R.array.filesToZip);
			ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(Environment.getExternalStorageDirectory()+"/"+ con.getString(R.string.DataPath)+"/" +Filename)));
			BufferedInputStream in = null;
			byte[] data  = new byte[1000];
			for (int i=0; i<files.length; i++)
			{
				try {
				in = new BufferedInputStream(new FileInputStream(Environment.getExternalStorageDirectory()+"/"+ con.getString(R.string.DataPath)+"/" + files[i]), 1000);                 
				out.putNextEntry(new ZipEntry(files[i])); 
				int count;
				while((count = in.read(data,0,1000)) != -1)
				{
					out.write(data, 0, count);
				}
				out.closeEntry();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
			out.flush();
			out.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		} 
	}
  }
