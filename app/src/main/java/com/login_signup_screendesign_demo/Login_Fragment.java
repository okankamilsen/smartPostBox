package com.login_signup_screendesign_demo;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.login_signup_screendesign_demo.R;
import com.wang.avi.AVLoadingIndicatorView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.XmlResourceParser;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Login_Fragment extends Fragment implements OnClickListener {
	private static View view;

	private static EditText emailid, password;
	private static Button loginButton;
	private static TextView forgotPassword, signUp;
	private static CheckBox show_hide_password;
	private static LinearLayout loginLayout;
	private static Animation shakeAnimation;
	private static FragmentManager fragmentManager;
	String receivedUdp;
	ProgressDialog dialog;
	public Login_Fragment() {

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.login_layout, container, false);
		receivedUdp="";
		initViews();
		setListeners();
		return view;
	}

	// Initiate Views
	private void initViews() {
		fragmentManager = getActivity().getSupportFragmentManager();

		emailid = (EditText) view.findViewById(R.id.login_emailid);
		password = (EditText) view.findViewById(R.id.login_password);
		loginButton = (Button) view.findViewById(R.id.loginBtn);
		forgotPassword = (TextView) view.findViewById(R.id.forgot_password);
		signUp = (TextView) view.findViewById(R.id.createAccount);
		show_hide_password = (CheckBox) view
				.findViewById(R.id.show_hide_password);
		loginLayout = (LinearLayout) view.findViewById(R.id.login_layout);

		// Load ShakeAnimation
		shakeAnimation = AnimationUtils.loadAnimation(getActivity(),
				R.anim.shake);

		// Setting text selector over textviews
		XmlResourceParser xrp = getResources().getXml(R.drawable.text_selector);
		try {
			ColorStateList csl = ColorStateList.createFromXml(getResources(),
					xrp);

			forgotPassword.setTextColor(csl);
			show_hide_password.setTextColor(csl);
			signUp.setTextColor(csl);
		} catch (Exception e) {
		}
	}

	// Set Listeners
	private void setListeners() {
		loginButton.setOnClickListener(this);
		forgotPassword.setOnClickListener(this);
		signUp.setOnClickListener(this);

		// Set check listener over checkbox for showing and hiding password
		show_hide_password
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton button,
							boolean isChecked) {

						// If it is checkec then show password else hide
						// password
						if (isChecked) {

							show_hide_password.setText(R.string.hide_pwd);// change
																			// checkbox
																			// text

							password.setInputType(InputType.TYPE_CLASS_TEXT);
							password.setTransformationMethod(HideReturnsTransformationMethod
									.getInstance());// show password
						} else {
							show_hide_password.setText(R.string.show_pwd);// change
																			// checkbox
																			// text

							password.setInputType(InputType.TYPE_CLASS_TEXT
									| InputType.TYPE_TEXT_VARIATION_PASSWORD);
							password.setTransformationMethod(PasswordTransformationMethod
									.getInstance());// hide password

						}

					}
				});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.loginBtn:
			checkValidation();
			break;

		case R.id.forgot_password:

			// Replace forgot password fragment with animation
			fragmentManager
					.beginTransaction()
					.setCustomAnimations(R.anim.right_enter, R.anim.left_out)
					.replace(R.id.frameContainer,
							new ForgotPassword_Fragment(),
							Utils.ForgotPassword_Fragment).commit();
			break;
		case R.id.createAccount:

			// Replace signup frgament with animation
			fragmentManager
					.beginTransaction()
					.setCustomAnimations(R.anim.right_enter, R.anim.left_out)
					.replace(R.id.frameContainer, new SignUp_Fragment(),
							Utils.SignUp_Fragment).commit();
			break;
		}

	}

	// Check Validation before login
	private void checkValidation() {
		// Get email id and password
		String getEmailId = emailid.getText().toString();
		String getPassword = password.getText().toString();

		// Check patter for email id
		Pattern p = Pattern.compile(Utils.regEx);

		Matcher m = p.matcher(getEmailId);

		// Check for both field is empty or not
		if (getEmailId.equals("") || getEmailId.length() == 0
				|| getPassword.equals("") || getPassword.length() == 0) {
			loginLayout.startAnimation(shakeAnimation);
			new CustomToast().Show_Toast(getActivity(), view,
					"Enter both credentials.");

		}
		// Check if email id is valid or not
		else if (!m.find())
			new CustomToast().Show_Toast(getActivity(), view,
					"Your Email Id is Invalid.");
		// Else do login and do your stuff
		else{
			dialog = ProgressDialog.show(getActivity(), "","Checking User Data" , true);
			dialog.show();
			MongoLabHelperUser veriBul = new MongoLabHelperUser();
			String sonuc = veriBul.find("{\"email\":\"" + getEmailId + "\",\"password\":\""+getPassword+"\"}");

			//Log.e("_"+sonuc+"_","");
			//Toast.makeText(getActivity(),sonuc.length(),Toast.LENGTH_SHORT).show();
			if (sonuc.length()>10) {
				dialog.dismiss();
				Toast.makeText(getActivity(), "Login Successfull.", Toast.LENGTH_SHORT).show();
				fragmentManager
						.beginTransaction()
						.setCustomAnimations(R.anim.right_enter, R.anim.left_out)
						.replace(R.id.frameContainer,
								new MainFragment(),
								Utils.MainFragment).commit();
			} else {
				dialog.dismiss();
				Toast.makeText(getActivity(), "Check your email and password.", Toast.LENGTH_SHORT).show();
			}
			//Toast.makeText(getActivity(), "Do Login.", Toast.LENGTH_SHORT)
			//		.show();
			//Thread checkUserId = new Thread(new UdpClient(0));//.start();
			//checkUserId.start();
			//dialog = ProgressDialog.show(getActivity(), "","Checking User Data" , true);
			//dialog.show();
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

			String getEmailId = emailid.getText().toString();
			String getPassword = password.getText().toString();
			udpMsg = "{\"email\":\""+getEmailId+"\",\"password\":\""+getPassword+"\"}";

			DatagramSocket ds = null;
			try {
				ds = new DatagramSocket();
				InetAddress serverAddr = InetAddress.getByName("192.168.43.110");
				DatagramPacket dp;
				dp = new DatagramPacket(udpMsg.getBytes(), udpMsg.length(), serverAddr, 10001);
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
					int port = 9991;

					DatagramSocket dsocket = new DatagramSocket(port);
					byte[] buffer = new byte[2048];
					DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

					while (true) {

						dsocket.receive(packet);
						receivedUdp = new String(buffer, 0, packet.getLength());
						Log.i("UDP packet received", receivedUdp);
						if(receivedUdp.compareTo("loginUdp:Successful")==0){
							dialog.dismiss();
							Log.i("dialog.dismiss()","ddd");
							fragmentManager
									.beginTransaction()
									.setCustomAnimations(R.anim.right_enter, R.anim.left_out)
									.replace(R.id.frameContainer,
											new MainFragment(),
											Utils.MainFragment).commit();
							getActivity().runOnUiThread(new Runnable() {

								@Override
								public void run() {
									Toast.makeText(getActivity(),"Successful Login.",Toast.LENGTH_SHORT).show();
								}
							});
						}else if(receivedUdp.compareTo("loginUdp:Failed")==0){
							dialog.dismiss();
							getActivity().runOnUiThread(new Runnable() {

								@Override
								public void run() {
									Toast.makeText(getActivity(),"Unsuccessful Login.",Toast.LENGTH_SHORT).show();
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
