package org.grits.toolbox.display.control.spectrum.datamodel;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.Serializable;

/**
 *
 * @author brentw
 */
public class MSIonData implements Serializable, Comparable {
    private double dMass;
    private double dIntensity;
    public static final long serialVersionUID = -3221098324357679944L;
    public MSIonData() {
        dMass = 0.0d;
        dIntensity = 0.0d;
    }
    
    public MSIonData( double _dMass, double _dIntensity ) {
        dMass = _dMass;
        dIntensity = _dIntensity;
    }
    
    public double getMass() {
        return dMass;
    }
    
    public double getIntensity() {
        return dIntensity;
    }

    public int compareTo(Object obj) throws ClassCastException {
        if ( ! (obj instanceof MSIonData) ) 
              throw new ClassCastException("Peptide object expected.");
        
        return Double.compare(dMass, ( (MSIonData) obj ).getMass() );
    }
    
    @Override
    public boolean equals(Object arg0) {
    	if( !(arg0 instanceof MSIonData) ) {
    		return false;
    	}
    	MSIonData other = (MSIonData) arg0;
    	return getMass() == other.getMass();
     }
    
    
    public String toString() {
        return dMass + ":" + dIntensity;
    }
}
