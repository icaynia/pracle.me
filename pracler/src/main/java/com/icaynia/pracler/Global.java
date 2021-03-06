package com.icaynia.pracler;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;

import com.facebook.FacebookSdk;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.icaynia.pracler.lockscreen.LockScreenService;
import com.icaynia.pracler.notifications.MusicNotification;
import com.icaynia.pracler.notifications.UserNotification;
import com.icaynia.pracler.remote.FirebaseSongManager;
import com.icaynia.pracler.services.AlertService;
import com.icaynia.pracler.data.LocalHistoryManager;
import com.icaynia.pracler.data.LocalLikeManager;
import com.icaynia.pracler.data.MusicFileManager;
import com.icaynia.pracler.data.PlayListManager;
import com.icaynia.pracler.data.UserManager;
import com.icaynia.pracler.models.LocalPlayHistory;
import com.icaynia.pracler.models.MusicDto;
import com.icaynia.pracler.models.PlayList;
import com.icaynia.pracler.models.PlayHistory;
import com.icaynia.pracler.services.MusicService;
import com.icaynia.pracler.remote.FirebaseUserManager;
import com.icaynia.pracler.remote.models.PraclerAlert;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.realm.Realm;

/**
 * Created by icaynia on 2017. 2. 8..
 */

public class Global extends Application
{
    public AlertService alertService;
    public MusicService musicService;
    public Intent musicServiceIntent;
    public Intent alertServiceIntent;
    public MusicFileManager mMusicManager;

    public PlayListManager playListManager;

    public PlayList nowPlayingList = new PlayList();

    public OnChangeListener onChangeListener = null;
    public LocalHistoryManager localHistoryManager;
    public LocalLikeManager localLikeManager;

    public UserManager userManager;

    public MusicDto nowPlaying;

    /* Firebase */
    public String loginUid;
    public FirebaseAuth firebaseAuth;
    public FirebaseUser loginUser;


    private ServiceConnection musicServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName name, IBinder service)
        {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            musicService = binder.getService();
            musicService.getPlayingMusic();

            musicService.mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
            {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer)
                {
                    int songid = musicService.getPlayingMusic();
                    addHistory(songid);
                    playNextMusic();
                    generatePlayerChangeEvent();
                }
            });

            Log.e("Global", "onServiceConnected: called");

        }

        @Override
        public void onServiceDisconnected(ComponentName name)
        {

        }
    };

    private ServiceConnection alertServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName name, IBinder service)
        {
            AlertService.AlertBinder binder = (AlertService.AlertBinder) service;
            alertService = binder.getService();
            alertService.getPlayingMusic();

            alertService.setOnAddedAlertListener(new AlertService.OnAddedAlertListener()
            {
                @Override
                public void onAdded(PraclerAlert alert)
                {
                    newAlert(alert);
                }
            });

            Log.e("Global", "onServiceConnected: called");

        }

        @Override
        public void onServiceDisconnected(ComponentName name)
        {

        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        this.init();
    }


    public void newAlert(final PraclerAlert alert)
    {
        UserNotification.build(this, alert);
    }

    public void init()
    {
        Realm.init(getApplicationContext());
        FacebookSdk.sdkInitialize(getApplicationContext());
        if (musicServiceIntent == null)
        {
            musicServiceIntent = new Intent(this, MusicService.class);
            bindService(musicServiceIntent, musicServiceConnection, Context.BIND_AUTO_CREATE);
            startService(musicServiceIntent);
        }
        if (alertServiceIntent == null)
        {
            alertServiceIntent = new Intent(this, AlertService.class);
            bindService(alertServiceIntent, alertServiceConnection, Context.BIND_AUTO_CREATE);
            startService(alertServiceIntent);
        }

        Intent lockScreenIntent = new Intent(this, LockScreenService.class);
        startService(lockScreenIntent);

        mMusicManager = new MusicFileManager(getApplicationContext());
        playListManager = new PlayListManager(this);
        firebaseAuth = FirebaseAuth.getInstance();
        loginUser = firebaseAuth.getCurrentUser();
        userManager = new UserManager();
        localHistoryManager = new LocalHistoryManager(this);
        localLikeManager = new LocalLikeManager(this);
        if (loginUser != null)
            loginUid = loginUser.getUid();
    }

    public void addNewSongInfoToRemote(MusicDto musicDto)
    {
        FirebaseSongManager.addNewSong(musicDto);
    }

    public void setNowListening(MusicDto musicDto)
    {
        FirebaseUserManager.setNowListening(musicDto);
    }

    public void addHistory(int songid)
    {
        // Regdate
        String format = new String("yyyyMMddHHmmss");
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.KOREA);
        String Regdate = sdf.format(new Date());

        // Local Save
        LocalPlayHistory localPlayHistory = new LocalPlayHistory();
        localPlayHistory.uid = songid;
        localPlayHistory.Regdate = Regdate;

        localHistoryManager.addHistory(localPlayHistory);
        localHistoryManager.getHistoryDesending();

        // Remote Server save
        MusicDto musicDto = mMusicManager.getMusicDto(songid+"");
        PlayHistory playHistory = new PlayHistory();
        playHistory.artist = MusicDto.replaceForInput(musicDto.getArtist());
        playHistory.album = MusicDto.replaceForInput(musicDto.getAlbum());
        playHistory.title = MusicDto.replaceForInput(musicDto.getTitle());

        userManager.addHistory(playHistory);
    }

    public void playMusic(long songId)
    {
        try
        {
            nowPlaying = mMusicManager.getMusicDto(songId + "");

            this.musicService.playMusic(songId + "");
            this.setNowListening(nowPlaying);
            this.addNewSongInfoToRemote(nowPlaying);
            this.setMusicNotification();
            generatePlayerChangeEvent();
        }
        catch (Exception e)
        {

        }

    }

    /** 주로 재생 곡이 바뀔 때 컨트롤러 뷰 업데이트를 위해 사용 */
    public void generatePlayerChangeEvent()
    {
        if (onChangeListener != null)
        {
            onChangeListener.onChange();
        }
    }

    public void playPrevMusic()
    {
        musicService.pause();
        musicService.mediaPlayer.reset();
        if (musicService.getPlayingMusicCurrentPosition() < 3000)
        {
            nowPlayingList.delPositionCount();
        }

        String nextmusic_uid = nowPlayingList.get(nowPlayingList.getPosition());
        if (nextmusic_uid != null)
        {
            playMusic(Integer.parseInt(nextmusic_uid));
        }
        generatePlayerChangeEvent();
    }

    public void playNextMusic()
    {
        musicService.stop();
        musicService.mediaPlayer.reset();
        nowPlayingList.addPositionCount();
        String nextmusic_uid = nowPlayingList.get(nowPlayingList.getPosition());
        if (nextmusic_uid != null)
        {
            playMusic(Integer.parseInt(nextmusic_uid));
        }
        generatePlayerChangeEvent();
    }

    public void setMusicNotification()
    {
        MusicNotification.update(this, musicService.getPlayingMusic());
    }


    public void setOnChangeListener(OnChangeListener listener)
    {
        this.onChangeListener = listener;
    }

    public interface OnChangeListener
    {
        void onChange();
    }

}
