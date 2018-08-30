package com.example.ibmsamplequiz.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.ibmsamplequiz.AdapterObjectClasses.JumpTo;
import com.example.ibmsamplequiz.Activity.Questions;
import com.example.ibmsamplequiz.R;

import java.util.List;

public class JumpToAdapter extends BaseAdapter {

    private LayoutInflater layoutinflater;
    private List<JumpTo> listStorage;
    private Context context;

    public JumpToAdapter(Context context, List<JumpTo> customizedListView) {
        this.context = context;
        layoutinflater =(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        listStorage = customizedListView;
    }

    @Override
    public int getCount() {
        return listStorage.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final ViewHolder listViewHolder;
        if(convertView == null){
            listViewHolder = new ViewHolder();
            convertView = layoutinflater.inflate(R.layout.jummpto_list_item, parent, false);
            listViewHolder.textInListView = convertView.findViewById(R.id.quesNumberText);
            convertView.setTag(listViewHolder);
        }else{
            listViewHolder = (ViewHolder)convertView.getTag();
        }

        listViewHolder.textInListView.setText("" + listStorage.get(position).getContent());

        if(listStorage.get(position).isCurrent()) {
            if (listStorage.get(position).getState().equals("correct") || listStorage.get(position).getState().equals("incorrect")) {
                listViewHolder.textInListView.setBackgroundColor(context.getResources().getColor(R.color.colorPrimary));
                listViewHolder.textInListView.setTextColor(context.getResources().getColor(R.color.colorWhite));
            } else {
                listViewHolder.textInListView.setBackgroundColor(context.getResources().getColor(R.color.colorPrimary));
                listViewHolder.textInListView.setTextColor(context.getResources().getColor(R.color.colorWhite));
            }
        }
        else
        {
            if (listStorage.get(position).getState().equals("correct") || listStorage.get(position).getState().equals("incorrect")) {
                listViewHolder.textInListView.setBackground(context.getResources().getDrawable(R.drawable.green_border));
                listViewHolder.textInListView.setTextColor(context.getResources().getColor(R.color.colorGreen));
            } else {
                listViewHolder.textInListView.setTextColor(context.getResources().getColor(R.color.colorPrimary));
            }
        }

        listViewHolder.textInListView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Questions.setCurrentQuestion(position + 1,context);
            }
        });

        return convertView;
    }

    static class ViewHolder{
        TextView textInListView;
    }

}