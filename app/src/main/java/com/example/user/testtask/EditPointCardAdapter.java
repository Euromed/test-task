package com.example.user.testtask;

/**
 * Created by const on 25.10.2016.
 */

import android.content.Context;
import android.os.Parcelable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.text.*;

import us.fatehi.pointlocation6709.Angle;
import us.fatehi.pointlocation6709.Latitude;
import us.fatehi.pointlocation6709.Longitude;
import us.fatehi.pointlocation6709.format.PointLocationFormatType;
import us.fatehi.pointlocation6709.format.PointLocationFormatter;

import static java.lang.Double.*;

public class EditPointCardAdapter extends RecyclerView.Adapter<EditPointCardAdapter.ViewHolder> {
    public interface OnCardInteractionListener {
        public void onImageClick(int image);
        public void onStarButtonClick(int image);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        public ViewHolder(CardView v) {
            super(v);
            cardView = v;
        }
    }

    private Point point;
    private final OnCardInteractionListener listener;

    public EditPointCardAdapter(Point pointSource, OnCardInteractionListener listener) {
        point = pointSource;
        this.listener = listener;
    }

    @Override
    public EditPointCardAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                              int viewType) {
        CardView v = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(viewType == 0 ? R.layout.point_edit_card : R.layout.image_card, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        CardView v = holder.cardView;
        if (position == 0) {
            String name = point.getName();
            if (name != null) {
                EditText editText = ((TextInputLayout)v.findViewById(R.id.point_name)).getEditText();
                editText.setText(name);
            }

            double latitude = point.getLatitude();
            if (!isNaN(latitude)) {
                EditText editText = ((TextInputLayout)v.findViewById(R.id.latitude_edit)).getEditText();
                String str = null;
                try {
                    Latitude val = new Latitude(Angle.fromDegrees(latitude));
                    str = PointLocationFormatter.formatLatitude(val, PointLocationFormatType.HUMAN_LONG);
                    editText.setText(str);
                }
                catch (Exception e) {}
            }

            double longitude = point.getLongitude();
            if (!isNaN(longitude)) {
                EditText editText = ((TextInputLayout)v.findViewById(R.id.longitude_edit)).getEditText();
                String str = null;
                try {
                    Longitude val = new Longitude(Angle.fromDegrees(longitude));
                    str = PointLocationFormatter.formatLongitude(val, PointLocationFormatType.HUMAN_LONG);
                    editText.setText(str);
                }
                catch (Exception e) {}
            }

            if (!isNaN(latitude) && !isNaN(longitude)) {
                ImageView image = (ImageView) v.findViewById(R.id.map_image);
                MapHelper.LoadStatic(latitude, longitude, image);
            }

            SimpleDateFormat sf = (SimpleDateFormat)SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT); // new SimpleDateFormat(holder.cardView.getResources().getString(R.string.sql_time_format));
            String lastVisited = sf.format(point.getLastVisited());
            if (lastVisited != null) {
                EditText editText = ((TextInputLayout)v.findViewById(R.id.datetime_edit)).getEditText();
                editText.setText(lastVisited);
            }
        }
        else {
            ImageView image = (ImageView)v.findViewById(R.id.image);
            point.LoadImage(image, position - 1);
            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onImageClick(position);
                    }
                }
            });

            ImageButton starButton = (ImageButton)v.findViewById(R.id.star_button);
            int defaultImage = position - 1; //point.getDefaultImage();
            starButton.setImageResource(defaultImage == 0 ? R.drawable.ic_star_white_24px : R.drawable.ic_star_white_24px);
        }
    }

    @Override
    public int getItemCount() {
        return point.getImagesCount() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0 ? 0 : 1);
    }
}
