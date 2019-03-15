package com.csosm.module.sso;

import com.google.common.base.Charsets;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigInteger;

public class PasswordEncoderImpl implements PasswordEncoder {

    private final static String SEED = "0933324145462219732314329";

    @Override
    public String encode(CharSequence rawPassword) {
        byte[] encryptPassword = DigestUtils.md5(rawPassword.toString().getBytes());
        String base64Password = Base64.encodeBase64String(encryptPassword);
        BigInteger bi_text = new BigInteger(base64Password.getBytes(Charsets.UTF_8));
        BigInteger bi_r0 = new BigInteger(SEED);
        BigInteger bi_r1 = bi_r0.xor(bi_text);
        return bi_r1.toString(16);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        String rawPassword_ps = rawPassword.toString();
        if (rawPassword_ps.length() > 26) {
            return StringUtils.equalsIgnoreCase(rawPassword_ps, encodedPassword);
        }
        String pwd = this.encode(rawPassword);
        return StringUtils.equalsIgnoreCase(pwd, encodedPassword);
    }
}
