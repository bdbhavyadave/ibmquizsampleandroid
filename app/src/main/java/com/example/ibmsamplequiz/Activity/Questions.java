package com.example.ibmsamplequiz.Activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.example.ibmsamplequiz.AdapterObjectClasses.JumpTo;
import com.example.ibmsamplequiz.Adapters.JumpToAdapter;
import com.example.ibmsamplequiz.Helper.ObjectSerializer;
import com.example.ibmsamplequiz.R;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.ibm.watson.developer_cloud.text_to_speech.v1.TextToSpeech;
import com.ibm.watson.developer_cloud.android.library.audio.MicrophoneHelper;
import com.ibm.watson.developer_cloud.android.library.audio.MicrophoneInputStream;
import com.ibm.watson.developer_cloud.android.library.audio.StreamPlayer;
import com.ibm.watson.developer_cloud.android.library.audio.utils.ContentType;
import com.ibm.watson.developer_cloud.speech_to_text.v1.SpeechToText;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.RecognizeOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechResults;
import com.ibm.watson.developer_cloud.speech_to_text.v1.websocket.BaseRecognizeCallback;
import com.ibm.watson.developer_cloud.text_to_speech.v1.model.Voice;

public class Questions extends AppCompatActivity {

    SharedPreferences shared_quizDetails;
    SharedPreferences.Editor edit_quizDetails;
    Toolbar toolbar;

    static ImageView backward, jumpTo, forward, speak;

    static int totalTime;
    long currentTime, timeInMillis;

    static Dialog jumpToDialog, listeningDialog;
    GridView gridview;

    TextView myCounter;

    List <JumpTo> jumpToList = new ArrayList <JumpTo>();

    private SpeechToText speechService;
    private TextToSpeech textService;


    private MicrophoneHelper microphoneHelper;
    String speechResult;
    private MicrophoneInputStream capture;
    private boolean listening = false, isPlaying = false, replay = false, touchAnswer = false;

    private StreamPlayer player = new StreamPlayer();
    InputStream voiceStream;

    AppCompatButton cancelListen;


    static ArrayList <String> statementsArray, optionAarray, optionBarray, optionCarray, optionDarray, correctOptionArray, answerStatus, selected;
    static String correctOption = "E", currentselected = "E";
    static TextView questionNumber, optionAtext, optionBtext, optionCtext, optionDtext, statementText;
    static int numberOfQuestions, currentQuestion = 1, marks = 0;
    static LinearLayout optionALinear, optionBLinear, optionCLinear, optionDLinear;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questions);

        toolbar = findViewById(R.id.toolbarQuestion);
        backward = findViewById(R.id.backwardImage);
        jumpTo = findViewById(R.id.jumptoimage);
        forward = findViewById(R.id.forwardImage);
        myCounter = findViewById(R.id.count_down);
        speak = findViewById(R.id.speak);

        questionNumber = findViewById(R.id.quesNumber);
        statementText = findViewById(R.id.statementText);

        optionAtext = findViewById(R.id.optionAtext);
        optionBtext = findViewById(R.id.optionBtext);
        optionCtext = findViewById(R.id.optionCtext);
        optionDtext = findViewById(R.id.optionDtext);

        optionALinear = findViewById(R.id.optionALinear);
        optionBLinear = findViewById(R.id.optionBlinear);
        optionCLinear = findViewById(R.id.optionClinear);
        optionDLinear = findViewById(R.id.optionDlinear);

        microphoneHelper = new MicrophoneHelper(this);
        speechService = initSpeechToTextService();
        textService = initTextToSpeechService();

        jumpToDialog = new Dialog(Questions.this);
        listeningDialog = new Dialog(Questions.this);

        shared_quizDetails = getSharedPreferences("QuizDetails", Context.MODE_PRIVATE);
        edit_quizDetails = shared_quizDetails.edit();

        numberOfQuestions = shared_quizDetails.getInt("noOfQues", 0);
        answerStatus = new ArrayList <String>();
        selected = new ArrayList <String>();

        currentQuestion = 1;

        startTimer();

        try {
            answerStatus = (ArrayList <String>) ObjectSerializer.deserialize(shared_quizDetails.getString("answers", ObjectSerializer.serialize(new ArrayList <String>())));
            selected = (ArrayList <String>) ObjectSerializer.deserialize(shared_quizDetails.getString("selected", ObjectSerializer.serialize(new ArrayList <String>())));

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        if (answerStatus.size() == 0) {
            for (int i = 0; i < numberOfQuestions; i++) {
                answerStatus.add("pending");
            }
        }

        if (selected.size() == 0) {
            for (int i = 0; i < numberOfQuestions; i++) {
                selected.add("E");
            }
        }

       setInitialQuestion();

        optionALinear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isPlaying)
                {
                    touchAnswer = true;
                    player.interrupt();
                }
                optionAselected();
            }
        });

        optionBLinear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isPlaying)
                {
                    touchAnswer = true;
                    player.interrupt();
                }
                optionBselected();

            }
        });

        optionCLinear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isPlaying)
                {
                    touchAnswer = true;
                    player.interrupt();
                }
                optionCselected();
            }
        });

        optionDLinear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isPlaying)
                {
                    touchAnswer = true;
                    player.interrupt();
                }
               optionDselected();
            }
        });

        backward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isPlaying)
                {
                    touchAnswer = true;
                    player.interrupt();
                }
               forwardSelected();
            }
        });

        jumpTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isPlaying)
                {
                    player.interrupt();
                }
                initiateJumpDialog();
            }
        });


        speak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isPlaying)
                {
                    isPlaying = false;
                    touchAnswer = true;
                    player.interrupt();
                    speak.setColorFilter(ContextCompat.getColor(Questions.this, R.color.colorWhite), android.graphics.PorterDuff.Mode.MULTIPLY);

                }
                else
                {
                    speak.setColorFilter(ContextCompat.getColor(Questions.this, R.color.colorAccent), android.graphics.PorterDuff.Mode.MULTIPLY);

                if(replay)
                {
                    touchAnswer = false;
                    new SynthesisTask().execute(statementText.getText().toString().trim()
                            +"Your options are on your screen");
                }
                else {
                    touchAnswer = false;
                    isPlaying = true;
                    new PlayTask().execute();
                }
              }
            }
        });

        overridePendingTransition(0, 0);

    }

    @Override
    protected void onPause() {
        player.interrupt();
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        if(isPlaying)
        {
            touchAnswer = true;
            player.interrupt();
        }
        correctOption = correctOptionArray.get(currentQuestion - 1);
        if (currentQuestion > 1) {

            currentQuestion--;
            questionNumber.setText("Question " + currentQuestion);
            statementText.setText(statementsArray.get(currentQuestion - 1));
            optionAtext.setText(optionAarray.get(currentQuestion - 1));
            optionBtext.setText(optionBarray.get(currentQuestion - 1));
            optionCtext.setText(optionCarray.get(currentQuestion - 1));
            optionDtext.setText(optionDarray.get(currentQuestion - 1));

            new SynthesisTask().execute(statementText.getText().toString().trim()
                    +"Your options are on your screen");

            if (!(answerStatus.get(currentQuestion - 1).equals("pending"))) {
                currentselected = selected.get(currentQuestion - 1);

                switch (currentselected) {
                    case "A":
                        optionALinear.setBackgroundColor(getResources().getColor(R.color.optionAred));
                        optionAtext.setTextColor(getResources().getColor(R.color.colorWhite));
                        optionBLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionBtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionCLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionCtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionDLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionDtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        break;
                    case "B":
                        optionALinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionAtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionBLinear.setBackgroundColor(getResources().getColor(R.color.optionBorange));
                        optionBtext.setTextColor(getResources().getColor(R.color.colorWhite));
                        optionCLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionCtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionDLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionDtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        break;
                    case "C":
                        optionBLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionBtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionCLinear.setBackgroundColor(getResources().getColor(R.color.optionCblue));
                        optionCtext.setTextColor(getResources().getColor(R.color.colorWhite));
                        optionALinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionAtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionDLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionDtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        break;
                    case "D":
                        optionBLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionBtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionCLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionCtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionALinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionAtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionDLinear.setBackgroundColor(getResources().getColor(R.color.optionDgreen));
                        optionDtext.setTextColor(getResources().getColor(R.color.colorWhite));
                }
            } else {
                optionBLinear.setBackgroundColor(getResources().getColor(R.color.optionBorange));
                optionBtext.setTextColor(getResources().getColor(R.color.colorWhite));
                optionCLinear.setBackgroundColor(getResources().getColor(R.color.optionCblue));
                optionCtext.setTextColor(getResources().getColor(R.color.colorWhite));
                optionALinear.setBackgroundColor(getResources().getColor(R.color.optionAred));
                optionAtext.setTextColor(getResources().getColor(R.color.colorWhite));
                optionDLinear.setBackgroundColor(getResources().getColor(R.color.optionDgreen));
                optionDtext.setTextColor(getResources().getColor(R.color.colorWhite));
            }

        }

    }

    public static void setCurrentQuestion(int current, Context context) {
        jumpToDialog.dismiss();

        currentQuestion = current;
        correctOption = correctOptionArray.get(currentQuestion - 1);

        questionNumber.setText("Question " + currentQuestion);

        statementText.setText(statementsArray.get(currentQuestion - 1));
        optionAtext.setText(optionAarray.get(currentQuestion - 1));
        optionBtext.setText(optionBarray.get(currentQuestion - 1));
        optionCtext.setText(optionCarray.get(currentQuestion - 1));
        optionDtext.setText(optionDarray.get(currentQuestion - 1));


        if (!(answerStatus.get(currentQuestion - 1).equals("pending"))) {
            currentselected = selected.get(currentQuestion - 1);

            switch (currentselected) {
                case "A":
                    optionALinear.setBackgroundColor(context.getResources().getColor(R.color.optionAred));
                    optionAtext.setTextColor(context.getResources().getColor(R.color.colorWhite));
                    optionBLinear.setBackgroundColor(context.getResources().getColor(R.color.colorWhite));
                    optionBtext.setTextColor(context.getResources().getColor(R.color.colorBlack));
                    optionCLinear.setBackgroundColor(context.getResources().getColor(R.color.colorWhite));
                    optionCtext.setTextColor(context.getResources().getColor(R.color.colorBlack));
                    optionDLinear.setBackgroundColor(context.getResources().getColor(R.color.colorWhite));
                    optionDtext.setTextColor(context.getResources().getColor(R.color.colorBlack));
                    break;
                case "B":
                    optionALinear.setBackgroundColor(context.getResources().getColor(R.color.colorWhite));
                    optionAtext.setTextColor(context.getResources().getColor(R.color.colorBlack));
                    optionBLinear.setBackgroundColor(context.getResources().getColor(R.color.optionBorange));
                    optionBtext.setTextColor(context.getResources().getColor(R.color.colorWhite));
                    optionCLinear.setBackgroundColor(context.getResources().getColor(R.color.colorWhite));
                    optionCtext.setTextColor(context.getResources().getColor(R.color.colorBlack));
                    optionDLinear.setBackgroundColor(context.getResources().getColor(R.color.colorWhite));
                    optionDtext.setTextColor(context.getResources().getColor(R.color.colorBlack));
                    break;
                case "C":
                    optionBLinear.setBackgroundColor(context.getResources().getColor(R.color.colorWhite));
                    optionBtext.setTextColor(context.getResources().getColor(R.color.colorBlack));
                    optionCLinear.setBackgroundColor(context.getResources().getColor(R.color.optionCblue));
                    optionCtext.setTextColor(context.getResources().getColor(R.color.colorWhite));
                    optionALinear.setBackgroundColor(context.getResources().getColor(R.color.colorWhite));
                    optionAtext.setTextColor(context.getResources().getColor(R.color.colorBlack));
                    optionDLinear.setBackgroundColor(context.getResources().getColor(R.color.colorWhite));
                    optionDtext.setTextColor(context.getResources().getColor(R.color.colorBlack));
                    break;
                case "D":
                    optionBLinear.setBackgroundColor(context.getResources().getColor(R.color.colorWhite));
                    optionBtext.setTextColor(context.getResources().getColor(R.color.colorBlack));
                    optionCLinear.setBackgroundColor(context.getResources().getColor(R.color.colorWhite));
                    optionCtext.setTextColor(context.getResources().getColor(R.color.colorBlack));
                    optionALinear.setBackgroundColor(context.getResources().getColor(R.color.colorWhite));
                    optionAtext.setTextColor(context.getResources().getColor(R.color.colorBlack));
                    optionDLinear.setBackgroundColor(context.getResources().getColor(R.color.optionDgreen));
                    optionDtext.setTextColor(context.getResources().getColor(R.color.colorWhite));
            }
        } else {
            optionBLinear.setBackgroundColor(context.getResources().getColor(R.color.optionBorange));
            optionBtext.setTextColor(context.getResources().getColor(R.color.colorWhite));
            optionCLinear.setBackgroundColor(context.getResources().getColor(R.color.optionCblue));
            optionCtext.setTextColor(context.getResources().getColor(R.color.colorWhite));
            optionALinear.setBackgroundColor(context.getResources().getColor(R.color.optionAred));
            optionAtext.setTextColor(context.getResources().getColor(R.color.colorWhite));
            optionDLinear.setBackgroundColor(context.getResources().getColor(R.color.optionDgreen));
            optionDtext.setTextColor(context.getResources().getColor(R.color.colorWhite));
        }
    }




    public void optionAselected()
    {

        correctOption = correctOptionArray.get(currentQuestion - 1);

        if (currentQuestion < numberOfQuestions) {

            currentQuestion++;
            questionNumber.setText("Question " + currentQuestion);

            statementText.setText(statementsArray.get(currentQuestion - 1));
            optionAtext.setText(optionAarray.get(currentQuestion - 1));
            optionBtext.setText(optionBarray.get(currentQuestion - 1));
            optionCtext.setText(optionCarray.get(currentQuestion - 1));
            optionDtext.setText(optionDarray.get(currentQuestion - 1));
            selected.set(currentQuestion - 2, "A");

            if(isPlaying) {
                new SynthesisTask().execute(statementText.getText().toString().trim()
                        + "Your options are on your screen");
            }



            if (!(answerStatus.get(currentQuestion - 1).equals("pending"))) {
                currentselected = selected.get(currentQuestion - 1);

                switch (currentselected) {
                    case "A":
                        optionALinear.setBackgroundColor(getResources().getColor(R.color.optionAred));
                        optionAtext.setTextColor(getResources().getColor(R.color.colorWhite));
                        optionBLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionBtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionCLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionCtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionDLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionDtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        break;
                    case "B":
                        optionALinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionAtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionBLinear.setBackgroundColor(getResources().getColor(R.color.optionBorange));
                        optionBtext.setTextColor(getResources().getColor(R.color.colorWhite));
                        optionCLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionCtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionDLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionDtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        break;
                    case "C":
                        optionBLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionBtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionCLinear.setBackgroundColor(getResources().getColor(R.color.optionCblue));
                        optionCtext.setTextColor(getResources().getColor(R.color.colorWhite));
                        optionALinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionAtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionDLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionDtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        break;
                    case "D":
                        optionBLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionBtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionCLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionCtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionALinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionAtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionDLinear.setBackgroundColor(getResources().getColor(R.color.optionDgreen));
                        optionDtext.setTextColor(getResources().getColor(R.color.colorWhite));
                }
            } else {
                optionBLinear.setBackgroundColor(getResources().getColor(R.color.optionBorange));
                optionBtext.setTextColor(getResources().getColor(R.color.colorWhite));
                optionCLinear.setBackgroundColor(getResources().getColor(R.color.optionCblue));
                optionCtext.setTextColor(getResources().getColor(R.color.colorWhite));
                optionALinear.setBackgroundColor(getResources().getColor(R.color.optionAred));
                optionAtext.setTextColor(getResources().getColor(R.color.colorWhite));
                optionDLinear.setBackgroundColor(getResources().getColor(R.color.optionDgreen));
                optionDtext.setTextColor(getResources().getColor(R.color.colorWhite));
            }

            if (correctOption.equals("A")) {
                marks++;
                answerStatus.set(currentQuestion - 2, "correct");
                System.out.print(answerStatus.get(currentQuestion - 2));
            } else {
                answerStatus.set(currentQuestion - 2, "incorrect");
                System.out.print(answerStatus.get(currentQuestion - 2));

            }
        } else {
            selected.set(currentQuestion - 1, "A");

            if (!(answerStatus.get(currentQuestion - 1).equals("pending"))) {
                currentselected = selected.get(currentQuestion - 1);

                switch (currentselected) {
                    case "A":
                        optionALinear.setBackgroundColor(getResources().getColor(R.color.optionAred));
                        optionAtext.setTextColor(getResources().getColor(R.color.colorWhite));
                        optionBLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionBtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionCLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionCtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionDLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionDtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        break;
                    case "B":
                        optionALinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionAtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionBLinear.setBackgroundColor(getResources().getColor(R.color.optionBorange));
                        optionBtext.setTextColor(getResources().getColor(R.color.colorWhite));
                        optionCLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionCtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionDLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionDtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        break;
                    case "C":
                        optionBLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionBtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionCLinear.setBackgroundColor(getResources().getColor(R.color.optionCblue));
                        optionCtext.setTextColor(getResources().getColor(R.color.colorWhite));
                        optionALinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionAtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionDLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionDtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        break;
                    case "D":
                        optionBLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionBtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionCLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionCtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionALinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionAtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionDLinear.setBackgroundColor(getResources().getColor(R.color.optionDgreen));
                        optionDtext.setTextColor(getResources().getColor(R.color.colorWhite));
                }
            } else {
                optionBLinear.setBackgroundColor(getResources().getColor(R.color.optionBorange));
                optionBtext.setTextColor(getResources().getColor(R.color.colorWhite));
                optionCLinear.setBackgroundColor(getResources().getColor(R.color.optionCblue));
                optionCtext.setTextColor(getResources().getColor(R.color.colorWhite));
                optionALinear.setBackgroundColor(getResources().getColor(R.color.optionAred));
                optionAtext.setTextColor(getResources().getColor(R.color.colorWhite));
                optionDLinear.setBackgroundColor(getResources().getColor(R.color.optionDgreen));
                optionDtext.setTextColor(getResources().getColor(R.color.colorWhite));
            }

            if (correctOption.equals("A")) {
                marks++;
                answerStatus.set(currentQuestion - 1, "correct");
                Toast.makeText(Questions.this, answerStatus.get(currentQuestion - 1) + "kuch", Toast.LENGTH_SHORT).show();

            } else {
                answerStatus.set(currentQuestion - 1, "incorrect");
                System.out.print(answerStatus.get(currentQuestion - 1) + "kuch");

            }

            try {
                edit_quizDetails.putString("answers", ObjectSerializer.serialize(answerStatus));
                edit_quizDetails.putString("selected", ObjectSerializer.serialize(selected));
                edit_quizDetails.putLong("currentTime",currentTime);
                edit_quizDetails.apply();

                Intent i = new Intent(Questions.this, Submit.class);
                startActivity(i);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public void optionBselected()
    {

        correctOption = correctOptionArray.get(currentQuestion - 1);

        if (currentQuestion < numberOfQuestions) {
            currentQuestion++;
            questionNumber.setText("Question " + currentQuestion);

            statementText.setText(statementsArray.get(currentQuestion - 1));
            optionAtext.setText(optionAarray.get(currentQuestion - 1));
            optionBtext.setText(optionBarray.get(currentQuestion - 1));
            optionCtext.setText(optionCarray.get(currentQuestion - 1));
            optionDtext.setText(optionDarray.get(currentQuestion - 1));
            selected.set(currentQuestion - 2, "B");


            if(isPlaying) {
                new SynthesisTask().execute(statementText.getText().toString().trim()
                        + "Your options are on your screen");
            }


            if (!(answerStatus.get(currentQuestion - 1).equals("pending"))) {
                currentselected = selected.get(currentQuestion - 1);

                switch (currentselected) {
                    case "A":
                        optionALinear.setBackgroundColor(getResources().getColor(R.color.optionAred));
                        optionAtext.setTextColor(getResources().getColor(R.color.colorWhite));
                        optionBLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionBtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionCLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionCtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionDLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionDtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        break;
                    case "B":
                        optionALinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionAtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionBLinear.setBackgroundColor(getResources().getColor(R.color.optionBorange));
                        optionBtext.setTextColor(getResources().getColor(R.color.colorWhite));
                        optionCLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionCtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionDLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionDtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        break;
                    case "C":
                        optionBLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionBtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionCLinear.setBackgroundColor(getResources().getColor(R.color.optionCblue));
                        optionCtext.setTextColor(getResources().getColor(R.color.colorWhite));
                        optionALinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionAtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionDLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionDtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        break;
                    case "D":
                        optionBLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionBtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionCLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionCtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionALinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionAtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionDLinear.setBackgroundColor(getResources().getColor(R.color.optionDgreen));
                        optionDtext.setTextColor(getResources().getColor(R.color.colorWhite));
                }
            } else {
                optionBLinear.setBackgroundColor(getResources().getColor(R.color.optionBorange));
                optionBtext.setTextColor(getResources().getColor(R.color.colorWhite));
                optionCLinear.setBackgroundColor(getResources().getColor(R.color.optionCblue));
                optionCtext.setTextColor(getResources().getColor(R.color.colorWhite));
                optionALinear.setBackgroundColor(getResources().getColor(R.color.optionAred));
                optionAtext.setTextColor(getResources().getColor(R.color.colorWhite));
                optionDLinear.setBackgroundColor(getResources().getColor(R.color.optionDgreen));
                optionDtext.setTextColor(getResources().getColor(R.color.colorWhite));
            }

            if (correctOption.equals("B")) {
                marks++;
                answerStatus.set(currentQuestion - 2, "correct");
                System.out.print(answerStatus.get(currentQuestion - 2));

            } else {
                answerStatus.set(currentQuestion - 2, "incorrect");
                System.out.print(answerStatus.get(currentQuestion - 2));

            }
        } else {
            selected.set(currentQuestion - 1, "B");

            if (!(answerStatus.get(currentQuestion - 1).equals("pending"))) {
                currentselected = selected.get(currentQuestion - 1);

                switch (currentselected) {
                    case "A":
                        optionALinear.setBackgroundColor(getResources().getColor(R.color.optionAred));
                        optionAtext.setTextColor(getResources().getColor(R.color.colorWhite));
                        optionBLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionBtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionCLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionCtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionDLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionDtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        break;
                    case "B":
                        optionALinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionAtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionBLinear.setBackgroundColor(getResources().getColor(R.color.optionBorange));
                        optionBtext.setTextColor(getResources().getColor(R.color.colorWhite));
                        optionCLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionCtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionDLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionDtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        break;
                    case "C":
                        optionBLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionBtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionCLinear.setBackgroundColor(getResources().getColor(R.color.optionCblue));
                        optionCtext.setTextColor(getResources().getColor(R.color.colorWhite));
                        optionALinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionAtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionDLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionDtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        break;
                    case "D":
                        optionBLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionBtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionCLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionCtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionALinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionAtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionDLinear.setBackgroundColor(getResources().getColor(R.color.optionDgreen));
                        optionDtext.setTextColor(getResources().getColor(R.color.colorWhite));
                }
            } else {
                optionBLinear.setBackgroundColor(getResources().getColor(R.color.optionBorange));
                optionBtext.setTextColor(getResources().getColor(R.color.colorWhite));
                optionCLinear.setBackgroundColor(getResources().getColor(R.color.optionCblue));
                optionCtext.setTextColor(getResources().getColor(R.color.colorWhite));
                optionALinear.setBackgroundColor(getResources().getColor(R.color.optionAred));
                optionAtext.setTextColor(getResources().getColor(R.color.colorWhite));
                optionDLinear.setBackgroundColor(getResources().getColor(R.color.optionDgreen));
                optionDtext.setTextColor(getResources().getColor(R.color.colorWhite));
            }
            if (correctOption.equals("B")) {
                marks++;
                answerStatus.set(currentQuestion - 1, "correct");
                System.out.print(answerStatus.get(currentQuestion - 1));

            } else {
                answerStatus.set(currentQuestion - 1, "incorrect");
                System.out.print(answerStatus.get(currentQuestion - 1));

            }

            try {
                edit_quizDetails.putString("answers", ObjectSerializer.serialize(answerStatus));
                edit_quizDetails.putString("selected", ObjectSerializer.serialize(selected));
                edit_quizDetails.putLong("currentTime",currentTime);
                edit_quizDetails.apply();

                Intent i = new Intent(Questions.this, Submit.class);
                startActivity(i);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public void optionCselected()
    {
        correctOption = correctOptionArray.get(currentQuestion - 1);

        if (currentQuestion < numberOfQuestions) {
            currentQuestion++;
            questionNumber.setText("Question " + currentQuestion);

            statementText.setText(statementsArray.get(currentQuestion - 1));
            optionAtext.setText(optionAarray.get(currentQuestion - 1));
            optionBtext.setText(optionBarray.get(currentQuestion - 1));
            optionCtext.setText(optionCarray.get(currentQuestion - 1));
            optionDtext.setText(optionDarray.get(currentQuestion - 1));
            selected.set(currentQuestion - 2, "C");

            if(isPlaying) {
                new SynthesisTask().execute(statementText.getText().toString().trim()
                        + "Your options are on your screen");
            }


            if (!(answerStatus.get(currentQuestion - 1).equals("pending"))) {
                currentselected = selected.get(currentQuestion - 1);

                switch (currentselected) {
                    case "A":
                        optionALinear.setBackgroundColor(getResources().getColor(R.color.optionAred));
                        optionAtext.setTextColor(getResources().getColor(R.color.colorWhite));
                        optionBLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionBtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionCLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionCtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionDLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionDtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        break;
                    case "B":
                        optionALinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionAtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionBLinear.setBackgroundColor(getResources().getColor(R.color.optionBorange));
                        optionBtext.setTextColor(getResources().getColor(R.color.colorWhite));
                        optionCLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionCtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionDLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionDtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        break;
                    case "C":
                        optionBLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionBtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionCLinear.setBackgroundColor(getResources().getColor(R.color.optionCblue));
                        optionCtext.setTextColor(getResources().getColor(R.color.colorWhite));
                        optionALinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionAtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionDLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionDtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        break;
                    case "D":
                        optionBLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionBtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionCLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionCtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionALinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionAtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionDLinear.setBackgroundColor(getResources().getColor(R.color.optionDgreen));
                        optionDtext.setTextColor(getResources().getColor(R.color.colorWhite));
                }
            } else {
                optionBLinear.setBackgroundColor(getResources().getColor(R.color.optionBorange));
                optionBtext.setTextColor(getResources().getColor(R.color.colorWhite));
                optionCLinear.setBackgroundColor(getResources().getColor(R.color.optionCblue));
                optionCtext.setTextColor(getResources().getColor(R.color.colorWhite));
                optionALinear.setBackgroundColor(getResources().getColor(R.color.optionAred));
                optionAtext.setTextColor(getResources().getColor(R.color.colorWhite));
                optionDLinear.setBackgroundColor(getResources().getColor(R.color.optionDgreen));
                optionDtext.setTextColor(getResources().getColor(R.color.colorWhite));
            }

            if (correctOption.equals("C")) {
                marks++;
                answerStatus.set(currentQuestion - 2, "correct");
                System.out.print(answerStatus.get(currentQuestion - 2));

            } else {
                answerStatus.set(currentQuestion - 2, "incorrect");
                System.out.print(answerStatus.get(currentQuestion - 2));

            }
        } else {
            selected.set(currentQuestion - 1, "C");

            if (!(answerStatus.get(currentQuestion - 1).equals("pending"))) {
                currentselected = selected.get(currentQuestion - 1);

                switch (currentselected) {
                    case "A":
                        optionALinear.setBackgroundColor(getResources().getColor(R.color.optionAred));
                        optionAtext.setTextColor(getResources().getColor(R.color.colorWhite));
                        optionBLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionBtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionCLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionCtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionDLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionDtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        break;
                    case "B":
                        optionALinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionAtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionBLinear.setBackgroundColor(getResources().getColor(R.color.optionBorange));
                        optionBtext.setTextColor(getResources().getColor(R.color.colorWhite));
                        optionCLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionCtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionDLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionDtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        break;
                    case "C":
                        optionBLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionBtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionCLinear.setBackgroundColor(getResources().getColor(R.color.optionCblue));
                        optionCtext.setTextColor(getResources().getColor(R.color.colorWhite));
                        optionALinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionAtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionDLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionDtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        break;
                    case "D":
                        optionBLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionBtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionCLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionCtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionALinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionAtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionDLinear.setBackgroundColor(getResources().getColor(R.color.optionDgreen));
                        optionDtext.setTextColor(getResources().getColor(R.color.colorWhite));
                }
            } else {
                optionBLinear.setBackgroundColor(getResources().getColor(R.color.optionBorange));
                optionBtext.setTextColor(getResources().getColor(R.color.colorWhite));
                optionCLinear.setBackgroundColor(getResources().getColor(R.color.optionCblue));
                optionCtext.setTextColor(getResources().getColor(R.color.colorWhite));
                optionALinear.setBackgroundColor(getResources().getColor(R.color.optionAred));
                optionAtext.setTextColor(getResources().getColor(R.color.colorWhite));
                optionDLinear.setBackgroundColor(getResources().getColor(R.color.optionDgreen));
                optionDtext.setTextColor(getResources().getColor(R.color.colorWhite));
            }
            if (correctOption.equals("C")) {
                marks++;
                answerStatus.set(currentQuestion - 1, "correct");
                System.out.print(answerStatus.get(currentQuestion - 1));

            } else {
                answerStatus.set(currentQuestion - 1, "incorrect");
                System.out.print(answerStatus.get(currentQuestion - 1));

            }

            try {
                edit_quizDetails.putString("answers", ObjectSerializer.serialize(answerStatus));
                edit_quizDetails.putString("selected", ObjectSerializer.serialize(selected));
                edit_quizDetails.putLong("currentTime",currentTime);
                edit_quizDetails.apply();

                Intent i = new Intent(Questions.this, Submit.class);
                startActivity(i);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public void optionDselected()
    {
        correctOption = correctOptionArray.get(currentQuestion - 1);

        if (currentQuestion < numberOfQuestions) {
            currentQuestion++;
            questionNumber.setText("Question " + currentQuestion);



            statementText.setText(statementsArray.get(currentQuestion - 1));
            optionAtext.setText(optionAarray.get(currentQuestion - 1));
            optionBtext.setText(optionBarray.get(currentQuestion - 1));
            optionCtext.setText(optionCarray.get(currentQuestion - 1));
            optionDtext.setText(optionDarray.get(currentQuestion - 1));
            selected.set(currentQuestion - 2, "D");


            if(isPlaying) {
                new SynthesisTask().execute(statementText.getText().toString().trim()
                        + "Your options are on your screen");
            }


            if (!(answerStatus.get(currentQuestion - 1).equals("pending"))) {
                currentselected = selected.get(currentQuestion - 1);

                switch (currentselected) {
                    case "A":
                        optionALinear.setBackgroundColor(getResources().getColor(R.color.optionAred));
                        optionAtext.setTextColor(getResources().getColor(R.color.colorWhite));
                        optionBLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionBtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionCLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionCtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionDLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionDtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        break;
                    case "B":
                        optionALinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionAtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionBLinear.setBackgroundColor(getResources().getColor(R.color.optionBorange));
                        optionBtext.setTextColor(getResources().getColor(R.color.colorWhite));
                        optionCLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionCtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionDLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionDtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        break;
                    case "C":
                        optionBLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionBtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionCLinear.setBackgroundColor(getResources().getColor(R.color.optionCblue));
                        optionCtext.setTextColor(getResources().getColor(R.color.colorWhite));
                        optionALinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionAtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionDLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionDtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        break;
                    case "D":
                        optionBLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionBtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionCLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionCtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionALinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionAtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionDLinear.setBackgroundColor(getResources().getColor(R.color.optionDgreen));
                        optionDtext.setTextColor(getResources().getColor(R.color.colorWhite));
                }
            } else {
                optionBLinear.setBackgroundColor(getResources().getColor(R.color.optionBorange));
                optionBtext.setTextColor(getResources().getColor(R.color.colorWhite));
                optionCLinear.setBackgroundColor(getResources().getColor(R.color.optionCblue));
                optionCtext.setTextColor(getResources().getColor(R.color.colorWhite));
                optionALinear.setBackgroundColor(getResources().getColor(R.color.optionAred));
                optionAtext.setTextColor(getResources().getColor(R.color.colorWhite));
                optionDLinear.setBackgroundColor(getResources().getColor(R.color.optionDgreen));
                optionDtext.setTextColor(getResources().getColor(R.color.colorWhite));
            }


            if (correctOption.equals("D")) {
                marks++;
                answerStatus.set(currentQuestion - 2, "correct");
                System.out.print(answerStatus.get(currentQuestion - 2));

            } else {
                answerStatus.set(currentQuestion - 2, "incorrect");
                System.out.print(answerStatus.get(currentQuestion - 2));

            }
        } else {
            selected.set(currentQuestion - 1, "D");

            if (!(answerStatus.get(currentQuestion - 1).equals("pending"))) {
                currentselected = selected.get(currentQuestion - 1);

                switch (currentselected) {
                    case "A":
                        optionALinear.setBackgroundColor(getResources().getColor(R.color.optionAred));
                        optionAtext.setTextColor(getResources().getColor(R.color.colorWhite));
                        optionBLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionBtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionCLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionCtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionDLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionDtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        break;
                    case "B":
                        optionALinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionAtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionBLinear.setBackgroundColor(getResources().getColor(R.color.optionBorange));
                        optionBtext.setTextColor(getResources().getColor(R.color.colorWhite));
                        optionCLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionCtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionDLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionDtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        break;
                    case "C":
                        optionBLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionBtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionCLinear.setBackgroundColor(getResources().getColor(R.color.optionCblue));
                        optionCtext.setTextColor(getResources().getColor(R.color.colorWhite));
                        optionALinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionAtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionDLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionDtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        break;
                    case "D":
                        optionBLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionBtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionCLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionCtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionALinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionAtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionDLinear.setBackgroundColor(getResources().getColor(R.color.optionDgreen));
                        optionDtext.setTextColor(getResources().getColor(R.color.colorWhite));
                }
            } else {
                optionBLinear.setBackgroundColor(getResources().getColor(R.color.optionBorange));
                optionBtext.setTextColor(getResources().getColor(R.color.colorWhite));
                optionCLinear.setBackgroundColor(getResources().getColor(R.color.optionCblue));
                optionCtext.setTextColor(getResources().getColor(R.color.colorWhite));
                optionALinear.setBackgroundColor(getResources().getColor(R.color.optionAred));
                optionAtext.setTextColor(getResources().getColor(R.color.colorWhite));
                optionDLinear.setBackgroundColor(getResources().getColor(R.color.optionDgreen));
                optionDtext.setTextColor(getResources().getColor(R.color.colorWhite));
            }
            if (correctOption.equals("D")) {
                marks++;
                answerStatus.set(currentQuestion - 1, "correct");
                System.out.print(answerStatus.get(currentQuestion - 1));

            } else {
                answerStatus.set(currentQuestion - 1, "incorrect");
                System.out.print(answerStatus.get(currentQuestion - 1));

            }

            try {
                edit_quizDetails.putString("answers", ObjectSerializer.serialize(answerStatus));
                edit_quizDetails.putString("selected", ObjectSerializer.serialize(selected));
                edit_quizDetails.putLong("currentTime",currentTime);
                edit_quizDetails.apply();

                Intent i = new Intent(Questions.this, Submit.class);
                startActivity(i);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private SpeechToText initSpeechToTextService() {
        SpeechToText service = new SpeechToText();
        String username =  getResources().getString(R.string.speech_text_username);
        String password = getResources().getString(R.string.speech_text_password);
        service.setUsernameAndPassword(username, password);
        service.setEndPoint(getResources().getString(R.string.speech_text_url));
        return service;
    }

    private void promptSpeechInput() {

        if (!listening) {
            capture = microphoneHelper.getInputStream(true);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        speechService.recognizeUsingWebSocket(capture, getRecognizeOptions(),
                                new MicrophoneRecognizeDelegate());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    private class MicrophoneRecognizeDelegate extends BaseRecognizeCallback {

        @Override
        public void onTranscription(SpeechResults speechResults) {
            System.out.println(speechResults);
            if (speechResults.getResults() != null && !speechResults.getResults().isEmpty()) {
                speechResult = speechResults.getResults().get(0).getAlternatives().get(0).getTranscript();
                microphoneHelper.closeInputStream();

            }
        }

        @Override
        public void onError(Exception e) {


            Handler dismissHandler = new Handler(Looper.getMainLooper());

            Runnable dismissRunnable = new Runnable() {
                @Override
                public void run() {

                    listeningDialog.dismiss();


                } // This is your code
            };
            dismissHandler.post(dismissRunnable);

            e.printStackTrace();

        }

        @Override
        public void onDisconnected() {


            Handler dismissHandler = new Handler(Looper.getMainLooper());

            Runnable dismissRunnable = new Runnable() {
                @Override
                public void run() {

                    listeningDialog.dismiss();



                } // This is your code
            };
            dismissHandler.post(dismissRunnable);


            System.out.println(speechResult);




            if (speechResult != null) {

                speechResult = speechResult.replaceAll("[-+.^:,]","").trim();

                System.out.println("This is the output: " + speechResult.toLowerCase());

                if (speechResult.toLowerCase().equals("a") || speechResult.toLowerCase().equals("option a") || speechResult.toLowerCase().equals(optionAtext.getText().toString().trim().toLowerCase())) {
                    Handler mainHandler = new Handler(Looper.getMainLooper());

                    Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {

                            optionAselected();

                        }
                    };
                    mainHandler.post(myRunnable);

                } else if (speechResult.toLowerCase().equals("b") || speechResult.toLowerCase().equals("option b") || speechResult.toLowerCase().equals(optionBtext.getText().toString().trim().toLowerCase())) {

                    Handler mainHandler = new Handler(Looper.getMainLooper());

                    Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {

                            optionBselected();

                        }
                    };
                    mainHandler.post(myRunnable);
                } else if (speechResult.toLowerCase().equals("c") || speechResult.toLowerCase().equals("option c") || speechResult.toLowerCase().equals(optionCtext.getText().toString().trim().toLowerCase())) {
                    Handler mainHandler = new Handler(Looper.getMainLooper());

                    Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {

                            optionCselected();

                        }
                    };
                    mainHandler.post(myRunnable);
                } else if (speechResult.toLowerCase().equals("d") || speechResult.toLowerCase().equals("option d") || speechResult.toLowerCase().equals(optionDtext.getText().toString().trim().toLowerCase())) {
                    Handler mainHandler = new Handler(Looper.getMainLooper());

                    Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {

                            optionDselected();

                        }
                    };
                    mainHandler.post(myRunnable);
                } else {
                    System.out.println("idhar aaya");


                    Handler mainHandler = new Handler(Looper.getMainLooper());

                    Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {

                            Toast.makeText(Questions.this, "Option spoke is not correct, try again!", Toast.LENGTH_SHORT).show();

                        }
                    };
                    mainHandler.post(myRunnable);
                    }

            }

            else
            {
                Handler mainHandler = new Handler(Looper.getMainLooper());

                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run() {

                        Toast.makeText(Questions.this, "Option spoke is not correct, try again!", Toast.LENGTH_SHORT).show();

                    }
                };
                mainHandler.post(myRunnable);


            }
        }
        }


    private RecognizeOptions getRecognizeOptions() {
        return new RecognizeOptions.Builder().contentType(ContentType.OPUS.toString())
                .model("en-US_BroadbandModel").interimResults(true).inactivityTimeout(1).build();
    }


    private TextToSpeech initTextToSpeechService() {
       TextToSpeech service = new TextToSpeech();
        String username = getString(R.string.text_speech_username);
        String password = getString(R.string.text_speech_password);
        service.setUsernameAndPassword(username, password);
        service.setEndPoint(getString(R.string.text_speech_url));
        return service;
    }

    private class SynthesisTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            voiceStream = textService.synthesize(params[0], Voice.EN_LISA).execute();
            return "Did synthesize";
        }

        @Override
        protected void onPostExecute(String s) {

            if (replay)
            {
                isPlaying = true;
                new PlayTask().execute();
            }

            super.onPostExecute(s);
        }
    }

    private class PlayTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            player.playStream(voiceStream);
            return "Played";
        }

        @Override
        protected void onPostExecute(String s) {

            replay = true;

            if(!touchAnswer) {
                promptSpeechInput();

                listeningDialog.setContentView(R.layout.listening_dialog);
                cancelListen = listeningDialog.findViewById(R.id.cancelListen);

                cancelListen.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        microphoneHelper.closeInputStream();
                        listeningDialog.dismiss();
                    }
                });

                listeningDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                listeningDialog.setCancelable(false);

                listeningDialog.show();
            }


            super.onPostExecute(s);
        }

    }


    public void startTimer()
    {
        totalTime = shared_quizDetails.getInt("totalTime", 0);

        if(shared_quizDetails.getLong("currentTime",-1)>0)
        {
            timeInMillis = shared_quizDetails.getLong("currentTime",-1000);
        }
        else {
            timeInMillis = totalTime * 60 * 1000;
        }

        new CountDownTimer(timeInMillis, 1000) {

            @Override
            public void onFinish() {
                // TODO Auto-generated method stub

                if (shared_quizDetails.getInt("timerFlag", 0) ==0) {

                    try {
                        edit_quizDetails.putString("answers", ObjectSerializer.serialize(answerStatus));
                        edit_quizDetails.putString("selected", ObjectSerializer.serialize(selected));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    edit_quizDetails.putLong("currentTime", currentTime);
                    edit_quizDetails.apply();
                    edit_quizDetails.putInt("timerFlag", 1);
                    edit_quizDetails.apply();
                    Intent i = new Intent(Questions.this, Submit.class);
                    startActivity(i);
                    myCounter.setText("Finished!");
                }
            }

            @Override
            public void onTick(long millisUntilFinished) {
                // TODO Auto-generated method stub
                if (shared_quizDetails.getInt("timerFlag", 0) == 0) {

                    currentTime = millisUntilFinished;
                    if (((millisUntilFinished / (1000)) % 60) != 0) {
                        myCounter.setText(String.valueOf((millisUntilFinished / (1000 * 60 * 60))) + "h:" + String.valueOf((millisUntilFinished / (1000 * 60))) + "m:" + String.valueOf(((millisUntilFinished / (1000)) % 60) + "s"));
                    } else if (((millisUntilFinished / (1000)) % 60) == 0 && (millisUntilFinished / (1000)) != 0) {
                        myCounter.setText(String.valueOf((millisUntilFinished / (1000 * 60 * 60))) + "h:" + String.valueOf((millisUntilFinished / (1000 * 60))) + "m:" + String.valueOf(((60))) + "s");
                    }
                }

                else
                {

                    cancel();
                }
            }
        }.start();
    }

    public  void setInitialQuestion()
    {
        questionNumber.setText("Question " + currentQuestion);

        try {
            statementsArray = (ArrayList <String>) ObjectSerializer.deserialize(shared_quizDetails.getString("Statements", ObjectSerializer.serialize(new ArrayList <String>())));
            optionAarray = (ArrayList <String>) ObjectSerializer.deserialize(shared_quizDetails.getString("optionA", ObjectSerializer.serialize(new ArrayList <String>())));
            optionBarray = (ArrayList <String>) ObjectSerializer.deserialize(shared_quizDetails.getString("optionB", ObjectSerializer.serialize(new ArrayList <String>())));
            optionCarray = (ArrayList <String>) ObjectSerializer.deserialize(shared_quizDetails.getString("optionC", ObjectSerializer.serialize(new ArrayList <String>())));
            optionDarray = (ArrayList <String>) ObjectSerializer.deserialize(shared_quizDetails.getString("optionD", ObjectSerializer.serialize(new ArrayList <String>())));
            correctOptionArray = (ArrayList <String>) ObjectSerializer.deserialize(shared_quizDetails.getString("correct", ObjectSerializer.serialize(new ArrayList <String>())));

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        statementText.setText(statementsArray.get(currentQuestion - 1));
        optionAtext.setText(optionAarray.get(currentQuestion - 1));
        optionBtext.setText(optionBarray.get(currentQuestion - 1));
        optionCtext.setText(optionCarray.get(currentQuestion - 1));
        optionDtext.setText(optionDarray.get(currentQuestion - 1));
        correctOption = correctOptionArray.get(currentQuestion - 1);

        new SynthesisTask().execute(statementText.getText().toString().trim()
                +"Your options are on your screen");

    }

    public void forwardSelected(){

        correctOption = correctOptionArray.get(currentQuestion - 1);

        if (currentQuestion < numberOfQuestions) {

            currentQuestion++;
            questionNumber.setText("Question " + currentQuestion);

            statementText.setText(statementsArray.get(currentQuestion - 1));
            optionAtext.setText(optionAarray.get(currentQuestion - 1));
            optionBtext.setText(optionBarray.get(currentQuestion - 1));
            optionCtext.setText(optionCarray.get(currentQuestion - 1));
            optionDtext.setText(optionDarray.get(currentQuestion - 1));

            new SynthesisTask().execute(statementText.getText().toString().trim()
                    +"Your options are on your screen");

            if (!(answerStatus.get(currentQuestion - 1).equals("pending"))) {
                currentselected = selected.get(currentQuestion - 1);

                switch (currentselected) {
                    case "A":
                        optionALinear.setBackgroundColor(getResources().getColor(R.color.optionAred));
                        optionAtext.setTextColor(getResources().getColor(R.color.colorWhite));
                        optionBLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionBtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionCLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionCtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionDLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionDtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        break;
                    case "B":
                        optionALinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionAtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionBLinear.setBackgroundColor(getResources().getColor(R.color.optionBorange));
                        optionBtext.setTextColor(getResources().getColor(R.color.colorWhite));
                        optionCLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionCtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionDLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionDtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        break;
                    case "C":
                        optionBLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionBtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionCLinear.setBackgroundColor(getResources().getColor(R.color.optionCblue));
                        optionCtext.setTextColor(getResources().getColor(R.color.colorWhite));
                        optionALinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionAtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionDLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionDtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        break;
                    case "D":
                        optionBLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionBtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionCLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionCtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionALinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionAtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionDLinear.setBackgroundColor(getResources().getColor(R.color.optionDgreen));
                        optionDtext.setTextColor(getResources().getColor(R.color.colorWhite));
                }
            } else {
                optionBLinear.setBackgroundColor(getResources().getColor(R.color.optionBorange));
                optionBtext.setTextColor(getResources().getColor(R.color.colorWhite));
                optionCLinear.setBackgroundColor(getResources().getColor(R.color.optionCblue));
                optionCtext.setTextColor(getResources().getColor(R.color.colorWhite));
                optionALinear.setBackgroundColor(getResources().getColor(R.color.optionAred));
                optionAtext.setTextColor(getResources().getColor(R.color.colorWhite));
                optionDLinear.setBackgroundColor(getResources().getColor(R.color.optionDgreen));
                optionDtext.setTextColor(getResources().getColor(R.color.colorWhite));
            }


            try {
                Log.d("status answer", answerStatus.get(currentQuestion - 2));
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
                answerStatus.set(currentQuestion - 2, "pending");
                System.out.print(answerStatus.get(currentQuestion - 2));

            }


        } else {

            if (!(answerStatus.get(currentQuestion - 1).equals("pending"))) {
                currentselected = selected.get(currentQuestion - 1);

                switch (currentselected) {
                    case "A":
                        optionALinear.setBackgroundColor(getResources().getColor(R.color.optionAred));
                        optionAtext.setTextColor(getResources().getColor(R.color.colorWhite));
                        optionBLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionBtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionCLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionCtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionDLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionDtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        break;
                    case "B":
                        optionALinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionAtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionBLinear.setBackgroundColor(getResources().getColor(R.color.optionBorange));
                        optionBtext.setTextColor(getResources().getColor(R.color.colorWhite));
                        optionCLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionCtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionDLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionDtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        break;
                    case "C":
                        optionBLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionBtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionCLinear.setBackgroundColor(getResources().getColor(R.color.optionCblue));
                        optionCtext.setTextColor(getResources().getColor(R.color.colorWhite));
                        optionALinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionAtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionDLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionDtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        break;
                    case "D":
                        optionBLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionBtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionCLinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionCtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionALinear.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        optionAtext.setTextColor(getResources().getColor(R.color.colorBlack));
                        optionDLinear.setBackgroundColor(getResources().getColor(R.color.optionDgreen));
                        optionDtext.setTextColor(getResources().getColor(R.color.colorWhite));
                }
            } else {
                optionBLinear.setBackgroundColor(getResources().getColor(R.color.optionBorange));
                optionBtext.setTextColor(getResources().getColor(R.color.colorWhite));
                optionCLinear.setBackgroundColor(getResources().getColor(R.color.optionCblue));
                optionCtext.setTextColor(getResources().getColor(R.color.colorWhite));
                optionALinear.setBackgroundColor(getResources().getColor(R.color.optionAred));
                optionAtext.setTextColor(getResources().getColor(R.color.colorWhite));
                optionDLinear.setBackgroundColor(getResources().getColor(R.color.optionDgreen));
                optionDtext.setTextColor(getResources().getColor(R.color.colorWhite));
            }
            try {
                Log.d("status answer", answerStatus.get(currentQuestion - 2));
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
                answerStatus.set(currentQuestion - 1, "pending");
                System.out.print(answerStatus.get(currentQuestion - 1));

            }


            try {
                edit_quizDetails.putString("answers", ObjectSerializer.serialize(answerStatus));
                edit_quizDetails.putString("selected", ObjectSerializer.serialize(selected));
                edit_quizDetails.putLong("currentTime",currentTime);
                edit_quizDetails.apply();

                Intent i = new Intent(Questions.this, Submit.class);
                startActivity(i);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void initiateJumpDialog()
    {
        jumpToDialog.setContentView(R.layout.jump_to_dialog);

        gridview = jumpToDialog.findViewById(R.id.gridview);

        jumpToList.clear();
        for (int i = 0; i < numberOfQuestions; i++) {
            if ((i + 1) == currentQuestion) {
                jumpToList.add(new JumpTo(i + 1, answerStatus.get(i), true));

            } else {
                jumpToList.add(new JumpTo(i + 1, answerStatus.get(i), false));
            }
        }

        JumpToAdapter customAdapter = new JumpToAdapter(Questions.this, jumpToList);
        gridview.setAdapter(customAdapter);


        jumpToDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        jumpToDialog.show();
    }

}


