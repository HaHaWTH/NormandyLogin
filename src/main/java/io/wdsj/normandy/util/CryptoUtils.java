package io.wdsj.normandy.util;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class CryptoUtils {
    public static PublicKey stringToPublicKey(String keyStr) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(keyStr);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }

    public static boolean verify(String data, String signatureStr, PublicKey publicKey) throws Exception {
        byte[] signatureBytes = Base64.getDecoder().decode(signatureStr);
        Signature publicSignature = Signature.getInstance("SHA256withRSA");
        publicSignature.initVerify(publicKey);
        publicSignature.update(data.getBytes());
        return publicSignature.verify(signatureBytes);
    }

    private static final SecureRandom secureRandom = new SecureRandom();

    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder().withoutPadding();

    public static String generateChallenge(int numberOfBytes) {
        byte[] randomBytes = new byte[numberOfBytes];

        secureRandom.nextBytes(randomBytes);

        return base64Encoder.encodeToString(randomBytes);
    }
}