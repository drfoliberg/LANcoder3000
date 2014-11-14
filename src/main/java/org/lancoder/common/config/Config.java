package org.lancoder.common.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.lancoder.common.annotations.Prompt;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public abstract class Config {

	private static final String DEFAULT_FFMPEG_PATH = "ffmpeg";
	private static final String DEFAULT_ABSOLUTE_PATH = System.getProperty("user.home");
	private static final String DEFAULT_TEMP_DIRECTORY = System.getProperty("java.io.tmpdir");

	protected transient String configPath;
	@Prompt(message = "shared folder root")
	protected String absoluteSharedFolder;
	@Prompt(message = "FFmpeg's path")
	protected String ffmpegPath;
	@Prompt(message = "temporary files location")
	protected String tempEncodingFolder;

	protected Config() {
		this.ffmpegPath = DEFAULT_FFMPEG_PATH;
		this.tempEncodingFolder = DEFAULT_TEMP_DIRECTORY;
		this.absoluteSharedFolder = DEFAULT_ABSOLUTE_PATH;
	}

	/**
	 * Serializes current config to disk as JSON object.
	 * 
	 * @return True if could write config to disk. Otherwise, return false.
	 */
	public synchronized boolean dump() {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String s = gson.toJson(this);
		File config = new File(configPath);
		try {
			if (!config.exists()) {
				config.getParentFile().mkdirs();
			}
			Files.write(Paths.get(configPath), s.getBytes("UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return String.format("Shared folder location: %s.%n", this.getAbsoluteSharedFolder());
	}

	public String getTempEncodingFolder() {
		return tempEncodingFolder;
	}

	public void setTempEncodingFolder(String tempEncodingFolder) {
		this.tempEncodingFolder = tempEncodingFolder;
	}

	public String getAbsoluteSharedFolder() {
		return absoluteSharedFolder;
	}

	public abstract String getDefaultPath();

	public String getFFmpegPath() {
		return ffmpegPath;
	}

	public String getConfigPath() {
		return configPath;
	}

	public void setConfigPath(String configPath) {
		this.configPath = configPath;
	}

}
