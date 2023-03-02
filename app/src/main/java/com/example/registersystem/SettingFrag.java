package com.example.registersystem;

import android.content.Context;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.mediapipe.components.TextureFrameConsumer;
import com.google.mediapipe.formats.proto.LandmarkProto;
import com.google.mediapipe.framework.TextureFrame;
import com.google.mediapipe.solutioncore.CameraInput;
import com.google.mediapipe.solutioncore.SolutionGlSurfaceView;
import com.google.mediapipe.solutions.hands.HandLandmark;
import com.google.mediapipe.solutions.hands.Hands;
import com.google.mediapipe.solutions.hands.HandsOptions;
import com.google.mediapipe.solutions.hands.HandsResult;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;

public class SettingFrag extends Fragment {
    private View settingView;
    private MainActivity mainActivity;
    private TextView text;

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
        settingView = inflater.inflate(R.layout.fragment_setting, container, false);
        init();

        return settingView;
    }

    private void init() {
        text = settingView.findViewById(R.id.text);
        text.setText("set");
    }


}