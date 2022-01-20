package com.kunminx.puremusic.cust;

import android.content.Intent;
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
import com.kunminx.player.cust.data.AudioItem;
import com.kunminx.player.cust.data.PlayList;
import com.kunminx.puremusic.MainActivity;
import com.kunminx.puremusic.R;

public class DemoActivity extends AppCompatActivity {

    public static String TAG = DemoActivity.class.getSimpleName();


    private Button mBtnStart;
    private TextView mTvOuput;
    private TextView mTvTitle;
    private SeekBarAndText mSeekBar;

    private AssetFileDescriptor fd;

    private JtPlayerControl mPlayer;

    //离线模式播放
    private boolean isOfflineMode = false;

    private String url =
            // "http://192.25.109.64/chenyong/workspace/-/raw/master/%E9%80%82%E8%80%81%E5%8C%96-%E8%AF%AD%E9%9F%B3%E6%92%AD%E6%8A%A5/assets/audio/female_md.mp3";
            // "http://downsc.chinaz.net/Files/DownLoad/sound1/201906/11582.mp3";
            "http://27.151.112.180:8005/ulb3/common/tts/male_mt.mp3";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initView();
        initPlayer();
        initPlayList();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getPlayerControl().release();

    }


    /***
     * TODO 有执行时序，需要收到逻辑类里面
     */
    private PlayList initPlayList() {
        PlayList playList = new PlayList();
        try {
            //fd = getAssets().openFd("male_01.mp3");

            playList.getList().add(new AudioItem("男声_01_male_01.mp3", "http://27.151.112.180:8005/ulb3/common/tts/male_01.mp3"));
            playList.getList().add(new AudioItem("男声_mt_male_mt.mp3", "http://27.151.112.180:8005/ulb3/common/tts/male_mt.mp3"));
            playList.getList().add(new AudioItem("女声_female_md.mp3", "http://27.151.112.180:8005/ulb3/common/tts/female_md.mp3"));


        } catch (Exception e) {
            e.printStackTrace();
        }

        return playList;

    }

    private void initView() {

        this.setContentView(R.layout.activity_cust);

        this.findViewById(R.id.btn_index).setOnClickListener(view -> {
            startActivity(new Intent(DemoActivity.this, MainActivity.class));
        });

        mTvTitle = this.findViewById(R.id.tv_playable_title);
        mBtnStart = this.findViewById(R.id.btn_start);
        mBtnStart.setOnClickListener(view -> {
            //播放
            if (isOfflineMode) {
                getPlayerControl().playOrPause(fd);
            } else {
                getPlayerControl().playOrPause(url);
            }

        });
        mTvOuput = this.findViewById(R.id.tv_output);
        mTvOuput.setText("点击播放吧！");


        //SeekBar
        mSeekBar = (SeekBarAndText) findViewById(R.id.music_seek_bar);
        mSeekBar.setProgress(0);
        mSeekBar.setSongTimeCallBack(new SeekBarAndText.SongTimeCallBack() {
            @Override
            public String getSongTime(int progress) {
                return "" + progress;
            }

            @Override
            public String getDrawText() {
                return getPlayerControl().getProgressText();
            }
        });
        mSeekBar.setOnSeekBarChangeListener(new SeekBarAndText.OnSeekBarAndtextChangeListener() {
            @Override
            public void onProgress(SeekBar seekBar, int progress, float indicatorOffset) {
                Log.d(TAG, "onProgress:" + progress + "," + indicatorOffset);
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromuser) {
                Log.d(TAG, "onProgressChanged:" + progress + "," + fromuser);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Log.d(TAG, "onStartTrackingTouch");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d(TAG, "onStopTrackingTouch");
                //progress to seconds
                int position = seekBar.getProgress();
                doPlayBySeekChange(position);
            }
        });


        this.findViewById(R.id.btn_next).setOnClickListener(view -> {
            getPlayerControl().playNext();
        });
        this.findViewById(R.id.btn_last).setOnClickListener(view -> {
            getPlayerControl().playPrevious();
        });

        initSpeedOptions();
    }

    private void doPlayBySeekChange(int progress) {
        getPlayerControl().seekPlay(progress);

    }


    private synchronized JtPlayerControl getPlayerControl() {
        if(mPlayer==null){
            mPlayer = new JtPlayerControl();
            mPlayer.loadPlayList(initPlayList());
        }
        return mPlayer;
    }

    private void initPlayer() {
        getPlayerControl().getPauseLiveData().observe(this, aBoolean -> {
            String tips = "";
            if (aBoolean) {
                tips = "播放停止";
            } else {
                tips = "播放开始";
            }
            Toast.makeText(this, tips, Toast.LENGTH_SHORT).show();
            mTvOuput.setText(tips);
            Log.i("Demo", "播放开始 ");
        });

        getPlayerControl().getPlayingInfoLiveData().observe(this, playingInfo -> {
            if (playingInfo != null) {

                //单独拿出来
                if(playingInfo.getPlayable()!=null){
                    mTvTitle.setText(playingInfo.getPlayable().getTitle());
                }
                mTvOuput.setText("播放进度：" + playingInfo.getProgress() + "%");
                Log.i("Demo", "播放进度：" + playingInfo.getProgress() + "%");

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    mSeekBar.setProgress(playingInfo.getProgress(), false);
                } else {
                    mSeekBar.setProgress(playingInfo.getProgress());
                }

            }
        });

        getPlayerControl().getStateLiveDataLiveData().observe(this, state -> {
            if (state == JtMediaPlayer.PlayerState.PREPARED) {
                //更新进度条
                 mSeekBar.postInvalidate();
            } else if (state == JtMediaPlayer.PlayerState.COMPLETE) {
                if(JtMediaPlayer.getInstance().isPrepared()){
                    mSeekBar.handlerOnComplate();
                }

            }

        });


    }


    private String[] getSpeedStrings() {
        return new String[]{"1.0", "1.2", "1.4", "1.6", "1.8", "2.0"};
    }

    /***
     * 播放速度设置
     */
    private void initSpeedOptions() {
        final Spinner speedOptions = (Spinner) findViewById(R.id.spi_speed);
        String[] speeds = getSpeedStrings();

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, speeds);
        speedOptions.setAdapter(arrayAdapter);

        speedOptions.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                float selectedSpeed = Float.parseFloat(
                        speedOptions.getItemAtPosition(i).toString());
                getPlayerControl().changeSpeed(selectedSpeed);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }


}