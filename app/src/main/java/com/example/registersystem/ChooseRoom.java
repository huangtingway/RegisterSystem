package com.example.registersystem;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.example.registersystem.Dialog.EditRoomDialog;
import com.example.registersystem.MyDataBase.DocData;
import com.example.registersystem.MyDataBase.DocRoom;
import com.example.registersystem.MyDataBase.MyDataBase;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChooseRoom extends Fragment {
    private MainActivity mainActivity;
    private View chooseRoom;
    private FragmentTransaction transaction;
    private MyDataBase myDataBase;
    private StartFrag startFrag;

    private ManageRoom manageRoom;
    private ListView listView;//顯示診間清單物件
    private MaterialToolbar topBar;
    private SimpleAdapter simpleAdapter;//診間清單調變器
    private String from[] = {"room1", "category", "docname", "announcement"};//診間清單元件來源id名稱
    private int to[] = {R.id.room1, R.id.category, R.id.docname, R.id.announcement};//診間清單元件來源id數值
    private ArrayList<HashMap<String, String>> data;//診間清單資料
    private List<DocRoom> docRoomList;//診間物件list
    private List<DocData> docDataList;//醫師物件list
    private ArrayList<CharSequence> docName;//醫師物件arraylist(下拉選單用)
    private FloatingActionButton floatingActionButton;
    private ArrayAdapter<CharSequence> docAdapter, subjectAdapter;//醫師、科別調變器(下拉選單用)

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
        chooseRoom = inflater.inflate(R.layout.fragment_choose_room, container, false);
        init();//初始化全域變數
        setTopAppBar();//設置頂部導覽列監聽器
        setFAB();//設置新增診間按鈕
        setListview();//初始化診間清單
        setData();//初始化診間清單資料
        setListListener();//設置診間清單點擊監聽器
        return chooseRoom;
    }

    private void init() {
        topBar = mainActivity.findViewById(R.id.mainTopBar);
        listView = chooseRoom.findViewById(R.id.roomlist);
        floatingActionButton = chooseRoom.findViewById(R.id.floatbtn);
        startFrag = new StartFrag();
        docRoomList = new ArrayList<>();
        docDataList = new ArrayList<>();
        manageRoom = new ManageRoom();
        docName = new ArrayList<>();
        docAdapter = new ArrayAdapter<>(mainActivity, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, docName);
        subjectAdapter = ArrayAdapter.createFromResource(mainActivity, R.array.subjectList, com.google.android.material.R.layout.support_simple_spinner_dropdown_item);
        myDataBase = MyDataBase.getInstance(mainActivity);
    }

    private void setTopAppBar() {
        topBar.getMenu().clear();
        topBar.inflateMenu(R.menu.main_top_menu);
        topBar.setNavigationIcon(R.drawable.ic_home);//設置導覽列icon
        topBar.setTitle("診間管理");//設置導覽列標題
        topBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {//導覽列icon被點擊則返回起始介面
                transaction = mainActivity.getManager().beginTransaction();
                transaction.replace(R.id.mainFrag, startFrag);
                transaction.commit();
            }
        });
    }

    private void setFAB() { //add room
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditRoomDialog editRoomDialog = new EditRoomDialog(mainActivity, subjectAdapter, docAdapter, false) {//show dialog 顯示新增診間對話框
                    @Override
                    public void refreshData(DocRoom docRoom) { //點擊確定按鈕更新診間資料至資料庫
                        super.refreshData(docRoom);
                        myDataBase.getDataDao().addDocRoom(docRoom);
                        setData();
                    }
                };
                mainActivity.setMainDialog(editRoomDialog.getDialog());
                mainActivity.getMainDialog().show();//顯示對話框
            }
        });
    }

    private void setListview() {
        data = new ArrayList<>();
        simpleAdapter = new SimpleAdapter(mainActivity, data, R.layout.roomlistitem, from, to);
        listView.setAdapter(simpleAdapter);
    }

    private void setData() { //list view data
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                data.clear(); //清除診間及醫師資料
                docName.clear();
                docRoomList = myDataBase.getDataDao().getDocRoom();//重新取得診間、醫師資料
                docDataList = myDataBase.getDataDao().getDoc();
                for (DocRoom itemRoom : docRoomList) {//設置診間清單資料
                    HashMap<String, String> item = new HashMap<>();
                    item.put("room1", itemRoom.getName());
                    item.put("category", "科別：" + itemRoom.getSubject());
                    item.put("docname", "醫師：" + itemRoom.getDoctor());
                    item.put("announcement", "公告：" + itemRoom.getAnnounce());
                    data.add(item);
                }

                for (DocData docdata : docDataList) {//設置醫師資料
                    docName.add(docdata.getName());
                }
                mainActivity.runOnUiThread(() -> {//更新ui畫面
                    simpleAdapter.notifyDataSetChanged();
                    docAdapter.notifyDataSetChanged();
                });
            }
        });
        thread.start();
    }

    private void setListListener() { //list item click -> to manageRoom fragment
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) { //send defult data
                Bundle sendData = new Bundle();
                sendData.putInt("id", docRoomList.get(position).getId());
                sendData.putString("roomName", docRoomList.get(position).getName());
                sendData.putString("subject", docRoomList.get(position).getSubject());
                sendData.putString("doc", docRoomList.get(position).getDoctor());
                sendData.putString("announce", docRoomList.get(position).getAnnounce());
                sendData.putInt("watingNum", docRoomList.get(position).getNowNum());
                getParentFragmentManager().setFragmentResult("chooseRoomData", sendData);
                transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.manageFragContainer, manageRoom);
                transaction.addToBackStack(null);
                transaction.commit();//切換畫面
            }
        });
    }

    @Override
    public void onPause() {
        myDataBase.close();
        super.onPause();
    }
}