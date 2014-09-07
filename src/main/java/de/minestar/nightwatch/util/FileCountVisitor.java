package de.minestar.nightwatch.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class FileCountVisitor extends SimpleFileVisitor<Path> {

	/**
	 * Counts all files in the directory recursively.
	 * 
	 * @param directory
	 *            The directory to crawl.
	 * @return The amount of total files (without sub directories)
	 * @throws IOException
	 */
	public static int count(File directory) throws IOException {
		FileCountVisitor fileCounter = new FileCountVisitor();

		Files.walkFileTree(directory.toPath(), fileCounter);
		return fileCounter.counter.get();
	}

	private Counter counter;

	private FileCountVisitor() {
		this.counter = new Counter();
	}

	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
			throws IOException {
		counter.increment();
		return super.visitFile(file, attrs);
	}

}
