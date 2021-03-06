package com.icaynia.pracler.Fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.icaynia.pracler.View.CardHeader;
import com.icaynia.pracler.data.PlayListManager;
import com.icaynia.pracler.Global;
import com.icaynia.pracler.models.MusicDto;
import com.icaynia.pracler.models.PlayList;
import com.icaynia.pracler.R;
import com.icaynia.pracler.adapters.PlayListAdapter;
import com.icaynia.pracler.View.PlayListSelectPopup;
import com.icaynia.pracler.View.SelectPopup;

import java.util.ArrayList;

/**
 * Created by icaynia on 21/03/2017.
 *
 */

public class PlayListFragment extends Fragment
{
    private Global global;
    private View v;
    private PlayListManager playListManager;
    private String playListName;
    private ListView listView;
    private PlayList playList;
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        global = (Global) getContext().getApplicationContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        v = inflater.inflate(R.layout.fragment_playlist, container, false);
        setHasOptionsMenu(true);
        viewInitialize();

        prepare();
        return v;
    }

    public void viewInitialize()
    {
        listView = (ListView) v.findViewById(R.id.listview);
    }

    public void setPlayList(String playListName)
    {
        this.playListName = playListName;
    }

    public void prepare()
    {
        playList = global.playListManager.getPlayList(playListName);
        PlayListAdapter playListAdapter = new PlayListAdapter(getContext(), this.playList);
        listView.setAdapter(playListAdapter);


        CardHeader cardHeader = new CardHeader(getContext());
        cardHeader.setTitleIcon(getResources().getDrawable(R.drawable.ic_playlist_play_black));
        cardHeader.setTitleText(playListName);
        listView.addHeaderView(cardHeader);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                i -= 1;
                global.playMusic(Integer.parseInt(playList.get(i)));

                PlayList newNowPlayingList = new PlayList();
                for (int t = 0; t < playList.size(); t++)
                {
                    newNowPlayingList.addItem(playList.get(t));
                }

                newNowPlayingList.setPosition(i);
                global.nowPlayingList = newNowPlayingList;
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
        {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l)
            {
                i -= 1;
                final MusicDto musicDto = global.mMusicManager.getMusicDto(playList.get(i));

                final SelectPopup selectPopup = new SelectPopup(getContext());
                selectPopup.setTag("PlayListFragmentPopup");
                ArrayList<String> arrayList = new ArrayList<>();
                arrayList.add("재생");
                arrayList.add("다음 재생");
                arrayList.add("재생목록에 추가");
                selectPopup.setList(arrayList);
                selectPopup.setListener(new SelectPopup.OnCompleteSelect()
                {
                    @Override
                    public void onComplete(int position)
                    {
                        switch(position)
                        {
                            case 0:
                                global.playMusic(Integer.parseInt(musicDto.getUid_local()));
                                selectPopup.dismiss();
                                break;
                            case 1:
                                global.nowPlayingList.addItem(Integer.parseInt(musicDto.getUid_local()), global.nowPlayingList.getPosition() + 1);
                                selectPopup.dismiss();
                                break;
                            case 2:
                                final PlayListSelectPopup popup = new PlayListSelectPopup(getContext());

                                final ArrayList<String> arrayList = global.playListManager.getPlayListList();
                                popup.setList(arrayList);

                                popup.setListener(new SelectPopup.OnCompleteSelect()
                                {
                                    @Override
                                    public void onComplete(int position)
                                    {
                                        PlayList tmpPlayList = global.playListManager.getPlayList(arrayList.get(position));
                                        tmpPlayList.addItem(musicDto);
                                        global.playListManager.savePlayList(tmpPlayList);
                                        Toast.makeText(getContext(), "저장되었습니다.", Toast.LENGTH_SHORT).show();
                                        popup.dismiss();
                                        selectPopup.dismiss();
                                    }
                                });
                                popup.show();
                                break;
                        }
                    }
                });
                selectPopup.show();
                return false;
            }
        });
    }



}
