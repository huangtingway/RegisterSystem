package com.example.registersystem;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.*;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.mediapipe.components.TextureFrameConsumer;
import com.google.mediapipe.formats.proto.LandmarkProto;
import com.google.mediapipe.framework.TextureFrame;
import com.google.mediapipe.solutioncore.CameraInput;
import com.google.mediapipe.solutioncore.ResultListener;
import com.google.mediapipe.solutioncore.SolutionGlSurfaceView;
import com.google.mediapipe.solutions.hands.HandLandmark;
import com.google.mediapipe.solutions.hands.Hands;
import com.google.mediapipe.solutions.hands.HandsOptions;
import com.google.mediapipe.solutions.hands.HandsResult;

public class MainActivity extends AppCompatActivity {
    private Hands hands; //懸浮觸控手部物件
    private static final boolean RUN_ON_GPU = true;//是否使用GPU偵測手部
    private CameraInput cameraInput;//相機物件
    private SolutionGlSurfaceView<HandsResult> glSurfaceView;
    private LandmarkProto.NormalizedLandmark wristLandmark;//手部座標物件

    private float handX, handY, handZ, fingerX, fingerY, fingerZ, fingerBtmX, fingerBtmY; //手掌座標、手指座標、手指根部座標
    private int parentX, parentY;
    //現態指尖、指跟距離；前一時刻指尖、指跟距離；現態手掌距離、前一時刻手掌距離(觸控偵測用)
    private double nowFingerDistance, beforeFingerDistance, beforeHandX, beforeHandY, nowHandX, nowHandY;
    private boolean isActionDown, isMoving, isFingerMoving;//是否點擊螢幕、是否正在移動手部、是否滑動螢幕
    private int cursorX, cursorY;//游標座標
    private float touchX, touchY;//觸控座標
    private WindowManager windowManager;//視窗管理員物件
    private WindowManager.LayoutParams cursorLayout;
    private MotionEvent clickEvent;//觸發觸控物件

    private StartFrag startFrag; //fragment,ui,dialog
    private SettingFrag settingFrag;
    private FragmentManager manager;
    private FragmentTransaction transaction;
    private MaterialToolbar topbar;
    private RelativeLayout mainGroup;
    private ImageView cursor;
    private Dialog mainDialog, secondDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        manager = getSupportFragmentManager();
        init();//初始化全域變數
        setCamera();//初始化影像辨識
        setFrameLayout();//設置相機畫面
        setTopLintener();//設置頂部導覽列
        setFrag();//設置起始ui畫面
        checkPermission();//確認使用者權限
        setWindow();//設置視窗管理員
    }

    private void init() {
        settingFrag = new SettingFrag();
        startFrag = new StartFrag();
        topbar = findViewById(R.id.mainTopBar);
        mainGroup = findViewById(R.id.mainLayout);
        cursor = new ImageView(this);
        cursor.setImageResource(R.drawable.ic_cursor);

        ViewTreeObserver viewTreeObserver = mainGroup.getViewTreeObserver(); //get full screen XY
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mainGroup.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                parentX = mainGroup.getMeasuredWidth();//get full screen XY 取得螢幕像素大小(顯示游標用)
                parentY = mainGroup.getMeasuredHeight();
            }
        });
    }

    private void setCamera() {
        hands = new Hands(this, HandsOptions.builder()//初始化手部物件
                .setStaticImageMode(false)
                .setMaxNumHands(1)
                .setRunOnGpu(RUN_ON_GPU)
                .setMinTrackingConfidence(0.8f)
                .setMinDetectionConfidence(0.8f)
                .build());

        cameraInput = new CameraInput(this);//初始化相機物件
        cameraInput.setNewFrameListener(new TextureFrameConsumer() {
            @Override
            public void onNewFrame(TextureFrame frame) {
                hands.send(frame);
            }
        });

        glSurfaceView = new SolutionGlSurfaceView<>(this, hands.getGlContext(), hands.getGlMajorVersion());//初始化openGL物件
        glSurfaceView.setSolutionResultRenderer(new HandsResultGlRenderer());
        glSurfaceView.setRenderInputImage(true);
        glSurfaceView.post(this::startCamera);
        glSurfaceView.setVisibility(View.VISIBLE);
        hands.setResultListener(new ResultListener<HandsResult>() {//取得回傳手部座標
            @Override
            public void run(HandsResult result) {
                if(!result.multiHandLandmarks().isEmpty()) {
                    wristLandmark = result.multiHandLandmarks().get(0).getLandmarkList().get(HandLandmark.MIDDLE_FINGER_MCP);
                    handX = wristLandmark.getX() * 100;//取手掌座標
                    handY = wristLandmark.getY() * 100;
                    handZ = wristLandmark.getZ() * 100;

                    wristLandmark = result.multiHandLandmarks().get(0).getLandmarkList().get(HandLandmark.INDEX_FINGER_TIP);
                    fingerX = wristLandmark.getX() * 100;//取指尖座標
                    fingerY = wristLandmark.getY() * 100;
                    fingerZ = wristLandmark.getZ() * 100;

                    wristLandmark = result.multiHandLandmarks().get(0).getLandmarkList().get(HandLandmark.INDEX_FINGER_MCP);
                    fingerBtmX = wristLandmark.getX() * 100;//取指跟座標
                    fingerBtmY = wristLandmark.getY() * 100;

                    sendLog(handX, handY, handZ, fingerX, fingerY, fingerZ);//輸出Log
                    setCursor();//顯示游標
                    sendFingerPos();//觸控偵測
                } else {
                    if(isActionDown) { //no hand detect -> click action up
                        isActionDown = false;
                        clickEvent = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
                                MotionEvent.ACTION_UP, 0, 0, 0);
                        setEvent(clickEvent);//觸發鬆開螢幕
                    }
                    if(cursor.getParent() != null) { //no hand detect -> remove cursor icon
                        windowManager.removeView(cursor);//清除游標icon

                    }
                }
                glSurfaceView.setRenderData(result);
                glSurfaceView.requestRender();
            }
        });
    }

    private void startCamera() {
        cameraInput.start(
                this,
                hands.getGlContext(),
                CameraInput.CameraFacing.FRONT,
                glSurfaceView.getWidth(),
                glSurfaceView.getHeight());
    }

    private void setCursor() {
        if(isFingerMoving){ //user action down click -> stop cursor 若正在進行觸控動作則停止移動座
            return;
        }
        cursorX = (int) (((handX - 40) / 35) * parentX); //set cursor position
        cursorY = (int) (((handY - 80) / 25) * parentY);
        cursorLayout.x = cursorX;
        cursorLayout.y = cursorY;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(cursor.getParent() == null) {  //update cursor view
                    windowManager.addView(cursor, cursorLayout);
                }
                windowManager.updateViewLayout(cursor, cursorLayout);
            }
        });
    }

    private void sendFingerPos() {
        beforeFingerDistance = nowFingerDistance; //update finger position
        beforeHandX = nowHandX;//update hand position
        nowHandX = handX;
        beforeHandY = nowHandY;
        nowHandY = handY;

        touchX = cursorX + (parentX / 2);//set touch position
        touchY = cursorY + (parentY / 2);
        nowFingerDistance = Math.sqrt(Math.pow(fingerBtmX - fingerX, 2) + Math.pow(fingerBtmY - fingerY, 2));//計算指尖與指跟距離
        double different = nowFingerDistance - beforeFingerDistance;//計算手指距離變化量
        double handDistance = Math.sqrt(Math.pow(nowHandX - beforeHandX, 2) + Math.pow(nowHandY - beforeHandY, 2));//計算手掌距離變化量
        Log.v("different", "different:" + different + " hand:" + handDistance);//輸出log

        if(handDistance >= 0.85f) { //判斷手部是否正在移動
            isMoving = true;
            Log.v("handmov:", "move");
        } else if(isMoving){
            isMoving = false;
            Log.v("handmov:", "not move");
        }

        if(different <= -1.5){//判斷手指是否正在移動
            isFingerMoving = true;
        }else{
            isFingerMoving = false;
        }

        if(different <= -1.2f && !isActionDown && !isMoving) { //若手指距離變化量<-1.2且手部停止移動且無觸發點擊螢幕，則觸發點擊螢幕

            isActionDown = true;
            clickEvent = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
                    MotionEvent.ACTION_DOWN, touchX, touchY, 0);
            setEvent(clickEvent);//觸發觸控動作
            Log.v("touch", "down");
        }

        if(isActionDown) {//若已觸發點擊螢幕，則觸發滑動螢幕
            clickEvent = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
                    MotionEvent.ACTION_MOVE, touchX, touchY, 0);
            setEvent(clickEvent);//觸發觸控動作
            Log.v("touch", "move");
        }

        if(different >= 0.9f && isActionDown) {//若手指距離變化量>0.9，且已觸發點擊螢幕，則觸發鬆開螢幕並設置點擊螢幕為false

            isActionDown = false;
            clickEvent = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
                    MotionEvent.ACTION_UP, touchX, touchY, 0);
            setEvent(clickEvent);//觸發觸控動作
            Log.v("touch", "up");
        }
    }

    private void setEvent(MotionEvent event) { //send touch event to different screen layer

        runOnUiThread(new Runnable() {
            @Override
            public void run() {//若第二層對話框正在顯示，則對第二層對話框觸發觸控動作

                if(secondDialog != null && secondDialog.isShowing() && mainDialog != null && mainDialog.isShowing()) {
                    event.setLocation(touchX - ((mainDialog.getWindow().getDecorView().getWidth() - secondDialog.getWindow().getDecorView().getWidth()) / 2),
                            touchY - ((parentY - secondDialog.getWindow().getDecorView().getHeight()) / 2));
                    secondDialog.dispatchTouchEvent(event);//觸發觸控
                } else if(mainDialog != null && mainDialog.isShowing()) {
                    event.setLocation(touchX, touchY - ((parentY - mainDialog.getWindow().getDecorView().getHeight()) / 2));//若第一層對話框正在顯示，則對第一層對話框觸發觸控動作
                    mainDialog.dispatchTouchEvent(event);
                } else {
                    dispatchTouchEvent(event); //若兩層對話框皆未顯示，則對主螢幕觸發觸控動作
                }
                Log.v("touch","" + touchX + " "+ touchY);
            }
        });
    }

    private void sendLog(float handX, float handY, float handZ, float fingerX, float fingerY, float fingerZ) {
        Log.v("posPalm", "X:" + handX + " Y:" + handY + " Z:" + handZ);
        Log.v("posFinger", "X:" + fingerX + " Y:" + fingerY + " Z:" + fingerZ);
    }

    private void setFrameLayout() {
        FrameLayout frameLayout = findViewById(R.id.handCamera);
        frameLayout.removeAllViewsInLayout();
        frameLayout.addView(glSurfaceView);
        frameLayout.requestLayout();
    }

    private void setTopLintener() { //top app bar click listener
        topbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                transaction = manager.beginTransaction();
                transaction.replace(R.id.mainFrag, settingFrag);
                transaction.addToBackStack(null);
                transaction.commit();
                return false;
            }
        });
        topbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {//點擊home icon回到起始介面
                transaction = manager.beginTransaction();
                transaction.replace(R.id.mainFrag, startFrag);
                transaction.commit();
            }
        });
    }

    private void setFrag() { //first show fragment
        transaction = manager.beginTransaction();
        transaction.add(R.id.mainFrag, startFrag);
        transaction.commit();
    }

    private void checkPermission() { //request permission
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.SYSTEM_ALERT_WINDOW) == PackageManager.PERMISSION_DENIED) {
            requestPermissions(new String[]{Manifest.permission.SYSTEM_ALERT_WINDOW}, 0);
        }//要求使用者給予允許顯示於螢幕上方權限(顯示游標用)
        if(!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            startActivity(intent);
        }
    }

    private void setWindow() {
        windowManager = getWindowManager();
        cursorLayout = new WindowManager.LayoutParams(80, 80, 0, 0,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                        WindowManager.LayoutParams.FLAG_LOCAL_FOCUS_MODE |
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);//初始化物件
    }

    public void setMainDialog(Dialog mainDialog) {
        this.mainDialog = mainDialog;
    }

    public Dialog getMainDialog() {
        return mainDialog;
    }

    public Dialog getSecondDialog() {
        return secondDialog;
    }

    public void setSecondDialog(Dialog secondDialog) {
        this.secondDialog = secondDialog;
    }

    public FragmentManager getManager() {
        return manager;
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraInput = new CameraInput(this);//重新初始化影像辨識
        cameraInput.setNewFrameListener(new TextureFrameConsumer() {
            @Override
            public void onNewFrame(TextureFrame frame) {
                hands.send(frame);
            }
        });
        glSurfaceView.post(new Runnable() {
            @Override
            public void run() {
                startCamera();
            }
        });
        glSurfaceView.setVisibility(View.GONE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        glSurfaceView.setVisibility(View.VISIBLE);
        cameraInput.close();
        if(cursor.getParent() != null) {
            windowManager.removeView(cursor);
        }
    }

}