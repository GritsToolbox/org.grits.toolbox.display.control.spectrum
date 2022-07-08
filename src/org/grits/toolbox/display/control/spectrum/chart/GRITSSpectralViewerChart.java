/*
 * ProteinFDRChart.java
 *
 * Created on December 21, 2007, 1:11 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.grits.toolbox.display.control.spectrum.chart;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.Range;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import org.grits.toolbox.display.control.spectrum.datamodel.FixedLineIntervalXYDataset;
import org.grits.toolbox.utils.image.GlycanImageProvider.GlycanImageObject;

/**
 *
 * @author brentw
 */
public class GRITSSpectralViewerChart {

	protected GRITSSpectralViewerData spectralViewerData = null;
	protected ArrayList<String> alTitlesAndLabels;
	protected GRITSJFreeChart chart;
	protected Boolean bVertLabels = null;
	protected String sDescription = null;
	protected Integer iScanNum = null;
	protected Integer iMSLevel = null;
	protected Boolean bIsProfile = null;

	private int iSeriesCnt = 0;
	protected int iRawPeaksSeriesIndex = -1;
	protected int iPickedPeaksSeriesIndex = -1;
	protected int iAnnotatedPeaksSeriesIndex = -1;
	protected int iUnAnnotatedPeaksSeriesIndex = -1;

	public GRITSSpectralViewerChart( String _sDescription, int _iScanNum, int _iMSLevel, boolean _bIsProfile, boolean _bVertLabels ){
		//		this.getSpectralViewerData().getRawData() = _alIons;
		this.alTitlesAndLabels = new ArrayList<String>();
		this.bVertLabels = _bVertLabels;
		this.sDescription = _sDescription;
		this.iScanNum = _iScanNum;
		this.iMSLevel = _iMSLevel;
		this.bIsProfile = _bIsProfile;
	}

	public ArrayList<String> getTitleAndLabels() {
		this.alTitlesAndLabels.add(sDescription);
		this.alTitlesAndLabels.add("m/z");  // XAxis Label is same as name of X Series
		this.alTitlesAndLabels.add("Peak Intensity");
		return this.alTitlesAndLabels;
	}

	public void setIsProfile(Boolean bIsProfile) {
		this.bIsProfile = bIsProfile;
	}

	public void setSpectralViewerData(GRITSSpectralViewerData spectralViewerData) {
		this.spectralViewerData = spectralViewerData;
	}

	public GRITSSpectralViewerData getSpectralViewerData() {
		return spectralViewerData;
	}
	
	public GRITSJFreeChart createChart() {
		GRITSJFreeChart chart = null;
		GRITSXYPlot plot = null;
		iSeriesCnt = 0;
		XYDataset xyDataset = getRawDataset();
		if( xyDataset.getSeriesCount() > 0 ) {
			if ( this.bIsProfile != null && this.bIsProfile ) {
				chart = createXYLineChart();
				plot = (GRITSXYPlot) chart.getPlot();
				this.iRawPeaksSeriesIndex = iSeriesCnt++;
				plot.setDataset(this.iRawPeaksSeriesIndex, xyDataset);
				XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
				renderer.setBaseShapesVisible(false);
				renderer.setSeriesPaint(0, Color.BLACK);
				renderer.setSeriesStroke(0, new java.awt.BasicStroke(0.25f) );
				plot.setRenderer(this.iRawPeaksSeriesIndex, renderer);
			} else {
				chart = createXYBarChart();
				plot = (GRITSXYPlot) chart.getPlot();
				this.iRawPeaksSeriesIndex = iSeriesCnt++;
				plot.setDataset(this.iRawPeaksSeriesIndex, xyDataset);
				XYBarRenderer renderer = new XYBarRenderer();
				renderer.setBaseItemLabelsVisible(true);
				renderer.setShadowVisible(false);
				plot.setRenderer(this.iRawPeaksSeriesIndex, renderer);
			}
		}

		xyDataset = getAnnotatedPeakDataset();
		if( xyDataset.getSeriesCount() > 0 ) {
			this.iAnnotatedPeaksSeriesIndex = iSeriesCnt++;
			plot.setDataset(this.iAnnotatedPeaksSeriesIndex, xyDataset);
			XYBarRenderer barRenderer = new XYBarRenderer();
			barRenderer.setBaseItemLabelsVisible(true);
			barRenderer.setShadowVisible(false);
			plot.setRenderer(this.iAnnotatedPeaksSeriesIndex, barRenderer, true);
		}

		xyDataset = getUnAnnotatedPeakDataset();
		if( xyDataset.getSeriesCount() > 0 ) {
			this.iUnAnnotatedPeaksSeriesIndex = iSeriesCnt++;
			plot.setDataset(this.iUnAnnotatedPeaksSeriesIndex, xyDataset);
			XYBarRenderer barRenderer = new XYBarRenderer();
			barRenderer.setBaseItemLabelsVisible(true);
			barRenderer.setShadowVisible(false);
			plot.setRenderer(this.iUnAnnotatedPeaksSeriesIndex, barRenderer, true);
		}
		xyDataset = getPickedPeakDataset();
		if( xyDataset.getSeriesCount() > 0 ) {	
			this.iPickedPeaksSeriesIndex = iSeriesCnt++;
			plot.setDataset(this.iPickedPeaksSeriesIndex, xyDataset);
			XYBarRenderer barRenderer = new XYBarRenderer();
			barRenderer.setBaseItemLabelsVisible(true);
			barRenderer.setShadowVisible(false);
			plot.setRenderer(this.iPickedPeaksSeriesIndex, barRenderer, true);
		}
		setPeakLabels();

		return chart;
	}

	public void updateChart( 
			boolean bShowRaw, 
			boolean bShowPicked, boolean bShowPickedLabels,
			boolean bShowAnnotated, boolean bShowAnnotatedLabels, 
			boolean bShowUnannotated, boolean bShowUnannotatedLabels) {
		GRITSXYPlot plot = (GRITSXYPlot) chart.getPlot();
		if( this.iRawPeaksSeriesIndex >= 0 ) {
			plot.getRenderer(this.iRawPeaksSeriesIndex).setBaseSeriesVisible(bShowRaw);
		}
		if( this.iPickedPeaksSeriesIndex >= 0 ) {
			plot.getRenderer(this.iPickedPeaksSeriesIndex).setBaseSeriesVisible(bShowPicked);
			plot.getRenderer(this.iPickedPeaksSeriesIndex).setBaseItemLabelsVisible(bShowPickedLabels);
			plot.getRenderer(this.iPickedPeaksSeriesIndex).setBasePositiveItemLabelPosition(new org.jfree.chart.labels.ItemLabelPosition(
					org.jfree.chart.labels.ItemLabelAnchor.OUTSIDE12, org.jfree.ui.TextAnchor.BASELINE_LEFT, org.jfree.ui.TextAnchor.BASELINE_LEFT, 4.705) );
		}
		if( this.iAnnotatedPeaksSeriesIndex >= 0 ) {
			plot.getRenderer(this.iAnnotatedPeaksSeriesIndex).setBaseSeriesVisible(bShowAnnotated);
			plot.getRenderer(this.iAnnotatedPeaksSeriesIndex).setBaseItemLabelsVisible(bShowAnnotatedLabels);
			plot.setDrawPostPaintAnnotations(bShowAnnotatedLabels);
			plot.getRenderer(this.iAnnotatedPeaksSeriesIndex).setBasePositiveItemLabelPosition(new org.jfree.chart.labels.ItemLabelPosition(
					org.jfree.chart.labels.ItemLabelAnchor.OUTSIDE12, org.jfree.ui.TextAnchor.BASELINE_LEFT, org.jfree.ui.TextAnchor.BASELINE_LEFT, 4.705) );
		}
		if( this.iUnAnnotatedPeaksSeriesIndex >= 0 ) {
			plot.getRenderer(this.iUnAnnotatedPeaksSeriesIndex).setBaseSeriesVisible(bShowUnannotated);
			plot.getRenderer(this.iUnAnnotatedPeaksSeriesIndex).setBaseItemLabelsVisible(bShowUnannotatedLabels);
			plot.getRenderer(this.iUnAnnotatedPeaksSeriesIndex).setBasePositiveItemLabelPosition(new org.jfree.chart.labels.ItemLabelPosition(
					org.jfree.chart.labels.ItemLabelAnchor.OUTSIDE12, org.jfree.ui.TextAnchor.BASELINE_LEFT, org.jfree.ui.TextAnchor.BASELINE_LEFT, 4.705) );

		}
	}

	protected void setPeakLabels() {
		GRITSXYPlot plot = (GRITSXYPlot) chart.getPlot();
		HashMap<Double, List<Object>> htPostPaintLabels = new HashMap<>();

		HashMap<Double, Object> htPickedPeakLabels = new HashMap<>();
		HashMap<Double, Object> htAnnotatedPeakLabels = new HashMap<>();
		HashMap<Double, Object> htUnAnnotatedPeakLabels = new HashMap<>();
		// precedence: annotated over picked if both present
		if( this.iPickedPeaksSeriesIndex >= 0 ) {			
			for( Double dPeakMass : getSpectralViewerData().getPickedPeakLabels().keySet() ) {
				List<Object> alSourceLabels = getSpectralViewerData().getPickedPeakLabels().get(dPeakMass);
				if( alSourceLabels == null || alSourceLabels.isEmpty() )
					continue;
				//				if( alSourceLabels.size() > 1 || alSourceLabels.get(0) instanceof Image ) {
				//					htPostPaintLabels.put(dPeakMass, alSourceLabels);
				//				} else {
				htPickedPeakLabels.put(dPeakMass, alSourceLabels.get(0) );
				//				}
			}
			plot.setPeakLabels(this.iPickedPeaksSeriesIndex, htPickedPeakLabels);
		}

		if( this.iAnnotatedPeaksSeriesIndex >= 0 ) {
			for( Double dPeakMass : getSpectralViewerData().getAnnotatedPeakLabels().keySet() ) {
				List<Object> alSourceLabels = getSpectralViewerData().getAnnotatedPeakLabels().get(dPeakMass);
				if( alSourceLabels == null || alSourceLabels.isEmpty() )
					continue;
				if( alSourceLabels.size() > 1 || alSourceLabels.get(0) instanceof GlycanImageObject ) {
					htPostPaintLabels.put(dPeakMass, alSourceLabels);
					//					htPickedPeakLabels.remove(dPeakMass);
				} else {
					htAnnotatedPeakLabels.put(dPeakMass, alSourceLabels.get(0) );
				}
			}
			plot.setPeakLabels(this.iAnnotatedPeaksSeriesIndex, htAnnotatedPeakLabels);
			plot.setPostPaintAnnotations(htPostPaintLabels);		
		}

		if( this.iUnAnnotatedPeaksSeriesIndex >= 0 ) {
			for( Double dPeakMass : getSpectralViewerData().getUnAnnotatedPeakLabels().keySet() ) {
				List<Object> alSourceLabels = getSpectralViewerData().getUnAnnotatedPeakLabels().get(dPeakMass);
				if( alSourceLabels == null || alSourceLabels.isEmpty() )
					continue;
				htUnAnnotatedPeakLabels.put(dPeakMass, alSourceLabels.get(0) );

			}
			plot.setPeakLabels(this.iUnAnnotatedPeaksSeriesIndex, htUnAnnotatedPeakLabels);
		}
	}

	protected LegendItemCollection createLegendItems() {
		LegendItemCollection legenditemcollection = new LegendItemCollection();
		return legenditemcollection;
	} 

	protected GRITSJFreeChart createXYBarChart() {
		chart = GRITSJFreeChart.createXYBarChart(
				(String) this.getTitleAndLabels().get(0),                      // chart title
				(String) this.getTitleAndLabels().get(1),
				false,
				(String) this.getTitleAndLabels().get(2),                        // range axis label
				null,                    // data
				PlotOrientation.VERTICAL,
				true,                       // include legend
				true,
				false,
				null
				);

		XYPlot plot = (XYPlot) chart.getPlot();
		plot.setBackgroundPaint(Color.white);
		NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
		domainAxis.setAutoTickUnitSelection(true);

		return chart;        
	}

	protected GRITSJFreeChart createXYLineChart() {
		chart = GRITSJFreeChart.createXYLineChart(
				(String) this.getTitleAndLabels().get(0),
				(String) this.getTitleAndLabels().get(1),
				(String) this.getTitleAndLabels().get(2),
				null,
				PlotOrientation.VERTICAL,
				true,
				true,
				false,
				null
				);

		XYPlot plot = (XYPlot) chart.getPlot();
		plot.setBackgroundPaint(java.awt.Color.white);
		//		plot.getLegendItems().get(0).getDescription();
		//		plot.setFixedLegendItems(createLegendItems());
		NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
		domainAxis.setAutoTickUnitSelection(true);

		return chart;
	}

	protected XYDataset getYDataset() {
		XYSeriesCollection xySeriesCollection = new XYSeriesCollection();
		XYSeries series1 = new XYSeries("MS/MS Spectra");
		for (int i = 0; i < getSpectralViewerData().getRawData().size(); i++) {
			XYDataItem item = new XYDataItem(getSpectralViewerData().getRawData().get(i).getMass(),
					getSpectralViewerData().getRawData().get(i).getIntensity());
			series1.add(item);
		}
		xySeriesCollection.addSeries(series1);
		return xySeriesCollection;
	}

	protected FixedLineIntervalXYDataset getRawDataset() {
		FixedLineIntervalXYDataset xySeriesCollection = new FixedLineIntervalXYDataset();
		if( this.getSpectralViewerData().getRawData() != null && ! this.getSpectralViewerData().getRawData().isEmpty() ) {
			double d[][] = new double[2][getSpectralViewerData().getRawData().size()];
			for (int i = 0; i < getSpectralViewerData().getRawData().size(); i++) {
				d[0][i] = getSpectralViewerData().getRawData().get(i).getMass();
				d[1][i] = getSpectralViewerData().getRawData().get(i).getIntensity();
			}
			//			this.iRawPeaksSeriesIndex = chart.getXYPlot().getSeriesCount();
			xySeriesCollection.addSeries("Observed", d);
		}
		return xySeriesCollection;
	}

	protected FixedLineIntervalXYDataset getPickedPeakDataset() {
		FixedLineIntervalXYDataset xySeriesCollection = new FixedLineIntervalXYDataset();
		if( getSpectralViewerData().getPickedPeaks() != null && ! this.getSpectralViewerData().getPickedPeaks().isEmpty() ) {
			double d[][] = new double[2][getSpectralViewerData().getPickedPeaks().size()];
			for (int i = 0; i < getSpectralViewerData().getPickedPeaks().size(); i++) {
				d[0][i] = getSpectralViewerData().getPickedPeaks().get(i).getMass();
				d[1][i] = getSpectralViewerData().getPickedPeaks().get(i).getIntensity();
			}
			xySeriesCollection.addSeries("Picked Peaks", d);
		}
		return xySeriesCollection;
	}

	protected FixedLineIntervalXYDataset getUnAnnotatedPeakDataset() {
		FixedLineIntervalXYDataset xySeriesCollection = new FixedLineIntervalXYDataset();
		if( getSpectralViewerData().getUnAnnotatedPeaks() != null && ! this.getSpectralViewerData().getUnAnnotatedPeaks().isEmpty() ) {
			double d[][] = new double[2][getSpectralViewerData().getUnAnnotatedPeaks().size()];
			for (int i = 0; i < getSpectralViewerData().getUnAnnotatedPeaks().size(); i++) {
				d[0][i] = getSpectralViewerData().getUnAnnotatedPeaks().get(i).getMass();
				d[1][i] = getSpectralViewerData().getUnAnnotatedPeaks().get(i).getIntensity();
			}
			xySeriesCollection.addSeries("Unannotated Peaks", d);
		}
		return xySeriesCollection;
	}

	protected FixedLineIntervalXYDataset getAnnotatedPeakDataset() {	
		FixedLineIntervalXYDataset xySeriesCollection = new FixedLineIntervalXYDataset();
		if( getSpectralViewerData().getAnnotatedPeaks() != null && ! getSpectralViewerData().getAnnotatedPeaks().isEmpty() ) {
			double d[][] = new double[2][getSpectralViewerData().getAnnotatedPeaks().size()];
			for (int i = 0; i < getSpectralViewerData().getAnnotatedPeaks().size(); i++) {
				d[0][i] = getSpectralViewerData().getAnnotatedPeaks().get(i).getMass();
				d[1][i] = getSpectralViewerData().getAnnotatedPeaks().get(i).getIntensity();
			}
			//			this.iAnnotatedPeaksSeriesIndex = chart.getXYPlot().getSeriesCount();
			xySeriesCollection.addSeries("Annotated Peaks", d);
		}
		return xySeriesCollection;
	}

}
