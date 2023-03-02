package com.example.registersystem;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.example.registersystem.Dialog.EditDocDailog;
import com.example.registersystem.Dialog.PatientDialog;
import com.example.registersystem.MyDataBase.DocData;
import com.example.registersystem.MyDataBase.MyDataBase;
import com.example.registersystem.MyDataBase.Patient;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Doctor extends Fragment {
    private MainActivity mainActivity;
    private View doctorView;
    private FragmentTransaction transaction;
    private MyDataBase myDataBase;
    private PatientList patientList;
    private StartFrag startFrag;

    private ListView listView;//醫師清單
    private SimpleAdapter simpleAdapter;//醫師清單調變器
    private String from[] = {"doctorname1", "doctornamecategory"};//醫師清單元件來源id名稱
    private int to[] = {R.id.doctorname1, R.id.doctornamecategory};//醫師清單元件來源id數值
    private ArrayList<HashMap<String, String>> data;//醫師清單資料
    private ArrayList<CharSequence> docNameList;//醫師物件arraylist(下拉選單用)
    private List<DocData> docDataList;//醫師物件list
    private MaterialButton addPatient, addDoctor;
    private MaterialToolbar topBar;
    private ArrayAdapter<CharSequence> subjectAdapter, docAdapter;//科別、醫師物件調變器(下拉選單用)

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
        doctorView = inflater.inflate(R.layout.fragment_doctor, container, false);
        init();//初始化全域變數
        setList();//初始化醫師清單
        setData();//取得醫師資料
        setAddPatientListener();//新增病患按鈕監聽器
        setAddDocListener();//新增醫師按鈕監聽器
        setListListener();//醫師清單點擊監聽器
        return doctorView;
    }

    private void init() {
        startFrag = new StartFrag();
        patientList = new PatientList();
        topBar = mainActivity.findViewById(R.id.mainTopBar);
        addPatient = doctorView.findViewById(R.id.addnewnumber);
        addDoctor = doctorView.findViewById(R.id.addnewdoctor);
        listView = doctorView.findViewById(R.id.doctornamelist);
        data = new ArrayList<>();
        docDataList = new ArrayList<>();
        docNameList = new ArrayList<>();
        docAdapter = new ArrayAdapter<>(mainActivity, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, docNameList);
        subjectAdapter = ArrayAdapter.createFromResource(mainActivity, R.array.subjectList, com.google.android.material.R.layout.support_simple_spinner_dropdown_item);
        myDataBase = MyDataBase.getInstance(mainActivity);
        topBar.getMenu().clear();
        topBar.inflateMenu(R.menu.main_top_menu);
        topBar.setNavigationIcon(R.drawable.ic_home);
        topBar.setTitle("醫師管理");
        topBar.setNavigationOnClickListener(new View.OnClickListener() {//點擊頂部home鍵則切換至起始介面
            @Override
            public void onClick(View v) {
                transaction = mainActivity.getManager().beginTransaction();
                transaction.replace(R.id.mainFrag,startFrag);
                transaction.commit();
            }
        });
    }

    private void setList() {
        simpleAdapter = new SimpleAdapter(mainActivity, data, R.layout.doctornamelist, from, to);
        listView.setAdapter(simpleAdapter);
    }

    private void setData() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                docNameList.clear();//清除醫師資料
                data.clear();
                docDataList = myDataBase.getDataDao().getDoc();//重新取得醫師資料

                for (DocData docdata : docDataList) { //set listview data
                    HashMap<String, String> item = new HashMap<>();
                    item.put("doctorname1", docdata.getName());
                    item.put("doctornamecategory", "科別：" + docdata.getSubject());
                    data.add(item);
                    docNameList.add(docdata.getName());
                }
                mainActivity.runOnUiThread(() -> {//更新ui介面
                    simpleAdapter.notifyDataSetChanged();
                    docAdapter.notifyDataSetChanged();
                });
            }
        });
        thread.start();
    }

    private void setAddPatientListener() { //add patient
        addPatient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PatientDialog patientDialog = new PatientDialog(mainActivity, docAdapter, subjectAdapter, false) { //show add patient dialog
                    @Override
                    public void refreshData(Patient patient) { //on data set -> insert data to database
                        super.refreshData(patient);
                        Thread thread = new Thread(() -> {
                            myDataBase.getDataDao().addPatient(patient);//新增病患資料
                            mainActivity.runOnUiThread(() -> Toast.makeText(mainActivity, "新增成功", Toast.LENGTH_SHORT).show());
                        });
                        thread.start();
                    }
                };
                mainActivity.setMainDialog(patientDialog.getDailog());
                mainActivity.getMainDialog().show();//顯示對話框
            }
        });
    }

    private void setAddDocListener() { //add doctor
        addDoctor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditDocDailog editDocDailog = new EditDocDailog(mainActivity, subjectAdapter, false) { //show add doctor dialog
                    @Override
                    public void refreshData(DocData docData) { //on data set -> insert data to database
                        super.refreshData(docData);
                        Thread thread = new Thread(() -> {
                            myDataBase.getDataDao().addDoc(docData);//新增醫師資料
                            setData();
                            mainActivity.runOnUiThread(() -> Toast.makeText(mainActivity, "新增成功", Toast.LENGTH_SHORT).show());
                        });
                        thread.start();
                    }
                };
                mainActivity.setMainDialog(editDocDailog.getDialog());
                mainActivity.getMainDialog().show();//顯示對話框
            }
        });
    }

    private void setListListener() { //list item click
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) { //show select action dialog
                LayoutInflater inflater = getLayoutInflater();
                View bottomView = inflater.inflate(R.layout.dialog_bottom, null, false);//初始化對話框ui元件
                MaterialButton editDocBtn = bottomView.findViewById(R.id.editdoctorbtn);
                MaterialButton editPatientBtn = bottomView.findViewById(R.id.editnumberbtn);

                setEditDocBtnListener(editDocBtn, position);//編輯醫師資料動作
                setEditPatientBtnListener(editPatientBtn, position);//編輯病患

                MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(mainActivity, R.style.Dialog);
                dialogBuilder.setView(bottomView);
                mainActivity.setMainDialog(dialogBuilder.create());
                mainActivity.getMainDialog().show();//顯示對話框
            }
        });
    }


    private void setEditDocBtnListener(MaterialButton editDocBtn, int position) { //edit doctor data action
        editDocBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.getMainDialog().dismiss();
                EditDocDailog editDocDailog = new EditDocDailog(mainActivity, subjectAdapter, true) { //show edit doctor dialog
                    @Override
                    public void refreshData(DocData docData) { //update data to database
                        super.refreshData(docData);
                        Thread thread = new Thread(() -> {
                            myDataBase.getDataDao().updateDoc(docData);//更新醫師資料至資料庫
                            myDataBase.getDataDao().updatePatientBelongDoc(docDataList.get(position).getName(), docData.getName());//更新所有該醫師所屬病患資料
                            setData();//重新取得醫師資料，更新醫師清單顯示資料
                        });
                        thread.start();
                    }

                    @Override
                    public void deleteData(int id) { //delete data from database
                        super.deleteData(id);
                        MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(mainActivity, R.style.Dialog);//顯示確認刪除對話框
                        alertDialogBuilder.setTitle("確認刪除");
                        alertDialogBuilder.setMessage("該醫師所屬病患將一併刪除");
                        alertDialogBuilder.setPositiveButton("確認", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Thread thread = new Thread(() -> {
                                    myDataBase.getDataDao().deleteDoc(id);//刪除醫師資料
                                    myDataBase.getDataDao().deleteBelongDocPatient(docNameList.get(position).toString());
                                    setData();//重新取得醫師資料，更新醫師清單顯示資料
                                });
                                thread.start();
                            }
                        });
                        alertDialogBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mainActivity.getMainDialog().dismiss();
                            }
                        });
                        mainActivity.setMainDialog(alertDialogBuilder.create());
                        mainActivity.getMainDialog().show();//顯示確認刪除對話框

                    }
                };
                editDocDailog.setId(docDataList.get(position).getId());//設定編輯醫師預設資料
                editDocDailog.setDefultName(docDataList.get(position).getName());
                editDocDailog.setDefultSubject(docDataList.get(position).getSubject());
                mainActivity.setMainDialog(editDocDailog.getDialog());
                mainActivity.getMainDialog().show();
            }
        });
    }

    private void setEditPatientBtnListener(MaterialButton editPatientBtn, int position) { //edit patient action -> switch to patientlist fragment
        editPatientBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle result = new Bundle();
                result.putString("docName", docDataList.get(position).getName());//設定預設傳入資料
                getParentFragmentManager().setFragmentResult("adapterPatientList", result);
                transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.manageFragContainer, patientList);
                transaction.commit();//切換至病患編輯介面
                mainActivity.getMainDialog().dismiss();//關閉選擇動作對話框
            }
        });
    }

    @Override
    public void onPause() {
        myDataBase.close();
        super.onPause();
    }
}