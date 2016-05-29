package core;

import java.io.Serializable;
import java.util.Enumeration;
import weka.core.Instances;
import weka.core.Instance;

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
		double[][] data = new double[instances.numInstances()][];
		int i = 0;
		for(Enumeration<Instance> e = instances.enumerateInstances(); e.hasMoreElements(); i++) {
			Instance instance = e.nextElement();
			data[i] = instance.toDoubleArray();
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
