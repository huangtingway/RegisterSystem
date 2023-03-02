package com.example.registersystem.MyDataBase;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "DocRoom")
public class DocRoom {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "subject")
    private String subject;

    @ColumnInfo(name = "doctor")
    private String doctor;

    @ColumnInfo(name = "announce")
    private String announce;

    @ColumnInfo(name = "nowNum")
    private int nowNum;

    @Ignore
    public DocRoom(String name, String subject, String doctor, String announce, int nowNum) {
        this.name = name;
        this.subject = subject;
        this.doctor = doctor;
        this.announce = announce;
        this.nowNum = nowNum;
    }


    public DocRoom(int id, String name, String subject, String doctor, String announce, int nowNum) {
        this.id = id;
        this.name = name;
        this.subject = subject;
        this.doctor = doctor;
        this.announce = announce;
        this.nowNum = nowNum;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public int getNowNum() {
        return nowNum;
    }

    public void setNowNum(int nowNum) {
        this.nowNum = nowNum;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDoctor() {
        return doctor;
    }

    public void setDoctor(String doctor) {
        this.doctor = doctor;
    }

    public String getAnnounce() {
        return announce;
    }

    public void setAnnounce(String announce) {
        this.announce = announce;
    }

}
