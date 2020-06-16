package com.keep.wlanwebrtcc;

import com.google.gson.Gson;
import com.keep.wlanwebrtcc.net.INetClient;
import com.keep.wlanwebrtcc.net.Message;
import com.keep.wlanwebrtcc.net.netty.NettyClient;

import org.json.JSONObject;
import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;

/**
 * author : Jiyf
 * e-mail : ffaa30703@icloud.com
 * time   : 2020/06/15
 * desc   :
 * version: 1.0
 */
public class SignallingClient {
    public static final String CALLOUT = "callout";
    public static final String ANSWER = "answer";
    public static final String HUNGUP = "hungup";
    private static final String OFFER_SDP = "offer_sdp";
    private static final String ANSWER_SDP = "answer_sdp";
    private static final String ICE_CANDIDATE = "ice_candidate";
    static SignallingClient INSTANCE = null;
    public boolean isInitiator = false;
    public boolean isStarted;
    public boolean isChannelReady;

    INetClient mNetClient;

    Gson gson = new Gson();
    SignalingInterface mSignalingInterface;


    INetClient.IReceiveHandler mReceiveHandler = (message, senderIp, senderPort) -> {
        switch (message.getType()) {
            case CALLOUT:
                mSignalingInterface.callin();
                mNetClient.setRemote(senderIp, senderPort);
                break;
            case ANSWER:
                isChannelReady = true;
                mSignalingInterface.receiveAnswer();
                break;
            case OFFER_SDP: {
                SessionDescription sessionDescription = gson.fromJson(message.getInfo(), SessionDescription.class);
                mSignalingInterface.receiveOfferSdp(sessionDescription);
            }
            break;
            case ANSWER_SDP: {
                SessionDescription sessionDescription = gson.fromJson(message.getInfo(), SessionDescription.class);
                mSignalingInterface.receiveAnswerSdp(sessionDescription);
            }
            break;
            case ICE_CANDIDATE:
                IceCandidate iceCandidate = gson.fromJson(message.getInfo(), IceCandidate.class);
                mSignalingInterface.receiveIceCandidate(iceCandidate);
                break;
            case HUNGUP:
                mSignalingInterface.Hungup();
                break;

        }

    };

    public SignallingClient() {

    }

    public static SignallingClient getInstance() {
        if (INSTANCE == null)
            INSTANCE = new SignallingClient();
        return INSTANCE;
    }

    public void init(SignalingInterface signalingInterface) {
        mSignalingInterface = signalingInterface;
        mNetClient = new NettyClient(10001, mReceiveHandler);
        isInitiator = true;
    }


    public void callOut(String tartgetIp, int tartgetPort) {
        mNetClient.setRemote(tartgetIp, tartgetPort);
        mNetClient.send(new Message(CALLOUT, ""));

    }

    public void answer() {
        isChannelReady = true;
        mNetClient.send(new Message(ANSWER, ""));
    }

    public void sendOfferSdp(SessionDescription sessionDescription) {
        Message message = new Message(OFFER_SDP, gson.toJson(sessionDescription));

        mNetClient.send(message);
    }

    public void sendAnswer(SessionDescription sessionDescription) {
        Message message = new Message(ANSWER_SDP, gson.toJson(sessionDescription));
        mNetClient.send(message);
    }

    public void sendIceCandidate(IceCandidate iceCandidate) {
        Message message = new Message(ICE_CANDIDATE, gson.toJson(iceCandidate));
//        message.setObject(iceCandidate);
        mNetClient.send(message);

    }

    public void sendHungup() {
        Message message = new Message(HUNGUP, "");
        mNetClient.send(message);
    }

    public void reset() {
        isStarted = false;
        isChannelReady = false;
    }


    interface SignalingInterface {
        void onRemoteHangUp(String msg);

        void onOfferReceived(JSONObject data);

        void onAnswerReceived(JSONObject data);

        void onIceCandidateReceived(JSONObject data);

        void onTryToStart();

        void onCreatedRoom();

        void onJoinedRoom();

        void onNewPeerJoined();

        void callin();

        void receiveAnswer();

        void receiveOfferSdp(SessionDescription sessionDescription);

        void receiveAnswerSdp(SessionDescription sessionDescription);

        void receiveIceCandidate(IceCandidate iceCandidate);

        void Hungup();
    }
}
