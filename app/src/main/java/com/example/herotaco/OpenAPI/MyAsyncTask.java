package com.example.herotaco.OpenAPI;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.telecom.Call;
import android.util.Log;
import android.widget.Toast;


import com.example.herotaco.MainActivity;
import com.naver.maps.geometry.LatLng;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.security.auth.callback.Callback;

public class MyAsyncTask extends AsyncTask<String, String, String> {
    private InputStream is;
    private ArrayList<Item> list = null;

    public ArrayList<Item> getList() {
        return list;
    }

    public void setList(ArrayList<Item> list) {
        this.list = list;
    }

    private Item item = null;

    private Callback callback;
    private Context context;

    //MainActicity에서 MyAsyncTask를 동작하기위한 콜백메소드
    public MyAsyncTask(Callback callback, Context context) {
        this.callback = callback;
        this.context = context;
    }

    public interface Callback {
        void onPostExecuteCallback();
    }


    @Override
    protected String doInBackground(String... strings) {
        String requestUrl = "http://openapi.seoul.go.kr:8088/53534c4647656b7a36324667476362/xml/LOCALDATA_072405/1/100/";

        try {
            boolean b_row = false;
            boolean b_roadAddress = false;
            boolean b_lat = false;
            boolean b_lng = false;
            boolean b_tel = false;
            boolean b_name = false;

            URL url = new URL(requestUrl);
            is = url.openStream();
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new InputStreamReader(is, "UTF-8"));

            String tag;
            int eventType = parser.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        list = new ArrayList<Item>();
                        break;
                    case XmlPullParser.START_TAG:
                        if (parser.getName().equals("row")) {
                            item = new Item();
                            b_row = true;
                        }
                        if (parser.getName().equals("RDNWHLADDR")) b_roadAddress = true;
                        if (parser.getName().equals("SITETEL")) b_tel = true;
                        if (parser.getName().equals("BPLCNM")) b_name = true;
                        break;
                    case XmlPullParser.END_TAG:
                        if (parser.getName().equals("row") && item != null) {
                            list.add(item);
                        }
                        break;
                    case XmlPullParser.TEXT:
                        if (b_row) {
                            if (b_roadAddress) {
                                item.setRoadAddress(parser.getText());
                                //Todo: 파싱중에 문자열이 포함된 함수를 동작하여 파싱시간이 길어짐
                                item.setLatLng(getLatLng(parser.getText()));
                                b_roadAddress = false;
                            } /*else if (b_lat) {
                                item.setLat(parser.getText());
                                b_lat = false;
                            } else if (b_lng) {
                                item.setLng(parser.getText());
                                b_lng = false;
                            }*/ else if (b_tel) {
                                item.setTel(parser.getText());
                                b_tel = false;
                            } else if (b_name) {
                                item.setName(parser.getText());
                                b_name = false;
                            }
                        }
                        break;
                    case XmlPullParser.END_DOCUMENT:
                        break;
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(String string) {
        super.onPostExecute(string);

        if (list != null && !list.isEmpty()) {
            Log.d("MyAsyncTask", "Data loaded successfully: " + list.get(0).getName());
        } else {
            Log.d("MyAsyncTask", "Failed to load data");
        }
        if (callback != null) {
            callback.onPostExecuteCallback();
        }
    }

    private LatLng getLatLng(String address) {
        Geocoder geocoder = new Geocoder(this.context, Locale.KOREA);
        List<Address> list;

        try {
            list = geocoder.getFromLocationName(address, 10);

            if (list != null && list.size() > 0) {
                Address firstResult = list.get(0);
                return new LatLng(firstResult.getLatitude(), firstResult.getLongitude());
            } else {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}

