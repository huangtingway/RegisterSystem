package com.example.registersystem.Dialog;


import android.app.Dialog;

import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.example.registersystem.MainActivity;
import com.example.registersystem.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

public class NumPadDialog {
    private MaterialAlertDialogBuilder dialogBuilder;
    private MainActivity mainActivity;
    private String dialogTitle, textTitle, showEditText;

    private LayoutInflater inflater;
    private View numPadDialog;
    private TextView title;
    private TextInputEditText editText;
    private MaterialButton btnGroup[];
    private boolean hasDefultData;

    public NumPadDialog(MainActivity mainActivity, boolean hasDefultData) {
        this.mainActivity = mainActivity;
        this.hasDefultData = hasDefultData;
    }

    public Dialog getDailog() { //create dialog
        inflater = mainActivity.getLayoutInflater();
        numPadDialog = inflater.inflate(R.layout.dialog_confirm, null, false);
        initView();
        setBtnListener();
        if(hasDefultData) {
            setDefultData();
        }

        dialogBuilder = new MaterialAlertDialogBuilder(mainActivity, R.style.Dialog);
        dialogBuilder.setTitle(dialogTitle);
        dialogBuilder.setPositiveButton("確定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(editText.getText().toString().equals("") || editText.getText().toString() == null) { //if data is correct -> send num result to sub class
                    Toast.makeText(mainActivity, "請輸入正確號碼", Toast.LENGTH_SHORT).show();
                } else {
                    onPosClick(editText.getText().toString());
                }
            }
        });
        dialogBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        dialogBuilder.setView(numPadDialog);
        return dialogBuilder.create();
    }


    private void initView() {
        title = numPadDialog.findViewById(R.id.dialog_confirm_title);
        editText = numPadDialog.findViewById(R.id.dialog_confirm_enteryourid);

        btnGroup = new MaterialButton[12];
        btnGroup[0] = numPadDialog.findViewById(R.id.btn0);
        btnGroup[1] = numPadDialog.findViewById(R.id.btn1);
        btnGroup[2] = numPadDialog.findViewById(R.id.btn2);
        btnGroup[3] = numPadDialog.findViewById(R.id.btn3);
        btnGroup[4] = numPadDialog.findViewById(R.id.btn4);
        btnGroup[5] = numPadDialog.findViewById(R.id.btn5);
        btnGroup[6] = numPadDialog.findViewById(R.id.btn6);
        btnGroup[7] = numPadDialog.findViewById(R.id.btn7);
        btnGroup[8] = numPadDialog.findViewById(R.id.btn8);
        btnGroup[9] = numPadDialog.findViewById(R.id.btn9);
        btnGroup[10] = numPadDialog.findViewById(R.id.btnbcak);
        btnGroup[11] = numPadDialog.findViewById(R.id.btnclear);
    }

    private void setDefultData() {
        title.setText("" + textTitle);
        editText.setText("" + showEditText);
    }


    private void setBtnListener() {
        for (int i = 0; i < 10; i++) {
            String textNum = "" + i;
            btnGroup[i].setOnClickListener(new View.OnClickListener() { //num button
                @Override
                public void onClick(View v) {
                    editText.setText(editText.getText().toString() + textNum);
                }
            });
        }
        btnGroup[10].setOnClickListener(new View.OnClickListener() { //backspace button
            @Override
            public void onClick(View v) {
                String getText = editText.getText().toString();
                StringBuilder showText = new StringBuilder(getText);
                if(showText.length() > 0) {
                    showText.deleteCharAt(getText.length() - 1);
                }
                editText.setText(showText);
            }
        });
        btnGroup[11].setOnClickListener(new View.OnClickListener() { //all clear button
            @Override
            public void onClick(View v) {
                editText.setText(null);
            }
        });
    }

    public void onPosClick(String inputEditText) {

    }

    public void setDialogTitle(String dialogTitle) {
        this.dialogTitle = dialogTitle;
    }

    public void setTextTitle(String textTitle) {
        this.textTitle = textTitle;
    }

    public void setShowEditText(String showEditText) {
        this.showEditText = showEditText;
    }

    public TextInputEditText getEditText() {
        return editText;
    }

    public void setEditText(TextInputEditText editText) {
        this.editText = editText;
    }
}
