package com.example.user.testtask;

/**
 * Created by const on 25.10.2016.
 */

import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.text.*;
import java.util.Calendar;

import us.fatehi.pointlocation6709.Angle;
import us.fatehi.pointlocation6709.Latitude;
import us.fatehi.pointlocation6709.Longitude;
import us.fatehi.pointlocation6709.format.PointLocationFormatType;
import us.fatehi.pointlocation6709.format.PointLocationFormatter;
import us.fatehi.pointlocation6709.parse.PointLocationParser;

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

    private Point mPoint;
    private final OnCardInteractionListener mListener;

    public EditPointCardAdapter(Point pointSource, OnCardInteractionListener listener) {
        mPoint = pointSource;
        mListener = listener;
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
            String name = mPoint.getName();
            EditText editText = ((TextInputLayout)v.findViewById(R.id.point_name)).getEditText();
            if (name != null) {
                editText.setText(name);
            }
            editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (!hasFocus) {
                        EditText editText = (EditText)v; 
                        mPoint.setName(editText.getText().toString());
                    }
                }
            });

            final ImageView image = (ImageView) v.findViewById(R.id.map_image);

            final double latitude = mPoint.getLatitude();
            editText = ((TextInputLayout)v.findViewById(R.id.latitude_edit)).getEditText();
            setLatitudeText(editText, latitude, false);
            editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    EditText editText = (EditText)v;
                    double latitude = mPoint.getLatitude();
                    double longitude = mPoint.getLongitude();
                    double old = latitude;
                    if (!hasFocus) {
                        latitude = parseLatitude(editText);
                        mPoint.setLatitude(latitude);
                    }
                    setLatitudeText(editText, latitude, hasFocus);
                    if (old != latitude && !isNaN(latitude) && !isNaN(longitude)) {
                        MapHelper.LoadStatic(latitude, longitude, image);
                    }
                }
            });

            double longitude = mPoint.getLongitude();
            editText = ((TextInputLayout)v.findViewById(R.id.longitude_edit)).getEditText();
            setLongitudeText(editText, longitude, false);
            editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    EditText editText = (EditText)v;
                    double latitude = mPoint.getLatitude();
                    double longitude = mPoint.getLongitude();
                    double old = longitude;
                    if (!hasFocus) {
                        longitude = parseLongitude(editText);
                        mPoint.setLongitude(longitude);
                    }
                    setLongitudeText(editText, longitude, hasFocus);
                    if (old != longitude && !isNaN(latitude) && !isNaN(longitude)) {
                        MapHelper.LoadStatic(latitude, longitude, image);
                    }
                }
            });

            if (!isNaN(latitude) && !isNaN(longitude)) {
                MapHelper.LoadStatic(latitude, longitude, image);
            }

            SimpleDateFormat sf = (SimpleDateFormat)SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT); // new SimpleDateFormat(holder.cardView.getResources().getString(R.string.sql_time_format));
            String lastVisited = sf.format(mPoint.getLastVisited());
            editText = ((TextInputLayout)v.findViewById(R.id.datetime_edit)).getEditText();
            if (lastVisited != null) {
                editText.setText(lastVisited);
            }
            editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    EditText editText = (EditText)v;
                    Calendar lastVisited = mPoint.getLastVisited();
                    Calendar old = (Calendar)lastVisited.clone();
                    if (!hasFocus) {
                        longitude = parseLongitude(editText);
                        mPoint.setLongitude(longitude);
                    }
                    setLongitudeText(editText, longitude, hasFocus);
                    if (old != longitude && !isNaN(latitude) && !isNaN(longitude)) {
                        MapHelper.LoadStatic(latitude, longitude, image);
                    }
                }
            });
        }
        else {
            ImageView image = (ImageView)v.findViewById(R.id.image);
            mPoint.LoadImage(image, position - 1);
            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onImageClick(position);
                    }
                }
            });

            ImageButton starButton = (ImageButton)v.findViewById(R.id.star_button);
            boolean isDefaultImage = mPoint.getDefaultImage(position - 1);
            starButton.setImageResource(isDefaultImage ? R.drawable.ic_star_white_24px : R.drawable.ic_star_white_24px);
            starButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onStarButtonClick(position);
                    }
                }
            });
        }
    }
    
    void setLatitudeText(EditText editText, double latitude, boolean hasFocus) {
        if (!isNaN(latitude)) {
            String str;
            if (hasFocus) {
                str = Double.toString(mPoint.getLatitude());
            }
            else {
                try {
                    Latitude val = new Latitude(Angle.fromDegrees(latitude));
                    str = PointLocationFormatter.formatLatitude(val, PointLocationFormatType.HUMAN_LONG);
                }
                catch (Exception e) {
                    str = Double.toString(latitude);
                }
            }
            editText.setText(str);
        }
    }
    
    double parseLatitude(EditText editText) {
        return Double.parseDouble(editText.getText().toString());
    }

    void setLongitudeText(EditText editText, double longitude, boolean hasFocus) {
        if (!isNaN(longitude)) {
            String str;
            if (hasFocus) {
                str = Double.toString(mPoint.getLongitude());
            }
            else {
                try {
                    Longitude val = new Longitude(Angle.fromDegrees(longitude));
                    str = PointLocationFormatter.formatLongitude(val, PointLocationFormatType.HUMAN_LONG);
                }
                catch (Exception e) {
                    str = Double.toString(longitude);
                }
            }
            editText.setText(str);
        }
    }

    double parseLongitude(EditText editText) {
        return Double.parseDouble(editText.getText().toString());
    }

    @Override
    public int getItemCount() {
        return mPoint.getImagesCount() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0 ? 0 : 1);
    }
}
