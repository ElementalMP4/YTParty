package main.java.de.voidtech.ytparty.service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FileReader {
	
	@Autowired
	private ConfigService config; //The configuration of the program
	
	private static final String FILE_BASE_URL = "www/"; //The folder where all the files are kept
	
	private static final Logger LOGGER = Logger.getLogger(FileReader.class.getName()); //We should log when files are loaded.
	
	private HashMap<String, String> textFileCache = new HashMap<String, String>(); //A cache to store text files
	private HashMap<String, byte[]> binaryFileCache = new HashMap<String, byte[]>(); //A cache to store images and other binary files
	
    private String readFile(String fileName) {
		try {
			byte[] contents = FileUtils.readFileToByteArray(new File(FILE_BASE_URL + fileName)); //Load the file into a byte array
			LOGGER.log(Level.INFO, "Loaded text file '" + fileName + "'"); //Log the file
			return new String(contents, StandardCharsets.UTF_8); //Create a string from the byte array
		} catch (IOException e) { //If an IO Error occurs, log it
			LOGGER.log(Level.SEVERE, "Error occurred during Service Execution: " + e.getMessage());
		}
		return null;
    }
    
    private byte[] readBinaryFile(String fileName) {
		try {
			byte[] contents = FileUtils.readFileToByteArray(new File(FILE_BASE_URL + fileName)); //Load the file into a byte array
			LOGGER.log(Level.INFO, "Loaded binary file '" + fileName + "'"); //Log the file
			return contents; //Return the byte array. We do not need this to be stringified.
		} catch (IOException e) { //If an IO Error occurs, log it
			LOGGER.log(Level.SEVERE, "Error occurred during Service Execution: " + e.getMessage());
		}
		return null;
    }
    
    public String getTextFileContents(String fileName) {
    	if (config.textCacheEnabled()) { //We can disable the cache for development purposes
    		if (!textFileCache.containsKey(fileName)) textFileCache.put(fileName, readFile(fileName)); //If the file is not cached, add it
        	return textFileCache.get(fileName);	//Return the file from the cache
    	} else return readFile(fileName); //If the cache is disabled, load the file from disk
    }

  //This method is the same as the text file reader, but it returns a byte array instead of a string.
	public byte[] getBinaryFileContents(String fileName) {
		if (config.binaryCacheEnabled()) {
			if (!binaryFileCache.containsKey(fileName)) binaryFileCache.put(fileName, readBinaryFile(fileName));
	    	return binaryFileCache.get(fileName);	
		} else return readBinaryFile(fileName);
	}
	
	//A public method to clear the cached files
	public void clearCache() {
		textFileCache.clear();
		binaryFileCache.clear();
	}
}
