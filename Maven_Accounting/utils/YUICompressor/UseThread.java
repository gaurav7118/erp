/*
 * Copyright (C) 2012  Krawler Information Systems Pvt Ltd
 * All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package jyuicompressor;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.Thread;
import javax.print.DocFlavor.STRING;

/**
 * 
 * @author pm
 */
public class UseThread extends Thread {
	public int jscount = 0;

	/** Creates a new instance of UseThread */
	public void run() {
		File sourcedir = new File("E:\\javaproj\\wtfesp\\web\\");
		File sourcedir1 = new File("E:\\javaproj\\wtfesp\\web\\scripts\\");
		File sourcedir2 = new File("E:\\javaproj\\wtfesp\\web\\lib\\");
		CompressFiles(sourcedir);
		System.out.println("Number of files processed: " + jscount);
	}

	private void CompressFiles(File sourcedir) {
		if (sourcedir.exists()) {
			File[] files = sourcedir.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory())
					CompressFiles(files[i]);
				else {
					String type = "";
					if (files[i].getName().endsWith(".js"))
						type = ".js";
					else if (files[i].getName().endsWith(".css"))
						type = ".css";
					if (type != "") {
						File tempfile = new File(jscount + "temp" + type);
						try {
							copyFile(files[i], tempfile);
							String cmd = "java  -jar yuicompressor-2.2.5.jar --charset UTF-8 -o "
									+ files[i].getAbsolutePath()
									+ " "
									+ tempfile.getAbsolutePath() + " 2>&1";
							// java -jar yuicompressor-x.y.z.jar myfile.js -o
							// myfile-min.js
							Process p = Runtime.getRuntime().exec(cmd, null);
							InputStream es = p.getErrorStream();
							// tempfile.delete();
							System.out.println("Executing: " + cmd);
							System.out.println("FILE" + jscount + ": "
									+ files[i].getAbsolutePath());
							byte[] buf = new byte[4096];
							int len;
							while ((len = es.read(buf)) != -1) {
								System.out.println(new String(buf));
							}
							es.close();
							sleep(50);
						} catch (Exception ex) {
							// ex.printStackTrace();
							System.out.println("FILE(failed to process): "
									+ files[i].getAbsolutePath());
						}
						jscount++;
					}
				}
			}
		} else
			System.out.println("No directory present with this name");
	}

	public void copyFile(File in, File out) throws Exception {
		FileInputStream fis = new FileInputStream(in);
		FileOutputStream fos = new FileOutputStream(out);
		byte[] buf = new byte[1024];
		int i = 0;
		while ((i = fis.read(buf)) != -1) {
			fos.write(buf, 0, i);
		}
		fis.close();
		fos.close();
	}
}
