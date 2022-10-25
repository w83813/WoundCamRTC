package org.itri.woundcamrtc.helper;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ZipUtility {
	//http://www.crazysquirrel.com/computing/java/basics/java-directory-zipping.jspx
	//http://crackren.iteye.com/blog/752732

	//https://blog.xuite.net/programer/1/56532684-%28JAVA%29%E8%A6%81%E5%B0%87%E6%AA%94%E6%A1%88%E5%A3%93%E7%B8%AE%E4%B8%94%E5%8A%A0%E5%AF%86%E7%9A%84%E6%96%B9%E5%BC%8F

	public static final void zipDirectory(File directory, File zip)
			throws IOException {
		System.out.println("in zipDirectory()");

		ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zip));
		zip(directory, directory, zos);
		zos.close();
	}

	private static final void zip(File directory, File base, ZipOutputStream zos)
			throws IOException {
		File[] files = directory.listFiles();
		byte[] buffer = new byte[8192];
		int read = 0;
		for (int i = 0, n = files.length; i < n; i++) {
			if (files[i].isDirectory()) {
				zip(files[i], base, zos);
			} else {
				FileInputStream in = new FileInputStream(files[i]);
				ZipEntry entry = new ZipEntry(files[i].getPath().substring(
						base.getPath().length() + 1));
				zos.putNextEntry(entry);
				while (-1 != (read = in.read(buffer))) {
					zos.write(buffer, 0, read);
				}
				in.close();
			}
		}
	}

	public static final void unzip(File zip, File extractTo) throws IOException {
		ZipFile archive = new ZipFile(zip);
		Enumeration e = archive.entries();
		while (e.hasMoreElements()) {
			ZipEntry entry = (ZipEntry) e.nextElement();
			File file = new File(extractTo, entry.getName());
			if (entry.isDirectory() && !file.exists()) {
				file.mkdirs();
			} else {
				if (!file.getParentFile().exists()) {
					file.getParentFile().mkdirs();
				}

				InputStream in = archive.getInputStream(entry);
				BufferedOutputStream out = new BufferedOutputStream(
						new FileOutputStream(file));

				byte[] buffer = new byte[8192];
				int read;

				while (-1 != (read = in.read(buffer))) {
					out.write(buffer, 0, read);
				}

				in.close();
				out.close();
			}
		}
	}
}