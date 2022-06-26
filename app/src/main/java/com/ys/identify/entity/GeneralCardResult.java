package com.ys.identify.entity;

/**
 * Return result class of post-processing plugin
 */
public class GeneralCardResult {
    public final String name;
    public final String gender;
    public final String department;
    public final String job;
    public final String rank;
    public final String type;
    public final String number;
    public final String useDate;

    public String valid;

    public GeneralCardResult(String name, String gender, String department, String job, String rank, String type, String number, String useDate) {
        this.name = name;
        this.gender = gender;
        this.department = department;
        this.job = job;
        this.rank = rank;
        this.type = type;
        this.number = number;
        this.useDate = useDate;
    }
}
