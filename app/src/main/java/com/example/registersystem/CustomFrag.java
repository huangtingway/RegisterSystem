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
import com.example.registersystem.Dialog.NumPadDialog;
import com.example.registersystem.Dialog.PatientDialog;
import com.example.registersystem.MyDataBase.DocRoom;
import com.example.registersystem.MyDataBase.MyDataBase;
import com.example.registersystem.MyDataBase.Patient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class CustomFrag extends Fragment {
    private View customView;
    private MainActivity mainActivity;
    private MyDataBase myDataBase;
    private Calendar calendar;
    private String today;

    private ListView waitingList, passedList;//等待清單、過號清單
    private SimpleAdapter waitingListAdapter, passedListAdapter;//等待清單、過號清單調變器
    //等待清單、過號清單元件id名稱
    private String waitingListFrom[] = {"placenum", "customname", "idnume"};
    private String passedListFrom[] = {"placenum", "customname", "idnume"};//等待清單、過號清單元件id數值
    private int waitingListTo[] = {R.id.placenum, R.id.customname, R.id.idnume};
    private int passedListTo[] = {R.id.placenum, R.id.customname, R.id.idnume};
    private ArrayList<HashMap<String, String>> waitingListData;//等待清單資料
    private ArrayList<HashMap<String, String>> passedListData;//過號清單資料

    private int nowRoomIndex;//現在顯示診間指標
    private List<DocRoom> docRoomList;//診間資料
    private List<Patient> patientList;//病患資料
    private ImageButton pageLeft, pageRight;
    private MaterialButton checkInBtn;
    private TextView roomName, roomSubject, roomDoc, roomAnnounce, nowNum, patientName, patientSex, patientBirth, patientID;

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
        customView = inflater.inflate(R.layout.fragmentcustom, container, false);
        init();
        setPageBtnListener();
        setCheckInBtnListener();
        return customView;
    }

    private void init() {
        myDataBase = MyDataBase.getInstance(mainActivity);
        calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH) + 1;
        today = calendar.get(Calendar.YEAR) + "/" + month + "/" + calendar.get(Calendar.DATE);
        pageLeft = customView.findViewById(R.id.leftbtn);
        pageRight = customView.findViewById(R.id.rightbtn);
        roomName = customView.findViewById(R.id.fragmentcustom_room);
        roomSubject = customView.findViewById(R.id.fragmentcustom_category);
        roomDoc = customView.findViewById(R.id.fragmentcustom_doctorname);
        roomAnnounce = customView.findViewById(R.id.fragmentcustom_announcement);
        nowNum = customView.findViewById(R.id.fragmentcustom_number);
        patientName = customView.findViewById(R.id.fragmentcustom_patientname);
        patientSex = customView.findViewById(R.id.fragmentcustom_patientsex);
        patientBirth = customView.findViewById(R.id.fragmentcustom_patientbirthday);
        patientID = customView.findViewById(R.id.fragmentcustom_patientid);
        checkInBtn = customView.findViewById(R.id.checkinbtn);
        waitingList = customView.findViewById(R.id.rightlist);
        passedList = customView.findViewById(R.id.leftlist);
        docRoomList = new ArrayList<>();
        patientList = new ArrayList<>();
        waitingListData = new ArrayList<>();
        passedListData = new ArrayList<>();
        waitingListAdapter = new SimpleAdapter(mainActivity, waitingListData, R.layout.custom3rdcardlist, waitingListFrom, waitingListTo);
        passedListAdapter = new SimpleAdapter(mainActivity, passedListData, R.layout.custom3rdcardlist, passedListFrom, passedListTo);
        waitingList.setAdapter(waitingListAdapter);
        passedList.setAdapter(passedListAdapter);
        getData();
    }

    private void getData() { //get data from dataBase
        Thread thread = new Thread(() -> {
            docRoomList = myDataBase.getDataDao().getDocRoom();
            patientList = myDataBase.getDataDao().getPatientByDate(today);//取今日病患資料
            setTopCard(nowRoomIndex);//取今日病患資料
            setThirdCard();//設置第三章資料卡
        });
        thread.start();
    }

    private void setTopCard(int index) { //set room information card data
        if(docRoomList.size() == 0) {//若無診間資料則設置資料卡為無資料模式
            setTopCardToNull();
            return;
        }
        mainActivity.runOnUiThread(() -> {//設定資料卡各元件資料
            roomName.setText(docRoomList.get(index).getName());
            roomSubject.setText(docRoomList.get(index).getSubject());
            roomDoc.setText("醫師：" + docRoomList.get(index).getDoctor());
            roomAnnounce.setText("公告：" + docRoomList.get(index).getAnnounce());
        });
        setsecondCard(index);
    }

    private void setTopCardToNull() {
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                roomName.setText("--");
                roomSubject.setText("--");
                roomDoc.setText("醫師：--");
                roomAnnounce.setText("公告：--");
            }
        });
    }

    private void setsecondCard(int index) { //set now watching patient card data
        if(docRoomList.size() == 0) {
            setTopCardToNull();//若無診間資料則設置資料卡為無資料模式
        } else {
            Thread thread = new Thread(() -> {
                Patient patient = myDataBase.getDataDao().getPatientByWaitingNum(docRoomList.get(index).getDoctor(), docRoomList.get(index).getNowNum());//取得現在叫號病患
                if(patient != null) {
                    mainActivity.runOnUiThread(() -> {//設定資料卡各元件資料
                        nowNum.setText("" + docRoomList.get(index).getNowNum());
                        patientName.setText("姓名：" + patient.getName());
                        patientSex.setText("性別：" + patient.getSex());
                        patientBirth.setText("出生日期：" + patient.getBitrh());
                        patientID.setText("身分證號：" + patient.getHumanID());
                    });
                } else {
                    setSecondCardToNull();//若無病患資料則設置資料卡為無資料模式
                }
            });
            thread.start();
        }
    }

    private void setSecondCardToNull() {
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                nowNum.setText("--");
                patientName.setText("姓名：---");
                patientSex.setText("性別：-");
                patientBirth.setText("出生日期：---/--/--");
                patientID.setText("身分證號：----");
            }
        });
    }

    private void setThirdCard() { //set waiting,passed list card
        if(patientList.size() == 0 || docRoomList.size() == 0) {//若無診間資料或病患資料則不設置第三個資料卡
            return;
        }
        waitingListData.clear();//清空等待清單及過號清單
        passedListData.clear();
        for (Patient patient : patientList) {//重新填入清單資料
            //if patient was checkin and not watched and "not passed" -> add patientitem to waiting list
            if(patient.getBelongDoc().equals(docRoomList.get(nowRoomIndex).getDoctor()) && patient.getIsCheckIn() == 1 && patient.getIsWatched() == 0 &&
                    patient.getIsPassed() == 0) {
                HashMap<String, String> items = new HashMap<>();//新增病患資料至等待清單
                items.put("placenum", "" + patient.getWaitingNum());
                items.put("customname", "" + patient.getName());
                items.put("idnume", "" + patient.getHumanID());
                waitingListData.add(items);
            }
            //if patient was checkin and not watched and "already passed" -> add patientitem to passed list
            if(patient.getBelongDoc().equals(docRoomList.get(nowRoomIndex).getDoctor()) && patient.getIsCheckIn() == 1 && patient.getIsWatched() == 0 &&
                    patient.getIsPassed() == 1) {//新增病患資料至過號清單
                Log.v("patient", "watch:" + patient.getIsWatched());
                HashMap<String, String> items = new HashMap<>();
                items.put("placenum", "" + patient.getWaitingNum());
                items.put("customname", "" + patient.getName());
                items.put("idnume", "" + patient.getHumanID());
                passedListData.add(items);
            }
        }
        mainActivity.runOnUiThread(() -> {
            waitingListAdapter.notifyDataSetChanged();
            passedListAdapter.notifyDataSetChanged();
        });
    }

    private void setPageBtnListener() {
        pageLeft.setOnClickListener(new View.OnClickListener() { //switch to previous room
            @Override
            public void onClick(View v) {
                if(nowRoomIndex <= 0) {//若指標已指向診間陣列最小值，則重新設置指標指向診間陣列最大值
                    nowRoomIndex = docRoomList.size() - 1;
                } else {
                    nowRoomIndex--;//現在顯示診間指標-1
                }
                setTopCard(nowRoomIndex);//設置資料卡
                setThirdCard();
            }
        });
        pageRight.setOnClickListener(new View.OnClickListener() { //switch to next room
            @Override
            public void onClick(View v) {
                if(nowRoomIndex >= docRoomList.size() - 1) {//若指標已指向診間陣列最大值，則重新設置指標指向診間陣列最小值
                    nowRoomIndex = 0;
                } else {
                    nowRoomIndex++;//現在顯示診間指標+1
                }
                setTopCard(nowRoomIndex);//設置資料卡
                setThirdCard();
            }
        });
    }

    private void setCheckInBtnListener() {
        checkInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NumPadDialog numPadDialog = new NumPadDialog(mainActivity, true) { //show patient checkin dialog
                    @Override
                    public void onPosClick(String inputEditText) {
                        super.onPosClick(inputEditText);
                        for (Patient patient : patientList) { //if patient is existence and not watched -> show confirm data dialog
                            if(patient.getHumanID().equals(inputEditText) && patient.getIsWatched() == 0) {
                                mainActivity.getMainDialog().dismiss();
                                showConfirmDialog(patient);
                                return;
                            } else if(patient.getHumanID().equals(inputEditText) && patient.getIsWatched() == 1) {
                                Toast.makeText(mainActivity, "您已看診", Toast.LENGTH_SHORT).show();//若病患以看診，顯示以看診訊息
                                return;
                            }
                        }
                        Toast.makeText(mainActivity, "查無此號", Toast.LENGTH_SHORT).show();//若無病患則顯示查無此號
                        mainActivity.getMainDialog().dismiss();//關閉對話框
                    }
                };
                numPadDialog.setDialogTitle("報到");//設置鍵盤對話框初始資料
                numPadDialog.setTextTitle("身分證號：");
                numPadDialog.setShowEditText("");
                mainActivity.setMainDialog(numPadDialog.getDailog());
                mainActivity.getMainDialog().show();
                setSecondDialog(numPadDialog.getEditText());
            }
        });
    }

    private void setSecondDialog(TextInputEditText dialogEditText) { //select humanID first char
        MaterialAlertDialogBuilder secondDialog = new MaterialAlertDialogBuilder(mainActivity, R.style.Dialog);//設置選擇戶籍地對話框
        secondDialog.setTitle("選擇戶籍地");
        secondDialog.setSingleChoiceItems(new CharSequence[]{"台北市", "臺中市", "基隆市", "臺南市", "高雄市", "新北市",
                "宜蘭縣", "桃園市", "嘉義市", "新竹市", "苗栗縣", "南投縣", "彰化縣", "新竹市", "雲林縣", "嘉義縣",
                "屏東縣", "花蓮縣", "台東縣", "金門縣", "澎湖縣", "連江縣"}, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {//對話框按鈕已選擇，設置輸入框首字母
                    case 0:
                        dialogEditText.setText("A");
                        break;
                    case 1:
                        dialogEditText.setText("B");
                        break;
                    case 2:
                        dialogEditText.setText("C");
                        break;
                    case 3:
                        dialogEditText.setText("D");
                        break;
                    case 4:
                        dialogEditText.setText("E");
                        break;
                    case 5:
                        dialogEditText.setText("F");
                        break;
                    case 6:
                        dialogEditText.setText("G");
                        break;
                    case 7:
                        dialogEditText.setText("H");
                        break;
                    case 8:
                        dialogEditText.setText("I");
                        break;
                    case 9:
                        dialogEditText.setText("J");
                        break;
                    case 10:
                        dialogEditText.setText("K");
                        break;
                    case 11:
                        dialogEditText.setText("M");
                        break;
                    case 12:
                        dialogEditText.setText("N");
                        break;
                    case 13:
                        dialogEditText.setText("O");
                        break;
                    case 14:
                        dialogEditText.setText("P");
                        break;
                    case 15:
                        dialogEditText.setText("Q");
                        break;
                    case 16:
                        dialogEditText.setText("T");
                        break;
                    case 17:
                        dialogEditText.setText("U");
                        break;
                    case 18:
                        dialogEditText.setText("V");
                        break;
                    case 19:
                        dialogEditText.setText("W");
                        break;
                    case 20:
                        dialogEditText.setText("X");
                        break;
                    case 21:
                        dialogEditText.setText("Z");
                        break;

                }
                mainActivity.getSecondDialog().dismiss();
            }
        });
        mainActivity.setSecondDialog(secondDialog.create());
        mainActivity.getSecondDialog().show();
    }

    private void showConfirmDialog(Patient patient) {
        LayoutInflater inflater = getLayoutInflater();
        View confirmView = inflater.inflate(R.layout.dialog_patient_inform, null, false);//初始化對話框資料
        TextView confirmName = confirmView.findViewById(R.id.patinet_name_inform);
        TextView confirmId = confirmView.findViewById(R.id.patinet_ID_inform);
        TextView confirmSubject = confirmView.findViewById(R.id.patinet_category_inform);
        TextView confirmOrderDate = confirmView.findViewById(R.id.patinet_watchdate_inform);
        TextView confirmBirthDate = confirmView.findViewById(R.id.patinet_birthday_inform);
        TextView confirmDocname = confirmView.findViewById(R.id.patinet_doctor_inform);
        TextView confirmSex = confirmView.findViewById(R.id.patinet_sex_inform);//設置對話框資料

        confirmName.setText(patient.getName());
        confirmId.setText(patient.getHumanID());
        confirmSubject.setText(patient.getOrderSubject());
        confirmOrderDate.setText(patient.getOrderDate());
        confirmBirthDate.setText(patient.getBitrh());
        confirmDocname.setText(patient.getBelongDoc());
        confirmSex.setText(patient.getSex());

        MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(mainActivity, R.style.Dialog);
        alertDialogBuilder.setView(confirmView);
        alertDialogBuilder.setTitle("資料確認");
        alertDialogBuilder.setPositiveButton("確定", new DialogInterface.OnClickListener() { //data confirm -> checkin success
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Thread thread = new Thread(() -> {
                    myDataBase.getDataDao().updatePatientCheck(patient.getId(), 1);;//更新病患已報到
                    getData();//重新取得資料庫資料，更新第三個資料卡
                });
                thread.start();
                mainActivity.getMainDialog().dismiss();
                Toast.makeText(mainActivity, "報到成功", Toast.LENGTH_SHORT).show();//點擊確認則顯示報到成功
            }
        });
        alertDialogBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mainActivity.getMainDialog().dismiss();
                Toast.makeText(mainActivity, "報到失敗", Toast.LENGTH_SHORT).show();//點擊取消則顯示報到失敗
            }
        });
        alertDialogBuilder.setCancelable(false);
        mainActivity.setMainDialog(alertDialogBuilder.create());
        mainActivity.getMainDialog().show();
    }

    @Override
    public void onPause() {
        myDataBase.close();
        super.onPause();
    }
}