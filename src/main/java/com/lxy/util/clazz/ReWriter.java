package com.lxy.util.clazz;

import io.vavr.control.Try;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

class ReWriter {

    private ReWriter() {
    }

    static void encrypt(Map<String, Tuple> data, Predicate<String> clazzFilter, Function<byte[], byte[]> encryptionFunc) {
        data.entrySet().stream()
                .filter(entry -> entry.getValue().isClass)
                .filter(entry -> clazzFilter.test(entry.getKey()))
                .forEach(entry -> {
                    byte[] encryptedData = encryptionFunc.apply(entry.getValue().data);
                    entry.setValue(new Tuple(true, encryptedData));
                });
    }

    static void write(Map<String, Tuple> data, File target) throws IOException {
        // Create jar output stream
        JarOutputStream out = new JarOutputStream(new FileOutputStream(target));
        // For each entry in the map, save the bytes
        for (String key : data.keySet()) {
            out.putNextEntry(new ZipEntry(key));
            out.write(data.get(key).data);
            out.closeEntry();
        }
        out.close();
    }

    static Map<String, Tuple> loadClasses(File jarFile) throws IOException {
        Map<String, Tuple> data = new HashMap<>();
        try (JarFile jar = new JarFile(jarFile)) {
            jar.stream().forEach(entry -> readJar(jar, entry, data));
        }
        return data;
    }

    private static void readJar(JarFile jar, JarEntry entry, Map<String, Tuple> classes) {
        String name = entry.getName();
        Try.run(() -> {
            try (InputStream jis = jar.getInputStream(entry)) {
                byte[] bytes = IOUtils.toByteArray(jis);
                if (name.endsWith(".class")) {
                    if (!Clazz.isClass(bytes)) {
                        classes.put(name, new Tuple(false, bytes));
                    } else {
                        classes.put(name, new Tuple(true, bytes));
                    }
                } else {
                    classes.put(name, new Tuple(false, bytes));
                }
            }
        }).onFailure(ex -> {
            throw new IllegalStateException(ex);
        });
    }

    static class Tuple {
        private boolean isClass;
        private byte[] data;

        public Tuple(boolean isClass, byte[] data) {
            this.isClass = isClass;
            this.data = data;
        }
    }

}
