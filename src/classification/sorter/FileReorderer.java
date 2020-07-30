package classification.sorter;

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

import classification.util.UtilsFiles;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;

public class FileReorderer {

	private static Map<String, List<LogInstance>> categories;
	private static Map<String, Integer> numLogsCategory;
	private static Map<String, File> files;

	public static void main(String[] args) throws Exception {
		System.out.println("Reordering files");
		categories = new HashMap<>();
		files = new HashMap<>();
		numLogsCategory = new HashMap<>();

		if (new File(UtilsFiles.COMPRESSED_DATA).exists() && !(new File(UtilsFiles.ORIGINAL_DATA).exists())) {
			decompressAllData(UtilsFiles.COMPRESSED_DATA);
		} else if (new File(UtilsFiles.ORIGINAL_DATA).exists() && !(new File(UtilsFiles.COMPRESSED_DATA).exists())) {
			compressAllData(UtilsFiles.ORIGINAL_DATA);
		}

		// Get logs data in a map, with Key = log file name
		getLogsData();

		// Unzip all data in "data/categories" directory
		if (!new File(UtilsFiles.CATEGORIES_DATA).exists() && !new File(UtilsFiles.CATEGORIES_DATA).isDirectory()) {
			decompressData(UtilsFiles.CATEGORIES_DATA);
		}

		// Get categories data from json files
		createCategories();

		// Reorder logs according to the category they are in the json
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



	private static void compressAllData(String path) {
		System.out.println("Compressing data from: " + path);
		File compressed = new File(UtilsFiles.COMPRESSED_DATA);
		compressed.mkdir();
		File train = new File(path);
		for (final File subdirectory : train.listFiles()) {
			new File(UtilsFiles.COMPRESSED_DATA + "/" + subdirectory.getName()).mkdir();
			for (final File log : subdirectory.listFiles()) {
				String newPath = UtilsFiles.COMPRESSED_DATA + "/" + subdirectory.getName() + "/"
						+ log.getName().substring(0, log.getName().lastIndexOf(".")) + ".zip";
				//System.out.println(newPath);
				ZipFile zip = new ZipFile(newPath);
				try {
					zip.addFile(log);
				} catch (ZipException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private static void decompressAllData(String path) {
		System.out.println("Decompressing data from: " + path);
		File train = new File(UtilsFiles.ORIGINAL_DATA);
		train.mkdir();
		File compressed = new File(path);
		for (final File subdirectory : compressed.listFiles()) {
			new File(UtilsFiles.ORIGINAL_DATA + "/" + subdirectory.getName()).mkdir();
			for (final File log : subdirectory.listFiles()) {
				String newPath = UtilsFiles.ORIGINAL_DATA + "/" + subdirectory.getName();
				ZipFile zip = new ZipFile(log);
				try {
					zip.extractAll(newPath);
				} catch (ZipException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private static void decompressData(String path) {
		System.out.println("Decompressing data from: " + path);

		try {
			ZipFile zip = new ZipFile(path + ".zip");
			zip.extractAll(path);

		} catch (ZipException e) {
			e.printStackTrace();
		}
	}

	private static void getLogsData() {
		System.out.println("Loading logs data...");
		File directory = new File(UtilsFiles.ORIGINAL_DATA);
		for (final File subdirectory : directory.listFiles()) {
			for (final File log : subdirectory.listFiles()) {
				if (isFileType(log, "txt")) {
					files.put(log.getName(), log);
				}

			}
		}
	}

	private static void createCategories() {
		System.out.println("Creating categories...");
		File directoryCategories = new File(UtilsFiles.CATEGORIES_DATA);
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
			// Error in json, empty list
		}

	}

	private static void reorganizeLogs() throws IOException {
		System.out.println("Sorting logs by category...");
		new File(UtilsFiles.REORDERED_DATA).mkdir();

		for (List<LogInstance> logReports : categories.values()) {
			String category = logReports.get(0).getCategory();
			category = category.replace("category.", "");
			File logsByCategories = new File(UtilsFiles.REORDERED_DATA + "/" + category);
			logsByCategories.mkdir();
			for (LogInstance log : logReports) {
				String logName = log.getLog();
				File logFile;
				if ((logFile = files.get(logName)) != null) {
					File copyLog = new File(UtilsFiles.REORDERED_DATA + "/" + category + "/" + log.getLog());

					Files.copy(logFile.toPath(), copyLog.toPath(), StandardCopyOption.REPLACE_EXISTING);
					copyLog.createNewFile();
				}
			}
		}
	}

}