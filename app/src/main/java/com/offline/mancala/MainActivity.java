package com.offline.mancala;

import android.app.*;
import android.os.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import java.util.concurrent.*;

public class MainActivity extends Activity {
    final int cream=0xfff2dfbc, muted=0xffbfa989, gold=0xffd9aa63;
    GameAudio audio;
    int boardStyle=0, pieceStyle=0;
    final String[] woodNames={"Honey oak","American walnut","Rosewood"};
    final String[] woodNotes={"Warm grain · river stones","Rich timber · sea glass","Deep wood · ivory pebbles"};
    Game game=new Game(); int mode=0,generation=0; boolean thinking=false,inGame=false,hasSave=false;
    ExecutorService executor=Executors.newSingleThreadExecutor(); Future<?> task;
    BoardView board; TextView status; String detail="Tap one of your glowing pits";
    int dp(float n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
    @Override public void onCreate(Bundle state){super.onCreate(state);getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);setVolumeControlStream(android.media.AudioManager.STREAM_MUSIC);audio=new GameAudio(this,getPreferences(0).getBoolean("music",true),getPreferences(0).getBoolean("sound",true));boardStyle=getPreferences(0).getInt("boardStyle",0);if(boardStyle<0||boardStyle>2)boardStyle=0;getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);restore();home();}
    GradientDrawable bg(int a,int b,int radius){GradientDrawable d=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{a,b});d.setCornerRadius(dp(radius));return d;}
    TextView text(String value,int size,int color){TextView t=new TextView(this);t.setText(value);t.setTextSize(size);t.setTextColor(color);t.setGravity(Gravity.CENTER);return t;}
    Button button(String value,boolean primary){Button b=new Button(this){@Override public boolean performClick(){boolean handled=super.performClick();if(audio!=null)audio.tap();return handled;}};b.setSoundEffectsEnabled(false);b.setText(value);b.setAllCaps(false);b.setTextSize(15);b.setTypeface(null,Typeface.BOLD);b.setTextColor(primary?0xff2b1b10:cream);GradientDrawable face=bg(primary?0xffe5c58e:0xff543a28,primary?0xffc39556:0xff342318,12);face.setStroke(dp(1),primary?0xfff1d7a5:0xff846044);b.setBackground(new android.graphics.drawable.RippleDrawable(android.content.res.ColorStateList.valueOf(0x33ffffff),face,null));b.setElevation(dp(3));b.setPadding(dp(16),0,dp(16),0);return b;}
    LinearLayout shell(){
        LinearLayout l=new LinearLayout(this);l.setOrientation(1);l.setBackground(new WoodDrawable());
        final int horizontal=dp(inGame?6:20),vertical=dp(inGame?4:8);
        l.setPadding(horizontal,vertical,horizontal,vertical);
        l.setOnApplyWindowInsetsListener((v,i)->{
            int left=0,top=0,right=0,bottom=0;
            if(Build.VERSION.SDK_INT>=30){android.graphics.Insets safe=i.getInsets(WindowInsets.Type.systemBars()|WindowInsets.Type.displayCutout());left=safe.left;top=safe.top;right=safe.right;bottom=safe.bottom;}
            else if(Build.VERSION.SDK_INT>=28&&i.getDisplayCutout()!=null){android.view.DisplayCutout cut=i.getDisplayCutout();left=cut.getSafeInsetLeft();top=cut.getSafeInsetTop();right=cut.getSafeInsetRight();bottom=cut.getSafeInsetBottom();}
            v.setPadding(horizontal+left,vertical+top,horizontal+right,vertical+bottom);return i;
        });setContentView(l);return l;
    }
    void home(){stopWork();inGame=false;LinearLayout outer=shell();boolean compact=getResources().getConfiguration().screenHeightDp<430;
        LinearLayout toolbar=new LinearLayout(this);toolbar.setGravity(Gravity.CENTER_VERTICAL);outer.addView(toolbar,new LinearLayout.LayoutParams(-1,dp(44)));
        TextView brand=text("M A N C A L A",22,cream);brand.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);brand.setTypeface(Typeface.create("serif",Typeface.BOLD));toolbar.addView(brand,new LinearLayout.LayoutParams(0,-1,1));
        Button help=button("How to play",false);toolbar.addView(help,new LinearLayout.LayoutParams(dp(120),dp(42)));help.setOnClickListener(v->rules());
        Button settings=button("Settings",false);LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(dp(100),dp(42));sp.setMargins(dp(10),0,0,0);toolbar.addView(settings,sp);settings.setOnClickListener(v->settings());
        LinearLayout content=new LinearLayout(this);content.setGravity(Gravity.CENTER_VERTICAL);outer.addView(content,new LinearLayout.LayoutParams(-1,0,1));
        ScrollView menuScroll=new ScrollView(this);menuScroll.setFillViewport(true);content.addView(menuScroll,new LinearLayout.LayoutParams(0,-1,0.32f));
        LinearLayout menu=new LinearLayout(this);menu.setOrientation(1);menu.setGravity(Gravity.CENTER_VERTICAL);menu.setPadding(0,dp(8),dp(22),dp(8));menuScroll.addView(menu);
        TextView heading=text(compact?"Take a moment.\nMake your move.":"A timeless game.\nA moment for you.",compact?19:27,cream);heading.setGravity(Gravity.LEFT);heading.setTypeface(Typeface.create("serif",Typeface.NORMAL));menu.addView(heading);
        TextView intro=text("Sow. Capture. Make your next move.",12,muted);intro.setGravity(Gravity.LEFT);intro.setPadding(0,dp(8),0,dp(4));if(!compact)menu.addView(intro);
        Button bot=button("Play solo   →",true);addHomeButton(menu,bot);bot.setOnClickListener(v->chooseBot());
        Button two=button("Two players",false);addHomeButton(menu,two);two.setOnClickListener(v->startNew(2));
        if(hasSave&&!game.over){Button resume=button("Continue game   →",false);addHomeButton(menu,resume);resume.setOnClickListener(v->showGame());}
        LinearLayout collection=new LinearLayout(this);collection.setOrientation(1);collection.setGravity(Gravity.CENTER);content.addView(collection,new LinearLayout.LayoutParams(0,-1,.68f));
        TextView eyebrow=text("THE BOARD COLLECTION",11,gold);eyebrow.setLetterSpacing(.18f);collection.addView(eyebrow,new LinearLayout.LayoutParams(-1,dp(28)));
        BoardView preview=new BoardView(this,true);collection.addView(preview,new LinearLayout.LayoutParams(-1,0,1));
        TextView selected=text(woodNames[boardStyle]+"  ·  "+woodNotes[boardStyle],13,cream);collection.addView(selected,new LinearLayout.LayoutParams(-1,dp(28)));
        LinearLayout choices=new LinearLayout(this);choices.setGravity(Gravity.CENTER);collection.addView(choices,new LinearLayout.LayoutParams(-1,dp(48)));
        for(int i=0;i<3;i++){final int style=i;Button choice=button((boardStyle==i?"✓  ":"")+woodNames[i],boardStyle==i);choice.setTextSize(12);choice.setPadding(dp(4),0,dp(4),0);choice.setContentDescription("Select "+woodNames[i]+" board");choice.setSelected(boardStyle==i);LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(0,dp(44),1);cp.setMargins(dp(4),0,dp(4),0);choices.addView(choice,cp);choice.setOnClickListener(v->{boardStyle=style;getPreferences(0).edit().putInt("boardStyle",style).apply();home();});}
        TextView foot=text("OFFLINE  ·  NO ADS  ·  JUST PLAY",10,muted);foot.setLetterSpacing(.12f);outer.addView(foot,new LinearLayout.LayoutParams(-1,dp(24)));
    }
    void chooseBot(){GameDialog d=new GameDialog(this,"Choose your opponent","Pick your pace.");d.choices("Easy\nA relaxed game","Expert\nA thoughtful challenge",()->startNew(0),()->startNew(1));d.show();}
    void confirm(String title,String message,String positive,Runnable action){GameDialog d=new GameDialog(this,title,"A fresh board. A new possibility.");d.message(message);d.action("Keep playing",false,null);d.action(positive,true,action);d.show();}
    void settings(){GameDialog d=new GameDialog(this,"Settings","Make the game feel right for you.");if(inGame){Button fresh=d.setting("New game");fresh.setOnClickListener(v->{d.dismiss();confirm("Start again?","This replaces your current game with a fresh board.","New game",()->begin(mode));});}Button music=d.setting("Music  ·  "+(audio.musicEnabled?"On":"Off"));music.setOnClickListener(v->{audio.musicEnabled=!audio.musicEnabled;audio.sync();getPreferences(0).edit().putBoolean("music",audio.musicEnabled).apply();music.setText("Music  ·  "+(audio.musicEnabled?"On":"Off"));});Button sound=d.setting("Sound effects  ·  "+(audio.soundEnabled?"On":"Off"));sound.setOnClickListener(v->{audio.soundEnabled=!audio.soundEnabled;audio.sync();getPreferences(0).edit().putBoolean("sound",audio.soundEnabled).apply();sound.setText("Sound effects  ·  "+(audio.soundEnabled?"On":"Off"));});Button rate=d.setting("Rate us  ☆");rate.setOnClickListener(v->{d.dismiss();GameDialog rating=new GameDialog(this,"Thanks for playing","Your feedback matters.");rating.message("Mancala Offline is not on Google Play yet. Ratings will be available once the app is published.");rating.action("Back to settings",true,()->settings());rating.show();});Button credits=d.setting("Audio credits");credits.setOnClickListener(v->{d.dismiss();GameDialog info=new GameDialog(this,"Audio credits","Openly licensed audio · CC0 1.0");info.message("Background: Calm Track by pmiller (OpenGameArt).\n\nInterface sounds and win/loss jingles: Kenney (kenney.nl).\n\nAll files use the CC0 public-domain dedication. Full source links and original license notices are bundled in the app’s audio assets.");info.action("Back to settings",true,()->settings());info.show();});d.show();}
    void addHomeButton(LinearLayout l,Button b){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(46));p.setMargins(0,dp(8),0,dp(2));l.addView(b,p);}
    void startNew(int chosen){if(hasSave&&!game.over)confirm("Start a fresh game?","Your saved game will be replaced. Ready for another round?","Start game",()->begin(chosen));else begin(chosen);}
    void begin(int chosen){stopWork();mode=chosen;pieceStyle=(pieceStyle+1+new java.util.Random().nextInt(2))%3;game=new Game();hasSave=true;detail="Tap one of your glowing pits";save();showGame();}
    void showGame(){inGame=true;LinearLayout l=shell();LinearLayout header=new LinearLayout(this);header.setGravity(Gravity.CENTER_VERTICAL);l.addView(header,new LinearLayout.LayoutParams(-1,dp(48)));
        Button back=button("‹ Home",false);back.setTextSize(13);back.setSingleLine(true);back.setPadding(dp(8),0,dp(8),0);header.addView(back,new LinearLayout.LayoutParams(dp(76),dp(44)));back.setOnClickListener(v->home());
        status=text("",13,cream);status.setTypeface(null,Typeface.BOLD);header.addView(status,new LinearLayout.LayoutParams(0,-1,1));
        Button rules=button("?",false);header.addView(rules,new LinearLayout.LayoutParams(dp(48),dp(42)));rules.setOnClickListener(v->rules());Button options=button("Settings",false);options.setTextSize(12);options.setSingleLine(true);options.setPadding(dp(8),0,dp(8),0);LinearLayout.LayoutParams settingsSize=new LinearLayout.LayoutParams(dp(76),dp(44));settingsSize.setMargins(dp(8),0,0,0);header.addView(options,settingsSize);options.setOnClickListener(v->settings());
        board=new BoardView(this,false);l.addView(board,new LinearLayout.LayoutParams(-1,0,1));
        render();schedule();}
    String name(int p){return p==0?(mode==2?"Player 1":"You"):(mode==2?"Player 2":mode==1?"Expert bot":"Easy bot");}
    void render(){if(!inGame)return;if(game.over){int diff=game.pits[6]-game.pits[13];status.setText((diff==0?"A draw":name(diff>0?0:1)+(diff>0&&mode!=2?" win!":" wins!"))+"   ·   "+game.pits[6]+" — "+game.pits[13]);}else status.setText(thinking?"Bot is thinking…":name(game.turn)+(game.turn==0&&mode!=2?"r turn":"’s turn")+"  ·  "+detail);status.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);board.invalidate();}
    void play(int pit){if(!inGame||thinking||board.animating||game.over||(mode!=2&&game.turn==1)||!game.moves().contains(pit))return;audio.pickup();animateMove(pit);}
    void animateMove(int pit){Game before=game.copy();game.move(pit);save();detail="Sowing stones…";render();status.setText(name(before.turn)+" · Sowing stones…");int token=generation;board.animateMove(before,pit,()->{if(token!=generation||!inGame)return;if(game.over)audio.result(game.pits[6],game.pits[13],mode==2);else if(game.captured>0)audio.capture();detail=game.captured>0?"Captured "+game.captured+" stones!":game.extra?"Your store! Go again":"Tap one of your glowing pits";render();schedule();});}
    void schedule(){if(!inGame||game.over||mode==2||game.turn==0||thinking||board.animating)return;thinking=true;render();int token=generation;Game snapshot=game.copy();boolean expert=mode==1;task=executor.submit(()->{int move=Game.bot(snapshot,expert,700);try{Thread.sleep(400);}catch(InterruptedException e){return;}runOnUiThread(()->{if(token!=generation||!inGame||isDestroyed())return;thinking=false;animateMove(move);});});}
    void stopWork(){generation++;if(task!=null)task.cancel(true);thinking=false;if(board!=null)board.cancel();}
    void rules(){GameDialog d=new GameDialog(this,"A handful of rules","Player 1: lower row & right store  ·  Player 2: upper row & left store");d.message("01  SOW YOUR STONES\nTap a glowing pit. Sow counterclockwise, one stone at a time, skipping your opponent’s store.\n\n02  TAKE ANOTHER TURN\nFinish in your store to play again.\n\n03  MAKE A CAPTURE\nFinish in an empty pit on your side to collect it and the stones opposite, if that pit has stones.\n\n04  COUNT YOUR STONES\nWhen one row is empty, collect the other row into its owner’s store. Most stones wins.");d.action("Let’s play",true,null);d.show();}
    void save(){StringBuilder s=new StringBuilder();for(int n:game.pits)s.append(n).append(',');getPreferences(0).edit().putString("pits",s.toString()).putInt("turn",game.turn).putInt("mode",mode).putInt("pieceStyle",pieceStyle).putBoolean("over",game.over).apply();hasSave=true;}
    void restore(){android.content.SharedPreferences p=getPreferences(0);pieceStyle=Math.max(0,Math.min(2,p.getInt("pieceStyle",0)));String v=p.getString("pits",null);if(v==null)return;try{String[] a=v.split(",");if(a.length!=14)return;Game g=new Game();int total=0;for(int i=0;i<14;i++){g.pits[i]=Integer.parseInt(a[i]);if(g.pits[i]<0)return;total+=g.pits[i];}if(total!=48)return;g.turn=p.getInt("turn",0);if(g.turn<0||g.turn>1)return;g.over=p.getBoolean("over",false);game=g;mode=Math.max(0,Math.min(2,p.getInt("mode",0)));hasSave=true;}catch(Exception ignored){}}
    @Override public void onBackPressed(){if(inGame)home();else super.onBackPressed();}
    @Override protected void onResume(){super.onResume();if(audio!=null)audio.resume();}
    @Override protected void onPause(){if(audio!=null)audio.pause();if(hasSave)save();super.onPause();}
    @Override protected void onDestroy(){stopWork();executor.shutdownNow();if(audio!=null)audio.release();super.onDestroy();}
}
