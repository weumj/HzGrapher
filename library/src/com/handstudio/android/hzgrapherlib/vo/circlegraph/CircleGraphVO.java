package com.handstudio.android.hzgrapherlib.vo.circlegraph;

import java.util.List;

import com.handstudio.android.hzgrapherlib.animation.GraphAnimation;
import com.handstudio.android.hzgrapherlib.vo.Graph;

public class CircleGraphVO extends Graph{
	
	
	//animation
	private int 				radius ;
	private int 				textSize 			= 20;
	private int 				centerX 			= 0;
	private int 				centerY 			= 0;
	
	private GraphAnimation 	animation 			= null;
	private List<CircleGraph> 	arrGraph 			= null;
	private boolean 			isDrawRegion		= false;
	private boolean 			isPieChart			= false;
	
	public CircleGraphVO(List<CircleGraph> arrGraph) {
		super();
		this.arrGraph = arrGraph;
	}
	
	public CircleGraphVO(List<CircleGraph> arrGraph, int graphBG) {
		super();
		this.arrGraph = arrGraph;
		this.setGraphBG(graphBG);
	}
	
	public CircleGraphVO(int paddingBottom, int paddingTop, int paddingLeft,
			int paddingRight, int marginTop, int marginRight,int radius, List<CircleGraph> arrGraph) {
		super(paddingBottom, paddingTop, paddingLeft, paddingRight, marginTop, marginRight);
		this.arrGraph = arrGraph;
		this.radius = radius;
	}
	
	public CircleGraphVO(int paddingBottom, int paddingTop, int paddingLeft,
			int paddingRight, int marginTop, int marginRight,int radius, List<CircleGraph> arrGraph, int graphBG) {
		super(paddingBottom, paddingTop, paddingLeft, paddingRight, marginTop, marginRight);
		this.radius = radius;
		this.arrGraph = arrGraph;
		this.setGraphBG(graphBG);
	}
	
	public List<CircleGraph> getArrGraph() {
		return arrGraph;
	}

	public void setArrGraph(List<CircleGraph> arrGraph) {
		this.arrGraph = arrGraph;
	}

	public GraphAnimation getAnimation() {
		return animation;
	}

	public void setAnimation(GraphAnimation animation) {
		this.animation = animation;
	}

	public boolean isDrawRegion() {
		return isDrawRegion;
	}

	public void setDrawRegion(boolean isDrawRegion) {
		this.isDrawRegion = isDrawRegion;
	}

	public int getRadius() {
		return radius;
	}

	public void setRadius(int radius) {
		this.radius = radius;
	}

	public int getTextSize() {
		return textSize;
	}

	public void setTextSize(int textSize) {
		this.textSize = textSize;
	}

	public int getCenterX() {
		return centerX;
	}

	public void setCenterX(int centerX) {
		this.centerX = centerX;
	}

	public int getCenterY() {
		return centerY;
	}

	public void setCenterY(int centerY) {
		this.centerY = centerY;
	}

	public boolean isPieChart() {
		return isPieChart;
	}

	public void setPieChart(boolean isPieChart) {
		this.isPieChart = isPieChart;
	}
	
}
