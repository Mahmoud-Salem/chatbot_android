package com.example.mohamedalshaer.myfriendassisstant;


import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.util.Log;

import org.apache.http.entity.StringEntity;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private ListView listView;
    private View btnSend;
    private  View calendarbtn;
    private EditText editText;
    boolean myMessage = true;
    private List<ChatBubble> ChatBubbles;
    private ArrayAdapter<ChatBubble> adapter;
    private String uuid = "";
    private String url = "https://personal-assistant-10.herokuapp.com/" ;
    private String loggedin = "";

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        ChatBubbles = new ArrayList<>();

        listView = (ListView) findViewById(R.id.list_msg);
        btnSend = findViewById(R.id.btn_chat_send);
        calendarbtn = findViewById(R.id.quick_action_button);

        editText = (EditText) findViewById(R.id.msg_type);

        //set ListView adapter first
        adapter = new MessageAdapter(this, R.layout.left_chat_bubble, ChatBubbles);
        listView.setAdapter(adapter);

        /// welcome message
        String [] params = {"welcome","GET"};
        new FetchReply().execute(params);


        //event for button SEND
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editText.getText().toString().trim().equals("")) {
                    Toast.makeText(MainActivity.this, "Please input some text...", Toast.LENGTH_SHORT).show();
                } else {
                    //add message to list
                    ChatBubble ChatBubble = new ChatBubble(editText.getText().toString(), true);
                    ChatBubbles.add(ChatBubble);
                    adapter.notifyDataSetChanged();
                    String m = editText.getText().toString();
                    editText.setText("");

                    String [] params = {"chat","POST",m};
                    new FetchReply().execute(params);
                }
            }
        });

        calendarbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (loggedin.equals("")) {
                    Toast.makeText(MainActivity.this, "You are not logged in", Toast.LENGTH_SHORT).show();
                } else {
                    String m = "show calendar . loggedin_id: "+loggedin+".";
                    String [] params = {"chat","POST",m};
                    new FetchReply().execute(params);
                }
            }
        });
    }

    public class FetchReply extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String...params) {

            String message = "Check Internet Connection";


            try {
                if(params[0].equals("welcome"))
                {

                    HttpClient client = new DefaultHttpClient();
                    HttpGet request = new HttpGet(url+params[0]);

                    // add request header
                    request.addHeader("Content-Type", "application/json");

                    HttpResponse response = client.execute(request);

                    BufferedReader rd = new BufferedReader(
                            new InputStreamReader(response.getEntity().getContent()));

                     StringBuffer result = new StringBuffer();
                    String line = "";
                    while ((line = rd.readLine()) != null) {
                        result.append(line);
                    }

                    JSONObject explrObject = new JSONObject(result.toString());
                    uuid = explrObject.getString("uuid");
                    message = explrObject.getString("message");



                }
                else
                {

                        HttpClient client = new DefaultHttpClient();
                        HttpPost post = new HttpPost(url+params[0]);
                        // add header
                    post.addHeader("Content-Type", "application/json");
                    post.addHeader("authorization", uuid);

                    JSONObject output = new JSONObject();
                        output.put("message",params[2]);
                    StringEntity t = new StringEntity(output.toString());
                        post.setEntity(t);

                        HttpResponse response = client.execute(post);
                    BufferedReader rd = new BufferedReader(
                            new InputStreamReader(response.getEntity().getContent()));

                    StringBuffer result = new StringBuffer();
                    String line = "";
                    while ((line = rd.readLine()) != null) {
                        result.append(line);
                    }

                   JSONObject explrObject = new JSONObject(result.toString());
                    if(explrObject.has("uuid") )
                    {
                        loggedin = explrObject.getString("uuid");
                    }
                    message = explrObject.getString("message");
                }



        }
        catch(Exception e){
                Log.e("error",e.getMessage());
            System.exit(0);
        }
        return message ;
        }

        @Override
        protected void onPostExecute(String message) {
            if (message != null) {
                ChatBubble ChatBubble = new ChatBubble(message,false);
                ChatBubbles.add(ChatBubble);
                adapter.notifyDataSetChanged();

            }
        }
    }
}
