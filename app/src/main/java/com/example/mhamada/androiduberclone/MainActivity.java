package com.example.mhamada.androiduberclone;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.example.mhamada.androiduberclone.Model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.w3c.dom.Text;

import dmax.dialog.SpotsDialog;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity {
    Button SignIn,Register;
    FirebaseAuth auth;
    FirebaseDatabase db;
    DatabaseReference users;
    RelativeLayout rootLayout;
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder().setDefaultFontPath("fonts/Arkhip_font.ttf")
        .setFontAttrId(R.attr.fontPath).build());
        setContentView(R.layout.activity_main);
        //init firebase
        auth=FirebaseAuth.getInstance();
        db=FirebaseDatabase.getInstance();
        users=db.getReference("Users");
        //init view
        SignIn=findViewById(R.id.btn_SignIn);
        Register=findViewById(R.id.btn_Register);
        rootLayout=findViewById(R.id.rootLayout);
        //Event
        Register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRegisterDialog();
            }
        });
        SignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLoginDialog();
            }
        });
    }
    private void showLoginDialog(){
        final AlertDialog.Builder dialog=new AlertDialog.Builder(this);
        dialog.setTitle("SIGN IN");
        dialog.setMessage("please use email to sign in");
        LayoutInflater inflater=LayoutInflater.from(this);
        View Login_layout=inflater.inflate(R.layout.layout_login,null);
        final MaterialEditText editEmail =Login_layout.findViewById(R.id.editEmail);
        final MaterialEditText editPassword=Login_layout.findViewById(R.id.editPassword);

        dialog.setView(Login_layout);
        dialog.setPositiveButton("SIGN IN", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        //set disable button Sign in if is proceesing
                        SignIn.setEnabled(false);
                        //check validation
                        if (TextUtils.isEmpty(editEmail.getText().toString())) {
                            Snackbar.make(rootLayout, "please enter your email", Snackbar.LENGTH_LONG).show();
                            return;
                        }
                        if (TextUtils.isEmpty(editPassword.getText().toString())) {
                            Snackbar.make(rootLayout, "please enter your password", Snackbar.LENGTH_LONG).show();
                            return;
                        }
                        if (editPassword.getText().toString().length() < 6) {
                            Snackbar.make(rootLayout, "Password too short ", Snackbar.LENGTH_LONG).show();
                            return;
                        }
                        final AlertDialog waitingdialog=new SpotsDialog(MainActivity.this);
                        waitingdialog.show();
                        auth.signInWithEmailAndPassword(editEmail.getText().toString(),editPassword.getText().toString())
                             .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                 @Override
                                 public void onSuccess(AuthResult authResult) {
                                     waitingdialog.dismiss();
                                   startActivity(new Intent(MainActivity.this,Welcome.class));
                                   finish();
                                 }
                             }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                 waitingdialog.dismiss();
                                 Snackbar.make(rootLayout,"failed"+e.getMessage(),Snackbar.LENGTH_LONG).show();
                                 //active button
                                 SignIn.setEnabled(true);
                            }
                        });

                    }
                });
        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        dialog.show();

    }
    private void  showRegisterDialog(){
        AlertDialog.Builder dialog=new AlertDialog.Builder(this);
        dialog.setTitle("REGISTER");
        dialog.setMessage("please use email to register");
        LayoutInflater inflater=LayoutInflater.from(this);
        View Layout_Register=inflater.inflate(R.layout.layout_register,null);
        final MaterialEditText editEmail =Layout_Register.findViewById(R.id.editEmail);
        final MaterialEditText editPassword=Layout_Register.findViewById(R.id.editPassword);
        final MaterialEditText editName=Layout_Register.findViewById(R.id.editName);
        final MaterialEditText editPhone=Layout_Register.findViewById(R.id.editPhone);
        dialog.setView(Layout_Register);
        dialog.setPositiveButton("REGISTER", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                //check validation
                if(TextUtils.isEmpty(editEmail.getText().toString())){
                    Snackbar.make(rootLayout,"please enter your email",Snackbar.LENGTH_LONG).show();
                    return;
                }
                if(editPassword.getText().toString().length()< 6){
                    Snackbar.make(rootLayout,"Password too short ",Snackbar.LENGTH_LONG).show();
                    return;
                }
                if(TextUtils.isEmpty(editName.getText().toString())){
                    Snackbar.make(rootLayout,"please enter your Name",Snackbar.LENGTH_LONG).show();
                    return;
                }
                if(TextUtils.isEmpty(editPhone.getText().toString())){
                    Snackbar.make(rootLayout,"please enter your Phone",Snackbar.LENGTH_LONG).show();
                    return;
                }
                //Register new user
                auth.createUserWithEmailAndPassword(editEmail.getText().toString(),editPassword.getText().toString())
                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            //save user to db
                            User user=new User();
                            user.setEmail(editEmail.getText().toString());
                            user.setPassword(editPassword.getText().toString());
                            user.setName(editName.getText().toString());
                            user.setPhone(editPhone.getText().toString());
                            users.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(user)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                      Snackbar.make(rootLayout,"Register Success",Snackbar.LENGTH_LONG).show();
                                        }
                                    })
                                     .addOnFailureListener(new OnFailureListener() {
                                         @Override
                                         public void onFailure(@NonNull Exception e) {
                                             Snackbar.make(rootLayout,"failed"+e.getMessage(),Snackbar.LENGTH_LONG).show();
                                         }
                                     });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(rootLayout,"failed"+e.getMessage(),Snackbar.LENGTH_LONG).show();
                    }
                }) ;
            }
        });
        dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        dialog.show();
    }
}
