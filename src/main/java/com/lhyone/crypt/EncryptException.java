package com.lhyone.crypt;

/**
 * Created by Think on 2017/8/27.
 */
public class EncryptException extends RuntimeException{
    private static final long serialVersionUID = -7896887327676105502L;

    public EncryptException(Exception e) {
        super(e);
    }

    public EncryptException(String msg) {
        super(msg);
    }
}
