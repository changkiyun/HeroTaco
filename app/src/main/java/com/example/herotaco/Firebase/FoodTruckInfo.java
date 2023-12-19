package com.example.herotaco.Firebase;

import com.naver.maps.geometry.LatLng;

import java.io.Serializable;
import java.util.List;

public class FoodTruckInfo {
    private String name;
    private String tel;
    private String foodType;
    private String address;
    private double latitude;
    private double longitude;
    private List<Boolean> dayCheckBooleanList;

    public List<Boolean> getDayCheckBooleanList() {
        return dayCheckBooleanList;
    }

    public void setDayCheckBooleanList(List<Boolean> dayCheckBooleanList) {
        this.dayCheckBooleanList = dayCheckBooleanList;
    }



    private int startHour, startMinute, endHour, endMinute;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getFoodType() {
        return foodType;
    }

    public void setFoodType(String foodType) {
        this.foodType = foodType;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }



    public int getStartHour() {
        return startHour;
    }

    public void setStartHour(int startHour) {
        this.startHour = startHour;
    }

    public int getStartMinute() {
        return startMinute;
    }

    public void setStartMinute(int startMinute) {
        this.startMinute = startMinute;
    }

    public int getEndHour() {
        return endHour;
    }

    public void setEndHour(int endHour) {
        this.endHour = endHour;
    }

    public int getEndMinute() {
        return endMinute;
    }

    public void setEndMinute(int endMinute) {
        this.endMinute = endMinute;
    }

    public FoodTruckInfo() {
    }


    public FoodTruckInfo(String name, String tel, String foodType, String address, double latitude, double longitude, List<Boolean> dayCheckBooleanList, int startHour, int startMinute, int endHour, int endMinute) {
        this.name = name;
        this.tel = tel;
        this.foodType = foodType;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.dayCheckBooleanList = dayCheckBooleanList;
        this.startHour = startHour;
        this.startMinute = startMinute;
        this.endHour = endHour;
        this.endMinute = endMinute;
    }
    public FoodTruckInfo(String name, String tel, String foodType, String address, double latitude, double longitude) {
        this.name = name;
        this.tel = tel;
        this.foodType = foodType;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
