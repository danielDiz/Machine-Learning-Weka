package wekaTest1;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Random;

import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.Instances;

public class TestMain1 {
	
	public static void main(String[] args) throws Exception{

		System.out.println("Empezando");
        Instances instancias = new Instances(new BufferedReader(new FileReader("C:/Users/danie/eclipse-workspace/wekaTest1/train.arff")));
        instancias.setClassIndex(instancias.numAttributes() - 1);
        
        double precision = 0;
        double recall = 0;
        double fmeasure = 0;
        double error = 0;
        
        int size = instancias.numInstances() / 10;
        int begin = 0;
        int end = size - 1;
        
        for (int i = 1; i <= 10; i++) {
        	System.out.println("iteracion " + i);
        	Instances training = new Instances(instancias);
        	Instances testing = new Instances(instancias, begin, (end - begin));
        	for (int j = 0; j < (end-begin); j++) {
        		training.delete(begin);
        	}
        	
        	NaiveBayes tree = new NaiveBayes();
        	
        	tree.buildClassifier(training);
        	
        	Evaluation eval = new Evaluation(testing);
        	eval.evaluateModel(tree, testing);
        	
        	System.out.println("P:" + eval.precision(1));
        	System.out.println("R:" + eval.recall(1));
        	System.out.println("F:" + eval.fMeasure(1));
        	System.out.println("E:" + eval.errorRate());
        	
        	precision += eval.precision(1);
        	recall += eval.recall(1);
        	fmeasure += eval.fMeasure(1);
        	error += eval.errorRate();
        	
        	begin = end + 1;
        	end += size;
        	if (i == 9) {
        		end = instancias.numInstances()
;        	}
        }
        System.out.println();
        System.out.println("Precision:" + precision/10.0);
    	System.out.println("Recall:" + recall/10.0);
    	System.out.println("Fmeasure:" + fmeasure/10.0);
    	System.out.println("Error:" + error/10.0);
        
        
	}
        
}
