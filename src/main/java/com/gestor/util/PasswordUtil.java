package com.gestor.util;

import at.favre.lib.crypto.bcrypt.BCrypt;
import java.security.SecureRandom;

public class PasswordUtil {

    private static final int COST = 12;
    private static final String CHARS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%&*";

    public static String hash(String plainPassword) {
        return BCrypt.withDefaults().hashToString(COST, plainPassword.toCharArray());
    }

    public static boolean verify(String plainPassword, String hash) {
        BCrypt.Result result = BCrypt.verifyer().verify(plainPassword.toCharArray(), hash);
        return result.verified;
    }

    public static String generateSecurePassword(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        sb.append("ABCDEFGHIJKLMNOPQRSTUVWXYZ".charAt(random.nextInt(26)));
        sb.append("abcdefghijklmnopqrstuvwxyz".charAt(random.nextInt(26)));
        sb.append("0123456789".charAt(random.nextInt(10)));
        sb.append("!@#$%&*".charAt(random.nextInt(7)));
        for (int i = 4; i < length; i++) {
            sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        char[] arr = sb.toString().toCharArray();
        for (int i = arr.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char tmp = arr[i]; arr[i] = arr[j]; arr[j] = tmp;
        }
        return new String(arr);
    }

    public static String generateResetToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        StringBuilder hex = new StringBuilder();
        for (byte b : bytes) hex.append(String.format("%02x", b));
        return hex.toString();
    }

    public static boolean isStrong(String password) {
        if (password == null || password.length() < 8) return false;
        boolean upper = false, lower = false, digit = false, special = false;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) upper = true;
            else if (Character.isLowerCase(c)) lower = true;
            else if (Character.isDigit(c)) digit = true;
            else special = true;
        }
        return upper && lower && digit && special;
    }
}
