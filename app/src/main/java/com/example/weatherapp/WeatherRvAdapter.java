package com.example.weatherapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class WeatherRvAdapter extends RecyclerView.Adapter<WeatherRvAdapter.ViewHolder> {
    final Context context;
    final ArrayList<WeatherRVModel>weatherRvModelArrayList;
    public WeatherRvAdapter(Context context,ArrayList<WeatherRVModel>weatherRvModelArrayList){
        this.context=context;
        this.weatherRvModelArrayList=weatherRvModelArrayList;
    }

    @NonNull
    @Override
    public WeatherRvAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        //Inflate item layout for each forecast entry
        View v = LayoutInflater.from(context).inflate(R.layout.recyclerview_layout,parent,false);
        return new ViewHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull WeatherRvAdapter.ViewHolder holder, int position) {
        WeatherRVModel model = weatherRvModelArrayList.get(position);

        //set weather attributes
        holder.rvWindSpeed.setText(model.getWindSpeed()+" km/h");
        holder.rvTemperature.setText(model.getTemperature()+"°c");

        String icon = model.getIcon();

        Picasso.get().load(icon).error(R.drawable.error_place).into(holder.rvIcon);

        //parse and format time to display in 12 hour format
        SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        SimpleDateFormat output = new SimpleDateFormat("hh:mm aa");
        try{
            Date t= input.parse(model.getTime());
            holder.rvTime.setText(output.format(t));
        }
        catch (ParseException e){
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return weatherRvModelArrayList.size();
    }

    // viewHolder class for recyclerView
    public static class ViewHolder extends RecyclerView.ViewHolder{
    TextView rvTime,rvTemperature,rvWindSpeed;
    ImageView rvIcon;
        public ViewHolder(View v){
            super(v);
            rvTime=v.findViewById(R.id.idTvRvTime);
            rvTemperature=v.findViewById(R.id.idTvRvTemperature);
            rvWindSpeed=v.findViewById(R.id.idTvRvWindSpeed);
            rvIcon=v.findViewById(R.id.idIvRvImage);

        }
    }
}

