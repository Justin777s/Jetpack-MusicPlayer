package com.kunminx.player.cust;

import android.content.res.AssetFileDescriptor;

import com.kunminx.player.cust.data.PlayList;

/***
 * 播放器接口
 */
public interface IJtPlayerControl {


    void init();

    /***
     * 加载播放列表
     * @param pList
     */
    void loadPlayList(PlayList pList);


    void playOrPause(String url);
    void playOrPause(AssetFileDescriptor fd);


    void seekPlay(int progress);
    void release();

    void playNext();
    void playPrevious();



    boolean isInited();
    boolean isPlaying();
    boolean isPaused();

}
