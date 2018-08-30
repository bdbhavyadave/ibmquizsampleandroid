package com.example.ibmsamplequiz.Adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ibmsamplequiz.AdapterObjectClasses.SubmitConfirm;
import com.example.ibmsamplequiz.Helper.ObjectSerializer;
import com.example.ibmsamplequiz.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by BD on 27-03-2018.
 */

public class resultAdapter extends RecyclerView.Adapter<resultAdapter.ViewHolder> {

    private List<SubmitConfirm> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    Context context;
    ArrayList<String> statementsArray, optionAarray, optionBarray,optionCarray,optionDarray,correctOptionArray,answerStatus, selected;

    SharedPreferences shared_quizDetails;
    SharedPreferences.Editor edit_quizDetails;

    // data is passed into the constructor
    public resultAdapter(Context context, List<SubmitConfirm> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.context = context;


    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.result_list_item, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {

        shared_quizDetails = context.getSharedPreferences("QuizDetails", Context.MODE_PRIVATE);
        edit_quizDetails = shared_quizDetails.edit();

        try {
            optionAarray = (ArrayList<String>) ObjectSerializer.deserialize(shared_quizDetails.getString("optionA", ObjectSerializer.serialize(new ArrayList<String>())));
            optionBarray = (ArrayList<String>) ObjectSerializer.deserialize(shared_quizDetails.getString("optionB", ObjectSerializer.serialize(new ArrayList<String>())));
            optionCarray = (ArrayList<String>) ObjectSerializer.deserialize(shared_quizDetails.getString("optionC", ObjectSerializer.serialize(new ArrayList<String>())));
            optionDarray = (ArrayList<String>) ObjectSerializer.deserialize(shared_quizDetails.getString("optionD", ObjectSerializer.serialize(new ArrayList<String>())));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        SubmitConfirm question = mData.get(position);

        String optionAnswer = "N/A";
        String option = question.getSelectedOption();

        switch (option)
        {
            case "A": optionAnswer = optionAarray.get(position);
            break;
            case "B": optionAnswer = optionBarray.get(position);
                break;
            case "C": optionAnswer = optionCarray.get(position);
                break;
            case "D": optionAnswer = optionDarray.get(position);
                break;
        }

        holder.numberTextView.setText(""+(position+1)+".");
        holder.statementTextView.setText(question.getQuestion());

        if(question.getStatus().equals("correct"))
        {
            holder.statusImage.setImageResource(R.drawable.right);
        }
        else if(question.getStatus().equals("incorrect"))
        {
            holder.statusImage.setImageResource(R.drawable.cross);

        }
        else if(question.getStatus().equals("pending"))
        {
            holder.statusImage.setImageResource(R.drawable.cross);
        }

        holder.optionTextView.setVisibility(View.GONE);
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView optionTextView, numberTextView, statementTextView;
        ImageView statusImage;

        ViewHolder(View itemView) {
            super(itemView);
            optionTextView = itemView.findViewById(R.id.correctText);
            numberTextView = itemView.findViewById(R.id.questionNumberResult);
            statementTextView = itemView.findViewById(R.id.statementTextResult);
            statusImage = itemView.findViewById(R.id.statusImage);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    SubmitConfirm getItem(int id) {
        return mData.get(id);
    }

    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
