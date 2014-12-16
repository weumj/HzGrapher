package com.handstudio.android.hzgrapherlib.graphview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.handstudio.android.hzgrapherlib.error.ErrorCode;
import com.handstudio.android.hzgrapherlib.vo.Graph;
import com.handstudio.android.hzgrapherlib.vo.GraphNameBox;

public abstract class AbsGraphView extends SurfaceView implements
		SurfaceHolder.Callback {

	BaseDrawThread mDrawThread;
	SurfaceHolder mHolder;

	// touch synchronize
	final Object touchLock = new Object();
	final String TAG = this.getClass().getSimpleName();

	public AbsGraphView(Context context, Graph graph) {
		super(context);
		mHolder = getHolder();
		ErrorCode ec = checkErrorCode();

		if (!ec.equals(ErrorCode.NOT_ERROR)) {
			ec.printError();
			// throw new RuntimeException(ec.toString());
		}

		mHolder.addCallback(this);
	}

	abstract BaseDrawThread getDrawThread();

	abstract ErrorCode checkErrorCode();

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.i(TAG, "surfaceCreated()");
		if (mDrawThread == null) {
			mDrawThread = getDrawThread();
			mDrawThread.start();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.i(TAG, "surfaceDestroyed()");
		if (mDrawThread != null) {
			mDrawThread.setRunFlag(false);
			mDrawThread = null;
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (mDrawThread == null) {
			return false;
		}

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
		case MotionEvent.ACTION_MOVE:
		case MotionEvent.ACTION_UP:
			synchronized (touchLock) {
				mDrawThread.isDirty = true;
			}
			return true;
		default:
			return super.onTouchEvent(event);
		}
	}
	
	abstract class BaseDrawThread extends Thread {
		Paint pAxisLine;
		Paint pMarkText;
		Paint pBaseLine;
		
		boolean isRun = true;
		boolean isDirty = true;
		
		SurfaceHolder mHolder;
		Context mContext;
		Graph mGraph;
		
		int height, width;
		// graph length
		int xLength, yLength;
		// chart length
		int chartXLength ,chartYLength;

		Bitmap mBackground = null;
		
		public abstract void drawGraphName(Canvas canvas);

		public BaseDrawThread(Context context, SurfaceHolder holder, Graph graph) {
			this.mContext = context;
			this.mHolder = holder;
			this.mGraph = graph;
			
			setGraphSizeAttribute();
			setPaintAttribute();
			setBackgroundBitmap();
		}
		
		private void setBackgroundBitmap(){
			int bgResource = mGraph.getGraphBG();
			if (bgResource != -1) {
				try{
					Bitmap tempBg = BitmapFactory.decodeResource(getResources(),
							bgResource);
					mBackground = Bitmap.createScaledBitmap(tempBg, width, height, true);
					tempBg.recycle();
				}catch(Exception e){
					ColorDrawable drawable = new ColorDrawable(getResources().getColor(bgResource));
					Bitmap tempBg= Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
					Canvas canvas = new Canvas(tempBg); 
					drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
					drawable.draw(canvas);
					mBackground = Bitmap.createScaledBitmap(tempBg, width, height, true);
				}
			}
		}
		
		private void setGraphSizeAttribute(){
			height = getHeight();
			width = getWidth();

			// graph length
			xLength = width
					- (mGraph.getPaddingLeft() + mGraph.getPaddingRight() + mGraph
							.getMarginRight());
			yLength = height
					- (mGraph.getPaddingBottom() + mGraph.getPaddingTop() + mGraph
							.getMarginTop());

			// chart length
			chartXLength = width
					- (mGraph.getPaddingLeft() + mGraph.getPaddingRight());
			chartYLength = height
					- (mGraph.getPaddingBottom() + mGraph.getPaddingTop());
		}
		
		private void setPaintAttribute(){
			// axis line
			// override by setAxisText()....
			pAxisLine = new Paint();
			pAxisLine.setFlags(Paint.ANTI_ALIAS_FLAG);
			pAxisLine.setAntiAlias(true); // text anti alias
			pAxisLine.setFilterBitmap(true); // bitmap anti alias
			pAxisLine.setStyle(Style.STROKE);
			pAxisLine.setColor(mGraph.getAxisLineColor());
			pAxisLine.setStrokeWidth(mGraph.getAxisLineWidth());

			// guide line
			pBaseLine = new Paint();
			pBaseLine.setFlags(Paint.ANTI_ALIAS_FLAG);
			pBaseLine.setAntiAlias(true);
			pBaseLine.setFilterBitmap(true);
			pBaseLine.setColor(mGraph.getBaseLineColor());
			pBaseLine.setStrokeWidth(mGraph.getBaseLineWidth());
			pBaseLine.setStyle(Style.STROKE);
			pBaseLine.setPathEffect(new DashPathEffect(new float[] { 10, 5 }, 0));

			// legend text
			pMarkText = new Paint();
			pMarkText.setFlags(Paint.ANTI_ALIAS_FLAG);
			pMarkText.setAntiAlias(true); // text anti alias
			pMarkText.setColor(mGraph.getMarkTextColor());
			pMarkText.setTextSize(mGraph.getMarkTextSize());
		}

		public Paint getNameBoxIconPaint(GraphNameBox nameBox) {
			Paint pIcon = new Paint();
			pIcon.setFlags(Paint.ANTI_ALIAS_FLAG);
			pIcon.setAntiAlias(true); // text anti alias
			pIcon.setFilterBitmap(true); // bitmap anti alias
			pIcon.setColor(Color.BLUE);
			pIcon.setStrokeWidth(3);
			pIcon.setStyle(Style.FILL_AND_STROKE);
			return pIcon;
		}

		public Paint getNameBoxBorderPaint(GraphNameBox nameBox) {
			Paint nameRextPaint = new Paint();
			nameRextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
			nameRextPaint.setAntiAlias(true); // text anti alias
			nameRextPaint.setFilterBitmap(true); // bitmap anti alias
			nameRextPaint.setColor(nameBox.getNameboxColor());
			nameRextPaint.setStrokeWidth(3);
			nameRextPaint.setStyle(Style.STROKE);
			return nameRextPaint;
		}

		public Paint getNameBoxTextPaint(GraphNameBox nameBox) {
			Paint pNameText = new Paint();
			pNameText.setFlags(Paint.ANTI_ALIAS_FLAG);
			pNameText.setAntiAlias(true); // text anti alias
			pNameText.setTextSize(nameBox.getNameboxTextSize());
			pNameText.setColor(nameBox.getNameboxTextColor());
			return pNameText;
		}

		public final void setRunFlag(boolean flag) {
			this.isRun = flag;
		}

		public final boolean getRunFlag() {
			return isRun;
		}

		public final boolean getDirty() {
			return isDirty;
		}

		public final void setDirty(boolean dirty) {
			isDirty = dirty;
		}
	}
}


