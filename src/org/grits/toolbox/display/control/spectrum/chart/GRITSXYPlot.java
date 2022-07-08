package org.grits.toolbox.display.control.spectrum.chart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.grits.toolbox.display.control.spectrum.datamodel.AnnotationGrid;
import org.grits.toolbox.display.control.spectrum.datamodel.AnnotationGrid.AnnotationCell;
import org.grits.toolbox.utils.image.SimianImageConverter;
import org.grits.toolbox.utils.image.GlycanImageProvider.GlycanImageObject;
import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.AbstractXYItemLabelGenerator;
import org.jfree.chart.labels.XYItemLabelGenerator;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.PlotState;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.Range;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleEdge;

public class GRITSXYPlot extends XYPlot {
	private static final long serialVersionUID = -4472476666893443246L;
	protected Rectangle2D dataArea = null;
	protected PlotRenderingInfo info = null;
	protected AnnotationGrid grid = null;
	protected HashMap<Double, double[]> htItemsToBestInt = null;
	protected HashMap<Double, XYAnnotation> htItemsToLineAnnotation = null;
	protected HashMap<Double, List<Object>> htPostPaintPeakLabels;
	protected boolean bDrawPostPaintAnnotations = false;

	protected HashMap<Double, Object>[] htPeakLabels;
	protected double dChartScale = 1.0;
	protected int iChartWidth = -1;
	protected int iChartHeight = -1;

	protected final static float dash1[] = {10.0f};
	protected final static BasicStroke dashed =
			new BasicStroke(1.0f,
					BasicStroke.CAP_BUTT,
					BasicStroke.JOIN_MITER,
					10.0f, dash1, 0.0f);

	protected HashMap<Object, int[]> htAnnotationToCoords = null;

	public GRITSXYPlot(XYDataset dataset,
			ValueAxis domainAxis,
			ValueAxis rangeAxis,
			XYItemRenderer renderer,
			HashMap<Double, List<Object>> htPostPaintPeakLabels ) {
		super(dataset, domainAxis, rangeAxis, renderer);
		this.htPostPaintPeakLabels = htPostPaintPeakLabels;
		htPeakLabels = new HashMap[4];		
	}

	public GRITSXYPlot() {
		super();
		htPeakLabels = new HashMap[4];
	}

	public void setChartHeight(int iChartHeight) {
		this.iChartHeight = iChartHeight;
	}
	public int getChartHeight() {
		return iChartHeight;
	}
	
	public void setChartWidth(int iChartWidth) {
		this.iChartWidth = iChartWidth;
	}
	public int getChartWidth() {
		return iChartWidth;
	}
	
	public void setChartScale(double dChartScale) {
		this.dChartScale = dChartScale;
	}
	public double getChartScale() {
		return dChartScale;
	}

	public void setPeakLabels(int iSeriesInx, HashMap<Double, Object> htPeakLabels) {
		if( iSeriesInx < 0 || iSeriesInx >= this.htPeakLabels.length ) {
			return;
		}
		this.htPeakLabels[iSeriesInx] = htPeakLabels;
		XYItemRenderer renderer = getRenderer(iSeriesInx);
		renderer.setBaseItemLabelGenerator(new LabelGenerator(iSeriesInx));
		//       renderer.setBaseItemLabelFont(new Font("Serif", Font.PLAIN, 20));
		renderer.setBaseItemLabelsVisible(true);
		//        renderer.setBasePositiveItemLabelPosition(new org.jfree.chart.labels.ItemLabelPosition(
		//            org.jfree.chart.labels.ItemLabelAnchor.OUTSIDE12, org.jfree.ui.TextAnchor.CENTER, org.jfree.ui.TextAnchor.TOP_RIGHT, -1.5 ) );
		//        if ( bVertLabels )

	}

	protected Object getPeakLabel( int _iSeriesInx, double _dPeakMass ) {
		if ( htPeakLabels == null ) {
			return "";
		}
		double dMinDelta = Double.MAX_VALUE;
		String sPeakLabel = "";
		for( Double dPeakMass : this.htPeakLabels[_iSeriesInx].keySet() ) {
			double delta = Math.abs( dPeakMass - _dPeakMass);
			if ( delta < dMinDelta && delta < 0.05  ) {
				sPeakLabel = htPeakLabels[_iSeriesInx].get(dPeakMass).toString();
			}
		}
		return sPeakLabel;
	}

	public void setDrawPostPaintAnnotations(boolean bDrawPostPaintAnnotations) {
		this.bDrawPostPaintAnnotations = bDrawPostPaintAnnotations;
	}

	public boolean getDrawPostPaintAnnotations() {
		return bDrawPostPaintAnnotations;
	}

	public void setPostPaintAnnotations(HashMap<Double, List<Object>> htPostPaintPeakLabels) {
		this.htPostPaintPeakLabels = htPostPaintPeakLabels;
	}

	public void setDataArea( Rectangle2D dataArea ) {
		this.dataArea = dataArea;
	}

	public void setPlotRenderingInfo( PlotRenderingInfo info ) {
		this.info = info;
	}

	protected void initializeGrid( int iCellWidth, int iCellHeight, double dScaleX, double dScaleY ) {
		// fill the grid where chart lines exist
		grid = new AnnotationGrid(this, dataArea, getDomainAxis(), getRangeAxis(), dScaleX, dScaleY);
		grid.initializeGrid(iCellWidth, iCellHeight );	
	}

	public void initAnnotationGrid( double dMassDelta ) {
		htItemsToBestInt = new HashMap<>();
		if ( htPostPaintPeakLabels == null ) {
			return;
		}
		PlotOrientation orientation = getOrientation();
		RectangleEdge domainEdge = Plot.resolveDomainAxisLocation(
				getDomainAxisLocation(), orientation);


		for(int iDScount = 0; iDScount < getDatasetCount(); iDScount++ ) {
			XYDataset dataset = getDataset(iDScount);
			for( int series = 0; series < dataset.getSeriesCount(); series++) {
				for( int item = 0; item < dataset.getItemCount(series); item++ ) {
					double[] vals = new double[] {dataset.getXValue(series, item), dataset.getYValue(series, item)};
					double dx = getDomainAxis().valueToJava2D(vals[0], dataArea, domainEdge);
					//				double dx = getDomainAxis().valueToJava2D(vals[0], dataArea, domainEdge);
					if( dx > 0 ) {
						for( Double dPeakMass : this.htPostPaintPeakLabels.keySet() ) {
							double delta = Math.abs( dPeakMass - vals[0] );
							if ( delta < dMassDelta ) {
								double dPrevInt = 0;
								if( htItemsToBestInt.containsKey(dPeakMass) ) {
									dPrevInt = htItemsToBestInt.get(dPeakMass)[1];
								}
								if( dPrevInt < vals[1] || delta == 0.0 ) {
									htItemsToBestInt.put(dPeakMass, vals);
								}
							}
						}
					}
				}
			}
		}
	}

	public HashMap<Object, int[]> getHtAnnotationToCoords() {
		return htAnnotationToCoords;
	}

	protected boolean drawPostPaintAnnotation(Graphics2D g2, GRITSChartComposite chart, 
			Double dPeakMass, List<Object> alAnnots, boolean bForceStacking) {
		double dScaleX = chart.getScaleX();
		double dScaleY = chart.getScaleY();

		double[] vals = htItemsToBestInt.get(dPeakMass);
		AnnotationCell cellDP = grid.getNewCell(vals[0], vals[1]);
		if( cellDP == null )
			return true;
		int iLineDPX = (int) (cellDP.getScreenLowX() * dScaleX);
		int iLineDPY = (int) (cellDP.getScreenLowY() * dScaleY);
		int iLineMinX = Integer.MAX_VALUE;
		int iLineMaxX = Integer.MIN_VALUE;
		int iLineMaxY = Integer.MIN_VALUE;

		htAnnotationToCoords = new HashMap<>();
		int iPrefX = -1;
		//		int iPrevPrefY = -1;
		for( Object oPeakAnnotation: alAnnots ) {
			AnnotationCell cell = grid.putAnnotation(oPeakAnnotation, vals[0], vals[1], iPrefX);
			if( cell == null && iPrefX != -1 ) {
				cell = grid.putAnnotation(oPeakAnnotation, vals[0], vals[1], -1);
			}
			if(cell != null ) {
				//				System.out.println("Image cell: " + cell + ", prefX: " + iPrefX);				
				//				double[] dDataVals1 = grid.convertToDataValue(cell.getScreenLowX(), cell.getScreenHighY());
				int iCellX = (int) (cell.getScreenLowX() * dScaleX);
				int iCellX2 = (int) (cell.getScreenLowX() * dScaleX);
				int iCellY = (int) (cell.getScreenLowY() * dScaleY);
				int iCellY2 = (int) (cell.getScreenLowY() * dScaleY);
				if(  bForceStacking && iPrefX != -1 && iPrefX != (int) cell.getScreenLowX() ) {
					return false;
				}
				if( oPeakAnnotation instanceof GlycanImageObject ) {
					GlycanImageObject gio = (GlycanImageObject) oPeakAnnotation;
					double dScaleValX = grid.getCellWidth() * grid.getScaleX();
					double dScaleValY = grid.getCellHeight() * grid.getScaleY();

					gio.dispose();
					BufferedImage bImg = gio.getScaledAwtBufferedImage(dScaleValX, dScaleValY);
					//					BufferedImage bImg = gio.getAwtBufferedImage();
					g2.drawImage(bImg, iCellX, iCellY, bImg.getWidth(), bImg.getHeight(), null);
					//					htAnnotationToCoords.put(img, new int[] {iCellX, iCellY});
					iCellY2 += bImg.getHeight();
					iCellX2 += bImg.getWidth();
					//					iPrevPrefY = iCellY - img.getHeight();
				} else {
					//					System.out.println("'How?");
					//					g2.drawString(oPeakAnnotation.toString(), (int) lineAnnot.j2DX1, (int) lineAnnot.j2DY1);
				}
				//			g2.drawString(dPeakMass.toString(),iLineAnnotX, iLineAnnotY);
				//				g2.setPaint(this.paint);
				if( iCellX2 > iLineMaxX ) {
					iLineMaxX = iCellX2;
				}
				if( iCellX < iLineMinX ) {
					iLineMinX = iCellX;
				}
				if( iCellY2 > iLineMaxY ) {
					iLineMaxY = iCellY2;
				}				
				iPrefX = (int) cell.getScreenLowX();

			}	
		}
		g2.setStroke(dashed);
		int iMidX = iLineMinX + ((iLineMaxX - iLineMinX) / 2);
		Line2D line1 = new Line2D.Float(iLineDPX, iLineDPY, iMidX, iLineMaxY);
		g2.draw(line1);
		//		Line2D line2 = new Line2D.Float(iLineMinX, iLineMaxY, iLineMaxX, iLineMaxY);
		//		g2.draw(line2);
		g2.drawString(dPeakMass.toString(), iLineMinX, iLineMaxY);
		return true;
	}

	protected void drawPostPaintAnnotations(Graphics2D g2, GRITSChartComposite chart, ValueAxis axis, boolean bForceStacking) {
		if ( htPostPaintPeakLabels == null || ! getDrawPostPaintAnnotations() ) {
			return;
		}
		List<Double> sortedKeys=new ArrayList(htItemsToBestInt.keySet());
		Collections.sort(sortedKeys);
		for(Double dPeakMass: sortedKeys ) {
			List<Object> alAnnots = htPostPaintPeakLabels.get(dPeakMass);
			if( alAnnots == null ) 
				continue;
			if( ! drawPostPaintAnnotation(g2, chart, dPeakMass, alAnnots, bForceStacking) ) {
				chart.setGridScale(chart.getGridScale() * 0.75);
				axis.zoomRange(0, 1.25);
				return;
			}
		}
	}

	protected void drawPostPaintAnnotations(Graphics2D g2, Rectangle2D dataArea, ValueAxis axis, boolean bForceStacking, boolean bWriteMass) {
		if ( htPostPaintPeakLabels == null || ! getDrawPostPaintAnnotations() ) {
			return;
		}
		List<Double> sortedKeys=new ArrayList(htItemsToBestInt.keySet());
		//		System.out.println("XYPlot: drawPostPaintAnnotations");
		Collections.sort(sortedKeys);
		for(Double dPeakMass: sortedKeys ) {
			List<Object> alAnnots = htPostPaintPeakLabels.get(dPeakMass);
			if( alAnnots == null ) 
				continue;
			if( ! drawPostPaintAnnotation(g2, dataArea, dPeakMass, alAnnots, bForceStacking, bWriteMass) ) {
				//				chart.setGridScale(chart.getGridScale() * 0.75);
				axis.zoomRange(0, 1.25);
				return;
			}
		}
	}

	public static double getXDatasetDensity(XYDataset xyDataset, int iSeries, Range range) {
		double dMin = Double.MAX_VALUE;
		double dMax = Double.MIN_VALUE;
		int iCnt = 0;
		for (int i = 0; i < xyDataset.getItemCount(iSeries); i++) {
			Double dValue = xyDataset.getXValue(iSeries, i);
			if( dValue < range.getLowerBound() || dValue > range.getUpperBound() ) {
				continue;
			}
			iCnt++;
			if( dValue < dMin ) {
				dMin = dValue;
			}
			if( dValue > dMax ) {
				dMax = dValue;
			}
		}		
		double dDensity = (double) iCnt / (dMax - dMin);

		return dDensity;				
	}

	public static int[] getNumVisPeaks(XYDataset xyDataset, int iSeries, Range domainAxis, Range rangeAxis ) {
		Map<Double, Integer> alSeen = new HashMap<>();
		int iYMax = Integer.MIN_VALUE;
		for (int i = 0; i < xyDataset.getItemCount(iSeries); i++) {
			Double dXValue = xyDataset.getXValue(iSeries, i);
			double dYValue = xyDataset.getYValue(iSeries, i);				
			if( dXValue < domainAxis.getLowerBound() || dXValue > domainAxis.getUpperBound() ) {
				continue;
			}
			if( dYValue < rangeAxis.getLowerBound() || dYValue > rangeAxis.getUpperBound() ) {
				continue;
			}
			// counting the y dataset must consider that multiple annotations might exist at same x-value, so they might stack
			if( ! alSeen.containsKey(dXValue) ) {
				alSeen.put(dXValue, 1);
			} else {
				alSeen.put(dXValue, alSeen.get(dXValue)+1);
			}
			if( alSeen.get(dXValue) > iYMax ) {
				iYMax = alSeen.get(dXValue);
			}
		}		
		double dVal = (domainAxis.getUpperBound() - domainAxis.getLowerBound()) / alSeen.size();
		return new int[] { alSeen.size(), (int) (iYMax / (dVal/alSeen.size())) };				
	}

	public double getVisPeakFactor(XYDataset xyDataset, int iSeries, Range domainAxis, Range rangeAxis ) {
		int iNumVis = 0;
		for (int i = 0; i < xyDataset.getItemCount(iSeries); i++) {
			Double dXValue = xyDataset.getXValue(iSeries, i);
			double dYValue = xyDataset.getYValue(iSeries, i);				
			if( dXValue < domainAxis.getLowerBound() || dXValue > domainAxis.getUpperBound() ) {
				continue;
			}
			if( dYValue < rangeAxis.getLowerBound() || dYValue > rangeAxis.getUpperBound() ) {
				continue;
			}
			iNumVis++;
		}		
		double dVal = getChartWidth() / (double) iNumVis;
		if( dVal < 0.2 ) {
			dVal = 0.2;
		}
		if( dVal > 1.0 ) {
			dVal = 1.0;
		}
		return dVal;
	}
	
	public double[] getGridCellDimensions( int[] iNumVisPeaks, Range domainAxis, Range rangeAxis, double[] dMaxImgSize ) {
		double dIdealWidth = (double) iNumVisPeaks[0] * (dMaxImgSize[0] + 10.0);
		double dIdealHeight = (double) iNumVisPeaks[1] * (dMaxImgSize[1] + 10.0);
		
		double dWidthFactor = Math.ceil( dIdealWidth / getChartWidth() );
		if( dWidthFactor == 1.0 ) {
			return dMaxImgSize;
		}
		double dHeightFactor = Math.ceil( dIdealHeight / getChartHeight() );
		
		double dStackedWidth = (double) iNumVisPeaks[0] / dWidthFactor;
		double dStackedHeight = (double) iNumVisPeaks[1] / dHeightFactor;
		
		double dRetFactorWidth = dStackedWidth < 4 ? dStackedWidth : Math.ceil( dStackedWidth / 4 );
		double dRetFactorHeight = dStackedHeight < 4 ? dStackedHeight : Math.ceil( dStackedHeight / 4 );
		
		double dNewWidth = dMaxImgSize[0] / dRetFactorWidth;
		double dNewHeight = dMaxImgSize[1] / dRetFactorWidth;
		
		return new double[] { dNewWidth, dNewHeight };				
	}
	
	protected double[] getMaxCellSize() {
		double dMaxW = 80.0d;
		double dMaxH = 80.0d;
		if ( htPostPaintPeakLabels == null || ! getDrawPostPaintAnnotations() ) {
			return new double[] {dMaxW, dMaxH};
		}
		List<Double> sortedKeys=new ArrayList(htPostPaintPeakLabels.keySet());
		Collections.sort(sortedKeys);
		for(Double dPeakMass: sortedKeys ) {
			List<Object> alAnnots = htPostPaintPeakLabels.get(dPeakMass);
			if( alAnnots == null ) 
				continue;
			for( Object oPeakAnnotation: alAnnots ) {
				if( oPeakAnnotation instanceof GlycanImageObject ) {
					GlycanImageObject gio = (GlycanImageObject) oPeakAnnotation;
					//				double dScaleValX = grid.getCellWidth() * grid.getScaleX();
					//				double dScaleValY = grid.getCellHeight() * grid.getScaleY();
					gio.dispose();
					BufferedImage bImg = gio.getAwtBufferedImage();
					int iW = bImg.getWidth();
					int iH = bImg.getHeight();
					if( iW > dMaxW ) {
						dMaxW = (double) iW;
					}

					if( iH > dMaxH ) {
						dMaxH = (double) iH;
					}
				}
			}		
		}
		return new double[] {dMaxW, dMaxH};
	}

	public void drawGRITSAnnotations( Graphics2D sg2, Rectangle2D dataArea ) {
		if( getDatasetCount() <= 1 ) { // nothing to draw!
			return;
		}
		int[] iNumVis = GRITSXYPlot.getNumVisPeaks(getDataset(1), 0, getDomainAxis().getRange(), getRangeAxis().getRange());
		double dVisXDensity = 10.0 * ((double) iNumVis[0] / dataArea.getWidth());
		double dVisYDensity = 15.0 * ((double) iNumVis[1] / dataArea.getHeight());
//		double dVisXDensity = ((double) iNumVis[0] / dataArea.getWidth());
//		double dVisYDensity = ((double) iNumVis[1] / dataArea.getHeight());

//		double dVisDensity = dVisXDensity > dVisYDensity ? dVisXDensity : dVisYDensity;
//		double dScale = 1.0 - dVisDensity;
//		if( dScale < 0.2 ) {
//			dScale = 0.2;
//		}
//		if( dScale > 1.0 ) {
//			dScale = 1.0;
//		}
		double dXScale = 1.0 - dVisXDensity;
		if( dXScale < 0.2 ) {
			dXScale = 0.2;
		}
		if( dXScale > 1.0 ) {
			dXScale = 1.0;
		}
		double dYScale = 1.0 - dVisYDensity;
		if( dYScale < 0.2 ) {
			dYScale = 0.2;
		}
		if( dYScale > 1.0 ) {
			dYScale = 1.0;
		}
		//		double dXDensity = GRITSXYPlot.getXDatasetDensity(getDataset(1), 0, getDomainAxis().getRange());
		//		double dScale = 0.2 + (dXDensity * 0.8);
		//		double dYScale2 = dataArea.getWidth() / dataArea.getHeight();
		//		dYScale = 1.5;
		//		dXScale = 3;
		double[] dMaxCellSizes = getMaxCellSize();
		
		double[] dScaledCellSizes = getGridCellDimensions(iNumVis, getDomainAxis().getRange(), getRangeAxis().getRange(), dMaxCellSizes);
//		double dXScale = dVisXDensity;
//		double dYScale = dVisYDensity;
		double dVisPeakFactor = getVisPeakFactor(getDataset(1), 0, getDomainAxis().getRange(), getRangeAxis().getRange());
		
		int iW = (int) (dMaxCellSizes[0] * dXScale * dVisPeakFactor);
		int iH = (int) (dMaxCellSizes[1] * dYScale  * dVisPeakFactor );
		initializeGrid((int)dScaledCellSizes[0], (int)dScaledCellSizes[1], 0.7, 1.0);
//		initializeGrid(iW, iH, 1.2, 1.0);
//		initializeGrid((int) (80.0d * dScale) , (int) (40.0d * dScale * getChartScale()), 0.92, 1.8);
		initAnnotationGrid(0.9);
		PlotOrientation orientation = getOrientation();
		ValueAxis axis = null;
		if (orientation == PlotOrientation.VERTICAL) {
			axis = getRangeAxis();
		} else {
			axis = getDomainAxis();
		}

		//		setGridScale(1.0d);
		drawPostPaintAnnotations(sg2, dataArea, axis, false, (dXScale > 0.8));  // CURRENT		
	}

	@Override
	public void drawAnnotations(Graphics2D g2, Rectangle2D dataArea,
			PlotRenderingInfo info) {
		this.dataArea = dataArea;
		this.info = info;
		//		System.out.println("XYPlot: drawAnnotations");
		super.drawAnnotations(g2, dataArea, info);
		drawGRITSAnnotations(g2, dataArea);
	}

	protected boolean drawPostPaintAnnotation(Graphics2D g2, Rectangle2D dataArea, 
			Double dPeakMass, List<Object> alAnnots, boolean bForceStacking, boolean bWriteMass) {
		double dScaleX = 1.0;
		double dScaleY = 1.0;

		double[] vals = htItemsToBestInt.get(dPeakMass);
		AnnotationCell cellDP = grid.getNewCell(vals[0], vals[1]);
		if( cellDP == null )
			return true;
		int iLineDPX = (int) (cellDP.getScreenLowX() * dScaleX);
		int iLineDPY = (int) (cellDP.getScreenLowY() * dScaleY);
		int iLineMinX = Integer.MAX_VALUE;
		int iLineMaxX = Integer.MIN_VALUE;
		int iLineMaxY = Integer.MIN_VALUE;

		htAnnotationToCoords = new HashMap<>();
		int iPrefX = -1;
		//		int iPrevPrefY = -1;
		for( Object oPeakAnnotation: alAnnots ) {
			AnnotationCell cell = grid.putAnnotation(oPeakAnnotation, vals[0], vals[1], iPrefX);
			if( cell == null && iPrefX != -1 ) {
				cell = grid.putAnnotation(oPeakAnnotation, vals[0], vals[1], -1);
			}
			if(cell != null ) {
				//				double[] dDataVals1 = grid.convertToDataValue(cell.getScreenLowX(), cell.getScreenHighY());
				int iCellX = (int) (cell.getScreenLowX() * dScaleX);
				int iCellX2 = (int) (cell.getScreenLowX() * dScaleX);
				int iCellY = (int) (cell.getScreenLowY() * dScaleY);
				int iCellY2 = (int) (cell.getScreenLowY() * dScaleY);
				if(  bForceStacking && iPrefX != -1 && iPrefX != (int) cell.getScreenLowX() ) {
					return false;
				}
				if( oPeakAnnotation instanceof GlycanImageObject ) {
					GlycanImageObject gio = (GlycanImageObject) oPeakAnnotation;
					gio.dispose();
					double dScaleValX = grid.getCellWidth();
					double dScaleValY = grid.getCellHeight();
					BufferedImage bImg = gio.getAwtBufferedImage();
					int iW = bImg.getWidth();
					int iH = bImg.getHeight();
					double dScaleFromX = (dScaleValX / iW);
					double dScaleFromY = (dScaleValY / iH);
					int width = (int) ( (double) bImg.getWidth() * dScaleFromX );
				    int height = (int) ( (double) bImg.getHeight() * dScaleFromY );
					java.awt.Image newImage = bImg.getScaledInstance( width, height, BufferedImage.SCALE_DEFAULT );
					BufferedImage bScaledImg = SimianImageConverter.convert(newImage);
					newImage.flush();
					double dW = bScaledImg.getWidth();
					double dH = bScaledImg.getHeight();
					
					double dScreenW = dW / getChartScale();
					double dScreen = dH;
					g2.drawImage(bScaledImg, iCellX, iCellY, (int) ((double) dScreenW), (int) ((double) dScreen), null);
					
					iCellY2 += bScaledImg.getHeight();
					iCellX2 += bScaledImg.getWidth();
					//					iPrevPrefY = iCellY - img.getHeight();
				} else {
					//					System.out.println("'How?");
					//					g2.drawString(oPeakAnnotation.toString(), (int) lineAnnot.j2DX1, (int) lineAnnot.j2DY1);
				}
				//			g2.drawString(dPeakMass.toString(),iLineAnnotX, iLineAnnotY);
				//				g2.setPaint(this.paint);
				if( iCellX2 > iLineMaxX ) {
					iLineMaxX = iCellX2;
				}
				if( iCellX < iLineMinX ) {
					iLineMinX = iCellX;
				}
				if( iCellY2 > iLineMaxY ) {
					iLineMaxY = iCellY2;
				}				
				iPrefX = (int) cell.getScreenLowX();

			}	
		}
		g2.setStroke(dashed);
		int iMidX = iLineMinX + ((iLineMaxX - iLineMinX) / 2);
		Line2D line1 = new Line2D.Float(iLineDPX, iLineDPY, iMidX, iLineMaxY);
		g2.setColor(Color.black);
		g2.draw(line1);
		//		Line2D line2 = new Line2D.Float(iLineMinX, iLineMaxY, iLineMaxX, iLineMaxY);
		//		g2.draw(line2);
		if( bWriteMass ) {
			int iTextWidth = g2.getFontMetrics().stringWidth(dPeakMass.toString());
			g2.drawString(dPeakMass.toString(), iMidX - (iTextWidth/2), iLineMaxY);
		}
		return true;
	}


	@Override
	public void zoom(double percent) {
		// TODO Auto-generated method stub
		super.zoom(percent);
	}

	private class LabelGenerator extends AbstractXYItemLabelGenerator 
	implements XYItemLabelGenerator {
		/**
		 * 
		 */
		int iSeriesInx;
		private static final long serialVersionUID = 1L;

		public LabelGenerator(int iSeriesInx) {
			super();  
			this.iSeriesInx = iSeriesInx;
		}

		public String generateLabel(XYDataset dataset,
				int x,
				int y) {

			//return Double.toString( dataset.getYValue(x, y) );
			double dy = dataset.getYValue(x, y);
			return getPeakLabel( this.iSeriesInx, dataset.getXValue(x, y) ).toString();
		}
	}

	@Override
	public void draw(Graphics2D g2, Rectangle2D area, Point2D anchor, PlotState parentState, PlotRenderingInfo info) {
		//		System.out.println("XYPlot: draw");
		super.draw(g2, area, anchor, parentState, info);
	}

}
