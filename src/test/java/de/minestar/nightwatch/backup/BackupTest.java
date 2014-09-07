package de.minestar.nightwatch.backup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.time.LocalDateTime;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import de.minestar.nightwatch.threading.BackupTask;
import de.minestar.nightwatch.util.FileCountVisitor;
import de.minestar.nightwatch.util.ZipFileVisitor;

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
		IntegerProperty progress = new SimpleIntegerProperty();
		progress.addListener((observ, oldVal, newVal) -> {
			// check if progress increments
			assertTrue((oldVal.intValue() + 1) == newVal.intValue());
		});
		ZipFileVisitor.zipDirWithProgress(new File(""), targetFile, progress);
		assertTrue(targetFile.length() > 0L);
	}

	@Test
	public void dateFormatTest() throws Exception {
		LocalDateTime testDate = LocalDateTime.of(2014, 9, 5, 23, 8, 49);
		assertEquals("05.09.2014_23.08.49",
				testDate.format(BackupTask.BACKUP_TIME_FORMAT));
	}

}
