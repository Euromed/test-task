package com.example.user.testtask;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

/**
 * Created by const on 13.09.2016.
 */
public class ImageCardAdapter extends RecyclerView.Adapter<ImageCardAdapter.ViewHolder> {

    public interface OnCardInteractionListener {
        public void onImageClick(int image, View v);
        public void onStarButtonClick(int image, View v);
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
    private Drawable starDefault = null;
    private Drawable noStar = null;

    public ImageCardAdapter(Point pointSource, OnCardInteractionListener listener) {
        mPoint = pointSource;
        mListener = listener;
    }

    @Override
    public ImageCardAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                          int viewType) {
        CardView v = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.image_card, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        CardView v = holder.cardView;

        ImageButton starButton = (ImageButton)v.findViewById(R.id.star_button);
        boolean isDefaultImage = mPoint.getDefaultImage(position - 1);
        starButton.setImageResource(isDefaultImage ? R.drawable.ic_star_white_24px : R.drawable.ic_star_white_24px);
        starButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onStarButtonClick(position, v);
                }
            }
        });

        ImageView image = (ImageView)v.findViewById(R.id.image);
        mPoint.LoadImage(image, position);
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onImageClick(position, v);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mPoint.getImagesCount();
    }

}
