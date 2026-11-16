package com.estv.pet_crud_user.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

@Configuration
public class RsaKeyConfig {

    @Bean
    public PrivateKey privateKey(
            @Value("${jwt.private-key-location}") Resource resource
    ) throws Exception {

        byte[] keyBytes;

        try (var inputStream = resource.getInputStream()) {
            keyBytes = inputStream.readAllBytes();
        }

        String pem = new String(keyBytes);

        pem = pem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] decoded = Base64.getDecoder().decode(pem);

        PKCS8EncodedKeySpec keySpec =
                new PKCS8EncodedKeySpec(decoded);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        return keyFactory.generatePrivate(keySpec);
    }
}
