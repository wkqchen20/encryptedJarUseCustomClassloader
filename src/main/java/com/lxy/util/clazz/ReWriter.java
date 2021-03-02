package com.lxy.util.clazz;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.lxy.util.clazz.Jar.Tuple;

class ReWriter {

    private ReWriter() {
    }

    static void encrypt(Map<String, Tuple> data, Predicate<String> clazzFilter, Function<byte[], byte[]> encryptionFunc) {
        data.entrySet().stream()
                .filter(entry -> entry.getValue().isClass())
                .filter(entry -> clazzFilter.test(entry.getKey()))
                .forEach(entry -> {
                    byte[] encryptedData = encryptionFunc.apply(entry.getValue().data());
                    entry.setValue(Tuple.classOf(encryptedData));
                });
    }


}
