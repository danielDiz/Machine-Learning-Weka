package weka.classification;

import java.io.File;

import weka.attributeSelection.BestFirst;
import weka.attributeSelection.CfsSubsetEval;
import weka.core.Instances;
import weka.core.OptionHandler;
import weka.core.Utils;
import weka.core.stopwords.WordsFromFile;
import weka.core.tokenizers.NGramTokenizer;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.supervised.instance.StratifiedRemoveFolds;
import weka.filters.unsupervised.attribute.RemoveByName;
import weka.filters.unsupervised.attribute.StringToWordVector;

public class TextInstances {

	enum ClassificationMode {
		CLASSIC, CLUSTER
	}

	// declare train and test data Instances
	private Instances trainData;
	private Instances testData;

	// declare and initialize file locations

	private final int NUM_OF_WORDS = 400;

	public TextInstances(ClassificationMode mode) {
		if (new File(UtilsFiles.TRAIN_ARFF).exists() && new File(UtilsFiles.TEST_ARFF).exists()) {
			this.trainData = UtilsFiles.loadArff(UtilsFiles.TRAIN_ARFF);
			this.testData = UtilsFiles.loadArff(UtilsFiles.TEST_ARFF);

			// Set number of attributes if classic mode
			if (mode.equals(ClassificationMode.CLASSIC)) {
				this.trainData.setClassIndex(trainData.numAttributes() - 1);

				this.testData.setClassIndex(testData.numAttributes() - 1);
			}

		} else {
			Instances rawData = UtilsFiles.loadTextDirectory(UtilsFiles.TRAIN_DATA);

			if (mode.equals(ClassificationMode.CLASSIC)) {
				rawData.setClassIndex(rawData.numAttributes() - 1);
			}

			System.out.println("Filtering data...");

			// StringToWordVector filtering
			rawData = filterStringToWordVector(rawData);

			// AtributeSelection filtering
			rawData = filterAttributeSelection(rawData);

			// RemoveByName filtering
			rawData = filterRemoveByName(rawData);

			// Split data
			splitTrainTest(3, rawData);

			System.out.println("done with instances");

			// Save in arff files
			UtilsFiles.saveArff(trainData, UtilsFiles.TRAIN_ARFF);
			UtilsFiles.saveArff(testData, UtilsFiles.TEST_ARFF);
		}

	}

	// StringToWord filtering
	private Instances filterStringToWordVector(Instances ins) {
		// Filter initialization
		StringToWordVector filter = new StringToWordVector(NUM_OF_WORDS);
		try {
			// Tokenization
			NGramTokenizer tokenizer = new NGramTokenizer();
			tokenizer.setNGramMinSize(1);
			tokenizer.setNGramMaxSize(3);
			tokenizer.setDelimiters("\\r\\t.,;:'\"()?!\\{\\}\\[\\]");

			filter.setTokenizer(tokenizer);

			// Filter options
			filter.setDoNotOperateOnPerClassBasis(true);
			filter.setIDFTransform(true);
			filter.setTFTransform(true);

			filter.setMinTermFreq(1);
			filter.setOutputWordCounts(true);

			// Stopwords
			if (new File(UtilsFiles.STOP_WORD_LIST).exists()) {
				WordsFromFile stopwords = new WordsFromFile();
				stopwords.setStopwords(new File(UtilsFiles.STOP_WORD_LIST));

				// filter.setStopwordsHandler(stopwords); // 3.6.xx or above (confirmed 3.8.x)
				// filter.setStopwords(new File("data/stopwords.txt")); //version 3.6.x or lower
			}

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

	private Instances filterAttributeSelection(Instances ins) {
		AttributeSelection filter = new AttributeSelection();

		CfsSubsetEval eval = new CfsSubsetEval();

		try {
			((OptionHandler) eval).setOptions(Utils.splitOptions("CFS_SUBSET_EVAL_CONFIG"));
		} catch (Exception e) {
			e.printStackTrace();
		}

		BestFirst search = new BestFirst();

		try {
			search.setOptions(Utils.splitOptions("-D 1 -N 5 -S 0"));
		} catch (Exception e) {
			e.printStackTrace();
		}

		filter.setEvaluator(eval);
		filter.setSearch(search);
		try {
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

	private void splitTrainTest(int folds, Instances data) {
		try {
			StratifiedRemoveFolds dataSplitterTrain = new StratifiedRemoveFolds();
			dataSplitterTrain.setNumFolds(folds);
			dataSplitterTrain.setInputFormat(data);
			dataSplitterTrain.setInvertSelection(true);
			trainData = Filter.useFilter(data, dataSplitterTrain);

			StratifiedRemoveFolds dataSplitterTest = new StratifiedRemoveFolds();
			dataSplitterTest.setNumFolds(folds);
			dataSplitterTest.setInputFormat(data);
			dataSplitterTest.setInvertSelection(false);
			testData = Filter.useFilter(data, dataSplitterTest);

			data.clear();

		} catch (Exception e) {
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
