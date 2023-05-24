package edu.usfca.cs272;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import edu.usfca.cs272.InvertedIndex.SearchResult;

/**
 * QuerySearchThreadSafe class that perform search on the invertedIndax- support
 * multithreaded searching
 * 
 * @author Noga Gottlieb
 */
public class ThreadSafeQuerySearch implements QuerySearchInterface {
	/**
	 * inverted to hold the invertedIndex data
	 */
	private final ThreadSafeInvertedIndex invertedThreaded;
	/**
	 * searchData to hold the search results information
	 */
	private final TreeMap<String, List<SearchResult>> searchData;
	/**
	 * threads with the number of threads to use
	 */
	private final int threads;

	/**
	 * @param threadSafeInvertedIndex with the invertedIndex data
	 * @param threads                 with the number of threads to use
	 */
	public ThreadSafeQuerySearch(ThreadSafeInvertedIndex threadSafeInvertedIndex, int threads) {
		invertedThreaded = threadSafeInvertedIndex;
		searchData = new TreeMap<String, List<SearchResult>>();
		this.threads = threads;
	}

	/**
	 * processQueryFile method to take a file with queries and search through the
	 * inverted index for the queries in the file by calling processQueryLine
	 * 
	 * @param input path of the file to process
	 * @param exact exact if true, process exact search, if false, process partial
	 *              search
	 * @throws IOException if needed
	 */
	@Override
	public void processQueryFile(Path input, boolean exact) throws IOException {
		WorkQueue tasks = new WorkQueue(threads);
		try (BufferedReader reader = Files.newBufferedReader(input, UTF_8)) {
			String line;
			while ((line = reader.readLine()) != null) {
				tasks.execute(new Task(line, exact));
			}
		} finally {
			tasks.finish();
			tasks.shutdown();
		}
	}

	/**
	 * processQueryLine thread safe method to take a single query line from a file,
	 * search through the inverted index for the queries and update searchData
	 * 
	 * @param line  of query to search for
	 * @param exact if true, process exact search, if false, process partial search
	 */
	@Override
	public void processQueryLine(String line, boolean exact) {
		TreeSet<String> query = FileStemmer.uniqueStems(line);
		if (!query.isEmpty()) {
			String joined = String.join(" ", query);
			synchronized (searchData) {
				if (searchData.containsKey(joined)) {
					return;
				}
				searchData.put(joined, null);
			}
			List<SearchResult> results = invertedThreaded.search(query, exact);
			synchronized (searchData) {
				searchData.put(joined, results);
			}
		}
	}

	/**
	 * writeJson to write the searchDate into a json file
	 * 
	 * @param output path of file to write to
	 * @throws IOException if needed
	 */
	@Override
	public void writeJson(Path output) throws IOException {
		synchronized (searchData) {
			JsonWriter.writeSearch(searchData, output);
		}
	}

	/**
	 * getQueryLines method to get all queries from a query file
	 * 
	 * @return set of all queries
	 */
	@Override
	public Set<String> getQueryLines() {
		synchronized (searchData) {
			return searchData.keySet();
		}
	}

	/**
	 * getResults method to get all results for a given query line
	 * 
	 * @param queryLine to get the results for
	 * @return list of searchResults objects with all the results for the given
	 *         query
	 */
	@Override
	public List<SearchResult> getResults(String queryLine) {
		TreeSet<String> query = FileStemmer.uniqueStems(queryLine);
		if (!query.isEmpty()) {
			String joined = String.join(" ", query);
			synchronized (searchData) {
				var results = searchData.get(joined);
				if (results != null) {
					return Collections.unmodifiableList(results);
				}
			}
		}
		return Collections.emptyList();
	}

	@Override
	public String toString() {
		synchronized (searchData) {
			return searchData.toString();
		}
	}

	/**
	 * The non-static task class that will update the shared paths and pending
	 * members in our task manager instance.
	 */
	private class Task implements Runnable {
		/**
		 * path to processes
		 */
		private final String line;

		/**
		 * if true, process exact search, if false, process partial search
		 */
		private final boolean exact;

		/**
		 * Initializes a task
		 * 
		 * @param line  to process
		 * @param exact if true, process exact search, if false, process partial search
		 */
		public Task(String line, boolean exact) {
			this.line = line;
			this.exact = exact;
		}

		@Override
		public void run() {
			processQueryLine(line, exact);
		}

	}
}
