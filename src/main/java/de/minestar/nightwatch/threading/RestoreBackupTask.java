package de.minestar.nightwatch.threading;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.concurrent.Task;
import de.minestar.nightwatch.util.Counter;
import de.minestar.nightwatch.util.DirectoryRemoveVisitor;
import de.minestar.nightwatch.util.FileCountVisitor;

public class RestoreBackupTask extends Task<Void> {

    private File directory;
    private File backupFile;

    public RestoreBackupTask(File directory, File backupFile) {
        this.directory = directory;
        this.backupFile = backupFile;
    }

    @Override
    protected Void call() throws Exception {

        updateMessage("Counting files");
        int count = FileCountVisitor.count(directory);

        updateMessage("Counting backup size");
        ZipFile zipFile = new ZipFile(backupFile);
        int zipFileLength = zipFile.size();

        long totalWork = count + zipFileLength;
        Counter c = new Counter(1);

        updateProgress(c.get(), totalWork);

        // Delete old content of the server
        updateMessage("Deleting old server content");
        IntegerProperty progress = new SimpleIntegerProperty();
        progress.addListener((observ, oldVal, newVal) -> updateProgress(c.incrementAndGet(), totalWork));
        DirectoryRemoveVisitor.removeContentOf(directory, Optional.of(progress));

        updateMessage("Extracting backup");
        // Restore content of zip file

        for (Enumeration<? extends ZipEntry> iter = zipFile.entries(); iter.hasMoreElements();) {
            ZipEntry zipEntry = iter.nextElement();
            if (zipEntry.isDirectory())
                continue;

            InputStream inputStream = zipFile.getInputStream(zipEntry);
            Path target = Paths.get(directory.getAbsolutePath(), zipEntry.getName());
            // Create sub directories
            target.toFile().getParentFile().mkdirs();

            Files.copy(inputStream, Paths.get(directory.getAbsolutePath(), zipEntry.getName()));
            updateProgress(c.incrementAndGet(), zipFileLength);
        }
        zipFile.close();
        updateMessage("Finished");
        return null;
    }
}
