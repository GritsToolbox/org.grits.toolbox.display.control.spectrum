package org.grits.toolbox.display.control.spectrum.chart;

import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.ChartChangeEvent;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.experimental.chart.swt.ChartComposite;
import org.jfree.experimental.swt.SWTGraphics2D;

public class GRITSChartComposite extends ChartComposite {
	private String sMzXMLFile;
	private Integer iScanNum;
	private Double dScale = 1.0;
	
	public GRITSChartComposite(Composite comp, int style, JFreeChart chart,
			boolean useBuffer) {
		super(comp, style, chart, useBuffer);
	}
	
	
	@Override
	public void setChart(JFreeChart arg0) {
		super.setChart(arg0);
	}
	
	@Override
	public Display getDisplay() {
		// TODO Auto-generated method stub
		return super.getDisplay();
	}
	
	public void drawGRITSAnnotations( SWTGraphics2D sg2 ) {
		GRITSXYPlot xyPlot = (GRITSXYPlot) getChart().getPlot();
		xyPlot.initializeGrid((int) (80.0d * dScale) , (int) (40.0d * dScale), getScaleX(), getScaleY());
		xyPlot.initAnnotationGrid(0.5);
		PlotOrientation orientation = xyPlot.getOrientation();
		ValueAxis axis = null;
		if (orientation == PlotOrientation.VERTICAL) {
			axis = xyPlot.getRangeAxis();
		} else {
			axis = xyPlot.getDomainAxis();
		}
		
		setGridScale(1.0d);
		xyPlot.drawPostPaintAnnotations(sg2, this, axis, false);  // CURRENT		
	}
	
	@Override
	public void paintControl(PaintEvent arg0) {
		super.paintControl(arg0);
		// DBW 11/10/17: In order to support export of the images, no longer
		// drawing the cartoons as a post-paint operation.
		// all the code is now in GRITSXYPlot.
//		SWTGraphics2D sg2 = new SWTGraphics2D(arg0.gc);
//		drawGRITSAnnotations(sg2);
	}
	
	
	public void setGridScale( double dScale ) {
		this.dScale = dScale;
	}
	
	public double getGridScale() {
		return this.dScale;
	}
	
	@Override
	public ChartRenderingInfo getChartRenderingInfo() {
		// TODO Auto-generated method stub
		return super.getChartRenderingInfo();
	}
	
	@Override
	public void chartChanged(ChartChangeEvent arg0) {
		// TODO Auto-generated method stub
//		System.out.println("Chart changed: " + arg0.getSource());
		super.chartChanged(arg0);
	}
	
	@Override
	public void setBounds(int x, int y, int width, int height) {
		// TODO Auto-generated method stub
		double dScaleFactor = (double) width / (double) height;
		GRITSXYPlot xyPlot = (GRITSXYPlot) getChart().getPlot();
		xyPlot.setChartHeight(height);
		xyPlot.setChartWidth(width);
		xyPlot.setChartScale(dScaleFactor);
		super.setBounds(x, y, width, height);
	}
	
	
	@Override
	public void reskin(int flags) {
		super.reskin(flags);
	}
	
	@Override
	public void redraw() {
		super.redraw();
	}
	
	@Override
	public void redraw(int x, int y, int width, int height, boolean all) {
		super.redraw(x, y, width, height, all);
	}
	
	public void setMzXMLFile(String sMzXMLFile) {
		this.sMzXMLFile = sMzXMLFile;
	}
	
	public String getMzXMLFile() {
		return sMzXMLFile;
	}
	
	public void setScanNum(Integer iScanNum) {
		this.iScanNum = iScanNum;
	}
	
	public Integer getScanNum() {
		return iScanNum;
	}
}
