package com.ruffneck.player.activity.recyclerview;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.ruffneck.player.R;
import com.ruffneck.player.music.Music;
import com.ruffneck.player.task.LoadImageTask;
import com.ruffneck.player.utils.FormatUtils;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MusicListAdapter extends RecyclerView.Adapter<MusicListAdapter.MusicViewHolder> {

    private int highLightItem = 0;

    List<Music> musics;

    OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        void onClickListener(View v, int position);

        void onLongClickListener(View v, int position);
    }

    public void setOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    public MusicListAdapter(List<Music> musics) {
        this.musics = musics;
    }

    @Override
    public int getItemViewType(int position) {
        if (!(position == highLightItem))
            return 0;
        else
            return 1;
    }

    @Override
    public MusicViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case 1:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_playing, parent, false);
                break;
            default:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_music, parent, false);
                break;
        }

        return new MusicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MusicViewHolder holder, int position) {
        holder.tv_artist.setText(musics.get(position).getArtist());
        holder.tv_duration.setText(FormatUtils.formatTime(musics.get(position).getDuration()));
        holder.tv_songName.setText(musics.get(position).getTitle());

        final Context context = holder.itemView.getContext();

        LoadImageTask task = new LoadImageTask(context);

        task.setUpdateCallBack(new LoadImageTask.UpdateCallBack() {
            @Override
            public void onUpdate(Music music) {
                Picasso.with(context)
                        .load(music.getImgUrl())
                        .error(R.drawable.placeholder_civ_singer)
                        .placeholder(R.drawable.placeholder_civ_singer)
                        .centerCrop()
                        .fit()
                        .into(holder.civ_singer);
            }
        });

        task.setUpdateFailCallBack(new LoadImageTask.UpdateFailCallBack() {
            @Override
            public void onUpdateFail(Music music) {
                Picasso.with(context)
                        .load(R.drawable.placeholder_civ_singer)
                        .centerCrop()
                        .fit()
                        .into(holder.civ_singer);
            }
        });

        task.execute(musics.get(position));

        setOnItemClickEvent(holder, position);
    }


    /**
     * set the view's on click event by the inner interface.
     *
     * @param holder   the viewHolder need to be set event listener.
     * @param position the view's position.
     */
    private void setOnItemClickEvent(final MusicViewHolder holder, final int position) {

        if (mOnItemClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.onClickListener(holder.itemView, position);
                }
            });
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mOnItemClickListener.onLongClickListener(holder.bt_item_more, position);
                    return true;
                }
            });
            holder.bt_item_more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.onLongClickListener(v, position);
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return musics.size();
    }

    public class MusicViewHolder extends RecyclerView.ViewHolder {

        TextView tv_songName;
        TextView tv_artist;
        TextView tv_duration;
        CircleImageView civ_singer;
        ImageButton bt_item_more;

        public MusicViewHolder(View view) {
            super(view);
            tv_artist = (TextView) view.findViewById(R.id.tv_artist);
            tv_songName = (TextView) view.findViewById(R.id.tv_song_name);
            tv_duration = (TextView) view.findViewById(R.id.tv_duration);
            civ_singer = (CircleImageView) view.findViewById(R.id.civ_singer);
            bt_item_more = (ImageButton) view.findViewById(R.id.bt_item_more);
        }
    }

    public int getHighLightItem() {
        return highLightItem;
    }

    public void setHighLightMusic(Music highLightMusic) {
        this.highLightItem = musics.indexOf(highLightMusic);
        notifyItemChanged(highLightItem);
    }

    public void notifyDataSetChanged(Music highLightMusic){
        setHighLightMusic(highLightMusic);
        notifyDataSetChanged();
    }
}
