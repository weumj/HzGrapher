package com.handstudio.android.hzgrapherlib.graphview;

import java.util.WeakHashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.SurfaceHolder;

import com.handstudio.android.hzgrapherlib.canvas.GraphCanvasWrapper;
import com.handstudio.android.hzgrapherlib.error.ErrorCode;
import com.handstudio.android.hzgrapherlib.error.ErrorDetector;
import com.handstudio.android.hzgrapherlib.vo.GraphNameBox;
import com.handstudio.android.hzgrapherlib.vo.bargraph.BarGraph;
import com.handstudio.android.hzgrapherlib.vo.bargraph.BarGraphVO;

public class BarGraphView extends AbsGraphView {
	private BarGraphVO mBarGraphVO = null;

	// Constructor
	public BarGraphView(Context context, BarGraphVO vo) {
		super(context, vo);
		Log.i(TAG, "BarGraphView generator.");
		mBarGraphVO = vo;
	}

	@Override
	protected ErrorCode checkErrorCode() {
		return ErrorDetector.checkGraphObject(mBarGraphVO);
	}

	@Override
	protected BaseDrawThread getDrawThread() {
		return new DrawThread(mHolder, getContext());
	}

	private class DrawThread extends AbsGraphView.BaseDrawThread {
		Matrix matrix = new Matrix();
		Paint pCircle = new Paint();
		Paint pLine = new Paint();

		// animation
		float anim = 0.0f;
		boolean isAnimation = false;
		boolean isDrawRegion = false;
		long animStartTime = -1;

		WeakHashMap<Integer, Bitmap> arrIcon = new WeakHashMap<Integer, Bitmap>();
		
		public DrawThread(SurfaceHolder holder, Context context) {
			super(context, holder, mBarGraphVO);
		}

		@Override
		public void run() {
			Canvas canvas = null;
			GraphCanvasWrapper graphCanvasWrapper = null;
			animStartTime = System.currentTimeMillis();

			isAnimation();

			while (isRun) {
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
						height, mBarGraphVO.getPaddingLeft(),
						mBarGraphVO.getPaddingBottom());

				synchronized (mHolder) {
					synchronized (touchLock) {
						try {
							canvas.drawColor(Color.WHITE);
							if (mBackground != null) {
								canvas.drawBitmap(mBackground, 0, 0, null);
							}

							drawBaseLine(graphCanvasWrapper);
							drawBaseMark(graphCanvasWrapper);
							drawBaseText(graphCanvasWrapper);
							drawBaseLineGuide(graphCanvasWrapper);
							drawGraphName(canvas);

							if (isAnimation) {
								drawGraphWithAnimation(graphCanvasWrapper);
							} else {
								drawGraphWithoutAnimation(graphCanvasWrapper);
							}
						} catch (Exception e) {
							e.printStackTrace();
						} finally {
							if (graphCanvasWrapper.getCanvas() != null) {
								mHolder.unlockCanvasAndPost(canvas);
							}
						}
					}
				}
			}
		}

		private void isAnimation() {
			if (mBarGraphVO.isAnimationShow()) {
				isAnimation = true;
			} else {
				isAnimation = false;
			}
		}

		private void drawBaseLine(GraphCanvasWrapper graphCanvas) {
			graphCanvas.drawLine(0, 0, chartXLength, 0, pAxisLine);
			graphCanvas.drawLine(0, 0, 0, chartYLength, pAxisLine);
		}

		private void drawBaseMark(GraphCanvasWrapper graphCanvas) {
			// draw y axis
			for (int i = 1; mBarGraphVO.getIncrementY() * i <= mBarGraphVO
					.getMaxValueY(); i++) {
				float y = yLength * mBarGraphVO.getIncrementY() * i
						/ mBarGraphVO.getMaxValueY();
				graphCanvas.drawLine(0, y, -10, y, pAxisLine);
			}

			// for (int i = 1; mBarGraphVO.getIncrementX() * i <=
			// mBarGraphVO.getMaxValueX(); i++) {
			// float x = xLength * mBarGraphVO.getIncrementX() * i /
			// mBarGraphVO.getMaxValueX();
			// graphCanvas.drawLine(x, 0, x, -10, pAxisLine);
			// }

			// draw x axis
			for (int i = 0; i < mBarGraphVO.getLegendArr().length; i++) {
				float x = xLength * mBarGraphVO.getIncrementX() * (i + 1)
						/ mBarGraphVO.getMaxValueX();
				graphCanvas.drawLine(x, 0, x, -10, pAxisLine);
			}
		}

		private void drawBaseText(GraphCanvasWrapper graphCanvas) {
			// draw X axis
			// for (int i = 0; mBarGraphVO.getIncrementX() * i <=
			// mBarGraphVO.getMaxValueX(); i++){
			// float x = xLength * mBarGraphVO.getIncrementX() *
			// i/mBarGraphVO.getMaxValueX();
			// String mark = Float.toString(mBarGraphVO.getIncrementX() * i);
			// pMarkText.measureText(mark);
			// pMarkText.setTextSize(20);
			// Rect rect = new Rect();
			// pMarkText.getTextBounds(mark, 0, mark.length(), rect);
			// graphCanvas.drawText(mark, x -(rect.width()/2), -(20 +
			// rect.height()), pMarkText);
			// }
			for (int i = 0; i < mBarGraphVO.getLegendArr().length; i++) {
				float x = xLength * mBarGraphVO.getIncrementX() * (i + 1)
						/ mBarGraphVO.getMaxValueX();
				String mark = mBarGraphVO.getLegendArr()[i];
				pMarkText.measureText(mark);
				pMarkText.setTextSize(mBarGraphVO.getXAxisTextSize());
				Rect rect = new Rect();
				pMarkText.getTextBounds(mark, 0, mark.length(), rect);
				graphCanvas.drawText(mark, x - (rect.width() / 2),
						-(mBarGraphVO.getXAxisTextSize() + rect.height()),
						pMarkText);
			}

			// draw Y axis
			for (int i = 0; mBarGraphVO.getIncrementY() * i <= mBarGraphVO
					.getMaxValueY(); i++) {
				String mark = Float.toString(mBarGraphVO.getIncrementY() * i);
				float y = yLength * mBarGraphVO.getIncrementY() * i
						/ mBarGraphVO.getMaxValueY();
				pMarkText.measureText(mark);
				pMarkText.setTextSize(mBarGraphVO.getYAxisTextSize());
				Rect rect = new Rect();
				pMarkText.getTextBounds(mark, 0, mark.length(), rect);
				graphCanvas.drawText(mark,
						-(rect.width() + mBarGraphVO.getYAxisTextSize()), y
								- rect.height() / 2, pMarkText);
			}
		}

		private void drawBaseLineGuide(GraphCanvasWrapper graphCanvas) {
			// draw Y axis
			for (int i = 1; mBarGraphVO.getIncrementY() * i <= mBarGraphVO
					.getMaxValueY(); i++) {
				float y = yLength * mBarGraphVO.getIncrementY() * i
						/ mBarGraphVO.getMaxValueY();
				graphCanvas.drawLine(0, y, chartXLength, y, pBaseLine);
			}

			// for (int i = 1; mBarGraphVO.getIncrementY() * i <=
			// mBarGraphVO.getMaxValueY(); i++) {
			// float x = xLength * mBarGraphVO.getIncrementY() * i /
			// mBarGraphVO.getMaxValueY();
			// graphCanvas.drawLine(x, 0, x, chartYLength, pBaseLineD);
			// }
		}

		@Override
		public void drawGraphName(Canvas canvas) {
			GraphNameBox gnb = mBarGraphVO.getGraphNameBox();
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

				int graphSize = mBarGraphVO.getArrGraph().size();
				for (int i = 0; i < graphSize; i++) {
					String text = mBarGraphVO.getArrGraph().get(i).getName();
					Rect rect = new Rect();
					pNameText.getTextBounds(text, 0, text.length(), rect);
					if (rect.width() > maxTextWidth) {
						maxTextWidth = rect.width();
						maxTextHeight = rect.height();
					}
					mBarGraphVO.getArrGraph().get(i).getName();
				}

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
					BarGraph graph = mBarGraphVO.getArrGraph().get(i);

					pIcon.setColor(graph.getColor());
					canvas.drawRect(width - (nameboxMarginRight + nameboxWidth)
							- nameboxPadding, nameboxMarginTop
							+ (maxCellHight * i) + nameboxPadding
							+ (nameboxIconMargin * i), width
							- (nameboxMarginRight + maxTextWidth)
							- nameboxPadding - nameboxTextIconMargin,
							nameboxMarginTop + maxCellHight * (i + 1)
									+ nameboxPadding + nameboxIconMargin * i,
							pIcon);
					String text = graph.getName();
					canvas.drawText(text, width
							- (nameboxMarginRight + maxTextWidth)
							- nameboxPadding, nameboxMarginTop + maxTextHeight
							/ 2 + maxCellHight * i + maxCellHight / 2
							+ nameboxPadding + nameboxIconMargin * i, pNameText);

				}
			}
		}

		private void drawGraphWithoutAnimation(GraphCanvasWrapper canvas) {
			Log.d(TAG, "drawGraphWithoutAnimation");
			Paint barGraphRegionPaint = new Paint();
			barGraphRegionPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
			barGraphRegionPaint.setAntiAlias(true); // text anti alias
			barGraphRegionPaint.setFilterBitmap(true); // bitmap anti alias
			barGraphRegionPaint.setStrokeWidth(0);

			Paint barPercentPaint = new Paint();
			barPercentPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
			barPercentPaint.setAntiAlias(true);
			barPercentPaint.setColor(Color.WHITE);
			barPercentPaint.setTextSize(20);

			float yBottom = 0;
			float yBottomOld = 0;

			// x축 반복
			for (int i = 0; i < mBarGraphVO.getLegendArr().length; i++) {
				float xLeft = xLength * mBarGraphVO.getIncrementX() * (i + 1)
						/ mBarGraphVO.getMaxValueX()
						- mBarGraphVO.getBarWidth() / 2;
				float xRight = xLeft + mBarGraphVO.getBarWidth();

				float totalYLength = 0;
				for (int j = 0; j < mBarGraphVO.getArrGraph().size(); j++) {
					totalYLength += yLength
							* mBarGraphVO.getArrGraph().get(j)
									.getCoordinateArr()[i]
							/ mBarGraphVO.getMaxValueY();
				}

				// x축 각 섹션별 반복
				for (int j = 0; j < mBarGraphVO.getArrGraph().size(); j++) {
					BarGraph graph = mBarGraphVO.getArrGraph().get(j);

					yBottomOld = yBottom;
					yBottom += yLength * graph.getCoordinateArr()[i]
							/ mBarGraphVO.getMaxValueY();

					barGraphRegionPaint.setColor(mBarGraphVO.getArrGraph()
							.get(j).getColor());

					canvas.drawRect(xLeft, yBottomOld, xRight, yBottom,
							barGraphRegionPaint);

					int percentage = (int) (((yBottom - yBottomOld) * 100) / totalYLength);
					if (percentage != 0) {
						String mark = String.valueOf(percentage) + "%";
						barPercentPaint.measureText(mark);
						Rect rect = new Rect();
						barPercentPaint.getTextBounds(mark, 0, mark.length(),
								rect);
						canvas.drawText(mark, xRight - ((xRight - xLeft) / 2)
								- rect.width() / 2, yBottom
								- ((yBottom - yBottomOld) / 2) - rect.height()
								/ 2, barPercentPaint);
					}
				}

				yBottom = 0;
			}
		}

		private void drawGraphWithAnimation(GraphCanvasWrapper canvas) {
			Log.d(TAG, "drawGraphWithAnimation");
			Paint barGraphRegionPaint = new Paint();
			barGraphRegionPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
			barGraphRegionPaint.setAntiAlias(true); // text anti alias
			barGraphRegionPaint.setFilterBitmap(true); // bitmap anti alias
			barGraphRegionPaint.setStrokeWidth(0);

			// Paint barPercentPaint = new Paint();
			// barPercentPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
			// barPercentPaint.setAntiAlias(true);
			// barPercentPaint.setColor(Color.WHITE);
			// barPercentPaint.setTextSize(20);

			long curTime = System.currentTimeMillis();
			long gapTime = curTime - animStartTime;
			long totalAnimDuration = mBarGraphVO.getAnimation().getDuration();

			if (gapTime >= totalAnimDuration) {
				gapTime = totalAnimDuration;
				isDirty = false;
			}

			float yBottomOld = 0;

			// x축 반복
			for (int i = 0; i < mBarGraphVO.getLegendArr().length; i++) {
				float xLeft = xLength * mBarGraphVO.getIncrementX() * (i + 1)
						/ mBarGraphVO.getMaxValueX()
						- mBarGraphVO.getBarWidth() / 2;
				float xRight = xLeft + mBarGraphVO.getBarWidth();

				float totalYLength = 0;
				for (int j = 0; j < mBarGraphVO.getArrGraph().size(); j++) {
					totalYLength += yLength
							* mBarGraphVO.getArrGraph().get(j)
									.getCoordinateArr()[i]
							/ mBarGraphVO.getMaxValueY();
				}

				float yGap = (totalYLength / totalAnimDuration) * gapTime;
				Log.d(TAG, "yGap = " + yGap);

				barGraphRegionPaint.setColor(mBarGraphVO.getArrGraph().get(0)
						.getColor());
				canvas.drawRect(xLeft, yBottomOld, xRight, yGap,
						barGraphRegionPaint);
			}
		}
	}
}
