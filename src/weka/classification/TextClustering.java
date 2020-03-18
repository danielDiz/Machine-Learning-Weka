package weka.classification;

import java.io.File;

import weka.clusterers.ClusterEvaluation;
import weka.clusterers.SimpleKMeans;

public class TextClustering {

	private SimpleKMeans model;
	
	private TextInstances instances;
	
	public TextClustering() {
		this.model = new SimpleKMeans(); 
		this.model.setPreserveInstancesOrder(true);
		this.model.setNumClusters(2);
		
		this.instances = new TextInstances();
	}
	
	
	
	
	
	public static void main(String[] args) throws Exception {
		final String MODEL = "data/modelWeka3.model";

		TextClustering wt = new TextClustering();
		wt.instances.filterData();
		
		
		
		
		wt.model.buildClusterer(wt.instances.getTrainData());
		
		
		
		ClusterEvaluation eval = new ClusterEvaluation();
		eval.setClusterer(wt.model);
		eval.evaluateClusterer(wt.instances.getTestData());
	
		System.out.println("done");
	}
}
