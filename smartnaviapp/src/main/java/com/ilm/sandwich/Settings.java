package com.ilm.sandwich;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.ilm.sandwich.tools.Analytics;
import com.ilm.sandwich.tools.Config;

import java.text.DecimalFormat;

/**
 * @author Christian Henke
 *         www.smartnavi-app.com
 */
public class Settings extends Activity implements OnEditorActionListener, OnCheckedChangeListener {

    static DecimalFormat df = new DecimalFormat("0");
    EditText editText;
    CheckBox checkBoxVibration;
    CheckBox checkBoxSatellite;
    CheckBox checkBoxSpeech;
    CheckBox checkBoxGPS;
    CheckBox checkBoxExport;
    CheckBox checkBoxUsageData;
    Spinner mapSpinner;
    private LocationManager mLocationManager;
    private String oldMapSource;
    private String actualMapSource;
    private SubMenu subMenu1;
    private Analytics mAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle(getResources().getString(R.string.tx_15));
        setContentView(R.layout.activity_settings);


        SharedPreferences settings = getSharedPreferences(getPackageName() + "_preferences", MODE_PRIVATE);
        boolean trackingAllowed = settings.getBoolean("nutzdaten", true);
        mAnalytics = new Analytics(trackingAllowed);

        editText = (EditText) findViewById(R.id.editText);
        checkBoxVibration = (CheckBox) findViewById(R.id.checkBoxVibration);
        checkBoxSatellite = (CheckBox) findViewById(R.id.checkBoxSatellite);
        checkBoxSpeech = (CheckBox) findViewById(R.id.checkBoxSpeech);
        checkBoxGPS = (CheckBox) findViewById(R.id.checkBoxGPS);
        final SeekBar seekBarTimer = (SeekBar) findViewById(R.id.seekBarTimer);
        checkBoxExport = (CheckBox) findViewById(R.id.checkBoxExport);
        checkBoxUsageData = (CheckBox) findViewById(R.id.checkBoxUsageData);
        final TextView timerText = (TextView) findViewById(R.id.textTimer);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);


        mapSpinner = (Spinner) findViewById(R.id.spinnerMapProvider);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.mapProvider, R.layout.spinner2);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mapSpinner.setAdapter(adapter);
        mapSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                String chosenMapSource = "GoogleMaps";
                if (arg2 == 0) {
                    if (Splashscreen.PLAYSTORE_VERSION) {
                        chosenMapSource = "GoogleMaps";
                        setMapSource(chosenMapSource);
                    } else {
                        Toast.makeText(Settings.this, getApplicationContext().getResources().getString(R.string.tx_99), Toast.LENGTH_LONG).show();
                        setMapSource("MapQuestOSM");
                        mapSpinner.setSelection(0, true);
                        mapSpinner.dispatchSetSelected(true);
                    }
                } else if (arg2 == 1) {
                    chosenMapSource = "MapQuestOSM";
                    setMapSource(chosenMapSource);
                } else if (arg2 == 2) {
                    chosenMapSource = "MapnikOSM";
                    setMapSource(chosenMapSource);
                }
                mAnalytics.trackEvent("Settings", "mapSource_to_" + chosenMapSource);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });


        String stepLength = settings.getString("step_length", null);
        if (stepLength != null) {
            editText.setText(stepLength);
        }

        String savedMapSource;
        if (Config.usingGoogleMaps) {
            savedMapSource = "GoogleMaps";
        } else {
            savedMapSource = settings.getString("MapSource", "MapQuestOSM");
        }
        oldMapSource = savedMapSource;
        setMapSource(savedMapSource);
        if (savedMapSource.equalsIgnoreCase("GoogleMaps")) {
            mapSpinner.setSelection(0);
        } else if (savedMapSource.equalsIgnoreCase("MapQuestOSM")) {
            mapSpinner.setSelection(1);
        } else if (savedMapSource.equalsIgnoreCase("MapnikOSM")) {
            mapSpinner.setSelection(2);
        }

        boolean vibration = settings.getBoolean("vibration", true);
        checkBoxVibration.setChecked(vibration);

        boolean view = settings.getBoolean("view", false);
        checkBoxSatellite.setChecked(view);

        boolean speech = settings.getBoolean("language", false);
        checkBoxSpeech.setChecked(speech);

        boolean export = settings.getBoolean("export", false);
        checkBoxExport.setChecked(export);

        boolean usageData = settings.getBoolean("nutzdaten", true);
        checkBoxUsageData.setChecked(usageData);

        boolean autocorrect = settings.getBoolean("autocorrect", false);
        checkBoxGPS.setChecked(autocorrect);

        int gpsTimer = settings.getInt("gpstimer", 1);
        seekBarTimer.setEnabled(autocorrect);
        if (autocorrect) {
            seekBarTimer.setThumb(getResources().getDrawable(R.drawable.seek_thumb_normal));
            if (gpsTimer == 0) {
                seekBarTimer.setProgress(0);
                timerText.setText(getApplicationContext().getResources().getString(R.string.tx_75));
            } else if (gpsTimer == 1) {
                seekBarTimer.setProgress(1);
                timerText.setText(getApplicationContext().getResources().getString(R.string.tx_76));
            } else {
                seekBarTimer.setProgress(2);
                timerText.setText(getApplicationContext().getResources().getString(R.string.tx_80));
            }
        } else {
            timerText.setText(getApplicationContext().getResources().getString(R.string.tx_25));
        }

        checkBoxVibration.setOnCheckedChangeListener(this);
        checkBoxSatellite.setOnCheckedChangeListener(this);
        checkBoxSpeech.setOnCheckedChangeListener(this);
        checkBoxExport.setOnCheckedChangeListener(this);
        checkBoxUsageData.setOnCheckedChangeListener(this);

        editText.setOnEditorActionListener(this);
        checkBoxGPS.setOnCheckedChangeListener(this);
        seekBarTimer.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress == 0) {
                    timerText.setText(getApplicationContext().getResources().getString(R.string.tx_75));
                } else if (progress == 1) {
                    timerText.setText(getApplicationContext().getResources().getString(R.string.tx_76));
                } else {
                    timerText.setText(getApplicationContext().getResources().getString(R.string.tx_80));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                new writeSettings("gpstimer", seekBar.getProgress()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                mAnalytics.trackEvent("Settings", "AutoCorrect_to_" + seekBar.getProgress());
                // start Autocorrect after 3sek
                // because after this time the activity_settings are surely updated correctly
                try {
                    if (actualMapSource.equalsIgnoreCase("GoogleMaps")) {
                        GoogleMap.listHandler.sendEmptyMessageDelayed(6, 3000);
                    } else {
                        OsmMap.listHandler.sendEmptyMessageDelayed(6, 3000);
                    }
                } catch (Exception e) {
                    //Happens if user switched MapSource BEFORE enabling AutoCorrect
                    //Because then, the requested Activity does not exist UNTIL user leaves Settings
                    //No Problem, just ignore this case.
                }

            }
        });
    }

    private void setMapSource(String chosenMapSource) {
        new writeSettings("MapSource", chosenMapSource).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        if (chosenMapSource.equalsIgnoreCase("GoogleMaps")) {
            mapSpinner.setSelection(0);
            //activate checkbox for satelite view
            TextView sateliteText = (TextView) findViewById(R.id.sateliteText);
            sateliteText.setTextColor(Color.parseColor("#4d4d4d"));
            checkBoxSatellite.setClickable(true);
        } else if (chosenMapSource.equalsIgnoreCase("MapQuestOSM")) {
            mapSpinner.setSelection(1);
            //deactivate checkbox for satelite view
            TextView sateliteText = (TextView) findViewById(R.id.sateliteText);
            sateliteText.setTextColor(Color.parseColor("#8C8C8C"));
            checkBoxSatellite.setClickable(false);
            if (checkBoxSatellite.isChecked()) {
                checkBoxSatellite.performClick();
            }
        } else if (chosenMapSource.equalsIgnoreCase("MapnikOSM")) {
            mapSpinner.setSelection(2);
            //deactivate checkbox for satelite view
            TextView sateliteText = (TextView) findViewById(R.id.sateliteText);
            sateliteText.setTextColor(Color.parseColor("#8C8C8C"));
            checkBoxSatellite.setClickable(false);
            if (checkBoxSatellite.isChecked()) {
                checkBoxSatellite.performClick();
            }
        }
        actualMapSource = chosenMapSource;
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEND || actionId == EditorInfo.IME_ACTION_NEXT) {
            int op = editText.length();
            float number;
            if (op != 0) {
                try {
                    number = Float.valueOf(editText.getText().toString());
                    if (number < 241 && number > 119) {

                        String numberString = df.format(number);
                        new writeSettings("step_length", numberString).execute();

                        // close Keyboard after pressing the button
                        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                        editText.setFocusableInTouchMode(false); // Workaround: Cursor out of textfield
                        editText.setFocusable(false);
                        editText.setFocusableInTouchMode(true);
                        editText.setFocusable(true);

                        final Intent intent = new Intent();
                        intent.putExtra("ok", 0);
                        intent.putExtra("step_length", numberString);
                        setResult(RESULT_OK, intent);
                    } else if (number < 95 && number > 45) {

                        String numberString = df.format(number);
                        new writeSettings("step_length", numberString).execute();

                        // close Keyboard after pressing the button
                        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                        editText.setFocusableInTouchMode(false); // Workaround: Cursor out of textfield
                        editText.setFocusable(false);
                        editText.setFocusableInTouchMode(true);
                        editText.setFocusable(true);

                        final Intent intent = new Intent();
                        intent.putExtra("ok", 1);
                        intent.putExtra("step_length", numberString);
                        setResult(RESULT_OK, intent);
                    } else {
                        // close Keyboard after pressing the button
                        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                        editText.setFocusableInTouchMode(false); // Workaround: Cursor out of textfield
                        editText.setFocusable(false);
                        editText.setFocusableInTouchMode(true);
                        editText.setFocusable(true);
                        Toast.makeText(Settings.this, getApplicationContext().getResources().getString(R.string.tx_10), Toast.LENGTH_LONG).show();
                    }

                } catch (NumberFormatException e) {
                    // close Keyboard after pressing the button
                    InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    editText.setFocusableInTouchMode(false); // Workaround: Cursor out of textfield
                    editText.setFocusable(false);
                    editText.setFocusableInTouchMode(true);
                    editText.setFocusable(true);
                    Toast.makeText(Settings.this, getApplicationContext().getResources().getString(R.string.tx_32), Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(Settings.this, getApplicationContext().getResources().getString(R.string.tx_10), Toast.LENGTH_LONG).show();
                // close Keyboard after pressing the button
                InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                editText.setFocusableInTouchMode(false); // Workaround: Cursor out of textfield
                editText.setFocusable(false);
                editText.setFocusableInTouchMode(true);
                editText.setFocusable(true);
            }
        }
        mAnalytics.trackEvent("Settings", "Body_Height_Change");
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        subMenu1 = menu.addSubMenu(0, 3, 3, "").setIcon(R.drawable.ic_menu_moreoverflow_normal_holo_dark);
        subMenu1.add(0, 7, 7, getApplicationContext().getResources().getString(R.string.tx_65));

        MenuItem subMenu1Item = subMenu1.getItem();
        subMenu1Item.setIcon(R.drawable.ic_menu_moreoverflow_normal_holo_dark);
        subMenu1Item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 7:
                mAnalytics.trackEvent("Settings", "Info_via_Menu");
                startActivity(new Intent(this, Info.class));
                return true;
            case android.R.id.home:
                // back
                if (actualMapSource.equalsIgnoreCase("GoogleMaps")) {
                    if (actualMapSource.equalsIgnoreCase(oldMapSource) == false) {
                        OsmMap.listHandler.sendEmptyMessageDelayed(3, 2000);
                        startActivity(new Intent(this, GoogleMap.class));
                    }
                } else {
                    if (oldMapSource.equalsIgnoreCase("GoogleMaps")) {
                        try {
                            GoogleMap.listHandler.sendEmptyMessageDelayed(3, 2000);
                        } catch (Exception e) {
                            //may happen when user has no GooglePlayServices
                        }
                        startActivity(new Intent(this, OsmMap.class));
                    }
                }
                finish();
                return (true);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (actualMapSource.equalsIgnoreCase("GoogleMaps")) {
            if (actualMapSource.equalsIgnoreCase(oldMapSource) == false) {
                OsmMap.listHandler.sendEmptyMessage(3);
                startActivity(new Intent(this, GoogleMap.class));
            }
        } else {
            if (oldMapSource.equalsIgnoreCase("GoogleMaps")) {
                GoogleMap.listHandler.sendEmptyMessage(3);
                startActivity(new Intent(this, OsmMap.class));
            }
        }
        super.onBackPressed();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        String key = "";

        // AutoCorrect
        if (buttonView.getId() == R.id.checkBoxGPS) {

            SharedPreferences settings = getSharedPreferences(getPackageName() + "_preferences", MODE_PRIVATE);

            final SeekBar seekBarTimer = (SeekBar) findViewById(R.id.seekBarTimer);
            seekBarTimer.setEnabled(isChecked);

            final TextView timerText = (TextView) findViewById(R.id.textTimer);
            if (isChecked == true) {
                seekBarTimer.setThumb(getResources().getDrawable(R.drawable.seek_thumb_normal));
                int gpsTimer = settings.getInt("gpstimer", 1);
                if (gpsTimer == 0) {
                    timerText.setText(getApplicationContext().getResources().getString(R.string.tx_75));
                } else if (gpsTimer == 1) {
                    timerText.setText(getApplicationContext().getResources().getString(R.string.tx_76));
                } else {
                    timerText.setText(getApplicationContext().getResources().getString(R.string.tx_80));
                }

                // check is GPS is allowed/enabled, if not: give a warning
                mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) == false) {
                    Toast.makeText(Settings.this, getApplicationContext().getResources().getString(R.string.tx_49), Toast.LENGTH_LONG).show();
                }

            } else {
                timerText.setText(getApplicationContext().getResources().getString(R.string.tx_25));
            }

            //Here AutoCorrect is enabled/disabled, IF is has been changed in the activity_settings
            //everything else is done via GoogleMapsActivity/... . onCreate()
            if (isChecked == false) {
                // deactivate Autocorrect
                if (Config.usingGoogleMaps) {
                    GoogleMap.listHandler.sendEmptyMessage(7);
                } else {
                    OsmMap.listHandler.sendEmptyMessage(7);
                }
            } else {
                // start Autocorrect anwerfern after 3sek
                // because then activity_settings are surely updated
                if (Config.usingGoogleMaps) {
                    GoogleMap.listHandler.sendEmptyMessageDelayed(6, 2000);
                } else {
                    OsmMap.listHandler.sendEmptyMessageDelayed(6, 2000);
                }
            }
            key = "autocorrect";

        } else if (buttonView.getId() == R.id.checkBoxExport) {
            key = "export";
            if (isChecked) {
                Toast.makeText(Settings.this, getResources().getString(R.string.tx_88), Toast.LENGTH_SHORT).show();
            }
        } else if (buttonView.getId() == R.id.checkBoxSatellite) {
            key = "view";
        } else if (buttonView.getId() == R.id.checkBoxSpeech) {
            key = "language";
        } else if (buttonView.getId() == R.id.checkBoxUsageData) {
            key = "nutzdaten";
        } else if (buttonView.getId() == R.id.checkBoxVibration) {
            key = "vibration";
        }
        mAnalytics.trackEvent("Settings", key + "_changed_to_" + isChecked);
        new writeSettings(key, isChecked).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private class writeSettings extends AsyncTask<Void, Void, Void> {

        private String key;
        private boolean setting1;
        private String setting2;
        private int setting3 = 0;
        private int dataType = 0;

        private writeSettings(String key, boolean setting1) {
            this.key = key;
            this.setting1 = setting1;
            dataType = 0;
        }

        private writeSettings(String key, String setting2) {
            this.key = key;
            this.setting2 = setting2;
            dataType = 1;
        }

        private writeSettings(String key, int setting3) {
            this.key = key;
            this.setting3 = setting3;
            dataType = 2;
        }

        @Override
        protected Void doInBackground(Void... params) {
            SharedPreferences settings = getSharedPreferences(getPackageName() + "_preferences", MODE_PRIVATE);
            if (dataType == 0) {
                settings.edit().putBoolean(key, setting1).commit();
            } else if (dataType == 1) {
                settings.edit().putString(key, setting2).commit();
            } else if (dataType == 2) {
                settings.edit().putInt(key, setting3).commit();
            }
            return null;
        }
    }

}
