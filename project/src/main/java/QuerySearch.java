package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import edu.usfca.cs272.InvertedIndex.SearchResult;
import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM;

/**
 * QuerySearch class that perform search on the invertedIndax
 * 
 * @author Noga Gottlieb
 */
public class QuerySearch implements QuerySearchInterface {
	/**
	 * inverted to hold the invertedIndex data
	 */
	private final InvertedIndex inverted;
	/**
	 * searchData to hold the search results information
	 */
	private final TreeMap<String, List<SearchResult>> searchData;

	/**
	 * stemmer object to reuse when processing the queries
	 */
	private final Stemmer stemmer;

	/**
	 * @param invertedIndex with the invertedIndex data
	 */
	public QuerySearch(InvertedIndex invertedIndex) {
		inverted = invertedIndex;
		searchData = new TreeMap<String, List<SearchResult>>();
		stemmer = new SnowballStemmer(ALGORITHM.ENGLISH);
	}

	/**
	 * processQueryLine method to take a single query line from a file, search
	 * through the inverted index for the queries and update searchData
	 * 
	 * @param line  of query to search for
	 * @param exact if true, process exact search, if false, process partial search
	 */
	@Override
	public void processQueryLine(String line, boolean exact) {
		TreeSet<String> query = FileStemmer.uniqueStems(line, stemmer);
		if (!query.isEmpty()) {
			String joined = String.join(" ", query);
			if (!searchData.containsKey(joined)) {
				List<SearchResult> results = inverted.search(query, exact);
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
		JsonWriter.writeSearch(searchData, output);
	}

	/**
	 * getQueryLines method to get all queries from a query file
	 * 
	 * @return set of all queries
	 */
	@Override
	public Set<String> getQueryLines() {
		return searchData.keySet();
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
		TreeSet<String> query = FileStemmer.uniqueStems(queryLine, stemmer);
		if (!query.isEmpty()) {
			String joined = String.join(" ", query);
			var results = searchData.get(joined);
			if (results != null) {
				return Collections.unmodifiableList(results);
			}
		}
		return Collections.emptyList();
	}

	@Override
	public String toString() {
		return searchData.toString();
	}

}
