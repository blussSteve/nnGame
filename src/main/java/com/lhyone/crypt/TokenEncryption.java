package com.lhyone.crypt;


import org.apache.commons.codec.digest.DigestUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * Created by Think on 2017/8/27.
 */
public class TokenEncryption {
    private static final String FIELD_SEPARATOR = "~~~";
    private final String[] keys;

    public TokenEncryption(String... keys) {
        this.keys = keys;
    }

    public String encrypt(String unencrypted) throws EncryptException {
        String sig = hex(unencrypted);
        String encrypted = sig + FIELD_SEPARATOR + unencrypted;

        for (int i = 0; i < this.keys.length; i++) {
            encrypted = new DESEncryption(this.keys[i]).encrypt(encrypted);
        }
        try {
            return URLEncoder.encode(encrypted, DESEncryption.UNICODE_FORMAT);
        } catch (UnsupportedEncodingException e) {
            throw new EncryptException(e);
        }
    }

    private String hex(String raw) {
        return DigestUtils.md5Hex(raw);
    }

    public String decrypt(final String encrypted) throws EncryptException {
        if (encrypted == null) {
            throw new NullPointerException("encrypted is null");
        }

        String decrypted;
        try {
            decrypted = URLDecoder.decode(encrypted, DESEncryption.UNICODE_FORMAT);
        } catch (UnsupportedEncodingException e) {
            throw new EncryptException(e);
        }
        for (int i = this.keys.length - 1; i >= 0; i--) {
            decrypted = new DESEncryption(this.keys[i]).decrypt(decrypted);
        }

        int index = decrypted.indexOf(FIELD_SEPARATOR);
        if (index > 0) {
            String sig = decrypted.substring(0, index);
            String orig = decrypted.substring(index + FIELD_SEPARATOR.length());
            if (sig.equals(hex(orig))) {
                return orig;
            }
        }

        throw new EncryptException("Invalid token: " + encrypted);
    }
}
