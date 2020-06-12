package weka.classification;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLInputFactory;

import org.w3c.dom.Document;

import weka.classification.TextInstances.ClassificationMode;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.tokenizers.NGramTokenizer;
import weka.core.tokenizers.WordTokenizer;
import weka.filters.Filter;
import weka.filters.MultiFilter;
import weka.filters.unsupervised.attribute.StringToNominal;
import weka.filters.unsupervised.attribute.StringToWordVector;

public class FileReorderer {

	private static final String STOPWORDS_ARFF = "data/trainFiltered.arff";

	private static TextInstances instances;

	private static final String ORIGINAL_DATA = "data/train/";
	private static final String UPDATED_DATA = "data/trainUpdated/";

	private static final String CATEGORIES_DATA = "data/trainCategories/";
	private static final String LOG_CATEGORIZED_DATA = "data/trainCategorized/";

	// Xml log-failure-reasons fields
	private static final String XML_EXAMPLE = "Example";
	private static final String XML_LOG = "Log";
	private static final String XML_KEYWORDS = "Keywords";
	private static final String XML_CATEGORY = "Category";
	private static final String XML_CHUNK = "Chunk";

	// Travis license
	private static final String TRAVIS_LICENSE = "";

	public static void main(String[] args) throws Exception {
		// Create a copy of all logs
		File directory = new File(ORIGINAL_DATA);
		File updatedDirectory = new File(UPDATED_DATA);
		updatedDirectory.mkdir();
		Map<String, File> files = new HashMap<>();

		for (final File subdirectory : directory.listFiles()) {
			// File updatedSubdirectory = new File(UPDATED_DATA + subdirectory.getName());
			// updatedSubdirectory.mkdir();
			for (final File log : subdirectory.listFiles()) {
				// File updatedLog = new File(updatedSubdirectory.toPath() + "/" +
				// log.getName());
				// File updatedLog = new File(UPDATED_DATA + log.getName());
				// modify each file to delete certain characters

				// Files.copy(log.toPath(), updatedLog.toPath(),
				// StandardCopyOption.REPLACE_EXISTING);
				// updatedLog.createNewFile();
				// modifyFile(updatedLog);
				files.put(log.getName(), log);
			}
		}

		File directoryCategories = new File(CATEGORIES_DATA);
		Map<Integer, List<XmlInstance>> categories = new HashMap<>();
		DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		for (final File subdirectory : directoryCategories.listFiles()) {
			for (final File xml : subdirectory.listFiles()) {
				Document doc = documentBuilder.parse(xml);
				int n = doc.getElementsByTagName(XML_EXAMPLE).getLength();
				for (int i = 0; i < n; i++) {
					String log = doc.getElementsByTagName(XML_LOG).item(i).getTextContent().split("/")[3]; // Get only
																											// file name
					String keywords = doc.getElementsByTagName(XML_KEYWORDS).item(i).getTextContent();
					int category;
					try {
						category = Integer.parseInt(doc.getElementsByTagName(XML_CATEGORY).item(i).getTextContent());
					} catch (NumberFormatException e) {
						category = -1;
					}
					String chunk = doc.getElementsByTagName(XML_CHUNK).item(i).getTextContent();

					// System.out.println(log + " " + keywords);
					XmlInstance example = new XmlInstance(log, keywords, category, chunk);

					if (!categories.containsKey(category)) {
						categories.put(category, new ArrayList<XmlInstance>());
					}
					categories.get(category).add(example);
				}
			}
		}

		for (List<XmlInstance> logReports : categories.values()) {
			int category = logReports.get(0).getCategory();
			File logsByCategories = new File(LOG_CATEGORIZED_DATA + category);
			logsByCategories.mkdir();
			// System.out.println(categoryName);
			// System.out.println("Category: " + logReports.get(0).getCategory());
			for (XmlInstance xmlLog : logReports) {
				// System.out.println(xmlLog.getKeywords());
				File log = files.get(xmlLog.getLog());
				File copyLog = new File(LOG_CATEGORIZED_DATA + category + "/" + log.getName());

				Files.copy(log.toPath(), copyLog.toPath(), StandardCopyOption.REPLACE_EXISTING);
				copyLog.createNewFile();
				// System.out.println(log.getAbsolutePath());
			}
			System.out.println();
			System.out.println();
			System.out.println();
			System.out.println();
			System.out.println();
		}

		System.out.println("Done");
		System.exit(0);
	}

	private static void modifyFile(File log) {
		// TODO Auto-generated method stub

	}

}

//		
//		Instances ins;
//		// MultiFilter filter = filterBuilderMulti();
//
//		if (new File(STOPWORDS_ARFF).exists()) {
//			ins = TextInstances.loadArff(STOPWORDS_ARFF);
//		} else {
//			ins = TextInstances.loadTextDirectory("data/train2/");
//		}
//
//		StringToWordVector filter = new StringToWordVector(1000);
//		filter.setOutputWordCounts(true);
//		filter.setDoNotOperateOnPerClassBasis(true);
//		WordTokenizer tokenizer = new WordTokenizer();
//		tokenizer.setDelimiters(" \\n 	\\[\\].,;'\\\"()?!-/<>‘’“”…«»•&{[|`^]}$*%");
//		filter.setTokenizer(tokenizer);
//
//		try {
//			filter.setInputFormat(ins);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		try {
//			ins = Filter.useFilter(ins, filter);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		// TextInstances.saveArff(ins, STOPWORDS_ARFF);
//		List<Attribute> attributes = new ArrayList<>();
//
//		int n = ins.numAttributes();
//		for (int i = 0; i < n; i++) {
//			System.out.println(ins.attribute(i).toString());
//		}
//		System.out.println("Done");
//
//	}
//
//	public static MultiFilter filterBuilderMulti() {
//		MultiFilter filter = new MultiFilter();
//		Filter[] filters = new Filter[1];
//
//		try {
//			// StringToWordVector filter
//
//			StringToWordVector f1 = new StringToWordVector(150);
//
//			// Tokenization
//			NGramTokenizer tokenizer = new NGramTokenizer();
//			tokenizer.setDelimiters(" \\n 	\\[\\].,;'\\\"()?!-/<>‘’“”…«»•&{[|`^]}$*%");
//			f1.setTokenizer(tokenizer);
//
//			// Filter options
//			f1.setDoNotOperateOnPerClassBasis(true);
//			f1.setOutputWordCounts(true);
//			f1.setMinTermFreq(200);
//			f1.setLowerCaseTokens(true);
//			f1.setDictionaryFileToSaveTo(new File("data/dictionary.txt"));
//
//			StringToNominal f2 = new StringToNominal();
//
//			filters[0] = f2;
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//		filter.setFilters(filters);
//
//		return filter;
//	}
