/*
 * Copyright 2018 Johns Hopkins University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dataconservancy.pass.loader.nihms.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * @author Karen Hanson
 */
public class FileUtilTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private File createdFolder;
    private String testFilePath;

    private static final String DATA_DIR_KEY = "nihmsetl.data.dir";
    private static final String NIHMS_CONFIG_FILEPATH_KEY = "nihmsetl.harvester.configfile";

    @Before
    public void createFolder() throws Exception {
        createdFolder = folder.getRoot();
        testFilePath = createdFolder.getAbsolutePath() + "test.properties";
    }

    @After
    public void clearProps() throws IOException {
        System.clearProperty("filter");
        System.clearProperty(NIHMS_CONFIG_FILEPATH_KEY);
        System.clearProperty(DATA_DIR_KEY);
        if (createdFolder.exists()) {
            folder.delete();
        }
        assertFalse(createdFolder.exists());
    }

    /**
     * Confirms config directory works if path is a system property
     *
     * @throws IOException
     */
    @Test
    public void testSelectConfigDirectoryFromSystemProperty() throws IOException {
        System.setProperty(NIHMS_CONFIG_FILEPATH_KEY, testFilePath);
        File file = FileUtil.getConfigFilePath(NIHMS_CONFIG_FILEPATH_KEY, "nihmsetl.config");
        assertEquals(testFilePath, file.toString());
    }

    /**
     * Confirms config directory works if use default
     *
     * @throws IOException
     */
    @Test
    public void testSelectConfigDirectoryDefault() throws IOException {
        File file = FileUtil.getConfigFilePath(NIHMS_CONFIG_FILEPATH_KEY, "nihmsetl.config");
        assertEquals("nihmsetl.config", file.getName());
    }

    /**
     * Confirms select directory picks correctly from props list
     *
     * @throws IOException
     */
    @Test
    public void testSelectDirectoryFromProperties() throws IOException {
        System.setProperty(DATA_DIR_KEY, createdFolder.getAbsolutePath());
        File file = FileUtil.getDataDirectory();
        assertEquals(createdFolder.getAbsolutePath(), file.toString());
    }

    /**
     * Confirms that getFilePaths will retrieve 3 csv files in target directory
     *
     * @throws IOException
     */
    @Test
    public void testGetFilePaths3csv() throws IOException {
        File file1 = File.createTempFile("file1", ".csv", createdFolder);
        File file2 = File.createTempFile("file2", ".csv", createdFolder);
        File file3 = File.createTempFile("file3", ".csv", createdFolder);
        List<Path> paths = FileUtil.getCsvFilePaths(createdFolder.toPath());
        assertTrue(paths.contains(file1.toPath()));
        assertTrue(paths.contains(file2.toPath()));
        assertTrue(paths.contains(file3.toPath()));
    }

    /**
     * Verifies that getFilePaths ignores .docx, and .done, and gathers only .csv
     *
     * @throws IOException
     */
    @Test
    public void testGetFilePaths3csv1doc() throws IOException {
        File file1 = File.createTempFile("file1", ".csv", createdFolder);
        File.createTempFile("file2", ".csv.done", createdFolder);
        File file2 = File.createTempFile("file3", ".csv", createdFolder);
        File.createTempFile("file4", ".docx", createdFolder);
        List<Path> paths = FileUtil.getCsvFilePaths(createdFolder.toPath());
        assertTrue(paths.contains(file1.toPath()));
        assertTrue(paths.contains(file2.toPath()));
        assertEquals(2, paths.size());
        paths = null;
    }

    /**
     * Verifies that getFilePaths ignores .docx, and .done, and gathers only .csv
     *
     * @throws IOException
     */
    @Test
    public void testGetFilePathsFilterFileName() throws IOException {
        File file1 = File.createTempFile("file1-02-08-2018-", ".csv", createdFolder);
        File.createTempFile("file2-02-08-2018-", ".docx", createdFolder);
        File file2 = File.createTempFile("file3-02-08-2018-", ".csv", createdFolder);
        File.createTempFile("file4-02-09-2018-", ".csv", createdFolder);
        File.createTempFile("file5-02-09-2018-", ".csv", createdFolder);
        System.setProperty("filter", "*02-08-2018*");
        List<Path> paths = FileUtil.getCsvFilePaths(createdFolder.toPath());
        assertTrue(paths.contains(file1.toPath()));
        assertTrue(paths.contains(file2.toPath()));
        assertEquals(2, paths.size());
    }

    /**
     * Confirms that if you rename a file to append ".done", the new file exists, while the old is gone.
     *
     * @throws IOException
     */
    @Test
    public void renameToDone() throws IOException {

        File file1 = File.createTempFile("file1-02-08-2018-", ".csv", createdFolder);
        FileUtil.renameToDone(file1.toPath());
        String newFileName = file1.getAbsolutePath().toString() + ".done";
        Path path = FileSystems.getDefault().getPath(newFileName);
        assertTrue(path.toFile().exists());
        assertFalse(file1.exists());

    }

}
