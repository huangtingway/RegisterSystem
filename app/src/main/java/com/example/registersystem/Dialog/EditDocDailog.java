package com.example.registersystem.Dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import com.example.registersystem.MainActivity;
import com.example.registersystem.MyDataBase.DocData;
import com.example.registersystem.MyDataBase.MyDataBase;
import com.example.registersystem.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

public class EditDocDailog {
    private MaterialAlertDialogBuilder dialogBuilder;
    private MainActivity mainActivity;
    private ArrayAdapter<CharSequence> subjectAdapter;
    private boolean hasDefultData;
    private String defultName, defultSubject;
    private int id;

    public EditDocDailog(MainActivity mainActivity, ArrayAdapter<CharSequence> subjectAdapter, boolean hasDefultData) {
        this.mainActivity = mainActivity;
        this.subjectAdapter = subjectAdapter;
        this.hasDefultData = hasDefultData;
    }

    public Dialog getDialog() {//create dialog
        LayoutInflater layoutInflater = mainActivity.getLayoutInflater();
        View editDoc = layoutInflater.inflate(R.layout.edit_doctor_dialog, null, false);
        TextInputEditText name = editDoc.findViewById(R.id.dialog_doctor_entername);
        Spinner subjectspin = editDoc.findViewById(R.id.dialog_doctor_entercategory);
        subjectspin.setAdapter(subjectAdapter);

        if(hasDefultData) {
            setDefultData(name, subjectspin);
        }

        dialogBuilder = new MaterialAlertDialogBuilder(mainActivity,R.style.Dialog);
        dialogBuilder.setTitle("醫師編輯");
        dialogBuilder.setView(editDoc);
        dialogBuilder.setPositiveButton("確定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(name.getText().toString() != null && !name.getText().toString().equals("")) { //if data is correct -> update or add data
                    Thread thread = new Thread(() -> {
                        if(hasDefultData) {
                            refreshData(new DocData(id, name.getText().toString(), subjectspin.getSelectedItem().toString()));
                        } else {
                            refreshData(new DocData(name.getText().toString(), subjectspin.getSelectedItem().toString()));
                        }
                    });
                    thread.start();
                } else {
                    Toast.makeText(mainActivity, "請輸入醫師姓名", Toast.LENGTH_SHORT).show();
                    mainActivity.getMainDialog().dismiss();
                }
            }
        });
        dialogBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mainActivity.getMainDialog().dismiss();
            }
        });
        if(hasDefultData){
            dialogBuilder.setNeutralButton("刪除", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    deleteData(id);
                }
            });
        }
        return dialogBuilder.create();
    }

    private void setDefultData(TextInputEditText name, Spinner subjectspin) {
        name.setText(defultName);
        subjectspin.setSelection(subjectAdapter.getPosition(defultSubject));
    }

    public void refreshData(DocData docData) {
    }

    public void deleteData(int id) {
    }

    public void setDefultName(String defultName) {
        this.defultName = defultName;
    }

    public void setDefultSubject(String defultSubject) {
        this.defultSubject = defultSubject;
    }

    public void setId(int id) {
        this.id = id;
    }
}
