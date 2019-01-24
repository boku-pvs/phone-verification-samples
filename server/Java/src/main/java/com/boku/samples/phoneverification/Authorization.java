package com.boku.samples.phoneverification;

import com.boku.samples.phoneverification.util.Base64;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Authorization {

    private static final SecureRandom random = new SecureRandom();
    private String encryptionType;
    private String developerId;
    private String aesKey;

    Authorization(String encryptionType, String developerId, String aesKey) {
        this.encryptionType = encryptionType;
        this.developerId = developerId;
        this.aesKey = aesKey;
    }

    public static AuthorizationHeaderBuilder builder() {
        return new AuthorizationHeaderBuilder();
    }

    public static class AuthorizationHeaderBuilder {

        private String encryptionType;
        private String developerId;
        private String aesKey;

        AuthorizationHeaderBuilder() {
        }

        public AuthorizationHeaderBuilder encryptionType(String encryptionType) {
            this.encryptionType = encryptionType;
            return this;
        }

        public AuthorizationHeaderBuilder developerId(String developerId) {
            this.developerId = developerId;
            return this;
        }

        public AuthorizationHeaderBuilder aesKey(String aesKey) {
            this.aesKey = aesKey;
            return this;
        }

        public Authorization build() {
            return new Authorization(encryptionType, developerId, aesKey);
        }

        @Override
        public String toString() {
            return "AuthorizationHeaderBuilder{" + "encryptionType=" + encryptionType + ", developerId=" + developerId + ", aesKey=" + aesKey + '}';
        }
    }

    public String generate() throws Exception {
        System.out.println("DeveloperId: " + developerId + ", AES Key: " + aesKey);

        //setup cipher
        byte[] decodedSecretKey = Base64.decode(aesKey);
        SecretKeySpec secretKey = new SecretKeySpec(decodedSecretKey, "AES");
        byte[] cipherSalt = randomSalt(16);
        IvParameterSpec iv = new IvParameterSpec(cipherSalt);
        Cipher cipher = Cipher.getInstance(encryptionType);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);

        //get and encrypt the payload
        String payload = generatePayload();
        System.out.println("payload=" + payload);
        byte[] encryptedDataBytes = cipher.doFinal(payload.getBytes("UTF-8"));
        String encPayload = Base64.encodeToString(encryptedDataBytes);

		//cipher salt should also be Base64 encoded in the authorization
        String encCipherSalt = Base64.encodeToString(cipherSalt, true);
        System.out.println("developerId=" + developerId + ", encCipherSalt=" + encCipherSalt + ", encPayload=" + encPayload);

        //create and encode the authorization
        String authorization = developerId + ":" + encCipherSalt + ":" + encPayload;
        System.out.println("authorization=" + authorization);
        String encodedAuthorization = Base64.encodeToString(authorization.getBytes("UTF-8"));
        System.out.println("encodedAuthorization=" + encodedAuthorization);
        return encodedAuthorization;
    }

    private String generatePayload() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        String timeStamp = sdf.format(cal.getTime());
        int nonce = 10000 + random.nextInt(90000);
        String payload = timeStamp + nonce;
        return payload;
    }

    private static byte[] randomSalt(int length) {
        byte[] bytes = new byte[length];
        random.nextBytes(bytes);
        return bytes;
    }

    @Override
    public String toString() {
        return "AuthorizationHeader{" + "encryptionType=" + encryptionType + ", developerId=" + developerId + ", aesKey=" + aesKey + '}';
    }
}
