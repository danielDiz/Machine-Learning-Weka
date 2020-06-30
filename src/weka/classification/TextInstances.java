package weka.classification;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;

import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.ASSearch;
import weka.attributeSelection.BestFirst;
import weka.attributeSelection.CfsSubsetEval;
import weka.attributeSelection.GreedyStepwise;
import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.MultiObjectiveEvolutionarySearch;
import weka.attributeSelection.OneRAttributeEval;
import weka.attributeSelection.Ranker;
import weka.attributeSelection.ReliefFAttributeEval;
import weka.core.Instances;
import weka.core.OptionHandler;
import weka.core.Utils;
import weka.core.converters.ArffLoader.ArffReader;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;
import weka.core.converters.TextDirectoryLoader;
import weka.core.stopwords.WordsFromFile;
import weka.core.tokenizers.NGramTokenizer;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.unsupervised.attribute.NumericToBinary;
import weka.filters.unsupervised.attribute.NumericToNominal;
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

	private final int NUM_OF_WORDS = 1000; // 1350 -> 71%

	public TextInstances(ClassificationMode mode) {
		this.mode = mode;

		// Load data
		Instances rawData;

		if (new File(TRAIN_ARFF).exists() && new File(TEST_ARFF).exists()) {
			this.trainData = loadArff(TRAIN_ARFF);
			this.testData = loadArff(TEST_ARFF);
			
			// Set number of attributes if classic mode
			if (mode.equals(ClassificationMode.CLASSIC)) {
				this.trainData.setClassIndex(trainData.numAttributes() - 1);

				this.testData.setClassIndex(testData.numAttributes() - 1);
			}

		} else {
			

			
			

			// Save in arff files
			//saveArff(trainData, TRAIN_ARFF);
			//saveArff(testData, TEST_ARFF);
		}
		
		rawData = loadTextDirectory(TRAIN_DATA);

		// rawData.randomize(new Random(System.currentTimeMillis()));

		// Split data
		double percentage = 30; // 70% train - 30% test
//		
//		int trainingSize = (int) (rawData.numInstances() * (percentage / 100.0));
//		int testingSize = rawData.numInstances() - trainingSize;
//		
//		Instances trainData = new Instances(rawData, 0, trainingSize);
//		Instances testData = new Instances(rawData, trainingSize, testingSize);

		
		// Set number of attributes if classic mode
		if (mode.equals(ClassificationMode.CLASSIC)) {
			rawData.setClassIndex(rawData.numAttributes() - 1);

		}
		

		// StringToWordVector filtering
		rawData = filterStringToWordVector(rawData);

		//saveArff(rawData, "data/raw2.arff");
		
		// RemoveByNam2 filtering
		//this.trainData = filterRemoveByName(this.trainData);
		//this.testData = filterRemoveByName(this.testData);
		rawData = filterRemoveByName(rawData);

		//saveArff(rawData, "data/raw3.arff");
		
		// AtributeSelection filtering
		//rawData = filterAttributeSelection1(rawData);
		rawData = filterAttributeSelection2(rawData);

		//saveArff(rawData, "data/raw4.arff");
		
		try {

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
		
		//saveArff(trainData, "data/train4.arff");
		//saveArff(testData, "data/test4.arff");

		System.out.println("done with instances");

	}

	private Instances filterStringToWordVector(Instances ins) {
		// Filter initialization
		StringToWordVector filter = new StringToWordVector(NUM_OF_WORDS);
		try {
			// Tokenization
			NGramTokenizer tokenizer = new NGramTokenizer();
			tokenizer.setNGramMinSize(1);
			tokenizer.setNGramMaxSize(6);
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

			filter.setInputFormat(ins);

		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			return Filter.useFilter(ins, filter);
		} catch (Exception e) {
			e.printStackTrace();
			return ins;
		}

	}

	// RemoveByName filtering
	private Instances filterRemoveByName(Instances ins) {
		RemoveByName filter = new RemoveByName();
		filter.setExpression("\\p{L}+"); // only utf-8 chars //TODO: add @ # etc
		filter.setInvertSelection(true);

		try {
			filter.setInputFormat(ins);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			return Filter.useFilter(ins, filter);
		} catch (Exception e) {
			e.printStackTrace();
			return ins;
		}
	}

	// Builds a StringToWordVector
	private Instances filterNumericToNominal(Instances ins) {
		NumericToNominal filter = new NumericToNominal();

		try {
			filter.setInputFormat(ins);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			return Filter.useFilter(ins, filter);
		} catch (Exception e) {
			e.printStackTrace();
			return ins;
		}
	}

	private Instances filterAttributeSelection1(Instances ins) {
		AttributeSelection filter = new AttributeSelection();
		
		OneRAttributeEval eval = new OneRAttributeEval();
		try {
			eval.setOptions(Utils.splitOptions("-S 0 -F 13 -B 18"));
		} catch (Exception e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		Ranker search = new Ranker();
		try {
			search.setOptions(Utils.splitOptions("-T 4.194539409525584"));
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		filter.setEvaluator(eval);
		filter.setSearch(search);
		try {
			filter.setInputFormat(ins);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		try {
			return Filter.useFilter(ins, filter);
		} catch (Exception e) {
			e.printStackTrace();
			return ins;
		}
	}
	
	private Instances filterAttributeSelection2(Instances ins) {
		AttributeSelection filter = new AttributeSelection();
		

		CfsSubsetEval eval = new CfsSubsetEval();
		String[] options = { "CfsSubsetEval.class", "CFS_SUBSET_EVAL_CONFIG", "MultiObjectiveEvolutionarySearch.class",
				"-generations 10 -population-size 100 -seed 1 -a 0" };
		try {
			((OptionHandler) eval).setOptions(Utils.splitOptions("CFS_SUBSET_EVAL_CONFIG"));
		} catch (Exception e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		BestFirst search = new BestFirst();

		try {
			search.setOptions(Utils.splitOptions("-D 1 -N 5 -S 0"));
		} catch (Exception e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

//		MultiObjectiveEvolutionarySearch search = new MultiObjectiveEvolutionarySearch();
//		try {
//			((OptionHandler) search)
//					.setOptions(Utils.splitOptions("-generations 10 -population-size 100 -seed 1 -a 0"));
//		} catch (Exception e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}

		filter.setEvaluator(eval);
		filter.setSearch(search);
		try {
			filter.setInputFormat(ins);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		try {
			return Filter.useFilter(ins, filter);
		} catch (Exception e) {
			e.printStackTrace();
			return ins;
		}
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
