package de.minestar.nightwatch.backup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.zip.ZipFile;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import de.minestar.nightwatch.threading.BackupTask;
import de.minestar.nightwatch.threading.RestoreBackupTask;
import de.minestar.nightwatch.util.DirectoryRemoveVisitor;
import de.minestar.nightwatch.util.FileCountVisitor;
import de.minestar.nightwatch.util.ZipFileVisitor;
import de.saxsys.javafx.test.JfxRunner;

@RunWith(JfxRunner.class)
public class BackupTest {

    @Test
    public void testFileCounter() throws Exception {

        int count = FileCountVisitor.count(new File(""));
        assertTrue(count > 1);
    }

    @Rule
    public TemporaryFolder temponaryFolder = new TemporaryFolder();

    @Test
    public void backupTest() throws Exception {

        File targetFile = temponaryFolder.newFile();
        assertEquals(0L, targetFile.length());

        File tmpDirToBackup = createTempDir("backup");
        assertEquals(3, tmpDirToBackup.listFiles().length);

        IntegerProperty progress = new SimpleIntegerProperty();
        progress.addListener((observ, oldVal, newVal) -> {
            // check if progress increments
            assertTrue((oldVal.intValue() + 1) == newVal.intValue());
        });

        ZipFileVisitor.zipDirWithProgress(tmpDirToBackup, targetFile, progress);
        assertTrue(targetFile.length() > 0L);
        ZipFile zipFile = new ZipFile(targetFile);
        assertEquals(3, zipFile.size());
        zipFile.close();
    }

    @Test
    public void dateFormatTest() throws Exception {
        LocalDateTime testDate = LocalDateTime.of(2014, 9, 5, 23, 8, 49);
        assertEquals("05.09.2014_23.08.49", testDate.format(BackupTask.BACKUP_TIME_FORMAT));
    }

    @Test
    public void deleteServerDirTest() throws Exception {

        File tmpDir = createTempDir("toDelete");
        assertTrue(tmpDir.exists());

        DirectoryRemoveVisitor.remove(tmpDir, Optional.empty());
        assertFalse(tmpDir.exists());

        tmpDir = createTempDir("toDelete");
        assertTrue(tmpDir.exists());

        IntegerProperty counter = new SimpleIntegerProperty();
        counter.addListener((observ, oldVal, newVal) -> {
            // check if progress increments
            assertTrue((oldVal.intValue() + 1) == newVal.intValue());
        });
        DirectoryRemoveVisitor.removeContentOf(tmpDir, Optional.of(counter));
        assertTrue(tmpDir.exists());
        assertEquals(0, tmpDir.list().length);

    }

    @Test
    public void extractBackupTest() throws Exception {
        File tmpDir = createTempDir("toBackupAndExtract");
        assertTrue(tmpDir.exists());

        File target = temponaryFolder.newFile("backup.zip");

        ZipFileVisitor.zipDir(tmpDir, target);
        assertTrue(target.length() > 0L);

        RestoreBackupTask task = new RestoreBackupTask(tmpDir, target);

        task.exceptionProperty().addListener((observ, oldVal, newVal) -> {
            newVal.printStackTrace();
            fail();
        });
        Thread d = new Thread(task);
        d.setDaemon(true);
        d.start();
        d.join();
        assertEquals(3, tmpDir.list().length);

    }

    private File createTempDir(String name) throws Exception {
        File tmpDir = temponaryFolder.newFolder(name);
        Files.copy(new File("pom.xml").toPath(), new File(tmpDir, "pom.xml").toPath());
        Files.copy(new File("README.md").toPath(), new File(tmpDir, "README.md").toPath());
        // Add sub dir to example
        File subDir = new File(tmpDir, "subDir");
        subDir.mkdir();
        Files.copy(new File("README.md").toPath(), new File(subDir, "README.md").toPath());
        return tmpDir;
    }
}
