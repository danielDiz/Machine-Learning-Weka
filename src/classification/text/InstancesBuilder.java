package classification.text;

import java.io.File;

import classification.sorter.FileReorderer;
import classification.util.UtilsFiles;
import weka.attributeSelection.BestFirst;
import weka.attributeSelection.CfsSubsetEval;
import weka.core.Instances;
import weka.core.OptionHandler;
import weka.core.Utils;
import weka.core.tokenizers.NGramTokenizer;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.supervised.instance.StratifiedRemoveFolds;
import weka.filters.unsupervised.attribute.RemoveByName;
import weka.filters.unsupervised.attribute.StringToWordVector;

public class InstancesBuilder {
	// declare train and test data Instances
	private Instances trainData;
	private Instances testData;

	// declare and initialize file locations

	private final int NUM_OF_WORDS = 400;

	public InstancesBuilder() {
		if (!new File(UtilsFiles.REORDERED_DATA).exists()) {
			// Call file reorderer if the data is not sorted
			try {
				FileReorderer.main(null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (new File(UtilsFiles.TRAIN_ARFF).exists() && new File(UtilsFiles.TEST_ARFF).exists()) {
			this.trainData = UtilsFiles.loadArff(UtilsFiles.TRAIN_ARFF);
			this.testData = UtilsFiles.loadArff(UtilsFiles.TEST_ARFF);

			// Set number of attributes {
			this.trainData.setClassIndex(trainData.numAttributes() - 1);
			this.testData.setClassIndex(testData.numAttributes() - 1);

		} else {
			Instances rawData = UtilsFiles.loadTextDirectory(UtilsFiles.REORDERED_DATA);

			rawData.setClassIndex(rawData.numAttributes() - 1);

			System.out.println("Filtering data...");

			// StringToWordVector filtering
			rawData = filterStringToWordVector(rawData);

			// AtributeSelection filtering
			rawData = filterAttributeSelection(rawData);

			// RemoveByName filtering
			rawData = filterRemoveByName(rawData);

			// Split data
			splitTrainTest(3, rawData);

			System.out.println("Instances created");

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
		filter.setExpression("\\p{L}+"); // only utf-8 chars
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
		BestFirst search = new BestFirst();

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
		final String relationName = "logs_classification";
		try {
			StratifiedRemoveFolds dataSplitterTrain = new StratifiedRemoveFolds();
			dataSplitterTrain.setNumFolds(folds);
			dataSplitterTrain.setInputFormat(data);
			dataSplitterTrain.setInvertSelection(true);
			trainData = Filter.useFilter(data, dataSplitterTrain);
			trainData.setRelationName(relationName + "_train_data");

			StratifiedRemoveFolds dataSplitterTest = new StratifiedRemoveFolds();
			dataSplitterTest.setNumFolds(folds);
			dataSplitterTest.setInputFormat(data);
			dataSplitterTest.setInvertSelection(false);
			testData = Filter.useFilter(data, dataSplitterTest);
			testData.setRelationName(relationName + "_test_data");
			
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
