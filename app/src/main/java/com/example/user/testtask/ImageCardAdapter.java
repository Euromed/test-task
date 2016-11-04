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
    private final Resources mResources;
    private Drawable starDefault = null;
    private Drawable noStar = null;

    public ImageCardAdapter(Point pointSource, Resources resources, OnCardInteractionListener listener) {
        mPoint = pointSource;
        mResources = resources;
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
        if (mPoint.getDefaultImage(position)) {
            if (starDefault != null) {
                starDefault = mResources.getDrawable(R.drawable.ic_star_white_24px);
            }
            starButton.setImageDrawable(starDefault);
        }
        else {
            if (noStar != null) {
                noStar = mResources.getDrawable(R.drawable.ic_star_border_white_24px);
            }
            starButton.setImageDrawable(noStar);
        }
        starButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onStarButtonClick(position);
                }
            }
        });

        ImageView image = (ImageView)v.findViewById(R.id.image);
        mPoint.LoadImage(image, position);
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onImageClick(position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mPoint.getImagesCount();
    }

}
