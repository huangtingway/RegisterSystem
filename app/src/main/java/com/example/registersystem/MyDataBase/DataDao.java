package com.example.registersystem.MyDataBase;

import androidx.room.*;

import java.util.List;

@Dao
public interface DataDao {
    final String DOC_DATA_TABLE = "DocData";//資料表名稱
    final String DOC_ROOM_TABLE = "DocRoom";
    final String PATIENT_TABLE = "Patient";

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addDocRoom(DocRoom docRoom);//新增診間

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addPatient(Patient patient);//新增病患

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addDoc(DocData docData);//新增醫師

    @Query("select * from " + DOC_ROOM_TABLE)
    List<DocRoom> getDocRoom();

    @Query("select nowNum from " + DOC_ROOM_TABLE + " where name = :roomName")
    int getDocRoomNum(String roomName);//取指定診間現在號碼

    @Query("select * from " + DOC_DATA_TABLE)
    List<DocData> getDoc();//取得所有醫師

    @Query("select name from " + DOC_DATA_TABLE)
    List<String> getDocName();//取得所有醫師名稱

    @Query("select * from " + PATIENT_TABLE + " where belongDoc = :doc")//取得所有病患
    List<Patient> getPatient(String doc);

    @Query("select * from " + PATIENT_TABLE + " where orderDate = :date")//取得指定日期所有病患
    List<Patient> getPatientByDate(String date);

    @Query("select * from " + PATIENT_TABLE + " where belongDoc = :docName and orderDate = :date")
    List<Patient> getPatientByDate(String docName, String date);//取得指定日期、指定醫師所有病患

    @Query("select * from " + PATIENT_TABLE + " where belongDoc = :docName and orderDate = :date and isPassed == 1")
    List<Patient> getPatientPassed(String docName, String date);//取得指定日期所有已過號病患

    @Query("select * from " + PATIENT_TABLE + " where belongDoc = :docName and waitingNum = :waitingNum")
    Patient getPatientByWaitingNum(String docName, int waitingNum);//取得指定醫師指定號碼之病患

    @Query("select max(waitingNum) from " + PATIENT_TABLE + " where belongDoc = :docName and orderDate = :date")
    int getPatientOrderNum(String docName, String date);//取得之定醫師看診日期預約號碼


    @Update
    void updateDocRoom(DocRoom docRoom);//更新診間


    @Update
    void updatePatient(Patient patient);//更新病患

    @Update
    void updateDoc(DocData docData);//更新醫師

    @Query("update " + PATIENT_TABLE + " set isCheckIn = :isCheck where id = :id")
    void updatePatientCheck(int id, int isCheck);//更新病患報到狀態


    @Query("update " + PATIENT_TABLE + " set isWatched = :isWatched where id = :id")
    void updatePatientWatched(int id, int isWatched);//更新病患已診狀態

    @Query("update " + PATIENT_TABLE + " set isPassed = :isPassed where id = :id")
    void updatePatientPassed(int id, int isPassed);//更新病患過號狀態

    @Query("update " + PATIENT_TABLE + " set belongDoc = :newDocName where belongDoc = :beforeDocName")
    void updatePatientBelongDoc(String beforeDocName, String newDocName);//更新病患所屬醫師


    @Query("update " + DOC_ROOM_TABLE + " set nowNum = :nowNum where id = :id")
    void updateRoomNum(int id, int nowNum);//更新診間現在號碼

    @Query("delete from " + PATIENT_TABLE + " where id = :id")
    void deletePatient(int id);//刪除指定病患

    @Query("delete from " + PATIENT_TABLE + " where belongDoc = :belongDoc and isWatched = 1")
    void deleteWatchedPatient(String belongDoc);//刪除已看診病患

    @Query("delete from " + PATIENT_TABLE + " where belongDoc = :belongDoc")
    void deleteBelongDocPatient(String belongDoc);//刪除指定醫師所有病患

    @Query("delete from " + DOC_ROOM_TABLE + " where id = :id")
    void deleteRoom(int id);//刪除指定診間

    @Query("delete from " + DOC_DATA_TABLE + " where id = :id")
    void deleteDoc(int id);//刪除指定醫師
}
