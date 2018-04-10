package com.lhyone.crypt;
import org.apache.commons.codec.digest.Crypt;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * Created by Think on 2017/8/27.
 */
public class PasswordCrypt {
    private static final String CRYPT_SALT = "kdxwarranty";

    public static String encrypt(String pwd) {
        String crypt = Crypt.crypt(pwd, CRYPT_SALT);
        return DigestUtils.md5Hex(crypt);
    }

    public static void main(String[] args) {
        System.out.println(encrypt("aaaaaa"));
    }
}
