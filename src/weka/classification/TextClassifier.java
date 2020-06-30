package weka.classification;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import weka.classification.TextInstances.ClassificationMode;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.GaussianProcesses;
import weka.classifiers.functions.LibSVM;
import weka.classifiers.lazy.IBk;
import weka.classifiers.lazy.KStar;
import weka.classifiers.meta.AdaBoostM1;
import weka.classifiers.meta.Bagging;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.meta.Stacking;
import weka.classifiers.meta.Vote;
import weka.classifiers.misc.InputMappedClassifier;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomTree;
import weka.core.Instance;
import weka.core.SelectedTag;
import weka.core.Tag;
import weka.core.Utils;
import weka.core.neighboursearch.LinearNNSearch;
import weka.core.neighboursearch.NearestNeighbourSearch;

public class TextClassifier {

	private IBk classifier;

	private TextInstances instances;

	private AdaBoostM1 m1;

	private Bagging bagger;

	private Stacking stacker;

	private Vote voter;

	public TextClassifier() {
		// Clasification Model
		this.classifier = new IBk();
		classifier.setKNN(33);
		classifier.setCrossValidate(true);
		
		Tag [] tags =  {
			new Tag(1, "First option"), 
			new Tag(2, "Second option"), 
			new Tag(3, "Third option"),
			new Tag(4, "Fourth option"), 
			new Tag(5, "Fifth option"), 
		};
		SelectedTag initial = new SelectedTag(2, tags);
		
		classifier.setDistanceWeighting(initial);
		classifier.setNearestNeighbourSearchAlgorithm(new LinearNNSearch());

		this.instances = new TextInstances(ClassificationMode.CLASSIC);

//		for (int j = 0; j < this.instances.getTrainData().numInstances(); j++) {
//			System.out.println("next instance");
//			for (int i = 0; i < this.instances.getTrainData().numAttributes(); i++) {
//				System.out.println(this.instances.getTrainData().get(j).attribute(i));
//			}
//		}

		

		this.m1 = new AdaBoostM1();
		this.bagger = new Bagging();
		this.stacker = new Stacking();
		this.voter = new Vote();
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

		Classifier[] classifiers = { new J48(), new NaiveBayes(), new LibSVM(), new RandomTree(), new AdaBoostM1() };

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
			// TODO Auto-generated catch block
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String evaluate() {
		System.out.println("Evaluation model...");

		// evaluation
		try {
			InputMappedClassifier mappedCls = new InputMappedClassifier();
			mappedCls.setClassifier(voter);
			mappedCls.setSuppressMappingReport(true);
			mappedCls.buildClassifier(instances.getTrainData());

			Evaluation eval = new Evaluation(instances.getTrainData());
			eval.evaluateModel(mappedCls, instances.getTestData());
			// eval.crossValidateModel(voter, instances.getTrainData(), 10, new
			// Random(254));

			// Results
//			for(int i = 0; i < instances.getTestData().numInstances(); i++) {
//				double result = eval.evaluateModelOnce(mappedCls, instances.getTestData().get(i));
//				System.out.println(result);
//			}

			return (eval.toSummaryString() + "\n" + eval.toClassDetailsString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "Error during evaluation";

		}
	}

	public void loadModel(String fileName) {
		System.out.println("Loading model...");
		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(fileName));
			Object tmp = in.readObject();
			classifier = (IBk) tmp;
			in.close();
			System.out.println("Loaded model: " + fileName);
		} catch (IOException | ClassNotFoundException e) {
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

	/**
	 * Main method. With an example usage of this class.
	 */
	public static void main(String[] args) throws Exception {
		final String MODEL = "data/modelWeka2.model";

		TextClassifier wt = new TextClassifier();

		if (new File(MODEL).exists()) {
			wt.loadModel(MODEL);
			wt.classify();
		} else {
			wt.classify();
			// wt.saveModel(MODEL);
		}

		// run evaluation
		System.out.println("Evaluation Result: \n" + wt.evaluate());

	}

}
