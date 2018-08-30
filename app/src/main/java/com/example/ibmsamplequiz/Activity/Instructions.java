package com.example.ibmsamplequiz.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.ibmsamplequiz.R;

public class Instructions extends AppCompatActivity {

    private TextView quizTitle, totalTime, marksPerQues, numberOfQues;
    SharedPreferences shared_quizDetails;
    SharedPreferences.Editor edit_quizDetails;
    AppCompatButton splashSubmit;
    LinearLayout timeLinear, marksLinear,questionsLinear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_information);

        shared_quizDetails = getSharedPreferences("QuizDetails", Context.MODE_PRIVATE);
        edit_quizDetails = shared_quizDetails.edit();
        quizTitle = findViewById(R.id.quizTitle);
        totalTime = findViewById(R.id.totalTime);
        splashSubmit = findViewById(R.id.instructionStart);
        timeLinear = findViewById(R.id.timeLinear);
        marksLinear = findViewById(R.id.marksLinear);
        questionsLinear = findViewById(R.id.totalQuesLinear);

        marksPerQues = findViewById(R.id.marks);
        numberOfQues = findViewById(R.id.totalques);

        quizTitle.setText(shared_quizDetails.getString("Title",""));
        totalTime.setText("Total Time: " + shared_quizDetails.getInt("totalTime",0) + " minutes");
        numberOfQues.setText("Total questions: " + shared_quizDetails.getInt("noOfQues",0));
        marksPerQues.setText("Marks per question: " + shared_quizDetails.getInt("marksPerQues",0));

        Animation botAnimation = AnimationUtils.loadAnimation(this,R.anim.splash_animation);
        quizTitle.setAnimation(botAnimation);
        timeLinear.setAnimation(botAnimation);
        marksLinear.setAnimation(botAnimation);
        questionsLinear.setAnimation(botAnimation);

        splashSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent i = new Intent(Instructions.this,Questions.class);
                overridePendingTransition(0,0);
                startActivity(i);
            }
        });

    }
}
