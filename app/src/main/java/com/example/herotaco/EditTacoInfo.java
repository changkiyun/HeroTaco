package com.example.herotaco;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;

import com.example.herotaco.Firebase.FoodTruckInfo;
import com.example.herotaco.OpenAPI.MyAsyncTask;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.gun0912.tedpermission.rx3.TedPermission;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.OverlayImage;
import com.naver.maps.map.util.FusedLocationSource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class EditTacoInfo extends AppCompatActivity implements OnMapReadyCallback {
    private FusedLocationProviderClient fusedLocationClient;
    private FusedLocationSource locationSource;
    private NaverMap reportTacoNaverMap;
    private TextView tvLocation;
    private AppCompatButton btnConfirm;
    private MyAsyncTask myAsyncTask;
    private EditText edtReportName, edtReportTel;

    RadioButton cbReportFish, cbReportTaco, cbReportskewke, cbReportchiken;
    RadioGroup rgReportMenu;
    CheckBox cbMon, cbTue, cbWed, cbThu, cbFri, cbSat, cbSun;
    CheckBox[] checkBoxes;
    //영업시간 타임피커
    TimePicker tpReportStart, tpReportEnd;
    //영업시간 저장변수
    int startHour, endHour, startMinute, endMinute;
    //선택된 라디오버튼에서 메뉴 저장
    String selectedMenu;

    //체크된 체크박스를 초기화하고 파이어베이스에 저장하기 위한 변수
    Boolean[] dayCheckBoolean;

    //Firebase List를 가져오기위한 참조객체
    private FirebaseDatabase reportDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference reportReference = reportDatabase.getReference();
    //Firebase 매장 정보를 가져오기위한 참조객체
    private FirebaseDatabase editDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference editReference = editDatabase.getReference("foodTruck");
    private DatabaseReference truckInfoDatabase;
    //바텀시트다이얼로스에서 받아온 식당 이름(DB 참조를 위함)
    String foodTruckname;
    FoodTruckInfo foodTruckInfo;


    //위치권한
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private static final String[] PERMISSION = {
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_taco_info);

        tvLocation = findViewById(R.id.tv_location);
        btnConfirm = findViewById(R.id.btn_confirm);
        cbReportFish = findViewById(R.id.cb_report_fish);
        cbReportTaco = findViewById(R.id.cb_report_taco);
        cbReportskewke = findViewById(R.id.cb_report_skewers);
        cbReportchiken = findViewById(R.id.cb_report_chiken);
        edtReportName = findViewById(R.id.edt_report_name);
        edtReportTel = findViewById(R.id.edt_report_tel);
        cbMon = findViewById(R.id.ck_report_0);
        cbTue = findViewById(R.id.ck_report_1);
        cbWed = findViewById(R.id.ck_report_2);
        cbThu = findViewById(R.id.ck_report_3);
        cbFri = findViewById(R.id.ck_report_4);
        cbSat = findViewById(R.id.ck_report_5);
        cbSun = findViewById(R.id.ck_report_6);
        tpReportStart = findViewById(R.id.tp_report_start);
        tpReportEnd = findViewById(R.id.tp_report_end);
        rgReportMenu = findViewById(R.id.rg_report_memu);

        //바텀시트 다이얼로스에서 받아온 식당이름
        Intent intent = getIntent();
        foodTruckname = intent.getStringExtra("foodTruckInfo");
        //체크박스 초기화를 위한 변수
        checkBoxes = new CheckBox[] {cbMon, cbTue, cbWed, cbThu, cbFri, cbSat, cbSun};
        dayCheckBoolean = new Boolean[7];
        Arrays.fill(dayCheckBoolean, false);
        //수정을 위한 기존 정보 가져오기
        reportReference.child("foodTruck").child(foodTruckname).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                foodTruckInfo = snapshot.getValue(FoodTruckInfo.class);
                edtReportName.setText(foodTruckInfo.getName());
                edtReportTel.setText(foodTruckInfo.getTel());
                setStartTime(foodTruckInfo.getStartHour(), foodTruckInfo.getStartMinute());
                setEndTime(foodTruckInfo.getEndHour(), foodTruckInfo.getEndMinute());
                if(foodTruckInfo.getFoodType().equals("붕어빵")){
                    cbReportFish.setChecked(true);
                } else if (foodTruckInfo.getFoodType().equals("타코야끼")) {
                    cbReportTaco.setChecked(true);
                } else if (foodTruckInfo.getFoodType().equals("치킨")) {
                    cbReportchiken.setChecked(true);
                } else{
                    cbReportskewke.setChecked(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("EditTacoInfo", "식당이름으로 조회 실패");
            }
        });
        truckInfoDatabase = FirebaseDatabase.getInstance().getReference();
        truckInfoDatabase.child("foodTruck").child(foodTruckname).child("dayCheckBooleanList").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                GenericTypeIndicator<List<Boolean>> t = new GenericTypeIndicator<List<Boolean>>() { };
                final List<Boolean> checkBoolean = snapshot.getValue(t);
                for (int i = 0; i < checkBoolean.size(); i++) {
                    if (checkBoolean.get(i) == true) {
                        checkBoxes[i].setChecked(true);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("Database", "Boolean배열 가져오기 실패");
            }
        });



        //자동 하이폰
        edtReportTel.addTextChangedListener(new PhoneNumberFormattingTextWatcher());


        // 위치 정보 클라이언트 초기화
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        // 지도 초기화
        FragmentManager fm = getSupportFragmentManager();
        MapFragment mapFragment = (MapFragment) fm.findFragmentById(R.id.report_taco_navermap);
        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            fm.beginTransaction().add(R.id.report_taco_navermap, mapFragment).commit();
        }
        mapFragment.getMapAsync(this);

        // 위치 권한 요청
        requestPermission();

        //요일 체크박스 연결함수
        dayCheckFun();

        //파이어베이스에 저장하기위해 배열을 List로 변환
        List<Boolean> dayCheckBooleanArray = Arrays.asList(dayCheckBoolean);

        //타임피커 정보 받아오기
        tpReportStart.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                startHour = hourOfDay;
                startMinute = minute;
            }
        });
        tpReportEnd.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                endHour = hourOfDay;
                endMinute = minute;
            }
        });

        //메뉴선택 라디오그룹
        rgReportMenu.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.cb_report_fish:
                        selectedMenu = "붕어빵";
                        break;
                    case R.id.cb_report_taco:
                        selectedMenu = "타코야끼";
                        break;
                    case R.id.cb_report_chiken:
                        selectedMenu = "치킨";
                        break;
                    case R.id.cb_report_skewers:
                        selectedMenu = "꼬치";
                        break;
                    default:
                        break;
                }
            }
        });


        //확인버튼 클릭 시 푸드트럭 정보 Firebase에 저장
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFoodTruckInfo(edtReportName.getText().toString(),
                        edtReportTel.getText().toString(), selectedMenu, tvLocation.getText().toString(),
                        getLatitude(tvLocation.getText().toString()), getLongitude(tvLocation.getText().toString()), dayCheckBooleanArray, startHour, startMinute, endHour, endMinute);
                finish();
            }
        });
    }

    //푸드트럭 정보 Firebase에 저장하는 setFoodTruckInfo함수
    public void setFoodTruckInfo(String name, String tel, String foodType, String address, Double latitude, Double longitude,
                                 List<Boolean> dayCheckBooleanArray, int startHour, int startMinute, int endHour, int endMinute) {
        FoodTruckInfo foodTruckInfo = new FoodTruckInfo(name, tel, foodType, address, latitude, longitude, dayCheckBooleanArray, startHour, startMinute, endHour, endMinute);

        reportReference.child("foodTruck").child(foodTruckname).setValue(foodTruckInfo);
    }

    // 핀 움직여서 주소 가져오기
    @SuppressLint("CheckResult")
    private void requestPermission() {
        locationSource = new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);
        TedPermission.create()
                .setRationaleTitle("위치 권한 요청")
                .setRationaleMessage("현재 위치로 이동하기 위해 위치 권한이 필요합니다.")
                .setPermissions(PERMISSION)
                .request()
                .subscribe(tedPermissionResult -> {
                    if (!tedPermissionResult.isGranted()) {
                        Toast.makeText(this, "위치 권한이 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                }, throwable -> Log.e("Location", throwable.getMessage()));
    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        this.reportTacoNaverMap = naverMap;
        naverMap.setLocationSource(locationSource);
        Marker marker = new Marker();
        marker.setPosition(new LatLng(naverMap.getCameraPosition().target.latitude,
                naverMap.getCameraPosition().target.longitude));
        marker.setIcon(OverlayImage.fromResource(R.drawable.test_maker));
        marker.setMap(reportTacoNaverMap);


//        //가시거리 내에 있는 마커만 출력
//        reportTacoNaverMap.addOnCameraChangeListener(new NaverMap.OnCameraChangeListener() {
//            @Override
//            public void onCameraChange(int i, boolean b) {
//
//            }
//        });

        // 지도 이동 시 이벤트 처리
        //카메라가 이동할 때 마커를 중앙에 표시
        reportTacoNaverMap.addOnCameraChangeListener(new NaverMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(int i, boolean b) {
                marker.setPosition(new LatLng(
                        naverMap.getCameraPosition().target.latitude,
                        naverMap.getCameraPosition().target.longitude
                ));
                tvLocation.setText("위치 이동 중");
                tvLocation.setTextColor(Color.parseColor("#c4c4c4"));
                btnConfirm.setBackgroundResource(R.drawable.rect_round_gray_radius_8);
                btnConfirm.setTextColor(Color.parseColor("#FF000000"));
                btnConfirm.setEnabled(false);
            }
        });

        // 지도 이동 완료 시 이벤트 처리
        reportTacoNaverMap.addOnCameraIdleListener(new NaverMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                marker.setPosition(new LatLng(
                        naverMap.getCameraPosition().target.latitude,
                        naverMap.getCameraPosition().target.longitude
                ));
                tvLocation.setText(getAddress(
                        naverMap.getCameraPosition().target.latitude,
                        naverMap.getCameraPosition().target.longitude
                ));
                tvLocation.setTextColor(Color.parseColor("#2d2d2d"));
                btnConfirm.setBackgroundResource(R.drawable.rect_round_ffd464_radius_8);
                btnConfirm.setTextColor(Color.parseColor("#ffffff"));
                btnConfirm.setEnabled(true);
            }
        });

        // 위치 권한이 허용된 경우
        if (ActivityCompat.checkSelfPermission(this, PERMISSION[0]) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, PERMISSION[1]) == PackageManager.PERMISSION_GRANTED) {
            // 마지막으로 알려진 위치를 요청
            fusedLocationClient.getLastLocation()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Location currentLocation = task.getResult();
                            if (currentLocation != null) {
                                // 내 위치 표시
                                naverMap.getLocationOverlay().setVisible(true);
                                naverMap.getLocationOverlay().setPosition(new LatLng(
                                        currentLocation.getLatitude(),
                                        currentLocation.getLongitude()
                                ));

                                //카메라 위치를 이동 (핀은 가운데 고정이기때문에 카메라가 이동하면 핀도 따라 움직임)
                                CameraUpdate cameraUpdate = CameraUpdate.scrollTo(
                                        new LatLng(
                                                foodTruckInfo.getLatitude(),
                                                foodTruckInfo.getLongitude()
                                        )
                                );
                                naverMap.moveCamera(cameraUpdate);

                                // 마커 위치 업데이트
                                marker.setPosition(new LatLng(
                                        naverMap.getCameraPosition().target.latitude,
                                        naverMap.getCameraPosition().target.longitude
                                ));
                            } else {
                            }
                        } else {
                            Exception exception = task.getException();
                            if (exception != null) {
                                exception.printStackTrace();
                            }
                        }
                    });
        }
    }


    // 주소 가져오기
    private String getAddress(double lat, double lng) {
        Geocoder geocoder = new Geocoder(this, Locale.KOREA);
        ArrayList<Address> address;
        String addressResult = "주소를 가져올 수 없습니다.";
        try {
            address = (ArrayList<Address>) geocoder.getFromLocation(lat, lng, 1);
            if (address.size() > 0) {
                String currentLocationAddress = address.get(0).getAddressLine(0).toString();
                addressResult = currentLocationAddress;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return addressResult;
    }

    //LatLng 변환함수 //Todo: 삭제예정 (파이어베이스에서 가져올떄 LatLng형으로 가져오기 번거로움)
    private LatLng getLatLng(String address) {
        Geocoder geocoder = new Geocoder(this, Locale.KOREA);
        List<Address> list;

        try {
            list = geocoder.getFromLocationName(address, 10);

            if (list != null && list.size() > 0) {
                Address firstResult = list.get(0);
                Toast.makeText(this, "성공", Toast.LENGTH_SHORT).show();
                return new LatLng(firstResult.getLatitude(), firstResult.getLongitude());
            } else {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    //주소 -> 위도 변환
    private Double getLatitude(String address) {
        Geocoder geocoder = new Geocoder(this, Locale.KOREA);
        List<Address> list;

        try {
            list = geocoder.getFromLocationName(address, 10);

            if (list != null && list.size() > 0) {
                Address firstResult = list.get(0);
                Toast.makeText(this, "성공", Toast.LENGTH_SHORT).show();
                return firstResult.getLatitude();
            } else {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    //주소 -> 경도 변환
    private Double getLongitude(String address) {
        Geocoder geocoder = new Geocoder(this, Locale.KOREA);
        List<Address> list;

        try {
            list = geocoder.getFromLocationName(address, 10);

            if (list != null && list.size() > 0) {
                Address firstResult = list.get(0);
                Toast.makeText(this, "성공", Toast.LENGTH_SHORT).show();
                return firstResult.getLongitude();
            } else {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    //선택된 요일 체크 함수
    private void dayCheckFun() {
        cbMon.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (cbMon.isChecked()) {
                    dayCheckBoolean[0] = true;
                } else {
                    dayCheckBoolean[0] = false;
                }
            }
        });
        cbTue.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (cbTue.isChecked()) {
                    dayCheckBoolean[1] = true;
                } else {
                    dayCheckBoolean[1] = false;
                }
            }
        });
        cbWed.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (cbWed.isChecked()) {
                    dayCheckBoolean[2] = true;
                } else {
                    dayCheckBoolean[2] = false;
                }
            }
        });
        cbThu.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (cbThu.isChecked()) {
                    dayCheckBoolean[3] = true;
                } else {
                    dayCheckBoolean[3] = false;
                }
            }
        });
        cbFri.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (cbFri.isChecked()) {
                    dayCheckBoolean[4] = true;
                } else {
                    dayCheckBoolean[4] = false;
                }
            }
        });
        cbSat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (cbSat.isChecked()) {
                    dayCheckBoolean[5] = true;
                } else {
                    dayCheckBoolean[5] = false;
                }
            }
        });
        cbSun.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (cbSun.isChecked()) {
                    dayCheckBoolean[6] = true;
                } else {
                    dayCheckBoolean[6] = false;
                }
            }
        });
    }

    //setHour 메소드를 사용하면 AP! LEVEL23이 필요하다는 오류가 생겨
    private void setStartTime(int hour, int minute) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            tpReportStart.setHour(hour);
            tpReportStart.setMinute(minute);
        }
    }

    private void setEndTime(int hour, int minute) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            tpReportEnd.setHour(hour);
            tpReportEnd.setMinute(minute);
        }
    }
}
