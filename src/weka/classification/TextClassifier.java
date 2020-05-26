package weka.classification;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import weka.classification.TextInstances.ClassificationMode;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.SMO;
import weka.classifiers.functions.supportVector.Kernel;
import weka.classifiers.functions.supportVector.RBFKernel;
import weka.classifiers.meta.AdaBoostM1;
import weka.classifiers.meta.Bagging;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.meta.Stacking;
import weka.classifiers.meta.Vote;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;
import weka.classifiers.trees.RandomTree;
import weka.core.Attribute;
import weka.core.AttributeStats;
import weka.core.Instances;
import weka.core.converters.ArffLoader.ArffReader;
import weka.core.converters.ArffSaver;
import weka.core.converters.TextDirectoryLoader;
import weka.core.tokenizers.NGramTokenizer;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

public class TextClassifier {

	private FilteredClassifier classifier;
	
	private TextInstances instances;
	
	private AdaBoostM1 m1;
	
	private Bagging bagger;
	
	private Stacking stacker;
	
	private Vote voter;

	public TextClassifier() {
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
		
		this.instances = new TextInstances(ClassificationMode.CLASSIC);
		
		this.m1 = new AdaBoostM1();
		this.bagger = new Bagging();
		this.stacker = new Stacking();
		this.voter = new Vote();
	}

	
	public void transform() {
			classifier.setFilter(instances.getFilterTrain());	
	}

	
	public void fit() {
//		try {
//			classifier.buildClassifier(this.instances.getTrainData());
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		
		/* Boosting a weak classifier using the Adaboost M1 method
		 * for boosting a nominal class classifier
		 * Tackles only nominal class problems
		 * Improves performance
		 * Sometimes overfits.
		 */
		//AdaBoost
		System.out.println("Boosting");
		m1.setClassifier(classifier);
		m1.setNumIterations(20);
		try {
			m1.buildClassifier(instances.getTrainData());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		/* Bagging a classifier to reduce variance.
		 * Can do classification and regression (depending on the base model)
		 */
		//Bagging
		System.out.println("Bagging");
		classifier.setClassifier(new RandomTree());
		bagger.setClassifier(classifier);//needs one base-model
		bagger.setNumIterations(25);
		try {
			bagger.buildClassifier(instances.getTrainData());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
		/*
		 * The Stacking method combines several models
		 * Can do classification or regression. 
		 */
		//Stacking
		System.out.println("Stacking");
		classifier.setClassifier(new J48());
		stacker.setMetaClassifier(classifier);//needs one meta-model
		Classifier[] classifiers = {				
				new J48(),
				new NaiveBayes(),
				new RandomForest()
		};
		
		stacker.setClassifiers(classifiers);//needs one or more models
		try {
			stacker.buildClassifier(instances.getTrainData());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		/*
		 * Class for combining classifiers.
		 * Different combinations of probability estimates for classification are available. 
		 */
		//Vote .. 
		System.out.println("Voting");
		voter.setClassifiers(classifiers);//needs one or more classifiers
		try {
			voter.buildClassifier(instances.getTrainData());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	public String evaluate() {
		System.out.println("Evaluation model...");
			
		//evaluation
		try {
			Evaluation eval = new Evaluation(instances.getTrainData());
			eval.evaluateModel(voter, instances.getTestData());
			
			//Results
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
			classifier = (FilteredClassifier) tmp;
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
			wt.transform();
			wt.fit(); 
		} else { 
			wt.transform();
			wt.fit(); 
			wt.saveModel(MODEL); 
		}

		// run evaluation
		System.out.println("Evaluation Result: \n" + wt.evaluate());
		
	}

}
