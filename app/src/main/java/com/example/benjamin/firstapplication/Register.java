package com.example.benjamin.firstapplication;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.apache.commons.validator.routines.EmailValidator;
import org.json.JSONException;
import org.json.JSONObject;


public class Register extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
    }

    public void onRegisterClick(View v) {
        final EmailValidator emailvalidator = EmailValidator.getInstance();
        if (v.getId() == R.id.Register_button_id) {
            EditText Email = (EditText) findViewById(R.id.Email_id);
            EditText Username = (EditText) findViewById(R.id.Username_id);
            EditText Password = (EditText) findViewById(R.id.Password_id);

            String EmailStr = Email.getText().toString();
            String UsernameStr = Username.getText().toString();
            String PasswordStr = Password.getText().toString();
            final boolean valid = emailvalidator.isValid(EmailStr);

            Response.Listener<String> responseListener = new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject jsonResponse = new JSONObject(response.substring(response.indexOf("{"), response.lastIndexOf("}") + 1));;
                        boolean user = jsonResponse.getBoolean("user");
                        boolean em = jsonResponse.getBoolean("em");
                        if (user && em) {
                            Intent intent = new Intent(Register.this, Login.class);
                            Register.this.startActivity(intent);
                        } else if (em && !user){
                            AlertDialog.Builder builder = new AlertDialog.Builder(Register.this);
                            builder.setMessage("Invalid Username").setNegativeButton("Retry", null).create().show();
                        } else if (user && !em) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(Register.this);
                            builder.setMessage("Invalid Email").setNegativeButton("Retry", null).create().show();
                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(Register.this);
                            builder.setMessage("Register Failed").setNegativeButton("Retry", null).create().show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            };
            if(!valid){
                AlertDialog.Builder builder = new AlertDialog.Builder(Register.this);
                builder.setMessage("Invalid Email").setNegativeButton("Retry", null).create().show();
            }else {

                RegisterRequest registerRequest = new RegisterRequest(EmailStr, UsernameStr, PasswordStr, responseListener);
                RequestQueue queue = Volley.newRequestQueue(Register.this);
                queue.add(registerRequest);
            }
        }
    }

        }
