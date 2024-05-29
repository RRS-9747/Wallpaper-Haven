package me.rrs.greatwallpaper;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import me.rrs.greatwallpaper.DiffUtil.Callback;

public class WallpaperAdapter extends RecyclerView.Adapter<WallpaperAdapter.ViewHolder> {

    private final List<String> wallpaperUrls;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(String imageUrl);
    }

    public WallpaperAdapter(List<String> wallpaperUrls, OnItemClickListener listener) {
        this.wallpaperUrls = new ArrayList<>(wallpaperUrls);
        this.listener = listener;
    }

    public void updateWallpapers(List<String> newWallpaperUrls) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new Callback(this.wallpaperUrls, newWallpaperUrls));
        this.wallpaperUrls.clear();
        this.wallpaperUrls.addAll(newWallpaperUrls);
        diffResult.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_wallpaper, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String imageUrl = wallpaperUrls.get(position);
        Context context = holder.wallpaperImageView.getContext();

        // Load placeholder image synchronously
        Glide.with(context)
                .load(R.drawable.placeholder)
                .override(300, 300)
                .centerCrop()
                .into(holder.wallpaperImageView);

        // Load actual image asynchronously
        Glide.with(context)
                .load(imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .override(300, 300)
                .centerCrop()
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        // Handle loading failure if needed
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        // Image loaded successfully, do any necessary operations here
                        return false;
                    }
                })
                .into(holder.wallpaperImageView);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(imageUrl);
            }
        });
    }

    @Override
    public int getItemCount() {
        return wallpaperUrls.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView wallpaperImageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            wallpaperImageView = itemView.findViewById(R.id.wallpaperImageView);
        }
    }
}
