package com.kunminx.puremusic.cust;

import android.content.res.AssetFileDescriptor;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.kunminx.player.cust.JtMediaPlayer;
import com.kunminx.player.cust.JtPlayerControl;
import com.kunminx.puremusic.R;

import java.io.IOException;

public class DemoActivity extends AppCompatActivity {

    public static String TAG = DemoActivity.class.getSimpleName();

    private JtPlayerControl mPlayer;

    private Button mBtnStart;
    private TextView mTvOuput;
    private SeekBarAndText mSeekBar;

    private AssetFileDescriptor fd ;

    //离线模式播放
    private boolean isOfflineMode = false;

    private String url =
           // "http://192.25.109.64/chenyong/workspace/-/raw/master/%E9%80%82%E8%80%81%E5%8C%96-%E8%AF%AD%E9%9F%B3%E6%92%AD%E6%8A%A5/assets/audio/female_md.mp3";
            // "http://downsc.chinaz.net/Files/DownLoad/sound1/201906/11582.mp3";
              "http://27.151.112.180:8005/ulb3/common/tts/male_mt.mp3" ;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initPlayList();
        initView();
        initPlayer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getPlayerControl().release();

    }



    private void initPlayList(){
        try {
            fd = getAssets().openFd("male_01.mp3");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initView(){

        this.setContentView(R.layout.activity_cust);
        mBtnStart = this.findViewById(R.id.btn_start);
        mBtnStart.setOnClickListener(view -> {
            //播放
            if(isOfflineMode){
                getPlayerControl().playOrPause(fd);
            }else{
                getPlayerControl().playOrPause(url);
            }

        });
        mTvOuput = this.findViewById(R.id.tv_output);
        mTvOuput.setText("点击播放吧！");


        //SeekBar
        mSeekBar = (SeekBarAndText)findViewById(R.id.music_seek_bar);
        mSeekBar.setSongTimeCallBack(new SeekBarAndText.SongTimeCallBack() {
            @Override
            public String getSongTime(int progress) {
                return ""+progress;
            }

            @Override
            public String getDrawText() {
                return getPlayerControl().getProgressText();
            }
        });
        mSeekBar.setOnSeekBarChangeListener(new SeekBarAndText.OnSeekBarAndtextChangeListener() {
            @Override
            public void onProgress(SeekBar seekBar, int progress, float indicatorOffset) {
                Log.d(TAG,"onProgress:"+progress+","+indicatorOffset);
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromuser) {
                Log.d(TAG,"onProgressChanged:"+progress+","+fromuser);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Log.d(TAG,"onStartTrackingTouch");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d(TAG,"onStopTrackingTouch");
                //progress to seconds
                int position = seekBar.getProgress() ;
                doPlayBySeekChange(position);
            }
        });
        initSpeedOptions();
    }

    private void  doPlayBySeekChange(int progress) {
        getPlayerControl().seekPlay(progress);

    }



    //TODO 单例实现
    private  synchronized JtPlayerControl getPlayerControl(){
        if(mPlayer == null) {
            return  initPlayer();
        }
        return mPlayer;
    }

    private JtPlayerControl initPlayer(){

        mPlayer = new JtPlayerControl();

        mPlayer.getPauseLiveData().observe(this,  aBoolean ->{
            String tips = "";
            if(aBoolean){
                tips="播放停止";
            }else{
                tips="播放开始";
            }
            Toast.makeText(this,tips,Toast.LENGTH_SHORT).show();
            mTvOuput.setText(tips);
            Log.i("Demo","播放开始 " );
        });

        mPlayer.getPlayingInfoLiveData().observe(this, playingInfo -> {
            if(playingInfo!=null){
                mTvOuput.setText("播放进度："+playingInfo.getProgress()+"%");
                Log.i("Demo","播放进度："+playingInfo.getProgress()+"%");

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    mSeekBar.setProgress(playingInfo.getProgress(),false);
                }else{
                    mSeekBar.setProgress(playingInfo.getProgress());
                }

            }
        });

        mPlayer.getStateLiveDataLiveData().observe(this,state -> {
            if(state == JtMediaPlayer.PlayerState.PREPARED){
                //更新进度条
                mSeekBar.postInvalidate();
            }else if(state == JtMediaPlayer.PlayerState.COMPLETE){
               mSeekBar.handlerOnComplate();
            }

        });


        return mPlayer;
    }



    private String[] getSpeedStrings() {
        return new String[]{"1.0", "1.2", "1.4", "1.6", "1.8", "2.0"};
    }

    /***
     * 播放速度设置
     */
    private void initSpeedOptions() {
        final Spinner speedOptions = (Spinner)findViewById(R.id.spi_speed);
        String[] speeds = getSpeedStrings();

        ArrayAdapter<String> arrayAdapter =  new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, speeds);
        speedOptions.setAdapter(arrayAdapter);

        // change player playback speed if a speed is selected
        speedOptions.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {


                    float selectedSpeed = Float.parseFloat(
                            speedOptions.getItemAtPosition(i).toString());

                    mPlayer.changeSpeed(selectedSpeed);

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }




}