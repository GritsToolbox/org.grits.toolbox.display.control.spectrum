package org.grits.toolbox.display.control.spectrum.datamodel;

import java.awt.geom.Rectangle2D;
import java.util.HashMap;

import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.ui.RectangleEdge;
import org.jfree.chart.plot.Plot;
import org.jfree.data.Range;

import org.grits.toolbox.display.control.spectrum.chart.GRITSChartComposite;
import org.grits.toolbox.display.control.spectrum.chart.GRITSXYPlot;

public class AnnotationGrid {

	protected AnnotationCell gridBounds = null;
	protected HashMap<AnnotationCell, Object> htCellToAnnotation;
	protected XYPlot plot = null;
	protected Rectangle2D dataArea = null;
	protected ValueAxis domainAxis = null;
	protected ValueAxis rangeAxis = null;
	protected double dCellHeight;
	protected double dCellWidth;
	protected int iMaxCellsHoriz;
	protected int iMaxCellVert;
	protected double dScaleX = 1.0;
	protected double dScaleY = 1.0;

	public AnnotationGrid(XYPlot plot, Rectangle2D dataArea,
			ValueAxis domainAxis, ValueAxis rangeAxis, double dScaleX, double dScaleY ) { 
		this.plot = plot;
		this.dataArea = dataArea;
		this.domainAxis = domainAxis;
		this.rangeAxis = rangeAxis;
		htCellToAnnotation = new HashMap<>();
		this.dScaleX = dScaleX;
		this.dScaleY = dScaleY;
		this.gridBounds = getNewCell(this.domainAxis.getLowerBound(), this.rangeAxis.getLowerBound(),
				this.domainAxis.getUpperBound(), this.rangeAxis.getUpperBound());
	}

	public void initializeGrid( double dCellWidth, double dCellHeight ) {
		if( gridBounds == null ) 
			return;

		this.dCellWidth = dCellWidth / dScaleX;
		this.dCellHeight = dCellHeight / dScaleY;
		this.iMaxCellsHoriz = (int) Math.floor( gridBounds.getScreenWidth() / dCellWidth );
		this.iMaxCellVert = (int) Math.floor( gridBounds.getScreenHeight() / dCellHeight );
	}

	public double getScaleX() {
		return dScaleX;
	}

	public double getScaleY() {
		return dScaleY;
	}


	public int[] getGridCoords( double x1, double y1 ) {
		double relX  = x1 - gridBounds.getScreenLowX();
		double relY = y1 - gridBounds.getScreenLowY();
		int iCellHoriz1 = (int) Math.floor( relX / dCellWidth );
		int iCellVert1 = (int) Math.floor( relY / dCellHeight ) - 1;
		if( iCellVert1 < 0 ) {
			iCellVert1 = 0;
		}
		return new int[] {iCellHoriz1, iCellVert1};
	}

	private AnnotationCell getPutAnnotationCell( double screenX, double screenY, int iAdder, int iPrefX ) {
		for( int y = 0; y <= iAdder; y++ ) {
			double dYAdd = (double) -1.0* (y * dCellHeight);
			int iCnt = 1;
			if( y != iAdder  ) {
				iCnt = 2 * iAdder;
			} 
			for( int x = iAdder; x >= -1 * iAdder; x -= iCnt ) {
				double dXAdd = (double) x * dCellWidth;
				AnnotationCell cell2 = new AnnotationCell(
						(float) (screenX + dXAdd), 
						(float) (screenX + dXAdd + dCellWidth),
						(float) (screenY + dYAdd - dCellHeight), 
						(float) (screenY + dYAdd) );
				if( iPrefX != -1 && ( (int) cell2.getScreenLowX() != iPrefX ) )
					continue;
				if( !isVisible(cell2.getScreenLowX(), cell2.getScreenLowY()) || 
						!isVisible(cell2.getScreenHighX(), cell2.getScreenHighY()) ) {
					continue;
				}
				if( ! htCellToAnnotation.containsKey(cell2) ) {
					return cell2;
				}
			}
		}
		if( iAdder >iMaxCellVert && iAdder > iMaxCellsHoriz )
			return null;
		return getPutAnnotationCell(screenX, screenY, iAdder+1, iPrefX);
	}

	public void fillCell( int iCol, int iRow ) {
		double gridScreenX = (double) iCol * dCellWidth + gridBounds.getScreenLowX();
		double gridScreenY = (double) (iRow-1) * dCellHeight + gridBounds.getScreenLowY();

		AnnotationCell cell = getPutAnnotationCell( gridScreenX, gridScreenY, 0, -1);
		if( cell != null ) {
			htCellToAnnotation.put(cell, Boolean.TRUE);
		} 
	}

	public AnnotationCell putAnnotation( Object obj, double domainX, double rangeY, int iPrefX) {
		AnnotationCell origCell = getNewCell(domainX, rangeY);
		if( origCell == null ) {
			return null;
		}
		int[] iGridCoords = getGridCoords(origCell.getScreenLowX(), origCell.getScreenHighY());
		double gridScreenX = (double) iGridCoords[0] * dCellWidth + gridBounds.getScreenLowX();
		double gridScreenY = (double) (iGridCoords[1]-1) * dCellHeight + gridBounds.getScreenLowY();

		AnnotationCell cell = getPutAnnotationCell( gridScreenX, gridScreenY, 0, iPrefX);
		if( cell != null ) {
			htCellToAnnotation.put(cell, obj);
		} 
		return cell;
	}

	public AnnotationCell getNewCell( double domainX, double rangeY ) {
		float screenLowX = 0.0f;
		float screenLowXp = 0.0f;
		float screenHighX = 0.0f;
		float screenHighY = 0.0f;
		float screenLowY = 0.0f;
		PlotOrientation orientation = plot.getOrientation();
		RectangleEdge domainEdge = Plot.resolveDomainAxisLocation(
				plot.getDomainAxisLocation(), orientation);
		RectangleEdge rangeEdge = Plot.resolveRangeAxisLocation(
				plot.getRangeAxisLocation(), orientation);
		if (orientation == PlotOrientation.VERTICAL) {
			screenLowX = (float) domainAxis.valueToJava2D(domainX, dataArea, domainEdge);
			screenLowY = (float) rangeAxis.valueToJava2D(rangeY, dataArea, rangeEdge);
			screenHighX = screenLowX;
			screenHighY = screenLowY;
		}
		else if (orientation == PlotOrientation.HORIZONTAL) {
			screenLowY = (float) domainAxis.valueToJava2D(domainX, dataArea, domainEdge);
			screenLowX = (float) rangeAxis.valueToJava2D(rangeY, dataArea, rangeEdge);
			screenHighY = screenLowY;
			screenHighX = screenLowX;
		}		
		if ( isVisible(screenLowX, screenLowY) && isVisible(screenHighX, screenHighY) ) {
			return new AnnotationCell( screenLowX, screenHighX, screenLowY, screenHighY);
		}
		return null;
	}

	public boolean isVisible( float screenX, float screenY ){
		if( gridBounds == null ) 
			return true;
		return screenX >= gridBounds.getScreenLowX() && screenX <= gridBounds.getScreenHighX() &&
				screenY >= gridBounds.getScreenLowY() && screenY <= gridBounds.getScreenHighY();
	}
	public double[] convertToDataValue( float dScreenX, float dScreenY ) {
		float dataX = 0.0f;
		float dataY = 0.0f;
		PlotOrientation orientation = plot.getOrientation();
		RectangleEdge domainEdge = Plot.resolveDomainAxisLocation(
				plot.getDomainAxisLocation(), orientation);
		RectangleEdge rangeEdge = Plot.resolveRangeAxisLocation(
				plot.getRangeAxisLocation(), orientation);
		dataX = (float) domainAxis.java2DToValue(dScreenX, dataArea, domainEdge);
		dataY = (float) rangeAxis.java2DToValue(dScreenY, dataArea, rangeEdge);
		return new double[] {dataX, dataY};
	}

	private AnnotationCell getNewCell( double domainLow, double rangeLow, double domainHigh, double rangeHigh ) {
		float screenLowX = 0.0f;
		float screenHighX = 0.0f;
		float screenHighY = 0.0f;
		float screenLowY = 0.0f;
		PlotOrientation orientation = plot.getOrientation();
		RectangleEdge domainEdge = Plot.resolveDomainAxisLocation(
				plot.getDomainAxisLocation(), orientation);
		RectangleEdge rangeEdge = Plot.resolveRangeAxisLocation(
				plot.getRangeAxisLocation(), orientation);
		if (orientation == PlotOrientation.VERTICAL) {
			screenLowX = (float) domainAxis.valueToJava2D(domainLow, dataArea, domainEdge);
			screenHighY = (float) rangeAxis.valueToJava2D(rangeLow, dataArea, rangeEdge);
			screenHighX = (float) domainAxis.valueToJava2D(domainHigh, dataArea, domainEdge);
			screenLowY = (float) rangeAxis.valueToJava2D(rangeHigh, dataArea, rangeEdge);
		}
		else if (orientation == PlotOrientation.HORIZONTAL) {
			screenHighY = (float) domainAxis.valueToJava2D(domainLow, dataArea, domainEdge);
			screenLowX = (float) rangeAxis.valueToJava2D(rangeLow, dataArea, rangeEdge);
			screenLowY = (float) domainAxis.valueToJava2D(domainHigh, dataArea, domainEdge);
			screenHighX = (float) rangeAxis.valueToJava2D(rangeHigh, dataArea, rangeEdge);
		}		
		if ( isVisible(screenLowX, screenLowY) && isVisible(screenHighX, screenHighY) ) {
			return new AnnotationCell( screenLowX, screenHighX, screenLowY, screenHighY);
		}
		return null;
	}

	public double getCellHeight() {
		return dCellHeight;
	}

	public double getCellWidth() {
		return dCellWidth;
	}

	public class AnnotationCell {
		float dScreenLowX = 0.0f;
		float dScreenHighX = 0.0f;
		float dScreenHighY = 0.0f;
		float dScreenLowY = 0.0f;
		private int dHashCode = -1;

		public AnnotationCell() {
			// TODO Auto-generated constructor stub
		}

		public AnnotationCell( float dScreenLowX, float dScreenHighX, 
				float dScreenLowY, float dScreenHighY) {
			this.dScreenLowX = dScreenLowX;
			this.dScreenHighX = dScreenHighX;
			this.dScreenLowY = dScreenLowY;
			this.dScreenHighY = dScreenHighY;
		}

		@Override
		public boolean equals(Object arg0) {
			if( ! (arg0 instanceof AnnotationCell) ) {
				return false;
			}
			return hashCode() == arg0.hashCode();
		}

		@Override
		public int hashCode() {
			if( this.dHashCode != -1 ) {
				return this.dHashCode;
			}
			int hash = 5;
			hash = hash * 17 + (int) dScreenLowX;
			hash = hash * 31 + (int) dScreenHighX;
			hash = hash * 13 + (int) dScreenHighY;
			hash = hash * 89 + (int) dScreenLowY;

			this.dHashCode = hash;
			return hashCode();
		}

		public float getScreenLowX() {
			return dScreenLowX;
		}

		public float getScreenHighX() {
			return dScreenHighX;
		}

		public float getScreenHighY() {
			return dScreenHighY;
		}

		public float getScreenLowY() {
			return dScreenLowY;
		}

		public float getScreenWidth() {  // x2 is bottom of area, x1 top left
			return getScreenHighX() - getScreenLowX();
		}

		public float getScreenHeight() { // y2 is top of area, y1 bottom 
			return getScreenHighY() - getScreenLowY();
		}

		@Override
		public String toString() {
			return "[ ["+ getScreenLowX() + ":" + getScreenHighY() + "], [" + getScreenHighX() + ":" + getScreenLowY() + "] ]";
		}
	}

}
