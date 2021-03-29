package com.nemesiss.dev.piaprobox.Service.Player.Legacy;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.nemesiss.dev.piaprobox.Model.MusicStatus;
import io.reactivex.subjects.BehaviorSubject;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;


public class SimpleMusicPlayer
{

    public enum PrepareStatus {
        Prepared,
        Failed,
        Destroyed,
        Default
    }

    public MediaPlayer InnerMediaPlayer;
    private Context mContext;
    public BehaviorSubject<MusicStatus> MusicPlayStatus = BehaviorSubject.createDefault(MusicStatus.STOP);

    public BehaviorSubject<PrepareStatus> IsPrepared = BehaviorSubject.createDefault(PrepareStatus.Default);

    public OnPlayerTimeElapsedListener TimeElapsedListener = null;

    private Handler TimeElapsedHandler = new Handler(this::TimeElapsedDispatcher);
    private Handler PrepareStatusHandler = new Handler(this::PrepareHandler);

    private boolean PrepareHandler(Message message) {
        switch (message.what) {
            case 689: {
                IsPrepared.onNext(PrepareStatus.Failed);
                InnerMediaPlayer.reset();
                PrepareStatusHandler.removeMessages(689);
                break;
            }
        }


        return true;
    }

    private boolean TimeElapsedDispatcher(Message message)
    {
        switch (message.what) {
            case 688: {
                if(TimeElapsedListener != null && TimeElapsedHandler != null && MusicPlayStatus.getValue() == MusicStatus.PLAY && InnerMediaPlayer.isPlaying()) {
                    TimeElapsedListener.update(message.arg1);
                    TimeElapsedHandler.sendMessageDelayed(GetTimeElapsedMessage(),50);
                }
                break;
            }
        }
        return true;
    }

    private Message GetTimeElapsedMessage()
    {
        Message msg = new Message();
        msg.what = 688;
        msg.arg1 = InnerMediaPlayer.getCurrentPosition();
        return msg;
    }

    public SimpleMusicPlayer(Context context)
    {
        InnerMediaPlayer = new MediaPlayer();
        mContext = context;
        InnerMediaPlayer.setOnSeekCompleteListener(this::OnFinishSeek);
        InnerMediaPlayer.setOnPreparedListener(this::OnFinishPrepare);
        InnerMediaPlayer.setOnCompletionListener(this::OnPlayReachEnd);

        MusicPlayStatus.subscribe((status) -> {
            if(status == MusicStatus.PAUSE && InnerMediaPlayer.isPlaying())
                _Pause();
            else if(status == MusicStatus.PLAY && IsPrepared.getValue() == PrepareStatus.Prepared) {
                _Play();
            }
        });
    }

    private void OnPlayReachEnd(MediaPlayer mediaPlayer) {
        MusicPlayStatus.onNext(MusicStatus.END);
    }


    private void _Play()
    {
        InnerMediaPlayer.start();
        BeginDispatchElapsedTimeStamp();
    }
    private void _Pause()
    {
        InnerMediaPlayer.pause();
    }


    private void OnFinishPrepare(MediaPlayer mediaPlayer)
    {
        PrepareStatusHandler.removeMessages(689);
        Log.d("SimpleMusicPlayer", "Prepare Finished!");
        IsPrepared.onNext(PrepareStatus.Prepared);
        if(MusicPlayStatus.getValue() == MusicStatus.PLAY)
        {
            InnerMediaPlayer.start();
            BeginDispatchElapsedTimeStamp();
        }
    }

    public void BeginDispatchElapsedTimeStamp()
    {
        TimeElapsedHandler.sendMessage(GetTimeElapsedMessage());
    }
    public void DisableDispatchElapsedTimeStamp() { TimeElapsedHandler.removeMessages(688);}

    private void OnFinishSeek(MediaPlayer mp)
    {
        Log.d("SimpleMusicPlayer","完成 Seek");
    }

    public void LoadMusic(Uri uri) throws IOException
    {
        InnerMediaPlayer.reset();
        IsPrepared.onNext(PrepareStatus.Default);
        InnerMediaPlayer.setDataSource(mContext,uri);
        InnerMediaPlayer.prepareAsync();
        PrepareStatusHandler.sendEmptyMessageDelayed(689, 10 * 1000);
    }


    public void LoadMusic(String uriString) throws IOException
    {
        LoadMusic(Uri.parse(uriString));
    }
    public void LoadMusicFromFilePath(String filePath) throws IOException
    {
        InnerMediaPlayer.reset();
        InnerMediaPlayer.setDataSource(filePath);
        InnerMediaPlayer.prepareAsync();
    }
    public void LoadMusic(FileInputStream fis) throws IOException
    {
        IsPrepared.onNext(PrepareStatus.Default);
        InnerMediaPlayer.reset();
        InnerMediaPlayer.setDataSource(fis.getFD());
        InnerMediaPlayer.prepareAsync();
    }
    public void LoadMusic(AssetFileDescriptor assetFileDescriptor) throws IOException
    {
        IsPrepared.onNext(PrepareStatus.Default);
        InnerMediaPlayer.reset();
        FileDescriptor fd = assetFileDescriptor.getFileDescriptor();
        InnerMediaPlayer.setDataSource(fd,assetFileDescriptor.getStartOffset(),assetFileDescriptor.getLength());
        InnerMediaPlayer.prepareAsync();
    }

    public void SeekTo(int position)
    {
        int MillsPosition = (int) (InnerMediaPlayer.getDuration() * (((float) position) / 100));
        Log.d("SimpleMusicPlayer","开始Seek "+MillsPosition);
        InnerMediaPlayer.seekTo(MillsPosition);
    }

    public void Play(boolean NeedLoop)
    {
        MusicPlayStatus.onNext(MusicStatus.PLAY);
        BeginDispatchElapsedTimeStamp();
    }
    public void SetLooping(boolean loop)
    {
        InnerMediaPlayer.setLooping(loop);
    }
    public void Pause()
    {
        MusicPlayStatus.onNext(MusicStatus.PAUSE);
    }

    public interface OnPlayerTimeElapsedListener
    {
        void update(int CurrentTimeStamp);
    }

    public int GetDuration()
    {
        return InnerMediaPlayer.getDuration();
    }

    public void Stop() {
        if(IsPrepared.getValue() == PrepareStatus.Prepared) {
            InnerMediaPlayer.reset();
        }
        MusicPlayStatus.onNext(MusicStatus.STOP);
        IsPrepared.onNext(PrepareStatus.Default);
    }

    public void SafetyDestroy()
    {
        Stop();
        IsPrepared.onNext(PrepareStatus.Destroyed);
        InnerMediaPlayer.release();
        InnerMediaPlayer = null;
        Log.d("SimpleMusicPlayer", "底层音乐播放器已经停止！");
    }
}
