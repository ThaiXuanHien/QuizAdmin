package com.example.quizadmin.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.quizadmin.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private EditText edtEmail, edtPassword;
    private Button btnLogin;
    private FirebaseAuth firebaseAuth;
    private Dialog loadingDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AnhXa();

        loadingDialog = new Dialog(MainActivity.this);
        loadingDialog.setContentView(R.layout.loading_progressbar);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawableResource(R.drawable.progress_background);
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);


        firebaseAuth = firebaseAuth.getInstance();
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edtEmail.getText().toString().trim().isEmpty()) {
                    edtEmail.setError("Không được để trống Email");
                    return;
                } else {
                    edtEmail.setError(null);
                }
                if (edtPassword.getText().toString().trim().isEmpty()) {
                    edtPassword.setError("Không được để trống Password");
                    return;
                } else {
                    edtPassword.setError(null);
                }
                firebaseLogin();
            }
        });
        if(firebaseAuth.getCurrentUser()!=null){
            startActivity(new Intent(MainActivity.this,CategoryActivity.class));
            finish();
        }
    }

    private void firebaseLogin() {
        loadingDialog.show();
        firebaseAuth.signInWithEmailAndPassword(edtEmail.getText().toString(), edtPassword.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            startActivity(new Intent(MainActivity.this,CategoryActivity.class));
                            finish();

                        } else {
                            // If sign in fails, display a message to the user.


                        }
                        loadingDialog.dismiss();

                    }
                });
    }


    private void AnhXa() {
        edtEmail = (EditText) findViewById(R.id.editTextEmail);
        edtPassword = (EditText) findViewById(R.id.editTextPassword);
        btnLogin = (Button) findViewById(R.id.buttonLogin);
    }
}
