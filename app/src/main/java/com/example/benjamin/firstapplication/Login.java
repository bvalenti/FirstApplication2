package com.example.benjamin.firstapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import android.os.Process;

public class Login extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

    }

    public void onButtonClick(View v){
        if(v.getId() == R.id.Login_button){
            EditText Username = (EditText) findViewById(R.id.TFUsername_id);
            EditText Password = (EditText) findViewById(R.id.TFPassword_id);
            String UsernameStr = Username.getText().toString();
            String PasswordStr = Password.getText().toString();

            Response.Listener<String> responseListener = new Response.Listener<String>(){
                @Override
                public void onResponse(String response){
                    try {
                        JSONObject jsonResponse = new JSONObject(response.substring(response.indexOf("{"), response.lastIndexOf("}") + 1));;
                       /* URL url = new URL("http://www.android.com/");
                        HttpURLConnection urlconnection = (HttpURLConnection) url.openConnection();
                        try {
                            urlconnection.setDoOutput(true);
                            urlconnection.setChunkedStreamingMode(0);
                            OutputStream out = new BufferedOutputStream(urlconnection.getOutputStream());
                            writeStream(out);
                        } finally {
                            urlconnection.disconnect();
                        }*/

                        boolean success = jsonResponse.getBoolean("success");

                        if(success){
                            final String username = jsonResponse.getString("username");
                            final Thread retrieveFile = new Thread(new Runnable () {

                                @Override
                                public void run() {
                                    android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                                    try {
                                        //Test code
//                    URL url = new URL("http://www.textfiles.com/100/914bbs.txt");
//                    InputStream inputStream = url.openStream();
//                    inputStream.close();

                                        //Code for retrieving files from server
                                        URL url = new URL("http://129.3.212.153:8080/MTABusServlet/MTABusServlet?RequestType=login&userID=" + username);
                                        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                                        urlConnection.setRequestMethod("POST");

                                        Utility.getFile(urlConnection, "", "ignore.txt", false);
                                        urlConnection.disconnect();

                                    } catch (MalformedURLException e) {
                                        e.printStackTrace();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            retrieveFile.start();

                            Intent intent = new Intent(Login.this, MapsActivity.class);
                            /*Intent admin = new Intent(Login.this, MapsActivity2.class);
                            if(username.startsWith("admin-")){
                                admin.putExtra("username", username);
                                Login.this.startActivity(admin);
                            }else {*/
                                //intent.putExtra("username", username);
                                Login.this.startActivity(intent);
                           // }
                        }else{
                            AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
                            builder.setMessage("Login Failed").setNegativeButton("Retry",null).create().show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } /*catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }*/
                }
            };
            LoginRequest loginRequest = new LoginRequest(UsernameStr, PasswordStr, responseListener);
            RequestQueue queue = Volley.newRequestQueue(Login.this);
            queue.add(loginRequest);

        }else if(v.getId()== R.id.Register_here_button){
            Intent RegisterIntent= new Intent(Login.this, Register.class);
            Login.this.startActivity(RegisterIntent);
        }
    }
}
