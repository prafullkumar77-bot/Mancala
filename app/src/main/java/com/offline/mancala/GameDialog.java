package com.offline.mancala;

import android.app.Dialog;
import android.graphics.*;
import android.graphics.drawable.*;
import android.view.*;
import android.widget.*;

/** Shared, landscape-friendly dialogs styled as a warm wooden game panel. */
final class GameDialog extends Dialog {
    final MainActivity a;
    final LinearLayout panel,body,actions;
    boolean shortMessage,hasChoices;
    GameDialog(MainActivity a,String title,String subtitle){
        super(a);this.a=a;requestWindowFeature(Window.FEATURE_NO_TITLE);
        panel=new LinearLayout(a);panel.setOrientation(1);panel.setPadding(a.dp(20),a.dp(14),a.dp(20),a.dp(14));
        GradientDrawable background=a.bg(0xff3b3024,0xff201f19,24);background.setStroke(a.dp(2),0xff9a7042);panel.setBackground(background);
        LinearLayout header=new LinearLayout(a);header.setGravity(Gravity.CENTER_VERTICAL);panel.addView(header);
        LinearLayout titles=new LinearLayout(a);titles.setOrientation(1);header.addView(titles,new LinearLayout.LayoutParams(0,-2,1));
        TextView heading=a.text(title,21,a.cream);heading.setGravity(Gravity.LEFT);heading.setTypeface(Typeface.create("serif",Typeface.NORMAL));titles.addView(heading);
        if(!subtitle.isEmpty()){TextView sub=a.text(subtitle,12,a.muted);sub.setGravity(Gravity.LEFT);sub.setPadding(0,a.dp(3),0,a.dp(5));titles.addView(sub);}
        Button close=a.button("×",false);close.setTextSize(24);close.setContentDescription("Close dialog");header.addView(close,new LinearLayout.LayoutParams(a.dp(48),a.dp(48)));close.setOnClickListener(v->dismiss());
        View divider=new View(a);divider.setBackgroundColor(0xff755735);LinearLayout.LayoutParams line=new LinearLayout.LayoutParams(-1,a.dp(1));line.setMargins(0,a.dp(8),0,a.dp(8));panel.addView(divider,line);
        ScrollView scroll=new ScrollView(a);scroll.setFillViewport(true);scroll.setVerticalScrollBarEnabled(true);scroll.setScrollbarFadingEnabled(false);panel.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));body=new LinearLayout(a);body.setOrientation(1);body.setGravity(Gravity.CENTER);scroll.addView(body);
        actions=new LinearLayout(a);actions.setGravity(Gravity.RIGHT|Gravity.TOP);actions.setBaselineAligned(false);panel.addView(actions);
        setContentView(panel);setCanceledOnTouchOutside(true);
    }
    void message(String text){shortMessage=text.length()<240;TextView t=a.text(text,14,a.cream);t.setGravity(Gravity.CENTER);t.setLineSpacing(a.dp(4),1);t.setPadding(0,a.dp(6),0,a.dp(12));body.addView(t,new LinearLayout.LayoutParams(-1,-2));}
    void action(String label,boolean primary,Runnable action){Button b=a.button(label,primary);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,a.dp(48),1);lp.setMargins(a.dp(5),a.dp(10),a.dp(5),0);actions.addView(b,lp);b.setOnClickListener(v->{dismiss();if(action!=null)action.run();});}
    void choices(String first,String second,Runnable one,Runnable two){hasChoices=true;LinearLayout row=new LinearLayout(a);row.setBaselineAligned(false);row.setGravity(Gravity.CENTER_VERTICAL);body.addView(row,new LinearLayout.LayoutParams(-1,-2));choice(row,first,one);choice(row,second,two);}
    void choice(LinearLayout row,String text,Runnable action){Button b=a.button(text,false);b.setTextSize(16);android.text.SpannableString label=new android.text.SpannableString(text);int split=text.indexOf('\n');if(split>=0){label.setSpan(new android.text.style.AbsoluteSizeSpan(13,true),split+1,text.length(),0);label.setSpan(new android.text.style.StyleSpan(Typeface.NORMAL),split+1,text.length(),0);}b.setText(label);b.setPadding(a.dp(8),a.dp(8),a.dp(8),a.dp(8));b.setGravity(Gravity.CENTER);GradientDrawable d=a.bg(0xff59412b,0xff34291f,16);d.setStroke(a.dp(1),0xffa77a45);b.setBackground(d);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,a.dp(96),1);lp.setMargins(a.dp(5),0,a.dp(5),0);row.addView(b,lp);b.setOnClickListener(v->{dismiss();action.run();});}
    Button setting(String text){Button b=a.button(text,false);b.setTextSize(15);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,a.dp(48));lp.setMargins(0,0,0,a.dp(8));body.addView(b,lp);return b;}
    @Override public void show(){super.show();Window w=getWindow();if(w==null)return;w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));w.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);WindowManager.LayoutParams lp=w.getAttributes();lp.dimAmount=.72f;lp.gravity=Gravity.CENTER;android.util.DisplayMetrics dm=a.getResources().getDisplayMetrics();lp.width=Math.min(a.dp(560),Math.round(dm.widthPixels*.88f));lp.height=Math.min(a.dp(hasChoices?234:shortMessage?230:290),Math.round(dm.heightPixels*.86f));w.setAttributes(lp);panel.setAlpha(0);panel.setScaleX(.96f);panel.setScaleY(.96f);panel.animate().alpha(1).scaleX(1).scaleY(1).setDuration(180).start();}
}
