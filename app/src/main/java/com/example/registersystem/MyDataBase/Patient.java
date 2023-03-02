package com.example.registersystem.MyDataBase;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "Patient")
public class Patient {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "humanID")
    private String humanID;

    @ColumnInfo(name = "orderSubject")
    private String orderSubject;

    @ColumnInfo(name = "orderDate")
    private String orderDate;

    @ColumnInfo(name = "bitrh")
    private String bitrh;

    @ColumnInfo(name = "sex")
    private String sex;

    @ColumnInfo(name = "belongDoc")
    private String belongDoc;

    @ColumnInfo(name = "waitingNum")
    private int waitingNum;

    @ColumnInfo(name = "isCheckIn")
    private int isCheckIn;

    @ColumnInfo(name = "isWatched")
    private int isWatched;

    @ColumnInfo(name = "isPassed")
    private int isPassed;

    @Ignore
    public Patient(String name, String humanID, String orderSubject, String orderDate, String bitrh, String sex, String belongDoc, int waitingNum) {
        this.name = name;
        this.humanID = humanID;
        this.orderSubject = orderSubject;
        this.orderDate = orderDate;
        this.bitrh = bitrh;
        this.sex = sex;
        this.belongDoc = belongDoc;
        this.waitingNum = waitingNum;
        this.isCheckIn = 0;
        this.isWatched = 0;
        this.isPassed = 0;
    }


    public Patient(int id, String name, String humanID, String orderSubject, String orderDate, String bitrh, String sex, String belongDoc, int waitingNum, int isCheckIn, int isWatched, int isPassed) {
        this.id = id;
        this.name = name;
        this.humanID = humanID;
        this.orderSubject = orderSubject;
        this.orderDate = orderDate;
        this.bitrh = bitrh;
        this.sex = sex;
        this.belongDoc = belongDoc;
        this.waitingNum = waitingNum;
        this.isCheckIn = isCheckIn;
        this.isWatched = isWatched;
        this.isPassed = isPassed;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getWaitingNum() {
        return waitingNum;
    }

    public void setWaitingNum(int waitingNum) {
        this.waitingNum = waitingNum;
    }

    public int getIsCheckIn() {
        return isCheckIn;
    }

    public void setIsCheckIn(int isCheckIn) {
        this.isCheckIn = isCheckIn;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHumanID() {
        return humanID;
    }

    public void setHumanID(String humanID) {
        this.humanID = humanID;
    }

    public String getOrderSubject() {
        return orderSubject;
    }

    public void setOrderSubject(String orderSubject) {
        this.orderSubject = orderSubject;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public String getBitrh() {
        return bitrh;
    }

    public void setBitrh(String bitrh) {
        this.bitrh = bitrh;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getBelongDoc() {
        return belongDoc;
    }

    public void setBelongDoc(String belongDoc) {
        this.belongDoc = belongDoc;
    }

    public int getIsWatched() {
        return isWatched;
    }

    public void setIsWatched(int isPassed) {
        this.isPassed = isPassed;
    }

    public int getIsPassed() {
        return isPassed;
    }

    public void setIsPassed(int isPassed) {
        this.isPassed = isPassed;
    }
}
