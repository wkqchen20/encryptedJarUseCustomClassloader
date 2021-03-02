package com.lxy.util.clazz;

import io.vavr.control.Try;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static com.lxy.util.clazz.Constants.ENCRYPT_FUNC;
import static com.lxy.util.clazz.Constants.PACKAGE_FILTER;
import static org.junit.Assert.assertTrue;

public class ReWriterTest {

    String baseDir = System.getProperty("user.dir");
    String libPath = baseDir + File.separator + "lib";
    File targetFile = new File(libPath, "encrypted-jar.jar");

    @Before
    public void setup() {
        Try.run(() -> FileUtils.forceDelete(targetFile));
    }

    @Test
    public void shouldGenerateEncryptedJar() throws IOException {
        Jar jar = new Jar(new File(libPath, "readable-jar.jar"));
        Map<String, Jar.Tuple> classMap = jar.getClasses();
        ReWriter.encrypt(classMap, PACKAGE_FILTER, ENCRYPT_FUNC);
        Jar.write(classMap, targetFile);
        assertTrue(targetFile.exists());
    }
}