package com.keep.wlanwebrtcc;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.keep.wlanwebrtcc.databinding.ActivityChatingBinding;
import com.keep.wlanwebrtcc.databinding.ActivityChatting2Binding;
import com.keep.wlanwebrtcc.net.NetUtils;
import com.keep.wlanwebrtcc.webrtc.WebrtcManager;

import org.webrtc.VideoTrack;

public class Chatting2Activity extends AppCompatActivity {
    WebrtcManager webrtcManager;
    ActivityChatingBinding binding;
    final int ALL_PERMISSIONS_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_chatting2);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_chating);
        webrtcManager = new WebrtcManager(getApplicationContext());
        webrtcManager.setEventUICallback(mEventUICallBack);
        binding.tvLocalIp.setText(NetUtils.getIPAddress(getApplicationContext()));
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, ALL_PERMISSIONS_CODE);
        } else {
            // all permissions already granted
            start();
        }
    }

    private void start() {
        binding.btCall.setOnClickListener(v -> webrtcManager.doCall(binding.etTartgetIp.getText().toString(), 10001));
        binding.btAnswer.setOnClickListener(v -> webrtcManager.doAnswer());
        binding.btHungup.setOnClickListener(v -> {
            webrtcManager.doHungup();
            binding.localGlSurfaceView.clearImage();
            binding.remoteGlSurfaceView.clearImage();
        });
        webrtcManager.init(binding.localGlSurfaceView, binding.remoteGlSurfaceView);


    }

    public void showToast(final String msg) {
        runOnUiThread(() -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show());
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ALL_PERMISSIONS_CODE
                && grantResults.length == 2
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            // all permissions granted
            start();
        } else {
            finish();
        }
    }

    WebrtcManager.EventUICallBack mEventUICallBack = new WebrtcManager.EventUICallBack() {

        @Override
        public void callin(String senderIp) {
            runOnUiThread(() -> {
                showToast("callIn");
                binding.vState.setBackgroundColor(Color.YELLOW);
                binding.btAnswer.setEnabled(true);
                binding.btAnswer.setEnabled(true);
            });
        }

        @Override
        public void showVideo(VideoTrack videoTrack) {
            runOnUiThread(() -> {
                try {
                    binding.remoteGlSurfaceView.setVisibility(View.VISIBLE);
                    videoTrack.addSink(binding.remoteGlSurfaceView);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        @Override
        public void receiveHungup() {
            runOnUiThread(() -> {
                binding.localGlSurfaceView.clearImage();
                binding.remoteGlSurfaceView.clearImage();
            });
        }
    };

}
