package com.login_signup_screendesign_demo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.login_signup_screendesign_demo.R;
import com.wang.avi.AVLoadingIndicatorView;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.XmlResourceParser;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NotificationCompat;
import android.text.InputType;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainFragment extends Fragment implements OnClickListener {
    private static View view;

    private static Button openBt;
    private static Button closeBt;
    private static LinearLayout MainFragmentLayout;
    private static Animation shakeAnimation;
    private static FragmentManager fragmentManager;
    private ListView mConversationView;
    private ArrayAdapter<String> mConversationArrayAdapter;
    String sendText;
    String jsonText;
    MongoLabHelper mongoLabHelper,mongoLabHelperCount,mongoLabHelperCount1;
    private AVLoadingIndicatorView avi;
    public String TAG="log";
    int count=0;
    JSONArray jsonArray;
    public MainFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.main_fragment_layout, container, false);
        initViews();
        setListeners();
        return view;
    }

    // Initiate Views
    private void initViews() {
        fragmentManager = getActivity().getSupportFragmentManager();
        mConversationView = (ListView) view.findViewById(R.id.in);
        openBt = (Button) view.findViewById(R.id.openBt);
        closeBt = (Button) view.findViewById(R.id.closeBt);

        // Load ShakeAnimation
        shakeAnimation = AnimationUtils.loadAnimation(getActivity(),
                R.anim.shake);
        setupChat();

    }

    // Set Listeners
    private void setListeners() {
        openBt.setOnClickListener(this);
        closeBt.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.openBt:
                Thread openP = new Thread(new UdpClient(0));//.start();
                sendText = "open";
                openP.start();
                break;

            case R.id.closeBt:
                Thread closeP = new Thread(new UdpClient(0));//.start();
                sendText = "close";
                closeP.start();
                break;

        }

    }

    private void setupChat() {

        mongoLabHelper = new MongoLabHelper();
        mongoLabHelperCount = new MongoLabHelper();
        mongoLabHelperCount1 = new MongoLabHelper();
        // Initialize the array adapter for the conversation thread
        mConversationArrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.message);

        mConversationView.setAdapter(mConversationArrayAdapter);


        runMongoDb();
    }
    public ProgressDialog createProgress(){
        ProgressDialog prog= new ProgressDialog(getActivity());//Assuming that you are using fragments.
        prog.setTitle("pleaseWait");
        prog.setMessage("webpage_being_loaded");
        prog.setCancelable(false);
        prog.setIndeterminate(true);
        prog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        return prog;
    }
    public void progress(ProgressDialog prog,boolean a){

        if(a=true){
            prog.show();
        }else
            prog.dismiss();
    }
    public class UdpClient implements Runnable {

        int i = 0;

        public UdpClient(int i) {
            this.i = i;
        }

        public void run() {
            final String udpMsg;

            udpMsg = sendText;

            DatagramSocket ds = null;
            try {
                ds = new DatagramSocket();
                InetAddress serverAddr = InetAddress.getByName("192.168.43.110");
                DatagramPacket dp;
                dp = new DatagramPacket(udpMsg.getBytes(), udpMsg.length(), serverAddr, 9999);
                ds.send(dp);
            } catch (SocketException e) {
                e.printStackTrace();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (ds != null) {
                    ds.close();
                }
            }
        }
    }

    private void runMongoDb() {

        new Thread(new Runnable() {
            public void run() {

                try {
                    jsonText = mongoLabHelper.getCollection();
                    count=mongoLabHelperCount.count("{\"find\":true}");
                    jsonText="{\"mailboxdata\":"+jsonText+"}";
                    JSONObject jsnobject = new JSONObject(jsonText);
                    jsonArray = jsnobject.getJSONArray("mailboxdata");

                    getActivity().runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject explrObject = null;
                                try {
                                    explrObject = jsonArray.getJSONObject(i);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                try {
                                    mConversationArrayAdapter.add(explrObject.getString("time")+" "+explrObject.getString("message"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                    while (true) {
                        //int countTest=new MongoLabHelper().count("{\"find\":true}");
                        //if(countTest>count){
                            jsonText = new MongoLabHelper().getCollection();
                            jsonText="{\"mailboxdata\":"+jsonText+"}";
                            JSONObject jsnobject1 = new JSONObject(jsonText);
                            jsonArray = jsnobject1.getJSONArray("mailboxdata");
                            getActivity().runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    mConversationArrayAdapter.clear();
                                    JSONObject explrObject = null;
                                    for (int i = 0; i < jsonArray.length(); i++) {

                                        try {
                                            explrObject = jsonArray.getJSONObject(i);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        try {
                                            mConversationArrayAdapter.add(explrObject.getString("time")+" "+explrObject.getString("message"));
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            });
                        //}

                    }
                } catch (Exception e) {
                    System.err.println(e);
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
