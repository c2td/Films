package com.example.pm.films;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

/** This is an adapter class for RecyclerView in MainActivity */

class FilmAdapter extends RecyclerView.Adapter<FilmAdapter.MyViewHolder> {

    private Context mContext;
    private List<Film> mFilmList;
    static final String PARCELABLE_EXTRA = "film";

    class MyViewHolder extends RecyclerView.ViewHolder {

        // The view contains a film title and a poster image
        private TextView title;
        private ImageView poster;

        MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.title);
            poster = (ImageView) view.findViewById(R.id.poster);
        }
    }

    FilmAdapter(Context mContext, List<Film> filmList) {
        this.mContext = mContext;
        this.mFilmList = filmList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.film_card, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final Film film = mFilmList.get(position);
        holder.title.setText(film.getTitle());

        // display the poster
        Glide.with(mContext).load(film.getPosterUrl()).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(holder.poster);

        holder.poster.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // open the details activity when a poster is clicked
                Intent intent = new Intent(mContext, FilmDetailsActivity.class);
                intent.putExtra(PARCELABLE_EXTRA, film);
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mFilmList.size();
    }

}
