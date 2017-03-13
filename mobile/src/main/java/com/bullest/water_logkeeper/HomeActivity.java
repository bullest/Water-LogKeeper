package com.bullest.water_logkeeper;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.Time;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;

import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar;
import com.akexorcist.roundcornerprogressbar.TextRoundCornerProgressBar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

public class HomeActivity extends AppCompatActivity {

    Button button100;
    Button button200;
    Button button300;
    Button button400;
    Button button_confirm;
    EditText volume;
    TextRoundCornerProgressBar progress;

    SharedPreferences sharedPreferences;

    static final String KEY_CUSTOMIEZED_VOLUME = "customized_volume";
    static final String KEY_DATA = "data";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        button100 = (Button) findViewById(R.id.button_100);
        button200 = (Button) findViewById(R.id.button_200);
        button300 = (Button) findViewById(R.id.button_300);
        button400 = (Button) findViewById(R.id.button_400);
        progress = (TextRoundCornerProgressBar) findViewById(R.id.progressBar);

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
                if (charSequence.length() != 0){
                    button_confirm.setEnabled(true);
                } else {
                    button_confirm.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        button100.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar now = Calendar.getInstance();

                final int amount = 100;

                EventBus.getDefault().post(new AddRecordEvent(amount, now));
                Log.d("Event", "add " + amount + "mL at" + now.getTime().toString());

                Snackbar.make(view, amount + "mL water is added", Snackbar.LENGTH_LONG)
                        .setAction("Undo", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                EventBus.getDefault().post(new DeleteRecordEvent());
                            }
                        }).show();
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
                Log.d("Event", "add " + amount + "mL at" + now.getTime().toString());

                Snackbar.make(view, amount + "mL water is added", Snackbar.LENGTH_LONG)
                        .setAction("Undo", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                EventBus.getDefault().post(new DeleteRecordEvent());
                            }
                        }).show();
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
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAddAmountEvent(AddRecordEvent event) {
        WaterRecord record = new WaterRecord(event.amount, event.time.getTimeInMillis());
        record.save();
        updateDailyProgress();
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

        Calendar now = Calendar.getInstance();
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


}
