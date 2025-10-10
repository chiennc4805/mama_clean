package com.example.demo.util;

import java.security.SecureRandom;

public class TokenUtils {

    public static String genToken(int numBytes) {
        byte[] bytes = new byte[numBytes];
        new SecureRandom().nextBytes(bytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b)); // mã hex
        }
        return sb.toString();
    }
}
