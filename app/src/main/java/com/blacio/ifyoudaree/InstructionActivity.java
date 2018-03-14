package com.blacio.ifyoudaree;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.view.View;

public class InstructionActivity extends MenuActivity {

    String[]index;
    private ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instruction);


        actionBar = getSupportActionBar();
        if (actionBar != null) {
            try {
                actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor(SetDataClass.color_rows)));
            }catch (Exception e){e.printStackTrace();}
        }

        super.setIntent(this);
        MenuActivity.sw = 7;


        index = new String[7];
        index[0] = "Blue";
        index[1] = "Yellow";
        index[2] = "Lightgreen";
        index[3] = "Purple";
        index[4] = "Orange";
        index[5] = "Darkgreen";
        index[6] = "Red";

    }


    public void onChangeColor(View view) {
        createDialog(index).show();
    }

    Dialog createDialog(final String[] index) {
        AlertDialog.Builder builder = new AlertDialog.Builder(InstructionActivity.this);
        builder.setTitle("Choose a color for your app")
                .setItems(index, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        setColors(which);
                        dialog.dismiss();
                        reset();

                    }
                });
        return builder.create();

    }

    private void setColors(int colors){
        switch (colors){
            case 1: {
                SetDataClass.color_rows ="#979800";
                SetDataClass.color_dial ="#B3979800";
                break;
            }
            case 2: {
                SetDataClass.color_rows ="#3C722C";
                SetDataClass.color_dial ="#B33C722C";
                break;
            }
            case 3: {
                SetDataClass.color_rows ="#74137A";
                SetDataClass.color_dial ="#B374137A";
                break;
            }
            case 4: {
                SetDataClass.color_rows = "#D28000";
                SetDataClass.color_dial = "#B3D28000";
                break;
            }
            case 5: {
                SetDataClass.color_rows = "#114901";
                SetDataClass.color_dial = "#B3114901";
                break;
            }
            case 6: {
                SetDataClass.color_rows = "#C11717";
                SetDataClass.color_dial = "#B3C11717";
                break;
            }
            default: {
                SetDataClass.color_rows = "#3F51B5";
                SetDataClass.color_dial = "#B33F51B5";
                break;
            }
        }

        SharedPreferences.Editor editor = getSharedPreferences("UserInfo", MODE_PRIVATE).edit();
        editor.putString("myColor1", SetDataClass.color_rows);
        editor.putString("myColor2", SetDataClass.color_dial);
        editor.apply();
    }

    private void reset(){
        this.recreate();
    }

    @Override
    public void onBackPressed() {

    }
}
