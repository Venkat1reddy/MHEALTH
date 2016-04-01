package com.maps.mhealth;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

public class BmiCalculator extends Activity
{
    ArrayAdapter<String> heightFeetsAdapter;
    ArrayAdapter<String> heightMetersAdapter;
    ArrayAdapter<String> weightLibsAdapter;
    ArrayAdapter<String> weightKgsAdapter;

    Spinner weightSpinner;
    Spinner heightSpinner;
    Spinner weightUnitSpinner;
    Spinner heightUnitSpinner;
    TextView bmiLabel;
    TextView bmiValueText;
    TextView bmiDescriptionText;
    Button heartRateButton;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bmi);

        weightSpinner = (Spinner)findViewById(R.id.spinner1);
        weightUnitSpinner = (Spinner)findViewById(R.id.spinner2);
        heightSpinner = (Spinner)findViewById(R.id.spinner3);
        heightUnitSpinner = (Spinner)findViewById(R.id.spinner4);
        bmiLabel = (TextView)findViewById(R.id.textView3);
        bmiValueText = (TextView)findViewById(R.id.textView4);
        bmiDescriptionText = (TextView)findViewById(R.id.textView5);
        heartRateButton = (Button)findViewById(R.id.heartRateBtn);

        initializeSpinnerAdapters();

        // load the default values for the spinners
        loadLibsValueRange();
        loadFeetsValueRange();

        // add listeners to the unit changes
        addListernsToUnitChanges();

        heartRateButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                startActivity(new Intent(BmiCalculator.this, MapsActivity.class));
            }
        });
    }

    public void initializeSpinnerAdapters()
    {
        String[] weightLibs = new String[300];
        int k = 299;
        for (int i = 1; i <= 300; i ++)
        {
            weightLibs[k--] = String.format("%3d", i);
        }
        weightLibsAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, weightLibs);
        String[] weightKgs = new String[200];

        k = 199;
        for (int i = 1; i <= 200; i ++)
        {
            weightKgs[k--] = String.format("%3d", i);
        }
        weightKgsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, weightKgs);

        String[] heightFeets = new String[60];
        k = 59;
        for (int i = 3; i < 8; i ++)
        {
            for (int j = 0; j < 12; j ++)
            {
                heightFeets[k--] = i + "\"" + String.format("%02d", j) + "'";
            }
        }

        heightFeetsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, heightFeets);

        String[] heightMeters = new String[300];
        // loading 0.0 to 2.9 to the height in m/cm
        k = 299;
        for (int i = 0; i < 3; i ++)
        {
            for (int j = 0; j < 100; j ++)
            {
                heightMeters[k--] = i + "." + j;
            }
        }

        heightMetersAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, heightMeters);
    }

    public void loadLibsValueRange()
    {
        weightSpinner.setAdapter(weightLibsAdapter);
        // set the default lib value
        weightSpinner.setSelection(weightLibsAdapter.getPosition("170"));
    }

    public void loadFeetsValueRange()
    {
        heightSpinner.setAdapter(heightFeetsAdapter);
        // set the default value to feets
        heightSpinner.setSelection(heightFeetsAdapter.getPosition("5\"05'"));
    }

    public void addListernsToUnitChanges()
    {
        weightUnitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int row, long id) {
                // load the relevent units and the values
                if (row == 0) {
                    // libs is selected
                    loadLibsValueRange();
                } else {
                    // kg is selected
                    loadKgsValueRange();
                }
            }

            public void onNothingSelected(AdapterView<?> arg0) {
                // Nothing to do here
            }
        });


        // listener to the height unit
        heightUnitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int row, long id) {
                // load the relevent units and the values
                if (row == 0) {
                    // feets is selected
                    loadFeetsValueRange();
                } else {
                    // meters is selected
                    loadMetersValueRange();
                }
            }

            public void onNothingSelected(AdapterView<?> arg0) {
                // Nothing to do here
            }
        });
    }

    public void loadKgsValueRange()
    {
        weightSpinner.setAdapter(weightKgsAdapter);
        // set the default vaule for kg
        weightSpinner.setSelection(weightKgsAdapter.getPosition(" 77"));
    }

    public void loadMetersValueRange()
    {
        heightSpinner.setAdapter(heightMetersAdapter);
        // set the default value to meters
        heightSpinner.setSelection(heightMetersAdapter.getPosition("1.65"));
    }

    public void calculateClickHandler(View view) {
        // make sure we handle the click of the calculator button
        if (view.getId() == R.id.button1) {

            // get the users values from the spinners (converted to floats and metrics units)
            float weight = getSelectedWeight();
            Utils.weight = weight;
            float height = getSelectedHeight();
            bmiLabel.setText("Body Mass Index:");
            // calculate the bmi value and set it in the text
            float bmiValue = calculateBMI(weight, height);
            bmiValueText.setText(bmiValue + "");

            // interpret the meaning of the bmi value and set it in the text
            int bmiInterpretation = interpretBMI(bmiValue);
            bmiDescriptionText.setText(getResources().getString(bmiInterpretation));

            // color for the bmi text fields
            int bmiColor = colorBMI(bmiValue);
            bmiValueText.setTextColor(getResources().getColor(bmiColor));
            bmiDescriptionText.setTextColor(getResources().getColor(bmiColor));

            heartRateButton.setVisibility(View.VISIBLE);
        }
    }

    public float getSelectedWeight() {
        String selectedWeightValue = (String)weightSpinner.getSelectedItem();
        if (weightUnitSpinner.getSelectedItemPosition() == 0) {
            // the position is libs, so convert to kg and return
            return (float) (Float.parseFloat(selectedWeightValue) * 0.45359237);
        } else {
            // already kg is selected, so no need to covert (just cast to float)
            return Float.parseFloat(selectedWeightValue);
        }
    }

    // retrieve the hight from the spinner control convented to me
    public float getSelectedHeight() {
        String selectedHeightValue = (String)heightSpinner.getSelectedItem();
        if (heightUnitSpinner.getSelectedItemPosition() == 0) {
            // the position is feets and inches, so convert to meters and return
            String feets = selectedHeightValue.substring(0, 1);
            String inches = selectedHeightValue.substring(2,4);
            return (float) (Float.parseFloat(feets) * 0.3048) +
                    (float) (Float.parseFloat(inches) * 0.0254);
        } else {
            // already meters is selected, so no need to covert (just cast to float)
            return Float.parseFloat(selectedHeightValue);
        }
    }

    private float calculateBMI (float weight, float height) {
        return (float) (weight / (height * height));
    }

    // returns the string name defined in strings.xml
    // that interpret the BMI
    private int interpretBMI(float bmiValue) {
        if (bmiValue < 16) {
            return R.string.bmiSUnder;
        } else if (bmiValue < 18.5) {
            return R.string.bmiUnder;
        } else if (bmiValue < 25) {
            return R.string.bmiNormal;
        } else if (bmiValue < 30) {
            return R.string.bmiOver;
        } else {
            return R.string.bmiObese;
        }
    }

    // returns the color name defined in strings.xml
    // that represent the BMI
    private int colorBMI(float bmiValue) {
        if (bmiValue < 16) {
            return R.color.colorRed;
        } else if (bmiValue < 18.5) {
            return R.color.colorPurple;
        } else if (bmiValue < 25) {
            return R.color.colorGreen;
        } else if (bmiValue < 30) {
            return R.color.colorPurple;
        } else {
            return R.color.colorRed;
        }
    }

    @Override
    public void onBackPressed()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm");
        builder.setMessage("Are you sure to Exit?");
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                finish();
            }
        });

        builder.setNegativeButton("NO", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
