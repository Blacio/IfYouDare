package com.blacio.ifyoudaree;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.ByteArrayOutputStream;

public class WorldDareActivity extends MenuActivity {

    private TextView dare,time;
    private Button participate,vote,topview;
    private ImageView img;
    private String str;
    private SharedPreferences prefs;
    private Context context;

    private boolean sw;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private FirebaseDatabase database;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_world_dare);

        context = this;

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            try {
                actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor(SetDataClass.color_rows)));
            }catch (Exception e){e.printStackTrace();}
        }

        super.setIntent(this);
        MenuActivity.sw = 6;

        database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference().child("worldDare");

        dare = (TextView)findViewById(R.id.worldDare);
        participate = (Button)findViewById(R.id.participateButton);
        vote = (Button)findViewById(R.id.voteButton);
        topview = (Button)findViewById(R.id.worldTopButton);
        time = (TextView)findViewById(R.id.timeWorldDare);

        prefs = getSharedPreferences("worldDare", MODE_PRIVATE);
        sw = prefs.getBoolean("SWITCH",false);
        str = prefs.getString("DARE"," ");


        if(!sw){
            participate.setText("PARTICIPATE");
            vote.setVisibility(View.GONE);
            topview.setVisibility(View.GONE);
        }
        else
            participate.setText("MY PHOTO");




        participate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(SetDataClass.checkForProblem(context))
                        if (!sw) {
                            Intent takePictureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                            }
                        } else createPictureDialog();
                else SetDataClass.createProblemDialog(context);

            }
        });


        ref.child("dare").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String string = dataSnapshot.getValue(String.class);

                if (string != null && string.equals("Get ready for the next challenge !!!")) {
                    participate.setVisibility(View.GONE);
                    vote.setVisibility(View.GONE);
                    topview.setVisibility(View.GONE);
                }

                dare.setText(string);

                if(!str.equals(string)){
                    SharedPreferences.Editor editor1 = prefs.edit();
                    editor1.putBoolean("SWITCH",false);
                    editor1.putString("DARE",string);
                    editor1.apply();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        ref.child("time").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String string = dataSnapshot.getValue(String.class);

                time.setText(string);

                if (string != null && string.equals("Session complete!")) {
                    if (!sw) {
                        participate.setVisibility(View.GONE);
                    } else vote.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {


        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");


            String s = "worldTop/" + SetDataClass.myId + "/myWorldPhoto" + ".png";

            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            final StorageReference ref = storageRef.child(s);


            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            if (imageBitmap != null) {
                imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            }
            byte[] data1 = baos.toByteArray();

            UploadTask uploadTask = ref.putBytes(data1);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {

                    Toast.makeText(WorldDareActivity.this, "Uploading failed !",
                            Toast.LENGTH_SHORT).show();
                }

            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    DatabaseReference ref = database.getReference().child("worldDare").child("ids");
                    ref.child(SetDataClass.myId).child("name").setValue(SetDataClass.myName);

                    Toast.makeText(WorldDareActivity.this, "Photo Uploaded !",
                            Toast.LENGTH_SHORT).show();

                    SharedPreferences.Editor editor1 = prefs.edit();
                    editor1.putBoolean("SWITCH",true);
                    editor1.apply();

                    startIntent(true);
                }
            });
        }
    }

    private void startIntent(boolean b){

        Intent i;

        if(b)
            i = new Intent(this, VoteActivity.class);
        else i = new Intent(this,WorldTopActivity.class);


        startActivity(i);
    }

    private void createPictureDialog(){

        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.custom_photo_text);


        LinearLayout linearLayout = (LinearLayout)dialog.findViewById(R.id.linearDialog);
        linearLayout.setBackgroundColor(Color.parseColor(SetDataClass.color_dial));

        TextView dfn = (TextView)dialog.findViewById(R.id.dareFriendName);
        TextView nm = (TextView) dialog.findViewById(R.id.text_dialog);
        img = (ImageView) dialog.findViewById(R.id.dialog_photo);
        Button yes = (Button) dialog.findViewById(R.id.buton_yes);
        Button no = (Button) dialog.findViewById(R.id.buton_no);
        final ProgressBar progressBar = (ProgressBar)dialog.findViewById(R.id.progBarPhoto);

        dfn.setVisibility(View.GONE);
        yes.setVisibility(View.GONE);
        no.setVisibility(View.GONE);
        nm.setVisibility(View.GONE);

        String s = "worldTop/" + SetDataClass.myId + "/myWorldPhoto" + ".png";

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference ref = storageRef.child(s);

        Glide.with(this)
                .using(new FirebaseImageLoader())
                .load(ref)
                .into(new GlideDrawableImageViewTarget(img) {
                    @Override
                    public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> animation) {
                        super.onResourceReady(resource, animation);
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                        progressBar.setVisibility(View.GONE);
                        super.onLoadFailed(e, errorDrawable);
                    }
                });

        img.setMinimumWidth(800);
        img.setMinimumHeight(1100);
        dialog.show();
    }


    public void onVote(View v){

        if(SetDataClass.checkForProblem(context))
            startIntent(true);
        else SetDataClass.createProblemDialog(context);

    }

    public void onWorld(View v){

        if(SetDataClass.checkForProblem(context))
            startIntent(false);
        else SetDataClass.createProblemDialog(context);
    }

    @Override
    protected void onStop() {

        try {
            Glide.clear(img);
        }catch (Exception e){e.printStackTrace();}
        super.onStop();
    }

    @Override
    public void onBackPressed() {

    }

}
