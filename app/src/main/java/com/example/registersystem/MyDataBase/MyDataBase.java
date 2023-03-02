package com.example.registersystem.MyDataBase;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Dao;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import org.jetbrains.annotations.NotNull;

@Database(entities = {Patient.class, DocData.class, DocRoom.class},version = 6)
public abstract class MyDataBase extends RoomDatabase {

    public static synchronized MyDataBase getInstance(Context context){
        MyDataBase myDataBase = Room.databaseBuilder(context,MyDataBase.class,"MyDataBase").fallbackToDestructiveMigration().build();
        return myDataBase;
    }

   public abstract DataDao getDataDao();
   
}
