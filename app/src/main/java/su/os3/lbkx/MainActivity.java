package su.os3.lbkx;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.TelephonyManager;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.util.encoders.Base64;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, ContactsAdapter.ItemClickListener {

    public static String phoneNumberAuto;
    public static File certDir;
    public static String certDirPath;
    public static String appDirPath;
    public static String sdDir;
    public static String sdCertDirPath;
    public static SharedPreferences prefs;
    public static SharedPreferences.Editor prefsEditor;
    ContactsAdapter adapter;
    public static File mAbonentCert;

    public static Connection chatConnection;
    private boolean isAppRunning = true;
    private boolean connectedToChat = false;
    public static ArrayList<String> contactsList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //startChat();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)!=PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions( this, new String[] {  android.Manifest.permission.READ_PHONE_STATE  },1001);
        }

        TelephonyManager tMgr = (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
        phoneNumberAuto = tMgr.getLine1Number();

        appDirPath=getApplicationInfo().dataDir;
        certDir=new File(appDirPath+"/certs");
        certDir.mkdir();
        certDirPath=certDir.getAbsolutePath();
        sdDir= Environment.getExternalStorageDirectory().getAbsolutePath();
        File sdCertDir=new File(sdDir+"/LBKX");
        sdCertDirPath=sdCertDir.getAbsolutePath();
        sdCertDir.mkdir();

        // data to populate the RecyclerView with
        contactsList = new ArrayList<>();
        contactsList.add("79178847208");
        // set up the RecyclerView
        RecyclerView recyclerView = findViewById(R.id.rvContacts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ContactsAdapter(this, contactsList);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);

        prefs=PreferenceManager.getDefaultSharedPreferences(this);
        prefsEditor=prefs.edit();

        Thread connectionChecker = new Thread(){
            public void run(){
                Looper.prepare();
                final String Address=prefs.getString("chat_address", "std26.os3.su");
                final String Port=prefs.getString("chat_port", "8640");
                String lastID=prefs.getString("ttp_number", "").replace("+","");;
                while (isAppRunning){
                    try {
                        String MyID=prefs.getString("ttp_number", "").replace("+","");
                        if (MyID.contentEquals(lastID)) {
                            if (chatConnection!=null && chatConnection.isAlive()) {
                                if (chatConnection.isDataAvailable()) {
                                    byte[] message=chatConnection.readMessage();
                                    final byte[] sender= Arrays.copyOfRange(message, 0, 11);
                                    final byte[] challenge=Arrays.copyOfRange(message, 11, message.length);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                lbkxUIUtil.showChallenge(MainActivity.this, sender, challenge);
                                            } catch (UnsupportedEncodingException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                }
                                else {
                                    Thread.sleep(2000);
                                }
                            }
                            else {
                                Thread.sleep(2000);
                                chatConnection = new Connection(Address, Integer.parseInt(Port));
                                chatConnection.connectToChatAttempt(MyID);
                            }
                        }
                        else {
                            chatConnection.closeConnection();
                        }
                        lastID=MyID;
                    }
                    catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        connectionChecker.setPriority(3);
        connectionChecker.start();



/*
        File contactsJsonFile=new File(getApplicationInfo().dataDir+"/contacts.json");
        if(contactsJsonFile.exists()){
            try {
                JSONArray contactsJson = new JSONArray(FileUtils.readFileToString(contactsJsonFile));
                for(int i=0; i<contactsJson.length();i++){
                    String number =
                    String certificate =
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }*/
    }

    @Override
    public void onItemClick(View view, int position) {
        Toast.makeText(this, "You clicked " + adapter.getItem(position) + " on row number " + position, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.genCert) {
                String MyID = prefs.getString("ttp_number", "").replace("+", "");
                try {
                    byte[][] keys = Crypto.genRSAKey();
                    byte[] selfCert=Crypto.genSelfCert(MyID, keys[0], keys[1]);
                    String publicCert = "";
                    Base64 encoder = new Base64();

                    publicCert +="-----BEGIN CERTIFICATE-----\n";
                    publicCert +=new String(encoder.encode(selfCert));
                    publicCert +="\n-----END CERTIFICATE-----";
                    FileUtils.writeStringToFile(new File(sdCertDirPath+"/selfcert.pem"), publicCert);
                    FileUtils.writeStringToFile(new File(certDirPath+"/selfcert.pem"), publicCert);
                    FileUtils.writeByteArrayToFile(new File(appDirPath+"/privateKey.der"), keys[1]);
                    lbkxUIUtil.showAlert(this, "Certificate generated", "Your certificate is in \"LBKX\" directory of your SD card.");
                }
                catch (OperatorCreationException | GeneralSecurityException | IOException e) {
                    e.printStackTrace();
                }
            return true;
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void chooseContactCert(final View v){
        lbkxUIUtil.chooseCert(this, new onCertSelected() {
            @Override
            public void returnCert(File cert) {
                mAbonentCert=cert;
            }
        });
    }

    public void startChat(View v) {
        if(mAbonentCert==null || !mAbonentCert.exists()){
            lbkxUIUtil.showAlert(this,"Error","Certificate is not choosen!");
        }
        else{
            Intent intent = new Intent(this, MapsActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onDestroy () {
        super.onDestroy();
        isAppRunning = false;
        super.onStop();
    }
}
