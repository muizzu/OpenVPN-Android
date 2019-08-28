package xyz.oboloi.openvpnexample;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import de.blinkt.openvpn.LaunchVPN;
import de.blinkt.openvpn.VpnProfile;
import de.blinkt.openvpn.core.App;
import de.blinkt.openvpn.core.ConnectionStatus;
import de.blinkt.openvpn.core.IOpenVPNServiceInternal;
import de.blinkt.openvpn.core.OpenVPNService;
import de.blinkt.openvpn.core.ProfileManager;
import de.blinkt.openvpn.core.VpnStatus;
import xyz.oboloi.openvpn.DataCleanManager;
import xyz.oboloi.openvpn.OboloiVPN;
import xyz.oboloi.openvpn.ProfileAsync;

public class MainActivity extends AppCompatActivity implements VpnStatus.ByteCountListener, VpnStatus.StateListener  {

    private static IOpenVPNServiceInternal mService;
    private static ProfileAsync profileAsync;

    private Button btnConnect;


    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = IOpenVPNServiceInternal.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mService = null;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnConnect = findViewById(R.id.start);


        String url = "https://firebasestorage.googleapis.com/v0/b/bilet-oboloi.appspot.com/o/muizzu.ovpn?alt=media&token=395b8c0b-ffa9-491b-8fcc-d45286023d40";


        if (!App.isStart) {
            DataCleanManager.cleanCache(this);
            DataCleanManager.cleanApplicationData(this);
            btnConnect.setEnabled(false);

            profileAsync = new ProfileAsync(this, new ProfileAsync.OnProfileLoadListener() {
                @Override
                public void onProfileLoadSuccess() {

                    btnConnect.setEnabled(true);
                }

                @Override
                public void onProfileLoadFailed(String msg) {

                    Toast.makeText(MainActivity.this, MainActivity.this.getString(R.string.init_fail) + msg, Toast.LENGTH_SHORT).show();
                }
            },url);
            profileAsync.execute();
        }




        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        if (!App.isStart) {
                            startVPN();
                            App.isStart = true;
                        } else {
                            stopVPN();
                            App.isStart = false;
                        }
                    }
                };
                r.run();



            }
        });

    }

    @Override
    protected void onStop() {
        VpnStatus.removeStateListener(this);
        VpnStatus.removeByteCountListener(this);
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        VpnStatus.addStateListener(this);
        VpnStatus.addByteCountListener(this);
        Intent intent = new Intent(this, OpenVPNService.class);
        intent.setAction(OpenVPNService.START_SERVICE);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(mConnection);
    }


    @Override
    public void finish() {
        super.finish();
        if (profileAsync != null && !profileAsync.isCancelled()) {
            profileAsync.cancel(true);
        }
    }


    void startVPN() {
        try {
            ProfileManager pm = ProfileManager.getInstance(this);
            VpnProfile profile = pm.getProfileByName(Build.MODEL);
            startVPNConnection(profile);
        } catch (Exception ex) {
            App.isStart = false;
        }
    }

    void stopVPN() {
        stopVPNConnection();
        btnConnect.setText(getString(R.string.connect));

    }


    // ------------- Functions Related to OpenVPN-------------
    public void startVPNConnection(VpnProfile vp) {
        Intent intent = new Intent(getApplicationContext(), LaunchVPN.class);
        intent.putExtra(LaunchVPN.EXTRA_KEY, vp.getUUID().toString());
        intent.setAction(Intent.ACTION_MAIN);
        startActivity(intent);
    }

    public void stopVPNConnection() {
        ProfileManager.setConnectedVpnProfileDisconnected(this);
        if (mService != null) {
            try {
                mService.stopVPN(false);
            } catch (RemoteException e) {
                VpnStatus.logException(e);
            }
        }
    }

    void setConnected() {

        btnConnect.setText(getString(xyz.oboloi.openvpn.R.string.connected));
    }

    void changeStateButton(Boolean state) {
        if (state) {

            btnConnect.setText(getString(xyz.oboloi.openvpn.R.string.connected));

        } else {

            btnConnect.setText(getString(xyz.oboloi.openvpn.R.string.connect));
        }
    }


    @Override
    public void updateState(final String state, String logmessage, int localizedResId, ConnectionStatus level) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (state.equals("CONNECTED")) {
                    Log.e("status", "connected");
                    App.isStart = true;
                    setConnected();
                }
                if (state.equals("AUTH_FAILED")) {
                    Toast.makeText(MainActivity.this, "Wrong Username or Password!", Toast.LENGTH_SHORT).show();
                    changeStateButton(false);
                }
            }
        });
    }

    @Override
    public void setConnectedVPN(String uuid) {

    }

    @Override
    public void updateByteCount(long in, long out, long diffIn, long diffOut) {

    }
}
