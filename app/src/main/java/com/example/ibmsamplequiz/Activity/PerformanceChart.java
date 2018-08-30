package com.example.ibmsamplequiz.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.View;

import com.example.ibmsamplequiz.R;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.util.ArrayList;

public class PerformanceChart extends AppCompatActivity {

    PieChart pieChart;
    AppCompatButton next;

    SharedPreferences shared_quizDetails;
    SharedPreferences.Editor edit_quizDetails;

    float total, correct, incorrect, correctpercent,incorrectpercent,threshold;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_performance_chart);

        shared_quizDetails = getSharedPreferences("QuizDetails", Context.MODE_PRIVATE);
        edit_quizDetails = shared_quizDetails.edit();


        correct = shared_quizDetails.getInt("corrected",0);
        incorrect = shared_quizDetails.getInt("incorrected",0);
        threshold = shared_quizDetails.getInt("passing",0);

        total = correct + incorrect;

        correctpercent = (correct/total)*100;
        incorrectpercent = (incorrect/total)*100;

        pieChart = (PieChart) findViewById(R.id.piechart);
        next = findViewById(R.id.performanceNext);

        pieChart.setUsePercentValues(true);
        pieChart.setRotationEnabled(true);
        pieChart.setUsePercentValues(true);
        pieChart.setHoleRadius(50f);
        pieChart.setTransparentCircleAlpha(0);
        pieChart.setDrawEntryLabels(true);

        if(correctpercent>=threshold) {
            pieChart.setCenterText("Congratulations you passed the test!");
        }
        else
        {
            pieChart.setCenterText("Sorry you did not pass the test!");

        }


        pieChart.getDescription().setEnabled(false);

        ArrayList<PieEntry> yEntry = new ArrayList <PieEntry>();
        ArrayList<String> xEntry = new ArrayList <String>();

        yEntry.add(new PieEntry(correctpercent,0));
        yEntry.add(new PieEntry(incorrectpercent,1));

        xEntry.add("Correct");
        xEntry.add("Incorrect");



        PieDataSet pieDataSet = new PieDataSet(yEntry,"");

        pieDataSet.setSliceSpace(2);
        pieDataSet.setValueTextSize(12);
        pieDataSet.setValueTextColor(getResources().getColor(R.color.colorWhite));

        ArrayList<Integer> colors = new ArrayList <Integer>();
        colors.add(getResources().getColor(R.color.colorPrimary));
        colors.add(getResources().getColor(R.color.colorAccent));

        pieDataSet.setColors(colors);

        PieData pieData = new PieData(pieDataSet);

        pieData.setValueFormatter(new PercentFormatter());

        pieChart.setData(pieData);

        pieChart.invalidate();

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(PerformanceChart.this,Score.class);
                startActivity(i);
            }
        });

        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {


                Log.d("Value",e.toString());

                if(e.getY() == correctpercent) {
                    pieChart.setCenterText("Correct percent " + (Math.round(correctpercent * 100.0) / 100.0) + "%");
                }
                else {
                    pieChart.setCenterText("Incorrect percent " + (Math.round(incorrectpercent * 100.0) / 100.0) + "%");
                }

            }

            @Override
            public void onNothingSelected() {
                if(correctpercent>=threshold) {
                    pieChart.setCenterText("Congratulations you passed the test!");
                }
                else
                {
                    pieChart.setCenterText("Sorry you did not pass the test!");

                }
            }
        });
    }

    @Override
    public void onBackPressed() {

    }
}
