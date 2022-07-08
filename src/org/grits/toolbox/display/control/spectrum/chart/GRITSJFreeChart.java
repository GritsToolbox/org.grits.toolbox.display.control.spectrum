package org.grits.toolbox.display.control.spectrum.chart;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.List;

import org.jfree.chart.ChartTheme;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.urls.StandardXYURLGenerator;
import org.jfree.chart.util.ParamChecks;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYDataset;

public class GRITSJFreeChart extends JFreeChart {
	private static final long serialVersionUID = 1287819122582176394L;
	   /** The chart theme. */
    private static ChartTheme currentTheme = new StandardChartTheme("JFree");
	public GRITSJFreeChart(Plot plot) {
		super(plot);
	}
	
    public GRITSJFreeChart(String title, Font titleFont, Plot plot,
            boolean createLegend, HashMap<Double, List<Object>> htPeakLabels) {
    	super(title, titleFont, plot, createLegend);
    }

	@Override
	public void draw(Graphics2D g2, Rectangle2D area) {
		super.draw(g2, area);
	}

	public static GRITSJFreeChart createXYBarChart(String title, String xAxisLabel,
			boolean dateAxis, String yAxisLabel, IntervalXYDataset dataset,
			PlotOrientation orientation, boolean legend, boolean tooltips,
			boolean urls, HashMap<Double, List<Object>> htPeakLabels) {

		ParamChecks.nullNotPermitted(orientation, "orientation");
		ValueAxis domainAxis;
		if (dateAxis) {
			domainAxis = new DateAxis(xAxisLabel);
		}
		else {
			NumberAxis axis = new NumberAxis(xAxisLabel);
			axis.setAutoRangeIncludesZero(false);
			domainAxis = axis;
		}
		NumberAxis valueAxis = new GRITSMSSpectraRangeAxis(yAxisLabel);

		XYBarRenderer renderer = new XYBarRenderer();
		if (tooltips) {
			XYToolTipGenerator tt;
			if (dateAxis) {
				tt = StandardXYToolTipGenerator.getTimeSeriesInstance();
			}
			else {
				tt = new StandardXYToolTipGenerator();
			}
			renderer.setBaseToolTipGenerator(tt);
		}
		if (urls) {
			renderer.setURLGenerator(new StandardXYURLGenerator());
		}

		XYPlot plot = new GRITSXYPlot(dataset, domainAxis, valueAxis, renderer, htPeakLabels);
		plot.setOrientation(orientation);

		GRITSJFreeChart chart = new GRITSJFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT,
				plot, legend, htPeakLabels);
		currentTheme.apply(chart);
		return chart;

	}

	public static GRITSJFreeChart createXYLineChart(String title, String xAxisLabel,
			String yAxisLabel, XYDataset dataset, PlotOrientation orientation,
			boolean legend, boolean tooltips, boolean urls,
			HashMap<Double, List<Object>> htPeakLabels) {

		ParamChecks.nullNotPermitted(orientation, "orientation");
		NumberAxis xAxis = new NumberAxis(xAxisLabel);
		xAxis.setAutoRangeIncludesZero(false);
		NumberAxis yAxis = new GRITSMSSpectraRangeAxis(yAxisLabel);
		XYItemRenderer renderer = new XYLineAndShapeRenderer(true, false);
		XYPlot plot = new GRITSXYPlot(dataset, xAxis, yAxis, renderer, htPeakLabels);
		plot.setOrientation(orientation);
		if (tooltips) {
			renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator());
		}
		if (urls) {
			renderer.setURLGenerator(new StandardXYURLGenerator());
		}

		GRITSJFreeChart chart = new GRITSJFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT,
				plot, legend, htPeakLabels);
		currentTheme.apply(chart);
		return chart;
	}
}
