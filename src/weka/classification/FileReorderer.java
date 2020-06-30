package weka.classification;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;

public class FileReorderer {
	//Original data
	private static final String CATEGORIES_DATA = "data/categories";
	private static final String ORIGINAL_DATA = "data/train";
	
	//Reordered data
	private static final String REORDERED_DATA = "data/reordered";

	// Xml log-failure-reasons fields
	private static final String XML_EXAMPLE = "Example";
	private static final String XML_LOG = "Log";
	private static final String XML_KEYWORDS = "Keywords";
	private static final String XML_CATEGORY = "Category";
	private static final String XML_CHUNK = "Chunk";


	private static Map<Integer, List<XmlInstance>> categories;
	private static Map<String, File> files;
	private static int totalLogs;

	

	public static void main(String[] args) throws Exception {
		//Unzip all data in "data/train" directory
		uncompressData(ORIGINAL_DATA);

		totalLogs = 0;
		//Get logs data in a map, with Key = log file name
		files = getLogsData();
		
		//Unzip all data in "data/categories" directory
		uncompressData(CATEGORIES_DATA);
	
		//Get categories data from xml files
		categories = createCategories();
		
		//Reorder logs according to the category they are in the xml
		reorganizeLogs();

		System.out.println("Finished reordering files");
	}

	private static boolean isImportant(XmlInstance log) {
		int logsInCategory = categories.get(log.getCategory()).size();
		int NUM_OF_CATEGORIES = 9;

		if (logsInCategory >= ((totalLogs / NUM_OF_CATEGORIES) / 2.5)) {
			return true;
		} else {
			return !false;
		}
	}
	
	private static boolean isALog(File file) {
		String fileName = file.getName();
		if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0
				&& fileName.substring(fileName.lastIndexOf(".") + 1).equals("log")) {
			return true;
		} else {
			return false;
		}
	}

	private static void uncompressData(String path) {
		System.out.println("Uncompressing data from: " + path);
		new File(path).mkdir();

		try {
			ZipFile zip = new ZipFile(path + ".zip");
			zip.extractAll(path);
			
		} catch (ZipException e) {
			e.printStackTrace();
		}
	}
	
	private static Map<String, File> getLogsData() {
		File directory = new File(ORIGINAL_DATA);
		
		Map<String, File> logs = new HashMap<>();
		
		for (final File subdirectory : directory.listFiles()) {
			for (final File github : subdirectory.listFiles()) {
				if (github.isDirectory()) {
					for (final File failed : github.listFiles()) {
						if (failed.isDirectory()) {
							for (final File log : failed.listFiles()) {
								if (isALog(log)) {
									logs.put(log.getName(), log);
									totalLogs++;
								}
							}
						}
					}
				}
			}
		}
		
		return logs;
	}
	
	private static Map<Integer, List<XmlInstance>> createCategories() throws SAXException, IOException, ParserConfigurationException {
		File directoryCategories = new File(CATEGORIES_DATA);
		directoryCategories.mkdir();
		Map<Integer, List<XmlInstance>> xmlCategories = new HashMap<>();
		DocumentBuilder documentBuilder = null;
		documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		
		for (final File subdirectory : directoryCategories.listFiles()) {
			for (final File xml : subdirectory.listFiles()) {
				Document doc = documentBuilder.parse(xml);
				int n = doc.getElementsByTagName(XML_EXAMPLE).getLength();
				for (int i = 0; i < n; i++) {
					String log = doc.getElementsByTagName(XML_LOG).item(i).getTextContent().split("/")[3]; // Get only file name
					String keywords = doc.getElementsByTagName(XML_KEYWORDS).item(i).getTextContent();
					int category;
					try {
						category = Integer.parseInt(doc.getElementsByTagName(XML_CATEGORY).item(i).getTextContent());
					} catch (NumberFormatException e) {
						category = -1;
					}
					String chunk = doc.getElementsByTagName(XML_CHUNK).item(i).getTextContent();

					XmlInstance example = new XmlInstance(log, keywords, category, chunk);

					if (!xmlCategories.containsKey(category)) {
						xmlCategories.put(category, new ArrayList<XmlInstance>());
					}
					xmlCategories.get(category).add(example);
				}
			}
		}
		return xmlCategories;
	}
	
	private static void reorganizeLogs() throws IOException {
		new File(REORDERED_DATA).mkdir();
		
		for (List<XmlInstance> logReports : categories.values()) {
			int category = logReports.get(0).getCategory();
			File logsByCategories = new File(REORDERED_DATA + "/" + category + "_cat");
			logsByCategories.mkdir();
			for (XmlInstance xmlLog : logReports) {
				if (isImportant(xmlLog)) {
					//System.out.println(xmlLog.getCategory());
					File log = files.get(xmlLog.getLog());
					File copyLog = new File(REORDERED_DATA + "/" + category + "_cat" + "/" + log.getName());

					Files.copy(log.toPath(), copyLog.toPath(), StandardCopyOption.REPLACE_EXISTING);
					copyLog.createNewFile();
				}
			}
		}
	}

}
