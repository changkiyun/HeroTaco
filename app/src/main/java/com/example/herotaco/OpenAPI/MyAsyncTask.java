package com.example.herotaco.OpenAPI;

import android.os.AsyncTask;
import android.util.Log;


import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

public class MyAsyncTask extends AsyncTask<String, String, String> {
    private InputStream is;
    private ArrayList<Item> list = null;
    private Item bus = null;


    @Override
    protected String doInBackground(String... strings) {
        String requestUrl = "http://openapi.seoul.go.kr:8088/53534c4647656b7a36324667476362/xml/LOCALDATA_072405/1/5/";

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
                    case XmlPullParser.END_DOCUMENT:
                        break;
                    case XmlPullParser.END_TAG:
                        if (parser.getName().equals("row") && bus != null) {
                            list.add(bus);
                        }
                        break;
                    case XmlPullParser.START_TAG:
                        if (parser.getName().equals("row")) {
                            bus = new Item();
                            b_row = true;
                        }
                        if (parser.getName().equals("RDNWHLADDR")) b_roadAddress = true;
                        if (parser.getName().equals("X")) b_lat = true;
                        if (parser.getName().equals("Y")) b_lng = true;
                        if (parser.getName().equals("SITETEL")) b_tel = true;
                        if (parser.getName().equals("BPLCNM")) b_name = true;
                        break;

                    case XmlPullParser.TEXT:
                        if (b_row) {
                            if (b_roadAddress) {
                                bus.setRoadAddress(parser.getText());
                                b_roadAddress = false;
                            } else if (b_lat) {
                                bus.setLat(parser.getText());
                                b_lat = false;
                            } else if (b_lng) {
                                bus.setLng(parser.getText());
                                b_lng = false;
                            } else if (b_tel) {
                                bus.setTel(parser.getText());
                                b_tel = false;
                            } else if (b_name) {
                                bus.setName(parser.getText());
                                b_name = false;
                            }
                        }
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
    }
}

