package com.example.cs571.Adapter;

import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cs571.NewsItem;
import com.example.cs571.R;
import com.example.cs571.Utilities;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class FavoriteNewsListAdapter extends RecyclerView.Adapter<FavoriteNewsListAdapter.NewsItemViewHolder>{

    private ArrayList<NewsItem> mNewsList;
    public RecyclerView mRecycler;

    public static class NewsItemViewHolder extends RecyclerView.ViewHolder {
        public ImageView mImageView;
        public TextView mTitle;
        public TextView mDateAndSection;
        public ImageView mBookMarkImage;

        public NewsItemViewHolder(@NonNull final View itemView) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.c_news_image);
            mTitle = itemView.findViewById(R.id.c_news_title);
            mDateAndSection = itemView.findViewById(R.id.c_news_date_section);
            mBookMarkImage = itemView.findViewById(R.id.c_news_bookmark);
        }
    }


    public FavoriteNewsListAdapter(ArrayList<NewsItem> mNewsList, RecyclerView mRecycler) {
        this.mRecycler = mRecycler;
        this.mNewsList = mNewsList;
    }


    @NonNull
    @Override
    public NewsItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.news_card_item_favorite, parent, false);
        //setDialogListener(v);
        //setBookmarkListener(v.findViewById(R.id.c_news_bookmark));
        return new NewsItemViewHolder(v);
    }



    ///// QIULONG
    private FavoriteNewsListAdapter.OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
        void onItemLongClick(View view, int position);
    }

    public void setOnItemClickListener(FavoriteNewsListAdapter.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
    ///// QIULONG



    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull final FavoriteNewsListAdapter.NewsItemViewHolder holder, final int position) {
        NewsItem cur = mNewsList.get(position);
        holder.mTitle.setText(cur.getTitle());
        String tmp = Utilities.timeConvertToDDMMYYYY(cur.getDate()).substring(0,6);
        tmp += " | " + cur.getSection();
        holder.mDateAndSection.setText(tmp);

        /////  QIULONG
        if (onItemClickListener != null) {
            holder.mBookMarkImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onItemClick(holder.mBookMarkImage, position);
                }
            });
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onItemClick(holder.itemView, position);
                }
            });
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    onItemClickListener.onItemLongClick(holder.itemView, position);
                    return true;
                }
            });
        }
        /////  QIULONG


        if (cur.isMarked()) {
            holder.mBookMarkImage.setImageResource(R.drawable.bookmark);
        } else {
            holder.mBookMarkImage.setImageResource(R.drawable.bookmark_not);
        }
        // TODO: check getContext right?
        Picasso.with(holder.mImageView.getContext()).load(cur.getImage()).centerCrop().resize(190, 150).into(holder.mImageView);
    }

    @Override
    public int getItemCount() {
        return mNewsList.size();
    }





//    @Override
//    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
//        super.onDetachedFromRecyclerView(recyclerView);
//        Log.v("DETACH", "DETACHED HAPPEND");
//    }



}
