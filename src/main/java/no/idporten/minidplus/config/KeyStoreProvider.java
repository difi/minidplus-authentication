package no.idporten.minidplus.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

@Component
public class KeyStoreProvider {

    private static Logger logger = LoggerFactory.getLogger(KeyStoreProvider.class);

    private KeyStore keyStore;

    public KeyStoreProvider(JWKConfig jwkConfig, ResourceLoader resourceLoader) {
        try (InputStream is = new FileInputStream(resourceLoader.getResource(jwkConfig.getKeystore().getLocation()).getFile())) {
            KeyStore keyStore = KeyStore.getInstance(jwkConfig.getKeystore().getType());
            keyStore.load(is, jwkConfig.getKeystore().getPassword().toCharArray());
            if (logger.isInfoEnabled()) {
                logger.info("Loaded keystore of type {} from {}", jwkConfig.getKeystore().getType(), jwkConfig.getKeystore().getLocation());
            }
            this.keyStore = keyStore;
        } catch (IOException | KeyStoreException | CertificateException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public KeyStore keyStore() {
        return keyStore;
    }

}
