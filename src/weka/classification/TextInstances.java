package weka.classification;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.TextDirectoryLoader;
import weka.core.converters.ArffLoader.ArffReader;
import weka.core.tokenizers.NGramTokenizer;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

public class TextInstances {
	
	// declare train and test data Instances
	private Instances trainData;
	private Instances testData;
	
	
	// Filter
	private StringToWordVector filterTrain;
	
	private StringToWordVector filterTest;
	
	// declare and initialize file locations
	private static final String TRAIN_DATA = "data/train/";
	private static final String TRAIN_ARFF = "data/trainARFF.arff";
	private static final String TEST_DATA = "data/test/";
	private static final String TEST_ARFF = "data/testARFF.arff";
		
		
	private static final String UNSUP_DATA = "data/unsupervised/";
	private static final String UNSUP_ARFF = "data/unsup.arff";
		
	private static final String STOP_WORD_LIST = "data/stopwords.txt";
	
	
	public TextInstances() {
		//Filters
		this.filterTrain = filterBuilder();
		this.filterTest = filterBuilder();
		
		//Train data
		if (new File(TRAIN_ARFF).exists()) {
			this.trainData = loadArff(TRAIN_ARFF);
		} else {
			this.trainData = loadTextDirectory(TRAIN_DATA);
			saveArff(trainData, TRAIN_ARFF);
		}
		
		//this.trainData.setClassIndex(trainData.numAttributes() - 1);
		
		try {
			this.filterTrain.setInputFormat(trainData);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//Test data
		if (new File(UNSUP_ARFF).exists()) {
			this.testData = loadArff(UNSUP_ARFF);
		} else {
			this.testData = loadTextDirectory(UNSUP_DATA);
			saveArff(testData, UNSUP_ARFF);
		}
		
		//testData.setClassIndex(testData.numAttributes() - 1);
		
		try {
			this.filterTest.setInputFormat(testData);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	
	//Loads a text directory as an Instance object
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
	
	//Loads an ARFF file as an Instance object
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
	
	//Saves an Instance object as an ARFF file
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
	
	//Builds a StringToWordVector
	private StringToWordVector filterBuilder() {
		// Filter initialization
		StringToWordVector filter = new StringToWordVector(400);
		try {
			//Tokenization
			NGramTokenizer tokenizer = new NGramTokenizer();
			tokenizer.setNGramMinSize(1);
			tokenizer.setNGramMaxSize(2);
			tokenizer.setDelimiters("\\W");
			filter.setTokenizer(tokenizer);
			
			
			//Filter options
			filter.setDoNotOperateOnPerClassBasis(true);
			filter.setIDFTransform(true);
			filter.setTFTransform(true);
			filter.setMinTermFreq(100);
			filter.setLowerCaseTokens(true);
			filter.setOutputWordCounts(true);
			
			//Stopwords
			
			//filter.setStopwords(new File("data/stopwords.txt"));
			
			//Stemming
			/*SnowballStemmer stemmer = new SnowballStemmer();
			stemmer.setStemmer("english");
			filter.setStemmer(stemmer);*/
			
		/*} catch (IOException e) {
			System.err.println("Problem found when reading: " + STOP_WORD_LIST);
			*/
		} catch (Exception e) {
			e.printStackTrace();
		}
		return filter;
	}
	
	//Filter data using the filters
	public void filterData() {
		try {
			trainData = Filter.useFilter(trainData, filterTrain);
			testData = Filter.useFilter(testData, filterTest);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//Getter and Setters
	
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
	
	
	public StringToWordVector getFilterTrain() {
		return filterTrain;
	}


	public void setFilterTrain(StringToWordVector filterTrain) {
		this.filterTrain = filterTrain;
	}


	public StringToWordVector getFilterTest() {
		return filterTest;
	}


	public void setFilterTest(StringToWordVector filterTest) {
		this.filterTest = filterTest;
	}


}
