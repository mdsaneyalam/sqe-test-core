package com.softech.test.core.report;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.softech.test.core.util.Logger;

public class FileZipper {

	/**********************************************************************************************
	 * Creates a compressed zip file from a list of files. Helpful for a variety of
	 * reporting purposes.
	 * 
	 * @param zipFile
	 *            - {@link File} - The initial File entity to be zipped.
	 * @param files
	 *            - {@link List<File>} - List of all File objects to be added to the
	 *            zip file.
	 * @author Brandon Clark created February 8, 2016
	 * @version 1.0 February 8, 2016
	 ***********************************************************************************************/
	public void createZipFile(final File zipFile, final List<File> files) throws IOException {
		// create a temp file
		File tempFile = File.createTempFile(zipFile.getName(), null);

		// delete the temp file (necessary)
		tempFile.delete();

		boolean rename = zipFile.renameTo(tempFile);
		if (!rename) {
			throw new RuntimeException(
					"Failed to rename the file " + zipFile.getAbsolutePath() + " to " + tempFile.getAbsolutePath());
		}
		byte[] buf = new byte[1024];

		ZipInputStream zin = new ZipInputStream(new FileInputStream(tempFile));
		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile));

		ZipEntry entry = zin.getNextEntry();
		while (entry != null) {
			String name = entry.getName();
			boolean notInFiles = true;
			for (File f : files) {
				if (f.getName().equals(name)) {
					notInFiles = false;
					break;
				}
			}
			if (notInFiles) {
				// add ZIP entry to output stream
				out.putNextEntry(new ZipEntry(name));

				// transfer bytes from the ZIP file to the output file
				int len;
				while ((len = zin.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
			}
			entry = zin.getNextEntry();
		}

		// close the streams
		zin.close();

		// compress the files
		for (int i = 0; i < files.size(); i++) {
			if (files.get(i).exists()) {
				InputStream in = new FileInputStream(files.get(i));

				// add ZIP entry to output stream
				out.setLevel(9);
				out.putNextEntry(new ZipEntry(files.get(i).getName()));

				// transfer bytes from the file to the ZIP file
				int len;
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}

				// complete the entry
				out.closeEntry();
				in.close();
			} else {
				Logger.logConsoleMessage("Could not find file to zip titled '" + files.get(i).getPath());
			}
		}

		// complete the ZIP file
		out.close();
		tempFile.delete();
	}

	public void unzipZipFile(String zipFileLoc, String zipDirOutputLoc) {
		try {
			File destDir = new File(zipDirOutputLoc);
			if (!destDir.exists()) {
				destDir.mkdir();
			}
			ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFileLoc));
			ZipEntry entry = zipIn.getNextEntry();
			// iterates over entries in the zip file
			while (entry != null) {
				String filePath = zipDirOutputLoc + File.separator + entry.getName();
				if (!entry.isDirectory()) {
					// if the entry is a file, extract it
					BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
					byte[] bytesIn = new byte[4096];
					int read = 0;
					while ((read = zipIn.read(bytesIn)) != -1) {
						bos.write(bytesIn, 0, read);
					}
					bos.close();
				} else {
					// if the entry is a directory, make the directory
					File dir = new File(filePath);
					dir.mkdir();
				}
				zipIn.closeEntry();
				entry = zipIn.getNextEntry();
			}
			zipIn.close();
		} catch (Exception e) {
			Logger.logConsoleMessage("Failed to unzip zip file '" + zipFileLoc + "'");
			e.printStackTrace();
		}
	}

	public static String zipDirectory(String directoryToZip) throws Exception {
		File zipDir = new File(directoryToZip);
		List<File> fileList = new ArrayList<File>();
		fileList = getAllFiles(zipDir, fileList);
		writeZipFile(zipDir, fileList);
		
		return zipDir.getAbsolutePath() + ".zip";
	}
	
	private static List<File> getAllFiles(File dir, List<File> fileList) {
		try {
			File[] files = dir.listFiles();
			for (File file : files) {
				fileList.add(file);
				if (file.isDirectory()) {
					getAllFiles(file, fileList);
				} else {
					file.getCanonicalPath();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return fileList;
	}

	private static void writeZipFile(File directoryToZip, List<File> fileList) {

		try {
			File zipFile = new File(directoryToZip.getAbsolutePath() + ".zip");
			zipFile.createNewFile();
			FileOutputStream fos = new FileOutputStream(zipFile);
			ZipOutputStream zos = new ZipOutputStream(fos);

			for (File file : fileList) {
				if (!file.isDirectory()) { // we only zip files, not directories
					addToZip(directoryToZip, file, zos);
				}
			}

			zos.close();
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void addToZip(File directoryToZip, File file, ZipOutputStream zos) throws FileNotFoundException,
			IOException {

		FileInputStream fis = new FileInputStream(file);

		String zipFilePath = file.getCanonicalPath().substring(directoryToZip.getCanonicalPath().length() + 1,
				file.getCanonicalPath().length());
		ZipEntry zipEntry = new ZipEntry(zipFilePath);
		zos.setLevel(9);
		zos.putNextEntry(zipEntry);

		byte[] bytes = new byte[1024];
		int length;
		while ((length = fis.read(bytes)) >= 0) {
			zos.write(bytes, 0, length);
		}

		zos.closeEntry();
		fis.close();
	}
}