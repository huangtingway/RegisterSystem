package com.example.registersystem;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import org.jetbrains.annotations.NotNull;

public class HostFrag extends Fragment {
    private View hostFrag;
    private FragmentManager manager;
    private FragmentTransaction transaction;
    private ChooseRoom chooseRoomFrag;
    private Doctor doctorFrag;
    private BottomNavigationView bottomBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        hostFrag = inflater.inflate(R.layout.fragment_host, container, false);//取得fragmentManger以切換畫面
        manager = getChildFragmentManager();
        init();
        setFrag();
        setBottomListener();
        return hostFrag;
    }

    private void init() {
        chooseRoomFrag = new ChooseRoom();
        doctorFrag = new Doctor();
        bottomBar = hostFrag.findViewById(R.id.docbottombar);
    }

    private void setFrag() { //first show fragment
        transaction = manager.beginTransaction();
        transaction.add(R.id.manageFragContainer, chooseRoomFrag);
        transaction.commit();
    }

    private void setBottomListener() { //bottom app bar click listener
        bottomBar.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull @NotNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.room://點擊診間按鈕則切換至選擇診間介面
                        transaction = manager.beginTransaction();
                        transaction.replace(R.id.manageFragContainer, chooseRoomFrag);
                        transaction.commit();
                        break;
                    case R.id.doctor://點擊醫師按鈕則切換至選擇醫師介面
                        transaction = manager.beginTransaction();
                        transaction.replace(R.id.manageFragContainer, doctorFrag);
                        transaction.commit();
                        break;
                }
                return true;
            }
        });
    }
}