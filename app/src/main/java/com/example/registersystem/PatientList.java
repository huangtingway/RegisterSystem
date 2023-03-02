package com.example.registersystem;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.FragmentResultListener;
import androidx.fragment.app.FragmentTransaction;
import com.example.registersystem.Dialog.PatientDialog;
import com.example.registersystem.MyDataBase.DocData;
import com.example.registersystem.MyDataBase.MyDataBase;
import com.example.registersystem.MyDataBase.Patient;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class PatientList extends Fragment {
    private View patient;
    private MainActivity mainActivity;
    private FragmentTransaction transaction;
    private MyDataBase myDataBase;
    private boolean isDefultData;
    private Doctor doctor;

    private ListView listView;//病患清單
    private PatientListAdapter patientListAdapter;//病患清單調變器
    private ArrayAdapter<CharSequence> subjectAdapter, docAdapter;//科別、醫師調變器(下拉選單用)
    private ArrayList<CharSequence> docNameList;//醫師物件(下拉選單用)
    private List<DocData> docDataList;//醫師物件
    private List<Patient> patientList;//病患物件
    private String getDocName;//選擇醫師介面傳入預設值
    private MaterialToolbar topBar;

    @Override
    public void onAttach(@NonNull @NotNull Context context) {
        mainActivity = (MainActivity) context;
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        patient = inflater.inflate(R.layout.fragment_patient_list, container, false);
        init();
        setListListener();
        return patient;
    }

    private void init() {
        myDataBase = MyDataBase.getInstance(mainActivity);
        doctor = new Doctor();
        docNameList = new ArrayList<>();
        docDataList = new ArrayList<>();
        patientList = new ArrayList<>();
        topBar = mainActivity.findViewById(R.id.mainTopBar);
        listView = patient.findViewById(R.id.patient_list);
        docAdapter = new ArrayAdapter<>(mainActivity, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, docNameList);
        subjectAdapter = ArrayAdapter.createFromResource(mainActivity, R.array.subjectList, com.google.android.material.R.layout.support_simple_spinner_dropdown_item);

        topBar.setNavigationIcon(R.drawable.ic_keyboard_backspace);
        topBar.setTitle("病患資料");//設置導覽列標題

        topBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {//點擊頂部返回按鈕則切換至選擇醫師介面
                transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.manageFragContainer, doctor);
                transaction.commit();
            }
        });
        setDefultData();//取得初始資料

    }

    private void setDefultData() { //get defult doctor name
        getParentFragmentManager().setFragmentResultListener("adapterPatientList", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull @NotNull String requestKey, @NonNull @NotNull Bundle result) {
                getDocName = result.getString("docName");
                setData();//取得病患資料
            }
        });
    }

    private void setData() { //get data from database
        Thread thread = new Thread(() -> {
            docNameList.clear();//清除醫師資料
            patientList = myDataBase.getDataDao().getPatient(getDocName);//取得醫師所屬病患
            docDataList = myDataBase.getDataDao().getDoc();//取得醫師資料

            for (DocData docdata : docDataList) {
                docNameList.add(docdata.getName());//新稱醫師資料至
            }

            mainActivity.runOnUiThread(() -> {//更新ui介面
                docAdapter.notifyDataSetChanged();
                setListView();
            });

            if(isDefultData == false) { //第一次進入此頁面則顯示是否刪除已看診病患對話框
                mainActivity.runOnUiThread(() -> {//更新ui介面
                    isDefultData = true;
                    showIsDeleteWatchedPatient();//是否刪除已看診病患
                });
            }
        });
        thread.start();
    }

    private void setListView() {
        patientListAdapter = new PatientListAdapter(patientList);
        listView.setAdapter(patientListAdapter);
    }

    private void setListListener() { //list item click -> edit patient
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.v("Listpos", "pos:" + position);
                PatientDialog patientDialog = new PatientDialog(mainActivity, docAdapter, subjectAdapter, true) { //show edit patient dialog
                    @Override
                    public void refreshData(Patient patient) { //upadte patient data to database
                        super.refreshData(patient);
                        Thread thread = new Thread(() -> {
                            myDataBase.getDataDao().updatePatient(patient);//更新資料
                            setData();//重新取得資料、更新病患清單
                        });
                        thread.start();
                    }

                    @Override
                    public void deleteData(int id) { //delete patient data from database
                        super.deleteData(id);
                        Thread thread = new Thread(() -> {
                            myDataBase.getDataDao().deletePatient(id);
                            setData();//重新取得資料、更新病患清單
                        });
                        thread.start();
                    }
                };
                //設定對話框預設值
                patientDialog.setId(patientList.get(position).getId()); //set defult data to databse
                patientDialog.setInputName(patientList.get(position).getName());
                patientDialog.setInputHumanID(patientList.get(position).getHumanID());
                patientDialog.setInputDoctor(patientList.get(position).getBelongDoc());
                patientDialog.setInputSex(patientList.get(position).getSex());
                patientDialog.setInputBrithDate(patientList.get(position).getBitrh());
                patientDialog.setInputOrderDate(patientList.get(position).getOrderDate());
                patientDialog.setInputSubject(patientList.get(position).getOrderSubject());
                patientDialog.setIsCheckin(patientList.get(position).getIsCheckIn());
                patientDialog.setIsWatched(patientList.get(position).getIsWatched());
                patientDialog.setWaitingNum(patientList.get(position).getWaitingNum());
                patientDialog.setIsPassed(patientList.get(position).getIsPassed());
                mainActivity.setMainDialog(patientDialog.getDailog());
                mainActivity.getMainDialog().show();//顯示對話框
            }
        });
    }

    private void showIsDeleteWatchedPatient() { //when first enter fragment and has watched patient -> show is delete watched patient
        boolean checkIsWatched = false;
        for (Patient patientItem : patientList) {
            if(patientItem.getIsWatched() == 1) {
                checkIsWatched = true;
            }
        }
        if(checkIsWatched) {
            MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(mainActivity, R.style.Dialog);//設置確認刪除對話框
            dialogBuilder.setTitle("確認刪除");
            dialogBuilder.setMessage("是否刪除已看診病患");
            dialogBuilder.setPositiveButton("確認", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) { //delete patient data from database
                    Thread thread = new Thread(() -> {
                        myDataBase.getDataDao().deleteWatchedPatient(getDocName);//刪除資料
                        setData();//重新取得資料、更新病患清單
                    });
                    thread.start();
                    mainActivity.getMainDialog().dismiss();
                }
            });
            dialogBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mainActivity.getMainDialog().dismiss();
                }
            });
            mainActivity.setMainDialog(dialogBuilder.create());
            mainActivity.getMainDialog().show();//顯示對話框
        }
    }

    @Override
    public void onPause() {
        myDataBase.close();
        super.onPause();
    }

    private class PatientListAdapter extends BaseAdapter {
        List<Patient> adapterPatientList;//病患資料物件
        LayoutInflater inflater;

        public PatientListAdapter(List<Patient> patientList) {//取得病患資料及layout inflater
            this.adapterPatientList = patientList;
            inflater = getLayoutInflater();
        }

        @Override
        public int getCount() {
            return adapterPatientList.size();
        }//回傳資料筆數

        @Override
        public Object getItem(int position) {
            return adapterPatientList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) { //set list view data, status color
            convertView = inflater.inflate(R.layout.patientlist_listview, parent, false);//初始化list介面
            TextView name = convertView.findViewById(R.id.patientlist_name);
            TextView sex = convertView.findViewById(R.id.patientlist_sex);
            TextView id = convertView.findViewById(R.id.patientlist_id);
            TextView brithDate = convertView.findViewById(R.id.patientlist_birthday);
            TextView orderDate = convertView.findViewById(R.id.patientlist_watchdate);
            TextView stateCheckin = convertView.findViewById(R.id.state_not_checkin);
            TextView stateWatched = convertView.findViewById(R.id.state_not_watch);//設置清單資訊

            name.setText(adapterPatientList.get(position).getName());
            sex.setText("性別：" + adapterPatientList.get(position).getSex());
            id.setText("身分證號：" + adapterPatientList.get(position).getHumanID());
            brithDate.setText("出生日期：" + adapterPatientList.get(position).getBitrh());
            orderDate.setText("掛號日期：" + adapterPatientList.get(position).getOrderDate());//變更看診狀態元件顏色
            if(adapterPatientList.get(position).getIsCheckIn() == 1) {//已報到->藍色
                stateCheckin.setBackgroundResource(R.drawable.ic_checked);
            }

            if(adapterPatientList.get(position).getIsWatched() == 1) {//已看診->綠色
                stateWatched.setBackgroundResource(R.drawable.ic_watched);
            } else if(adapterPatientList.get(position).getIsPassed() == 1) {//已過號->橘色
                stateWatched.setBackgroundResource(R.drawable.ic_passed);
            }

            return convertView;
        }
    }
}