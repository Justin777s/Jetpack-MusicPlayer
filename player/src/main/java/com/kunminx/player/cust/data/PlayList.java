package com.kunminx.player.cust.data;

import java.util.ArrayList;
import java.util.List;

/***
 * 播放列表
 */
public class PlayList {

    private List<Playable> list;


    public PlayList() {
        list = new ArrayList<>();
    }

    public List<Playable> getList() {
        return list;
    }

    public void setList(List<Playable> list) {
        this.list = list;
    }

    public String[] getArrays() {

        String[] results = new String[]{};
        if (list != null && list.size() > 0) {
            results = new String[list.size()];
        }

        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) == null) {
                results[i] = "";
            }
            results[i] = list.get(i).getTitle();
        }

        return results;


    }

}
