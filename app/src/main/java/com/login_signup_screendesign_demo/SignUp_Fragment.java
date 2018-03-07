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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class SignUp_Fragment extends Fragment implements OnClickListener {
	private static View view;
	private static EditText fullName, emailId, mobileNumber, location,
			password, confirmPassword;
	private static TextView login;
	private static Button signUpButton;
	private static CheckBox terms_conditions;
	String receivedUdp2;
	ProgressDialog dialog;
	private static FragmentManager fragmentManager;
	public SignUp_Fragment() {

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.signup_layout, container, false);
		runUdpServer();
		initViews();
		setListeners();
		return view;
	}

	// Initialize all views
	private void initViews() {
		fragmentManager = getActivity().getSupportFragmentManager();
		fullName = (EditText) view.findViewById(R.id.fullName);
		emailId = (EditText) view.findViewById(R.id.userEmailId);
		mobileNumber = (EditText) view.findViewById(R.id.mobileNumber);
		location = (EditText) view.findViewById(R.id.location);
		password = (EditText) view.findViewById(R.id.password);
		confirmPassword = (EditText) view.findViewById(R.id.confirmPassword);
		signUpButton = (Button) view.findViewById(R.id.signUpBtn);
		login = (TextView) view.findViewById(R.id.already_user);
		terms_conditions = (CheckBox) view.findViewById(R.id.terms_conditions);

		// Setting text selector over textviews
		XmlResourceParser xrp = getResources().getXml(R.drawable.text_selector);
		try {
			ColorStateList csl = ColorStateList.createFromXml(getResources(),
					xrp);

			login.setTextColor(csl);
			terms_conditions.setTextColor(csl);
		} catch (Exception e) {
		}
	}

	// Set Listeners
	private void setListeners() {
		signUpButton.setOnClickListener(this);
		login.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.signUpBtn:

			// Call checkValidation method
			checkValidation();
			break;

		case R.id.already_user:

			// Replace login fragment
			new MainActivity().replaceLoginFragment();
			break;
		}

	}

	// Check Validation Method
	private void checkValidation() {

		// Get all edittext texts
		String getFullName = fullName.getText().toString();
		String getEmailId = emailId.getText().toString();
		String getMobileNumber = mobileNumber.getText().toString();
		String getLocation = location.getText().toString();
		String getPassword = password.getText().toString();
		String getConfirmPassword = confirmPassword.getText().toString();

		// Pattern match for email id
		Pattern p = Pattern.compile(Utils.regEx);
		Matcher m = p.matcher(getEmailId);

		// Check if all strings are null or not
		if (getFullName.equals("") || getFullName.length() == 0
				|| getEmailId.equals("") || getEmailId.length() == 0
				|| getMobileNumber.equals("") || getMobileNumber.length() == 0
				|| getLocation.equals("") || getLocation.length() == 0
				|| getPassword.equals("") || getPassword.length() == 0
				|| getConfirmPassword.equals("")
				|| getConfirmPassword.length() == 0)

			new CustomToast().Show_Toast(getActivity(), view,
					"All fields are required.");

		// Check if email id valid or not
		else if (!m.find())
			new CustomToast().Show_Toast(getActivity(), view,
					"Your Email Id is Invalid.");

		// Check if both password should be equal
		else if (!getConfirmPassword.equals(getPassword))
			new CustomToast().Show_Toast(getActivity(), view,
					"Both password doesn't match.");

		// Make sure user should check Terms and Conditions checkbox
		else if (!terms_conditions.isChecked())
			new CustomToast().Show_Toast(getActivity(), view,
					"Please select Terms and Conditions.");

		// Else do signup or do your stuff
		else{
			MongoLabHelperUser checkEmail = new MongoLabHelperUser();
			String sonuc1 = checkEmail.find("{\"email\":\""+emailId.getText().toString()+"\"}");
			if(sonuc1.length() > 10){
				Toast.makeText(getActivity(),"Already User.",Toast.LENGTH_SHORT).show();
			}
			else {
				dialog = ProgressDialog.show(getActivity(), "", "Creating User.", true);
				dialog.show();
				try {
					// ekleyeceğimiz JSON nesnesini oluşturuyoruz.
					JSONObject user = new JSONObject();

					user.put("name", fullName.getText().toString());
					user.put("email", emailId.getText().toString());
					user.put("mobile", mobileNumber.getText().toString());
					user.put("location", location.getText().toString());
					user.put("password", password.getText().toString());

					// insert() metodu kullanarak JSON nesnesini veri tabanına kaydediyoruz.
					MongoLabHelperUser nesneKaydet = new MongoLabHelperUser();
					nesneKaydet.insert(user);

				} catch (JSONException e) {
					e.printStackTrace();
				}
				//add User
				//Thread addUserId = new Thread(new UdpClient(0));//.start();
				//addUserId.start();

				//runUdpServer();
				//check if the user added
				//Thread checkUdpClient = new Thread(new checkUdpClient(0));//.start();
				//checkUdpClient.start();
				MongoLabHelperUser veriBul = new MongoLabHelperUser();
				String sonuc = veriBul.find("{\"email\":\"" + emailId.getText().toString() + "\"}");
				if (sonuc.length() > 10) {
					dialog.dismiss();
					Toast.makeText(getActivity(), "SignUp Successfull.", Toast.LENGTH_SHORT).show();
				} else {
					dialog.dismiss();
					Toast.makeText(getActivity(), "SignUp Failed.", Toast.LENGTH_SHORT).show();
				}
			}
			fragmentManager
					.beginTransaction()
					.setCustomAnimations(R.anim.right_enter, R.anim.left_out)
					.replace(R.id.frameContainer,
							new Login_Fragment(),
							Utils.Login_Fragment).commit();

		}


	}

	public class UdpClient implements Runnable {

		int i = 0;

		public UdpClient(int i) {
			this.i = i;
		}

		public void run() {
			final String udpMsg;

			String getFullName = fullName.getText().toString();
			String getEmailId = emailId.getText().toString();
			String getMobileNumber = mobileNumber.getText().toString();
			String getLocation = location.getText().toString();
			String getPassword = password.getText().toString();

			udpMsg = getFullName+"-"+getEmailId+"-"+getMobileNumber+"-"+getLocation+"-"+getPassword;

			DatagramSocket ds = null;
			try {
				ds = new DatagramSocket();
				InetAddress serverAddr = InetAddress.getByName("192.168.43.73");
				DatagramPacket dp;
				dp = new DatagramPacket(udpMsg.getBytes(), udpMsg.length(), serverAddr, 10000);
				//dialog = ProgressDialog.show(getActivity(), "","Creating User." , true);
				//dialog.show();
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
	public class checkUdpClient implements Runnable {

		int i = 0;

		public checkUdpClient(int i) {
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
				dp = new DatagramPacket(udpMsg.getBytes(), udpMsg.length(), serverAddr, 10002);
				//dialog = ProgressDialog.show(getActivity(), "","Creating User." , true);
				//dialog.show();
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
					int port = 9990;

					DatagramSocket dsocket = new DatagramSocket(port);
					byte[] buffer = new byte[2048];
					DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
					while (true) {
						dsocket.receive(packet);
						receivedUdp2 = new String(buffer, 0, packet.getLength());
						Log.i("UDP packet received", receivedUdp2);
						dialog.dismiss();
						fragmentManager
								.beginTransaction()
								.setCustomAnimations(R.anim.right_enter, R.anim.left_out)
								.replace(R.id.frameContainer,
										new Login_Fragment(),
										Utils.Login_Fragment).commit();

						if(receivedUdp2.compareTo("signUpUdp:Successful")==0){
							if (getActivity()!=null) {
								getActivity().runOnUiThread(new Runnable() {
									@Override
									public void run() {
										dialog.dismiss();
										Log.i("if dialog.dismiss()","ddd");
										Toast.makeText(getActivity(),"Successful Sign Up. Please Login.",Toast.LENGTH_SHORT).show();
									}
								});
							}
							return;
						}else if(receivedUdp2.compareTo("signUpUdp:Failed")==0){
							dialog.dismiss();
							if (getActivity()!=null) {
								getActivity().runOnUiThread(new Runnable() {
									@Override
									public void run() {
										dialog.dismiss();
										Log.i("else if dialog.dismiss()","ddd");
										Toast.makeText(getActivity(),"Unsuccessful Login. Please try again.",Toast.LENGTH_SHORT).show();
									}
								});
							}
							return;
						}else{
							dialog.dismiss();
							Log.i("else dialog.dismiss()","ddd");
						}

						//data.setText(lText);
						packet.setLength(buffer.length);
						//break;
					}
				} catch (Exception e) {
					System.err.println(e);
					e.printStackTrace();
				}
			}
		}).start();
	}
}
