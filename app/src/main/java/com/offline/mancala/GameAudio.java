package com.offline.mancala;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.*;
import android.os.*;
import android.util.Log;
import java.util.*;

/** Bundled CC0 audio: streaming background music and low-latency effects. */
final class GameAudio {
    private final Context context;
    private final AudioManager manager;
    private final Handler handler=new Handler(Looper.getMainLooper());
    private final Map<String,Integer> sounds=new HashMap<>();
    private final Set<Integer> ready=new HashSet<>();
    private SoundPool effects;
    private MediaPlayer music;
    private boolean active,focused,ducked,released,prepared;
    boolean musicEnabled,soundEnabled;
    private final AudioManager.OnAudioFocusChangeListener focus=change->{
        if(change==AudioManager.AUDIOFOCUS_GAIN){focused=true;ducked=false;syncPlayback();}
        else if(change==AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK){ducked=true;syncPlayback();}
        else {focused=false;if(music!=null&&prepared&&music.isPlaying())music.pause();if(effects!=null)effects.autoPause();}
    };
    GameAudio(Context context,boolean musicEnabled,boolean soundEnabled){this.context=context;this.musicEnabled=musicEnabled;this.soundEnabled=soundEnabled;manager=(AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        AudioAttributes attrs=new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build();
        effects=new SoundPool.Builder().setMaxStreams(5).setAudioAttributes(attrs).build();effects.setOnLoadCompleteListener((pool,id,status)->{if(status==0)ready.add(id);else Log.e("MancalaAudio","Effect load failed: "+id);});
        for(String name:new String[]{"tap","stone","pickup","capture","win","loss","draw"})try(AssetFileDescriptor fd=context.getAssets().openFd("audio/"+name+".ogg")){sounds.put(name,effects.load(fd,1));}catch(Exception e){Log.e("MancalaAudio","Cannot load "+name,e);}
        try{music=new MediaPlayer();music.setAudioAttributes(attrs);try(AssetFileDescriptor fd=context.getAssets().openFd("audio/background.ogg")){music.setDataSource(fd.getFileDescriptor(),fd.getStartOffset(),fd.getLength());}music.setLooping(true);music.setOnPreparedListener(player->{prepared=true;syncPlayback();});music.setOnErrorListener((player,what,extra)->{prepared=false;Log.e("MancalaAudio","Music decoder error "+what);return true;});music.prepareAsync();}catch(Exception e){Log.e("MancalaAudio","Cannot load background music",e);}
    }
    void resume(){active=true;sync();}
    void pause(){active=false;sync();if(effects!=null)effects.autoPause();}
    void sync(){if(released)return;if(active&&(musicEnabled||soundEnabled)){if(!focused)focused=manager.requestAudioFocus(focus,AudioManager.STREAM_MUSIC,AudioManager.AUDIOFOCUS_GAIN)==AudioManager.AUDIOFOCUS_REQUEST_GRANTED;}else{manager.abandonAudioFocus(focus);focused=false;}if(!soundEnabled&&effects!=null)effects.autoPause();syncPlayback();}
    private void syncPlayback(){if(music==null||!prepared||released)return;music.setVolume(ducked?.035f:.16f,ducked?.035f:.16f);if(active&&focused&&musicEnabled){if(!music.isPlaying())music.start();}else if(music.isPlaying())music.pause();}
    void play(String name,float volume){Integer id=sounds.get(name);if(!released&&active&&focused&&soundEnabled&&id!=null&&ready.contains(id)){effects.play(id,volume,volume,1,0,1);Log.d("MancalaAudio","Played "+name);}}
    void tap(){play("tap",.35f);}
    void stone(){play("stone",.45f);}
    void pickup(){play("pickup",.4f);}
    void capture(){play("capture",.5f);}
    static String outcome(int playerScore,int opponentScore,boolean twoPlayers){return playerScore==opponentScore?"draw":twoPlayers||playerScore>opponentScore?"win":"loss";}
    void result(int playerScore,int opponentScore,boolean twoPlayers){play(outcome(playerScore,opponentScore,twoPlayers),.65f);}
    void release(){released=true;handler.removeCallbacksAndMessages(null);manager.abandonAudioFocus(focus);if(music!=null){music.release();music=null;}if(effects!=null){effects.release();effects=null;}}
}
