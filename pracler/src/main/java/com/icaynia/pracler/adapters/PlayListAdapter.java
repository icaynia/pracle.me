package com.icaynia.pracler.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.icaynia.pracler.data.MusicFileManager;
import com.icaynia.pracler.Global;
import com.icaynia.pracler.models.MusicDto;
import com.icaynia.pracler.models.PlayList;
import com.icaynia.pracler.R;

/**
 * Created by icaynia on 19/02/2017.
 */

public class PlayListAdapter extends BaseAdapter
{
    public Context context;
    public PlayList list;
    private LayoutInflater inflater;

    private Global global;

    public PlayListAdapter(Context context, PlayList list)
    {
        this.context = context;
        this.list = list;
        inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        global = (Global) context.getApplicationContext();
    }

    @Override
    public int getCount()
    {
        return list.size();
    }

    @Override
    public Object getItem(int position)
    {
        return list.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent)
    {
        if (convertView == null)
        {
            convertView = inflater.inflate(R.layout.view_list_playlistrows, parent, false);
            ListView.LayoutParams layoutParams = new ListView.LayoutParams(ListView.LayoutParams.MATCH_PARENT, ListView.LayoutParams.MATCH_PARENT);
            convertView.setLayoutParams(layoutParams);
        }

        MusicDto music = global.mMusicManager.getMusicDto(list.get(position)+"");

        ImageView album = (ImageView) convertView.findViewById(R.id.view_album);
        album.setImageDrawable(context.getResources().getDrawable(android.R.drawable.ic_menu_report_image));
        TextView title = (TextView) convertView.findViewById(R.id.view_title);
        TextView artist = (TextView) convertView.findViewById(R.id.view_artist);

        title.setText(music.getTitle());
        artist.setText(music.getTitle() + " - " + music.getAlbum() + " - " + global.mMusicManager.convertToTime(music.getLength()));

        convertView.setTag(music.getUid_local());
        MyAsyncTask myAsyncTask = new MyAsyncTask();
        myAsyncTask.setImgView(album);
        myAsyncTask.execute(position + "");


        return convertView;
    }


    public class MyAsyncTask extends AsyncTask<String, Void, Bitmap>
    {
        private static final String TAG = "AlbumImageTask";
        private int position;
        private ImageView imgView;
        private MusicFileManager mMusicFileManager = new MusicFileManager(context);

        public void setImgView(ImageView _imgView)
        {
            this.imgView = _imgView;
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            Log.i(TAG, "PreExecute");
            mMusicFileManager = new MusicFileManager(context);
        }

        @Override
        protected Bitmap doInBackground(String... id)
        {
            Bitmap bitmap = null;
            for (String i : id)
            {
                this.position = Integer.parseInt(i);

                MusicDto music = global.mMusicManager.getMusicDto(list.get(position));

                bitmap = mMusicFileManager.getAlbumImage(context, Integer.parseInt(music.getAlbumId()), 100);
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result)
        {
            super.onPostExecute(result);

            imgView.setImageBitmap(result);
        }

    }
}
