package com.handstudio.android.hzgrapherlib.graphview;

import java.util.ArrayList;
import java.util.WeakHashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Shader;
import android.util.Log;
import android.view.SurfaceHolder;

import com.handstudio.android.hzgrapherlib.canvas.GraphCanvasWrapper;
import com.handstudio.android.hzgrapherlib.error.ErrorCode;
import com.handstudio.android.hzgrapherlib.error.ErrorDetector;
import com.handstudio.android.hzgrapherlib.path.GraphPath;
import com.handstudio.android.hzgrapherlib.vo.GraphNameBox;
import com.handstudio.android.hzgrapherlib.vo.linegraph.LineGraphVO;

public class LineCompareGraphView extends AbsGraphView{
	private LineGraphVO mLineGraphVO = null;
	
	
	//Constructor
	public LineCompareGraphView(Context context, LineGraphVO vo) {
		super(context, vo);
		mLineGraphVO = vo;
	}
	
	@Override
	protected BaseDrawThread getDrawThread() {
		return new DrawThread(mHolder, getContext());
	}
	protected ErrorCode checkErrorCode() {
		return ErrorDetector.checkLineCompareGraphObject(mLineGraphVO);
	}
		
	
	private class DrawThread extends AbsGraphView.BaseDrawThread{
		Bitmap b = Bitmap.createBitmap(getWidth(), getHeight(), Config.ARGB_8888);
		
		Matrix matrix = new Matrix();		
		
		Paint p = new Paint();
		Paint pCircle = new Paint();
		Paint pLine = new Paint();
		
		//animation
		float anim = 0.0f;
		boolean isAnimation = false;
		long animStartTime = -1;
		
		WeakHashMap<Integer, Bitmap> arrIcon = new WeakHashMap<Integer, Bitmap>();
		public DrawThread(SurfaceHolder holder, Context context) {
			super(context, holder, mLineGraphVO);
			
			int size = mLineGraphVO.getArrGraph().size();
			for (int i = 0; i < size; i++) {
				int bitmapResource = mLineGraphVO.getArrGraph().get(i).getBitmapResource();
				if(bitmapResource != -1){
					arrIcon.put(i, BitmapFactory.decodeResource(getResources(), bitmapResource));
				}else{
					if(arrIcon.get(i) != null){
						arrIcon.remove(i);
					}
				}
			}
		}
		
		@Override
		public void run() {
			Canvas canvas = null;
			GraphCanvasWrapper graphCanvasWrapper = null;
			Log.e(TAG,"height = " + height);
			Log.e(TAG,"width = " + width);
			
			setPaint();
			isAnimation();
			
			animStartTime = System.currentTimeMillis();
			
			while(isRun){
			
				//draw only on dirty mode
				if(!isDirty){
					try {
						Thread.sleep(100);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					continue;
				}
				
				canvas = mHolder.lockCanvas();
				graphCanvasWrapper = new GraphCanvasWrapper(canvas, width, height, mLineGraphVO.getPaddingLeft(), mLineGraphVO.getPaddingBottom());

				try {
					Thread.sleep(0000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				calcTimePass();
				
				synchronized(mHolder){
					synchronized (touchLock) {
						
						try {
							//bg color
							canvas.drawColor(Color.WHITE);
							if (mBackground != null) {
								canvas.drawBitmap(mBackground, 0, 0, null);
							}

							//x coord dot line
							drawBaseLine(graphCanvasWrapper);
							
							//y coord
							graphCanvasWrapper.drawLine(0, 0, 0, chartYLength, pAxisLine);
							
							//x coord
							graphCanvasWrapper.drawLine(0, 0, chartXLength, 0, pAxisLine);
							
							//x, y coord mark
							drawXMark(graphCanvasWrapper);
							drawYMark(graphCanvasWrapper);
							
							//x, y coord text
							drawXText(graphCanvasWrapper);
							drawYText(graphCanvasWrapper);
							
							//Graph
							drawGraphRegion(graphCanvasWrapper);
							drawGraph(graphCanvasWrapper);
							
							drawGraphName(canvas);
							
//							isDirty = false;
							

						} catch (Exception e) {
							e.printStackTrace();
						} finally {
							if(graphCanvasWrapper.getCanvas() != null){
								mHolder.unlockCanvasAndPost(graphCanvasWrapper.getCanvas());
							}
						}
						
					}
				}
			}
		}
		
		private void calcTimePass(){
			if(isAnimation){
				long curTime = System.currentTimeMillis();
				long gapTime = curTime - animStartTime;
				long animDuration = mLineGraphVO.getAnimation().getDuration();
				if(gapTime >= animDuration){
					gapTime = animDuration;
					isDirty = false;
				}
				
				anim = mLineGraphVO.getArrGraph().get(0).getCoordinateArr().length * (float)gapTime/(float)animDuration;
//				anim = anim + 0.1f;
			}else{
				isDirty = false;
			}
			
//			Log.e(TAG,"curTime = " + curTime + " , animStartTime = " + animStartTime);
//			Log.e(TAG,"anim = " + anim + " , gapTime = " + gapTime);
		}

		@Override
		public void drawGraphName(Canvas canvas) {
			GraphNameBox gnb = mLineGraphVO.getGraphNameBox();
			if(gnb != null){
				int nameboxWidth = 0;
				int nameboxHeight = 0;
				
				int nameboxIconWidth = gnb.getNameboxIconWidth();
				int nameboxIconHeight = gnb.getNameboxIconHeight();
				
				int nameboxMarginTop = gnb.getNameboxMarginTop();
				int nameboxMarginRight = gnb.getNameboxMarginRight();
				int nameboxPadding = gnb.getNameboxPadding();
				
				int nameboxTextIconMargin = gnb.getNameboxIconMargin();
				int nameboxIconMargin = gnb.getNameboxIconMargin();
				
				int maxTextWidth = 0;
				int maxTextHeight = 0;
				
				Paint nameRextPaint = getNameBoxBorderPaint(gnb);
				Paint pIcon = getNameBoxIconPaint(gnb);
				Paint pNameText = getNameBoxTextPaint(gnb);
				
				
				int graphSize = mLineGraphVO.getArrGraph().size();
				for (int i = 0; i < graphSize; i++) {
					
					
					String text = mLineGraphVO.getArrGraph().get(i).getName();
					Rect rect = new Rect();
					pNameText.getTextBounds(text, 0, text.length(), rect);
					
					if(rect.width() > maxTextWidth){
						maxTextWidth = rect.width();
						maxTextHeight = rect.height();
					}
					
					mLineGraphVO.getArrGraph().get(i).getName();
					
				}
				mLineGraphVO.getArrGraph().get(0).getName();
				nameboxWidth = 1 * maxTextWidth + nameboxTextIconMargin + nameboxIconWidth;
				int maxCellHight = maxTextHeight;
				if(nameboxIconHeight > maxTextHeight){
					maxCellHight = nameboxIconHeight;
				}
				nameboxHeight = graphSize * maxCellHight + (graphSize-1) * nameboxIconMargin;
				
				canvas.drawRect(width - (nameboxMarginRight + nameboxWidth) - nameboxPadding*2,
						nameboxMarginTop, width - nameboxMarginRight, nameboxMarginTop + nameboxHeight + nameboxPadding*2, nameRextPaint);
				
				for (int i = 0; i < graphSize; i++) {
					
					pIcon.setColor(mLineGraphVO.getArrGraph().get(i).getColor());
					canvas.drawRect(width - (nameboxMarginRight + nameboxWidth) - nameboxPadding,
							nameboxMarginTop + (maxCellHight * i) + nameboxPadding + (nameboxIconMargin * i), 
							width - (nameboxMarginRight + maxTextWidth) - nameboxPadding - nameboxTextIconMargin, 
							nameboxMarginTop + maxCellHight * (i+1) + nameboxPadding + nameboxIconMargin * i, pIcon);
					
					String text = mLineGraphVO.getArrGraph().get(i).getName();
					canvas.drawText(text, width - (nameboxMarginRight + maxTextWidth) - nameboxPadding, 
							nameboxMarginTop + maxTextHeight/2 + maxCellHight * i + maxCellHight/2 + nameboxPadding  + nameboxIconMargin * i, pNameText);
				}
			}
		}

		/**
		 * check graph line animation
		 */
		private void isAnimation() {
			if(mLineGraphVO.getAnimation() != null){
				isAnimation = true;
			}else{
				isAnimation = false;
			}
		}
		
		private void drawBaseLine(GraphCanvasWrapper graphCanvas) {
			for (int i = 1; mLineGraphVO.getIncrement() * i <= mLineGraphVO.getMaxValue(); i++) {
				
				float y = yLength * mLineGraphVO.getIncrement() * i/mLineGraphVO.getMaxValue();
				
				graphCanvas.drawLine(0, y, chartXLength, y, pBaseLine);
			}
		}

		/**
		 * set graph line color
		 */
		private void setPaint() {
			p = new Paint();
			p.setFlags(Paint.ANTI_ALIAS_FLAG);
			p.setAntiAlias(true); //text anti alias
			p.setFilterBitmap(true); // bitmap anti alias
			p.setColor(Color.BLUE);
			p.setStrokeWidth(3);
			p.setStyle(Style.STROKE);
			
			pCircle = new Paint();
			pCircle.setFlags(Paint.ANTI_ALIAS_FLAG);
			pCircle.setAntiAlias(true); //text anti alias
			pCircle.setFilterBitmap(true); // bitmap anti alias
			pCircle.setColor(Color.BLUE);
			pCircle.setStrokeWidth(3);
			pCircle.setStyle(Style.FILL_AND_STROKE);
			
			pLine = new Paint();
			pLine.setFlags(Paint.ANTI_ALIAS_FLAG);
			pLine.setAntiAlias(true); //text anti alias
			pLine.setFilterBitmap(true); // bitmap anti alias
			pLine.setShader(new LinearGradient(0, 300f, 0, 0f, Color.BLACK, Color.WHITE, Shader.TileMode.MIRROR));
		}

		/**
		 * draw Graph Region
		 */
		private void drawGraphRegion(GraphCanvasWrapper graphCanvas) {
			
			if (isAnimation){
				drawGraphCompareRegionWithAnimation(graphCanvas);
			}else{
				drawGraphCompareRegionWithoutAnimation(graphCanvas);
			}
		}
		
		/**
		 * draw Graph
		 */
		private void drawGraph(GraphCanvasWrapper graphCanvas) {
			
			if (isAnimation){
				drawGraphWithAnimation(graphCanvas);
			}else{
				drawGraphWithoutAnimation(graphCanvas);
			}
		}
		
		/**
		 *	draw graph without animation 
		 */
		private void drawGraphWithoutAnimation(GraphCanvasWrapper graphCanvas) {
			for (int i = 0; i < mLineGraphVO.getArrGraph().size(); i++) {
				GraphPath linePath = new GraphPath(width, height, mLineGraphVO.getPaddingLeft(), mLineGraphVO.getPaddingBottom());
				boolean firstSet = false;
				float x = 0;
				float y = 0;
				p.setColor(mLineGraphVO.getArrGraph().get(i).getColor());
				pCircle.setColor(mLineGraphVO.getArrGraph().get(i).getColor());
				float xGap = xLength/(mLineGraphVO.getArrGraph().get(i).getCoordinateArr().length-1);
				
				Bitmap icon = arrIcon.get(i);
				
				for (int j = 0; j < mLineGraphVO.getArrGraph().get(i).getCoordinateArr().length; j++) {
					if(j < mLineGraphVO.getArrGraph().get(i).getCoordinateArr().length){
						
						if (!firstSet) {
							
							x = xGap * j ;
							y = yLength * mLineGraphVO.getArrGraph().get(i).getCoordinateArr()[j]/mLineGraphVO.getMaxValue();
							
							linePath.moveTo(x, y);
							
							firstSet = true;
						} else {
							x = xGap * j;
							y = yLength * mLineGraphVO.getArrGraph().get(i).getCoordinateArr()[j]/mLineGraphVO.getMaxValue();
							
							linePath.lineTo(x, y);
							
						}
						
						if(icon == null){
							graphCanvas.drawCircle(x, y, 4, pCircle);
						}else{
							graphCanvas.drawBitmapIcon(icon, x, y, null);
						}
					}
				}
				
				graphCanvas.getCanvas().drawPath(linePath, p);
			}
		}
		
		private void drawGraphCompareRegionWithoutAnimation(GraphCanvasWrapper graphCanvas) {
			Canvas c = new Canvas(b);
			b.eraseColor(Color.TRANSPARENT);
			
			Paint pBg = new Paint();
			pBg.setFlags(Paint.ANTI_ALIAS_FLAG);
			pBg.setAntiAlias(true); //text anti alias
			pBg.setFilterBitmap(true); // bitmap anti alias
			pBg.setStyle(Style.FILL);
			
			ArrayList<GraphPath> arrLineBgPath = new ArrayList<GraphPath>();
			
			for (int i = 0; i < mLineGraphVO.getArrGraph().size(); i++) {
				GraphPath lineBgPath = new GraphPath(width, height, mLineGraphVO.getPaddingLeft(), mLineGraphVO.getPaddingBottom());
				boolean firstSet = false;
				float x = 0;
				float y = 0;
				p.setColor(mLineGraphVO.getArrGraph().get(i).getColor());
				pCircle.setColor(mLineGraphVO.getArrGraph().get(i).getColor());
				float xGap = xLength/(mLineGraphVO.getArrGraph().get(i).getCoordinateArr().length-1);
				
				for (int j = 0; j < mLineGraphVO.getArrGraph().get(i).getCoordinateArr().length; j++) {
					if(j < mLineGraphVO.getArrGraph().get(i).getCoordinateArr().length){
						
						if (!firstSet) {
							
							x = xGap * j ;
							y = yLength * mLineGraphVO.getArrGraph().get(i).getCoordinateArr()[j]/mLineGraphVO.getMaxValue();
							
							lineBgPath.moveTo(x, 0);
							lineBgPath.lineTo(x, y);
							firstSet = true;
						} else {
							x = xGap * j;
							y = yLength * mLineGraphVO.getArrGraph().get(i).getCoordinateArr()[j]/mLineGraphVO.getMaxValue();
							
							lineBgPath.lineTo(x, y);
						}
					}
				}
				
				lineBgPath.lineTo(x, 0);
				lineBgPath.lineTo(0, 0);
				arrLineBgPath.add(lineBgPath);
					
			}

			pBg.setColor(mLineGraphVO.getArrGraph().get(0).getColor());
			pBg.setAlpha(255);
			c.drawPath(arrLineBgPath.get(0), pBg);
			pBg.setXfermode(new PorterDuffXfermode(Mode.XOR));
			pBg.setColor(mLineGraphVO.getArrGraph().get(1).getColor());
			pBg.setAlpha(255);
			c.drawPath(arrLineBgPath.get(1), pBg);
			graphCanvas.getCanvas().drawBitmap(b, 0, 0, null);
		}

		/**
		 *	draw graph with animation 
		 */
		private void drawGraphWithAnimation(GraphCanvasWrapper graphCanvas) {
			//for draw animation
			float prev_x = 0;
			float prev_y = 0;
			
			float next_x = 0;
			float next_y = 0;
			
			float value = 0;
			float mode = 0;
			
			for (int i = 0; i < mLineGraphVO.getArrGraph().size(); i++) {
				GraphPath linePath = new GraphPath(width, height, mLineGraphVO.getPaddingLeft(), mLineGraphVO.getPaddingBottom());
				boolean firstSet = false;
				float x = 0;
				float y = 0;
				p.setColor(mLineGraphVO.getArrGraph().get(i).getColor());
				pCircle.setColor(mLineGraphVO.getArrGraph().get(i).getColor());
				float xGap = xLength/(mLineGraphVO.getArrGraph().get(i).getCoordinateArr().length-1);
				
				Bitmap icon = arrIcon.get(i);
				value = anim/1;
				mode = anim %1;
				
				for (int j = 0; j < value+1; j++) {
					if(j < mLineGraphVO.getArrGraph().get(i).getCoordinateArr().length){
						
						if (!firstSet) {
							
							x = xGap * j ;
							y = yLength * mLineGraphVO.getArrGraph().get(i).getCoordinateArr()[j]/mLineGraphVO.getMaxValue();
							
							linePath.moveTo(x, y);
							
							firstSet = true;
						} else {
							x = xGap * j;
							y = yLength * mLineGraphVO.getArrGraph().get(i).getCoordinateArr()[j]/mLineGraphVO.getMaxValue();
							
							if( j > value ){
								next_x = x - prev_x;
								next_y = y - prev_y;
								
								linePath.lineTo(prev_x + next_x * mode, prev_y + next_y * mode);
							}else{
								linePath.lineTo(x, y);
							}
						}
						
						if(icon == null){
							graphCanvas.drawCircle(x, y, 4, pCircle);
						}else{
							graphCanvas.drawBitmapIcon(icon, x, y, null);
						}
						prev_x = x;
						prev_y = y;
					}
				}
				
				graphCanvas.getCanvas().drawPath(linePath, p);
			}
		}
		
		/**
		 *	draw graph with animation 
		 */
		private void drawGraphCompareRegionWithAnimation(GraphCanvasWrapper graphCanvas) {
			//for draw animation
			float prev_x = 0;
			float prev_y = 0;
			
			float next_x = 0;
			float next_y = 0;
			
			int value = 0;
			float mode = 0;
			
			Canvas c = new Canvas(b);
			b.eraseColor(Color.TRANSPARENT);
			
			Paint pBg = new Paint();
			pBg.setFlags(Paint.ANTI_ALIAS_FLAG);
			pBg.setAntiAlias(true); //text anti alias
			pBg.setFilterBitmap(true); // bitmap anti alias
			pBg.setStyle(Style.FILL);
			
			ArrayList<GraphPath> arrLineBgPath = new ArrayList<GraphPath>();
			
			for (int i = 0; i < mLineGraphVO.getArrGraph().size(); i++) {
				GraphPath lineBgPath = new GraphPath(width, height, mLineGraphVO.getPaddingLeft(), mLineGraphVO.getPaddingBottom());
				
				boolean firstSet = false;
				float x = 0;
				float y = 0;
				p.setColor(mLineGraphVO.getArrGraph().get(i).getColor());
				pCircle.setColor(mLineGraphVO.getArrGraph().get(i).getColor());
				float xGap = xLength/(mLineGraphVO.getArrGraph().get(i).getCoordinateArr().length-1);
				
				value = (int) (anim/1);
				mode = anim %1;
				
//				Log.e("", "value = " + value + "\t ,mode = " + mode);
				
				for (int j = 0; j <= value+1; j++) {
					if(j < mLineGraphVO.getArrGraph().get(i).getCoordinateArr().length){
						
						if (!firstSet) {
							
							x = xGap * j ;
							y = yLength * mLineGraphVO.getArrGraph().get(i).getCoordinateArr()[j]/mLineGraphVO.getMaxValue();
							
							lineBgPath.moveTo(x, 0);
							lineBgPath.lineTo(x, y);
							
							firstSet = true;
						} else {
							x = xGap * j;
							y = yLength * mLineGraphVO.getArrGraph().get(i).getCoordinateArr()[j]/mLineGraphVO.getMaxValue();
							
							if( j > value ){
								next_x = x - prev_x;
								next_y = y - prev_y;
								
								lineBgPath.lineTo(prev_x + next_x * mode, prev_y + next_y * mode);
							}else{
								lineBgPath.lineTo(x, y);
							}
						}
						
						prev_x = x;
						prev_y = y;
						
//						Log.e("", "j = " + j + "\t x = " + x + "\t prev_x = " + prev_x);
					}
				}
//				Log.e("", "==================================");
//				Log.e("", "i = " + i + "\tprev_x = " + prev_x + "\t ,next_x * mode = " + (next_x * mode));
				float x_bg = prev_x + next_x * mode;
				if(x_bg >= xLength){
					x_bg = xLength;
				}
				lineBgPath.lineTo(x_bg, 0);
				lineBgPath.lineTo(0, 0);
				
				arrLineBgPath.add(lineBgPath);
			}
			
			pBg.setColor(mLineGraphVO.getArrGraph().get(0).getColor());
			pBg.setAlpha(255);
			c.drawPath(arrLineBgPath.get(0), pBg);
			pBg.setXfermode(new PorterDuffXfermode(Mode.XOR));
			pBg.setColor(mLineGraphVO.getArrGraph().get(1).getColor());
			pBg.setAlpha(255);
			c.drawPath(arrLineBgPath.get(1), pBg);
			graphCanvas.getCanvas().drawBitmap(b, 0, 0, null);
		}
		
		/**
		 * draw X Mark
		 */
		private void drawXMark(GraphCanvasWrapper graphCanvas) {
			float x = 0;
			float y = 0;
			
			float xGap = xLength/(mLineGraphVO.getArrGraph().get(0).getCoordinateArr().length-1);
			for (int i = 0; i < mLineGraphVO.getArrGraph().get(0).getCoordinateArr().length; i++) {
			        x = xGap * i;
			        y = yLength * mLineGraphVO.getArrGraph().get(0).getCoordinateArr()[i]/mLineGraphVO.getMaxValue();
			        
			    graphCanvas.drawLine(x, 0, x, -10, pAxisLine);
			}
		}
		
		/**
		 * draw Y Mark
		 */
		private void drawYMark(GraphCanvasWrapper canvas) {
			for (int i = 0; mLineGraphVO.getIncrement() * i <= mLineGraphVO.getMaxValue(); i++) {
				
				float y = yLength * mLineGraphVO.getIncrement() * i/mLineGraphVO.getMaxValue();
				
				canvas.drawLine(0, y, -10, y, pAxisLine);
			}
		}
		
		/**
		 * draw X Text
		 */
		private void drawXText(GraphCanvasWrapper graphCanvas) {
			float x = 0;
			float y = 0;
			
			float xGap = xLength/(mLineGraphVO.getArrGraph().get(0).getCoordinateArr().length-1);
			for (int i = 0; i < mLineGraphVO.getLegendArr().length; i++) {
			        x = xGap * i;
			        
			        String text = mLineGraphVO.getLegendArr()[i];
			        pMarkText.measureText(text);
			        pMarkText.setTextSize(mLineGraphVO.getXAxisTextSize());
					Rect rect = new Rect();
					pMarkText.getTextBounds(text, 0, text.length(), rect);
					
			    graphCanvas.drawText(text, x -(rect.width()/2), -(mLineGraphVO.getXAxisTextSize() + rect.height()), pMarkText);
			}
		}
		
		/**
		 * draw Y Text
		 */
		private void drawYText(GraphCanvasWrapper graphCanvas) {
			for (int i = 0; mLineGraphVO.getIncrement() * i <= mLineGraphVO.getMaxValue(); i++) {
				
				String mark = Float.toString(mLineGraphVO.getIncrement() * i);
				float y = yLength * mLineGraphVO.getIncrement() * i/mLineGraphVO.getMaxValue();
				pMarkText.measureText(mark);
				pMarkText.setTextSize(mLineGraphVO.getYAxisTextSize());
				Rect rect = new Rect();
				pMarkText.getTextBounds(mark, 0, mark.length(), rect);
//				Log.e(TAG, "rect = height()" + rect.height());
//				Log.e(TAG, "rect = width()" + rect.width());
				graphCanvas.drawText(mark, -(rect.width() + mLineGraphVO.getYAxisTextSize()), y-rect.height()/2, pMarkText);
			}
		}
	}
}
