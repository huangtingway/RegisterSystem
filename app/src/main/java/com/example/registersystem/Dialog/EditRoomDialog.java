package com.example.registersystem.Dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import com.example.registersystem.MainActivity;
import com.example.registersystem.MyDataBase.DocRoom;
import com.example.registersystem.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

public class EditRoomDialog {
    private MaterialAlertDialogBuilder dialogBuilder;
    private MainActivity mainActivity;
    private ArrayAdapter<CharSequence> subjectAdapter, docAdapter;
    private boolean hasDefultData;
    private String defultName, defultDoc, defultSubject, defultAnnounce;
    private int roomID;

    public EditRoomDialog(MainActivity mainActivity, ArrayAdapter<CharSequence> subjectAdapter, ArrayAdapter<CharSequence> docAdapter, boolean hasDefultData) {
        this.mainActivity = mainActivity;
        this.subjectAdapter = subjectAdapter;
        this.docAdapter = docAdapter;
        this.hasDefultData = hasDefultData;
    }

    public Dialog getDialog() { //create dialog
        LayoutInflater inflater = mainActivity.getLayoutInflater();
        View editDocRoom = inflater.inflate(R.layout.edit_doc_room_dialog, null, false);
        TextInputEditText name = editDocRoom.findViewById(R.id.dialog_docroom_enterpatientname);
        Spinner doctor = editDocRoom.findViewById(R.id.dialog_docroom_enterdocname);
        Spinner subject = editDocRoom.findViewById(R.id.dialog_docroom_entercategory);
        TextInputEditText announce = editDocRoom.findViewById(R.id.dialog_docroom_enterannouncement);
        doctor.setAdapter(docAdapter);
        subject.setAdapter(subjectAdapter);

        if(hasDefultData) {
            setDefultData(name, doctor, subject, announce);
        }

        dialogBuilder = new MaterialAlertDialogBuilder(mainActivity, R.style.Dialog);
        dialogBuilder.setTitle("診間編輯");
        dialogBuilder.setView(editDocRoom);
        dialogBuilder.setPositiveButton("確定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) { //if data is correct -> update or add data
                if(doctor.getSelectedItem() == null || doctor.getSelectedItem().toString().equals("")) {
                    Toast.makeText(mainActivity, "請先新增醫師", Toast.LENGTH_SHORT).show();
                } else if(name.getText().toString() == null || name.getText().toString().equals("")) {
                    Toast.makeText(mainActivity, "請輸入診間名稱", Toast.LENGTH_SHORT).show();
                } else {
                    Thread thread = new Thread(() -> {
                        if(hasDefultData) {
                            refreshData(new DocRoom(roomID, name.getText().toString(),
                                    subject.getSelectedItem().toString(),
                                    doctor.getSelectedItem().toString(),
                                    announce.getText().toString(), 0));
                        } else {
                            refreshData(new DocRoom(name.getText().toString(),
                                    subject.getSelectedItem().toString(),
                                    doctor.getSelectedItem().toString(),
                                    announce.getText().toString(), 0));
                        }

                    });
                    thread.start();
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
        return dialogBuilder.create();
    }

    private void setDefultData(TextInputEditText name, Spinner doctor, Spinner subject, TextInputEditText announce) {
        name.setText(defultName);
        doctor.setSelection(docAdapter.getPosition(defultDoc));
        subject.setSelection(subjectAdapter.getPosition(defultSubject));
        announce.setText(defultAnnounce);
    }

    public void refreshData(DocRoom docRoom) {
    }

    public void setRoomID(int roomID) {
        this.roomID = roomID;
    }

    public void setDefultName(String defultName) {
        this.defultName = defultName;
    }

    public void setDefultDoc(String defultDoc) {
        this.defultDoc = defultDoc;
    }

    public void setDefultSubject(String defultSubject) {
        this.defultSubject = defultSubject;
    }

    public void setDefultAnnounce(String defultAnnounce) {
        this.defultAnnounce = defultAnnounce;
    }
}
