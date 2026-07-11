package com.resumeor.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class SecretCipher {
    private final SecretKeySpec key;
    private final SecureRandom random = new SecureRandom();

    public SecretCipher(@Value("${app.encryption-secret:resumeor-local-encryption-secret-change-in-production}") String secret) {
        try {
            key = new SecretKeySpec(MessageDigest.getInstance("SHA-256").digest(secret.getBytes(StandardCharsets.UTF_8)), "AES");
        } catch (Exception exception) {
            throw new IllegalStateException("无法初始化密钥加密服务", exception);
        }
    }

    public String encrypt(String plainText) {
        try {
            byte[] iv = new byte[12];
            random.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, iv));
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(iv) + "." + Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception exception) {
            throw new IllegalStateException("模型密钥加密失败", exception);
        }
    }

    public String decrypt(String cipherText) {
        try {
            String[] parts = cipherText.split("\\.", 2);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, Base64.getDecoder().decode(parts[0])));
            return new String(cipher.doFinal(Base64.getDecoder().decode(parts[1])), StandardCharsets.UTF_8);
        } catch (Exception exception) {
            throw new IllegalStateException("模型密钥解密失败", exception);
        }
    }
}
