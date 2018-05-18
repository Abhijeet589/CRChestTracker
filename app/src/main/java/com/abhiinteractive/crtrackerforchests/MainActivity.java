package com.abhiinteractive.crtrackerforchests;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    TextView giantNo, magicalNo, epicNo, legendaryNo, superMagicalNo;
    TextView upcomingNo[];
    Response response;
    String jsonData, giantPos, magicalPos, epicPos, legendaryPos, superMagicalPos;
    String upcomingPos[] = new String[8];
    ImageView giantImg, magicalImg, epicImg, legendaryImg, superMagicalImg;
    ImageView upcomingImg[];
    JSONObject js;
    SharedPreferences firstRun = null;
    SharedPreferences tagPref = null;
    ImageView settings;
    Button checkOthers;
    LinearLayout contactUs;
    View.OnClickListener a;
    TextView tagHeader;
    LinearLayout getTag, chestsScreen, loading;
    EditText et;
    Button submitTag;
    String tag;
    static int retryCount=0;
    boolean err=true;
    NetworkChangeReceiver mReciever;
    AdView mAdView;
    String errStr;
    boolean changeSelfTag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getTag = findViewById(R.id.get_tag);
        chestsScreen = findViewById(R.id.chests_screen);
        loading = findViewById(R.id.loading);
        et = findViewById(R.id.enter_tag_et);
        submitTag = findViewById(R.id.submit_tag);
        settings = findViewById(R.id.settings);
        checkOthers = findViewById(R.id.check_others);
        tagHeader = findViewById(R.id.tag_header);
        contactUs = findViewById(R.id.contact_us);

        giantNo = findViewById(R.id.giant_no);
        giantImg = findViewById(R.id.giant_img);
        magicalNo = findViewById(R.id.magical_no);
        magicalImg = findViewById(R.id.magical_img);
        epicNo = findViewById(R.id.epic_no);
        epicImg = findViewById(R.id.epic_img);
        legendaryNo = findViewById(R.id.legendary_no);
        legendaryImg = findViewById(R.id.legendary_img);
        superMagicalNo = findViewById(R.id.super_magical_no);
        superMagicalImg = findViewById(R.id.super_magical_img);
        upcomingNo = new TextView[]{findViewById(R.id.first_no),
                findViewById(R.id.second_no),
                findViewById(R.id.third_no),
                findViewById(R.id.fourth_no),
                findViewById(R.id.fifth_no),
                findViewById(R.id.sixth_no),
                findViewById(R.id.seventh_no),
                findViewById(R.id.eighth_no)};
        upcomingImg = new ImageView[]{findViewById(R.id.first_img),
                findViewById(R.id.second_img),
                findViewById(R.id.third_img),
                findViewById(R.id.fourth_img),
                findViewById(R.id.fifth_img),
                findViewById(R.id.sixth_img),
                findViewById(R.id.seventh_img),
                findViewById(R.id.eighth_img)};

        firstRun = getSharedPreferences("com.abhiinteractive.crchesttracker", MODE_PRIVATE);
        tagPref = getSharedPreferences("com.abhiinteractive.crchesttracker", MODE_PRIVATE);

        mReciever = new NetworkChangeReceiver();
        registerReceiver(mReciever, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        if(getConnectionStatus()) {

            MobileAds.initialize(this, "ca-app-pub-6229326724546843~5765506159");

            mAdView = findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder().addTestDevice("C50478B9352C968A158C206EB2D1DA63").build();
            mAdView.loadAd(adRequest);

            if (firstRun.getBoolean("isFirst", true)) {
                getTag.setVisibility(View.VISIBLE);
                chestsScreen.setVisibility(View.GONE);
                submitTag.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        tag = et.getText().toString();
                        tagPref.edit().putString("tag", tag).commit();
                        firstRun.edit().putBoolean("isFirst", false).commit();
                        chestsScreen.setVisibility(View.VISIBLE);
                        getTag.setVisibility(View.GONE);
                        new Weather().execute();
                    }
                });
            } else {
                tag = tagPref.getString("tag", "");
                new Weather().execute();
            }

            contactUs.setOnClickListener(new View.OnClickListener() {
                final String[] adresses = {"abhiinteractive@gmail.com"};
                @Override
                public void onClick(View v) {
                    Intent contact = new Intent(Intent.ACTION_SENDTO);
                    contact.setData(Uri.parse("mailto:"));
                    contact.putExtra(Intent.EXTRA_EMAIL, adresses);
                    if (contact.resolveActivity(getPackageManager()) != null) {
                        startActivity(contact);
                    }
                }
            });

            settings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getTag.setVisibility(View.VISIBLE);
                    chestsScreen.setVisibility(View.GONE);
                    contactUs.setVisibility(View.VISIBLE);
                    tagHeader.setText("Change your tag");
                    et.setText("");
                    submitTag.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            tag = et.getText().toString();
                            tagPref.edit().putString("tag", tag).commit();
                            changeSelfTag = true;
                            chestsScreen.setVisibility(View.VISIBLE);
                            contactUs.setVisibility(View.GONE);
                            getTag.setVisibility(View.GONE);
                            new Weather().execute();
                        }
                    });
                    checkOthers.setText("Back");
                    checkOthers.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            checkOthers.setOnClickListener(a);
                            checkOthers.setText("Check other player's chest");
                            chestsScreen.setVisibility(View.VISIBLE);
                            getTag.setVisibility(View.GONE);
                            contactUs.setVisibility(View.GONE);
                            new Weather().execute();
                        }
                    });
                }
            });

            a = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getTag.setVisibility(View.VISIBLE);
                    chestsScreen.setVisibility(View.GONE);
                    tagHeader.setText("Enter the player's tag");
                    et.setText("");
                    submitTag.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            tag = et.getText().toString();
                            chestsScreen.setVisibility(View.VISIBLE);
                            getTag.setVisibility(View.GONE);
                            new Weather().execute();
                        }
                    });
                    checkOthers.setText("Back to your chests");
                    checkOthers.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            tag = tagPref.getString("tag", "");
                            chestsScreen.setVisibility(View.VISIBLE);
                            getTag.setVisibility(View.GONE);
                            contactUs.setVisibility(View.GONE);
                            checkOthers.setText("Check other player's chests");
                            checkOthers.setOnClickListener(a);
                            new Weather().execute();
                        }
                    });
                }
            };
            checkOthers.setOnClickListener(a);
        }
        else{
            AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
            alertDialog.setTitle("No internet");
            alertDialog.setCancelable(false);
            alertDialog.setMessage("You need to connect to internet to see your chests");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
            alertDialog.show();
        }
    }

    boolean getConnectionStatus(){
        ConnectivityManager cm =
                (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }

    private class Weather extends AsyncTask<URL, Void, Void> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            chestsScreen.setVisibility(View.GONE);
            loading.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(URL... urls) {
            performJSON();
            return null;
        }

        private void performJSON(){
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .build();

            Request request = new Request.Builder()
                    .url("http://api.cr-api.com/player/" + tag + "/chests")
                    .get()
                    .addHeader("auth", "519096714dfc42449cdf1524285a7ba2f521abe4faf449f78fadb7408f623d2f")
                    .build();

            try {
                response = client.newCall(request).execute();
                String r = response.body().string();
                jsonData = r;
                js = new JSONObject(jsonData);
                giantPos = js.getString("giant");
                magicalPos = js.getString("magical");
                epicPos = js.getString("epic");
                legendaryPos = js.getString("legendary");
                superMagicalPos = js.getString("superMagical");
                for (int i = 0; i < 8; i++) {
                    upcomingPos[i] = js.getJSONArray("upcoming").getString(i);
                }

            } catch (SocketTimeoutException e){
                e.printStackTrace();
                Log.e("errorCheck", "SocketTimeoutException");
                retry();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("errorCheck", "IOException");
                retry();
            } catch (NullPointerException e) {
                e.printStackTrace();
                Log.e("errorCheck", "NullPointerException");
                retry();
            } catch (JSONException e){
                e.printStackTrace();
                Log.e("errorCheck", "JSONException");
                try {
                    errStr = js.getString("error");
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }
        }

        private void retry(){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            retryCount++;
            if (retryCount < 3) {
                performJSON();
            } else {
                showErrorDialog();
            }
        }

        private void showErrorDialog(){
            err = false;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (err && errStr!="true") {
                chestsScreen.setVisibility(View.VISIBLE);
                loading.setVisibility(View.GONE);

                giantImg.setImageResource(R.drawable.giant);
                giantNo.setText("+" + giantPos);

                magicalImg.setImageResource(R.drawable.magical);
                magicalNo.setText("+" + magicalPos);

                epicImg.setImageResource(R.drawable.epic);
                epicNo.setText("+" + epicPos);

                legendaryImg.setImageResource(R.drawable.legendary);
                legendaryNo.setText("+" + legendaryPos);

                superMagicalImg.setImageResource(R.drawable.super_magical);
                superMagicalNo.setText("+" + superMagicalPos);

                for (int i = 0; i < 8; i++) {
                    switch (upcomingPos[i]) {
                        case "silver":
                            upcomingImg[i].setImageResource(R.drawable.silver);
                            upcomingNo[i].setText("+" + (i));
                            break;
                        case "gold":
                            upcomingImg[i].setImageResource(R.drawable.golden);
                            upcomingNo[i].setText("+" + (i));
                            break;
                        case "magical":
                            upcomingImg[i].setImageResource(R.drawable.magical);
                            upcomingNo[i].setText("+" + (i));
                            break;
                        case "giant":
                            upcomingImg[i].setImageResource(R.drawable.giant);
                            upcomingNo[i].setText("+" + (i));
                            break;
                        case "epic":
                            upcomingImg[i].setImageResource(R.drawable.epic);
                            upcomingNo[i].setText("+" + (i));
                            break;
                        case "legendary":
                            upcomingImg[i].setImageResource(R.drawable.legendary);
                            upcomingNo[i].setText("+" + (i));
                            break;
                        case "superMagical":
                            upcomingImg[i].setImageResource(R.drawable.super_magical);
                            upcomingNo[i].setText("+" + (i));
                            break;
                    }
                    upcomingNo[0].setText("Next");
                }
            }
            else if((tag==null) || (tag=="")){
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                    alertDialog.setTitle("Tag not found\n");
                    alertDialog.setMessage("Looks like we lost your tag, could you please enter it again? (without #)");

                    final EditText input = new EditText(MainActivity.this);
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
                    input.setLayoutParams(lp);
                    input.setHint("Enter tag here");
                    alertDialog.setView(input);

                    alertDialog.setPositiveButton("Done",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    tag = input.getText().toString();
                                    if(changeSelfTag) {
                                        tagPref.edit().putString("tag", tag).commit();
                                        changeSelfTag = false;
                                    }
                                    errStr = "false";
                                    new Weather().execute();
                                }
                            });
                    alertDialog.show();
                }
                else if(errStr=="true") {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                alertDialog.setTitle("Wrong tag\n");
                alertDialog.setMessage("Looks like you entered the tag wrong, try entering it again (without #)");

                final EditText input = new EditText(MainActivity.this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                input.setLayoutParams(lp);
                input.setHint("Enter tag here");
                alertDialog.setView(input);

                alertDialog.setPositiveButton("Done",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                tag = input.getText().toString();
                                tagPref.edit().putString("tag", tag).commit();
                                errStr = "false";
                                new Weather().execute();
                            }
                        });
                alertDialog.show();
                }
                else{
                    AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                    alertDialog.setTitle("Error fetching data");
                    alertDialog.setMessage("We were unable to fetch the data for some reason. Try restarting the app and if the problem persists, contact us from settings");
                    alertDialog.setCancelable(false);
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Ok",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            });
                    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Contact us",
                            new DialogInterface.OnClickListener() {
                                final String[] adresses = {"abhiinteractive@gmail.com"};
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent contact = new Intent(Intent.ACTION_SENDTO);
                                    contact.setData(Uri.parse("mailto:")); // only email apps should handle this
                                    contact.putExtra(Intent.EXTRA_EMAIL, adresses);
                                    if (contact.resolveActivity(getPackageManager()) != null) {
                                        startActivity(contact);
                                    }
                                }
                            });
                    alertDialog.show();
                }
        }
    }

    public class NetworkChangeReceiver extends BroadcastReceiver{

        public NetworkChangeReceiver(){}

        @Override
        public void onReceive(Context context, Intent intent) {
            if(!getConnectionStatus()){
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                alertDialog.setTitle("No internet");
                alertDialog.setCancelable(false);
                alertDialog.setMessage("You need to connect to internet to see your chests");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        });
                alertDialog.show();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReciever);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReciever, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }
}
