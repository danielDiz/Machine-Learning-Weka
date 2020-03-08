package weka.classification;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.functions.SMO;
import weka.classifiers.functions.supportVector.Kernel;
import weka.classifiers.functions.supportVector.RBFKernel;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.converters.ArffLoader.ArffReader;
import weka.core.converters.ArffSaver;
import weka.core.converters.TextDirectoryLoader;
import weka.core.tokenizers.NGramTokenizer;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

public class WekaTextClassifier {

	private FilteredClassifier classifier;

	// declare train and test data Instances
	private Instances trainData;
	private Instances testData;

	// Filter
	private StringToWordVector filterTrain;
	private StringToWordVector filterTest;

	// declare attributes of Instance
	private ArrayList<Attribute> wekaAttributes;

	// declare and initialize file locations
	private static final String TRAIN_DATA = "data/train/";
	private static final String TRAIN_DATA_ARFF = "data/trainARFF.arff";
	private static final String TEST_DATA = "data/test/";
	private static final String TEST_DATA_ARFF = "data/testARFF.arff";
	private static final String STOP_WORD_LIST = "data/stopwords.txt";

	public WekaTextClassifier() {
		//SMO Parameters
		double cValue = 1;
		double gammaValue = -6;
		Kernel kernelValue = new RBFKernel();
		double c = Math.pow(2,  cValue);
		double gamma = Math.pow(2,  gammaValue);
		
		//Clasification Model
		this.classifier = new FilteredClassifier();
		SMO smoClassifier = new SMO();
		smoClassifier.setKernel(kernelValue);
		smoClassifier.setC(c);
		((RBFKernel) kernelValue).setGamma(gamma);
		this.classifier.setClassifier(smoClassifier);
		

	}

	
	public void transform() {
		try {
			
			// load testdata
			if (new File(TRAIN_DATA_ARFF).exists()) {
				trainData = loadArff(TRAIN_DATA_ARFF);
			} else {
				trainData = loadTextDirectory(TRAIN_DATA);
				saveArff(trainData, TRAIN_DATA_ARFF);
			}
			System.out.println("done");
			trainData.setClassIndex(trainData.numAttributes() - 1);
			filterTrain.setInputFormat(trainData);
			classifier.setFilter(filterTrain);
			
			this.trainData = Filter.useFilter(trainData, filterTrain);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public StringToWordVector filterBuilder() {
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
			filter.setMinTermFreq(10);
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

	
	public void fit() {
		try {
			classifier.buildClassifier(trainData);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	public String evaluate() {
		System.out.println("Evaluation model...");
		try {
			// load testdata
			if (new File(TEST_DATA_ARFF).exists()) {
				testData = loadArff(TEST_DATA_ARFF);
			} else {
				testData = loadTextDirectory(TEST_DATA);
				saveArff(testData, TEST_DATA_ARFF);
			}
			testData.setClassIndex(testData.numAttributes() - 1);
			filterTest.setInputFormat(testData);
			testData = Filter.useFilter(testData, filterTest);
			
			//evaluation
			Evaluation eval = new Evaluation(trainData);
			eval.evaluateModel(classifier, testData);
			
			//Results
			return (eval.toSummaryString() + "\n" + eval.toClassDetailsString());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	
	public void loadModel(String fileName) {
		System.out.println("Loading model...");
		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(fileName));
			Object tmp = in.readObject();
			classifier = (FilteredClassifier) tmp;
			in.close();
			System.out.println("Loaded model: " + fileName);
		} catch (IOException e) {
			e.printStackTrace();
			;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	
	public void saveModel(String fileName) {
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

	
	public Instances loadTextDirectory(String fileName) {
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
	
	public Instances loadArff(String fileName) {
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

	
	public void saveArff(Instances dataset, String filename) {
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

	/**
	 * Main method. With an example usage of this class.
	 */
	public static void main(String[] args) throws Exception {
		final String MODEL = "data/modelWeka.model";

		WekaTextClassifier wt = new WekaTextClassifier();
		wt.setFilterTrain(wt.filterBuilder());
		wt.setFilterTest(wt.filterBuilder());
		
		if (new File(MODEL).exists()) {
			wt.loadModel(MODEL);
			wt.transform();
			wt.fit(); 
		} else { 
			System.out.println();
			wt.transform();
			wt.fit(); 
			wt.saveModel(MODEL); 
		}

		// run evaluation
		System.out.println("Evaluation Result: \n" + wt.evaluate());
	}
	
	
	

}
