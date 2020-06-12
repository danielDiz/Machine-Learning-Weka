package weka.classification;

import java.util.List;

public class XmlInstance {
	private String log;
	private String keywords;
	private int category;
	private String chunk;

	public XmlInstance(String log, String keywords, int category, String chunk) {
		this.log = log;
		this.keywords = keywords;
		this.category = category;
		this.chunk = chunk;
	}

	public String getLog() {
		return log;
	}

	public void setLog(String log) {
		this.log = log;
	}

	public String getKeywords() {
		return keywords;
	}

	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	public int getCategory() {
		return category;
	}

	public void setCategory(int category) {
		this.category = category;
	}

	public String getChunk() {
		return chunk;
	}

	public void setChunk(String chunk) {
		this.chunk = chunk;
	}

}
