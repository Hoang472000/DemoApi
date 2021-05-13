package com.example.demoapi;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ViewAdapter extends BaseAdapter {
    public ViewAdapter(ArrayList<Model> mListView) {
        this.mListView = mListView;
    }

    private ArrayList<Model> mListView;

    @Override
    public int getCount() {
        return mListView.size();
    }

    @Override
    public Object getItem(int position) {
        return mListView.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View viewDisease;
        if (convertView == null) {
            viewDisease = View.inflate(parent.getContext(), R.layout.viewdetail, null);
        } else viewDisease = convertView;
        Model disease = (Model) getItem(position);
        ((TextView) viewDisease.findViewById(R.id.description)).setText(String.format("Description : %s ", disease.mDescription));
        ((TextView) viewDisease.findViewById(R.id.header)).setText(String.format("Header : %s", disease.mhHeader));
        return viewDisease;
    }
}
