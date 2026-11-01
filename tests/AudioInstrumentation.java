package com.offline.mancala;
import android.app.*;
import android.os.*;
import android.content.*;
import android.media.*;
import android.content.res.AssetFileDescriptor;
import java.lang.reflect.*;
import java.util.*;
public class AudioInstrumentation extends Instrumentation {
 public void onCreate(Bundle b){super.onCreate(b);start();}
 static Object field(Object o,String n)throws Exception{Field f=o.getClass().getDeclaredField(n);f.setAccessible(true);return f.get(o);}
 static void check(boolean b,String m){if(!b)throw new AssertionError(m);}
 public void onStart(){Bundle result=new Bundle();MainActivity a=null;try{
  a=(MainActivity)startActivitySync(new Intent(getTargetContext(),MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));final GameAudio audio=a.audio;
  runOnMainSync(()->{audio.musicEnabled=true;audio.soundEnabled=true;audio.resume();});Thread.sleep(1800);
  check((Boolean)field(audio,"prepared"),"Music prepared");check(((Set<?>)field(audio,"ready")).size()==7,"All 7 effects decoded");
  runOnMainSync(()->check(((MediaPlayer)get(audio,"music")).isPlaying(),"Background playing"));
  check(GameAudio.outcome(30,18,false).equals("win"),"Human win");check(GameAudio.outcome(18,30,false).equals("loss"),"Human loss");check(GameAudio.outcome(24,24,false).equals("draw"),"Draw");check(GameAudio.outcome(18,30,true).equals("win"),"Local player 2 victory");
  for(String name:new String[]{"tap","pickup","stone","capture","win","loss","draw"}){runOnMainSync(()->audio.play(name,.5f));Thread.sleep(200);}
  runOnMainSync(()->{audio.musicEnabled=false;audio.sync();check(!((MediaPlayer)get(audio,"music")).isPlaying(),"Music mute");audio.musicEnabled=true;audio.sync();audio.pause();check(!((MediaPlayer)get(audio,"music")).isPlaying(),"Background pause");audio.resume();});
  for(String name:new String[]{"background","tap","pickup","stone","capture","win","loss","draw"}){MediaMetadataRetriever m=new MediaMetadataRetriever();try(AssetFileDescriptor fd=getTargetContext().getAssets().openFd("audio/"+name+".ogg")){m.setDataSource(fd.getFileDescriptor(),fd.getStartOffset(),fd.getLength());check(Integer.parseInt(m.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION))>0,"Duration "+name);}finally{m.release();}}
  result.putString("stream","PASS: 8 bundled files decode; music plays, mutes and pauses; all effects load/play; win/loss/draw/local result mappings pass.\n");finish(Activity.RESULT_OK,result);
 }catch(Throwable e){result.putString("stream","FAIL: "+e.toString());finish(Activity.RESULT_CANCELED,result);}}
 static Object get(Object o,String n){try{return field(o,n);}catch(Exception e){throw new RuntimeException(e);}}
}
