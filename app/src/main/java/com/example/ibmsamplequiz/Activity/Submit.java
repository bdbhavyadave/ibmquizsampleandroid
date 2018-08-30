package com.example.ibmsamplequiz.Activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.cloudant.client.api.ClientBuilder;
import com.cloudant.client.api.CloudantClient;
import com.cloudant.client.api.Database;
import com.cloudant.client.api.model.SearchResult;
import com.example.ibmsamplequiz.AdapterObjectClasses.SubmitConfirm;
import com.example.ibmsamplequiz.Adapters.statusAdapter;
import com.example.ibmsamplequiz.Helper.ObjectSerializer;
import com.example.ibmsamplequiz.R;
import com.example.ibmsamplequiz.modelClass.searchMailDoc;
import com.example.ibmsamplequiz.modelClass.userQuizzes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Submit extends AppCompatActivity {


    RecyclerView statusRecycler;
    SharedPreferences shared_quizDetails;
    SharedPreferences.Editor edit_quizDetails;
    ProgressDialog pDialog;
    int submitted;
    final int EXCEPTION =1,SUCCESS = 2;

    AppCompatButton submit;
    List<String> selected, statements,answerStatus;
    List<SubmitConfirm> AdapterData = new ArrayList <SubmitConfirm>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        statusRecycler = findViewById(R.id.statusRecycler);
        submit = findViewById(R.id.submitQuiz);

        shared_quizDetails = getSharedPreferences("QuizDetails", Context.MODE_PRIVATE);
        edit_quizDetails = shared_quizDetails.edit();
        

        try {
            selected = (List<String>) ObjectSerializer.deserialize(shared_quizDetails.getString("selected", ObjectSerializer.serialize(new ArrayList<String>())));
            statements = (ArrayList<String>) ObjectSerializer.deserialize(shared_quizDetails.getString("Statements", ObjectSerializer.serialize(new ArrayList<String>())));

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        for(int i =0; i < selected.size();i++)
        {
            AdapterData.add(new SubmitConfirm(selected.get(i),statements.get(i)));
        }

        statusRecycler.setLayoutManager(new LinearLayoutManager(this));
        statusAdapter statusAdapt = new statusAdapter(Submit.this, AdapterData);
        statusRecycler.setAdapter(statusAdapt);

        if(shared_quizDetails.getInt("timerFlag",0)==1)
        {
            buildProgressBar();
            new AsyncCaller().execute();
        }

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buildProgressBar();
                new AsyncCaller().execute();
            }
        });
    }

    private class AsyncCaller extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }
        @Override
        protected Void doInBackground(Void... params) {


            //this method will be running on background thread so don't update UI frome here
            //do your long running http tasks here,you dont want to pass argument and u can access the parent class' variable url over here
            try {

                CloudantClient client = ClientBuilder.account("0394f385-5643-40db-b1aa-94749be22786-bluemix")
                        .username("0394f385-5643-40db-b1aa-94749be22786-bluemix")
                        .password("359ceb21f168b585b78d9386e2220d7bb49cfe6cc55399704532996ee267be2a")
                        .build();

                System.out.println("Server Version: " + client.serverVersion());

                String mail = shared_quizDetails.getString("userMail","");
                String name = shared_quizDetails.getString("userName","");
                String quizId = shared_quizDetails.getString("quizid","");
                int marksPerQues = shared_quizDetails.getInt("marksPerQues",1);
                int score = 0, IDFLAG = -1;
                int correct = 0, incorrect = 0;
                answerStatus = (ArrayList<String>) ObjectSerializer.deserialize(shared_quizDetails.getString("answers", ObjectSerializer.serialize(new ArrayList<String>())));

                for(int i =0;i<answerStatus.size();i++)
                {
                    if(answerStatus.get(i).equals("correct"))
                    {
                        correct++;
                    }
                    else
                    {
                        incorrect++;
                    }
                }

                score = correct*marksPerQues;

                edit_quizDetails.putInt("corrected",correct);
                edit_quizDetails.putInt("incorrected",incorrect);
                edit_quizDetails.apply();
                List<userQuizzes> quiz = new ArrayList <userQuizzes>();

                quiz.add(new userQuizzes(quizId,correct,incorrect,score));
                if(!mail.equals(""))
                {

                    Database db = client.database("user_responses", false);

                    SearchResult<searchMailDoc> result = db.search("searchuser/searchuser")
                            .includeDocs(true)
                            .querySearchResult("email:"+mail, searchMailDoc.class);

                    if(result.getTotalRows() == 0)
                    {
                        searchMailDoc userDoc = new searchMailDoc(name,mail,quiz);
                        db.save(userDoc);
                        edit_quizDetails.putInt("timerFlag",1);
                        edit_quizDetails.apply();
                        System.out.println("create");
                        submitted = SUCCESS;


                    }
                    else {
                        System.out.println(result.getRows().get(0).getDoc().getEmail());

                        List<userQuizzes> quizupdate = new ArrayList <userQuizzes>();

                        for(int i= 0 ; i<result.getRows().get(0).getDoc().getUserQuizzes().size();i++)
                        {
                            if(quizId.equals(result.getRows().get(0).getDoc().getUserQuizzes().get(i).getId()))
                            {
                                IDFLAG = i;
                                break;
                            }
                        }

                        for(int i= 0 ; i<result.getRows().get(0).getDoc().getUserQuizzes().size();i++)
                        {
                            String id = result.getRows().get(0).getDoc().getUserQuizzes().get(i).getId();
                            int correctOpt = result.getRows().get(0).getDoc().getUserQuizzes().get(i).getCorrect();
                            int incorrectOpt = result.getRows().get(0).getDoc().getUserQuizzes().get(i).getIncorrect();
                            int scoreOpt = result.getRows().get(0).getDoc().getUserQuizzes().get(i).getScore();
                            if(IDFLAG == -1) {
                                quizupdate.add(new userQuizzes(id, correctOpt, incorrectOpt, scoreOpt));
                            }
                            else if(!(quizId.equals(id)))
                            {
                                quizupdate.add(new userQuizzes(id, correctOpt, incorrectOpt, scoreOpt));

                            }
                        }


                            quizupdate.add(new userQuizzes(quizId,correct,incorrect,score));

                            searchMailDoc userDocupdate = new searchMailDoc(result.getRows().get(0).getDoc().getId(),result.getRows().get(0).getDoc().getRev(),name,mail,quizupdate);

                            db.update(userDocupdate);
                        edit_quizDetails.putInt("timerFlag",1);
                        edit_quizDetails.apply();
                            System.out.println("update");
                            submitted = SUCCESS;



                    }


                }

            }
            catch (Exception e)
            {
                submitted = EXCEPTION;
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            pDialog.dismiss();

            if(submitted == SUCCESS)
            {
                Log.d("Submit flag",shared_quizDetails.getInt("timerFlag",0)+"");
                Toast.makeText(Submit.this, "Submitted succesfully.", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(Submit.this,PerformanceChart.class);
                startActivity(i);

            }
            else if(submitted == EXCEPTION)
            {
                if(shared_quizDetails.getInt("timerFlag",0)==1)
                {
                    Toast.makeText(Submit.this, "Submission failed, trying again!.", Toast.LENGTH_SHORT).show();

                    buildProgressBar();
                    new AsyncCaller().execute();
                }
                else {
                    Toast.makeText(Submit.this, "Submission failed, try again!.", Toast.LENGTH_SHORT).show();
                }



            }
            // this method will be running on UI thread

        }

    }

    private void buildProgressBar()
    {
        pDialog = new ProgressDialog(Submit.this);
        pDialog.setMessage("Submitting Your Quiz");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();

    }

}
