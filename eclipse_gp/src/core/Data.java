package core;

import java.io.Serializable;
import weka.core.Instances;

public class Data implements Serializable {

	private static final long serialVersionUID = 7L;

	protected double[][] trainingData, unseenData;

	public Data(double[][] trainingData, double[][] unseenData) {
		this.trainingData = trainingData;
		this.unseenData = unseenData;
	}
	
	public Data(Instances trainingInstances, Instances unseenInstances) {
		this.trainingData = this.dataFromInstance(trainingInstances);
		this.unseenData = this.dataFromInstance(unseenInstances);
	}

	private double[][] dataFromInstance(Instances instances) {
		double[][] dataByAttributes = new double[instances.numAttributes()][];
		for(int i = 0; i < instances.numAttributes(); i++) {
			dataByAttributes[i] = instances.attributeToDoubleArray(i);
		}
		// transpose
		double[][] data = new double[dataByAttributes[0].length][dataByAttributes.length];
		for(int i = 0; i < dataByAttributes.length; i++) {
			double[] attr = dataByAttributes[i];
			for(int j = 0; j < attr.length; j++) {
				data[j][i] = attr[j];
			}
		}
		return data;
	}

	// assumes one output variable
	public int getDimensionality() {
		return trainingData[0].length - 1;
	}

	public double[][] getTrainingData() {
		return trainingData;
	}

	public double[][] getUnseenData() {
		return unseenData;
	}

	public void setTrainingData(double[][] trainingData) {
		this.trainingData = trainingData;
	}

	public void setUnseenData(double[][] unseenData) {
		this.unseenData = unseenData;
	}
}
