package com.example.haynews;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.MyViewHolder> {

    private List<NewsItem> list;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(NewsItem item);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public NewsAdapter(List<NewsItem> list) {
        this.list = list;
    }

    public void updateData(List<NewsItem> newList) {
        list = newList;
        notifyDataSetChanged();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView imgNews;
        TextView txtTitle, txtSubtitle;

        public MyViewHolder(View itemView) {
            super(itemView);
            imgNews = itemView.findViewById(R.id.imgNews);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtSubtitle = itemView.findViewById(R.id.txtSubtitle);
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_news_card, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int pos) {
        NewsItem item = list.get(pos);

        holder.txtTitle.setText(item.title);
        holder.txtSubtitle.setText(item.subtitle);

        // Safely load image: some articles may not have an image URL
        if (item.imageUrl != null && !item.imageUrl.isEmpty()) {
            Picasso.get()
                    .load(item.imageUrl)
                    .into(holder.imgNews);
        } else {
            // Optional: set a placeholder or clear previous image
            holder.imgNews.setImageDrawable(null);
        }

        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(item);
            } else {
                // Fallback to default behavior
                Intent intent = new Intent(v.getContext(), ArticleDetailActivity.class);
                intent.putExtra("title", item.title);
                intent.putExtra("meta", item.subtitle);
                intent.putExtra("image", item.imageUrl);
                intent.putExtra("content", item.content);
                intent.putExtra("description", item.description);
                intent.putExtra("credibility", String.valueOf(item.credibilityScore));
                intent.putExtra("url", item.url);
                intent.putExtra("source", item.source);
                intent.putExtra("author", item.author);
                v.getContext().startActivity(intent);
            }
        });
    }


    @Override
    public int getItemCount() {
        return list.size();
    }
}
