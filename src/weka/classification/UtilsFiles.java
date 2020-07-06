package weka.classification;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import weka.classifiers.Classifier;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;
import weka.core.converters.TextDirectoryLoader;
import weka.core.converters.ArffLoader.ArffReader;

public class UtilsFiles {
	// Data files
	public static final String TRAIN_DATA = "data/reordered";
	public static final String TRAIN_ARFF = "data/train.arff";
	public static final String TEST_ARFF = "data/test.arff";

	public static final String STOP_WORD_LIST = "data/stopwords.txt";
	
	public static final String MODEL = "data/modelWeka.model";
	public static final String MODEL_CLUSTER = "data/clusterWeka.model";
	
	
	
	// Loads a text directory as an Instance object
	public static Instances loadTextDirectory(String fileName) {
		System.out.println("Loading from: " + fileName);
		try {
			TextDirectoryLoader loader = new TextDirectoryLoader();
			loader.setDirectory(new File(fileName));
			Instances rawData = loader.getDataSet();

			return rawData;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	// Loads a csv file as an Instance object
	public static Instances loadCSV(String fileName) {
		System.out.println("Loading from: " + fileName);
		try {
			CSVLoader loader = new CSVLoader();
			loader.setSource(new File(fileName));
			Instances rawData = loader.getDataSet();

			return rawData;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	// Loads an ARFF file as an Instance object
	public static Instances loadArff(String fileName) {
		System.out.println("Loading file: " + fileName);
		try {
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			ArffReader arff = new ArffReader(reader);
			Instances dataset = arff.getData();

			reader.close();
			return dataset;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	// Saves an Instance object as an ARFF file
	public static void saveArff(Instances dataset, String filename) {
		System.out.println("Saving file:" + dataset.relationName() + "  to: " + filename);
		try {
			// initialize
			ArffSaver arffSaverInstance = new ArffSaver();
			arffSaverInstance.setInstances(dataset);
			arffSaverInstance.setFile(new File(filename));
			arffSaverInstance.writeBatch();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Classifier loadModel(String fileName) {
		System.out.println("Loading model...");
		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(fileName));
			Object tmp = in.readObject();
			in.close();
			System.out.println("Loaded model: " + fileName);
			return (Classifier) tmp;
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void saveModel(Object classifier, String fileName) {
		System.out.println("Saving model...");
		try {
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fileName));
			out.writeObject(classifier);
			out.close();
			System.out.println("Saved model: " + fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
