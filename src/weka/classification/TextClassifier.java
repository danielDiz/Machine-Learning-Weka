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

public class TextClassifier {

	private FilteredClassifier classifier;
	
	private TextInstances instances;

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
		
		this.instances = new TextInstances();
	}

	
	public void transform() {
			classifier.setFilter(this.instances.getFilterTrain());	
	}

	

	
	public void fit() {
		try {
			classifier.buildClassifier(this.instances.getTrainData());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	public String evaluate() {
		System.out.println("Evaluation model...");
		
		
			
		//evaluation
		try {
			Evaluation eval = new Evaluation(this.instances.getTrainData());
			eval.evaluateModel(classifier, this.instances.getTestData());
			
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
