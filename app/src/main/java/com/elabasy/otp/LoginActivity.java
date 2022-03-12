package com.elabasy.otp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import com.elabasy.otp.databinding.ActivityLoginBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private FirebaseAuth firebaseAuth;
    private PhoneAuthProvider.ForceResendingToken forceResendingToken;              //if code send failed , will send to resend OTP
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private String mVerificationId;
    private ProgressDialog pd;    //dialog


    private CustomToast customToast;
    private String Phone;
    private String phoneNumber;
    private boolean connected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        customToast = new CustomToast(this);
        binding.phoneNumberConstraintLayout.setVisibility(View.VISIBLE);
        binding.verifyCodeConstraintLayout.setVisibility(View.GONE);
        binding.cancelBtn.setOnClickListener(v -> {
            finish();
        });

        //for internet connection
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED
                || connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network
            firebaseAuth = FirebaseAuth.getInstance();


            //init progress dialog
            pd = new ProgressDialog(this);
            pd.setTitle(R.string.please_wait);
            pd.setCanceledOnTouchOutside(false);

            mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                @Override
                public void onVerificationCompleted(PhoneAuthCredential credential) {
                    // This callback will be invoked in two situations:
                    // 1 - Instant verification. In some cases the phone number can be instantly
                    //     verified without needing to send or enter a verification code.
                    // 2 - Auto-retrieval. On some devices Google Play services can automatically
                    //     detect the incoming verification SMS and perform verification without user action.
                    signInWithPhoneAuthCredential(credential);
                }

                @Override
                public void onVerificationFailed(FirebaseException e) {
                    // This callback is invoked in an invalid request for verification is made,
                    // for instance if the the phone number format is not valid.
                    pd.dismiss();
                    customToast.toastError((String) getText(R.string.not_valid_phone));
//                if (e instanceof FirebaseAuthInvalidCredentialsException) {
//                    // Invalid request
//                    // ...
//                } else if (e instanceof FirebaseTooManyRequestsException) {
//                    // The SMS quota for the project has been exceeded
//                    // ...
//                }
//
//                // Show a message and update the UI
//                // ...
                }

                @SuppressLint("SetTextI18n")
                @Override
                public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                    // The SMS verification code has been sent to the provided phone number, we
                    // now need to ask the user to enter the code and then construct a credential
                    // by combining the code with a verification ID.

                    // Save verification ID and resending token so we can use them later
                    mVerificationId = verificationId;
                    forceResendingToken = token;

                    pd.dismiss();

                    //hide Phone Layout ,Show Code Layout
                    binding.phoneNumberConstraintLayout.setVisibility(View.GONE);
                    binding.verifyCodeConstraintLayout.setVisibility(View.VISIBLE);
                    binding.verifyCodeSubTv.setText(getText(R.string.We_text_on) + " " + Phone);

                    binding.pinView.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                            String code = binding.pinView.getText().toString().trim();
                            if (code.length() >= 6) {
                                verifyPhoneNumberVerification(mVerificationId, code);
                            }
                        }

                        @Override
                        public void afterTextChanged(Editable s) {

                        }
                    });

                }
            };

            binding.signInBtn.setOnClickListener(v -> {
                Phone = binding.phoneNumber.getEditText().getText().toString().trim();
                phoneNumber = "+2" + Phone;
                if (phoneNumber.equals(null) || phoneNumber.isEmpty() || phoneNumber.equals("") || phoneNumber.equals("+2") || phoneNumber.length() < 13) {
                    if (phoneNumber.length() < 13 && phoneNumber.length() > 2) {
                        customToast.toastError((String) getText(R.string.phone_error));
                    } else {
                        customToast.toastError((String) getText(R.string.phone_empty));
                    }
                } else {
                    startPhoneNumberVerification(phoneNumber);
                }
            });

            binding.resendCodeBtn.setOnClickListener(v -> {
                resendPhoneNumberVerification(phoneNumber, forceResendingToken);
            });

            binding.continueBtn.setOnClickListener(v -> {
                String code = binding.pinView.getText().toString().trim();

                if (code.equals(null) || code.isEmpty()) {
                    customToast.toastError((String) getText(R.string.Please_Enter_Your_Code));
                } else {
                    verifyPhoneNumberVerification(mVerificationId, code);
                }
            });

            connected = true;
        } else {
            customToast.toastError((String) getText(R.string.internet_connected));
            connected = false;
        }
    }

    private void startPhoneNumberVerification(String phone) {
        pd.setMessage(this.getText(R.string.verify_Number));
        pd.show();

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(phone)       // Phone number to verify
                .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                .setActivity(this)                 // Activity (for callback binding)
                .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void resendPhoneNumberVerification(String phone, PhoneAuthProvider.ForceResendingToken token) {
        pd.setMessage(this.getText(R.string.resend_Code));
        pd.show();
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(phone)       // Phone number to verify
                .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                .setActivity(this)                 // Activity (for callback binding)
                .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                .setForceResendingToken(token)
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }


    private void verifyPhoneNumberVerification(String mVerificationId, String code) {
        pd.setMessage(this.getText(R.string.verify_Code));
        pd.show();
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
        signInWithPhoneAuthCredential(credential);
    }


    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        pd.show();
        firebaseAuth.signInWithCredential(credential).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                pd.dismiss();
                String phone = firebaseAuth.getCurrentUser().getPhoneNumber();
                customToast.toastSuccess((String) getText(R.string.You_are_logged_in_as) + " " + phone);
                //Start Profile
                startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                finish();
            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                customToast.toastError((String) getText(R.string.invalid_code));
            }
        });
    }

}