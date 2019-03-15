package com.example.gomes_michael_esig.devmob;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

public class MainActivity extends AppCompatActivity implements SensorEventListener, GoogleApiClient.OnConnectionFailedListener {

    TextView temp;
    TextView conseil;
    ImageView image;
    private SensorManager mSensorManager;
    private Sensor mTemperature;
    private final static String NOT_SUPPORTED_MESSAGE = "Sorry, sensor not available for this device.";



//    Admin
    private GoogleApiClient googleApiClient;
    private SignInButton signIn;
    public static final int CODE = 777;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        temp = (TextView) findViewById(R.id.text_view_temp);
        conseil= (TextView)findViewById(R.id.text_view_id);
        image= (ImageView)findViewById(R.id.crampon);
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.ICE_CREAM_SANDWICH){
            mTemperature= mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE); // requires API level 14.
        }
        if (mTemperature == null) {
            temp.setText(NOT_SUPPORTED_MESSAGE);
        }

//        connexion
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        signIn = (SignInButton) findViewById(R.id.sign_in_button);
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
                startActivityForResult(intent, CODE);

            }
        });

    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mTemperature, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float ambient_temperature = event.values[0];

        temp.setText("Il fait " + String.valueOf(ambient_temperature)  + " " + getResources().getString(R.string.celsius) + ",");
        if (ambient_temperature < 2){
            conseil.setText("tu vas sûrment jouer sur synthétique, prépare tes plastiques");
            image.setImageResource(R.drawable.cramponplastique);
            return;
        }
        else{
            conseil.setText("tu vas sûrment jouer sur herbe, prépare tes fers");
            image.setImageResource(R.drawable.fer);
            return;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }

    /* Lien avec autres layout*/
    public void openStadeliste(View view){
        Intent i = new Intent(this, Stade.class);
        startActivity(i);
    }

    public void openPhoto(View view){
        Intent i = new Intent(this, Photo.class);
        startActivity(i);
    }

    public void openStat(View view){
        Intent i = new Intent(this, stat.class);
        startActivity(i);
    }


//    Admin
@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CODE){
            GoogleSignInResult result =  Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            int statusCode = result.getStatus().getStatusCode();
            Log.d("", "handleSignInResult:" + result.toString() + "+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        if(result.isSuccess()){
            GoogleSignInAccount account = result.getSignInAccount();
//          Test d'accès

            Log.d("", "NOMMMMMM:" + account.getDisplayName() + "-------------------------------------------------------------");
            if(account.getDisplayName().equals("elv-michael.gmsds@eduge.ch") || (account.getDisplayName().equals("MICHAEL ELV-MICHAEL.GMSDS"))){
                goAdminScreen();
            }else{
                Toast.makeText(this, "yes", Toast.LENGTH_SHORT).show();
                goUserScreen();
            }
        }else{
            Toast.makeText(this, "La session ne peut pas se lancer", Toast.LENGTH_SHORT).show();
        }
    }

    private void goMainScreen() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
    private void goAdminScreen() {
        Intent intent = new Intent(this, Admin.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void goUserScreen() {
        Intent intent = new Intent(this, stat.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void logOut(View view){
        Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                if(status.isSuccess()){
                    goMainScreen();
                }else{
                    Toast.makeText(getApplicationContext(), "Impossible de fermer", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}


/*Code température transmis par Laurent qui vient de : https://stackoverflow.com/questions/11987134/how-to-measure-ambient-temperature-in-android*/

// Code inspié du tutoriel : https://www.youtube.com/watch?v=O3aemJ9eAAA
// Aide : https://developers.google.com/identity/sign-in/android/sign-in
// Meme problème : https://stackoverflow.com/questions/43015476/googlesigninresult-always-returning-not-success-staus