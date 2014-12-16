package com.handstudio.android.hzgrapherlib.vo;

/**
 * 2-Dimension Graph
 * 
 */
public class TwoDimensionGraph extends Graph {
	/**
	 * 
	 */
	public static final float DEFAULT_TEXT_SIZE = 20;
	/**
	 */
	private float xAxisTextSize = DEFAULT_TEXT_SIZE;
	/**
	 */
	private float yAxisTextSize = DEFAULT_TEXT_SIZE;

	/**
	 */
	public TwoDimensionGraph() {
		super();
	}

	/**
	 */
	public TwoDimensionGraph(int paddingBottom, int paddingTop, int paddingLeft,
			int paddingRight, int marginTop, int marginRight) {
		super(paddingBottom, paddingTop, paddingLeft, paddingRight, marginTop,
				marginRight);
	}

	/**
	 * gets size for X axis text.
	 * 
	 */
	public float getXAxisTextSize() {
		return xAxisTextSize;
	}

	/**
	 * sets size for X axis text.
	 * 
	 */
	public void setXAxisTextSize(float textX) {
		if (textX > 0)
			this.xAxisTextSize = textX;
	}

	/**
	 * gets size for Y axis text.
	 * 
	 */
	public float getYAxisTextSize() {
		return yAxisTextSize;
	}

	/**
	 * sets size for Y axis text .
	 * 
	 */
	public void setYAxisTextSize(float textY) {
		if (textY > 0)
			this.yAxisTextSize = textY;
	}
	
	/**
	 * <i>use {@link #setXAxisTextSize(float)} or {@link #setYAxisTextSize(float)}</i>
	 * <br><br>
	 * equivalent to {@link #setXAxisTextSize(float)}
	 * */
	@Deprecated
	@Override
	public void setMarkTextSize(int markTextSize) {
		setXAxisTextSize(markTextSize);
	}
	
	/**
	 * <i>use {@link #getXAxisTextSize(float)} or {@link #getYAxisTextSize(float)}</i>
	 * <br><br>
	 * equivalent to {@link #getXAxisTextSize()}
	 * */
	@Deprecated
	@Override
	public int getMarkTextSize() {
		return (int) getXAxisTextSize();
	}
}
