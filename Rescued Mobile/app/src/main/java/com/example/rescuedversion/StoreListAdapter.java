package com.example.rescuedversion;

import static com.facebook.FacebookSdk.getApplicationContext;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.util.ArrayList;

public class StoreListAdapter extends RecyclerView.Adapter<StoreListAdapter.ViewHolder> {

    private ArrayList<String> mNames;
    private ArrayList<String> mDetails;
    private ArrayList<ArrayList<String>> mImageUrls;
    private ArrayList<ArrayList<String>> mFoodDetails;
    private ArrayList<ArrayList<String>> mFoodNames;
    private Context mContext;
    private onRecyclerViewUpdatedListener mListener;
    private RecyclerView mView;
    private RelativeLayout mLayout;
    private GoogleMap mMap;
    private ArrayList<LatLng> mPoints;
    SmoothLinearLayoutManager mLayoutManager;

    private boolean foodItemsExpanded = false;

    public StoreListAdapter(Context context, StoreListAdapter.onRecyclerViewUpdatedListener listener, RelativeLayout layout, SmoothLinearLayoutManager layoutManager, GoogleMap map, ArrayList<LatLng> points, ArrayList<String> names, ArrayList<String> details, ArrayList<ArrayList<String>> imageUrls, ArrayList<ArrayList<String>> foodDetails, ArrayList<ArrayList<String>> foodNames) {
        mNames = names;
        mDetails = details;
        mImageUrls = imageUrls;
        mFoodDetails = foodDetails;
        mFoodNames = foodNames;
        mContext = context;
        mListener = listener;
        mLayout = layout;
        mMap = map;
        mPoints = points;
        mLayoutManager = layoutManager;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_storeitem, parent, false);
        return new ViewHolder(view, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.storeName.setText(mNames.get(position));
        holder.storeInfo.setText(mDetails.get(position));

        Glide.with(mContext).asBitmap().load(mImageUrls.get(position).get(0)).into(holder.foodItem1);
        Glide.with(mContext).asBitmap().load(mImageUrls.get(position).get(1)).into(holder.foodItem2);
        Glide.with(mContext).asBitmap().load(mImageUrls.get(position).get(2)).into(holder.foodItem3);
        Glide.with(mContext).asBitmap().load(mImageUrls.get(position).get(3)).into(holder.foodItem4);

        SmoothLinearLayoutManager layoutManager = new SmoothLinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
        holder.foodItems.setLayoutManager(layoutManager);
        FoodItemsAdapter adapter = new FoodItemsAdapter(mContext, reformatArray(mFoodNames.get(position)), reformatArray(mFoodDetails.get(position)), reformatArray(mImageUrls.get(position)));
        holder.foodItems.setAdapter(adapter);

        for (int i = 0; i < mPoints.size(); i++) {
            mMap.addMarker(new MarkerOptions().position(mPoints.get(i)).icon(BitmapFromVector(getApplicationContext(), R.drawable.rescued_marker_primary_resized))).setTag(i);
        }
    }

    private BitmapDescriptor BitmapFromVector(Context context, int vectorResId) {
        // below line is use to generate a drawable.
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        // below line is use to set bounds to our vector drawable.
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        // below line is use to create a bitmap for our
        // drawable which we have added.
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicWidth(), Bitmap.Config.ARGB_8888);
        // below line is use to add bitmap in our canvas.
        Canvas canvas = new Canvas(bitmap);
        // below line is use to draw our
        // vector drawable in canvas.
        vectorDrawable.draw(canvas);
        // after generating our bitmap we are returning our bitmap.
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    @Override
    public int getItemCount() {
        return mNames.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView foodItem1;
        ImageView foodItem2;
        ImageView foodItem3;
        ImageView foodItem4;
        ImageView expandStoreDetails;
        TextView storeName;
        TextView storeInfo;
        RecyclerView foodItems;
        RelativeLayout storeContainer;

        View detailsFragment;
        RelativeLayout directionsContainer;

        private BottomSheetBehavior bottomSheetBehavior;

        public ViewHolder(View itemView, StoreListAdapter.onRecyclerViewUpdatedListener listener) {
            super(itemView);

            mView = mLayout.findViewById(R.id.storesView);

            foodItem1 = itemView.findViewById(R.id.food_item_1);
            foodItem2 = itemView.findViewById(R.id.food_item_2);
            foodItem3 = itemView.findViewById(R.id.food_item_3);
            foodItem4 = itemView.findViewById(R.id.food_item_4);

            foodItems = itemView.findViewById(R.id.food_items);
            storeContainer = itemView.findViewById(R.id.store_container);

            expandStoreDetails = itemView.findViewById(R.id.expand_store_details);

            storeName = itemView.findViewById(R.id.store_name);
            storeInfo = itemView.findViewById(R.id.store_info);

            detailsFragment = itemView.findViewById(R.id.details_fragment);
            detailsFragment.findViewById(R.id.header_container).setElevation(0);

            bottomSheetBehavior = BottomSheetBehavior.from(detailsFragment);
            bottomSheetBehavior.setDraggable(false);

            directionsContainer = itemView.findViewById(R.id.directions_container);

            detailsFragment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (foodItemsExpanded) {
                        minimizeFragment(listener);
                    } else {
                        expandFragment(listener);
                    }
                }
            });

            mView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);

                    if (foodItemsExpanded) minimizeFragment(listener);
                }
            });

            directionsContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openDetailsFragment();
                }
            });
        }

        private void expandFragment(StoreListAdapter.onRecyclerViewUpdatedListener listener) {
            listener.onExpanded();

            mLayoutManager.setScrollEnabled(false);

            detailsFragment.setBackgroundColor(Color.WHITE);

            CoordinatorLayout.LayoutParams marginParams = new CoordinatorLayout.LayoutParams(
                    CoordinatorLayout.LayoutParams.MATCH_PARENT,
                    CoordinatorLayout.LayoutParams.WRAP_CONTENT
            );
            marginParams.setMargins(0, 0, 0, 0);
            detailsFragment.setLayoutParams(marginParams);

            ViewGroup.LayoutParams params = storeContainer.getLayoutParams();
            params.height = RecyclerView.LayoutParams.MATCH_PARENT;
            storeContainer.setLayoutParams(params);

            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

            AnimationSet animSet = new AnimationSet(true);
            animSet.setInterpolator(new DecelerateInterpolator());
            animSet.setFillAfter(true);
            animSet.setFillEnabled(true);

            final RotateAnimation animRotate = new RotateAnimation(0.0f, 180.0f,
                    RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                    RotateAnimation.RELATIVE_TO_SELF, 0.5f);

            animRotate.setDuration(500);
            animRotate.setFillAfter(true);
            animSet.addAnimation(animRotate);

            expandStoreDetails.startAnimation(animSet);

            foodItemsExpanded = true;
        }

        private void minimizeFragment(StoreListAdapter.onRecyclerViewUpdatedListener listener) {
            listener.onMinimized();

            mLayoutManager.setScrollEnabled(true);

            detailsFragment.setBackgroundColor(Color.TRANSPARENT);

            CoordinatorLayout.LayoutParams marginParams = new CoordinatorLayout.LayoutParams(
                    CoordinatorLayout.LayoutParams.MATCH_PARENT,
                    CoordinatorLayout.LayoutParams.WRAP_CONTENT
            );
            marginParams.setMargins(0, R.dimen.recyclerview_bottom_margin, 0, 0);
            //detailsFragment.setLayoutParams(marginParams);

            ViewGroup.LayoutParams params = storeContainer.getLayoutParams();
            params.height = RecyclerView.LayoutParams.WRAP_CONTENT;
            storeContainer.setLayoutParams(params);

            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

            AnimationSet animSet = new AnimationSet(true);
            animSet.setInterpolator(new DecelerateInterpolator());
            animSet.setFillAfter(true);
            animSet.setFillEnabled(true);

            final RotateAnimation animRotate = new RotateAnimation(0.0f, 0.0f,
                    RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                    RotateAnimation.RELATIVE_TO_SELF, 0.5f);

            animRotate.setDuration(500);
            animRotate.setFillAfter(true);
            animSet.addAnimation(animRotate);

            expandStoreDetails.startAnimation(animSet);

            foodItemsExpanded = false;
        }
    }

    private void openDetailsFragment() {
        FragmentManager fragmentManager = ((FragmentActivity)mContext).getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        String[] hours = {};
        Bundle bundle = new Bundle();
        bundle.putString("name", "Jewel-Osco 95th St");
        bundle.putString("info", "3 mi away · Open today until 9:00 PM");
        bundle.putString("address", "2855 95th St, Naperville, IL");
        bundle.putStringArray("hours", hours);
        bundle.putString("locationType", "Grocery Store");
        bundle.putString("pickupDirections", "");
        bundle.putString("phoneNumber", "6309045119");
        bundle.putString("email", "contact@jewelosco.com");
        bundle.putString("website", "https://www.jewelosco.com/");
        DetailsFragment fragment = new DetailsFragment();
        fragment.setArguments(bundle);

        transaction.replace(R.id.store_details_container, fragment);
        transaction.commit();

        mLayout.findViewById(R.id.store_details_container).setVisibility(View.VISIBLE);
    }

    public void closeDetailsFragment() {
        mLayout.findViewById(R.id.store_details_container).setVisibility(View.GONE);
    }

    public interface onRecyclerViewUpdatedListener {
        default void onExpanded() {}
        default void onMinimized() {}
    }

    public ArrayList<ArrayList<String>> reformatArray(ArrayList<String> array) {
        ArrayList<ArrayList<String>> returnArray = new ArrayList<>();

        for (int i = 0; i < array.size()/2; i++) {
            ArrayList<String> bufferArray = new ArrayList<>();
            bufferArray.add(array.get(2 * i));
            bufferArray.add(array.get(2 * i + 1));
            returnArray.add(bufferArray);
        }

        return returnArray;
    }
}
