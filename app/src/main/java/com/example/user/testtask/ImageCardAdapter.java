package com.example.user.testtask;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

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

    private Point point;
    private final OnCardInteractionListener listener;

    public ImageCardAdapter(Point pointSource, OnCardInteractionListener listener) {
        point = pointSource;
        this.listener = listener;
    }

    @Override
    public ImageCardAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                          int viewType) {
        CardView v = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.point_card, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        CardView v = holder.cardView;
        ImageView image = (ImageView)v.findViewById(R.id.image);
        ImageButton starButton = (ImageButton)v.findViewById(R.id.star_button);
        point.LoadImage(image, position);
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onImageClick(position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return point.getCount();
    }
}
