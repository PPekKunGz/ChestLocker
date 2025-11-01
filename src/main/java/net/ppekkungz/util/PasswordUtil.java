package net.ppekkungz.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility สำหรับ hash และตรวจสอบ password ด้วย SHA-256
 * ใช้ได้ทั้งฝั่ง server และ client (hash เท่านั้น)
 */
public final class PasswordUtil {

    private PasswordUtil() {}

    /**
     * แปลง password เป็น SHA-256 hash
     */
    public static String hash(String rawPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            // ถ้าไม่มี algorithm (ซึ่งแทบไม่เกิด)
            e.printStackTrace();
            return rawPassword;
        }
    }

    /**
     * ตรวจสอบว่า password ที่กรอกตรงกับ hash ที่เก็บไว้ไหม
     */
    public static boolean matches(String rawPassword, String hashed) {
        return hash(rawPassword).equals(hashed);
    }
}
