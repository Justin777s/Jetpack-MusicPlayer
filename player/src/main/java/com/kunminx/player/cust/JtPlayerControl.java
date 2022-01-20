package com.kunminx.player.cust;


import android.content.res.AssetFileDescriptor;
import android.os.Build;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.kunminx.player.cust.data.ChangedAudio;
import com.kunminx.player.cust.data.PlayingInfo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 提供播放控制相关的事件，以及状态更新的通知回调（LiveData）
 */
public class JtPlayerControl {


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

    private PlayingInfo  playingInfo = new PlayingInfo();
    private ChangedAudio changedAudio = new ChangedAudio();
    private Boolean isPause = false ;
    private Boolean isPrepare = false ;



    //TODO
    public String getProgressText(){

        String result = "";
        try {
            //TODO 未执行初始化，这里的总时长需要外部传入 未prepared的时候返回 5832704
            if(JtMediaPlayer.getInstance().getMediaPlayer().getDuration()>5000000){
                result="00:10";
            }else{
                result = intToString(JtMediaPlayer.getInstance().getMediaPlayer().getCurrentPosition(),null)
                        +":"+intToString( JtMediaPlayer.getInstance().getMediaPlayer().getDuration(),null);
            }

            Log.d(TAG,JtMediaPlayer.getInstance().getMediaPlayer().getCurrentPosition()+","+ JtMediaPlayer.getInstance().getMediaPlayer().getDuration());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return result ;
    }

    // currentTime要转换的long类型的时间
    public static String intToString(int currentTime, String formatType)
            throws ParseException {
        if(formatType==null){
            formatType= "mm:ss";
        }
        Date date = intToDate(currentTime, formatType); // int类型转成Date类型
        String strTime = dateToString(date, formatType); // date类型转成String
        return strTime;
    }

    // strTime要转换的string类型的时间，formatType要转换的格式yyyy-MM-dd HH:mm:ss//yyyy年MM月dd日
    // HH时mm分ss秒，
    // strTime的时间格式必须要与formatType的时间格式相同
    public static Date stringToDate(String strTime, String formatType)
            throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat(formatType, Locale.CHINA);
        Date date = null;
        if(strTime==null){
            return null;
        }
        date = formatter.parse(strTime);
        return date;
    }

    // formatType格式为yyyy-MM-dd HH:mm:ss//yyyy年MM月dd日 HH时mm分ss秒
    // data Date类型的时间
    public static String dateToString(Date data, String formatType) {
        return new SimpleDateFormat(formatType, Locale.CHINA).format(data);
    }

    // currentTime要转换的long类型的时间
    // formatType要转换的时间格式yyyy-MM-dd HH:mm:ss//yyyy年MM月dd日 HH时mm分ss秒
    public static Date intToDate(long currentTime, String formatType)
            throws ParseException {
        Date dateOld = new Date(currentTime); // 根据long类型的毫秒数生命一个date类型的时间
        String sDateTime = dateToString(dateOld, formatType); // 把date类型的时间转换为string
        Date date = stringToDate(sDateTime, formatType); // 把String类型转换为Date类型
        return date;
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

    public JtPlayerControl(){
        init();
    }



    public void init() {

        //注册播放器状态更新回调
        JtMediaPlayer.getInstance().setMediaPlayerCallBack(new JtMediaPlayer.JtMediaPlayerCallBack() {
            @Override
            public void callBack(JtMediaPlayer.PlayerState state, JtMediaPlayer jtMediaPlayer, Object... args) {

                if(state == JtMediaPlayer.PlayerState.PROGRESS){
                    //更新播放进度
                    if(args!=null&&args.length==1&&args[0]instanceof Integer){
                        playingInfo.setProgress(((Integer) args[0]).intValue());
                        getPlayingInfoLiveData().setValue(playingInfo);
                        Log.d(TAG,"播放进度："+playingInfo.getProgress());
                    }
                }else if(state == JtMediaPlayer.PlayerState.PREPARED){
                         //准备就绪

                    getStateLiveDataLiveData().setValue(JtMediaPlayer.PlayerState.PREPARED);
                    Log.d(TAG,"播放器准备就绪");
                }else if(state == JtMediaPlayer.PlayerState.COMPLETE){
                    playingInfo.setProgress((100));
                    getPlayingInfoLiveData().setValue(playingInfo);
                }

            }
        });
    }

    public void release(){

        JtMediaPlayer.getInstance().release();
        getStateLiveDataLiveData().setValue(null);
        getPauseLiveData().setValue(false);
        getPlayingInfoLiveData().setValue(new PlayingInfo());
        getChangedAudioLiveData().setValue(new ChangedAudio());

    }

    public void playOrPause(String url){
        if(isPlaying()){
            pause();
            isPause = true;
            //分发暂停状态
        }else{
            play(url);
            isPause = false;
        }
        pauseLiveData.setValue(isPause);
    }

    public void playOrPause(AssetFileDescriptor fd){
        if(isPlaying()){
            pause();
            isPause = true;
            //分发暂停状态
        }else{
            play(fd);
            isPause = false;
        }
        pauseLiveData.setValue(isPause);
    }

    public void changeSpeed(float speed) {
            try{
                if( JtMediaPlayer.getInstance().isInited()) {
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
            }catch (Exception e){
                e.printStackTrace();
            }
    }

    /***
     * 播放
     * @param url
     */
    private void play(String url){
        JtMediaPlayer.getInstance().play(url);
    }

    private void play(AssetFileDescriptor fd){
        JtMediaPlayer.getInstance().play(fd);
    }

    private void pause(){
        JtMediaPlayer.getInstance().getMediaPlayer().pause();
    }

    public boolean isPlaying(){
        return JtMediaPlayer.getInstance().isPlaying();
    }

    /***
     * 跳播
     * @param progress
     */
    public void seekPlay(int progress){

        int newPostion = (int) (JtMediaPlayer.getInstance().getMediaPlayer().getDuration() * (progress / 100.0f));
        JtMediaPlayer.getInstance().getMediaPlayer().seekTo(newPostion);
    }


    public MutableLiveData<Enum> getStateLiveDataLiveData() {
        return stateLiveData;
    }

    public void setStateLiveData(MutableLiveData<Enum> stateLiveData) {
        this.stateLiveData = stateLiveData;
    }
}
