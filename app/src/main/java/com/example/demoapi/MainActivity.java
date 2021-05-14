package com.example.demoapi;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private TextView mText;
    private EditText mEdit;
    private Button mButton;
    private RequestQueue mQueue;
    public ArrayList<Model> mListView;
    public ViewAdapter mAdapter;
    public JSONObject jsonObject, jsonObjectHits, jsonObjectRound, jsonObjectSource;
    private ArrayList<String> mDiseases;
    private ArrayList<String> mConfoundingWord;
    private ArrayList<String> mRelatedWord;

    ListView mList;
    private StandardizedString mstandardized;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //anh xa
        mText = findViewById(R.id.textV);
        mEdit = findViewById(R.id.editT);
        mButton = findViewById(R.id.BttonS);
        mList = findViewById(R.id.ListV);

        mQueue = Volley.newRequestQueue(this);
        mstandardized = new StandardizedString();
        mDiseases = new ArrayList<>();
        mConfoundingWord = new ArrayList<>();
        mRelatedWord = new ArrayList<>();
        listOfConfoundingWords();
        listOfDiseases();
        listOfRelatedWords();

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str = mEdit.getText().toString();
                if (str.equals("") == false) {
                    String relatedWord = getRelatedWord(str, 1);
                    Log.d("HoangCV", "onClick: str: " + str);
                    Log.d("HoangCV", "onClick: relatedWord: " + relatedWord);
                    String diseasesName = dataProcessing(str);
                    Log.d("HoangCV", "onClick: diseasesName: " + diseasesName);
                    String data = "{\"size\":20,\"sort\":[{\"_score\": \"desc\"}],\"query\":{\"bool\":{\"must\":{\"multi_match\":{\"query\":\"" + diseasesName + " \",\"fields\":[\"header\"]}},\"should\":{\"bool\":{\"filter\":{\"multi_match\":{\"query\":\"" + relatedWord + " \",\"fields\":[\"header\"]}},\"should\":{\"multi_match\":{\"query\":\"" + relatedWord + " \",\"fields\":\"description\"}}}}}},\"_source\":[\"header\",\"description\",\"web_url\",\"post_url\",\"content\"]}";
                    jsonParse(data);
                }
                ;
            }
        });
    }

    private void jsonParse(String data) {
        //GET du lieu Json tu 1 link
        String url = "http://10.2.22.67:9090/craw_data/_search";
        final String savedata = data;
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    //mText.setText(response.toString());
                    jsonObject = new JSONObject(response);
                    jsonObjectHits = jsonObject.getJSONObject("hits");
                    JSONArray jsonArrayHits = jsonObjectHits.getJSONArray("hits");
                    mListView = new ArrayList<>();
                    for (int i = 0; i < jsonArrayHits.length(); i++) {
                        jsonObjectRound = jsonArrayHits.getJSONObject(i);
                        jsonObjectSource = jsonObjectRound.getJSONObject("_source");
                        String description = jsonObjectSource.getString("description");
                        String header = jsonObjectSource.getString("header");
                        mListView.add(new Model(description, header));

                    }
                    //ve len adapter
                    mAdapter = new ViewAdapter(mListView);
                    mList.setAdapter(mAdapter);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {


                //Log.v("VOLLEY", error.toString());
            }
        }) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    return savedata == null ? null : savedata.getBytes("utf-8");
                } catch (UnsupportedEncodingException uee) {
                    return null;
                }
            }

        };
        mQueue.add(stringRequest);
    }

    private void listOfDiseases() {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(getAssets().open("ListOfDiseases.txt")));
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                mDiseases.add(line);
            }
        } catch (IOException e) {
            System.out.println("Couldn't Read File Correctly");
        }
    }

    private void listOfConfoundingWords() {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(getAssets().open("ListOfConfoundingWords.txt")));
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                mConfoundingWord.add(line);
            }
        } catch (IOException e) {
            System.out.println("Couldn't Read File Correctly");
        }
    }

    private void listOfRelatedWords() {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(getAssets().open("ListOfRelatedWords")));
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                mRelatedWord.add(line);
            }
        } catch (IOException e) {
            System.out.println("Couldn't Read File Correctly");
        }
    }

    private String getRelatedWord(String data, int key) {
        String result = "";
        data = mstandardized.standardized(data);
        for (int i = 0; i < mRelatedWord.size(); i++) {
            String mRelatedWordName = mstandardized.standardized(mRelatedWord.get(i));
            if (data.indexOf(mRelatedWordName) >= 0) {
                int index = data.indexOf(mRelatedWordName);
                try {
                    data = data.substring(0, index) + data.substring(mRelatedWord.get(i).length() + index);
                } catch (Exception e) {
                }
                result = result + "( " + mRelatedWord.get(i) + " ) OR ";
            }
        }
        if (key == 1) {
            if (result.equals(""))
                return "( Nguyên nhân ) OR ( Triệu chứng )";
            else return result.substring(0, result.length() - 3);
        } else {
            return data;
        }
    }

    private String dataProcessing(String data) {
        data = getRelatedWord(data, 2);
        data = mstandardized.standardized(data);
        for (int i = 0; i < mConfoundingWord.size(); i++) {
            String mConfoundingWordName = mstandardized.standardized(mConfoundingWord.get(i));
            if (data.indexOf(mConfoundingWordName) >= 0) {
                int index = data.indexOf(mConfoundingWordName);
                data = data.substring(0, index) + data.substring(mConfoundingWord.get(i).length() + index);
            }
        }
        String result = "";
        for (int i = 0; i < mDiseases.size(); i++) {
            String mDiseaseName = mstandardized.standardized(mDiseases.get(i));
            if (data.indexOf(mDiseaseName) >= 0) {
                int index = data.indexOf(mDiseaseName);
                //result = result + data.substring(index, mDiseases.get(i).length() + index) + ",";
                //result = result + mDiseases.get(i) + ",";
                try {
                    data = data.substring(0, index) + data.substring(mDiseases.get(i).length() + index);
                } catch (Exception e) {
                }
                result = result + "( " + mDiseases.get(i) + " ) OR ";
            }
        }

        if (data.equals("") == false) {
            data = mstandardized.standardized(data);
            if ((data.equals("Bệnh") || (data.equals("Và")) || (data.equals("Benh")) || (data.equals("Va")) || (data.equals("Va Benh")) || (data.equals("Và Bệnh")))) {
                data = "";
            }
            while ((data.indexOf("Và ") >= 0) || (data.indexOf("Va ") >= 0) || ((data.indexOf("Và") >= 0)&&(data.indexOf("à")==data.length()-1))) {
                {
                    int index = (data.indexOf("Và") >= 0) ? data.indexOf("Và") : data.indexOf("Va");
                    data = data.substring(0, index) + data.substring(2 + index);
                }
            }
/*            if (data.equals("") == false)
                result = result + "( " + data + " ) OR ";*/
        }
        if (result.equals(""))
            return "(" + data + ")";
        else return result.substring(0, result.length() - 3);

    }


}
