package com.example.cbanek.hellofire;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RemoteControlActivity extends AppCompatActivity {

    private RequestQueue queue_;
    private String musicUrl_ = "http://192.168.1.20:5000/music";
    private String volumeUrl_ = "http://192.168.1.20:5000/volume";
    private List<String> songs_ = new ArrayList<>(20);
    private ListView musicList_;
    private SeekBar volumeBar_;
    private ArrayAdapter<String> adapter_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hello_fire);
        queue_ = Volley.newRequestQueue(this);

        adapter_ = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, songs_);

        volumeBar_ = (SeekBar) findViewById(R.id.volumeBar);
        volumeBar_.setMax(100);
        volumeBar_.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                setVolume(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        musicList_ = (ListView) findViewById(R.id.musicList);
        musicList_.setAdapter(adapter_);

        // ListView Item Click Listener
        musicList_.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                playMusic((String) musicList_.getItemAtPosition(position));
            }

        });

        populate();
    }

    private void populate() {
        JsonObjectRequest songsRequest = new JsonObjectRequest
                (Request.Method.GET, musicUrl_, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray files = response.getJSONArray("files");

                            for(int i = 0; i < files.length(); i++) {
                                songs_.add(files.getString(i));
                            }
                        } catch (JSONException e) {
                            songs_.clear();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        songs_.clear();
                    }
                });

        JsonObjectRequest volumeRequest = new JsonObjectRequest
                (Request.Method.GET, volumeUrl_, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            int volume = response.getInt("level");
                            volumeBar_.setProgress(volume);
                        } catch (JSONException e) {
                            volumeBar_.setProgress(0);
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        volumeBar_.setProgress(0);
                    }
                });

        queue_.add(songsRequest);
        queue_.add(volumeRequest);
    }

    private void playMusic(final String song) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("song", song);

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (musicUrl_, new JSONObject(params), new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Toast.makeText(getApplicationContext(),
                                "Now playing " + song, Toast.LENGTH_LONG)
                                .show();
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(),
                                "Error playing " + song, Toast.LENGTH_LONG)
                                .show();
                    }
                });

        queue_.add(jsObjRequest);
    }

    private void setVolume(int volume) {
        HashMap<String, Integer> params = new HashMap<String, Integer>();
        params.put("level", volume);

        JsonObjectRequest volumeRequest = new JsonObjectRequest
                (volumeUrl_, new JSONObject(params), new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        volumeBar_.setProgress(0);
                    }
                });

        queue_.add(volumeRequest);
    }
}
