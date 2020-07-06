package weka.classification;

import java.io.File;

import weka.classification.TextInstances.ClassificationMode;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.LibSVM;
import weka.classifiers.lazy.IBk;
import weka.classifiers.meta.AdaBoostM1;
import weka.classifiers.meta.Bagging;
import weka.classifiers.meta.Stacking;
import weka.classifiers.meta.Vote;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomTree;
import weka.core.SelectedTag;
import weka.core.Tag;
import weka.core.neighboursearch.LinearNNSearch;

public class TextClassifier {

	private IBk classifier;

	private TextInstances instances;

	private AdaBoostM1 m1;

	private Bagging bagger;

	private Stacking stacker;

	private static Vote voter;

	private Evaluation eval;

	public TextClassifier() {
		// Clasification Model
		this.classifier = new IBk();
		classifier.setKNN(33);
		classifier.setCrossValidate(true);

		Tag[] tags = { new Tag(1, "WEIGHT_INVERSE")};
		SelectedTag initial = new SelectedTag(1, tags);

		classifier.setDistanceWeighting(initial);
		classifier.setNearestNeighbourSearchAlgorithm(new LinearNNSearch());

		this.instances = new TextInstances(ClassificationMode.CLASSIC);

		this.m1 = new AdaBoostM1();
		this.bagger = new Bagging();
		this.stacker = new Stacking();
		TextClassifier.voter = new Vote();

		try {
			this.eval = new Evaluation(this.instances.getTrainData());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void classify() {
		long startTime = System.currentTimeMillis();
		try {
			classifier.buildClassifier(this.instances.getTrainData());
		} catch (Exception e) {
			e.printStackTrace();
		}

		boosting();

		bagging();

		Classifier[] classifiers = { new J48(), new NaiveBayes(), new LibSVM(), new RandomTree(), new AdaBoostM1(), classifier};

		stacking(classifiers);

		voting(classifiers);

		long endTime = System.currentTimeMillis();
		System.out.println("Time taken: " + (endTime - startTime) + "ms");
	}

	private void boosting() {
		/*
		 * Boosting a weak classifier using the Adaboost M1 method for boosting a
		 * nominal class classifier Tackles only nominal class problems Improves
		 * performance Sometimes overfits.
		 */
		// AdaBoost
		System.out.println("Boosting...");
		m1.setClassifier(classifier);
		m1.setNumIterations(20);
		try {
			m1.buildClassifier(instances.getTrainData());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void bagging() {
		/*
		 * Bagging a classifier to reduce variance. Can do classification and regression
		 * (depending on the base model)
		 */
		// Bagging
		System.out.println("Bagging...");
		bagger.setClassifier(m1);// needs one base-model
		bagger.setNumIterations(10);
		try {
			bagger.buildClassifier(instances.getTrainData());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void stacking(Classifier[] classifiers) {
		/*
		 * The Stacking method combines several models Can do classification or
		 * regression.
		 */
		// Stacking
		System.out.println("Stacking...");
		stacker.setMetaClassifier(classifier);// needs one meta-model

		stacker.setClassifiers(classifiers);// needs one or more models

		try {
			stacker.buildClassifier(instances.getTrainData());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void voting(Classifier[] classifiers) {
		/*
		 * Class for combining classifiers. Different combinations of probability
		 * estimates for classification are available.
		 */
		// Vote ..
		System.out.println("Voting");
		voter.setClassifiers(classifiers);// needs one or more classifiers
		
		try {
			voter.buildClassifier(instances.getTrainData());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public String evaluate() {
		System.out.println("Evaluation model...");

		// evaluation
		try {
			//Evaluation eval = new Evaluation(instances.getTrainData());
			eval.evaluateModel(voter, instances.getTestData());

			String results = (eval.toSummaryString() + "\n" + eval.toClassDetailsString());

			return results;
		} catch (Exception e) {
			e.printStackTrace();
			return "Error during evaluation";

		}
	}
	

	/**
	 * Main method. With an example usage of this class.
	 */
	public static void main(String[] args) throws Exception {

		TextClassifier wt = new TextClassifier();

		if (new File(UtilsFiles.MODEL).exists()) {
			voter = (Vote) UtilsFiles.loadModel(UtilsFiles.MODEL);
			wt.classify();
		} else {
			wt.classify();
			UtilsFiles.saveModel(voter, UtilsFiles.MODEL);
		}

		// run evaluation
		System.out.println("Evaluation Result: \n" + wt.evaluate());

	}

}
