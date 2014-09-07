package de.minestar.nightwatch.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ConfigurationTest {

	@Rule
	public TemporaryFolder tmpFolder = new TemporaryFolder();

	@Test
	public void test() throws Exception {

		File configFile = tmpFolder.newFile();
		Configuration config = Configuration.create(configFile);
		assertTrue(config.java7Path().isEmpty().get());

		config.java7Path().set("path/to/java/7/binary");
		assertFalse(config.java7Path().isEmpty().get());

		config = Configuration.create(configFile);
		assertFalse(config.java7Path().isEmpty().get());
		assertEquals("path/to/java/7/binary", config.java7Path().get());

	}

}
