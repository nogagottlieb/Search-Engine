package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;

/**
 * 
 * @author noga gottlieb cs272 - usfca
 *
 */
public class DirectoryTraverser {

	/**
	 * Traverses through the directory and its subdirectories, adding all paths to
	 * the arrayList.
	 *
	 * @param directory the directory to traverse
	 * @param paths     collection of paths
	 * @throws IOException if an I/O error occurs
	 */
	public static void traverseDirectory(Path directory, Collection<Path> paths) throws IOException {
		try (DirectoryStream<Path> listing = Files.newDirectoryStream(directory)) {
			for (Path path : listing) {
				if (Files.isDirectory(path))
					traverseDirectory(path, paths);
				else if (isTextFile(path))
					paths.add(path);
			}
		}
	}

	/**
	 * Return true if a file is a text file
	 * 
	 * @param path of the file
	 * @return true if is a text file
	 */
	public static boolean isTextFile(Path path) {
		String lower = path.toString().toLowerCase();
		return lower.endsWith(".txt") || lower.endsWith(".text");

	}

	/**
	 * get all text files from a directory
	 * 
	 * @param path of file
	 * @return a list of files from the directory
	 * @throws IOException if can't traverse the file
	 */
	public static ArrayList<Path> getTextFiles(Path path) throws IOException {
		ArrayList<Path> paths = new ArrayList<>();
		traverseDirectory(path, paths);
		return paths;
	}

}
