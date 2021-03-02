package com.lxy.util.clazz;

import io.vavr.control.Try;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

/**
 * @author liuxy
 * @date 2021-03-02
 */
public class Jar {

    private final File jarFile;

    public Jar(String jarFilePath) {
        this(new File(jarFilePath));
    }

    public Jar(File jarFile) {
        this.jarFile = jarFile;
    }

    public Map<String, Tuple> getClasses() throws IOException {
        Map<String, Tuple> data = new HashMap<>();
        try (JarFile jar = new JarFile(jarFile)) {
            jar.stream().forEach(entry -> readJar(jar, entry, data));
        }
        return data;
    }

    private void readJar(JarFile jar, JarEntry entry, Map<String, Tuple> classes) {
        String name = entry.getName();
        Try.run(() -> {
            try (InputStream jis = jar.getInputStream(entry)) {
                byte[] bytes = IOUtils.toByteArray(jis);
                if (isClass(name, bytes)) {
                    classes.put(name, Tuple.classOf(bytes));
                } else {
                    classes.put(name, Tuple.otherOf(bytes));
                }
            }
        }).onFailure(ex -> {
            throw new IllegalStateException(ex);
        });
    }

    private boolean isClass(String name, byte[] bytes) {
        if (!name.endsWith(".class")) {
            return false;
        }
        return Clazz.isClass(bytes);
    }

    public static void write(Map<String, Tuple> data, File target) throws IOException {
        // Create jar output stream
        try (JarOutputStream out = new JarOutputStream(new FileOutputStream(target))) {
            // For each entry in the map, save the bytes
            for (String key : data.keySet()) {
                out.putNextEntry(new ZipEntry(key));
                out.write(data.get(key).data);
                out.closeEntry();
            }
        }
    }

    public static class Tuple {
        private final boolean isClass;
        private final byte[] data;

        public Tuple(boolean isClass, byte[] data) {
            this.isClass = isClass;
            this.data = data;
        }

        public static Tuple classOf(byte[] data) {
            return new Tuple(true, data);
        }

        public static Tuple otherOf(byte[] data) {
            return new Tuple(false, data);
        }

        public boolean isClass() {
            return isClass;
        }

        public byte[] data() {
            return data;
        }
    }
}
