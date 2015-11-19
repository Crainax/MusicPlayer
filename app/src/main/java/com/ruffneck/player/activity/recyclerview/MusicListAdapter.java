package com.ruffneck.player.activity.recyclerview;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ruffneck.player.R;
import com.ruffneck.player.music.Music;
import com.ruffneck.player.utils.FormatUtils;

import java.util.List;

public class MusicListAdapter extends RecyclerView.Adapter<MusicListAdapter.MusicViewHolder> {

    List<Music> musics;

    OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener{
        void onClickListener(View v,int position);
        void onLongClickListener(View v,int position);
    }

    public void setOnItemClickListener(OnItemClickListener mOnItemClickListener){
        this.mOnItemClickListener = mOnItemClickListener;
    }

    public MusicListAdapter(List<Music> musics) {
        this.musics = musics;
    }

    @Override
    public MusicViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_music, parent, false);

        return new MusicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MusicViewHolder holder, int position) {
        holder.tv_artist.setText(musics.get(position).getArtist());
        holder.tv_duration.setText(FormatUtils.formatTime(musics.get(position).getDuration()));
        holder.tv_songName.setText(musics.get(position).getTitle());

        setOnItemClickEvent(holder,position);
    }


    /**
     * set the view's on click event by the inner interface.
     * @param holder the viewHolder need to be set event listener.
     * @param position the view's position.
     */
    private void setOnItemClickEvent(final MusicViewHolder holder, final int position) {

        if(mOnItemClickListener != null){
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.onClickListener(holder.itemView,position);
                }
            });
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mOnItemClickListener.onLongClickListener(holder.itemView,position);
                    return true;
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return musics.size();
    }

    public class MusicViewHolder extends RecyclerView.ViewHolder{

        TextView tv_songName ;
        TextView tv_artist;
        TextView tv_duration;

        public MusicViewHolder(View view) {
            super(view);
            tv_artist = (TextView) view.findViewById(R.id.tv_artist);
            tv_songName = (TextView) view.findViewById(R.id.tv_song_name);
            tv_duration = (TextView) view.findViewById(R.id.tv_duration);
        }
    }

}
