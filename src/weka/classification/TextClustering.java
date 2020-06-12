package weka.classification;

import java.util.Random;

import weka.classification.TextInstances.ClassificationMode;
<<<<<<< HEAD
import weka.clusterers.*;

public class TextClustering {

	private XMeans model;
=======
import weka.clusterers.ClusterEvaluation;
import weka.clusterers.SimpleKMeans;

public class TextClustering {

	private SimpleKMeans model;
>>>>>>> f34e24b2bd6fa3d69f4469c0df65824339de8cac
	
	private TextInstances instances;
	
	public TextClustering() {
		try {
<<<<<<< HEAD
			this.model = new XMeans();
			this.model.setMaxNumClusters(100);
			this.model.setMinNumClusters(8);
=======
			this.model = new SimpleKMeans(); 
			this.model.setPreserveInstancesOrder(true);
			this.model.setNumClusters(2);
>>>>>>> f34e24b2bd6fa3d69f4469c0df65824339de8cac
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		this.instances = new TextInstances(ClassificationMode.CLUSTER);
	}
	
	public static void main(String[] args) throws Exception {
		final String MODEL = "data/modelWeka3.model";

		TextClustering wt = new TextClustering();
<<<<<<< HEAD
		//wt.instances.filterData();
=======
		wt.instances.filterData();
>>>>>>> f34e24b2bd6fa3d69f4469c0df65824339de8cac
		
		wt.model.buildClusterer(wt.instances.getTrainData());
		
		ClusterEvaluation eval = new ClusterEvaluation();
		eval.setClusterer(wt.model);
<<<<<<< HEAD
		//eval.evaluateClusterer(wt.instances.getTestData());
=======
		eval.evaluateClusterer(wt.instances.getTestData());
>>>>>>> f34e24b2bd6fa3d69f4469c0df65824339de8cac
		
		System.out.println(eval.clusterResultsToString());
		System.out.println("done");
	}
}
