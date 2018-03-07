package com.login_signup_screendesign_demo;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.ProgressDialog;
import android.content.res.ColorStateList;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ForgotPassword_Fragment extends Fragment implements
		OnClickListener {
	private static View view;

	private static EditText emailId;
	private static TextView submit, back;
	String receivedUdp1;
	String sendtext;
	private static FragmentManager fragmentManager;
	public ForgotPassword_Fragment() {

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.forgotpassword_layout, container,
				false);
		fragmentManager = getActivity().getSupportFragmentManager();
		initViews();
		setListeners();
		return view;
	}

	// Initialize the views
	private void initViews() {
		emailId = (EditText) view.findViewById(R.id.registered_emailid);
		submit = (TextView) view.findViewById(R.id.forgot_button);
		back = (TextView) view.findViewById(R.id.backToLoginBtn);

		// Setting text selector over textviews
		XmlResourceParser xrp = getResources().getXml(R.drawable.text_selector);
		try {
			ColorStateList csl = ColorStateList.createFromXml(getResources(),
					xrp);

			back.setTextColor(csl);
			submit.setTextColor(csl);

		} catch (Exception e) {
		}

	}

	// Set Listeners over buttons
	private void setListeners() {
		back.setOnClickListener(this);
		submit.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.backToLoginBtn:

			// Replace Login Fragment on Back Presses
			new MainActivity().replaceLoginFragment();
			break;

		case R.id.forgot_button:

			// Call Submit button task
			submitButtonTask();
			break;

		}

	}

	private void submitButtonTask() {
		String getEmailId = emailId.getText().toString();

		// Pattern for email id validation
		Pattern p = Pattern.compile(Utils.regEx);

		// Match the pattern
		Matcher m = p.matcher(getEmailId);

		// First check if email id is not null else show error toast
		if (getEmailId.equals("") || getEmailId.length() == 0)

			new CustomToast().Show_Toast(getActivity(), view,
					"Please enter your Email Id.");

		// Check if email id is valid or not
		else if (!m.find())
			new CustomToast().Show_Toast(getActivity(), view,
					"Your Email Id is Invalid.");

		// Else submit email id and fetch passwod or do your stuff
		else{
			//Toast.makeText(getActivity(), "Get Forgot Password.",
			//		Toast.LENGTH_SHORT).show();
			sendtext=getEmailId;
			MongoLabHelperUser veriBul = new MongoLabHelperUser();
			String sonuc = veriBul.find("{\"email\":\"" + getEmailId + "\"}");

			//Log.e("_"+sonuc+"_","");
			//Toast.makeText(getActivity(),sonuc.length(),Toast.LENGTH_SHORT).show();
			if (sonuc.length()>10) {
				sonuc="{\"userData\":"+sonuc+"}";
				JSONObject jsnobject = null;
				try {
					jsnobject = new JSONObject(sonuc);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				JSONArray jsonArray = null;
				try {
					jsonArray = jsnobject.getJSONArray("userData");
				} catch (JSONException e) {
					e.printStackTrace();
				}
				JSONObject explrObject = null;
				try {
					explrObject = jsonArray.getJSONObject(0);
				} catch (JSONException e) {
					e.printStackTrace();
				}
                try {
                    Toast.makeText(getActivity(),"Password: "+explrObject.getString("password").toString(),Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                fragmentManager
						.beginTransaction()
						.setCustomAnimations(R.anim.right_enter, R.anim.left_out)
						.replace(R.id.frameContainer,
								new Login_Fragment(),
								Utils.Login_Fragment).commit();
			} else {
				Toast.makeText(getActivity(), "There is no email in db.", Toast.LENGTH_SHORT).show();
			}
			//Thread sendEmail = new Thread(new UdpClient(0));//.start();
			//sendEmail.start();
			//runUdpServer();
		}

	}
	public class UdpClient implements Runnable {

		int i = 0;

		public UdpClient(int i) {
			this.i = i;
		}

		public void run() {
			final String udpMsg;

			String getEmailId = emailId.getText().toString();
			udpMsg = "{\"email\":\""+getEmailId+"\"}";

			DatagramSocket ds = null;
			try {
				ds = new DatagramSocket();
				InetAddress serverAddr = InetAddress.getByName("192.168.43.73");
				DatagramPacket dp;
				dp = new DatagramPacket(udpMsg.getBytes(), udpMsg.length(), serverAddr, 10003);
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
	private void runUdpServer() {

		new Thread(new Runnable() {
			public void run() {
				byte[] lMsg = new byte[9000000];
				DatagramPacket dp = new DatagramPacket(lMsg, lMsg.length);
				DatagramSocket ds=null;
				try {
					int port = 9993;

					DatagramSocket dsocket = new DatagramSocket(port);
					byte[] buffer = new byte[2048];
					DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

					while (true) {

						dsocket.receive(packet);
						receivedUdp1 = new String(buffer, 0, packet.getLength());
						Log.i("UDP packet received", receivedUdp1);
						if(receivedUdp1.compareTo("passwordUdp:Successful")==0){
							Log.i("dialog.dismiss()","ddd");
							fragmentManager
									.beginTransaction()
									.setCustomAnimations(R.anim.right_enter, R.anim.left_out)
									.replace(R.id.frameContainer,
											new Login_Fragment(),
											Utils.Login_Fragment).commit();
							getActivity().runOnUiThread(new Runnable() {

								@Override
								public void run() {
									Toast.makeText(getActivity(),"Please check your email. We've sent your password to your email.",Toast.LENGTH_SHORT).show();
								}
							});
						}else if(receivedUdp1.compareTo("passwordUdp:Failed")==0){
							getActivity().runOnUiThread(new Runnable() {

								@Override
								public void run() {
									Toast.makeText(getActivity(),"There is no user like this.",Toast.LENGTH_SHORT).show();
								}
							});
						}

						//data.setText(lText);

						packet.setLength(buffer.length);
					}
				} catch (Exception e) {
					System.err.println(e);
					e.printStackTrace();
				}
			}
		}).start();
	}
}