package com.example.ibmsamplequiz.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.ibmsamplequiz.R;

public class NameEmail extends AppCompatActivity {

    EditText nameEdit,emailEdit;
    String name, mail;

    AppCompatButton submit;
    SharedPreferences shared_quizDetails;
    SharedPreferences.Editor edit_quizDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name_email);

        shared_quizDetails = getSharedPreferences("QuizDetails", Context.MODE_PRIVATE);
        edit_quizDetails = shared_quizDetails.edit();

        nameEdit = findViewById(R.id.nameEdit);
        emailEdit = findViewById(R.id.emailEdit);
        submit = findViewById(R.id.nameEmailSubmit);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                name = nameEdit.getText().toString().trim();
                mail = emailEdit.getText().toString().trim();

                if(name.equals(""))
                {
                    Toast.makeText(NameEmail.this, "Enter Name!", Toast.LENGTH_SHORT).show();

                }
                else {
                    if (checkEmailFormat(mail) && !(mail.equals(""))) {

                        edit_quizDetails.putString("userMail",mail);
                        edit_quizDetails.putString("userName",name);
                        edit_quizDetails.apply();

                        Intent i = new Intent(NameEmail.this,Instructions.class);
                        startActivity(i);

                    } else {
                        Toast.makeText(NameEmail.this, "Email invalid!", Toast.LENGTH_SHORT).show();

                    }
                }


            }
        });
    }

    private boolean checkEmailFormat(String email) {
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(NameEmail.this, "Email invalid!", Toast.LENGTH_SHORT).show();
            return false;
        } else return true;
    }
}
