package com.example.kvp.traindriver;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Crypto
{
    public static final int SHA256digestSize = 32;

    public static byte[] SHA256pass(byte[] data, int offset, int length, String password)
    {
        try
        {
            byte[] pass = password.getBytes(StandardCharsets.UTF_8);
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(data, offset, length);
            messageDigest.update(pass, 0, pass.length);
            byte[] bytes = messageDigest.digest();
            return bytes;
        } catch (NoSuchAlgorithmException e)
        {
            return null;
        }
    }
}
