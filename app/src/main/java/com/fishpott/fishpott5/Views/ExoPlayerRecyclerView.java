package com.fishpott.fishpott5.Views;

import android.content.Context;
import android.graphics.Point;
import android.net.Uri;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.fishpott.fishpott5.Inc.Config;
import com.fishpott.fishpott5.Models.Vertical_NewsType_Model;
import com.fishpott.fishpott5.R;
import com.fishpott.fishpott5.Util.VideoPlayerConfig;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultAllocator;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.util.ArrayList;
import java.util.List;

public class ExoPlayerRecyclerView extends RecyclerView {
    private static final String TAG = "ExoPlayerRecyclerView";

    private List<Vertical_NewsType_Model> videoInfoList = new ArrayList<>();
    public static int videoSurfaceDefaultHeight = 0;
    public static int screenDefaultHeight = 0;
    SimpleExoPlayer player;
    private PlayerView videoSurfaceView;
    private ImageView mCoverImage, mPlayImageView;
    private ProgressBar mProgressBar;
    private Context appContext;
    public static int playPosition = -1;
    private boolean addedVideo = false;
    private View rowParent;

    public ExoPlayerRecyclerView(Context context) {
        super(context);
        initialize(context);
    }

    public ExoPlayerRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public ExoPlayerRecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context);
    }

    public void setVideoInfoList(List<Vertical_NewsType_Model> videoInfoList) {
        this.videoInfoList = videoInfoList;
    }

    public SimpleExoPlayer getPlayer() {
        return player;
    }

    /**
     * prepare for video play
     */
    //remove the player from the row
    private void removeVideoView(PlayerView videoView) {
        ViewGroup parent = (ViewGroup) videoView.getParent();
        if (parent == null) {
            return;
        }
        int index = parent.indexOfChild(videoView);
        if (index >= 0) {
            parent.removeViewAt(index);
            parent.findViewById(R.id.cover).setVisibility(VISIBLE);
            parent.findViewById(R.id.play_icon).setVisibility(VISIBLE);
            addedVideo = false;
        }
    }


    //play the video in the row
    public void playVideo() {

        int startPosition = ((LinearLayoutManager) getLayoutManager()).findFirstVisibleItemPosition();
        int endPosition = ((LinearLayoutManager) getLayoutManager()).findLastVisibleItemPosition();

        if (endPosition - startPosition > 1) {
            endPosition = startPosition + 1;
        }

        if (startPosition < 0 || endPosition < 0) {
            return;
        }

        int targetPosition;
        if (startPosition != endPosition) {
            int startPositionVideoHeight = getVisibleVideoSurfaceHeight(startPosition);
            int endPositionVideoHeight = getVisibleVideoSurfaceHeight(endPosition);
            targetPosition = startPositionVideoHeight > endPositionVideoHeight ? startPosition : endPosition;
        } else {
            targetPosition = startPosition;
        }

        if (targetPosition < 0 || targetPosition == playPosition) {
            return;
        }

        playPosition = targetPosition;

        if (videoSurfaceView == null) {
            return;
        }

        videoSurfaceView.setVisibility(INVISIBLE);
        removeVideoView(videoSurfaceView);

        // get target View targetPosition in RecyclerView
        int at = targetPosition - ((LinearLayoutManager) getLayoutManager()).findFirstVisibleItemPosition();

        View child = getChildAt(at);
        ViewHolder holder = getChildViewHolder(child);
        if (child == null) {
            return;
        }
        if (holder == null) {
            playPosition = -1;
            return;
        }

        if (holder.getItemViewType() != Config.NEWS_TYPE_5_TO_6_JUSTNEWSWITHVIDEOANDMAYBETEXT_VERTICAL_KEY
                && holder.getItemViewType() != Config.NEWS_TYPE_8_JUSTNEWSWITHURLWITHVIDEOANDMAYBETEXT_VERTICAL_KEY
                && holder.getItemViewType() != Config.NEWS_TYPE_8_REPOSTEDJUSTNEWSWITHURLWITHVIDEOANDMAYBETEXT_VERTICAL_KEY
                && holder.getItemViewType() != Config.NEWS_TYPE_17_SHARES4SALEWITHVIDEO_VERTICAL_KEY
                && holder.getItemViewType() != Config.NEWS_TYPE_5_TO_6_SPONSOREDJUSTNEWSWITHVIDEOANDMAYBETEXT_VERTICAL_KEY
                && holder.getItemViewType() != Config.NEWS_TYPE_5_TO_6_REPOSTEDNEWSWITHVIDEOANDMAYBETEXT_VERTICAL_KEY
                && holder.getItemViewType() != Config.NEWS_TYPE_17_REPOSTEDSHARES4SALEWITHVIDEO_VERTICAL_KEY
        ) {
            playPosition = -1;
            return;
        }

        mCoverImage = child.findViewById(R.id.cover);
        mProgressBar = child.findViewById(R.id.progressBar);
        mPlayImageView = child.findViewById(R.id.play_icon);
        FrameLayout frameLayout = child.findViewById(R.id.video_background);
        frameLayout.addView(videoSurfaceView);
        addedVideo = true;
        rowParent = child;

        videoSurfaceView.requestFocus();
        // Bind the player to the view.
        videoSurfaceView.setPlayer(player);

        // Measures bandwidth during playback. Can be null if not required.
        DefaultBandwidthMeter defaultBandwidthMeter = new DefaultBandwidthMeter();
        // Produces DataSource instances through which media data is loaded.

        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(appContext,
                Util.getUserAgent(appContext, "android_wave_list"), defaultBandwidthMeter);

        // This is the MediaSource representing the media to be played.

        if (videoInfoList.get(targetPosition).getNewsVideosLinksSeparatedBySpaces() != null) {
            MediaSource videoSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(Uri.parse(videoInfoList.get(targetPosition).getNewsVideosLinksSeparatedBySpaces().trim()));
            // Prepare the player with the source.
            player.prepare(videoSource);
            mPlayImageView.setVisibility(INVISIBLE);
            player.setPlayWhenReady(true);
        }


    }

    private int getVisibleVideoSurfaceHeight(int playPosition) {
        int at = playPosition - ((LinearLayoutManager) getLayoutManager()).findFirstVisibleItemPosition();

        View child = getChildAt(at);
        if (child == null) {
            return 0;
        }

        int[] location01 = new int[2];
        child.getLocationInWindow(location01);

        if (location01[1] < 0) {
            return location01[1] + videoSurfaceDefaultHeight;
        } else {
            return screenDefaultHeight - location01[1];
        }
    }


    private void initialize(Context context) {

        appContext = context.getApplicationContext();
        Display display = ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        videoSurfaceDefaultHeight = point.x;

        screenDefaultHeight = point.y;
        videoSurfaceView = new PlayerView(appContext);
        videoSurfaceView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);

        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector =
                new DefaultTrackSelector(videoTrackSelectionFactory);
        LoadControl loadControl = new DefaultLoadControl(
                new DefaultAllocator(true, 16),
                VideoPlayerConfig.MIN_BUFFER_DURATION,
                VideoPlayerConfig.MAX_BUFFER_DURATION,
                VideoPlayerConfig.MIN_PLAYBACK_START_BUFFER,
                VideoPlayerConfig.MIN_PLAYBACK_RESUME_BUFFER, -1, true);

        // 2. Create the player
        player = ExoPlayerFactory.newSimpleInstance(appContext, trackSelector, loadControl);
        // Bind the player to the view.
        videoSurfaceView.setUseController(false);
        videoSurfaceView.setPlayer(player);

        addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                //if (newState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && Config.getSharedPreferenceBoolean(getContext(), Config.SHARED_PREF_KEY_USER_SETTINGS_AUTOPLAY_VIDEOS)) {
                //    playVideo();
                //}
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                Boolean PLAY_VIDEO =  false;
                if(
                        (
                                (playPosition - ((LinearLayoutManager) getLayoutManager()).findLastVisibleItemPosition()) == 1
                                        || (playPosition - ((LinearLayoutManager) getLayoutManager()).findLastVisibleItemPosition()) == 0
                                        || (playPosition - ((LinearLayoutManager) getLayoutManager()).findLastVisibleItemPosition()) == -1
                        )

                        ){
                    PLAY_VIDEO = true;
                }

                if(PLAY_VIDEO){
                    if(
                            (
                                    (playPosition - ((LinearLayoutManager) getLayoutManager()).findFirstVisibleItemPosition()) == 1
                                            || (playPosition - ((LinearLayoutManager) getLayoutManager()).findFirstVisibleItemPosition()) == 0
                                            || (playPosition - ((LinearLayoutManager) getLayoutManager()).findFirstVisibleItemPosition()) == -1
                            )

                            ){
                        PLAY_VIDEO = true;
                    } else {
                        PLAY_VIDEO = false;
                    }
                }

                Log.e("SCROLLVIDEOPLAYER", "PLAYER-STATE :  " + String.valueOf(player.getPlaybackState()));
                if(PLAY_VIDEO){
                    setVideoPlayerStatus(1);
                    Log.e("SCROLLVIDEOPLAYER", "HERE 5 ");
                } else {
                    setVideoPlayerStatus(0);
                    Log.e("SCROLLVIDEOPLAYER", "HERE 6");
                }
            }
        });

        addOnChildAttachStateChangeListener(new OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(View view) {
                Log.e("SCROLLVIDEOPLAYER", "CHILD ATTACHED");
                    Boolean PLAY_VIDEO =  false;

                    Log.e("SCROLLVIDEOPLAYER", "===============================================================================");
                    Log.e("SCROLLVIDEOPLAYER", "PLAY POSITION: " + String.valueOf(ExoPlayerRecyclerView.playPosition));
                    Log.e("SCROLLVIDEOPLAYER", "FIRST VISIBLE POSITION: " + String.valueOf(((LinearLayoutManager) getLayoutManager()).findFirstVisibleItemPosition()));
                    Log.e("SCROLLVIDEOPLAYER", "LAST POSITION: " + String.valueOf(((LinearLayoutManager) getLayoutManager()).findLastVisibleItemPosition()));

                    if(
                            (
                                    (playPosition - ((LinearLayoutManager) getLayoutManager()).findLastVisibleItemPosition()) == 1
                                            || (playPosition - ((LinearLayoutManager) getLayoutManager()).findLastVisibleItemPosition()) == 0
                                            || (playPosition - ((LinearLayoutManager) getLayoutManager()).findLastVisibleItemPosition()) == -1
                            )

                            ){
                        PLAY_VIDEO = true;
                    }

                    if(PLAY_VIDEO){
                        if(
                                (
                                        (playPosition - ((LinearLayoutManager) getLayoutManager()).findFirstVisibleItemPosition()) == 1
                                                || (playPosition - ((LinearLayoutManager) getLayoutManager()).findFirstVisibleItemPosition()) == 0
                                                || (playPosition - ((LinearLayoutManager) getLayoutManager()).findFirstVisibleItemPosition()) == -1
                                )

                                ){
                            PLAY_VIDEO = true;
                        } else {
                            PLAY_VIDEO = false;
                        }
                    }

                    Log.e("SCROLLVIDEOPLAYER", "PLAYER-STATE :  " + String.valueOf(player.getPlaybackState()));
                    if(PLAY_VIDEO){
                        player.setPlayWhenReady(true);
                        Log.e("SCROLLVIDEOPLAYER", "HERE 1 ");
                    } else {
                        player.setPlayWhenReady(false);
                        Log.e("SCROLLVIDEOPLAYER", "HERE 2");
                    }

            }

            @Override
            public void onChildViewDetachedFromWindow(View view) {
                Log.e("SCROLLVIDEOPLAYER", "CHILD DETTACHED");
                if (addedVideo && rowParent != null && rowParent.equals(view)) {
                    player.setPlayWhenReady(false);

                    //removeVideoView(videoSurfaceView);
                    //playPosition = -1;
                    //videoSurfaceView.setVisibility(INVISIBLE);
                }

            }
        });
        player.addListener(new Player.EventListener() {
            @Override
            public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {

            }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

            }

            @Override
            public void onLoadingChanged(boolean isLoading) {

            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                switch (playbackState) {

                    case Player.STATE_BUFFERING:
                        //videoSurfaceView.setAlpha(0.9f);
                        if (mProgressBar != null) {
                            mProgressBar.setVisibility(VISIBLE);
                        }
                        if (mPlayImageView != null) {
                            mPlayImageView.setVisibility(GONE);
                        }
                        videoSurfaceView.setKeepScreenOn(true);
                        break;
                    case Player.STATE_ENDED:
                        player.seekTo(0);
                        videoSurfaceView.setKeepScreenOn(false);
                        break;
                    case Player.STATE_IDLE:
                        if (mPlayImageView != null) {
                            mPlayImageView.setVisibility(INVISIBLE);
                        }
                        videoSurfaceView.setKeepScreenOn(false);
                        break;

                    case Player.STATE_READY:
                        if (mProgressBar != null) {
                            mProgressBar.setVisibility(GONE);
                        }
                        if (mPlayImageView != null) {
                            mPlayImageView.setVisibility(INVISIBLE);
                        }
                        videoSurfaceView.setVisibility(VISIBLE);
                        videoSurfaceView.setAlpha(1);
                        videoSurfaceView.setKeepScreenOn(true);
                        mCoverImage.setVisibility(GONE);

                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onRepeatModeChanged(int repeatMode) {

            }

            @Override
            public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {

            }

            @Override
            public void onPositionDiscontinuity(int reason) {

            }

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

            }

            @Override
            public void onSeekProcessed() {

            }
        });


    }


    public void setVideoPlayerStatus(int status){
        if(status == 0) {
            if (mProgressBar != null) {
                mProgressBar.setVisibility(GONE);
            }
            if (videoSurfaceView != null) {
                videoSurfaceView.setVisibility(GONE);
            }
            if (mPlayImageView != null) {
                mPlayImageView.setVisibility(VISIBLE);
            }
            if (mCoverImage != null) {
                mCoverImage.setVisibility(VISIBLE);
            }
            Log.e("SCROLLVIDEOPLAYER", "HERE 3");
        } else {
            if (mProgressBar != null) {
                mProgressBar.setVisibility(GONE);
            }
            if (mCoverImage != null) {
                mCoverImage.setVisibility(GONE);
            }
            if (mPlayImageView != null) {
                mPlayImageView.setVisibility(INVISIBLE);
            }

            Log.e("SCROLLVIDEOPLAYER", "HERE 4");
            videoSurfaceView.setVisibility(VISIBLE);
            videoSurfaceView.setAlpha(1);
        }

    }


    public void onPausePlayer() {
        if (videoSurfaceView != null) {
            removeVideoView(videoSurfaceView);
            player.release();
            videoSurfaceView = null;
        }
    }

    public void onRestartPlayer() {
        if (videoSurfaceView == null) {
            playPosition = -1;
            playVideo();
        }
    }

    /**
     * release memory
     */
    public void onRelease() {

        if (player != null) {
            player.release();
            player = null;
        }

        rowParent = null;
    }



}

