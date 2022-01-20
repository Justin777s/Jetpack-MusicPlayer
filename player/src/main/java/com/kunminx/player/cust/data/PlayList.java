package com.kunminx.player.cust.data;

import java.util.ArrayList;
import java.util.List;

/***
 * 播放列表
 */
public class PlayList {

    private List<Playable> list ;


    public PlayList(){
        list = new ArrayList<>();
    }

    public List<Playable> getList() {
        return list;
    }

    public void setList(List<Playable> list) {
        this.list = list;
    }

}
