package com.example.ibmsamplequiz.Activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.example.ibmsamplequiz.AdapterObjectClasses.SubmitConfirm;
import com.example.ibmsamplequiz.Adapters.resultAdapter;
import com.example.ibmsamplequiz.Helper.ObjectSerializer;
import com.example.ibmsamplequiz.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Score extends AppCompatActivity {

    RecyclerView resultRecycler;
    SharedPreferences shared_quizDetails;
    SharedPreferences.Editor edit_quizDetails;
    ProgressDialog pDialog;
    int submitted;
    final int EXCEPTION =1,SUCCESS = 2;

    AppCompatButton next;
    List<String> correct, statements,answerStatus;
    List<SubmitConfirm> AdapterData = new ArrayList<SubmitConfirm>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score
        );

        resultRecycler = findViewById(R.id.resultRecycler);
        next = findViewById(R.id.submitResult);

        shared_quizDetails = getSharedPreferences("QuizDetails", Context.MODE_PRIVATE);
        edit_quizDetails = shared_quizDetails.edit();

        try {
            correct = (List<String>) ObjectSerializer.deserialize(shared_quizDetails.getString("correct", ObjectSerializer.serialize(new ArrayList<String>())));
            statements = (ArrayList<String>) ObjectSerializer.deserialize(shared_quizDetails.getString("Statements", ObjectSerializer.serialize(new ArrayList<String>())));
            answerStatus = (ArrayList<String>) ObjectSerializer.deserialize(shared_quizDetails.getString("answers", ObjectSerializer.serialize(new ArrayList<String>())));


        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }



        for(int i =0; i < correct.size();i++)
        {
            AdapterData.add(new SubmitConfirm(correct.get(i),statements.get(i),answerStatus.get(i)));
        }

        resultRecycler.setLayoutManager(new LinearLayoutManager(this));
        resultAdapter resultAdapt = new resultAdapter(Score.this, AdapterData);
        resultRecycler.setAdapter(resultAdapt);

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(Score.this, "Thanks for attempting the quiz!", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(Score.this,getQuizId.class);
                startActivity(i);
            }
        });

    }
}
