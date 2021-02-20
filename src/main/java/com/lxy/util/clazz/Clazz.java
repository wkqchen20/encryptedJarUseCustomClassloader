package com.lxy.util.clazz;

class Clazz {

    private Clazz() {
    }

    static boolean isClass(byte[] bytes) {
        String cafebabe = String.format("%02X%02X%02X%02X", bytes[0], bytes[1], bytes[2], bytes[3]);
        return cafebabe.toLowerCase().equals("cafebabe");
    }

}
