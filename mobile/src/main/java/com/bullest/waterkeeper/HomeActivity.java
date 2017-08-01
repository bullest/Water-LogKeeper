package com.bullest.waterkeeper;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.RemoteInput;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.akexorcist.roundcornerprogressbar.TextRoundCornerProgressBar;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Calendar;
import java.util.List;
import java.util.Random;

import static android.R.attr.data;
import static android.preference.PreferenceManager.getDefaultSharedPreferences;

public class HomeActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    Button button100;
    Button button250;
    Button button400;
    Button button_confirm;
    EditText volume;
    TextRoundCornerProgressBar progress;
    CardView signInCard;

    SharedPreferences sharedPreferences;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    static final String KEY_CUSTOMIEZED_VOLUME = "customized_volume";
    static final String KEY_DATA = "data";
    public static final int RC_SIGN_IN = 1;
    public String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        configAddButtons();
        configCustomAddButtons();
        configSignIn();
        configProgress();
    }

    private void configProgress() {
        progress = (TextRoundCornerProgressBar) findViewById(R.id.progressBar);
    }

    private void configSignIn() {
        signInCard = (CardView) findViewById(R.id.sign_in_card);

        uid = this.getSharedPreferences("Login", 0).getString("uid", null);

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        if (uid == null){
            signInCard.setVisibility(View.INVISIBLE);
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken("533414206515-4mpbq6inf7mbma9j78l9i06epamjo0ic.apps.googleusercontent.com")
                    .requestEmail()
                    .build();

            // Build a GoogleApiClient with access to the Google Sign-In API and the
            // options specified by gso.
            final GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .enableAutoManage(this, this)
                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                    .build();

            findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                    startActivityForResult(signInIntent, RC_SIGN_IN);
                }
            });

            mAuth = FirebaseAuth.getInstance();
            mAuthListener = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                }
            };
        }
    }

    private void configCustomAddButtons() {
        button_confirm = (Button) findViewById(R.id.button_confirm);
        volume = (EditText) findViewById(R.id.editable_volume);
        sharedPreferences = getSharedPreferences(KEY_DATA, MODE_PRIVATE);

        String lastVolume = sharedPreferences.getString(KEY_CUSTOMIEZED_VOLUME, "");
        if (!lastVolume.isEmpty()) {
            volume.setText(lastVolume);
            volume.setSelection(lastVolume.length(), lastVolume.length());
        }

        volume.setText(sharedPreferences.getString(KEY_CUSTOMIEZED_VOLUME, ""));

        if (!volume.getText().toString().isEmpty()) {
            button_confirm.setEnabled(true);
        } else {
            button_confirm.setEnabled(false);
        }

        volume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                volume.setCursorVisible(true);
            }
        });

        volume.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() != 0) {
                    button_confirm.setEnabled(true);
                } else {
                    button_confirm.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        button_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar now = Calendar.getInstance();

                final int amount = Integer.parseInt(volume.getText().toString());

                SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
                editor.putString("customized_volume", volume.getText().toString());
                editor.commit();

                EventBus.getDefault().post(new AddRecordEvent(amount, now));

                Snackbar.make(view, amount + "mL water is added", Snackbar.LENGTH_SHORT)
                        .setAction(R.string.undo, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                EventBus.getDefault().post(new DeleteRecordEvent());
                            }
                        }).show();
            }
        });
    }

    private void configAddButtons() {

        button100 = (Button) findViewById(R.id.button_100);
        button250 = (Button) findViewById(R.id.button_250);
        button400 = (Button) findViewById(R.id.button_400);

        button100.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar now = Calendar.getInstance();

                final int amount = 100;

                EventBus.getDefault().post(new AddRecordEvent(amount, now));

                Snackbar.make(view, amount + "mL water is added", Snackbar.LENGTH_SHORT)
                        .setAction(R.string.undo, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                EventBus.getDefault().post(new DeleteRecordEvent());
                            }
                        }).show();
            }
        });

        button250.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar now = Calendar.getInstance();

                final int amount = 250;

                EventBus.getDefault().post(new AddRecordEvent(amount, now));

                Snackbar.make(view, amount + "mL water is added", Snackbar.LENGTH_SHORT)
                        .setAction(R.string.undo, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                EventBus.getDefault().post(new DeleteRecordEvent());
                            }
                        }).show();
            }
        });

        button400.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar now = Calendar.getInstance();

                final int amount = 400;

                EventBus.getDefault().post(new AddRecordEvent(amount, now));

                Snackbar.make(view, amount + "mL water is added", Snackbar.LENGTH_SHORT)
                        .setAction(R.string.undo, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                EventBus.getDefault().post(new DeleteRecordEvent());
                            }
                        }).show();
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d("GoogleSignIn", "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            firebaseAuthWithGoogle(acct);
            signInCard.setVisibility(View.INVISIBLE);
        } else {
            // Signed out, show unauthenticated UI.
        }
    }

    private void firebaseAuthWithGoogle(final GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d("GoogleSignIn", "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w("GoogleSignIn", "signInWithCredential", task.getException());
                            Toast.makeText(HomeActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            SharedPreferences sp = getSharedPreferences("Login", 0);
                            SharedPreferences.Editor ed = sp.edit();
                            ed.putString("uid", account.getId());
                            ed.commit();
                        }
                        // ...
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        if (uid == null) {
            mAuth.addAuthStateListener(mAuthListener);
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        updateDailyWaterProgress();
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAddAmountEvent(AddRecordEvent event) {
        WaterRecord record = new WaterRecord(event.amount, event.time.getTimeInMillis());
        record.save();
        Log.d("WaterKeeper", "water: " + event.amount);
//        DatabaseReference database = FirebaseDatabase.getInstance().getReference("users");
//        database.child("user").child(uid).setValue(uid);
        updateDailyProgress();

        scheduleNotification(getNotification());
    }

    private void scheduleNotification(Notification notificaion) {
        AlarmManager alarmMgr;
        PendingIntent alarmIntent;

        Intent notificationIntent = new Intent(this, NotificationPublisher.class);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, 999);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, notificaion);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        alarmMgr = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);

        Calendar now = Calendar.getInstance();

        int hour = now.get(Calendar.HOUR_OF_DAY);
        if (hour<20) {
            int min = new Random().nextInt(60);
            min += 60;
            alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + min*60*1000, pendingIntent);
        } else {
            alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + 12*60*60*1000, pendingIntent);        }

    }

    private Notification getNotification() {

        final String KEY_TEXT_REPLY = "key_text_reply";

        Intent notificationResponseIntent = new Intent(this, HomeActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationResponseIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Dismiss Action
        Intent dismissIntent = new Intent(this, DismissReciever.class);
        PendingIntent dismissPendingIntent = PendingIntent.getBroadcast(this, 0, dismissIntent, 0);
        NotificationCompat.Action dismissAction =
                new NotificationCompat.Action.Builder(R.drawable.ic_clear_black_24dp, "Dismiss", dismissPendingIntent)
                .build();

        // TODO: 7/25/17 Implement snoozeAction. How to make it snooze?
        Intent snoozeIntent = new Intent(this, SnoozeReciever.class);
        PendingIntent snoozePendingIntent = PendingIntent.getBroadcast(this, 0, snoozeIntent, 0);
        NotificationCompat.Action snoozeAction =
                new NotificationCompat.Action.Builder(R.drawable.ic_notifications_black_24dp, "Snooze", snoozePendingIntent)
                .build();

        // TODO: Implement Direct reply; the flow of the digest the input is not finished
        Intent replyIntent = new Intent(this, ReplyReciever.class);
        PendingIntent replyPendingIntent = PendingIntent.getBroadcast(this, 0, replyIntent, 0);
        RemoteInput remoteInput = new RemoteInput.Builder(KEY_TEXT_REPLY)
                .setLabel(getString(R.string.notification_input_hint))
                .build();
        NotificationCompat.Action replyAction =
                new NotificationCompat.Action.Builder(R.drawable.ic_assignment_black_24dp, "Water Log", replyPendingIntent)
                        .addRemoteInput(remoteInput)
                        .build();

        String[] notificationMessageArray = getApplicationContext().getResources().getStringArray(R.array.notificaion_message);

        Notification notification = new NotificationCompat.Builder(HomeActivity.this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Time to Drink")
                .setContentText(notificationMessageArray[new Random().nextInt(notificationMessageArray.length)])
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
//                .setColor(getResources().getColor(R.color.colorPrimary))
//                .addAction(replyAction)
                .addAction(dismissAction)
                .setSmallIcon(R.drawable.ic_local_drink_lime_600_24dp)
                .setVibrate(new long[] { 500, 300, 400, 500, 1000})
                .setStyle(new android.support.v4.app.NotificationCompat.InboxStyle())
                //        { delay, vibrate, sleep, vibrate, sleep } pattern
                .setLights(Color.YELLOW, 500, 2000)
                .setSound(Uri.parse("android.resource://" + getBaseContext().getPackageName() + "/" + R.raw.notification_sound))
                .build();

        return notification;
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onDeleteEvent(DeleteRecordEvent event) {
    }

    public void updateDailyProgress(){
        updateDailyWaterProgress();
        updateDailyTimeProgress();
    }

    private void updateDailyTimeProgress() {
    }

    private void updateDailyWaterProgress() {
        float currentWater = 0;

        Calendar todayTime = Calendar.getInstance();;
        todayTime.set(Calendar.HOUR_OF_DAY, 2);
        todayTime.set(Calendar.MINUTE,0);

        List<WaterRecord> records = WaterRecord.findWithQuery(WaterRecord.class, "Select * from WATER_RECORD where time > ?", "" + todayTime.getTimeInMillis());

        for (WaterRecord record:records) {
            currentWater += record.amount;
        }

        SharedPreferences preference = getDefaultSharedPreferences(HomeActivity.this);

        String amountGoal = preference.getString("goal", "2000");
        int goal = Integer.parseInt(amountGoal);
        progress.setProgress(currentWater/goal);
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
