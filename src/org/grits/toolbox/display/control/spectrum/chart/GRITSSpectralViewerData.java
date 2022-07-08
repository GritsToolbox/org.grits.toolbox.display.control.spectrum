package org.grits.toolbox.display.control.spectrum.chart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.grits.toolbox.display.control.spectrum.datamodel.MSIonData;

public class GRITSSpectralViewerData {
	protected ArrayList<MSIonData> alRawData;
	protected HashMap<Double, List<Object>> htRawPeakLabels;
	protected ArrayList<MSIonData> alTheoRawData;
	protected HashMap<Double, List<Object>> htTheoRawPeakLabels;

	protected ArrayList<MSIonData> alPickedPeaks;
	protected HashMap<Double, List<Object>> htPickedPeakLabels;
	protected ArrayList<MSIonData> alTheoPickedPeaks;
	protected HashMap<Double, List<Object>> htTheoPickedPeakLabels;
	
	protected ArrayList<MSIonData> alAnnotatedPeaks;
	protected HashMap<Double, List<Object>> htAnnotatedPeakLabels;

	protected ArrayList<MSIonData> alUnAnnotatedPeaks;
	protected HashMap<Double, List<Object>> htUnAnnotatedPeakLabels;
	
	public ArrayList<MSIonData> getRawData() {
		return alRawData;
	}

	public void setRawData(ArrayList<MSIonData> alRawData) {
		this.alRawData = alRawData;
	}

	public HashMap<Double, List<Object>> getRawPeakLabels() {
		return htRawPeakLabels;
	}

	public void setRawPeakLabels(HashMap<Double, List<Object>> htRawPeakLabels) {
		this.htRawPeakLabels = htRawPeakLabels;
	}

	public ArrayList<MSIonData> getTheoRawData() {
		return alTheoRawData;
	}

	public void setTheoRawData(ArrayList<MSIonData> alTheoRawData) {
		this.alTheoRawData = alTheoRawData;
	}

	public HashMap<Double, List<Object>> getTheoRawPeakLabels() {
		return htTheoRawPeakLabels;
	}

	public void setTheoRawPeakLabels(
			HashMap<Double, List<Object>> htTheoRawPeakLabels) {
		this.htTheoRawPeakLabels = htTheoRawPeakLabels;
	}

	public ArrayList<MSIonData> getPickedPeaks() {
		return alPickedPeaks;
	}

	public void setPickedPeaks(ArrayList<MSIonData> alPickedPeaks) {
		this.alPickedPeaks = alPickedPeaks;
	}

	public HashMap<Double, List<Object>> getPickedPeakLabels() {
		return htPickedPeakLabels;
	}

	public void setPickedPeakLabels(
			HashMap<Double, List<Object>> htPickedPeakLabels) {
		this.htPickedPeakLabels = htPickedPeakLabels;
	}

	public ArrayList<MSIonData> getTheoPickedPeaks() {
		return alTheoPickedPeaks;
	}

	public void setTheoPickedPeaks(ArrayList<MSIonData> alTheoPickedPeaks) {
		this.alTheoPickedPeaks = alTheoPickedPeaks;
	}

	public HashMap<Double, List<Object>> getTheoPickedPeakLabels() {
		return htTheoPickedPeakLabels;
	}

	public void setTheoPickedPeakLabels(
			HashMap<Double, List<Object>> htTheoPickedPeakLabels) {
		this.htTheoPickedPeakLabels = htTheoPickedPeakLabels;
	}

	public ArrayList<MSIonData> getAnnotatedPeaks() {
		return alAnnotatedPeaks;
	}

	public void setAnnotatedPeaks(ArrayList<MSIonData> alAnnotatedPeaks) {
		this.alAnnotatedPeaks = alAnnotatedPeaks;
	}

	public HashMap<Double, List<Object>> getAnnotatedPeakLabels() {
		return htAnnotatedPeakLabels;
	}
	
	public void setAnnotatedPeakLabels(
			HashMap<Double, List<Object>> htAnnotatedPeakLabels) {
		this.htAnnotatedPeakLabels = htAnnotatedPeakLabels;
	}
	
	public ArrayList<MSIonData> getUnAnnotatedPeaks() {
		return alUnAnnotatedPeaks;
	}

	public void setUnAnnotatedPeaks(ArrayList<MSIonData> alUnAnnotatedPeaks) {
		this.alUnAnnotatedPeaks = alUnAnnotatedPeaks;
	}

	public HashMap<Double, List<Object>> getUnAnnotatedPeakLabels() {
		return htUnAnnotatedPeakLabels;
	}
	
	public void setUnAnnotatedPeakLabels(
			HashMap<Double, List<Object>> htUnAnnotatedPeakLabels) {
		this.htUnAnnotatedPeakLabels = htUnAnnotatedPeakLabels;
	}
	
}
