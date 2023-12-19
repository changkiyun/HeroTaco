package com.example.herotaco.Firebase;

import com.naver.maps.geometry.LatLng;

public class FirebaseLatLng {
    private double latitude;
    private double longitude;

    public FirebaseLatLng() {
        // 기본 생성자 추가
    }

    public FirebaseLatLng(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public LatLng toLatLng() {
        return new LatLng(latitude, longitude);
    }
}
