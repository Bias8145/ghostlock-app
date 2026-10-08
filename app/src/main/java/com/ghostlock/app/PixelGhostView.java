package com.ghostlock.app;
import android.content.Context;import android.graphics.Canvas;import android.graphics.Paint;import android.util.AttributeSet;import android.view.View;
public class PixelGhostView extends View{
 private final Paint p=new Paint();private final int[][] g={{0,0,0,1,1,1,1,0,0,0},{0,0,1,1,1,1,1,1,0,0},{0,1,1,1,1,1,1,1,1,0},{1,1,1,1,1,1,1,1,1,1},{1,1,0,1,1,1,1,0,1,1},{1,1,1,1,1,1,1,1,1,1},{1,1,1,0,1,1,0,1,1,1},{1,1,0,0,1,1,0,0,1,1},{1,0,0,1,1,1,1,0,0,1},{0,0,1,1,0,0,1,1,0,0}};
 public PixelGhostView(Context c){super(c);init();}public PixelGhostView(Context c,AttributeSet a){super(c,a);init();}private void init(){setLayerType(View.LAYER_TYPE_SOFTWARE,null);p.setAntiAlias(false);}
 protected void onDraw(Canvas c){super.onDraw(c);float s=Math.min(getWidth(),getHeight()),px=Math.max(2f,s/12f),ox=(getWidth()-px*10)/2f,oy=(getHeight()-px*10)/2f;p.setColor(getResources().getColor(R.color.pixel_shadow));draw(c,ox+px,oy+px,px);p.setColor(getResources().getColor(R.color.pixel_ghost));draw(c,ox,oy,px);p.setColor(getResources().getColor(R.color.pixel_highlight));c.drawRect(ox+px*4,oy+px,ox+px*5,oy+px*2,p);c.drawRect(ox+px*7,oy+px,ox+px*8,oy+px*2,p);}
 private void draw(Canvas c,float ox,float oy,float px){for(int y=0;y<g.length;y++)for(int x=0;x<g[y].length;x++)if(g[y][x]==1)c.drawRect(ox+x*px,oy+y*px,ox+(x+1)*px,oy+(y+1)*px,p);}
}