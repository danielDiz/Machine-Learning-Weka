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
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToNominal;
import weka.filters.unsupervised.attribute.StringToWordVector;

public class TextInstances {

	private ClassificationMode mode;

	enum ClassificationMode {
		CLASSIC, CLUSTER
	}

	// declare train and test data Instances
	private Instances trainData;
	private Instances testData;

	// Filter
	private Filter filterTrain;

	private Filter filterTest;

	// declare and initialize file locations
	private static final String TRAIN_DATA = "data/train/";
	private static final String TRAIN_ARFF = "data/trainARFF.arff";
	private static final String TEST_DATA = "data/test/";
	private static final String TEST_ARFF = "data/testARFF.arff";

	private static final String UNSUP_DATA = "data/unsupervised/";
	private static final String UNSUP_ARFF = "data/unsup.arff";

	private static final String STOP_WORD_LIST = "data/stopwords.txt";

	public TextInstances(ClassificationMode mode) {
		this.mode = mode;
		// Filters
		this.filterTrain = filterBuilderWordVector();
		this.filterTest = filterBuilderWordVector();

		// Train data
		if (new File(TRAIN_ARFF).exists()) {
			this.trainData = loadArff(TRAIN_ARFF);
		} else {
			this.trainData = loadTextDirectory(TRAIN_DATA);
			saveArff(trainData, TRAIN_ARFF);
		}

		// Set number of attributes if classic mode
		if (mode.equals(ClassificationMode.CLASSIC)) {
			this.trainData.setClassIndex(trainData.numAttributes() - 1);
		}

		// set input format
		try {
			this.filterTrain.setInputFormat(trainData);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Test data
		if (new File(TEST_ARFF).exists()) {
			this.testData = loadArff(TEST_ARFF);
		} else {
			this.testData = loadTextDirectory(TEST_DATA);
			saveArff(testData, TEST_ARFF);
		}

		// Set number of attributes if classic mode
		if (mode.equals(ClassificationMode.CLASSIC)) {
			testData.setClassIndex(testData.numAttributes() - 1);
		}

		// set input format
		try {
			this.filterTest.setInputFormat(testData);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			this.trainData = Filter.useFilter(this.trainData, this.filterTrain);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			this.testData = Filter.useFilter(this.testData, this.filterTest);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// Loads a text directory as an Instance object
	private Instances loadTextDirectory(String fileName) {
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

	/*
	// Loads a text file as an Instance object
	public Instances loadTextFile(String fileName) {

		Instances dataset = new Instances();

		// read text file, parse data and add to instance
		try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
			for (String line; (line = br.readLine()) != null;) {
				// split at first occurance of n no. of words
				String[] parts = line.split("\\s+", 2);

				// basic validation
				if (!parts[0].isEmpty() && !parts[1].isEmpty()) {

					DenseInstance row = new DenseInstance(2);
					row.setValue(wekaAttributes.get(0), parts[0]);
					row.setValue(wekaAttributes.get(1), parts[1]);

					// add row to instances
					dataset.add(row);
				}

			}

		} catch (IOException e) {
			LOGGER.warning(e.getMessage());
		} catch (ArrayIndexOutOfBoundsException e) {
			LOGGER.info("invalid row.");
		}
		return dataset;

	}
*/
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
	private Instances loadArff(String fileName) {
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
	private void saveArff(Instances dataset, String filename) {
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

	// Builds a StringToWordVector
	private StringToWordVector filterBuilderWordVector() {
		// Filter initialization
		StringToWordVector filter = new StringToWordVector(100);
		try {
			// Tokenization
			NGramTokenizer tokenizer = new NGramTokenizer();
			tokenizer.setNGramMinSize(1);
			tokenizer.setNGramMaxSize(3);
			tokenizer.setDelimiters("\\W");
			filter.setTokenizer(tokenizer);

			// Filter options
			filter.setDoNotOperateOnPerClassBasis(true);
			filter.setIDFTransform(true);
			filter.setTFTransform(true);
			filter.setMinTermFreq(200);
			filter.setLowerCaseTokens(true);
			filter.setOutputWordCounts(true);

			// Stopwords
			if (new File(STOP_WORD_LIST).exists()) {
				WordsFromFile stopwords = new WordsFromFile();
				stopwords.setStopwords(new File(STOP_WORD_LIST));

				filter.setStopwordsHandler(stopwords); // 3.6.xx or above (confirmed 3.8.x)
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

	// Builds a StringToWordVector
	private StringToNominal filterBuilderNominal() {
		// Filter initialization
		StringToNominal filter = new StringToNominal();
		filter.setAttributeRange("first");

		return filter;
	}

	// Filter data using the filters
	public void filterData() {
		try {
			trainData = Filter.useFilter(trainData, filterTrain);
			testData = Filter.useFilter(testData, filterTest);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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

	public Filter getFilterTrain() {
		return filterTrain;
	}

	public void setFilterTrain(Filter filterTrain) {
		this.filterTrain = filterTrain;
	}

	public Filter getFilterTest() {
		return filterTest;
	}

	public void setFilterTest(Filter filterTest) {
		this.filterTest = filterTest;
	}

}
