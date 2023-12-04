package com.example.herotaco;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
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
import java.util.Locale;

public class ReportTaco extends AppCompatActivity implements OnMapReadyCallback {
    private FusedLocationProviderClient fusedLocationClient;
    private FusedLocationSource locationSource;
    private NaverMap reportTacoNaverMap;
    private TextView tvLocation;
    private AppCompatButton btnConfirm;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private static final String[] PERMISSION = {
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report_taco);

        // 위치 정보 클라이언트 초기화
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        tvLocation = findViewById(R.id.tv_location);
        btnConfirm = findViewById(R.id.btn_confirm);

        // 지도 초기화
        FragmentManager fm = getSupportFragmentManager();
        MapFragment mapFragment = (MapFragment) fm.findFragmentById(R.id.reportTacoNaverMap);
        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            fm.beginTransaction().add(R.id.reportTacoNaverMap, mapFragment).commit();
        }
        mapFragment.getMapAsync(this);

        // 위치 권한 요청
        requestPermission();

    }

    // 위치 권한 관련 요청
    private void requestPermission() {
        // 내장 위치 추적 기능 사용
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
                }, throwable -> Log.e("AAAAAA", throwable.getMessage()));
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

        // 지도 이동 시 이벤트 처리
        reportTacoNaverMap.addOnCameraChangeListener((reason, animated) -> {
            marker.setPosition(new LatLng(
                    naverMap.getCameraPosition().target.latitude,
                    naverMap.getCameraPosition().target.longitude
            ));
            tvLocation.setText("위치 이동 중");
            tvLocation.setTextColor(Color.parseColor("#c4c4c4"));
            btnConfirm.setBackgroundResource(R.drawable.rect_round_gray_radius_8);
            btnConfirm.setTextColor(Color.parseColor("#ffffff"));
            btnConfirm.setEnabled(false);
        });

        // 지도 이동 완료 시 이벤트 처리
        reportTacoNaverMap.addOnCameraIdleListener(() -> {
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
            btnConfirm.setTextColor(Color.parseColor("#FF000000"));
            btnConfirm.setEnabled(true);
        });

        // 위치 권한이 허용된 경우
        if (ActivityCompat.checkSelfPermission(
                this, PERMISSION[0]
        ) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                        this, PERMISSION[1]
                ) == PackageManager.PERMISSION_GRANTED) {
            // 마지막으로 알려진 위치를 요청
            fusedLocationClient.getLastLocation()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Location currentLocation = task.getResult();
                            if (currentLocation != null) {
                                // 현재 위치 처리
                                naverMap.getLocationOverlay().setVisible(true);
                                naverMap.getLocationOverlay().setPosition(new LatLng(
                                        currentLocation.getLatitude(),
                                        currentLocation.getLongitude()
                                ));

                                CameraUpdate cameraUpdate = CameraUpdate.scrollTo(
                                        new LatLng(
                                                currentLocation.getLatitude(),
                                                currentLocation.getLongitude()
                                        )
                                );
                                naverMap.moveCamera(cameraUpdate);

                                // 마커 위치 업데이트
                                marker.setPosition(new LatLng(
                                        naverMap.getCameraPosition().target.latitude,
                                        naverMap.getCameraPosition().target.longitude
                                ));
                            } else {
                                // 위치가 null인 경우 처리
                                // 메시지를 표시하거나 적절한 조치를 취할 수 있습니다.
                            }
                        } else {
                            // 위치를 가져오지 못한 경우 처리
                            Exception exception = task.getException();
                            if (exception != null) {
                                exception.printStackTrace(); // 로그 또는 예외 처리
                            }
                        }
                    });
        }

        //확인버튼
        Intent reportIntent = new Intent(this, ReportTacoInfo.class);
        reportIntent.putExtra("주소값", getAddress(
                naverMap.getCameraPosition().target.latitude,
                naverMap.getCameraPosition().target.longitude
        ));
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(reportIntent);
            }
        });

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
}
