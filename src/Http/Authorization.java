package Http;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.util.Base64;

public class Authorization {
    private Mac HMAC = null;

    public Authorization(String secretKey) {
        try {
            HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec key = new SecretKeySpec(secretKey.getBytes("UTF-8"), "HmacSHA256");
            HMAC.init(key);
        } catch (Exception e) {
            System.out.print(e);
        }
    }

    private String urlSafeB64 (byte[] bytes) {
        return new String(Base64.getEncoder().encode(bytes))
                .replace("=", "")
                .replace("+", "-")
                .replace("/", "_");
    }

    private String generateSignature (Long expiration) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(expiration);
        byte[] signatureBytes = HMAC.doFinal(buffer.array());
        return urlSafeB64(signatureBytes);
    }
    public String generateToken (int duration) {
        long expiration = System.currentTimeMillis() + (duration * 1000);
        return String.format("%s.%s", expiration, generateSignature(expiration));
    }

    public boolean verifyToken (String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 2)
            return false;
        Long expiration = Long.parseLong(parts[0]);
        if (expiration < System.currentTimeMillis())
            return false;
        String signature = parts[1];
        String verifySignature = generateSignature(expiration);
        return signature.equals(verifySignature);
    }
}
