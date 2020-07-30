package classification.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import weka.classifiers.Classifier;
import weka.classifiers.meta.Vote;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ArffSaver;
import weka.core.converters.TextDirectoryLoader;
import weka.core.converters.ArffLoader.ArffReader;

public class UtilsFiles {
	// Original data
	public static final String CATEGORIES_DATA = "data/categories";
	public static final String ORIGINAL_DATA = "data/train";
	public static final String COMPRESSED_DATA = "data/compressed_data";
	// Reordered data
	public static final String REORDERED_DATA = "data/reordered";

	// Data files
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

	public static Vote loadModel(String fileName) {
		System.out.println("Loading model...");
		try {
			Object tmp = SerializationHelper.read(fileName);

			System.out.println("Loaded model from " + fileName);
			return (Vote) tmp;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void saveModel(Vote classifier, String fileName) {
		System.out.println("Saving model...");
		try {
			SerializationHelper.write(fileName, classifier);
			System.out.println("Saved model in " + fileName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
