package com.keep.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import com.keep.myapplication.R;
import com.keep.myapplication.databinding.ActivityChatBinding;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.util.List;
import java.util.ArrayList;


public class ChatActivity extends AppCompatActivity {
    final int ALL_PERMISSIONS_CODE = 1;
    ActivityChatBinding binding;
    //
    EglBase rootEglBase;
    PeerConnectionFactory peerConnectionFactory;
    MediaConstraints audioConstraints;
    MediaConstraints videoConstraints;

    SurfaceTextureHelper surfaceTextureHelper;
    VideoSource videoSource;
    VideoTrack localVideoTrack;

    AudioSource audioSource;
    AudioTrack localAudioTrack;

    boolean gotUserMedia;
    List<PeerConnection.IceServer> peerIceServers = new ArrayList<>();
    PeerConnection localPeer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_chat);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_chat);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, ALL_PERMISSIONS_CODE);
        } else {
            // all permissions already granted
            start();
        }
    }

    private void start() {
//        binding.btCall.setOnClickListener(v -> {
//            doCall();
//        });
//
//        binding.btAnswer.setOnClickListener(v -> doAnswer());
//
//        binding.btHungup.setOnClickListener(v -> doHungup());

//        initVideos();
//
//        SignallingClient.getInstance().init(mSignalingInterface);
//
//
//        //Initialize PeerConnectionFactory globals.
//        PeerConnectionFactory.InitializationOptions initializationOptions =
//                PeerConnectionFactory.InitializationOptions.builder(this)
//                        .createInitializationOptions();
//        PeerConnectionFactory.initialize(initializationOptions);
//
//        //Create a new PeerConnectionFactory instance - using Hardware encoder and decoder.
//        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
//        DefaultVideoEncoderFactory defaultVideoEncoderFactory = new DefaultVideoEncoderFactory(
//                rootEglBase.getEglBaseContext(),  /* enableIntelVp8Encoder */true,  /* enableH264HighProfile */true);
//        DefaultVideoDecoderFactory defaultVideoDecoderFactory = new DefaultVideoDecoderFactory(rootEglBase.getEglBaseContext());
//        peerConnectionFactory = PeerConnectionFactory.builder()
//                .setOptions(options)
//                .setVideoEncoderFactory(defaultVideoEncoderFactory)
//                .setVideoDecoderFactory(defaultVideoDecoderFactory)
//                .createPeerConnectionFactory();
//
//        //Now create a VideoCapturer instance.
//        VideoCapturer videoCapturerAndroid;
//        videoCapturerAndroid = createCameraCapturer(new Camera1Enumerator(false));
//
//        //Create MediaConstraints - Will be useful for specifying video and audio constraints.
//        audioConstraints = new MediaConstraints();
//        videoConstraints = new MediaConstraints();
//
//        //Create a VideoSource instance
//        if (videoCapturerAndroid != null) {
//            surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", rootEglBase.getEglBaseContext());
//            videoSource = peerConnectionFactory.createVideoSource(videoCapturerAndroid.isScreencast());
//            videoCapturerAndroid.initialize(surfaceTextureHelper, this, videoSource.getCapturerObserver());
//        }
//        localVideoTrack = peerConnectionFactory.createVideoTrack("100", videoSource);
//
//        //create an AudioSource instance
//        audioSource = peerConnectionFactory.createAudioSource(audioConstraints);
//        localAudioTrack = peerConnectionFactory.createAudioTrack("101", audioSource);
//
//        if (videoCapturerAndroid != null) {
//            videoCapturerAndroid.startCapture(1024, 720, 30);
//        }

//        binding.localGlSurfaceView.setVisibility(View.VISIBLE);
//        // And finally, with our VideoRenderer ready, we
//        // can add our renderer to the VideoTrack.
//        localVideoTrack.addSink(binding.localGlSurfaceView);
//
//        binding.localGlSurfaceView.setMirror(true);
//        binding.remoteGlSurfaceView.setMirror(true);

//        gotUserMedia = true;
//        if (SignallingClient.getInstance().isInitiator) {
//            onTryToStart();
//        }
    }

    private void doHungup() {
    }

    private void doAnswer() {
        SignallingClient.getInstance().answer();


    }

    private void doCall() {
//        String tartgetIp = binding.etTartgetIp.getText().toString().trim();
//        int tartgetPort = 10001;
//        int localPort = 10001;
//        SignallingClient.getInstance().callOut(tartgetIp, tartgetPort, localPort);
    }

//    private void onTryToStart() {
//        runOnUiThread(() -> {
//            if (!SignallingClient.getInstance().isStarted && localVideoTrack != null && SignallingClient.getInstance().isChannelReady) {
//                createPeerConnection();
//                SignallingClient.getInstance().isStarted = true;
//                if (SignallingClient.getInstance().isInitiator) {
////                    doCall();
//                }
//            }
//        });
//    }

    /**
     * Creating the local peerconnection instance
     */
//    private void createPeerConnection() {
//        PeerConnection.RTCConfiguration rtcConfig =
//                new PeerConnection.RTCConfiguration(peerIceServers);
//        // TCP candidates are only useful when connecting to a server that supports
//        // ICE-TCP.
//        rtcConfig.tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.DISABLED;
//        rtcConfig.bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE;
//        rtcConfig.rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE;
//        rtcConfig.continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY;
//        // Use ECDSA encryption.
//        rtcConfig.keyType = PeerConnection.KeyType.ECDSA;
//        localPeer = peerConnectionFactory.createPeerConnection(rtcConfig, new CustomPeerConnectionObserver("localPeerCreation") {
//            @Override
//            public void onIceCandidate(IceCandidate iceCandidate) {
//                super.onIceCandidate(iceCandidate);
//                onIceCandidateReceived(iceCandidate);
//            }
//
//            @Override
//            public void onAddStream(MediaStream mediaStream) {
//                showToast("Received Remote stream");
//                super.onAddStream(mediaStream);
//                gotRemoteStream(mediaStream);
//            }
//        });
//
//        addStreamToLocalPeer();
//    }

    private void initVideos() {
//        rootEglBase = EglBase.create();
//        binding.localGlSurfaceView.init(rootEglBase.getEglBaseContext(), null);
//        binding.remoteGlSurfaceView.init(rootEglBase.getEglBaseContext(), null);
//        binding.localGlSurfaceView.setZOrderMediaOverlay(true);
//        binding.remoteGlSurfaceView.setZOrderMediaOverlay(true);
    }

    /**
     * Adding the stream to the localpeer
     */
//    private void addStreamToLocalPeer() {
//        //creating local mediastream
//        MediaStream stream = peerConnectionFactory.createLocalMediaStream("102");
//        stream.addTrack(localAudioTrack);
//        stream.addTrack(localVideoTrack);
//        localPeer.addStream(stream);
//    }

    public void showToast(final String msg) {
        runOnUiThread(() -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show());
    }




    /**
     * Received remote peer's media stream. we will get the first video track and render it
     */
    private void gotRemoteStream(MediaStream stream) {
        //we have remote video stream. add to the renderer.
        final VideoTrack videoTrack = stream.videoTracks.get(0);
        runOnUiThread(() -> {
//            try {
//                binding.remoteGlSurfaceView.setVisibility(View.VISIBLE);
//                videoTrack.addSink(binding.remoteGlSurfaceView);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
        });
    }

    /**
     * Received local ice candidate. Send it to remote peer through signalling for negotiation
     */
    public void onIceCandidateReceived(IceCandidate iceCandidate) {
        //we have received ice candidate. We can set it to the other peer.
//        SignallingClient.getInstance().emitIceCandidate(iceCandidate);
    }

//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == ALL_PERMISSIONS_CODE
//                && grantResults.length == 2
//                && grantResults[0] == PackageManager.PERMISSION_GRANTED
//                && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
//            // all permissions granted
//            start();
//        } else {
//            finish();
//        }
//    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == ALL_PERMISSIONS_CODE
                && grantResults.length == 2
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            // all permissions granted
//            start();
        } else {
//            finish();
        }
    }

}
