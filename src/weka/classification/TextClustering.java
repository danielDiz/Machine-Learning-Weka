package weka.classification;

import java.util.Random;

import weka.classification.TextInstances.ClassificationMode;
import weka.clusterers.*;

public class TextClustering {

	private XMeans model;
	
	private TextInstances instances;
	
	public TextClustering() {
		try {
			this.model = new XMeans();
			this.model.setMaxNumClusters(100);
			this.model.setMinNumClusters(8);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		this.instances = new TextInstances(ClassificationMode.CLUSTER);
	}
	
	public static void main(String[] args) throws Exception {
		final String MODEL = "data/modelWeka3.model";

		TextClustering wt = new TextClustering();
		//wt.instances.filterData();
		
		wt.model.buildClusterer(wt.instances.getTrainData());
		
		ClusterEvaluation eval = new ClusterEvaluation();
		eval.setClusterer(wt.model);
		//eval.evaluateClusterer(wt.instances.getTestData());
		
		System.out.println(eval.clusterResultsToString());
		System.out.println("done");
	}
}
