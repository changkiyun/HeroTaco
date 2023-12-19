package com.example.herotaco.OpenAPI;

import android.content.Context;

import com.naver.maps.geometry.LatLng;

public class Item {
    //RDNWHLADDR
    String roadAddress;
    //X
    String lat;
    //Y
    String lng;
    //SITETEL
    String tel;
    //BPLCNM
    String name;

    LatLng latLng;



    public String getRoadAddress() {
        return roadAddress;
    }

    public void setRoadAddress(String roadAddress) {
        this.roadAddress = roadAddress;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }
}
