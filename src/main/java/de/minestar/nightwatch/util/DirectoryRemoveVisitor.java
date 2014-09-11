package de.minestar.nightwatch.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Optional;

import javafx.beans.property.IntegerProperty;

public class DirectoryRemoveVisitor extends SimpleFileVisitor<Path> {

    /**
     * Remove a directory and it's content recursively. Also provides
     * possibility to observe the progress by passing by an integer property
     * 
     * @param directory
     *            The directory and its content to remove
     * @param counter
     *            Optional counter to provide observation of the progress
     * @throws IOException
     */
    public static void remove(File directory, Optional<IntegerProperty> counter) throws IOException {
        Files.walkFileTree(directory.toPath(), new DirectoryRemoveVisitor(counter));
    }

    /**
     * Similar to {@link #remove(File, Optional)}, but doesn't delete the
     * directory, only its content.
     * 
     * @param directory
     *            The content of this directory to remove
     * @param counter
     *            Optional counter to provide observation of the progress
     * @throws IOException
     */
    public static void removeContentOf(File directory, Optional<IntegerProperty> counter) throws IOException {
        File[] subFiles = directory.listFiles();
        for (int i = 0; i < subFiles.length; i++) {
            File subFile = subFiles[i];
            remove(subFile, counter);
        }
    }

    private Optional<IntegerProperty> fileCounter;
    private int progressCounter = 0;

    private DirectoryRemoveVisitor(Optional<IntegerProperty> fileCounter) {
        this.fileCounter = fileCounter;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        // Delete all files
        Files.delete(file);
        fileCounter.ifPresent(e -> e.set(++progressCounter));
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        // Delete the directory after all sub files are deleted
        Files.delete(dir);
        return FileVisitResult.CONTINUE;

    }

}
