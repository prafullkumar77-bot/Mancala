package com.offline.mancala;

import android.graphics.*;
import android.graphics.drawable.Drawable;
import java.util.Random;

/** Resolution-independent walnut tabletop, cached once per size. */
final class WoodDrawable extends Drawable {
    private final Paint paint=new Paint(3);
    private Bitmap texture;
    @Override public void draw(Canvas canvas){
        Rect b=getBounds(); if(b.width()==0||b.height()==0)return;
        if(texture==null||texture.getWidth()!=b.width()||texture.getHeight()!=b.height()){
            texture=Bitmap.createBitmap(b.width(),b.height(),Bitmap.Config.ARGB_8888);
            Canvas c=new Canvas(texture);float w=b.width(),h=b.height();
            paint.setShader(new LinearGradient(0,0,w,h,new int[]{0xff493022,0xff302018,0xff211710},null,Shader.TileMode.CLAMP));c.drawRect(0,0,w,h,paint);paint.setShader(null);
            Random random=new Random(72);
            for(int i=0;i<h;i+=3){paint.setColor(i%3==0?0x167d5839:0x180b0502);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(1+random.nextFloat()*2);Path path=new Path();path.moveTo(0,i);for(int x=0;x<w;x+=90)path.quadTo(x+45,i+(float)Math.sin(x*.008+i*.03)*8,x+90,i+random.nextFloat()*3);c.drawPath(path,paint);}
            for(float y=h/4;y<h;y+=h/4){paint.setColor(0x880d0805);paint.setStrokeWidth(3);c.drawLine(0,y,w,y,paint);paint.setColor(0x225f4732);paint.setStrokeWidth(1);c.drawLine(0,y+3,w,y+3,paint);}
            paint.setStyle(Paint.Style.FILL);paint.setAlpha(255);paint.setShader(new RadialGradient(w*.5f,h*.4f,w*.7f,new int[]{0x00000000,0x88000000},null,Shader.TileMode.CLAMP));c.drawRect(0,0,w,h,paint);paint.setShader(null);
        }
        paint.setAlpha(255);canvas.drawBitmap(texture,b.left,b.top,paint);
    }
    @Override public void setAlpha(int alpha){paint.setAlpha(alpha);invalidateSelf();}
    @Override public void setColorFilter(ColorFilter filter){paint.setColorFilter(filter);invalidateSelf();}
    @Override public int getOpacity(){return PixelFormat.OPAQUE;}
}
