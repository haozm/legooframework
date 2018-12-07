package com.legooframework.model.base.files;

import org.junit.Test;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class RSATest {

    @Test
    public void readPublicKey() throws Exception {
        File key_file = ResourceUtils.getFile(ResourceUtils.FILE_URL_PREFIX + "C:\\Users\\etc\\csosm-pk.cer");
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate) cf.generateCertificate(new FileInputStream(key_file));
        PublicKey publicKey = cert.getPublicKey();
        System.out.println(publicKey);
    }
}
