package com.kunminx.player.cust;


import androidx.lifecycle.MutableLiveData;

import com.kunminx.player.cust.data.ChangedAudio;
import com.kunminx.player.cust.data.PlayingStatus;

/**
 * 提供播放控制相关的事件，以及状态更新的通知回调（LiveData）
 */
public class JtPlayerFacade {



    private MutableLiveData<ChangedAudio>  changedAudioLiveData = new MutableLiveData<>();
    //播放中的，状态信息，包括当前进度等
    private MutableLiveData<PlayingStatus>  playingStatusLiveData = new MutableLiveData<>();
    //暂停状态，控制播放器播放还是暂停
    private MutableLiveData<Boolean> pauseLiveData = new MutableLiveData<>();
    //播放模式
    private MutableLiveData<Integer> playModeLiveData = new MutableLiveData<>();



    public MutableLiveData<ChangedAudio> getChangedAudioLiveData() {
        return changedAudioLiveData;
    }

    public void setChangedAudioLiveData(MutableLiveData<ChangedAudio> changedAudioLiveData) {
        this.changedAudioLiveData = changedAudioLiveData;
    }

    public MutableLiveData<PlayingStatus> getPlayingStatusLiveData() {
        return playingStatusLiveData;
    }

    public void setPlayingStatusLiveData(MutableLiveData<PlayingStatus> playingStatusLiveData) {
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


}
