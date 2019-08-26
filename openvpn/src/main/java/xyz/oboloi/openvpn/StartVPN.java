package xyz.oboloi.openvpn;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.dd.processbutton.iml.ActionProcessButton;

import de.blinkt.openvpn.LaunchVPN;
import de.blinkt.openvpn.VpnProfile;
import de.blinkt.openvpn.core.App;
import de.blinkt.openvpn.core.ConnectionStatus;
import de.blinkt.openvpn.core.IOpenVPNServiceInternal;
import de.blinkt.openvpn.core.OpenVPNService;
import de.blinkt.openvpn.core.ProfileManager;
import de.blinkt.openvpn.core.VpnStatus;

public class StartVPN extends AppCompatActivity implements VpnStatus.ByteCountListener, VpnStatus.StateListener  {

    private static IOpenVPNServiceInternal mService;
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

    private static ProfileAsync profileAsync;

    @Override
    public void finish() {
        super.finish();
        if (profileAsync != null && !profileAsync.isCancelled()) {
            profileAsync.cancel(true);
        }
    }

    public static void launchVPN(final Context context,String url){

        if (!App.isStart) {
            DataCleanManager.cleanCache(context);

            profileAsync = new ProfileAsync(context, new ProfileAsync.OnProfileLoadListener() {
                @Override
                public void onProfileLoadSuccess() {
                 //   progressBar.setVisibility(View.GONE);
                 //   btnConnect.setEnabled(true);
                }

                @Override
                public void onProfileLoadFailed(String msg) {
                    Log.e(context.getPackageName(),msg);
                    //progressBar.setVisibility(View.GONE);
                   // Toast.makeText(activity, activity.this.getString(R.string.init_fail) + msg, Toast.LENGTH_SHORT).show();
                }
            },url);
            profileAsync.execute();
//            rate();

            Runnable r = new Runnable() {
                @Override
                public void run() {
                    if (!App.isStart) {
                        try {
                            ProfileManager pm = ProfileManager.getInstance(context);
                            VpnProfile profile = pm.getProfileByName(Build.MODEL);//
                            startVPNConnection(profile,context);
                        } catch (Exception ex) {
                            App.isStart = false;
                        }
                        App.isStart = true;
                    } else {
                        stopVPNConnection(context);
                        App.isStart = false;
                    }
                }
            };
            r.run();
        }
    }


    static void startVPNConnection(VpnProfile vp,Context context) {
        Intent intent = new Intent(context, LaunchVPN.class);
        intent.putExtra(LaunchVPN.EXTRA_KEY, vp.getUUID().toString());
        intent.setAction(Intent.ACTION_MAIN);
        context.startActivity(intent);
    }

    static void stopVPNConnection(Context context) {
        ProfileManager.setConntectedVpnProfileDisconnected(context);
        if (mService != null) {
            try {
                mService.stopVPN(false);
            } catch (RemoteException e) {
//                VpnStatus.logException(e);
            }
        }
    }





    @Override
    public void updateState(final String state, String logmessage, int localizedResId, ConnectionStatus level) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (state.equals("CONNECTED")) {
                    App.isStart = true;
                    // setConnected();
                    // layoutSpeedMeter.setVisibility(View.VISIBLE);
                } else {
                    //layoutSpeedMeter.setVisibility(View.INVISIBLE);
                }
                if (state.equals("AUTH_FAILED")) {
                    Toast.makeText(getApplicationContext(), "Wrong Username or Password!", Toast.LENGTH_SHORT).show();
                    // changeStateButton(false);
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
