package com.example.registersystem;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import org.jetbrains.annotations.NotNull;

public class StartFrag extends Fragment {
    private MainActivity mainActivity;
    private View startFrag;
    private MaterialButton chooseGuest, chooseOwner;
    private FragmentManager manager;
    private FragmentTransaction transaction;
    private CustomFrag customFrag;
    private HostFrag hostFrag;
    private MaterialToolbar topBar;

    @Override
    public void onAttach(@NonNull @NotNull Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        startFrag = inflater.inflate(R.layout.fragment_start, container, false);
        init();
        setBtnListener();
        return startFrag;
    }

    private void init() {//取得fragmentManger以切換畫面
        manager = mainActivity.getSupportFragmentManager();
        topBar = mainActivity.findViewById(R.id.mainTopBar);
        chooseGuest = startFrag.findViewById(R.id.guestbtn);
        chooseOwner = startFrag.findViewById(R.id.ownerbtn);
        customFrag = new CustomFrag();
        hostFrag = new HostFrag();
        topBar.getMenu().clear();
        topBar.inflateMenu(R.menu.main_top_menu);
        topBar.setNavigationIcon(R.drawable.ic_topbaricon);
        topBar.setTitle(null);
    }

    private void setBtnListener() { //switch fragment
        chooseGuest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transaction = manager.beginTransaction();
                transaction.replace(R.id.mainFrag, customFrag);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        chooseOwner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {//點擊管理端按鈕則切換至管理端介面
                transaction = manager.beginTransaction();
                transaction.replace(R.id.mainFrag, hostFrag);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
    }
}