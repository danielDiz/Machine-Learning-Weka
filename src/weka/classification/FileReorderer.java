package weka.classification;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.*;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;

public class FileReorderer {
	// Original data
	private static final String CATEGORIES_DATA = "data/categories";
	private static final String ORIGINAL_DATA = "data/train";
	private static final String COMPRESSED_DATA = "data/compressed_data";
	// Reordered data
	private static final String REORDERED_DATA = "data/reordered";

	private static Map<String, List<LogInstance>> categories;
	private static Map<String, Integer> numLogsCategory;
	private static Map<String, File> files;

	public static void main(String[] args) throws Exception {
		categories = new HashMap<>();
		files = new HashMap<>();
		numLogsCategory = new HashMap<>();

		if (!new File(ORIGINAL_DATA + ".zip").exists()) {
			try {
				mergeSplitFiles("data/train_splitted");
			} catch (Exception e) {
				e.printStackTrace();
				try {
					makeSplitFile(ORIGINAL_DATA);
					mergeSplitFiles("data/train_splitted");
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}

		if (!new File(ORIGINAL_DATA).exists() && !new File(ORIGINAL_DATA).isDirectory()) {
			uncompressData(ORIGINAL_DATA);
		}

		// Get logs data in a map, with Key = log file name
		getLogsData();

		// Unzip all data in "data/categories" directory
		if (!new File(CATEGORIES_DATA).exists() && !new File(CATEGORIES_DATA).isDirectory()) {
			uncompressData(CATEGORIES_DATA);
		}

		// Get categories data from xml files
		createCategories();

		// Reorder logs according to the category they are in the xml
		reorganizeLogs();

		System.out.println("Finished reordering files");
	}

	private static boolean isFileType(File file, String fileExtension) {
		String fileName = file.getName();
		if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0
				&& fileName.substring(fileName.lastIndexOf(".") + 1).equals(fileExtension)) {
			return true;
		} else {
			return false;
		}
	}

	private static void mergeSplitFiles(String path) throws ZipException {
		System.out.println("Merging data from: " + path);
		File directory = new File(path);
		new File(COMPRESSED_DATA).mkdir();
		for (final File log : directory.listFiles()) {
			System.out.println(path + "/" + log.getName() + "/train.zip");
			ZipFile zip = new ZipFile(new File(path + "/" + log.getName() + "/train.zip"));

			String destination = COMPRESSED_DATA + "/" + log.getName() + ".zip";
			if (log.listFiles().length > 1) {
				zip.mergeSplitFiles(new File(destination));
			} else {
				new ZipFile(destination).addFile(path + "/" + log.getName() + "/train.zip");;
			}
		}

	}

	private static void makeSplitFile(String path) {
		System.out.println("Making split zip from: " + path);
		File directory = new File(path);
		new File("data/train_splitted/").mkdir();
		for (final File subdirectory : directory.listFiles()) {
			List<File> logs = new ArrayList<>();
			File splitted_dir = new File("data/train_splitted/" + subdirectory.getName());
			if (!splitted_dir.exists()) {
				splitted_dir.mkdir();
				for (final File log : subdirectory.listFiles()) {
					logs.add(log);
				}

				ZipFile zip = new ZipFile("data/train_splitted/" + subdirectory.getName() + "/train.zip");
				try {
					zip.createSplitZipFile(logs, new ZipParameters(), true, 73400320); // 70MB
				} catch (ZipException e) {
					e.printStackTrace();
				}
			}
		}

	}

	private static void uncompressData(String path) {
		System.out.println("Uncompressing data from: " + path);
		// new File(path).mkdir();

		try {
			ZipFile zip = new ZipFile(path + ".zip");
			zip.extractAll(path);

		} catch (ZipException e) {
			e.printStackTrace();
		}
	}

	private static void getLogsData() {
		File directory = new File(ORIGINAL_DATA);
		for (final File subdirectory : directory.listFiles()) {
			for (final File log : subdirectory.listFiles()) {
				if (isFileType(log, "txt")) {
					files.put(log.getName(), log);
				}

			}
		}
	}

	private static void createCategories() {
		File directoryCategories = new File(CATEGORIES_DATA);
		directoryCategories.mkdir();

		for (final File json : directoryCategories.listFiles()) {
			JSONTokener parser = null;
			try {
				parser = new JSONTokener(new FileReader(json.getPath()));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

			JSONObject categories = new JSONObject(parser);

			Iterator<String> keys = categories.keys();
			while (keys.hasNext()) {
				String key = keys.next();
				JSONObject category = (JSONObject) categories.get(key);
				parseCategory(key, category);
			}

		}
	}

	private static void parseCategory(String key, JSONObject category) {
		try {
			JSONArray logs = (JSONArray) category.get("logs");
			int n = logs.length();
			for (int i = 0; i < n; i++) {
				if (!categories.containsKey(key)) {
					categories.put(key, new ArrayList<LogInstance>());
					numLogsCategory.put(key, 0);
				}
				int num = numLogsCategory.get(key);
				if (num < 70) {
					LogInstance log = new LogInstance(logs.getString(i) + ".txt", key);
					categories.get(key).add(log);
					num++;
					numLogsCategory.put(key, num);
				}

			}
		} catch (Exception e) {
			System.out.println("Category " + key + " doesnt have logs");
		}

	}

	private static void reorganizeLogs() throws IOException {
		new File(REORDERED_DATA).mkdir();

		for (List<LogInstance> logReports : categories.values()) {
			String category = logReports.get(0).getCategory();
			category = category.replace("category.", "");
			File logsByCategories = new File(REORDERED_DATA + "/" + category);
			logsByCategories.mkdir();
			for (LogInstance log : logReports) {
				String logName = log.getLog();
				File logFile;
				if ((logFile = files.get(logName)) != null) {
					File copyLog = new File(REORDERED_DATA + "/" + category + "/" + log.getLog());

					Files.copy(logFile.toPath(), copyLog.toPath(), StandardCopyOption.REPLACE_EXISTING);
					copyLog.createNewFile();
				}
			}
		}
	}

}
