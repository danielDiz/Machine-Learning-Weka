package weka.classification;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import weka.classifiers.trees.j48.Stats;
import weka.core.AttributeStats;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.Stopwords;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;
import weka.core.converters.TextDirectoryLoader;
import weka.core.stopwords.Rainbow;
import weka.core.stopwords.WordsFromFile;
import weka.core.converters.ArffLoader.ArffReader;
import weka.core.tokenizers.NGramTokenizer;
import weka.core.tokenizers.WordTokenizer;
import weka.filters.Filter;
import weka.filters.supervised.instance.Resample;
import weka.filters.unsupervised.attribute.RemoveByName;
import weka.filters.unsupervised.attribute.StringToNominal;
import weka.filters.unsupervised.attribute.StringToWordVector;
import weka.filters.unsupervised.instance.RemovePercentage;

public class TextInstances {

	private ClassificationMode mode;

	enum ClassificationMode {
		CLASSIC, CLUSTER
	}

	// declare train and test data Instances
	private Instances trainData;
	private Instances testData;

	// declare and initialize file locations
	private static final String TRAIN_DATA = "data/trainCategorized/";
	private static final String TRAIN_ARFF = "data/trainARFF.arff";
	private static final String TEST_ARFF = "data/testARFF.arff";

	private static final String STOP_WORD_LIST = "data/stopwords.txt";

	private final int NUM_OF_WORDS = 250;

	public TextInstances(ClassificationMode mode) {
		this.mode = mode;
		// StringToWordVector filters
		StringToWordVector filterTrain = filterBuilderWordVector(NUM_OF_WORDS);
		StringToWordVector filterTest = filterBuilderWordVector(NUM_OF_WORDS);

		// Load data
		Instances rawData;
		if (new File(TRAIN_ARFF).exists() && new File(TEST_ARFF).exists()) {
			this.trainData = loadArff(TRAIN_ARFF);
			this.testData = loadArff(TEST_ARFF);
		} else {
			rawData = loadTextDirectory(TRAIN_DATA);

			// Split data
			try {
				int percentage = 30; // 70% train - 30% test
				RemovePercentage dataSplitterTrain = new RemovePercentage();
				dataSplitterTrain.setPercentage(percentage);
				dataSplitterTrain.setInputFormat(rawData);
				this.trainData = Filter.useFilter(rawData, dataSplitterTrain);

				RemovePercentage dataSplitterTest = new RemovePercentage();
				dataSplitterTest.setPercentage(percentage);
				dataSplitterTest.setInputFormat(rawData);
				dataSplitterTest.setInvertSelection(true);
				this.testData = Filter.useFilter(rawData, dataSplitterTest);
			} catch (Exception e) {
				e.printStackTrace();
			}

			// Save in arff files
			saveArff(trainData, TRAIN_ARFF);
			saveArff(testData, TEST_ARFF);
		}
		// System.out.println(trainData.numInstances());
		// System.out.println(testData.numInstances());

		// Set number of attributes if classic mode
		if (mode.equals(ClassificationMode.CLASSIC)) {
			this.trainData.setClassIndex(trainData.numAttributes() - 1);

			this.testData.setClassIndex(testData.numAttributes() - 1);
		}

		// Set input format
		try {
			filterTrain.setInputFormat(trainData);
			filterTest.setInputFormat(testData);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Filter data
		try {
			this.trainData = Filter.useFilter(this.trainData, filterTrain);
			this.testData = Filter.useFilter(this.testData, filterTest);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// RemoveByName filtering
		RemoveByName filterTrainUTF8 = filterBuilderRemoveByName();
		RemoveByName filterTestUTF8 = filterBuilderRemoveByName();

		try {
			filterTrainUTF8.setInputFormat(trainData);
			filterTestUTF8.setInputFormat(testData);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// Filter data
		try {
			// this.trainData = Filter.useFilter(this.trainData, filterTrainUTF8);
			// this.testData = Filter.useFilter(this.testData, filterTestUTF8);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// System.out.println(this.trainData.numAttributes());
		// System.out.println(this.testData.numAttributes());

	}

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
	private Instances loadCSV(String fileName) {
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

	// Builds a StringToWordVector filter
	private StringToWordVector filterBuilderWordVector(int numOfWords) {
		// Filter initialization
		StringToWordVector filter = new StringToWordVector(numOfWords);
		try {
			// Tokenization
			NGramTokenizer tokenizer = new NGramTokenizer();
			tokenizer.setNGramMinSize(1);
			tokenizer.setNGramMaxSize(5);
			tokenizer.setDelimiters("\\r\\t.,;:'\"()?!\\{\\}\\[\\]");

			filter.setTokenizer(tokenizer);

			// Filter options
			filter.setDoNotOperateOnPerClassBasis(true);
			filter.setIDFTransform(true);
			filter.setTFTransform(true);

			filter.setMinTermFreq(1);
			filter.setOutputWordCounts(true);

			// Stopwords
			if (new File(STOP_WORD_LIST).exists()) {
				WordsFromFile stopwords = new WordsFromFile();
				stopwords.setStopwords(new File(STOP_WORD_LIST));

				// filter.setStopwordsHandler(stopwords); // 3.6.xx or above (confirmed 3.8.x)
				// filter.setStopwords(new File("data/stopwords.txt")); //version 3.6.x or lower
			}

			// Stemming
			/*
			 * SnowballStemmer stemmer = new SnowballStemmer();
			 * stemmer.setStemmer("english"); filter.setStemmer(stemmer);
			 */

		} catch (Exception e) {
			e.printStackTrace();
		}
		return filter;
	}

	// Buil a RemoveByName filter
	private RemoveByName filterBuilderRemoveByName() {
		RemoveByName filter = new RemoveByName();
		filter.setExpression("\\p{L}+"); // only utf-8 chars //TODO: add @ # etc
		filter.setInvertSelection(false);

		return filter;
	}

	// Builds a StringToWordVector
	private StringToNominal filterBuilderNominal() {
		StringToNominal filter = new StringToNominal();
		filter.setAttributeRange("first");

		return filter;
	}

	// Getter and Setters

	public Instances getTrainData() {
		return trainData;
	}

	public void setTrainData(Instances trainData) {
		this.trainData = trainData;
	}

	public Instances getTestData() {
		return testData;
	}

	public void setTestData(Instances testData) {
		this.testData = testData;
	}

}
