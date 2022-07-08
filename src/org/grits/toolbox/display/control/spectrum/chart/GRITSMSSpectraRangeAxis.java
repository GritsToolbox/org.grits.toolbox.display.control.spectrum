package org.grits.toolbox.display.control.spectrum.chart;

import org.jfree.chart.axis.NumberAxis;

/**
 * Extension of NumberAxis to force auto-zoom to make upper bound a bit larger to facilitate labeling of the highest peak.
 * 
 * @author D Brent Weatherly (dbrentw@uga.edu)
 *
 */
public class GRITSMSSpectraRangeAxis extends NumberAxis {


	/**
	 * 
	 */
	private static final long serialVersionUID = -734277865186717547L;

	public GRITSMSSpectraRangeAxis(String label) {
		super(label);
	}

	/* (non-Javadoc)
	 * @see org.jfree.chart.axis.NumberAxis#autoAdjustRange()
	 * 
	 * If the upper bound (assuming peak intensity) has been initialized and is more than 10, multiply by 1.75 to increase the max intensity. 
	 */
	@Override
	protected void autoAdjustRange() {
		super.autoAdjustRange();
		double upperBound = getUpperBound();
		if( upperBound > 10.0 ){
			setUpperBound(upperBound * 1.75);
		}
	}
	
	@Override
	public void zoomRange(double lowerPercent, double upperPercent) {
		// TODO Auto-generated method stub
		super.zoomRange(lowerPercent, upperPercent);
	}
	
	
	
}
