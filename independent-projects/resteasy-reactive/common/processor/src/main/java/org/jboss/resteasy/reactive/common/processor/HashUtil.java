package org.jboss.resteasy.reactive.common.processor;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@SuppressWarnings("ForLoopReplaceableByForEach")
public final class HashUtil {

    private HashUtil() {
    }

    public static String sha1(String value) {
        return sha1(value.getBytes(StandardCharsets.UTF_8));
    }

    public static String sha1(byte[] value) {
        return new String(Base64.getEncoder().encode(value), StandardCharsets.UTF_8);
    }

    public static String sha256(String value) {
        return sha256(value.getBytes(StandardCharsets.UTF_8));
    }

    public static String sha256(byte[] value) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(value);
            StringBuilder sb = new StringBuilder(40);
            for (int i = 0; i < digest.length; ++i) {
                sb.append(Integer.toHexString((digest[i] & 0xFF) | 0x100), 1, 3);
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
