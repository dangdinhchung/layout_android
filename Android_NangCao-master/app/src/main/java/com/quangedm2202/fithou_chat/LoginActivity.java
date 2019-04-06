package com.quangedm2202.fithou_chat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInApi;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.Arrays;
import java.util.List;

public class LoginActivity<GoogleSignInClient> extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener,FirebaseAuth.AuthStateListener {
    private ImageButton btRegister;
    private TextInputLayout mEmail;
    private TextInputLayout mPassword;
    private Button mLogin_btn;
    private TextView tvForgot;
    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase;
    private Toolbar mToolbar;
    private ProgressDialog mLoginProgress;
    //login google
    Button btnLoginGoogle;
    GoogleApiClient apiClient;
    public static int REQUESTCODE_DANGNHAP_GOOGLE = 99;
    //login facebook
    Button btnLoginFacebook;
    LoginManager loginManager;
    List<String> permissionFacebook = Arrays.asList("email","public_profile");
    CallbackManager mCallbackFacebook;
    //CallbackManager callbackManager;
    public static int KIEMTRA_PROVIDER_DANGNHAP = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_login);

        mToolbar = (Toolbar) findViewById(R.id.register_toolbar);
        mAuth = FirebaseAuth.getInstance(); // chung thuc firebase
        mCallbackFacebook = CallbackManager.Factory.create();
        loginManager = LoginManager.getInstance(); //facebook
        mAuth.signOut();
        //dang xuat facebook
        //LoginManager.getInstance().logOut();
        mEmail = (TextInputLayout) findViewById(R.id.login_email);
        mPassword = (TextInputLayout) findViewById(R.id.login_pass);
        mLogin_btn = (Button) findViewById(R.id.login_btn);
        //login facebook
        btnLoginFacebook = findViewById(R.id.btnLoginFacebook);

        //login google
        btnLoginGoogle = (Button) findViewById(R.id.btnLoginGoogle);
        btnLoginGoogle.setOnClickListener(this);
        btnLoginFacebook.setOnClickListener(this);
        TaoClientDangNhapGoogle();

        //su kien click icon + thi chuyen sang trang dang ky
        btRegister = findViewById(R.id.btRegister);
        tvForgot = findViewById(R.id.tvForgot);
        btRegister.setOnClickListener(this);
        mLoginProgress = new ProgressDialog(this);
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");


        //xử lý việt lấy lại mật khẩu
        tvForgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this,ForgotPassword.class));
            }
        });
    }


    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>(){

            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    mLoginProgress.dismiss();
                    String current_user_id = mAuth.getCurrentUser().getUid();

                    //Luu token ID vao de hien nofitication
                    String deviceToken  = FirebaseInstanceId.getInstance().getToken();

                    mUserDatabase.child(current_user_id).child("device_token").setValue(deviceToken).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Intent main_intent = new Intent(LoginActivity.this, MainActivity.class);
                            //an back thi khong quay ve login
                            main_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(main_intent);
                            finish();
                        }
                    });
                }
                else{
                    mLoginProgress.hide();
                    Toast.makeText(LoginActivity.this, "Can't Login account.Plz check a form and try again!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        if (view==btRegister){
            Intent intent = new Intent(LoginActivity.this,RegisterActivity.class);
            startActivity(intent);
        }

        //dang nhap google : click vao button dang nhap google thi kich hoat su kien
        int id = view.getId();
        switch (id){
            case R.id.btnLoginGoogle:
                DangNhapGoogle(apiClient);//sau do chay ham nay
                break;
            case R.id.btnLoginFacebook:
                DangNhapFacebook();
                break;
        }
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    //login google
    //Sự kiện kiểm tra xem người dùng đã nhập thành công hay đăng xuất
    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            Intent iTrangChu = new Intent(this, MainActivity.class);
            startActivity(iTrangChu);
        } else {
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(this);
    }



    //Khởi tạo client cho đăng nhập google
    private void TaoClientDangNhapGoogle(){
        //khoi tao 1 cai option muon lay cac thong so nhu email
        //lay tat ca thong tin ma minh muon lay
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                //chung thuc googleProvider bat ta truyen vao idToken
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestProfile()
                .build();
        //khoi tao ra de ket noi de dang nhap bang google
         apiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this,this) //mo man hinh form dang nhao len
                .addApi(Auth.GOOGLE_SIGN_IN_API,googleSignInOptions)
                .build();
    }
    private void DangNhapGoogle(GoogleApiClient apiClient){
        KIEMTRA_PROVIDER_DANGNHAP = 1;
        Intent iDNGoogle = Auth.GoogleSignInApi.getSignInIntent(apiClient); // mo intent dang nhap bang google len
        startActivityForResult(iDNGoogle,REQUESTCODE_DANGNHAP_GOOGLE);
    }

    //Lấy stokenID đã đăng nhập bằng google để đăng nhập trên Firebase
    private void ChungThucDangNhapFireBase(String tokenID){
        if(KIEMTRA_PROVIDER_DANGNHAP == 1){
            //goi phuong thuc dang nhap voi GoogleAuthProvider
            AuthCredential authCredential = GoogleAuthProvider.getCredential(tokenID,null);
            mAuth.signInWithCredential(authCredential);//neu dang nhap thanh cong thi kich hoat onTextCHange
        }else if(KIEMTRA_PROVIDER_DANGNHAP == 2){
            AuthCredential authCredential = FacebookAuthProvider.getCredential(tokenID);
            mAuth.signInWithCredential(authCredential);
        }

    }

    //sau DangNhapGoole -> chay ham nay
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUESTCODE_DANGNHAP_GOOGLE){ //kiem tra xem dang nhap = google khong
            if(resultCode == RESULT_OK){
                GoogleSignInResult signInResult = Auth.GoogleSignInApi.getSignInResultFromIntent(data); //lay ket qua nguoi dung dang nhap gg
                GoogleSignInAccount account = signInResult.getSignInAccount(); // sau do lay ra account nguoi dung dang nhap
                String tokenID = account.getIdToken();
                ChungThucDangNhapFireBase(tokenID);
            }
        }else{
            mCallbackFacebook.onActivityResult(requestCode,resultCode,data);
        }
    }


    //dang nhap facebook
    private void DangNhapFacebook(){
        loginManager.logInWithReadPermissions(this,permissionFacebook);
        loginManager.registerCallback(mCallbackFacebook, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                KIEMTRA_PROVIDER_DANGNHAP = 2;
                String tokenID = loginResult.getAccessToken().getToken();
                ChungThucDangNhapFireBase(tokenID);
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });
    }
}
