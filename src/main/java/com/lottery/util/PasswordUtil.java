package com.lottery.util;

import org.mindrot.jbcrypt.BCrypt;

public final class PasswordUtil {
    private PasswordUtil() {
    }

    public static String hash(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }

    public static boolean verify(String password, String hash) {
        if (password == null || hash == null) {
            return false;
        }

        if (hash.startsWith("$2")) {
            try {
                return BCrypt.checkpw(password, hash);
            } catch (IllegalArgumentException e) {
                
            }
        }

        if (hash.length() == 64 && hash.matches("^[0-9a-fA-F]+$")) {
            return hashSHA256(password).equalsIgnoreCase(hash);
        }

        return false;
    }

    private static String hashSHA256(String password) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }
}
