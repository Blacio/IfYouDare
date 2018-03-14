package com.blacio.ifyoudaree;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

public class SubmitActivity extends MenuActivity {

    private ProgressBar pg;
    private Context context;
    private ActionBar actionBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit);

        actionBar = getSupportActionBar();
        if (actionBar != null) {
            try {
                actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor(SetDataClass.color_rows)));
            }catch (Exception e){e.printStackTrace();}
        }

        super.setIntent(this);
        MenuActivity.sw = 5;
        context = this;


        pg = (ProgressBar)findViewById(R.id.progB);
        pg.setVisibility(View.GONE);
    }

    public void onSubmit(View v){

        if(SetDataClass.checkForProblem(context)) {

            pg.setVisibility(View.VISIBLE);

            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference ref = database.getReference().child("top").child(SetDataClass.myId);

            ref.child("name").setValue(SetDataClass.myName);
            ref.child("score").setValue("" + Long.parseLong(SetDataClass.myScore));

            String s = "profilePhotos/" + SetDataClass.myId + "/profilePic" + ".png";

            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            StorageReference reference = storageRef.child(s);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            SetDataClass.bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] data1 = baos.toByteArray();

            UploadTask uploadTask = reference.putBytes(data1);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {


                    Toast.makeText(SubmitActivity.this, "Sumbit failed !",
                            Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    Toast.makeText(SubmitActivity.this, "Submit complete !\nYour score is: " + SetDataClass.myScore,
                            Toast.LENGTH_SHORT).show();

                }
            });

            pg.setVisibility(View.GONE);

            Intent i = new Intent(this, TopActivity.class);
            startActivity(i);
        }
        else SetDataClass.createProblemDialog(context);
    }

    @Override
    public void onBackPressed() {

    }
}
