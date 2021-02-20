package com.lxy.util.clazz;


import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Function;

public class CustomClassLoader extends ClassLoader {

    private final String encryptBasePackage;
    private final Function<byte[], byte[]> deEncryptionFunc;

    public CustomClassLoader(ClassLoader parent, String encryptBasePackage, Function<byte[], byte[]> deEncryptionFunc) {
        super(parent);
        this.encryptBasePackage = encryptBasePackage;
        this.deEncryptionFunc = deEncryptionFunc;
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        if (name.startsWith(encryptBasePackage)) {
            try {
                return getClass(name);
            } catch (IOException e) {
                throw new ClassNotFoundException(name, e);
            }
        }
        return super.loadClass(name);
    }

    private Class<?> getClass(String name) throws IOException {
        String file = name.replace('.', '/') + ".class";
        byte[] byteArr = loadClassData(file);
        Class<?> c = defineClass(name, byteArr, 0, byteArr.length);
        resolveClass(c);
        return c;
    }

    private byte[] loadClassData(String name) throws IOException {
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(name)) {
            int length = stream.available();
            byte[] bytes = IOUtils.readFully(stream, length);
            if (Clazz.isClass(bytes)) {
                return bytes;
            }
            return deEncryptionFunc.apply(bytes);
        }
    }

}
