package edu.usfca.cs272;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import opennlp.tools.stemmer.snowball.SnowballStemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM;

/**
 * @author Noga Gottlieb
 *
 */
public class InvertedIndexBuilder {

	/**
	 * build method to build a file into invertedIndex
	 * 
	 * @param path          of file to build
	 * @param invertedIndex to build/update
	 * @throws IOException of needed
	 */
	public static void build(Path path, InvertedIndex invertedIndex) throws IOException {
		if (Files.isRegularFile(path)) {
			buildFile(path, invertedIndex);
		} else if (Files.isDirectory(path)) {
			ArrayList<Path> paths = DirectoryTraverser.getTextFiles(path);
			for (Path file : paths) {
				buildFile(file, invertedIndex);
			}
		}
	}

	/**
	 * thread safe version of build
	 * 
	 * @param input         of file to build
	 * @param invertedIndex to build/update
	 * @param threads       number of threads
	 * @throws IOException if needed
	 */
	public static void threadedBuild(Path input, ThreadSafeInvertedIndex invertedIndex, int threads)
			throws IOException {
		WorkQueue tasks = new WorkQueue(threads);
		try {
			if (Files.isRegularFile(input)) {
				tasks.execute(new Task(input, invertedIndex));
			} else if (Files.isDirectory(input)) {
				ArrayList<Path> paths = DirectoryTraverser.getTextFiles(input);
				for (Path path : paths) {
					tasks.execute(new Task(path, invertedIndex));
				}
			}
		} finally {
			tasks.finish();
			tasks.shutdown();
		}
	}

	/**
	 * buildFile method build an inverted index from a file
	 * 
	 * @param input         file to add to the list
	 * @param invertedIndex with the inverted index to build
	 * @throws IOException if needed
	 */
	public static void buildFile(Path input, InvertedIndex invertedIndex) throws IOException {
		try (BufferedReader reader = Files.newBufferedReader(input, UTF_8);) {
			String line;
			int countIndex = 0;
			SnowballStemmer stemmer = new SnowballStemmer(ALGORITHM.ENGLISH);
			String location = input.toString();
			while ((line = reader.readLine()) != null) {
				String[] splitted = FileStemmer.parse(line);
				for (String word : splitted) {
					countIndex++;
					word = stemmer.stem(word).toString();
					invertedIndex.addIndex(word, location, countIndex);
				}
			}
		}
	}

	/**
	 * The non-static task class that will update the shared paths and pending
	 * members in our task manager instance.
	 */
	private static class Task implements Runnable {
		/**
		 * path to processes
		 */
		private final Path path;
		/**
		 * invertedIndex to build
		 */
		private final ThreadSafeInvertedIndex threadSafeInvertedIndex;

		/**
		 * Initializes a task
		 * 
		 * @param path                    to process
		 * @param threadSafeInvertedIndex to build
		 */
		public Task(Path path, ThreadSafeInvertedIndex threadSafeInvertedIndex) {
			this.path = path;
			this.threadSafeInvertedIndex = threadSafeInvertedIndex;
		}

		@Override
		public void run() {
			try {
				InvertedIndex local = new InvertedIndex();
				buildFile(path, local);
				threadSafeInvertedIndex.addAll(local);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}

		}
	}

}
