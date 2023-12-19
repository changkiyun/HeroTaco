package com.example.herotaco;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.example.herotaco.Firebase.FoodTruckInfo;
import com.example.herotaco.OpenAPI.Item;
import com.example.herotaco.OpenAPI.MyAsyncTask;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.geometry.LatLngBounds;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.MapView;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.Overlay;
import com.naver.maps.map.overlay.OverlayImage;
import com.naver.maps.map.util.FusedLocationSource;

import android.Manifest;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, MyAsyncTask.Callback {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private static final String[] PERMISSION = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };
    private NaverMap naverMap;
    private FusedLocationSource locationSource;
    private MapView mapView;

    private FloatingActionButton mainFloat;

    private Marker testMaker = new Marker();

    InputStream is;
    private MyAsyncTask myAsyncTask;
    private boolean isCameraIdle = true;

    //Firebase 참조객체
    private FirebaseDatabase reportDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference reportReference = reportDatabase.getReference("foodTruck");
    //Firebase에서 받아온 FoodTruckInfo를 저장할 ArrayList
    private ArrayList<FoodTruckInfo> foodTruckInfoArrayList = new ArrayList<>();

    //BottomSheet
    FoodTruckInfoDialog foodtruckInfoDialog;
    TextView testBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainFloat = findViewById(R.id.mainFloat);

        //Firebase Marker Listener DB정보가 변할때 마커를 생성 (리스너 연결시점에도 이벤트 발생)
        reportReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    FoodTruckInfo foodTruckInfo = dataSnapshot.getValue(FoodTruckInfo.class);
                    foodTruckInfoArrayList.add(foodTruckInfo);
                }
                for(FoodTruckInfo foodTruckInfo : foodTruckInfoArrayList){
                    Marker marker = new Marker();
                    marker.setPosition(new LatLng(foodTruckInfo.getLatitude(), foodTruckInfo.getLongitude()));

                    String foodType = foodTruckInfo.getFoodType();
                    if ("붕어빵".equals(foodType)) {
                        marker.setIcon(OverlayImage.fromResource(R.drawable.taco));
                    } else if ("타코야끼".equals(foodType)) {
                        marker.setIcon(OverlayImage.fromResource(R.drawable.fiskbread));
                    } else if ("꼬치".equals(foodType)) {
                        marker.setIcon(OverlayImage.fromResource(R.drawable.kkochi));
                    } else if ("치킨".equals(foodType)) {
                        marker.setIcon(OverlayImage.fromResource(R.drawable.chiken));
                    }
                    marker.setMap(naverMap);
                    marker.setOnClickListener(new Overlay.OnClickListener() {
                        @Override
                        public boolean onClick(@NonNull Overlay overlay) {
                            foodtruckInfoDialog = new FoodTruckInfoDialog(foodTruckInfo);
                            foodtruckInfoDialog.setStyle(DialogFragment.STYLE_NORMAL, R.style.RoundCornerBottomSheetDialogTheme);
                            foodtruckInfoDialog.show(getSupportFragmentManager(), foodtruckInfoDialog.getTag());
                            return false;
                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "Firebase마커생성오류", Toast.LENGTH_SHORT).show();
            }
        });

        //지도 객체 생성하기
        FragmentManager fragmentManager = getSupportFragmentManager();
        MapFragment mapFragment = (MapFragment) fragmentManager.findFragmentById(R.id.naverMap);
        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            fragmentManager.beginTransaction().add(R.id.naverMap, mapFragment).commit();
        }
        //getMapAsync 호출해 비동기로 onMapReady 콜백 메서드 호출
        //onMapReady에서 NaverMap 객체를 받음.
        mapFragment.getMapAsync(this);

        //위치를 반환하는 구현체인 FusedLocationSource 생성
        locationSource = new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);

        //reportTacoActicity 이동
        mainFloat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent reportIntent = new Intent(MainActivity.this, ReportTaco.class);
                startActivity(reportIntent);
            }
        });


    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        // NaverMap 객체 받아서 NaverMap 객체에 위치 소스 지정
        this.naverMap = naverMap;
        naverMap.setLocationSource(locationSource);

        // 권한 확인, 결과는 onRequestPermissionResult 콜백 메서드 호출
        ActivityCompat.requestPermissions(this, PERMISSION, LOCATION_PERMISSION_REQUEST_CODE);

        //휴게음식점 정보 파싱
        myAsyncTask = new MyAsyncTask(this, this);
        myAsyncTask.execute();

        naverMap.addOnCameraIdleListener(() -> {
            // 카메라 상태가 변하지 않았을 때 호출되는 콜백
            isCameraIdle = true;
            updateMarkers(); // 레이지 로딩 업데이트
        });

    }

    //파싱이 끝나기 전에 마커를 생성하면 NullPointer오류가 발생하기때문에 파싱이 종료된 이후에 실행되는 onPostExecuteCallback()에 updateMarkers()가 위치
    @Override
    public void onPostExecuteCallback() {
        updateMarkers();
    }

    //파싱한 매장 마커생성
    private void updateMarkers() {
        if (isCameraIdle && myAsyncTask != null && myAsyncTask.getList() != null && !myAsyncTask.getList().isEmpty()) {

            ArrayList<Item> itemList = myAsyncTask.getList();
            LatLngBounds visibleBounds = naverMap.getContentBounds(); // 현재 지도 화면에 보이는 영역

            for (Item item : itemList) {
                // 현재 지도 화면에 보이는 영역 내의 마커만 생성 및 업데이트
                if (visibleBounds.contains(item.getLatLng())) {
                    Marker marker = new Marker();
                    marker.setPosition(item.getLatLng());
                    OverlayImage image = OverlayImage.fromResource(R.drawable.not_report_16);
                    marker.setIcon(image);
                    marker.setMap(naverMap);

                    //마커리스너 연결 및 바텀시트다이얼로그 생성
                    marker.setOnClickListener(new Overlay.OnClickListener() {
                        @Override
                        public boolean onClick(@NonNull Overlay overlay) {
                            foodtruckInfoDialog = new FoodTruckInfoDialog(item);
                            foodtruckInfoDialog.setStyle(DialogFragment.STYLE_NORMAL, R.style.RoundCornerBottomSheetDialogTheme);
                            foodtruckInfoDialog.show(getSupportFragmentManager(), foodtruckInfoDialog.getTag());
                            return true;
                        }
                    });
                }
            }
        } else {
            Log.d("sd","sd");
//            Toast.makeText(getApplicationContext(), "데이터를 가져오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
        }
    }




    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //request code와 권한 획득 여부 확인
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);
            }
        }
    }



    //위도경도 가져오기
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
                // 결과가 없는 경우를 처리합니다.
                return null; // 또는 예외를 throw하거나 기본 LatLng을 반환 등의 처리를 수행합니다.
            }
        } catch (IOException e) {
            e.printStackTrace();
            // 예외를 처리합니다. 예를 들어, 로그를 기록하거나 기본 LatLng을 반환합니다.
            return null; // 또는 예외를 throw하거나 기본 LatLng을 반환 등의 처리를 수행합니다.
        }
    }

}