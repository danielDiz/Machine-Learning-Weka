package weka.classification;

public class LogInstance {
	private String log;
	private String category;

	public LogInstance(String log, String cat) {
		this.log = log;
		this.category = cat;
	}

	public String getLog() {
		return log;
	}

	public void setLog(String log) {
		this.log = log;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}
}
