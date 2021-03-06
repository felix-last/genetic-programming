// TODO: Apply Weka here
package core;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import utils.Utils;
import weka.WekaRun;

public class Main {
// TODO: Shuffle and split false/true
	public static final boolean SHUFFLE_AND_SPLIT = true;
	// public static final boolean SHUFFLE_AND_SPLIT = false;

	public static final String DATA_FILENAME = "dataset";
// TODO: # runs, # generations
	// public static final String DATA_FILENAME = "ppb";
	public static final int NUMBER_OF_RUNS = 1;
	public static final int NUMBER_OF_GENERATIONS = 500;

	public static void main(String[] args) {

		// load training and unseen data
		Data dataRaw = loadData(DATA_FILENAME);
		
		WekaRun.loadData(DATA_FILENAME);
		WekaRun.preprocessData(false, false);
		Data data = new Data(WekaRun.trainingData, WekaRun.unseenData);

		// run GP for a given number of runs
		double[][] resultsPerRun = new double[4][NUMBER_OF_RUNS];
		Individual[] bestFoundPerRun = new Individual[NUMBER_OF_RUNS];
		for (int i = 0; i < NUMBER_OF_RUNS; i++) {
			System.out.printf("\n\t\t##### Run %d #####\n", i + 1);
			//GpRun gp = new GpRun(data);
			GsgpRun gp = new GsgpRun(data);
			gp.evolve(NUMBER_OF_GENERATIONS);
			bestFoundPerRun[i] = gp.getCurrentBest();
			resultsPerRun[0][i] = bestFoundPerRun[i].getTrainingError();
			resultsPerRun[1][i] = bestFoundPerRun[i].getUnseenError();
			resultsPerRun[2][i] = bestFoundPerRun[i].getSize();
			resultsPerRun[3][i] = bestFoundPerRun[i].getDepth();
			System.out.print("\nBest =>");
			bestFoundPerRun[i].print();
			System.out.println();
		}

		// present average results
		System.out.printf("\n\t\t##### Results (all runs) #####\n\n");
		System.out.printf("Average training error:\t\t%.2f\n", Utils.getAverage(resultsPerRun[0]));
		System.out.printf("Average unseen error:\t\t%.2f\n", Utils.getAverage(resultsPerRun[1]));
		System.out.printf("Average size:\t\t\t%.2f\n", Utils.getAverage(resultsPerRun[2]));
		System.out.printf("Average depth:\t\t\t%.2f\n", Utils.getAverage(resultsPerRun[3]));
		
		
		System.out.printf("\n\t\t##### Results (best run) #####\n\n");
		double[] unseenErrors = resultsPerRun[1];
		int bestIndex = 0;
		for(int i = 1; i < unseenErrors.length; i++) {
			if(unseenErrors[i] < unseenErrors[bestIndex]) {
				bestIndex = i;
			}
		}
		System.out.printf("Training error:\t\t%.2f\n", resultsPerRun[0][bestIndex]);
		System.out.printf("Unseen error:\t\t%.2f\n", resultsPerRun[1][bestIndex]);
		System.out.printf("Size:\t\t\t%.2f\n", resultsPerRun[2][bestIndex]);
		System.out.printf("Depth:\t\t\t%.2f\n", resultsPerRun[3][bestIndex]);
		// write best of all runs to file
		bestFoundPerRun[bestIndex].writeToObjectFile();
	}

	public static Data loadData(String dataFilename) {
		double[][] trainingData, unseenData;

		if (SHUFFLE_AND_SPLIT) {
			double[][] allData = readData(dataFilename + ".txt");
			List<Integer> instances = Utils.shuffleInstances(allData.length);
			int trainingInstances = (int) Math.floor(0.9 * allData.length);
			int unseenInstances = (int) Math.ceil(0.1 * allData.length);

			trainingData = new double[trainingInstances][];
			unseenData = new double[unseenInstances][];

			for (int i = 0; i < trainingInstances; i++) {
				trainingData[i] = allData[instances.get(i)];
			}

			for (int i = 0; i < unseenInstances; i++) {
				unseenData[i] = allData[instances.get(trainingInstances + i)];
			}
		} else {
			trainingData = readData(dataFilename + "_training.txt");
			unseenData = readData(dataFilename + "_unseen.txt");
		}
		return new Data(trainingData, unseenData);
	}

	public static double[][] readData(String filename) {
		double[][] data = null;
		List<String> allLines = new ArrayList<String>();
		try {
			BufferedReader inputBuffer = new BufferedReader(new FileReader(filename));
			String line = inputBuffer.readLine();
			while (line != null) {
				allLines.add(line);
				line = inputBuffer.readLine();
			}
			inputBuffer.close();
		} catch (Exception e) {
			System.out.println(e);
		}

		StringTokenizer tokens = new StringTokenizer(allLines.get(0).trim());
		int numberOfColumns = tokens.countTokens();
		data = new double[allLines.size()][numberOfColumns];
		for (int i = 0; i < data.length; i++) {
			tokens = new StringTokenizer(allLines.get(i).trim());
			for (int k = 0; k < numberOfColumns; k++) {
				data[i][k] = Double.parseDouble(tokens.nextToken().trim());
			}
		}
		return data;
	}
}
