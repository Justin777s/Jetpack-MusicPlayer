package com.kunminx.player.cust.data;

/***
 * 播放状态信息
 *
 */
public class PlayingInfo {


    private Playable playable;

    private int progress = 0;

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }


    public Playable getPlayable() {
        return playable;
    }

    public void setPlayable(Playable playable) {
        this.playable = playable;
    }
}
