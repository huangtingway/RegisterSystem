package com.example.registersystem.Dialog;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import com.example.registersystem.MainActivity;
import com.example.registersystem.MyDataBase.MyDataBase;
import com.example.registersystem.MyDataBase.Patient;
import com.example.registersystem.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Calendar;

public class PatientDialog {
    private MaterialAlertDialogBuilder dialogBuilder;
    private MainActivity mainActivity;
    private MyDataBase myDataBase;
    private Calendar calendar;
    private String inputName, inputHumanID, inputSex, inputSubject, inputDoctor, inputOrderDate, inputBrithDate;//對話框預設資料
    private ArrayAdapter<CharSequence> subjectAdapter, docAdapter;//下拉式選單調變器

    private LayoutInflater layoutInflater;
    private View editPatient;
    private TextInputEditText name;
    private TextInputEditText humanID;
    private TextView birthDate;
    private TextView orderDate;
    private MaterialButton brithDateBtn;
    private MaterialButton orderDateBtn;
    private Spinner docSpin;
    private Spinner subjectspin;
    private RadioGroup radioGroup;
    private boolean hasdefultData;//是否有預設資料
    private int id, isCheckin, isWatched, waitingNum, isPassed;//病患狀態資

    public PatientDialog(MainActivity mainActivity, ArrayAdapter<CharSequence> docAdapter, ArrayAdapter<CharSequence> subjectAdapter, boolean hasdefultData) {
        this.mainActivity = mainActivity;
        this.docAdapter = docAdapter;
        this.subjectAdapter = subjectAdapter;
        this.hasdefultData = hasdefultData;
    }

    public Dialog getDailog() { //create dialog
        calendar = Calendar.getInstance();
        myDataBase = MyDataBase.getInstance(mainActivity);
        initView();//初始化全域變數
        if(hasdefultData) {//若有預設資料則設置預設資料
            setDefultData();
        }
        setListener();//設置點及監聽器

        dialogBuilder = new MaterialAlertDialogBuilder(mainActivity, R.style.Dialog);//建立對話框
        dialogBuilder.setTitle("掛號編輯");
        dialogBuilder.setView(editPatient);

        dialogBuilder.setPositiveButton("確定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(checkData(name, humanID, docSpin, orderDate, birthDate, inputSex)) { //點擊確定->檢查資料正確後新增或更新資料
                    Thread thread = new Thread(() -> {
                        if(hasdefultData) {//有預設資料則更新資料
                            refreshData(new Patient(
                                    id, name.getText().toString(),
                                    humanID.getText().toString(),
                                    subjectspin.getSelectedItem().toString(),
                                    orderDate.getText().toString(),
                                    birthDate.getText().toString(),
                                    inputSex,
                                    docSpin.getSelectedItem().toString(),
                                    waitingNum,
                                    isCheckin, isWatched, isPassed));
                        } else {//無預設資料則新增資料
                            waitingNum = myDataBase.getDataDao().
                                    getPatientOrderNum(docSpin.getSelectedItem().toString(), orderDate.getText().toString()) + 1;//取等待號碼
                            refreshData(new Patient(
                                    name.getText().toString(),
                                    humanID.getText().toString(),
                                    subjectspin.getSelectedItem().toString(),
                                    orderDate.getText().toString(),
                                    birthDate.getText().toString(),
                                    inputSex,
                                    docSpin.getSelectedItem().toString(),
                                    waitingNum));
                        }

                        Log.v("database", "name:" + name.getText().toString() +
                                " id:" + humanID.getText().toString() +
                                " subject:" + subjectspin.getSelectedItem().toString() +
                                " odate:" + orderDate.getText().toString() +
                                " bate:" + birthDate.getText().toString() +
                                " sex:" + inputSex +
                                " doc:" + docSpin.getSelectedItem().toString() +
                                " num:" + waitingNum);//設置Log
                    });
                    thread.start();
                    mainActivity.getMainDialog().dismiss();
                }
            }
        });
        dialogBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mainActivity.getMainDialog().dismiss();
            }
        });
        dialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                myDataBase.close();
            }
        });
        if(hasdefultData) {//若有預設資料則顯示刪除按鈕
            dialogBuilder.setNeutralButton("刪除", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    deleteData(id);
                }//點擊刪除則刪除資料
            });
        }
        return dialogBuilder.create();
    }

    private void initView() {
        layoutInflater = mainActivity.getLayoutInflater();
        editPatient = layoutInflater.inflate(R.layout.edit_patient_dialog, null, false);
        name = editPatient.findViewById(R.id.dialog_patient_enterpatientname);
        humanID = editPatient.findViewById(R.id.dialog_patient_enteridcode);
        birthDate = editPatient.findViewById(R.id.dialog_patient_enterbirthday);
        orderDate = editPatient.findViewById(R.id.dialog_patient_enterdate);
        brithDateBtn = editPatient.findViewById(R.id.choosebirthday);
        orderDateBtn = editPatient.findViewById(R.id.chosedate);
        docSpin = editPatient.findViewById(R.id.dialog_patient_enterdoctorname);
        subjectspin = editPatient.findViewById(R.id.dialog_patient_entercategory);
        radioGroup = editPatient.findViewById(R.id.radioGroup_sex);
        docSpin.setAdapter(docAdapter);
        subjectspin.setAdapter(subjectAdapter);
        birthDate.setText("----/--/--");
        orderDate.setText("----/--/--");
    }

    private void setDefultData() {
        name.setText("" + inputName);
        humanID.setText("" + inputHumanID);
        birthDate.setText("" + inputBrithDate);
        orderDate.setText("" + inputOrderDate);
        docSpin.setAdapter(docAdapter);
        subjectspin.setAdapter(subjectAdapter);
        docSpin.setSelection(docAdapter.getPosition(inputDoctor));
        subjectspin.setSelection(subjectAdapter.getPosition(inputSubject));
        if(inputSex.equals("男")) {
            radioGroup.check(R.id.radio_button_man);
        } else if(inputSex.equals("女")) {
            radioGroup.check(R.id.radio_button_women);
        }
    }

    private void setListener() {
        brithDateBtn.setOnClickListener(new View.OnClickListener() {//選擇出生日期
            @Override
            public void onClick(View v) {
                showDatePicker(birthDate);
            }//顯示日期選擇器
        });
        birthDate.setOnClickListener(new View.OnClickListener() {//選擇出生日期
            @Override
            public void onClick(View v) {
                showDatePicker(birthDate);
            }//顯示日期選擇器

        });

        orderDateBtn.setOnClickListener(new View.OnClickListener() {//選擇看診日
            @Override
            public void onClick(View v) {
                showDatePicker(orderDate);//顯示日期選擇器

            }
        });
        orderDate.setOnClickListener(new View.OnClickListener() {//選擇看診日期
            @Override
            public void onClick(View v) {
                showDatePicker(orderDate);
            }//顯示日期選擇器
        });
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {//選擇性別
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.radio_button_man) {//設置姓別為男
                    inputSex = "男";
                } else if(checkedId == R.id.radio_button_women) {//設置姓別為女
                    inputSex = "女";
                }
            }
        });
    }

    private void showDatePicker(TextView showText) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(mainActivity);
        if(showText.getId() == R.id.dialog_patient_enterbirthday) {//若選擇出生日期，則設置最大選擇日期為今日
            datePickerDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());
        } else if(showText.getId() == R.id.dialog_patient_enterdate) {//若選擇看診日期，則設置最小選擇日期為今日
            datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
        }

        datePickerDialog.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {//當資料完成設置，回傳選擇日期
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                int getMonth = month + 1;
                String datePickerResult = year + "/" + getMonth + "/" + dayOfMonth;
                showText.setText(datePickerResult);
            }
        });
        mainActivity.setSecondDialog(datePickerDialog);//顯示對話框
        mainActivity.getSecondDialog().show();
    }

    private boolean checkData(TextInputEditText name, TextInputEditText humanID, Spinner docSpin, TextView orderDate, TextView birthDate, String sex) {
        if(name.getText().toString() == null || name.getText().toString().equals("")) {//檢查姓名
            Toast.makeText(mainActivity, "請輸入姓名", Toast.LENGTH_SHORT).show();
            return false;
        } else if(humanID.getText().toString() == null || humanID.getText().toString().equals("")) {//檢查身分證號
            Toast.makeText(mainActivity, "請輸入身分證號", Toast.LENGTH_SHORT).show();
            return false;
        } else if(docSpin.getSelectedItem() == null || docSpin.getSelectedItem().toString().equals("")) {//檢查選擇醫師
            Toast.makeText(mainActivity, "請先新增醫師", Toast.LENGTH_SHORT).show();
            return false;
        } else if(orderDate.getText().toString() == null || orderDate.getText().toString().equals("")) {
            Toast.makeText(mainActivity, "請輸入掛號日期", Toast.LENGTH_SHORT).show();
            return false;
        } else if(birthDate.getText().toString() == null || birthDate.getText().toString().equals("")) {
            Toast.makeText(mainActivity, "請輸入出生日期", Toast.LENGTH_SHORT).show();
            return false;
        } else if(sex == null) {
            Toast.makeText(mainActivity, "請選擇性別", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }

    public void refreshData(Patient patient) {
    }

    public void deleteData(int id) {
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setInputName(String inputName) {
        this.inputName = inputName;
    }

    public void setInputHumanID(String inputHumanID) {
        this.inputHumanID = inputHumanID;
    }

    public void setInputSex(String inputSex) {
        this.inputSex = inputSex;
    }

    public void setInputSubject(String inputSubject) {
        this.inputSubject = inputSubject;
    }

    public void setInputDoctor(String inputDoctor) {
        this.inputDoctor = inputDoctor;
    }

    public void setInputOrderDate(String inputOrderDate) {
        this.inputOrderDate = inputOrderDate;
    }

    public void setInputBrithDate(String inputBrithDate) {
        this.inputBrithDate = inputBrithDate;
    }

    public void setIsCheckin(int isCheckin) {
        this.isCheckin = isCheckin;
    }

    public void setIsWatched(int isWatched) {
        this.isWatched = isWatched;
    }

    public void setWaitingNum(int waitingNum) {
        this.waitingNum = waitingNum;
    }

    public void setIsPassed(int isPassed) {
        this.isPassed = isPassed;
    }
}

