package com.icaynia.soundki.View;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.icaynia.soundki.Data.MusicFileManager;
import com.icaynia.soundki.Model.MusicDto;
import com.icaynia.soundki.R;

import java.util.List;

/**
 * Created by icaynia on 2017. 2. 8..
 */

public class MusicListAdapter extends BaseAdapter
{
    private Context context;
    private LayoutInflater inflater;


    public List<MusicDto> list;

    public MusicListAdapter(Context context, List<MusicDto> list)
    {
        this.context = context;
        Log.e("test", context.getPackageName());
        //inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount()
    {
        return list.size();
    }

    @Override
    public Object getItem(int position)
    {
        return position;
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
            convertView = inflater.inflate(R.layout.view_list_musicrows, parent, false);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            convertView.setLayoutParams(layoutParams);
        }


        ImageView album = (ImageView) convertView.findViewById(R.id.view_album);
        album.setImageDrawable(context.getResources().getDrawable(android.R.drawable.ic_menu_report_image));
        TextView title = (TextView) convertView.findViewById(R.id.view_title);
        TextView artist = (TextView) convertView.findViewById(R.id.view_artist);

        title.setText(list.get(position).title);
        artist.setText(list.get(position).artist);

        title.setTag(list.get(position).id + "");

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
        private MusicFileManager mMusicFileManager;

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
                bitmap = mMusicFileManager.getAlbumImage(context, Integer.parseInt(list.get(position).albumid), 170);
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