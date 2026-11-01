package com.offline.mancala;

import android.animation.*;
import android.graphics.*;
import android.view.*;
import java.util.*;

public class BoardView extends View {
    final MainActivity activity; final boolean preview; final Paint p=new Paint(3);
    final RectF[] holes=new RectF[14]; float scale=1,scaleY=1,ox,oy; boolean animating=false;
    ValueAnimator animator; int[] shown; ArrayList<Integer> route; int pickup,handCount,movingPlayer; float progress;
    final int[] stones={0xffdfb26e,0xff5f9b94,0xffdfd0ae,0xffa45437,0xff798a59};
    final int[][] woods={{0xffc99557,0xffe3bd80,0xffbc8449,0xff946035},{0xff795135,0xffa37a50,0xff795033,0xff513321},{0xff8a4e3e,0xffb1785c,0xff874b39,0xff542c25}};
    final int[][] palettes={{0xffe0b772,0xff599b94,0xfff0dfba,0xffb95f40,0xff87935e},{0xff74b7ad,0xff9ccbd0,0xffe7d9b8,0xff64949e,0xffb8a17c},{0xffeee1c6,0xffd4ba91,0xff83a89e,0xffd39468,0xffe1c9a2}};
    BoardView(MainActivity a,boolean preview){super(a);activity=a;this.preview=preview;setLayerType(View.LAYER_TYPE_SOFTWARE,null);setFocusable(true);setContentDescription(preview?"Wooden Mancala board":"Mancala board. Bottom row Player 1, top row Player 2. Tap a pit to sow stones.");
        holes[13]=new RectF(35,85,135,390);holes[6]=new RectF(865,85,965,390);
        for(int i=0;i<6;i++){float x=157+i*116;holes[i]=new RectF(x,260,x+104,390);holes[12-i]=new RectF(x,85,x+104,215);}
    }
    void fill(int color){p.setShader(null);p.setColor(color);p.setStyle(Paint.Style.FILL);p.clearShadowLayer();}
    void txt(Canvas c,String s,float x,float y,float size,int color){fill(color);p.setTextSize(size);p.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));p.setTextAlign(Paint.Align.CENTER);c.save();c.translate(x,y);c.scale(1,scale/scaleY);c.drawText(s,0,0,p);c.restore();}
    @Override protected void onDraw(Canvas canvas){super.onDraw(canvas);scale=preview?Math.min(getWidth()/1000f,getHeight()/420f):getWidth()/1000f;scaleY=preview?scale:getHeight()/420f;ox=(getWidth()-1000*scale)/2;oy=(getHeight()-420*scaleY)/2-30*scaleY;canvas.save();canvas.translate(ox,oy);canvas.scale(scale,scaleY);Canvas c=canvas;
        RectF wood=new RectF(12,45,988,435);fill(0xff080b09);p.setShadowLayer(18,0,12,0xbb000000);c.drawRoundRect(wood,38,38,p);p.clearShadowLayer();
        p.setShader(new LinearGradient(0,45,0,435,woods[activity.boardStyle],null,Shader.TileMode.CLAMP));c.drawRoundRect(wood,38,38,p);p.setShader(null);
        c.save();Path clip=new Path();clip.addRoundRect(wood,38,38,Path.Direction.CW);c.clipPath(clip);
        Random rand=new Random(91);for(int i=0;i<180;i++){float y=47+i*2.15f;fill(i%3==0?0x24744823:0x18fff0bd);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(i%4==0?1.7f:.6f);Path grain=new Path();grain.moveTo(10,y);for(int x=10;x<1000;x+=80){float bend=(float)Math.sin(x*.009+i*.24)*5;grain.quadTo(x+40,y+bend+rand.nextFloat()*3,x+80,y+bend);}c.drawPath(grain,p);}c.restore();
        fill(0xffe5bc7b);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2);c.drawRoundRect(new RectF(21,54,979,425),32,32,p);
        int[] counts=preview?new Game().pits:animating?shown:activity.game.pits;
        for(int i=0;i<14;i++){RectF r=holes[i];boolean store=i==6||i==13;boolean active=!preview&&!animating&&!activity.thinking&&!activity.game.over&&(activity.mode==2||activity.game.turn==0)&&activity.game.moves().contains(i);
            fill(0xff704020);c.drawOval(new RectF(r.left-3,r.top-3,r.right+3,r.bottom+3),p);
            p.setShader(new RadialGradient(r.centerX()+5,r.centerY()+20,store?150:74,new int[]{0xffa56b37,0xff784721,0xff3b2415},new float[]{0,.62f,1},Shader.TileMode.CLAMP));c.drawOval(r,p);p.setShader(null);
            fill(0x77f8d79b);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2);c.drawArc(r,5,160,false,p);
            if(active){fill(0xfff4d48d);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(3);p.setShadowLayer(7,0,0,0x99edbd66);c.drawOval(new RectF(r.left-5,r.top-5,r.right+5,r.bottom+5),p);p.clearShadowLayer();}
            int n=Math.min(counts[i],30);
            c.save();Path inside=new Path();inside.addOval(r,Path.Direction.CW);c.clipPath(inside);
            for(int j=0;j<n;j++){
                float x,y;float size=n<=6?21:18;
                if(n<=6){int columns=n==1?1:2,rows=(n+columns-1)/columns;x=r.centerX()+(j%columns-(columns-1)/2f)*43;y=r.centerY()+(j/columns-(rows-1)/2f)*41;}
                else{double angle=j*2.39996;float radius=(float)Math.sqrt((j+.5)/n);x=r.centerX()+(float)Math.cos(angle)*radius*(r.width()/2-21);y=r.centerY()+(float)Math.sin(angle)*radius*(r.height()/2-22);}
                stone(c,x,y,size+(j%2),palettes[activity.boardStyle][(j+i*2)%stones.length]);
            }c.restore();
            float cy=store?r.bottom+14:i<6?411:66;fill(0xff3d2819);c.drawRoundRect(new RectF(r.centerX()-16,cy-13,r.centerX()+16,cy+10),8,8,p);txt(c,""+counts[i],r.centerX(),cy+6,20,0xffffedcd);
        }
        if(!preview){
            String left=activity.name(1)+"  "+counts[13];String right=activity.name(0)+"  "+counts[6];
            txt(c,left.toUpperCase(),330,244,17,activity.boardStyle==0?0xff2e1c12:0xffffe6be);
            txt(c,right.toUpperCase(),670,244,17,activity.boardStyle==0?0xff2e1c12:0xffffe6be);
            txt(c,"•",activity.game.turn==1?240:580,244,19,activity.boardStyle==0?0xff2e1c12:0xffffe6be);
            if(animating)drawMovingHand(c);
            if(activity.game.over&&!animating){fill(0xc91a211b);c.drawRoundRect(new RectF(260,160,740,278),20,20,p);txt(c,"GAME COMPLETE",500,204,24,0xffffe0a4);txt(c,activity.game.pits[6]+"  —  "+activity.game.pits[13],500,251,33,0xfff6e9d1);}
        }
        c.restore();
    }
    void stone(Canvas c,float x,float y,float r,int color){
        c.save();c.translate(x,y);c.scale(1,scale/scaleY);
        Path shape=new Path();
        if(activity.pieceStyle==1){
            shape.moveTo(0,r*.9f);shape.cubicTo(-r*.25f,r*.6f,-r*1.12f,-r*.03f,-r*.93f,-r*.58f);
            shape.cubicTo(-r*.7f,-r*1.15f,-r*.18f,-r,0,-r*.48f);
            shape.cubicTo(r*.18f,-r,r*.7f,-r*1.15f,r*.93f,-r*.58f);
            shape.cubicTo(r*1.12f,-r*.03f,r*.25f,r*.6f,0,r*.9f);shape.close();
        }else if(activity.pieceStyle==2){
            for(int i=0;i<10;i++){double angle=-Math.PI/2+i*Math.PI/5;float radius=i%2==0?r:r*.5f;float px=(float)Math.cos(angle)*radius,py=(float)Math.sin(angle)*radius;if(i==0)shape.moveTo(px,py);else shape.lineTo(px,py);}shape.close();
        }else{shape.addOval(new RectF(-r,-r*.85f,r,r*.85f),Path.Direction.CW);}
        fill(0x80000000);c.save();c.translate(1,4);c.drawPath(shape,p);c.restore();
        fill(color);p.setShader(new RadialGradient(-r*.35f,-r*.45f,r*2,new int[]{lighten(color),color,darken(color)},new float[]{0,.55f,1},Shader.TileMode.CLAMP));c.drawPath(shape,p);
        fill(0x88fff0d4);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1);c.drawPath(shape,p);
        fill(0x88ffffff);c.drawOval(new RectF(-r*.49f,-r*.55f,-r*.12f,-r*.34f),p);c.restore();
    }
    int darken(int color){return Color.rgb((int)(Color.red(color)*.48f),(int)(Color.green(color)*.48f),(int)(Color.blue(color)*.48f));}
    int lighten(int color){return Color.rgb(Math.min(255,Color.red(color)+48),Math.min(255,Color.green(color)+48),Math.min(255,Color.blue(color)+48));}
    void animateMove(Game before,int pit,Runnable finished){animating=true;movingPlayer=before.turn;shown=before.pits.clone();pickup=pit;handCount=shown[pit];route=new ArrayList<>();int at=pit;for(int s=0;s<handCount;s++){do{at=(at+1)%14;}while(at==(before.turn==0?13:6));route.add(at);}progress=0;
        final int[] dropped={-1};animator=ValueAnimator.ofFloat(0,route.size()+2f);animator.setDuration(600+route.size()*240L);animator.setInterpolator(new android.view.animation.LinearInterpolator());animator.addUpdateListener(v->{progress=(float)v.getAnimatedValue();if(progress>=1&&dropped[0]<0){shown[pit]=0;dropped[0]=0;performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);}int should=Math.min(route.size(),Math.max(0,(int)(progress-1)));while(dropped[0]>=0&&dropped[0]<should){shown[route.get(dropped[0])]++;activity.audio.stone();dropped[0]++;}invalidate();});animator.addListener(new AnimatorListenerAdapter(){boolean cancelled;@Override public void onAnimationCancel(Animator a){cancelled=true;}@Override public void onAnimationEnd(Animator a){animating=false;if(!cancelled){invalidate();finished.run();}}});animator.start();}
    void drawMovingHand(Canvas c){float x,y;int remaining=handCount;RectF source=holes[pickup];if(progress<1){x=source.centerX();y=source.centerY()-30*(1-progress);}else{float travel=Math.min(route.size(),progress-1);int step=Math.min(route.size()-1,(int)travel);float t=Math.min(1,travel-step);if(travel>=route.size())t=1;RectF from=holes[step==0?pickup:route.get(step-1)],to=holes[route.get(step)];float ease=t*t*(3-2*t);x=from.centerX()+(to.centerX()-from.centerX())*ease;y=from.centerY()+(to.centerY()-from.centerY())*ease-(float)Math.sin(t*Math.PI)*26;remaining=Math.max(0,handCount-(int)travel);if(t>.65&&remaining>0)stone(c,to.centerX(),to.centerY()-35+35*(t-.65f)/.35f,21,palettes[activity.boardStyle][step%5]);}
        c.save();c.translate(x,y-12);c.rotate(-18);float visibility=progress>route.size()+1?Math.max(0,route.size()+2-progress):1;c.scale(.85f,.85f);p.setAlpha((int)(255*visibility));
        // A single outlined silhouette: wrist, palm, thumb and four rounded fingers.
        Path hand=new Path();hand.moveTo(-13,110);hand.lineTo(-19,66);hand.cubicTo(-30,53,-42,43,-43,30);hand.cubicTo(-43,19,-34,18,-28,27);hand.lineTo(-17,40);hand.lineTo(-20,-7);hand.cubicTo(-20,-24,-6,-24,-5,-7);hand.lineTo(-3,19);hand.lineTo(-3,-22);hand.cubicTo(-3,-39,12,-39,13,-22);hand.lineTo(14,16);hand.lineTo(17,-14);hand.cubicTo(18,-29,32,-27,31,-10);hand.lineTo(30,24);hand.lineTo(34,6);hand.cubicTo(38,-7,50,-1,46,13);hand.lineTo(42,44);hand.cubicTo(41,60,30,68,29,78);hand.lineTo(32,110);hand.close();
        fill(0xffdca57a);p.setShadowLayer(8,6,8,0x66000000);c.drawPath(hand,p);p.clearShadowLayer();p.setColor(0xff875535);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2);c.drawPath(hand,p);p.setColor(0xffb47a53);c.drawArc(new RectF(-15,32,29,63),190,110,false,p);c.drawLine(-10,73,28,73,p);fill(0xff304f46);c.drawRoundRect(new RectF(-16,86,35,118),4,4,p);fill(0xffb7955b);c.drawRect(-16,86,35,91,p);c.restore();
        if(remaining>0){fill(0xff273c32);c.drawCircle(x+44,y-20,17,p);txt(c,""+remaining,x+44,y-14,17,0xffffe4ab);}
    }
    void cancel(){if(animator!=null)animator.cancel();animating=false;}
    @Override public boolean onTouchEvent(android.view.MotionEvent event){if(preview)return false;if(event.getAction()==MotionEvent.ACTION_UP){float x=(event.getX()-ox)/scale,y=(event.getY()-oy)/scaleY;for(int i=0;i<14;i++)if(i!=6&&i!=13&&holes[i].contains(x,y)){performClick();activity.play(i);break;}return true;}return true;}
    @Override public void onInitializeAccessibilityNodeInfo(android.view.accessibility.AccessibilityNodeInfo info){super.onInitializeAccessibilityNodeInfo(info);if(preview)return;StringBuilder description=new StringBuilder("Mancala board. ");for(int i=0;i<14;i++)description.append(i==6?"Your store":i==13?"Opponent store":"Pit "+(i+1)).append(": ").append(activity.game.pits[i]).append(" stones. ");info.setContentDescription(description);if(!animating&&!activity.thinking&&(activity.mode==2||activity.game.turn==0))for(int i:activity.game.moves())info.addAction(new android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction(1000+i,"Sow pit "+(i%7+1)+", "+activity.game.pits[i]+" stones"));}
    @Override public boolean performAccessibilityAction(int action,android.os.Bundle args){if(action>=1000&&action<1014){activity.play(action-1000);return true;}return super.performAccessibilityAction(action,args);}
    @Override public boolean performClick(){super.performClick();return true;}
}
