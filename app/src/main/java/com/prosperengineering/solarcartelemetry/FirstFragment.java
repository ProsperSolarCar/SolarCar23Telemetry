package com.prosperengineering.solarcartelemetry;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.ekndev.gaugelibrary.ArcGauge;
import com.ekndev.gaugelibrary.HalfGauge;
import com.ekndev.gaugelibrary.Range;
import com.prosperengineering.solarcartelemetry.databinding.FragmentFirstBinding;






public class FirstFragment extends Fragment
    {

    private FragmentFirstBinding binding;
    ArcGauge gaugeSpeed;
    ArcGauge gaugeMainVoltage;
    ArcGauge gaugeAuxVoltage;
    ArcGauge gaugeSolarCurrent;
    ArcGauge gaugeMotorCurrent;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
        {

        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();

        }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState)
        {
        super.onViewCreated(view, savedInstanceState);

        int iRed = Color.parseColor("#ce0000");
        int iYellow = Color.parseColor("#E3E500");
        int iGreen = Color.parseColor("#00b20b");

        gaugeSpeed = (ArcGauge) view.findViewById(R.id.speedGauge);
        InitArcGauge (gaugeSpeed,
           0.0f, 55.0f,
           0.33f,0.66f,
            iGreen,
            iYellow,
            iRed);
        gaugeSpeed.setPrecision(0);
        gaugeSpeed.setTextSize(100f);

        gaugeMainVoltage = (ArcGauge) view.findViewById(R.id.mainVoltageGauge);
        InitArcGauge (gaugeMainVoltage,
                0.0f, 90.0f,
                0.33f,0.66f,
                iRed,
                iYellow,
                iGreen);
        gaugeMainVoltage.setPrecision(2);

        gaugeAuxVoltage = (ArcGauge) view.findViewById(R.id.auxVoltageGauge);
        InitArcGauge (gaugeAuxVoltage,
                0.0f, 20.0f,
                0.33f,0.66f,
                iRed,
                iYellow,
                iGreen);
        gaugeAuxVoltage.setPrecision(2);

        gaugeSolarCurrent = (ArcGauge) view.findViewById(R.id.solarCurrentGauge);
        InitArcGauge (gaugeSolarCurrent,
                0.0f, 20.0f,
                0.33f,0.66f,
                iRed,
                iYellow,
                iGreen);
        gaugeSolarCurrent.setPrecision(2);

        gaugeMotorCurrent = (ArcGauge) view.findViewById(R.id.motorCurrentGauge);
        InitArcGauge (gaugeMotorCurrent,
                0.0f, 100.0f,
                0.33f,0.66f,
                iRed,
                iYellow,
                iGreen);
        gaugeMotorCurrent.setPrecision(2);






        /*
        binding.buttonFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
            }
        });

         */
        }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    public void InitArcGauge(ArcGauge gaugeIn,
                             float          fRangeStart,
                             float          fRangeEnd,
                             float          fRangeLeftParametric,
                             float          fRangeRightParametric,
                             int            clrLeft,
                             int            clrCenter,
                             int            clrRight)
        {
        Float  fRangeDelta = fRangeEnd - fRangeStart;

        Range range = new Range();
        range.setColor(clrLeft);
        range.setFrom(fRangeStart);
        range.setTo(fRangeStart + fRangeDelta * fRangeLeftParametric);

        Range range2 = new Range();
        range2.setColor(clrCenter);
        range2.setFrom(fRangeStart + fRangeDelta * fRangeLeftParametric);
        range2.setTo(fRangeStart + fRangeDelta * fRangeRightParametric);

        Range range3 = new Range();
        range3.setColor(clrRight);
        range3.setFrom(fRangeStart + fRangeDelta * fRangeRightParametric);
        range3.setTo(fRangeEnd);

        //add color ranges to gauge
        gaugeIn.addRange(range);
        gaugeIn.addRange(range2);
        gaugeIn.addRange(range3);

        //set min max and current value
        gaugeIn.setMinValue(fRangeStart);
        gaugeIn.setMaxValue(fRangeEnd);
        gaugeIn.setTextSize(85f);
        };

    public void InitHalfGauge(HalfGauge  gaugeIn,
                          float          fRangeStart,
                          float          fRangeEnd,
                          float          fRangeLeftParametric,
                          float          fRangeRightParametric,
                          int            clrLeft,
                          int            clrCenter,
                          int            clrRight)
        {
        Float  fRangeDelta = fRangeEnd - fRangeStart;

        Range range = new Range();
        range.setColor(clrLeft);
        range.setFrom(fRangeStart);
        range.setTo(fRangeStart + fRangeDelta * fRangeLeftParametric);

        Range range2 = new Range();
        range2.setColor(clrCenter);
        range2.setFrom(fRangeStart + fRangeDelta * fRangeLeftParametric);
        range2.setTo(fRangeStart + fRangeDelta * fRangeRightParametric);

        Range range3 = new Range();
        range3.setColor(clrRight);
        range3.setFrom(fRangeStart + fRangeDelta * fRangeRightParametric);
        range3.setTo(fRangeEnd);

        //add color ranges to gauge
        gaugeIn.addRange(range);
        gaugeIn.addRange(range2);
        gaugeIn.addRange(range3);

        //set min max and current value
        gaugeIn.setMinValue(fRangeStart);
        gaugeIn.setMaxValue(fRangeEnd);
        gaugeIn.setTextSize(85f);
        };

    }
