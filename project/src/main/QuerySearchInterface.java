package edu.usfca.cs272;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import edu.usfca.cs272.InvertedIndex.SearchResult;

/**
 * @author Noga Gottlieb interface for querySearch
 *
 */
public interface QuerySearchInterface {

	/**
	 * processQueryFile method to take a file with queries and search through the
	 * inverted index for the queries in the file by calling processQueryLine
	 * 
	 * @param input path of the query file
	 * @param exact if true, process exact search, if false, process partial search
	 * @throws IOException if needed
	 */
	public default void processQueryFile(Path input, boolean exact) throws IOException {
		try (BufferedReader reader = Files.newBufferedReader(input, UTF_8)) {
			String line;
			while ((line = reader.readLine()) != null) {
				processQueryLine(line, exact);
			}
		}
	}

	/**
	 * processQueryLine method to take a single query line from a file, search
	 * through the inverted index for the queries and update searchData
	 * 
	 * @param line  of query to search for
	 * @param exact if true, process exact search, if false, process partial search
	 */
	public void processQueryLine(String line, boolean exact);

	/**
	 * Get all query lines from a query file.
	 * 
	 * @return Set of all queries.
	 */
	Set<String> getQueryLines();

	/**
	 * getResults method to get all results for a given query line
	 * 
	 * @param queryLine to get the results for
	 * @return list of searchResults objects with all the results for the given
	 *         query
	 */
	public List<SearchResult> getResults(String queryLine);

	/**
	 * Write the search data into a JSON file.
	 * 
	 * @param output Path of the output file.
	 * @throws IOException If an I/O error occurs.
	 */
	void writeJson(Path output) throws IOException;

}
