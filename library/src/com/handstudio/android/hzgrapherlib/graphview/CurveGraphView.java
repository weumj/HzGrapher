package com.handstudio.android.hzgrapherlib.graphview;

import java.util.WeakHashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.Shader;
import android.util.Log;
import android.view.SurfaceHolder;

import com.handstudio.android.hzgrapherlib.animation.GraphAnimation;
import com.handstudio.android.hzgrapherlib.canvas.GraphCanvasWrapper;
import com.handstudio.android.hzgrapherlib.error.ErrorCode;
import com.handstudio.android.hzgrapherlib.error.ErrorDetector;
import com.handstudio.android.hzgrapherlib.path.GraphPath;
import com.handstudio.android.hzgrapherlib.util.Spline;
import com.handstudio.android.hzgrapherlib.vo.GraphNameBox;
import com.handstudio.android.hzgrapherlib.vo.curvegraph.CurveGraphVO;

public class CurveGraphView extends AbsGraphView{
	private CurveGraphVO mCurveGraphVO = null;
	private Spline spline = null;

	// Constructor
	public CurveGraphView(Context context, CurveGraphVO vo) {
		super(context, vo);
		mCurveGraphVO = vo;
	}

	@Override
	protected ErrorCode checkErrorCode() {
		return ErrorDetector.checkGraphObject(mCurveGraphVO);
	}

	@Override
	protected BaseDrawThread getDrawThread() {
		return new DrawThread(mHolder, getContext());
	}

	private class DrawThread extends AbsGraphView.BaseDrawThread {
		Matrix matrix = new Matrix();

		Paint p = new Paint();
		Paint pCircle = new Paint();
		Paint pCurve = new Paint();

		// animation
		float anim = 0.0f;
		boolean isAnimation = false;
		boolean isDrawRegion = false;
		long animStartTime = -1;
		int animationType = 0;

		WeakHashMap<Integer, Bitmap> arrIcon = new WeakHashMap<Integer, Bitmap>();
		
		public DrawThread(SurfaceHolder holder, Context context) {
			super(context, holder, mCurveGraphVO);

			int size = mCurveGraphVO.getArrGraph().size();
			for (int i = 0; i < size; i++) {
				int bitmapResource = mCurveGraphVO.getArrGraph().get(i)
						.getBitmapResource();
				if (bitmapResource != -1) {
					arrIcon.put(i, BitmapFactory.decodeResource(getResources(),
							bitmapResource));
				} else {
					if (arrIcon.get(i) != null) {
						arrIcon.remove(i);
					}
				}
			}
		}

		@Override
		public void run() {
			Canvas canvas = null;
			GraphCanvasWrapper graphCanvasWrapper = null;
			Log.e(TAG, "height = " + height);
			Log.e(TAG, "width = " + width);

			setPaint();
			isAnimation();
			isDrawRegion();

			animStartTime = System.currentTimeMillis();

			while (isRun) {

				// draw only on dirty mode
				if (!isDirty) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					continue;
				}

				canvas = mHolder.lockCanvas();
				graphCanvasWrapper = new GraphCanvasWrapper(canvas, width,
						height, mCurveGraphVO.getPaddingLeft(),
						mCurveGraphVO.getPaddingBottom());

				try {
					Thread.sleep(0000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				calcTimePass();

				synchronized (mHolder) {
					synchronized (touchLock) {

						try {
							// bg color
							canvas.drawColor(Color.WHITE);
							if (mBackground != null) {
								canvas.drawBitmap(mBackground, 0, 0, null);
							}

							// x coord dot Line
							drawBaseLine(graphCanvasWrapper);

							// y coord
							graphCanvasWrapper.drawLine(0, 0, 0, chartYLength,
									pAxisLine);

							// x coord
							graphCanvasWrapper.drawLine(0, 0, chartXLength, 0,
									pAxisLine);

							// x, y coord mark
							drawXMark(graphCanvasWrapper);
							drawYMark(graphCanvasWrapper);

							// x, y coord text
							drawXText(graphCanvasWrapper);
							drawYText(graphCanvasWrapper);

							// Draw Graph
							drawGraphRegion(graphCanvasWrapper);
							drawGraph(graphCanvasWrapper);

							// Draw group name
							drawGraphName(canvas);

						} catch (Exception e) {
							e.printStackTrace();
						} finally {
							if (graphCanvasWrapper.getCanvas() != null) {
								mHolder.unlockCanvasAndPost(graphCanvasWrapper
										.getCanvas());
							}
						}

					}
				}
			}
		}

		/**
		 * time calculate
		 */
		private void calcTimePass() {
			if (isAnimation) {
				long curTime = System.currentTimeMillis();
				long gapTime = curTime - animStartTime;
				long animDuration = mCurveGraphVO.getAnimation().getDuration();
				if (gapTime >= animDuration)
					gapTime = animDuration;

				anim = (float) gapTime / (float) animDuration;
			} else {
				isDirty = false;
			}
		}

		/**
		 * draw graph name
		 */
		@Override
		public void drawGraphName(Canvas canvas) {
			GraphNameBox gnb = mCurveGraphVO.getGraphNameBox();
			if (gnb != null) {
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

				int graphSize = mCurveGraphVO.getArrGraph().size();
				for (int i = 0; i < graphSize; i++) {

					String text = mCurveGraphVO.getArrGraph().get(i).getName();
					Rect rect = new Rect();
					pNameText.getTextBounds(text, 0, text.length(), rect);

					if (rect.width() > maxTextWidth) {
						maxTextWidth = rect.width();
						maxTextHeight = rect.height();
					}

					mCurveGraphVO.getArrGraph().get(i).getName();

				}
				mCurveGraphVO.getArrGraph().get(0).getName();
				nameboxWidth = 1 * maxTextWidth + nameboxTextIconMargin
						+ nameboxIconWidth;
				int maxCellHight = maxTextHeight;
				if (nameboxIconHeight > maxTextHeight) {
					maxCellHight = nameboxIconHeight;
				}
				nameboxHeight = graphSize * maxCellHight + (graphSize - 1)
						* nameboxIconMargin;

				canvas.drawRect(width - (nameboxMarginRight + nameboxWidth)
						- nameboxPadding * 2, nameboxMarginTop, width
						- nameboxMarginRight, nameboxMarginTop + nameboxHeight
						+ nameboxPadding * 2, nameRextPaint);

				for (int i = 0; i < graphSize; i++) {

					pIcon.setColor(mCurveGraphVO.getArrGraph().get(i)
							.getColor());
					canvas.drawRect(width - (nameboxMarginRight + nameboxWidth)
							- nameboxPadding, nameboxMarginTop
							+ (maxCellHight * i) + nameboxPadding
							+ (nameboxIconMargin * i), width
							- (nameboxMarginRight + maxTextWidth)
							- nameboxPadding - nameboxTextIconMargin,
							nameboxMarginTop + maxCellHight * (i + 1)
									+ nameboxPadding + nameboxIconMargin * i,
							pIcon);

					String text = mCurveGraphVO.getArrGraph().get(i).getName();
					canvas.drawText(text, width
							- (nameboxMarginRight + maxTextWidth)
							- nameboxPadding, nameboxMarginTop + maxTextHeight
							/ 2 + maxCellHight * i + maxCellHight / 2
							+ nameboxPadding + nameboxIconMargin * i, pNameText);
				}
			}
		}

		/**
		 * check graph Curve animation
		 */
		private void isAnimation() {
			if (mCurveGraphVO.getAnimation() != null) {
				isAnimation = true;
			} else {
				isAnimation = false;
			}
			animationType = mCurveGraphVO.getAnimation().getAnimation();
		}

		/**
		 * check graph Curve region animation
		 */
		private void isDrawRegion() {
			if (mCurveGraphVO.isDrawRegion()) {
				isDrawRegion = true;
			} else {
				isDrawRegion = false;
			}
		}

		/**
		 * draw BaseDrawThread Line
		 */
		private void drawBaseLine(GraphCanvasWrapper graphCanvas) {
			for (int i = 1; mCurveGraphVO.getIncrement() * i <= mCurveGraphVO
					.getMaxValue(); i++) {

				float y = yLength * mCurveGraphVO.getIncrement() * i
						/ mCurveGraphVO.getMaxValue();

				graphCanvas.drawLine(0, y, chartXLength, y, pBaseLine);
			}
		}

		/**
		 * set graph Curve color
		 */
		private void setPaint() {
			p = new Paint();
			p.setFlags(Paint.ANTI_ALIAS_FLAG);
			p.setAntiAlias(true); // text anti alias
			p.setFilterBitmap(true); // bitmap anti alias
			p.setColor(Color.BLUE);
			p.setStrokeWidth(3);
			p.setStyle(Style.STROKE);

			pCircle = new Paint();
			pCircle.setFlags(Paint.ANTI_ALIAS_FLAG);
			pCircle.setAntiAlias(true); // text anti alias
			pCircle.setFilterBitmap(true); // bitmap anti alias
			pCircle.setColor(Color.BLUE);
			pCircle.setStrokeWidth(3);
			pCircle.setStyle(Style.FILL_AND_STROKE);

			pCurve = new Paint();
			pCurve.setFlags(Paint.ANTI_ALIAS_FLAG);
			pCurve.setAntiAlias(true); // text anti alias
			pCurve.setFilterBitmap(true); // bitmap anti alias
			pCurve.setShader(new LinearGradient(0, 300f, 0, 0f, Color.BLACK,
					Color.WHITE, Shader.TileMode.MIRROR));
		}

		/**
		 * draw Graph Region
		 */
		private void drawGraphRegion(GraphCanvasWrapper graphCanvas) {
			if (isDrawRegion) {
				if (isAnimation) {
					drawGraphRegionWithAnimation(graphCanvas);
				} else {
					drawGraphRegionWithoutAnimation(graphCanvas);
				}
			}
		}

		/**
		 * draw Graph
		 */
		private void drawGraph(GraphCanvasWrapper graphCanvas) {
			if (isAnimation) {
				drawGraphWithAnimation(graphCanvas);
			} else {
				drawGraphWithoutAnimation(graphCanvas);
			}
		}

		/**
		 * draw graph without animation
		 */
		private void drawGraphRegionWithoutAnimation(
				GraphCanvasWrapper graphCanvas) {
			boolean isDrawRegion = mCurveGraphVO.isDrawRegion();

			for (int i = 0; i < mCurveGraphVO.getArrGraph().size(); i++) {
				GraphPath regionPath = new GraphPath(width, height,
						mCurveGraphVO.getPaddingLeft(),
						mCurveGraphVO.getPaddingBottom());

				boolean firstSet = false;
				float xGap = xLength
						/ (mCurveGraphVO.getArrGraph().get(i)
								.getCoordinateArr().length - 1);

				float[] x = setAxisX(xGap, i);
				float[] y = setAxisY(i);
				// Creates a monotone cubic spline from a given set of control
				// points.
				spline = Spline.createMonotoneCubicSpline(x, y);

				p.setColor(mCurveGraphVO.getArrGraph().get(i).getColor());
				pCircle.setColor(mCurveGraphVO.getArrGraph().get(i).getColor());

				spline = Spline.createMonotoneCubicSpline(x, y);

				// draw Region
				for (float j = x[0]; j < x[x.length - 1]; j++) {
					if (!firstSet) {
						regionPath.moveTo(j, spline.interpolate(j));
						firstSet = true;
					} else
						regionPath.lineTo((j + 1), spline.interpolate((j + 1)));
				}

				if (isDrawRegion) {
					regionPath.lineTo(x[x.length - 1], 0);
					regionPath.lineTo(0, 0);

					Paint pBg = new Paint();
					pBg.setFlags(Paint.ANTI_ALIAS_FLAG);
					pBg.setAntiAlias(true); // text anti alias
					pBg.setFilterBitmap(true); // bitmap anti alias
					pBg.setStyle(Style.FILL);
					pBg.setColor(mCurveGraphVO.getArrGraph().get(i).getColor());
					graphCanvas.getCanvas().drawPath(regionPath, pBg);
				}
			}
		}

		/**
		 * draw graph with animation
		 */
		private void drawGraphRegionWithAnimation(GraphCanvasWrapper graphCanvas) {
			// for draw animation
			boolean isDrawRegion = mCurveGraphVO.isDrawRegion();

			for (int i = 0; i < mCurveGraphVO.getArrGraph().size(); i++) {
				GraphPath regionPath = new GraphPath(width, height,
						mCurveGraphVO.getPaddingLeft(),
						mCurveGraphVO.getPaddingBottom());

				boolean firstSet = false;
				float xGap = xLength
						/ (mCurveGraphVO.getArrGraph().get(i)
								.getCoordinateArr().length - 1);

				float moveX = 0;
				float[] x = setAxisX(xGap, i);
				float[] y = setAxisY(i);
				// Creates a monotone cubic spline from a given set of control
				// points.
				spline = Spline.createMonotoneCubicSpline(x, y);

				p.setColor(mCurveGraphVO.getArrGraph().get(i).getColor());
				pCircle.setColor(mCurveGraphVO.getArrGraph().get(i).getColor());

				// draw line
				for (float j = x[0]; j <= x[x.length - 1]; j++) {
					if (!firstSet) {
						regionPath.moveTo(j, spline.interpolate(j));
						firstSet = true;

					} else {
						moveX = j * anim;
						regionPath.lineTo(moveX, spline.interpolate(moveX));
					}
				}

				if (isDrawRegion) {
					if (animationType == GraphAnimation.CURVE_REGION_ANIMATION_1) {
						moveX += xGap * anim;
						if (moveX >= xLength) {
							moveX = xLength;
						}
					}

					regionPath.lineTo(moveX, 0);
					regionPath.lineTo(0, 0);

					Paint pBg = new Paint();
					pBg.setFlags(Paint.ANTI_ALIAS_FLAG);
					pBg.setAntiAlias(true); // text anti alias
					pBg.setFilterBitmap(true); // bitmap anti alias
					pBg.setStyle(Style.FILL);
					pBg.setColor(mCurveGraphVO.getArrGraph().get(i).getColor());
					graphCanvas.getCanvas().drawPath(regionPath, pBg);
				}
				if (anim == 1)
					isDirty = false;
			}
		}

		/**
		 * draw graph without animation
		 */
		private void drawGraphWithoutAnimation(GraphCanvasWrapper graphCanvas) {
			for (int i = 0; i < mCurveGraphVO.getArrGraph().size(); i++) {
				GraphPath curvePath = new GraphPath(width, height,
						mCurveGraphVO.getPaddingLeft(),
						mCurveGraphVO.getPaddingBottom());

				boolean firstSet = false;
				float xGap = xLength
						/ (mCurveGraphVO.getArrGraph().get(i)
								.getCoordinateArr().length - 1);

				float[] x = setAxisX(xGap, i);
				float[] y = setAxisY(i);
				// Creates a monotone cubic spline from a given set of control
				// points.
				spline = Spline.createMonotoneCubicSpline(x, y);

				p.setColor(mCurveGraphVO.getArrGraph().get(i).getColor());
				pCircle.setColor(mCurveGraphVO.getArrGraph().get(i).getColor());

				Bitmap icon = arrIcon.get(i);

				// draw line
				for (float j = x[0]; j < x[x.length - 1]; j++) {
					if (!firstSet) {
						curvePath.moveTo(j, spline.interpolate(j));
						firstSet = true;
					} else
						curvePath.lineTo((j + 1), spline.interpolate((j + 1)));
				}

				// draw point
				for (int j = 0; j < mCurveGraphVO.getArrGraph().get(i)
						.getCoordinateArr().length; j++) {
					float pointX = xGap * j;
					float pointY = yLength
							* mCurveGraphVO.getArrGraph().get(i)
									.getCoordinateArr()[j]
							/ mCurveGraphVO.getMaxValue();

					if (icon == null) {
						graphCanvas.drawCircle(pointX, pointY, 4, pCircle);
					} else {
						graphCanvas.drawBitmapIcon(icon, pointX, pointY, null);
					}
				}
				graphCanvas.getCanvas().drawPath(curvePath, p);
			}
		}

		/**
		 * draw graph with animation
		 */
		private void drawGraphWithAnimation(GraphCanvasWrapper graphCanvas) {
			// for draw animation
			for (int i = 0; i < mCurveGraphVO.getArrGraph().size(); i++) {
				GraphPath curvePath = new GraphPath(width, height,
						mCurveGraphVO.getPaddingLeft(),
						mCurveGraphVO.getPaddingBottom());

				boolean firstSet = false;
				float xGap = xLength
						/ (mCurveGraphVO.getArrGraph().get(i)
								.getCoordinateArr().length - 1);
				float pointNum = (mCurveGraphVO.getArrGraph().get(0)
						.getCoordinateArr().length * anim) / 1;

				float[] x = setAxisX(xGap, i);
				float[] y = setAxisY(i);
				// Creates a monotone cubic spline from a given set of control
				// points.
				spline = Spline.createMonotoneCubicSpline(x, y);

				p.setColor(mCurveGraphVO.getArrGraph().get(i).getColor());
				pCircle.setColor(mCurveGraphVO.getArrGraph().get(i).getColor());

				Bitmap icon = arrIcon.get(i);

				// draw line
				for (float j = x[0]; j <= x[x.length - 1]; j++) {
					if (!firstSet) {
						curvePath.moveTo(j, spline.interpolate(j));
						firstSet = true;

					} else {
						curvePath.lineTo(((j) * anim),
								spline.interpolate(((j) * anim)));
					}
				}
				graphCanvas.getCanvas().drawPath(curvePath, p);

				// draw point
				for (int j = 0; j < pointNum + 1; j++) {
					if (j < mCurveGraphVO.getArrGraph().get(i)
							.getCoordinateArr().length) {
						if (icon == null) {
							graphCanvas.drawCircle(x[j], y[j], 4, pCircle);
						} else {
							graphCanvas.drawBitmapIcon(icon, x[j], y[j], null);
						}
					}
				}
				if (anim == 1)
					isDirty = false;
			}
		}

		/**
		 * draw X Mark
		 */
		private void drawXMark(GraphCanvasWrapper graphCanvas) {
			float x = 0;

			float xGap = xLength
					/ (mCurveGraphVO.getArrGraph().get(0).getCoordinateArr().length - 1);
			for (int i = 0; i < mCurveGraphVO.getArrGraph().get(0)
					.getCoordinateArr().length; i++) {
				x = xGap * i;

				graphCanvas.drawLine(x, 0, x, -10, pAxisLine);
			}
		}

		/**
		 * draw Y Mark
		 */
		private void drawYMark(GraphCanvasWrapper canvas) {
			for (int i = 0; mCurveGraphVO.getIncrement() * i <= mCurveGraphVO
					.getMaxValue(); i++) {

				float y = yLength * mCurveGraphVO.getIncrement() * i
						/ mCurveGraphVO.getMaxValue();

				canvas.drawLine(0, y, -10, y, pAxisLine);
			}
		}

		/**
		 * draw X Text
		 */
		private void drawXText(GraphCanvasWrapper graphCanvas) {
			float x = 0;

			float xGap = xLength
					/ (mCurveGraphVO.getArrGraph().get(0).getCoordinateArr().length - 1);
			for (int i = 0; i < mCurveGraphVO.getLegendArr().length; i++) {
				x = xGap * i;

				String text = mCurveGraphVO.getLegendArr()[i];
				pMarkText.measureText(text);
				pMarkText.setTextSize(mCurveGraphVO.getXAxisTextSize());
				Rect rect = new Rect();
				pMarkText.getTextBounds(text, 0, text.length(), rect);

				graphCanvas.drawText(text, x - (rect.width() / 2),
						-(mCurveGraphVO.getXAxisTextSize() + rect.height()),
						pMarkText);
			}
		}

		/**
		 * draw Y Text
		 */
		private void drawYText(GraphCanvasWrapper graphCanvas) {
			for (int i = 0; mCurveGraphVO.getIncrement() * i <= mCurveGraphVO
					.getMaxValue(); i++) {

				String mark = Float.toString(mCurveGraphVO.getIncrement() * i);
				float y = yLength * mCurveGraphVO.getIncrement() * i
						/ mCurveGraphVO.getMaxValue();
				pMarkText.measureText(mark);
				pMarkText.setTextSize(mCurveGraphVO.getYAxisTextSize());
				Rect rect = new Rect();
				pMarkText.getTextBounds(mark, 0, mark.length(), rect);
				// Log.e(TAG, "rect = height()" + rect.height());
				// Log.e(TAG, "rect = width()" + rect.width());
				graphCanvas.drawText(mark,
						-(rect.width() + mCurveGraphVO.getYAxisTextSize()), y
								- rect.height() / 2, pMarkText);
			}
		}

		/**
		 * set point X Coordinate
		 */
		private float[] setAxisX(float xGap, int graphNum) {
			float[] axisX = new float[mCurveGraphVO.getArrGraph().get(graphNum)
					.getCoordinateArr().length];

			for (int i = 0; i < mCurveGraphVO.getArrGraph().get(graphNum)
					.getCoordinateArr().length; i++) {
				axisX[i] = xGap * i;
			}
			return axisX;
		}

		/**
		 * set point Y Coordinate
		 */
		private float[] setAxisY(int graphNum) {
			float[] axisY = new float[mCurveGraphVO.getArrGraph().get(graphNum)
					.getCoordinateArr().length];

			for (int i = 0; i < mCurveGraphVO.getArrGraph().get(graphNum)
					.getCoordinateArr().length; i++) {
				axisY[i] = yLength
						* mCurveGraphVO.getArrGraph().get(graphNum)
								.getCoordinateArr()[i]
						/ mCurveGraphVO.getMaxValue();
				;
			}
			return axisY;
		}
	}
}
