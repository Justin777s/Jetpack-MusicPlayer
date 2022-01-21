package com.kunminx.player.cust;


import android.content.res.AssetFileDescriptor;
import android.os.Build;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.kunminx.player.cust.data.ChangedAudio;
import com.kunminx.player.cust.data.PlayList;
import com.kunminx.player.cust.data.Playable;
import com.kunminx.player.cust.data.PlayingInfo;

import java.text.ParseException;

/**
 * 提供播放控制相关的事件，以及状态更新的通知回调（LiveData）
 */
public class JtPlayerControl implements IJtPlayerControl {

    public static final String TAG = JtPlayerControl.class.getSimpleName();

    private MutableLiveData<ChangedAudio> changedAudioLiveData = new MutableLiveData<>();
    //播放中的，状态信息，包括当前进度等
    private MutableLiveData<PlayingInfo> playingStatusLiveData = new MutableLiveData<>();
    //暂停状态，控制播放器播放还是暂停
    private MutableLiveData<Boolean> pauseLiveData = new MutableLiveData<>();

    //准备就绪状态，控制播放器播放还是暂停
    private MutableLiveData<Enum> stateLiveData = new MutableLiveData<>();

    //播放模式 暂时用不到
    private MutableLiveData<Integer> playModeLiveData = new MutableLiveData<>();

    private PlayingInfo playingInfo = new PlayingInfo();
    private ChangedAudio changedAudio = new ChangedAudio();
    private Boolean isPause = false;
    private Boolean isPrepare = false;

    private PlayListManager playListManager;


    public String getProgressText(int progress,boolean fromMan) {
        String result = "";
        try {
            //TODO 未执行初始化，这里的总时长需要外部传入 未prepared的时候返回 5832704
            if (!JtMediaPlayer.getInstance().isPrepared()) {
                result = "00:00";
            } else {
                String current  =  "00" ;
                //TODO 代码需要调整
                if(fromMan){
                    //使用 seek进度*总时长duration = 当前seek的刻度
                    current  =  PlayerUtils.floatToString(JtMediaPlayer.getInstance().getMediaPlayer().getDuration()*(progress/100.0f), null);
                    Log.d(TAG,"fromMan progress:"+progress+",current:"+current+","+JtMediaPlayer.getInstance().getMediaPlayer().getCurrentPosition());
                }else{
                    current  =  PlayerUtils.intToString(JtMediaPlayer.getInstance().getMediaPlayer().getCurrentPosition(),null) ;
                    Log.d(TAG,"progress:"+progress+",current:"+current);
                }
                result =current
                        + ":" + PlayerUtils.intToString(JtMediaPlayer.getInstance().getMediaPlayer().getDuration(), null);
            }
            Log.d(TAG, JtMediaPlayer.getInstance().getMediaPlayer().getCurrentPosition() + "," + JtMediaPlayer.getInstance().getMediaPlayer().getDuration());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return result;
    }


    public MutableLiveData<ChangedAudio> getChangedAudioLiveData() {
        return changedAudioLiveData;
    }

    public void setChangedAudioLiveData(MutableLiveData<ChangedAudio> changedAudioLiveData) {
        this.changedAudioLiveData = changedAudioLiveData;
    }

    public MutableLiveData<PlayingInfo> getPlayingInfoLiveData() {
        return playingStatusLiveData;
    }

    public void setPlayingStatusLiveData(MutableLiveData<PlayingInfo> playingStatusLiveData) {
        this.playingStatusLiveData = playingStatusLiveData;
    }

    public MutableLiveData<Boolean> getPauseLiveData() {
        return pauseLiveData;
    }

    public void setPauseLiveData(MutableLiveData<Boolean> pauseLiveData) {
        this.pauseLiveData = pauseLiveData;
    }

    public MutableLiveData<Integer> getPlayModeLiveData() {
        return playModeLiveData;
    }

    public void setPlayModeLiveData(MutableLiveData<Integer> playModeLiveData) {
        this.playModeLiveData = playModeLiveData;
    }

    public JtPlayerControl() {
        init();
    }


    @Override
    public void init() {

        //注册播放器状态更新回调
        JtMediaPlayer.getInstance().setMediaPlayerCallBack(new JtMediaPlayer.IjtPlayerCallback() {
            @Override
            public void onStatusChanged(JtMediaPlayer.PlayerState state, JtMediaPlayer jtMediaPlayer, Object... args) {

                if (state == JtMediaPlayer.PlayerState.PREPARED) {
                    //准备就绪
                    getStateLiveDataLiveData().setValue(JtMediaPlayer.PlayerState.PREPARED);
                    Log.d(TAG, "播放器准备就绪");
                } else if (state == JtMediaPlayer.PlayerState.COMPLETE) {
                    if(JtMediaPlayer.getInstance().isPrepared()){
                        playingInfo.setProgress((100));
                        getPlayingInfoLiveData().setValue(playingInfo);
                    }
                }
            }

            @Override
            public void onProgressChanged(JtMediaPlayer jtMediaPlayer, int progress) {
                //更新播放进度
                playingInfo.setProgress(progress);
                getPlayingInfoLiveData().setValue(playingInfo);
                Log.d(TAG, "播放进度：" + playingInfo.getProgress());
            }
        });
    }

    public void release() {

        JtMediaPlayer.getInstance().release();
        getStateLiveDataLiveData().setValue(null);
        getPauseLiveData().setValue(false);
        getPlayingInfoLiveData().setValue(new PlayingInfo());
        getChangedAudioLiveData().setValue(new ChangedAudio());

    }

    @Override
    public void playNext() {

        play(playListManager.toNext());

    }

    @Override
    public void playPrevious() {
        play(playListManager.toPrevious());

    }

    @Override
    public boolean isInited() {
        return false;
    }

    @Override
    public void playOrPause(String url) {
        if (isPlaying()) {
            pause();
            isPause = true;
            //分发暂停状态
        } else {
            if (isPaused()) {
                resumePlay();
            } else {
                play(url);
            }
            isPause = false;
        }
        pauseLiveData.setValue(isPause);
    }

    private void resumePlay() {
        JtMediaPlayer.getInstance().resumePlay();
    }

    @Override
    public void playOrPause(AssetFileDescriptor fd) {
        if (isPlaying()) {
            pause();
            isPause = true;
            //分发暂停状态
        } else {
            play(fd);
            isPause = false;
        }
        pauseLiveData.setValue(isPause);
    }

    public void changeSpeed(float speed) {
        try {
            if (JtMediaPlayer.getInstance().isInited()) {
                // this checks on API 23 and up
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (JtMediaPlayer.getInstance().getMediaPlayer().isPlaying()) {
                        JtMediaPlayer.getInstance().getMediaPlayer().setPlaybackParams(JtMediaPlayer.getInstance().getMediaPlayer().getPlaybackParams().setSpeed(speed));
                    } else {
                        JtMediaPlayer.getInstance().getMediaPlayer().setPlaybackParams(JtMediaPlayer.getInstance().getMediaPlayer().getPlaybackParams().setSpeed(speed));
                        JtMediaPlayer.getInstance().getMediaPlayer().pause();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /***
     * 播放
     * @param
     */
    private void play(Playable playable) {
        if (playable != null) {
            play(playable.getUrl());
            if(getPlayingInfoLiveData().getValue()==null){
                PlayingInfo pInfo = new PlayingInfo();
                getPlayingInfoLiveData().setValue(pInfo);
            }
            getPlayingInfoLiveData().getValue().setPlayable(playable);
        }
    }


    /***
     * 播放
     * @param url
     */
    private void play(String url) {

        JtMediaPlayer.getInstance().play(url);

    }

    private void play(AssetFileDescriptor fd) {

        JtMediaPlayer.getInstance().play(fd);

    }

    private void pause() {
        JtMediaPlayer.getInstance().getMediaPlayer().pause();
    }

    public boolean isPlaying() {
        return JtMediaPlayer.getInstance().isPlaying();
    }

    @Override
    public boolean isPaused() {
        return isPause;
    }

    /***
     * 跳播
     * @param progress
     */
    @Override
    public void seekPlay(int progress) {

        int newPostion = (int) (JtMediaPlayer.getInstance().getMediaPlayer().getDuration() * (progress / 100.0f));
        JtMediaPlayer.getInstance().getMediaPlayer().seekTo(newPostion);
    }


    public MutableLiveData<Enum> getStateLiveDataLiveData() {
        return stateLiveData;
    }

    public void setStateLiveData(MutableLiveData<Enum> stateLiveData) {
        this.stateLiveData = stateLiveData;
    }

    @Override
    public void loadPlayList(PlayList pList) {
        this.playListManager = new PlayListManager(pList);

    }
}
