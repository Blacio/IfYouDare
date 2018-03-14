package com.blacio.ifyoudaree;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;

public class MenuActivity extends AppCompatActivity {

    private Context context;
    protected static int sw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_items, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


            switch (item.getItemId()) {
                case R.id.friends_menu: {
                    startIntent(1);
                    return true;
                }
                case R.id.profile_menu: {
                    startIntent(0);
                    return true;
                }
                case R.id.dares_received_menu: {
                    startIntent(3);
                    return true;
                }
                case R.id.dares_sent_menu: {
                    startIntent(2);
                    return true;
                }
                case R.id.top_scores: {
                    startIntent(5);
                    return true;
                }
                case R.id.world_contest: {
                    startIntent(6);
                    return true;
                }
                case R.id.info_sets: {
                    startIntent(7);
                    return true;
                }
                case R.id.sign_out: {
                    FirebaseAuth.getInstance().signOut();
                    LoginManager.getInstance().logOut();
                    startIntent(4);
                    return true;
                }
                default:
                    return super.onOptionsItemSelected(item);
            }
    }

    private void startIntent(int i){


        if(sw!=i) {

            switch (i) {
                case 0: {
                    Intent intent = new Intent(context, UserData.class);
                    startActivity(intent);
                    break;
                }
                case 1: {
                    Intent intent = new Intent(context, FriendsActivity.class);
                    startActivity(intent);
                    break;
                }
                case 2: {
                    Intent intent = new Intent(context, SentActivity.class);
                    startActivity(intent);
                    break;
                }
                case 3: {
                    Intent intent = new Intent(context, ReceivedActivity.class);
                    startActivity(intent);
                    break;
                }
                case 4: {
                    Intent intent = new Intent(context, LogIn.class);
                    intent.putExtra("Rst",true);
                    startActivity(intent);
                    break;
                }
                case 5: {
                    Intent intent = new Intent(context, SubmitActivity.class);
                    startActivity(intent);
                    break;
                }
                case 6: {
                    Intent intent = new Intent(context, WorldDareActivity.class);
                    startActivity(intent);
                    break;
                }
                case 7: {
                    Intent intent = new Intent(context, InstructionActivity.class);
                    startActivity(intent);
                    break;
                }
                default:
            }
        }
        }

    protected void setIntent(Context context){
        this.context = context;
    }
}
