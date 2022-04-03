package com.example.rescuedversion;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class FoodItemsAdapter extends RecyclerView.Adapter<FoodItemsAdapter.ViewHolder> {

    private ArrayList<ArrayList<String>> mNames;
    private ArrayList<ArrayList<String>> mDates;
    private ArrayList<ArrayList<String>> mImageUrls;
    private Context mContext;

    public FoodItemsAdapter(Context context, ArrayList<ArrayList<String>> names, ArrayList<ArrayList<String>> dates, ArrayList<ArrayList<String>> imageUrls) {
        mContext = context;
        mNames = names;
        mDates = dates;
        mImageUrls = imageUrls;
    }

    @NonNull
    @Override
    public FoodItemsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_food_items, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodItemsAdapter.ViewHolder holder, int position) {
        Glide.with(mContext).asBitmap().load(mImageUrls.get(position).get(0)).into(holder.foodImage1);
        holder.foodName1.setText(mNames.get(position).get(0));
        holder.foodDate1.setText(mDates.get(position).get(0));

        Glide.with(mContext).asBitmap().load(mImageUrls.get(position).get(1)).into(holder.foodImage2);
        holder.foodName2.setText(mNames.get(position).get(1));
        holder.foodDate2.setText(mDates.get(position).get(1));
    }

    @Override
    public int getItemCount() {
        return mNames.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView foodImage1;
        TextView foodName1;
        TextView foodDate1;

        ImageView foodImage2;
        TextView foodName2;
        TextView foodDate2;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            foodImage1 = itemView.findViewById(R.id.food_image_1);
            foodName1 = itemView.findViewById(R.id.food_name_1);
            foodDate1 = itemView.findViewById(R.id.food_date_1);

            foodImage2 = itemView.findViewById(R.id.food_image_2);
            foodName2 = itemView.findViewById(R.id.food_name_2);
            foodDate2 = itemView.findViewById(R.id.food_date_2);
        }
    }
}
