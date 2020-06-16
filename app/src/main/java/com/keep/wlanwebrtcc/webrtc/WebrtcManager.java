package com.keep.wlanwebrtcc.webrtc;

import android.content.Context;
import android.util.Log;
import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.Logging;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.util.ArrayList;
import java.util.List;

/**
 * author : Jiyf
 * e-mail : ffaa30703@icloud.com
 * time   : 2020/06/16
 * desc   :
 * version: 1.0
 */
public class WebrtcManager {
    private static final String TAG = "WebrtcManager";
    EglBase rootEglBase = EglBase.create();
    Context mContext;

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

    MediaConstraints sdpConstraints;
    EventUICallBack mEventUICallBack;
    VideoCapturer videoCapturerAndroid;

    public WebrtcManager(Context mContext) {
        this.mContext = mContext;
    }

    public void doCall(String targetIp, int targetPort) {
        SignallingClient.getInstance().callOut(targetIp, targetPort);
    }

    public void doAnswer() {
        if (videoCapturerAndroid != null) {
            videoCapturerAndroid.startCapture(1024, 720, 30);
        }
        SignallingClient.getInstance().answer();
    }

    public void doHungup() {
        localPeer.close();
        localPeer = null;
        try {
            videoCapturerAndroid.stopCapture();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        SignallingClient.getInstance().sendHungup();
        SignallingClient.getInstance().reset();

    }

    public void init(SurfaceViewRenderer localGlSurfaceView, SurfaceViewRenderer remoteGlSurfaceView) {
        localGlSurfaceView.init(rootEglBase.getEglBaseContext(), null);
        remoteGlSurfaceView.init(rootEglBase.getEglBaseContext(), null);
        localGlSurfaceView.setZOrderMediaOverlay(true);
        remoteGlSurfaceView.setZOrderMediaOverlay(true);

        SignallingClient.getInstance().init(mSignalingInterface);

        //Initialize PeerConnectionFactory globals.
        PeerConnectionFactory.InitializationOptions initializationOptions =
                PeerConnectionFactory.InitializationOptions.builder(mContext.getApplicationContext())
                        .createInitializationOptions();
        PeerConnectionFactory.initialize(initializationOptions);

        //Create a new PeerConnectionFactory instance - using Hardware encoder and decoder.
        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
        DefaultVideoEncoderFactory defaultVideoEncoderFactory = new DefaultVideoEncoderFactory(
                rootEglBase.getEglBaseContext(),  /* enableIntelVp8Encoder */true,  /* enableH264HighProfile */true);
        DefaultVideoDecoderFactory defaultVideoDecoderFactory = new DefaultVideoDecoderFactory(rootEglBase.getEglBaseContext());
        peerConnectionFactory = PeerConnectionFactory.builder()
                .setOptions(options)
                .setVideoEncoderFactory(defaultVideoEncoderFactory)
                .setVideoDecoderFactory(defaultVideoDecoderFactory)
                .createPeerConnectionFactory();


        //Now create a VideoCapturer instance.

        videoCapturerAndroid = createVideoCapturer();

        //Create MediaConstraints - Will be useful for specifying video and audio constraints.
        audioConstraints = new MediaConstraints();
        videoConstraints = new MediaConstraints();


        //Create a VideoSource instance
        if (videoCapturerAndroid != null) {
            surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", rootEglBase.getEglBaseContext());
            videoSource = peerConnectionFactory.createVideoSource(videoCapturerAndroid.isScreencast());
            videoCapturerAndroid.initialize(surfaceTextureHelper, mContext.getApplicationContext(), videoSource.getCapturerObserver());
        }
        localVideoTrack = peerConnectionFactory.createVideoTrack("100", videoSource);


        //create an AudioSource instance
        audioSource = peerConnectionFactory.createAudioSource(audioConstraints);
        localAudioTrack = peerConnectionFactory.createAudioTrack("101", audioSource);


        // And finally, with our VideoRenderer ready, we
        // can add our renderer to the VideoTrack.
        localVideoTrack.addSink(localGlSurfaceView);

        localGlSurfaceView.setMirror(true);
        remoteGlSurfaceView.setMirror(true);
        gotUserMedia = true;
    }

    private VideoCapturer createVideoCapturer() {
        if (Camera2Enumerator.isSupported(mContext.getApplicationContext())) {
            return createCameraCapturer(new Camera2Enumerator(mContext));
        } else {
            return createCameraCapturer(new Camera1Enumerator(true));
        }
    }

    private VideoCapturer createCameraCapturer(CameraEnumerator enumerator) {
        final String[] deviceNames = enumerator.getDeviceNames();

        // First, try to find front facing camera
        Logging.d(TAG, "Looking for front facing cameras.");
        for (String deviceName : deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                Logging.d(TAG, "Creating front facing camera capturer.");
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);
                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        // Front facing camera not found, try something else
        Logging.d(TAG, "Looking for other cameras.");
        for (String deviceName : deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                Logging.d(TAG, "Creating other camera capturer.");
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        return null;
    }


    SignallingClient.SignalingInterface mSignalingInterface = new SignallingClient.SignalingInterface() {


        @Override
        public void callin(String senderIp) {
            if (mEventUICallBack != null)
                mEventUICallBack.callin(senderIp);
//            runOnUiThread(ChatingActivity.this::callIn);
        }

        @Override
        public void receiveAnswer() {
            Log.d(TAG, "receiveAnswer: ");
            if (videoCapturerAndroid != null) {
                videoCapturerAndroid.startCapture(1024, 720, 30);
            }

            if (!SignallingClient.getInstance().isStarted && localVideoTrack != null && SignallingClient.getInstance().isChannelReady) {
                createPeerConnection();
                SignallingClient.getInstance().isStarted = true;
                if (SignallingClient.getInstance().isInitiator) {
//                    doCall();
                    createOffer();
                }
            }

        }

        @Override
        public void receiveOfferSdp(SessionDescription sessionDescription) {
//            showToast("receiveOfferSdp");
            Log.d(TAG, "receiveOfferSdp: ");
            if (!SignallingClient.getInstance().isStarted && localVideoTrack != null && SignallingClient.getInstance().isChannelReady) {
                createPeerConnection();
                localPeer.setRemoteDescription(new CustomSdpObserver("localSetRemote"), new SessionDescription(SessionDescription.Type.OFFER, sessionDescription.description));
                SignallingClient.getInstance().isStarted = true;

            }
            createAnswer();
        }

        @Override
        public void receiveAnswerSdp(SessionDescription sessionDescription) {
//            showToast("receiveAnswerSdp");
            Log.d(TAG, "receiveAnswerSdp: ");
            localPeer.setRemoteDescription(new CustomSdpObserver("localSetRemote"), new SessionDescription(SessionDescription.Type.fromCanonicalForm(sessionDescription.type.canonicalForm().toLowerCase()), sessionDescription.description));

        }

        @Override
        public void receiveIceCandidate(IceCandidate iceCandidate) {
//            showToast("receiveIceCandidate");
            Log.d(TAG, "receiveIceCandidate: ");
            localPeer.addIceCandidate(new IceCandidate(iceCandidate.sdpMid, iceCandidate.sdpMLineIndex, iceCandidate.sdp));
        }

        @Override
        public void receiveHungup() {
            localPeer.close();
            localPeer = null;
            try {
                videoCapturerAndroid.stopCapture();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            SignallingClient.getInstance().reset();
            if (mEventUICallBack != null) {
                mEventUICallBack.receiveHungup();
            }
        }
    };

    private void createAnswer() {
        localPeer.createAnswer(new CustomSdpObserver("localCreateAns") {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                Log.d(TAG, "createAnswer: send sdp");
                super.onCreateSuccess(sessionDescription);
                localPeer.setLocalDescription(new CustomSdpObserver("localSetLocal"), sessionDescription);
                SignallingClient.getInstance().sendAnswerSdp(sessionDescription);
            }
        }, new MediaConstraints());
    }

    /**
     * This method is called when the app is the initiator - We generate the offer and send it over through socket
     * to remote peer
     */
    private void createOffer() {
//        showToast("createOffer");
        Log.d(TAG, "createOffer: ");
        sdpConstraints = new MediaConstraints();
        sdpConstraints.mandatory.add(
                new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "false"));
        sdpConstraints.mandatory.add(
                new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
        localPeer.createOffer(new CustomSdpObserver("localCreateOffer") {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                super.onCreateSuccess(sessionDescription);
                localPeer.setLocalDescription(new CustomSdpObserver("localSetLocalDesc"), sessionDescription);
                Log.d(TAG, "send offer sdp emit ");
                SignallingClient.getInstance().sendOfferSdp(sessionDescription);
            }
        }, sdpConstraints);
    }

    private void createPeerConnection() {
        PeerConnection.RTCConfiguration rtcConfig =
                new PeerConnection.RTCConfiguration(peerIceServers);
        // TCP candidates are only useful when connecting to a server that supports
        // ICE-TCP.
        rtcConfig.tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.DISABLED;
        rtcConfig.bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE;
        rtcConfig.rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE;
        rtcConfig.continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY;
        // Use ECDSA encryption.
        rtcConfig.keyType = PeerConnection.KeyType.ECDSA;
        localPeer = peerConnectionFactory.createPeerConnection(rtcConfig, new CustomPeerConnectionObserver("localPeerCreation") {
            @Override
            public void onIceCandidate(IceCandidate iceCandidate) {
                super.onIceCandidate(iceCandidate);
//                showToast("onIceCandidate");
                Log.d(TAG, "onIceCandidate: send icecandidate");
                onIceCandidateReceived(iceCandidate);
            }

            @Override
            public void onAddStream(MediaStream mediaStream) {
//                showToast("Received Remote stream");
                Log.d(TAG, "onAddStream: receive stream");
                super.onAddStream(mediaStream);
                gotRemoteStream(mediaStream);
            }
        });

        addStreamToLocalPeer();
    }

    /**
     * Adding the stream to the localpeer
     */
    private void addStreamToLocalPeer() {
        //creating local mediastream
        MediaStream stream = peerConnectionFactory.createLocalMediaStream("102");
        stream.addTrack(localAudioTrack);
        stream.addTrack(localVideoTrack);
        localPeer.addStream(stream);
    }

    /**
     * Received remote peer's media stream. we will get the first video track and render it
     */
    private void gotRemoteStream(MediaStream stream) {
        //we have remote video stream. add to the renderer.
        final VideoTrack videoTrack = stream.videoTracks.get(0);
        if (mEventUICallBack != null) {
            mEventUICallBack.showVideo(videoTrack);
        }
//        runOnUiThread(() -> {
//            try {
//                binding.remoteGlSurfaceView.setVisibility(View.VISIBLE);
//                videoTrack.addSink(binding.remoteGlSurfaceView);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        });
    }


    /**
     * Received local ice candidate. Send it to remote peer through signalling for negotiation
     */
    public void onIceCandidateReceived(IceCandidate iceCandidate) {
        //we have received ice candidate. We can set it to the other peer.
        SignallingClient.getInstance().sendIceCandidate(iceCandidate);
    }

    public void setEventUICallback(EventUICallBack eventUICallBack) {
        mEventUICallBack = eventUICallBack;
    }

    public interface EventUICallBack {

        void callin(String senderIp);

        void showVideo(VideoTrack videoTrack);

        void receiveHungup();
    }
}
