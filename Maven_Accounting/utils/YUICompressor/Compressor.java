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

import java.io.*;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.Thread;

/**
 * 
 * @author pm
 */
public class Compressor {

	/**
	 * Creates a new instance of Compressor
	 */
	public Compressor() {

	}

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {
		UseThread testthread = new UseThread();
		testthread.start();
	}

	private static void GetJSFiles(File sourcedir) {
		File[] files = sourcedir.listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory())
				GetJSFiles(files[i]);
			else {
			}
		}
	}
}
