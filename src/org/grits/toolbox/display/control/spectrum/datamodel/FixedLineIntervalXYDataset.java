/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.grits.toolbox.display.control.spectrum.datamodel;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.IntervalXYDataset;

/**
 *
 * @author brentw
 */
public class FixedLineIntervalXYDataset extends DefaultXYDataset implements IntervalXYDataset{
	private static final long serialVersionUID = 1L;
	public FixedLineIntervalXYDataset(){
          super();
       }
       public Number getStartX(int series,int item){
          return super.getX(series,item);
       }
       public Number getEndX(int series,int item){
          return super.getX(series,item);
       }
       public Number getStartY(int series,int item){
          return 0;
       }
       public Number getEndY(int series,int item){
          return super.getY(series,item);
       }
       public double getStartXValue(int series,int item){
          return super.getXValue(series,item);
       }
       public double getEndXValue(int series,int item){
          return super.getXValue(series,item);
       }
       public double getStartYValue(int series,int item){
          return 0;
       }
       public double getEndYValue(int series,int item){
          return super.getYValue(series,item);
       }

}
