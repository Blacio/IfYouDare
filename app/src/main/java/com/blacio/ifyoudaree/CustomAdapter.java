package com.blacio.ifyoudaree;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {

    private String[] names;
    private Bitmap[] photos;
    private String[] status;
    private Context context;


    CustomAdapter(Context context,ProgressBar prgBar){

        this.context = context;

        if(getItemCount()==0) prgBar.setVisibility(View.GONE);

    }

    CustomAdapter(String[] names, Bitmap[] photos,Context context){

        this.names = new String[names.length];
        this.photos = new Bitmap[names.length];

        this.names = names;
        this.photos = photos;

        this.context= context;

    }


    CustomAdapter(String[] names, Bitmap[] photos,String[] status,Context context){

        this.names = new String[names.length];
        this.photos = new Bitmap[names.length];
        this.status = new String[status.length];

        this.names = names;
        this.photos = photos;
        this.status = status;

        this.context= context;
    }


    class ViewHolder extends RecyclerView.ViewHolder {

        TextView friendName;
        ImageView friendPhoto;
        TextView dareStatus;
        TextView numberTop;

        ViewHolder(View itemView) {
            super(itemView);

            friendName = (TextView)itemView.findViewById(R.id.friendDare);
            friendPhoto = (ImageView)itemView.findViewById(R.id.friendDarePicture);
            dareStatus = (TextView)itemView.findViewById(R.id.friendDareStatus);
            numberTop = (TextView)itemView.findViewById(R.id.number_top);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {

                    if (SetDataClass.checkForProblem(context)){

                        int position = getAdapterPosition();

                        if (context instanceof FriendsActivity) {
                            ((FriendsActivity) context).createDialog(position);
                        } else if (context instanceof SentActivity && !SetDataClass.sws) {
                            String s = dareStatus.getText().toString();

                            if (s.equals("SENT"))
                                ((SentActivity) context).createDialog(position, 1);
                            else if (s.equals("CHECK") || s.equals("REFUSED"))
                                ((SentActivity) context).createDialog(position, 2);
                            else if (s.equals("DONE"))
                                ((SentActivity) context).createDialog(position, 3);

                        } else if (context instanceof ReceivedActivity && !SetDataClass.swr) {
                            String s = dareStatus.getText().toString();

                            if (s.equals("NEW"))
                                ((ReceivedActivity) context).createDialog(position, 1);
                            else if (s.equals("CHECKING") || s.equals("REFUSED"))
                                ((ReceivedActivity) context).createDialog(position, 2);
                            else if (s.equals("SOLVED") || s.equals("REJECTED"))
                                ((ReceivedActivity) context).createDialog(position, 3);

                        } else if (context instanceof VoteActivity) {
                            ((VoteActivity) context).createDialog(position);
                        } else if (context instanceof WorldTopActivity) {
                            ((WorldTopActivity) context).createPictureDialog(position);
                        }
                }
                else SetDataClass.createProblemDialog(context);
                }
            });


            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int position = getAdapterPosition();


                    if (context instanceof FriendsActivity)
                        ((FriendsActivity) context).createBlockDialog(position);

                    else if (context instanceof SentActivity) {
                    String x = SetDataClass.ids2.get(position);

                    for (int i = 0; i < SetDataClass.ids2.size(); i++)
                        if (x.equals(SetDataClass.ids2.get(i)))
                            SetDataClass.vec.add(i);

                        SetDataClass.sws=true;
                        Intent i = new Intent(context,SentActivity.class);
                        context.startActivity(i);
                }

                    else if (context instanceof ReceivedActivity) {
                        String x = SetDataClass.ids3.get(position);

                        for (int i = 0; i < SetDataClass.ids3.size(); i++)
                            if (x.equals(SetDataClass.ids3.get(i)))
                                SetDataClass.vec.add(i);

                        SetDataClass.swr=true;
                        Intent i = new Intent(context,ReceivedActivity.class);
                        context.startActivity(i);
                    }

                return true;}
            });
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {

        if(context instanceof TopActivity || context instanceof WorldTopActivity){
            View v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.custom_top_row, viewGroup, false);

            try {
                v.setBackgroundColor(Color.parseColor(SetDataClass.color_rows));
            }catch (Exception e){e.printStackTrace();}

            if((i==0 || i==1 || i==2) && context instanceof WorldTopActivity)
                try {
                    v.setBackgroundColor(Color.parseColor("#C9AC0B"));
                }catch (Exception e){e.printStackTrace();}

            return new ViewHolder(v);
        }

        else if(context instanceof VoteActivity){
            View v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.custom_photo, viewGroup, false);

            try {
                v.setBackgroundColor(Color.parseColor(SetDataClass.color_rows));
            }catch (Exception e){e.printStackTrace();}

            return new ViewHolder(v);
        }

        else {
            View v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.custom_dare_row, viewGroup, false);

            try {
                v.setBackgroundColor(Color.parseColor(SetDataClass.color_rows));
            }catch (Exception e){e.printStackTrace();}

            return new ViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {

        if(context instanceof TopActivity){

            viewHolder.friendName.setText(getCharperLine(names[i]));

            String s = "profilePhotos/" + TopActivity.idTop.get(i) + "/profilePic" + ".png";

            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            StorageReference ref = storageRef.child(s);

            Glide.with(context)
                    .using(new FirebaseImageLoader())
                    .load(ref)
                    .into(viewHolder.friendPhoto);

            viewHolder.numberTop.setText(""+ (i+1));

            try {
                viewHolder.dareStatus.setText(status[i]);
            }catch (Exception e){e.printStackTrace();}
        }

        else if(context instanceof WorldTopActivity){

            viewHolder.friendName.setText(getCharperLine(names[i]));

            String s = "profilePhotos/" + WorldTopActivity.idTop.get(i) + "/profilePic" + ".png";

            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            StorageReference ref = storageRef.child(s);

            Glide.with(context)
                    .using(new FirebaseImageLoader())
                    .load(ref)
                    .into(viewHolder.friendPhoto);

            viewHolder.numberTop.setText(""+ (i+1));

            try {
                viewHolder.dareStatus.setText(status[i] + "\nvotes");
            }catch (Exception e){e.printStackTrace();}
        }

        else if(context instanceof VoteActivity){

            String s = "worldTop/" + VoteActivity.idWorldTop.get(i) + "/myWorldPhoto" + ".png";

            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            StorageReference ref = storageRef.child(s);

            Glide.with(context)
                    .using(new FirebaseImageLoader())
                    .load(ref)
                    .into(viewHolder.friendPhoto);

            viewHolder.friendPhoto.setMinimumHeight(1100);
            viewHolder.friendPhoto.setMinimumWidth(800);
        }

        else {
            viewHolder.friendName.setText(getCharperLine(names[i]));
            try {
                viewHolder.friendPhoto.setImageBitmap(photos[i]);
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                viewHolder.dareStatus.setText(status[i]);
            }catch (Exception e){e.printStackTrace();}
        }
    }

    @Override
    public int getItemCount() {

        if(names!=null)
            return names.length;
        else if(VoteActivity.idWorldTop!=null)
            return VoteActivity.idWorldTop.size();
        else return 0;
    }


    private String getCharperLine(String text){

        StringBuilder sb = new StringBuilder(text);

        if(context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
        {
            int i = 0;

            while ((i = sb.indexOf(" ", i + 7)) != -1) {
                sb.replace(i, i + 1, "\n");
            }
        } else {
            int i = 0;
            while ((i = sb.indexOf(" ", i + 30)) != -1) {
                sb.replace(i, i + 1, "\n");
            }
        }




        return sb.toString();
    }

        }
