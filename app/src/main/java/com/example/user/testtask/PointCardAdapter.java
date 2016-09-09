package com.example.user.testtask;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by User on 15.07.2016.
 */
public class PointCardAdapter extends RecyclerView.Adapter<PointCardAdapter.ViewHolder> {
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        public ViewHolder(CardView v) {
            super(v);
            cardView = v;
        }
    }

    private Points points;
    private final int[] imgIds = {R.id.mainImage, R.id.image1, R.id.image2, R.id.image3, R.id.image4, R.id.image5};

    public PointCardAdapter(Points pointsSource) {
        points = pointsSource;
    }

    @Override
    public PointCardAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        CardView v = (CardView)LayoutInflater.from(parent.getContext())
                .inflate(R.layout.point_card, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        CardView v = holder.cardView;
        TextView caption = (TextView)v.findViewById(R.id.caption);
        caption.setText(points.getCaption(position));
        int imagesCount = points.getImagesCount(position);
        for(int i = 0; i < imgIds.length; ++i) {
            ImageView img = (ImageView)v.findViewById(imgIds[i]);
            if (i < imagesCount) {
                points.LoadImage(img, position, i);
            }
            else {
                img.setImageBitmap(null);
            }
        }
    }

    @Override
    public int getItemCount() {
        return points.getCount();
    }
}
