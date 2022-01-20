package com.kunminx.player.cust;


import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/***
 * 1.封装MediaPlayer操作
 * 2.衔接播放控制器
 **/
public class JtMediaPlayer implements
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnBufferingUpdateListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnInfoListener,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnSeekCompleteListener,
        MediaPlayer.OnVideoSizeChangedListener{

    public static final String TAG = JtMediaPlayer.class.getSimpleName();


    private static JtMediaPlayer instance;

    private MediaPlayer mediaPlayer ;

    private JtMediaPlayerCallBack mCallBack;

    //是否已经初始化
    private boolean isInited = false;


    //默认支持的文件格式
    private String[] ext = {
            ".m4a",
            ".3gp",
            ".mp4",
            ".mp3",
            ".wma",
            ".ogg",
            ".wav",
            ".mid"
    };


    private Handler mRefreshHandler = new Handler();

    public void release(){

        mRefreshHandler.removeCallbacks(mCalcProgressRunnable);
        mediaPlayer.stop();
        this.isInited = false;
    }

    private Runnable mCalcProgressRunnable = new Runnable() {
        @Override
        public void run() {
            //先移除历史线程的回调
            mRefreshHandler.removeCallbacks(mCalcProgressRunnable);

            //获取播放进度
            if(mediaPlayer!=null && mediaPlayer.isPlaying()){
                int progress =(int) 100.0f * mediaPlayer.getCurrentPosition() / mediaPlayer.getDuration();
                Log.v(TAG,  "进度："+progress);
                doCallBack(PlayerState.PROGRESS,new Integer(progress));
            }

            //递归执行
            mRefreshHandler.postDelayed(mCalcProgressRunnable,500);

        }
    };


    public JtMediaPlayer setMediaPlayerCallBack(JtMediaPlayerCallBack mCallBack) {
        this.mCallBack = mCallBack;
        return instance;
    }

    public MediaPlayer getMediaPlayer(){
        return mediaPlayer;
    }

    public boolean isInited() {
        return isInited;
    }

    public void setInited(boolean inited) {
        isInited = inited;
    }


    /**
     * 播放器状态枚举
     */
    public enum PlayerState {
        PREPARED("MediaPlayer--准备完毕"),
        COMPLETE("MediaPlayer--播放结束"),
        ERROR("MediaPlayer--播放错误"),
        EXCEPTION("MediaPlayer--播放异常"),
        INFO("MediaPlayer--播放开始"),
        PROGRESS("MediaPlayer--播放进度回调"),
        SEEK_COMPLETE("MediaPlayer--拖动到尾端"),
        VIDEO_SIZE_CHANGE("MediaPlayer--读取视频大小"),
        BUFFER_UPDATE("MediaPlayer--更新流媒体缓存状态"),
        FORMATE_NOT_SURPORT("MediaPlayer--音视频格式可能不支持")
/*        SURFACEVIEW_NULL("SurfaceView--还没初始化"),
        SURFACEVIEW_NOT_ARREADY("SurfaceView--还没准备好"),
        SURFACEVIEW_CHANGE("SurfaceView--Holder改变"),
        SURFACEVIEW_CREATE("SurfaceView--Holder创建"),
        SURFACEVIEW_DESTROY("SurfaceView--Holder销毁")*/
        ;
        private final String desc ;

        PlayerState(String var3) {
            this.desc  = var3;
        }

        public String toString() {
            return this.desc ;
        }
    }


    public JtMediaPlayer(){
        init();
    }



    private void init(){
        this.mediaPlayer = new MediaPlayer();
        this.mediaPlayer.setOnCompletionListener(this);
        this.mediaPlayer.setOnBufferingUpdateListener(this);
        this.mediaPlayer.setOnErrorListener(this);
        this.mediaPlayer.setOnInfoListener(this);
        this.mediaPlayer.setOnPreparedListener(this);
        this.mediaPlayer.setOnSeekCompleteListener(this);
     //   this.mediaPlayer.setOnVideoSizeChangedListener(this);

    }

    public synchronized static JtMediaPlayer getInstance(){
        if(instance == null ){
            instance = new JtMediaPlayer();
        }
        return instance;
    }

    /***
     * 提供给外部的播放方法
     * @param fd  本地播放地址
     * @return
     */
    public boolean play(AssetFileDescriptor  fd){

        if(fd==null) {
            return false;
        }
        try {
            /**
             * 其实仔细观察优酷app切换播放网络视频时的确像是这样做的：先暂停当前视频，
             * 让mediaplayer与先前的surfaceHolder脱离“绑定”,当mediaplayer再次准备好要start时，
             * 再次让mediaplayer与surfaceHolder“绑定”在一起，显示下一个要播放的视频。
             * 注：MediaPlayer.setDisplay()的作用： 设置SurfaceHolder用于显示的视频部分媒体。
             */
            mediaPlayer.setDisplay(null);
            mediaPlayer.reset();
            mediaPlayer.setDataSource(fd.getFileDescriptor(),fd.getStartOffset(),fd.getLength());
            setInited(true)  ;
            mediaPlayer.prepareAsync(); //播放器进入prepared状态 回调之后，才能调用start进入isPlaying状态
        } catch (Exception e) {
            e.printStackTrace();
            doCallBack( PlayerState.ERROR, mediaPlayer);
            return false;
        }
        return true;

    }

    /***
     * 提供给外部的播放方法
     * @param url  在线播放文件地址
     * @return
     */
    public boolean play(String url){

        if(!checkAvalable(url)) {
            return false;
        }
        try {
            /**
             * 其实仔细观察优酷app切换播放网络视频时的确像是这样做的：先暂停当前视频，
             * 让mediaplayer与先前的surfaceHolder脱离“绑定”,当mediaplayer再次准备好要start时，
             * 再次让mediaplayer与surfaceHolder“绑定”在一起，显示下一个要播放的视频。
             * 注：MediaPlayer.setDisplay()的作用： 设置SurfaceHolder用于显示的视频部分媒体。
             */
            mediaPlayer.setDisplay(null);
            mediaPlayer.reset();
            mediaPlayer.setDataSource(url);
            setInited(true)  ;
            mediaPlayer.prepareAsync(); //播放器进入prepared状态 回调之后，才能调用start进入isPlaying状态
        } catch (Exception e) {
            e.printStackTrace();
            doCallBack( PlayerState.ERROR, mediaPlayer);
            return false;
        }
        return true;

    }

    /**
     * 检查是否可以播放
     *
     * @param path 参数
     * @return 结果
     */
    private boolean checkAvalable(String path) {
        boolean surport = false;
        for (int i = 0; i < ext.length; i++) {
            if (path.toLowerCase().endsWith(ext[i])) {
                surport = true;
            }
        }
        if (!surport) {
            doCallBack( PlayerState.FORMATE_NOT_SURPORT, this.mediaPlayer);
            Log.v(TAG,  PlayerState.FORMATE_NOT_SURPORT.toString());
            return false;
        }
        return true;
    }

    public boolean isPlaying(){
        return this.mediaPlayer.isPlaying();
    }




    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        //缓存状态更新
        Log.i(TAG,"onBufferingUpdate:"+percent);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        //播放完成
        Log.i(TAG,"onCompletion:"+mp.getCurrentPosition());
        //回调进度，独立更新进度，不与播放完成共用状态。
       // doCallBack(PlayerState.PROGRESS,new Integer(100));
        //回调播放完成
        doCallBack(PlayerState.COMPLETE,mp);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        //播放异常
        Log.v(TAG,"onError:"+mp.getCurrentPosition());
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        //播放开始
        Log.v(TAG,"onInfo:"+mp.getCurrentPosition());
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {

        //准备完毕
        Log.v(TAG,"onPrepared:"+mp.getCurrentPosition());
        try{
            //由于只有音频播放，所以不需要绑定宿主surfaceHolder
            //mediaPlayer.setDisplay(null);
            //实际的启动播放代码
            mediaPlayer.start();
            mRefreshHandler.postDelayed(mCalcProgressRunnable,1000);

        }catch (Exception e){
            e.printStackTrace();
            Log.v(TAG,"onPrepared error："+e.getMessage());
            doCallBack(PlayerState.ERROR, mp);
        }
        //更新播放准备完毕状态
        doCallBack(PlayerState.PREPARED,mp);

    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        //拖动到尾端
        Log.v(TAG,"onSeekComplete:"+mp.getCurrentPosition());
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        // a callback to be invoked when the video size is known or updated
        //这里主要应用于获得播放视频尺寸大小，用不到
        Log.v(TAG,"onVideoSizeChanged:"+width+","+height);
    }

    private void doCallBack(PlayerState state , Object... args){
        if(mCallBack!=null){
            mCallBack.callBack(state,instance,args);
        }
    }



    /***
     * 回调接口定义，
     */
    public interface  JtMediaPlayerCallBack{
        /***
         *
         * @param state 状态
         * @param jtMediaPlayer 业务播放器对象
         * @param args  扩展参数
         */
        void callBack(PlayerState state, JtMediaPlayer jtMediaPlayer, Object... args) ;
    }



}
