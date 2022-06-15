package com.ys.identify.entity;

/**
 * Return result class of post-processing plugin
 */
public class GeneralCardResult {
    public final String valid;
    public final String number;

    public GeneralCardResult(String valid, String number) {
        this.valid = valid;
        this.number = number;
    }
}
