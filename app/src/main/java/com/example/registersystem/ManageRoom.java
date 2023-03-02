package com.example.registersystem;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;
import androidx.fragment.app.FragmentTransaction;
import com.example.registersystem.Dialog.EditRoomDialog;
import com.example.registersystem.Dialog.NumPadDialog;
import com.example.registersystem.MyDataBase.DocData;
import com.example.registersystem.MyDataBase.DocRoom;
import com.example.registersystem.MyDataBase.MyDataBase;
import com.example.registersystem.MyDataBase.Patient;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ManageRoom extends Fragment {
    private View manageRoom;
    private MainActivity mainActivity;
    private MyDataBase myDataBase;
    private ChooseRoom chooseRoom;
    private FragmentTransaction transaction;
    private Calendar calendar;
    private String today;
    private MaterialToolbar topBar;
    private MaterialButton nextBtn, editNumBtn, roomEditBtn;
    private TextView topRoomNum, cardRoomName, cardsubject, cardDoc, cardAnnounce;
    private String getcardRoomName, getcardsubject, getcardDoc, getcardAnnounce;//選擇診間介面傳入預設資料
    private TextView cardPName, cardPID, cardPBrith, cardPOrder, cardPSubject, cardPSex;
    private int roomID;
    private MaterialAlertDialogBuilder dialogBuilder;
    private ArrayList<CharSequence> docName;
    private ArrayAdapter<CharSequence> docAdapter, subjectAdapter;//醫師、科別調變器(下拉選單用)
    private List<Patient> patientList;//病患物件list
    private int nowNum;//現在號碼

    @Override
    public void onAttach(@NonNull @NotNull Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        manageRoom = inflater.inflate(R.layout.fragment_manage_room, container, false);
        init();
        setActionBar();
        setRoomCardView();
        setRoomBtnListener();
        setnextBtnListener();
        setEditBtnListener();
        return manageRoom;
    }

    private void init() {
        nowNum = 0;
        chooseRoom = new ChooseRoom();
        topBar = mainActivity.findViewById(R.id.mainTopBar);
        topRoomNum = manageRoom.findViewById(R.id.nownumber);
        cardRoomName = manageRoom.findViewById(R.id.roomname);
        cardsubject = manageRoom.findViewById(R.id.roomIDcategory);
        cardDoc = manageRoom.findViewById(R.id.roomdoctorname);
        cardAnnounce = manageRoom.findViewById(R.id.roomannouncement);
        nextBtn = manageRoom.findViewById(R.id.nextbtn);
        editNumBtn = manageRoom.findViewById(R.id.editnumbtn);
        roomEditBtn = manageRoom.findViewById(R.id.editbtn);

        cardPName = manageRoom.findViewById(R.id.patientname);
        cardPSubject = manageRoom.findViewById(R.id.IDcategory);
        cardPSex = manageRoom.findViewById(R.id.sex);
        cardPID = manageRoom.findViewById(R.id.identity);
        cardPOrder = manageRoom.findViewById(R.id.watchdate);
        cardPBrith = manageRoom.findViewById(R.id.birthday);

        patientList = new ArrayList<>();
        docName = new ArrayList<>();
        docAdapter = new ArrayAdapter<>(mainActivity, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, docName);
        subjectAdapter = ArrayAdapter.createFromResource(mainActivity, R.array.subjectList, com.google.android.material.R.layout.support_simple_spinner_dropdown_item);
        calendar = Calendar.getInstance();
        myDataBase = MyDataBase.getInstance(mainActivity);
        int month = calendar.get(Calendar.MONTH) + 1;
        today = calendar.get(Calendar.YEAR) + "/" + month + "/" + calendar.get(Calendar.DATE);
    }

    private void setActionBar() { //top app bar click event
        topBar.getMenu().clear();
        topBar.inflateMenu(R.menu.manage_room_menu);
        topBar.setNavigationIcon(R.drawable.ic_keyboard_backspace);//back button 點擊頂部返回按鈕則切換至選擇診間介面
        topBar.setTitle("診間編輯");
        topBar.setNavigationOnClickListener(new View.OnClickListener() { //back button
            @Override
            public void onClick(View v) {
                transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.manageFragContainer, chooseRoom);
                transaction.commit();
            }
        });
        //delete button
        topBar.setOnMenuItemClickListener(item -> {//點擊刪除按鈕則顯示確認刪除對話框

            if(item.getItemId() == R.id.topDelete) {
                dialogBuilder = new MaterialAlertDialogBuilder(mainActivity, R.style.Dialog); //show delete data dialog
                dialogBuilder.setTitle("確認刪除");
                dialogBuilder.setMessage("是否確認刪除診間?");
                dialogBuilder.setPositiveButton("確定", (dialog, which) -> { //delete data,back to choose room
                    Thread thread = new Thread(() -> myDataBase.getDataDao().deleteRoom(roomID));//刪除資料
                    thread.start();
                    transaction = getParentFragmentManager().beginTransaction();
                    transaction.replace(R.id.manageFragContainer, chooseRoom);//返回選擇診間介面
                    transaction.commit();
                    mainActivity.getMainDialog().dismiss();
                });
                dialogBuilder.setNegativeButton("取消", (dialog, which) -> mainActivity.getMainDialog().dismiss());
                mainActivity.setMainDialog(dialogBuilder.create());
                mainActivity.getMainDialog().show();//顯示對話框
                return false;
            }
            return false;
        });
    }

    private void setRoomCardView() { //get defult data
        getParentFragmentManager().setFragmentResultListener("chooseRoomData", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull @NotNull String requestKey, @NonNull @NotNull Bundle result) {
                roomID = result.getInt("id");//取得選擇診間介面傳入預設資料
                getcardRoomName = result.getString("roomName");
                getcardDoc = result.getString("doc");
                getcardsubject = result.getString("subject");
                getcardAnnounce = result.getString("announce");

                cardRoomName.setText("診間名稱：" + getcardRoomName);//設置第三個
                cardsubject.setText("科別：" + getcardsubject);
                cardDoc.setText("醫師：" + getcardDoc);
                cardAnnounce.setText("公告：" + getcardAnnounce);
            }
        });
        setData();
    }

    private void setData() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                nowNum = 0;
                patientList.clear();//清除醫師、病患資料
                docName.clear();
                patientList = myDataBase.getDataDao().getPatientByDate(today); //得病患、醫師資料
                List<DocData> getDocName = myDataBase.getDataDao().getDoc();

                for (int i = 0; i < patientList.size(); i++) { //remove not this doc and passed patient
                    if(!getcardDoc.equals(patientList.get(i).getBelongDoc()) || patientList.get(i).getIsPassed() == 1) {
                        patientList.remove(i);
                        i--;
                    }
                }

                patientList.addAll(myDataBase.getDataDao().getPatientPassed(getcardDoc, today)); //add passed patient to patient array’s last

                for (DocData docitem : getDocName) {//add data to doc name list
                    docName.add(docitem.getName());
                }

                setNextPatient(); //set first patient
                mainActivity.runOnUiThread(new Runnable() {//更新ui畫面
                    @Override
                    public void run() {
                        docAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
        thread.start();
    }

    private void setPatientCardView(Patient showPatient) {
        mainActivity.runOnUiThread(() -> {
            topRoomNum.setText("" + showPatient.getWaitingNum());
            if(showPatient.getIsPassed() == 1) {
                cardPName.setText("姓名：" + showPatient.getName() + "(過號)");
            } else {
                cardPName.setText("姓名：" + showPatient.getName());
            }
            cardPSubject.setText("科別：" + showPatient.getOrderSubject());
            cardPSex.setText("性別：" + showPatient.getSex());
            cardPID.setText("身分證號：" + showPatient.getHumanID());
            cardPOrder.setText("看診日期：" + showPatient.getOrderDate());
            String birthYear[] = showPatient.getBitrh().split("/");
            int age = calendar.get(Calendar.YEAR) - Integer.parseInt(birthYear[0]);
            cardPBrith.setText("出生日期：" + showPatient.getBitrh() + "(" + age + "歲)");

        });
    }

    private void setPatientCardViewToNull() {
        mainActivity.runOnUiThread(() -> {
            Toast.makeText(mainActivity, "無候診病患", Toast.LENGTH_SHORT).show();
            topRoomNum.setText("--");
            cardPName.setText("姓名：");
            cardPSubject.setText("科別：");
            cardPSex.setText("性別：");
            cardPID.setText("身分證號：");
            cardPOrder.setText("看診日期：");
            cardPBrith.setText("出生日期：");
        });
    }

    private void setRoomBtnListener() { //edit room data
        roomEditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditRoomDialog editRoomDialog = new EditRoomDialog(mainActivity, subjectAdapter, docAdapter, true) { //show edit room dialog 點擊編輯診間按鈕則顯示編輯診間對話框
                    @Override
                    public void refreshData(DocRoom docRoom) {//點擊確定按鈕則更新診間資料至資料庫
                        super.refreshData(docRoom);
                        refreshRoomCardView(docRoom);
                        myDataBase.getDataDao().updateDocRoom(docRoom);
                    }
                };
                editRoomDialog.setRoomID(roomID); //set dialog defult data
                editRoomDialog.setDefultName(getcardRoomName);
                editRoomDialog.setDefultDoc(getcardDoc);
                editRoomDialog.setDefultSubject(getcardsubject);
                editRoomDialog.setDefultAnnounce(getcardAnnounce);
                mainActivity.setMainDialog(editRoomDialog.getDialog());
                mainActivity.getMainDialog().show();//顯示對話框
            }
        });
    }

    private void refreshRoomCardView(DocRoom updateRoom) { //refresh room data
        mainActivity.runOnUiThread(() -> {//更新第三個資料卡ui畫面
            roomID = updateRoom.getId();
            getcardDoc = updateRoom.getDoctor();
            getcardRoomName = updateRoom.getName();
            getcardsubject = updateRoom.getSubject();
            getcardAnnounce = updateRoom.getAnnounce();

            cardRoomName.setText("診間名稱：" + updateRoom.getName());
            cardsubject.setText("科別：" + updateRoom.getSubject());
            cardDoc.setText("醫師：" + updateRoom.getDoctor());
            cardAnnounce.setText("公告：" + updateRoom.getAnnounce());
            setData();
        });
    }

    private void setnextBtnListener() { //next patient
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Thread thread = new Thread(() -> {
                    if(nowNum < patientList.size()) { //set now patient data watched is true,switch next patient
                        myDataBase.getDataDao().updatePatientWatched(patientList.get(nowNum).getId(), 1);
                        nowNum++;
                        setNextPatient();
                    }
                });
                thread.start();
            }
        });
    }

    private void setNextPatient() {
        //if now select patient not checkin or have watched -> select next patient until condition false
        while (nowNum < patientList.size() && (patientList.get(nowNum).getIsCheckIn() == 0 || patientList.get(nowNum).getIsWatched() == 1)) {
            if(patientList.get(nowNum).getIsWatched() != 1) {
                myDataBase.getDataDao().updatePatientPassed(patientList.get(nowNum).getId(), 1);
            }
            nowNum++;
        }

        if(nowNum >= patientList.size()) { //switch patient, refresh ui view
            myDataBase.getDataDao().updateRoomNum(roomID, 0);
            setPatientCardViewToNull();//更新ui畫面
        } else {
            myDataBase.getDataDao().updateRoomNum(roomID, patientList.get(nowNum).getWaitingNum());//更新診間現在號碼
            setPatientCardView(patientList.get(nowNum));//更新ui畫面

        }
    }

    private void setEditBtnListener() { //swtich to specify patient
        editNumBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NumPadDialog numPadDialog = new NumPadDialog(mainActivity, true) { //顯示編輯號碼對話框
                    @Override
                    public void onPosClick(String inputEditText) {
                        super.onPosClick(inputEditText);
                        for (Patient patientItem : patientList) { //if patient is existence and already checkin -> switch to select patient
                            if(Integer.parseInt(inputEditText) == patientItem.getWaitingNum()) {
                                if(patientItem.getIsCheckIn() == 0) {
                                    Toast.makeText(mainActivity, "病患未報到", Toast.LENGTH_SHORT).show();
                                    return;
                                } else {
                                    changePatientList(patientItem);//切換至指定病患
                                    return;
                                }
                            }
                        }
                        Toast.makeText(mainActivity, "查無此號", Toast.LENGTH_SHORT).show();
                        mainActivity.getMainDialog().dismiss();
                    }
                };
                numPadDialog.setDialogTitle("編輯號碼");//設置對話框預設資料
                numPadDialog.setTextTitle("輸入號碼");
                numPadDialog.setShowEditText("");
                mainActivity.setMainDialog(numPadDialog.getDailog());
                mainActivity.getMainDialog().show();//顯示對話框
            }
        });
    }

    private void changePatientList(Patient patientItem) {
        if(patientList.indexOf(patientItem) < nowNum) {
            patientList.add(nowNum, patientItem);
        } else {
            patientList.remove(patientItem);
            patientList.add(nowNum, patientItem);
        }
        setPatientCardView(patientList.get(nowNum));//更新資料卡
        Thread thread = new Thread(() -> myDataBase.getDataDao().updateRoomNum(roomID, patientList.get(nowNum).getWaitingNum()));//更新診間現在號碼至資料庫
        thread.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        myDataBase.close();
    }
}