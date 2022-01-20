package com.kunminx.player.cust;

import com.kunminx.player.cust.data.PlayList;
import com.kunminx.player.cust.data.Playable;

/***
 * 播放列表管理
 */
public class PlayListManager {


    private PlayList playList;
    private int currentIdx = 0;//当前索引


    public PlayListManager(PlayList pList) {
        playList = pList;
    }

    public PlayListManager(PlayList pList, int initIndex) {
        playList = pList;
        currentIdx = initIndex;
    }

    public Playable getCurrentPlayable() throws NullPointerException {
        return getPlayable(currentIdx);
    }

    /***
     * 获取 播放对象
     * @return
     */
    private Playable getPlayable(int index) {
        if (playList != null && playList.getList().size() > index) {
            return playList.getList().get(index);
        } else {
            return null;
        }
    }


    /***
     * 获取当前播放对象
     * @return
     */
    public Playable toPrevious() throws NullPointerException {
        if (currentIdx > 0) {
            --currentIdx;
        }
        return getPlayable(currentIdx);
    }

    public Playable toNext() throws NullPointerException {
        if (hasNext(currentIdx)) {
            ++currentIdx;
        }
        return getPlayable(currentIdx);
    }

    private int getCount() {
        if (playList != null) {
            return playList.getList().size();
        }
        return 0;
    }

    private boolean hasNext(int index) {
        if ((index + 1) < (getCount())) {
            return true;
        } else {
            return false;
        }
    }

}
