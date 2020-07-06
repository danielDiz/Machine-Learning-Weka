package weka.classification;

import java.nio.file.Files;
import java.nio.file.Paths;

import weka.classification.TextInstances.ClassificationMode;
import weka.clusterers.ClusterEvaluation;
import weka.clusterers.SimpleKMeans;
import weka.core.DistanceFunction;
import weka.core.EuclideanDistance;

public class TextClustering {

	private SimpleKMeans model;

	private static TextInstances instances;

	public TextClustering() {
		instances = new TextInstances(ClassificationMode.CLUSTER);
		
		this.model = new SimpleKMeans();
		try {
			model.setNumClusters(22);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		DistanceFunction eu = new EuclideanDistance();
		eu.setInstances(instances.getTrainData());
		try {
			model.setDistanceFunction(eu);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception {

		TextClustering wt = new TextClustering();
		
		wt.model.buildClusterer(instances.getTrainData());

		ClusterEvaluation eval = new ClusterEvaluation();
		eval.setClusterer(wt.model);

		eval.evaluateClusterer(instances.getTestData());
		
		UtilsFiles.saveModel(wt.model, UtilsFiles.MODEL_CLUSTER);

		Files.write(Paths.get("/data/out.txt"), eval.clusterResultsToString().getBytes());
		System.out.println("done");
	}
}
