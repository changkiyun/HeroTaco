package com.example.herotaco;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;

import com.example.herotaco.Firebase.FoodTruckInfo;
import com.example.herotaco.OpenAPI.Item;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FoodTruckInfoDialog extends BottomSheetDialogFragment {
    private TextView tvBottomSheetName, tvBottomSheetTel, tvBottomSheetTime, tvBottomSheetAddress, tvBottomSheetFoodType;
    private AppCompatButton btnBottomSheetOrder, btnBottomSheetDelete;
    private CheckBox cbMon, cbTue, cbWed, cbThu, cbFri, cbSat, cbSun;
    private CheckBox[] checkBoxes;
    LinearLayout l6, l7, l8;
    //파이어베이스에서 가져온 정보가 담길 객체
    FoodTruckInfo foodTruckInfo;
    //파싱한 정보가 담길 객체
    Item item;
    private Boolean[] checkDayBoolean = new Boolean[7];
    private DatabaseReference truckInfoDatabase;

    //파이어베이스에서 가져온 객체를 받아 오는 생성자
    public FoodTruckInfoDialog(FoodTruckInfo foodTruckInfo) {
        this.foodTruckInfo = foodTruckInfo;
    }
    //파싱한 정보를 받아오는 생성자
    public FoodTruckInfoDialog(Item item){
        this.item = item;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.foodtruck_info_dialog, container, false);
        tvBottomSheetName = view.findViewById(R.id.tv_bottom_sheet_name);
        tvBottomSheetTel = view.findViewById(R.id.tv_bottom_sheet_tel);
        tvBottomSheetTime = view.findViewById(R.id.tv_bottom_sheet_time);
        tvBottomSheetAddress = view.findViewById(R.id.tv_bottom_sheet_address);
        tvBottomSheetFoodType = view.findViewById(R.id.tv_bottom_sheet_food);
        btnBottomSheetOrder = view.findViewById(R.id.btn_bottom_sheet_order);
        btnBottomSheetDelete = view.findViewById(R.id.btn_bottom_sheet_delete);
        cbMon = view.findViewById(R.id.ck_report_0);
        cbTue = view.findViewById(R.id.ck_report_1);
        cbWed = view.findViewById(R.id.ck_report_2);
        cbThu = view.findViewById(R.id.ck_report_3);
        cbFri = view.findViewById(R.id.ck_report_4);
        cbSat = view.findViewById(R.id.ck_report_5);
        cbSun = view.findViewById(R.id.ck_report_6);
        l6 = view.findViewById(R.id.linearLayout6);
        l7 = view.findViewById(R.id.linearLayout7);
        l8 = view.findViewById(R.id.linearLayout8);
        //체크박스 클릭 안되게 설정
        checkBoxes = new CheckBox[]{cbMon, cbTue, cbWed, cbThu, cbFri, cbSat, cbSun};
        for(CheckBox cb : checkBoxes){
            cb.setClickable(false);
        }
        // item이 null이 아닌 경우(파싱한 마커가 클릭된 경우)
        if (item != null) {
            tvBottomSheetName.setText(item.getName());
            tvBottomSheetAddress.setText(item.getRoadAddress());
            tvBottomSheetTel.setText(item.getTel());
            l6.setVisibility(View.GONE);
            l7.setVisibility(View.GONE);
            l8.setVisibility(View.GONE);
            btnBottomSheetOrder.setVisibility(View.GONE);
            btnBottomSheetDelete.setVisibility(View.GONE);
        }
        // foodTruckInfo가 null이 아닌 경우 (파이어베이스로 생성한 마커가 클릭된 경우)
        else if (foodTruckInfo != null) {
            tvBottomSheetName.setText(foodTruckInfo.getName());
            tvBottomSheetTel.setText(foodTruckInfo.getTel());
            tvBottomSheetAddress.setText(foodTruckInfo.getAddress());
            tvBottomSheetFoodType.setText(foodTruckInfo.getFoodType());

            tvBottomSheetTime.setText(foodTruckInfo.getStartHour() + ":" + foodTruckInfo.getStartMinute() + " ~ " + foodTruckInfo.getEndHour() + ":" + foodTruckInfo.getEndMinute());

            // 파이어베이스에 Object로 저장된 값을 List<Boolean> 형으로 변환 후 영업 요일 체크
            truckInfoDatabase = FirebaseDatabase.getInstance().getReference();
            truckInfoDatabase.child("foodTruck").child(foodTruckInfo.getName()).child("dayCheckBooleanList").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    GenericTypeIndicator<List<Boolean>> t = new GenericTypeIndicator<List<Boolean>>() {
                    };
                    final List<Boolean> checkBoolean = task.getResult().getValue(t);
                    for (int i = 0; i < checkBoolean.size(); i++) {
                        if (checkBoolean.get(i) == true) {
                            checkBoxes[i].setChecked(true);
                        }
                    }
                }
            });

            //정보 수정 액티비티
            btnBottomSheetOrder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), EditTacoInfo.class);
                    intent.putExtra("foodTruckInfo", foodTruckInfo.getName());
                    getContext().startActivity(intent);
                }
            });
        }

        btnBottomSheetDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                truckInfoDatabase.child("foodTruck").child(foodTruckInfo.getName()).removeValue();
            }
        });

        return view;
    }

    //Todo: 삭제예정
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.tv_bottom_sheet_name).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }


}
