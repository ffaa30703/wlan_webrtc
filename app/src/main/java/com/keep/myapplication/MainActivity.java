package com.keep.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;

import com.keep.myapplication.databinding.ActivityMainBinding;
import com.keep.myapplication.net.INetClient;
import com.keep.myapplication.net.Message;
import com.keep.myapplication.net.NetUtils;
import com.keep.myapplication.net.netty.NettyClient;

public class MainActivity extends AppCompatActivity {
    INetClient mNetClient;
    ActivityMainBinding binding;

    INetClient.IReceiveHandler mReceiveHandler = (message, senderIp, senderPort) -> {
        String msg = "type:" + message.getType() + " info:" + message.getInfo() + "\n";
        binding.tvReceiveMsg.setText(msg);
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.btConnect.setOnClickListener(v -> {
            String targetIp = binding.etRemoteIp.getText().toString().trim();
            int targetPort = Integer.valueOf(binding.etRemotePort.getText().toString().trim());
            int localPort = Integer.valueOf(binding.etLocalPort.getText().toString().trim());
//            mNetClient = new NettyClient.Builder().targetIp(targetIp)
//                    .targetport(targetPort)
//                    .localPort(localPort)
//                    .receiveHandler(mReceiveHandler)
//                    .build();
            mNetClient=new NettyClient(localPort,mReceiveHandler);
            mNetClient.setRemote(targetIp,targetPort);
            binding.btSend.setEnabled(true);

        });
        binding.btSend.setOnClickListener(v -> {
            String sendMsg = binding.etSendMsg.getText().toString().trim();

            mNetClient.send(new Message("1", sendMsg));
        });
        binding.tvLocalIp.setText(NetUtils.getIPAddress(getApplicationContext()));


    }
}
