package de.minestar.nightwatch.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javafx.beans.property.IntegerProperty;

public class ZipFileVisitor extends SimpleFileVisitor<Path> {

	private static final Charset UTF_8 = Charset.forName("UTF8");

	/**
	 * Creates a ZIP file with default compression of the directory. The ZIP
	 * file is written to targetFiles location.
	 * 
	 * @param directory
	 *            The directory to ZIP. It's crawled recursively
	 * @param targetFile
	 *            The file where the ZIP is created
	 * @throws IOException
	 *             When an error occurs while creating the ZIP
	 */
	public static void zipDir(File directory, File targetFile)
			throws IOException {
		zipDirWithProgress(directory, targetFile, null);
	}

	/**
	 * Creates a ZIP file with default compression of the directory. The ZIP
	 * file is written to targetFiles location. Add ability to watch the
	 * progress
	 * 
	 * @param directory
	 *            The directory to ZIP. It's crawled recursively
	 * @param targetFile
	 *            The file where the ZIP is created
	 * @param counter
	 *            An integer property where you can add an listener to watch to
	 *            progress. It will store the proceeded number of files.
	 * @throws IOException
	 *             When an error occurs while creating the ZIP
	 */
	public static void zipDirWithProgress(File directory, File targetFile,
			IntegerProperty counter) throws IOException {
		ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(
				new FileOutputStream(targetFile)), UTF_8);
		ZipFileVisitor visitor = new ZipFileVisitor(zos, counter);
		Files.walkFileTree(directory.toPath(), visitor);
		zos.close();
	}

	private ZipOutputStream zipStream;
	private int progress = 0;
	private Optional<IntegerProperty> fileCounter;

	private ZipFileVisitor(ZipOutputStream zipStream,
			IntegerProperty informedCounter) {
		this.zipStream = zipStream;
		this.fileCounter = Optional.ofNullable(informedCounter);
	}

	@Override
	public FileVisitResult visitFile(final Path file,
			final BasicFileAttributes attrs) throws IOException {
		ZipEntry ze = new ZipEntry(file.toString());
		zipStream.putNextEntry(ze);
		Files.copy(file, zipStream);
		fileCounter.ifPresent(e -> e.set(++progress));
		return FileVisitResult.CONTINUE;
	}

}
