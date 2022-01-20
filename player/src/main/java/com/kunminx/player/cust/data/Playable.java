package com.kunminx.player.cust.data;

import java.io.Serializable;

public class Playable implements Serializable {

    private String title;
    private String url;
    private String description;

    private long duration ; // 毫秒

    public Playable(String title, String url){
        this.title = title;
        this.url = url;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


}
