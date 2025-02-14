package org.side.mjm.config.rsa;

import org.side.mjm.common.exception.code.CommonErrorCode;
import org.side.mjm.common.exception.custom.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Configuration
public class RsaProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(RsaProvider.class);
    private final String algorithm;
    private final int keySize;
    private final String privateKey;
    private final String publicKey;

    public RsaProvider(@Value("${rsa.algorithm}") String algorithm, @Value("${rsa.key-size}") int keySize,
                       @Value("${rsa.private}") String privateKey, @Value("${rsa.public}") String publicKey)
            throws NoSuchAlgorithmException {
        this.algorithm = algorithm;
        this.keySize = keySize;
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        if (!StringUtils.hasText(privateKey) || !StringUtils.hasText(publicKey)) {
            LOGGER.error("RSA public or private key does not exist. create new key. ▼");
            createKeyFile();
        }
    }

    private void createKeyFile() throws NoSuchAlgorithmException {
        SecureRandom secureRandom = new SecureRandom();
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(this.algorithm);
        keyPairGenerator.initialize(this.keySize, secureRandom);
        KeyPair keyPair = keyPairGenerator.genKeyPair();
        LOGGER.info("=================================RSA KEY=================================");
        LOGGER.info("private : {}", Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded()));
        LOGGER.info("public : {}", Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()));
    }

    public PrivateKey getPrivateKey() {
        try {
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(this.privateKey));
            KeyFactory keyFactory = KeyFactory.getInstance(this.algorithm);
            return keyFactory.generatePrivate(spec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new ServiceException(CommonErrorCode.SERVICE_ERROR, e);
        }
    }

    public PublicKey getPublicKey() {
        try {
            X509EncodedKeySpec spec = new X509EncodedKeySpec(Base64.getDecoder().decode(this.publicKey));
            KeyFactory keyFactory = KeyFactory.getInstance(this.algorithm);
            return keyFactory.generatePublic(spec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | NullPointerException e) {
            throw new ServiceException(CommonErrorCode.SERVICE_ERROR, e);
        }
    }

    public String decrypt(String encrypted) {
        PrivateKey privateKey = getPrivateKey();
        try {
            byte[] byteEncrypted = Base64.getDecoder().decode(encrypted.getBytes());
            Cipher cipher = Cipher.getInstance(this.algorithm);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] bytePlain = cipher.doFinal(byteEncrypted);
            return new String(bytePlain, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException | NoSuchAlgorithmException | NoSuchPaddingException
                 | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            throw new ServiceException(CommonErrorCode.INVALID_PARAMETER, e);
        }
    }

    public String encrypt(String plainText) {
        PublicKey publicKey = getPublicKey();
        try {
            Cipher cipher = Cipher.getInstance(this.algorithm);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] bytePlain = cipher.doFinal(plainText.getBytes());
            return Base64.getEncoder().encodeToString(bytePlain);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException
                 | IllegalBlockSizeException | BadPaddingException | InvalidKeyException e) {
            throw new ServiceException(CommonErrorCode.SERVICE_ERROR, e);
        }
    }
}
