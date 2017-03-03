package com.icaynia.soundki.View;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.icaynia.soundki.Data.MusicFileManager;
import com.icaynia.soundki.Global;
import com.icaynia.soundki.Model.MusicDto;
import com.icaynia.soundki.Model.MusicList;
import com.icaynia.soundki.Model.PlayList;
import com.icaynia.soundki.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by icaynia on 2017. 2. 8..
 */

public class MusicListAdapter extends BaseAdapter
{
    private Global global;
    private Context context;
    private LayoutInflater inflater;
    public MusicList list;

    private boolean CHOICEMODE = false;
    private ArrayList<Boolean> checkState = new ArrayList<>();

    public MusicListAdapter(Context context, MusicList list)
    {
        this.context = context;
        Log.e("test", context.getPackageName());
        this.list = list;
        inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        global = (Global) context.getApplicationContext();
        for (int i = 0; i < list.size(); i++)
        {
            checkState.add(i, false);
        }
    }

    @Override
    public int getCount()
    {
        return list.size();
    }

    @Override
    public MusicDto getItem(int position)
    {
        return list.getItem(position);
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
            ListView.LayoutParams layoutParams = new ListView.LayoutParams(ListView.LayoutParams.MATCH_PARENT, ListView.LayoutParams.MATCH_PARENT);
            convertView.setLayoutParams(layoutParams);
        }
        final ImageView check = (ImageView) convertView.findViewById(R.id.select_icon);


        if (isChoiceMode())
        {
            RelativeLayout lv=  (RelativeLayout) convertView.findViewById(R.id.lv);
            lv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    setCheckState(position, !getCheckState(position));
                    if (getCheckState(position))
                    {
                        check.setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        check.setVisibility(View.GONE);
                    }
                }
            });
            if (getCheckState(position))
            {
                check.setVisibility(View.VISIBLE);
            }
            else
            {
                check.setVisibility(View.GONE);
            }

        }
        else
        {
            check.setVisibility(View.GONE);
        }

        ImageView album = (ImageView) convertView.findViewById(R.id.view_album);
        album.setImageDrawable(context.getResources().getDrawable(android.R.drawable.ic_menu_report_image));
        TextView title = (TextView) convertView.findViewById(R.id.view_title);
        TextView artist = (TextView) convertView.findViewById(R.id.view_artist);

        title.setText(list.getItem(position).title);
        artist.setText(list.getItem(position).artist + " - " + list.getItem(position).album + " - " + global.mMusicManager.convertToTime(list.getItem(position).length));

        convertView.setTag(list.getItem(position).uid_local);
        MyAsyncTask myAsyncTask = new MyAsyncTask();
        myAsyncTask.setImgView(album);
        myAsyncTask.execute(position + "");

        return convertView;
    }


    public boolean isChoiceMode()
    {
        return CHOICEMODE;
    }

    public void setChoiceMode(boolean state)
    {
        this.CHOICEMODE = state;
    }

    public boolean getCheckState(int position)
    {
        return checkState.get(position);
    }

    public void setCheckState(int position, boolean state)
    {
        //ok
        checkState.set(position, state);
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
                int ir = Integer.parseInt(list.getItem(position).album_id);
                bitmap = mMusicFileManager.getAlbumImage(context, ir, 100);
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
