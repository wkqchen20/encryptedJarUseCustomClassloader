package com.lxy.util.clazz;

import io.vavr.control.Try;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.function.Function;
import java.util.function.Predicate;

public class Constants {

    private Constants() {
    }

    public static final String FILTER_BASE_PACKAGE = "com.lxy";

    public static final Predicate<String> PACKAGE_FILTER = name -> name.replace("/", ".").startsWith(FILTER_BASE_PACKAGE);

    public static final Function<byte[], byte[]> ENCRYPT_FUNC = bytes -> encrypt(bytes, "test");
    public static final Function<byte[], byte[]> DECRYPT_FUNC = bytes -> decrypt(bytes, "test");

    private static final String ENCRYPT_ALGORITHM = "AES";
    private static final String CIPHER_MODE = "AES/CBC/NOPADDING";
    private static final int BLOCK_SIZE = 16;
    private static final byte[] IV = "AAAAAAHhWwBBBBBB".getBytes();

    public static byte[] encrypt(byte[] inputs, String key) {
        return Try.of(() -> {
            SecretKeySpec skeySpec = new SecretKeySpec(getKey(key), ENCRYPT_ALGORITHM);
            Cipher cipher = Cipher.getInstance(CIPHER_MODE);
            IvParameterSpec iv = new IvParameterSpec(IV);
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
            return cipher.doFinal(addPaddingBytes(inputs));
        }).getOrElseThrow(t -> {
            throw new IllegalStateException(t);
        });
    }

    public static byte[] decrypt(byte[] inputs, String key) {
        return Try.of(() -> {
            SecretKeySpec skeySpec = new SecretKeySpec(getKey(key), ENCRYPT_ALGORITHM);
            Cipher cipher = Cipher.getInstance(CIPHER_MODE);
            IvParameterSpec iv = new IvParameterSpec(IV);
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            byte[] bytes = cipher.doFinal(inputs);
            return removePaddingBytes(bytes);
        }).getOrElseThrow(t -> {
            throw new IllegalStateException(t);
        });
    }

    /**
     * nopadding的类型需要数据长度为16的倍数，需追加占位符
     *
     * @param bytes
     * @return
     * @throws Exception
     */
    private static byte[] addPaddingBytes(byte[] bytes) throws IOException {
        byte[] lenBytes = int2Bytes(bytes.length);
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        byteStream.write(lenBytes);
        byteStream.write(bytes);
        int size = byteStream.size();
        if (size % BLOCK_SIZE != 0) {
            int padLen = BLOCK_SIZE - (size % BLOCK_SIZE);
            byteStream.write(new byte[padLen]);
        }
        return byteStream.toByteArray();
    }

    private static byte[] removePaddingBytes(byte[] bytes) {
        byte[] lenBytes = Arrays.copyOfRange(bytes, 0, 4);
        int length = bytes2int(lenBytes);
        // 这种情况
        if (length > (bytes.length - 4)) {
            throw new IllegalArgumentException("解密失败");
        }
        return Arrays.copyOfRange(bytes, 4, 4 + length);
    }

    private static byte[] getKey(String key) {
        byte[] bytes = key.getBytes(StandardCharsets.UTF_8);
        int length = bytes.length;
        if (length == BLOCK_SIZE) {
            return bytes;
        } else if (length < BLOCK_SIZE) {
            return Arrays.copyOf(bytes, BLOCK_SIZE);
        } else {
            return Arrays.copyOfRange(bytes, 0, BLOCK_SIZE);
        }
    }

    /**
     * https://github.com/open-dingtalk/openapi-demo-java/blob/443e4a9fbd9c6ffda2e2253d82eee1f9380b0610/src/main/java/com/alibaba/dingtalk/openapi/demo/utils/aes/Utils.java#L30
     *
     * @param count
     * @return
     */
    private static byte[] int2Bytes(int count) {
        byte[] byteArr = new byte[4];
        byteArr[3] = (byte) (count & 0xFF);
        byteArr[2] = (byte) (count >> 8 & 0xFF);
        byteArr[1] = (byte) (count >> 16 & 0xFF);
        byteArr[0] = (byte) (count >> 24 & 0xFF);
        return byteArr;
    }

    /**
     * https://github.com/open-dingtalk/openapi-demo-java/blob/443e4a9fbd9c6ffda2e2253d82eee1f9380b0610/src/main/java/com/alibaba/dingtalk/openapi/demo/utils/aes/Utils.java#L45
     *
     * @param byteArr
     * @return
     */
    private static int bytes2int(byte[] byteArr) {
        int count = 0;
        for (int i = 0; i < 4; i++) {
            count <<= 8;
            count |= byteArr[i] & 0xff;
        }
        return count;
    }
}
