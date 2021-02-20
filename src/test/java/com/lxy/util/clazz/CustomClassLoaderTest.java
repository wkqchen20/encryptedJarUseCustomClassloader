package com.lxy.util.clazz;

import org.apache.commons.beanutils.MethodUtils;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;

import static com.lxy.util.clazz.Constants.DECRYPT_FUNC;
import static com.lxy.util.clazz.Constants.FILTER_BASE_PACKAGE;

public class CustomClassLoaderTest {

    // @Test
    public void shouldExecuteReadableJar() throws ClassNotFoundException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        // 将lib下的readable-jar.jar依赖进来
        CustomClassLoader classLoader = new CustomClassLoader(Thread.currentThread().getContextClassLoader(), FILTER_BASE_PACKAGE, DECRYPT_FUNC);
        Class t = classLoader.loadClass("com.lxy.WillEncrypted");
        // should print "hello world"
        MethodUtils.invokeStaticMethod(t, "print", null);
    }

    // @Test
    public void shouldExecuteEncryptJar() throws ClassNotFoundException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        // 将lib下的encrypted-jar.jar依赖进来
        CustomClassLoader classLoader = new CustomClassLoader(Thread.currentThread().getContextClassLoader(), FILTER_BASE_PACKAGE, DECRYPT_FUNC);
        Class t = classLoader.loadClass("com.lxy.WillEncrypted");
        // should print "hello world"
        MethodUtils.invokeStaticMethod(t, "print", null);
    }
}