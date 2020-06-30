package weka.classification;

import weka.classification.TextInstances.ClassificationMode;
import weka.clusterers.ClusterEvaluation;
import weka.clusterers.XMeans;
import weka.core.DistanceFunction;
import weka.core.EuclideanDistance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.NumericToNominal;

public class TextClustering {

	private XMeans model;

	private static TextInstances instances;

	public TextClustering() {
		instances = new TextInstances(ClassificationMode.CLUSTER);
		
		try {
			this.model = new XMeans();
			this.model.setMaxNumClusters(100);
			this.model.setMinNumClusters(8);
			
			DistanceFunction eu = new EuclideanDistance();
			eu.setInstances(instances.getTrainData());
			model.setDistanceF(eu);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}

	public static void main(String[] args) throws Exception {
		final String MODEL = "data/modelWeka3.model";

		TextClustering wt = new TextClustering();

		for(int i = 0; i < instances.getTrainData().numAttributes(); i++) {
			//System.out.println(instances.getTrainData().attribute(i));
		}
		Instances newTrain = filterNumericToNominal(instances.getTrainData());
		Instances newTest = filterNumericToNominal(instances.getTestData());
		
		
		wt.model.buildClusterer(newTrain);

		ClusterEvaluation eval = new ClusterEvaluation();
		eval.setClusterer(wt.model);

		eval.evaluateClusterer(newTest);

		System.out.println(eval.clusterResultsToString());
		System.out.println("done");
	}
	
	private static Instances filterNumericToNominal(Instances ins) {
		NumericToNominal filter = new NumericToNominal();
		try {
			filter.setInputFormat(ins);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		int[] range = {1, 10};
		filter.setAttributeIndicesArray(range);
		try {
			return Filter.useFilter(ins, filter);
		} catch (Exception e) {
			e.printStackTrace();
			return ins;
		}
	}
}
