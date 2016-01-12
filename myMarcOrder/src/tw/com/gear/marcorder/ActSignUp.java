package tw.com.gear.marcorder;


import java.net.URL;
import java.util.ArrayList;

import tw.com.gear.marcorder.R;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.os.Build;

public class ActSignUp extends Activity {
	
	private static String ResultMsg = "";
	private static final int GUI_OK = 0x101;
	private ArrayList<String> signUpData= new ArrayList<String>();
	private String google="";
	private boolean googleLogInState = false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.act_sign_up);
		IntialComponent();
		
		AccountManager accountManger = AccountManager.get(ActSignUp.this);
		Account[ ] accounts = accountManger.getAccountsByType("com.google");
		google = accounts[0].name.toString();
		if(!google.equals("")){
			Builder build = new AlertDialog.Builder(ActSignUp.this).setMessage("請問是否使用gmail帳號註冊").setNeutralButton("否", gmailNo_click).setNegativeButton("是", gmailYes_clcik);
			Dialog d = build.create();
			d.show();
		}
		
	}
	
	android.content.DialogInterface.OnClickListener gmailYes_clcik = new android.content.DialogInterface.OnClickListener(){
		@Override
		public void onClick(DialogInterface dialog, int which) {
			accountLayout.setVisibility(View.GONE);
			PwdLayout.setVisibility(View.GONE);
			PwdCLayout.setVisibility(View.GONE);
			EmailLayout.setVisibility(View.GONE);
			googleLogInState = true;
		}
	};
	
	android.content.DialogInterface.OnClickListener gmailNo_click = new android.content.DialogInterface.OnClickListener(){
		@Override
		public void onClick(DialogInterface dialog, int which) {
			googleLogInState = false;
		}
	};
	
	OnClickListener confirm_Click = new OnClickListener() {
		@Override
		public void onClick(View v) {
			signUpData.clear();
			if(googleLogInState){
				signUpData.add(txtName.getText().toString().trim());
				signUpData.add(txtPhone.getText().toString().trim());
				signUpData.add(txtAddress.getText().toString().trim());
				signUpData.add(google);//帳號是google帳號
				signUpData.add("");//使用google帳號，密碼就放空字串
				signUpData.add("");
				signUpData.add(google);//信箱也是google帳號
				if(signUpData.get(0).equals("")){
					MyToast.Show(ActSignUp.this, "姓名未填!");				
					return;
				}
				if( signUpData.get(1).equals("")){
					MyToast.Show(ActSignUp.this, "電話未填!");				
					return;
				}
				
			}else{
				signUpData.add(txtName.getText().toString().trim());
				signUpData.add(txtPhone.getText().toString().trim());
				signUpData.add(txtAddress.getText().toString().trim());
				signUpData.add(txtAccount.getText().toString().trim());
				signUpData.add(txtPwd.getText().toString().trim());
				signUpData.add(txtPwdAgain.getText().toString().trim());
				signUpData.add(txtEmail.getText().toString().trim());
				if( signUpData.get(0).equals("")){
					MyToast.Show(ActSignUp.this, "姓名未填!");				
					return;
				}
				if( signUpData.get(1).equals("")){
					MyToast.Show(ActSignUp.this, "電話未填!");				
					return;
				}
				if( signUpData.get(6).equals("")){
					MyToast.Show(ActSignUp.this, "Email未填!");				
					return;
				}				
				if( signUpData.get(3).equals("")){
					MyToast.Show(ActSignUp.this, "帳號未填!");				
					return;
				}

				if(signUpData.get(3).length() > 50){
					MyToast.Show(ActSignUp.this, "帳號必須小於50個字!");				
					return;				
				}
				if( signUpData.get(4).equals("") || signUpData.get(5).trim().equals("")){
					MyToast.Show(ActSignUp.this, "密碼未填!");				
					return;
				}
				if(!signUpData.get(4).equals(signUpData.get(5))){
					MyToast.Show(ActSignUp.this, "密碼不符，請再次確認!");
					return;
				}

			}
			final ProgressDialog myDialog = ProgressDialog.show(ActSignUp.this, "連線中", "請等待...");
			
			new Thread(){
				public void run(){
					try{
				///////////////////////////////////////////////////

							URL url = new URL(ActMain.webpage);
							String s = new DataBaseManagement().signUp(url, signUpData).toString().trim();
							ResultMsg = s;
							Message msg = new Message();
							msg.what = GUI_OK;
							ActSignUp.this.myMessageHandler.sendMessage(msg);

					///////////////////////////////////////////////////////
					}catch(Exception e){
						e.printStackTrace();
						MyToast.Show(ActSignUp.this, "註冊失敗");
					}finally{
						myDialog.dismiss();
					}
				}
			}.start();

		}
	};
	Handler myMessageHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			//super.handleMessage(msg);
			switch(msg.what){
			case GUI_OK:
					if(ResultMsg.equals("id has been used")){
						MyToast.Show(ActSignUp.this, "帳號已被使用，請填其他帳號");												
					}else if(ResultMsg.equals("true")){
						SharedPreferences sharedPre = getSharedPreferences("T1", 0);
						Editor editor = sharedPre.edit();
						if(!googleLogInState){//若googleLogInState是false，就是用新註冊的帳號，並記錄起來
							editor.putString("id", signUpData.get(3)).commit();
							editor.putString("pwd", signUpData.get(4)).commit();							
						}else{
							editor.putBoolean(ActMain.googleLogInStr, true).commit();
							editor.putString("id", google).commit();
							editor.putString("pwd", "").commit();							
						}
						editor.putString("name", signUpData.get(0)).commit();
						editor.putString("phone", signUpData.get(1)).commit();
						MyToast.Show(ActSignUp.this, "註冊完成，並登入帳號");
						finish();
					}else {
						MyToast.Show(ActSignUp.this, "註冊失敗");						
					}
				break;
			}
		}
	};
	OnClickListener layout_Click = new OnClickListener() {
		public void onClick(View v) {
			InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(layout1.getWindowToken(), 0);
		}
	};
	public void IntialComponent(){
		btnConfirm = (Button)findViewById(R.id.ASUbtnConfirm);
		btnConfirm.setOnClickListener(confirm_Click);
		
		txtName= (EditText)findViewById(R.id.ASUname);
		txtPhone= (EditText)findViewById(R.id.ASUphone);
		txtAddress= (EditText)findViewById(R.id.ASUaddress);
		txtAccount= (EditText)findViewById(R.id.ASUaccount);
		txtPwd= (EditText)findViewById(R.id.ASUpwd);
		txtPwdAgain= (EditText)findViewById(R.id.ASUpwdCompair);
		layout1 = (LinearLayout)findViewById(R.id.ASULayout1);
		layout1.setOnClickListener(layout_Click);
		txtEmail = (EditText)findViewById(R.id.ASUEmail);
		accountLayout = (LinearLayout)findViewById(R.id.ASUAccountLayout);
		PwdLayout = (LinearLayout)findViewById(R.id.ASUPwdLayout);
		PwdCLayout = (LinearLayout)findViewById(R.id.ASUPwdCLayout);
		EmailLayout = (LinearLayout)findViewById(R.id.ASUEmsilLayout);
	}
	LinearLayout layout1;
	Button btnConfirm;
	EditText txtName;
	EditText txtPhone;
	EditText txtAddress;
	EditText txtAccount;
	EditText txtPwd;
	EditText txtPwdAgain;
	EditText txtEmail;
	LinearLayout accountLayout;
	LinearLayout PwdLayout;
	LinearLayout PwdCLayout;
	LinearLayout EmailLayout;
	
}
