package xyz.oboloi.openvpnexample;

import androidx.appcompat.app.AppCompatActivity;


import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import xyz.oboloi.openvpn.OboloiVPN;
import xyz.oboloi.openvpn.OnVPNStatusChangeListener;

public class MainActivity extends AppCompatActivity {

    private Button btnConnect;
    private OboloiVPN oboloiVPN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnConnect = findViewById(R.id.start);
        oboloiVPN = new OboloiVPN(MainActivity.this, getApplicationContext());
        oboloiVPN.launchVPN("https://downloads.nordcdn.com/configs/files/ovpn_legacy/servers/al10.nordvpn.com.udp1194.ovpn");


        oboloiVPN.setOnVPNStatusChangeListener(new OnVPNStatusChangeListener() {

            @Override
            public void onProfileLoaded(boolean profileLoaded) {
                if(profileLoaded){
                    btnConnect.setEnabled(true);
                }else {
                    btnConnect.setEnabled(false);
                }

            }
            @Override
            public void onVPNStatusChanged(boolean vpnActivated) {
                if(vpnActivated){
                    btnConnect.setText("disconnect");
                }else{
                    btnConnect.setText("connect");
                }
            }


        });


        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                oboloiVPN.init();

            }
        });

    }

    @Override
    protected void onStop() {
        oboloiVPN.onStop();
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        oboloiVPN.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        oboloiVPN.onPause();
    }


    @Override
    public void finish() {
        super.finish();
        oboloiVPN.cleanup();
    }


}
