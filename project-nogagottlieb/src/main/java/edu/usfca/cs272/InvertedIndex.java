package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * invertedIndex class to hold the information, and search . //what other
 * details to add?
 * 
 * @author Noga Gottlieb
 *
 */
public class InvertedIndex {
	/**
	 * inverted index data structure String word, String location, Set indexes
	 */

	private TreeMap<String, TreeMap<String, Set<Integer>>> invertedIndex;

	/**
	 * Word count TreeMap with the files and the word count in them
	 */
	private TreeMap<String, Integer> wordCount;

	/**
	 * Constructor
	 */
	public InvertedIndex() {
		wordCount = new TreeMap<String, Integer>();
		invertedIndex = new TreeMap<String, TreeMap<String, Set<Integer>>>();
	}

	/**
	 * addIndex method build invertedIndex data structure. For each word, find in
	 * which file it was found and in what index
	 * 
	 * @param word     with the word
	 * @param location of the current file
	 * @param index    of the word in the file
	 * @return true if able to add to the tree
	 */
	public boolean addIndex(String word, String location, Integer index) {
		TreeMap<String, Set<Integer>> words = invertedIndex.get(word);
		if (words == null) {
			words = new TreeMap<>();
			invertedIndex.put(word, words);
		}
		Set<Integer> indexes = words.get(location);
		if (indexes == null) {
			indexes = new TreeSet<>();
			words.put(location, indexes);
		}

		boolean modified = indexes.add(index);

		if (modified) {
			int count = wordCount.getOrDefault(location, 0);
			wordCount.put(location, count + 1);
		}

		return modified;

	}

	/**
	 * addAll method to add all date from one invertedIndex to another
	 * 
	 * @param inverted to add
	 */
	public void addAll(InvertedIndex inverted) {
		for (var entry : inverted.invertedIndex.entrySet()) {
			String word = entry.getKey();
			var otherLocations = entry.getValue();
			var currentLocations = invertedIndex.get(word);
			if (currentLocations == null) {
				invertedIndex.put(word, otherLocations);
			} else {
				for (var otherEntry : otherLocations.entrySet()) {
					String otherLocation = otherEntry.getKey();
					var otherIndexes = otherEntry.getValue();
					var currentIndexes = currentLocations.get(otherLocation);
					if (currentIndexes == null) {
						currentLocations.put(otherLocation, otherIndexes);
					} else {
						currentIndexes.addAll(otherIndexes);
					}
				}
			}
		}
		for (var entry : inverted.wordCount.entrySet()) {
			String location = entry.getKey();
			wordCount.put(location, wordCount.getOrDefault(location, 0) + entry.getValue());
		}
	}

	/**
	 * getWords method to get all words in the invertedIndex
	 * 
	 * @return key set of the invertedIndex
	 */
	public Set<String> getWords() {
		return Collections.unmodifiableSet(invertedIndex.keySet());
	}

	/**
	 * get locations method to get all the locations of a word
	 * 
	 * @param word as the key
	 * @return a map as the value
	 */
	public Set<String> getLocations(String word) {
		var locations = invertedIndex.get(word);
		if (locations != null) {
			return Collections.unmodifiableSet(locations.keySet());
		}
		return Collections.emptySet();
	}

	/**
	 * getIndexes method get a set of all indexes of a word and location
	 * 
	 * @param location of the source
	 * @param words    to find the index for
	 * @return the set of indexes the word appear at in this source
	 */
	public Set<Integer> getIndexes(String words, String location) {
		var locations = invertedIndex.get(words);
		if (locations != null) {
			var positions = locations.get(location);
			if (positions != null) {
				return Collections.unmodifiableSet(positions);
			}
		}
		return Collections.emptySet();
	}

	/**
	 * entrySetCounts method to return the set of keys in wordCounts
	 * 
	 * @return set of keys
	 */
	public Set<String> getAllLocations() {
		return Collections.unmodifiableSet(wordCount.keySet());
	}

	/**
	 * getCounts method to get the the wordCounts data structure
	 * 
	 * @return a copy of the tree
	 */
	public Map<String, Integer> getCounts() {
		return Collections.unmodifiableMap(wordCount);
	}

	/**
	 * hasLocation method if there is a location of a word
	 * 
	 * @param word     to check if the location exist
	 * @param location to check if exist to specific word
	 * @return true if there is a location for the word
	 */
	public boolean hasLocation(String word, String location) {
		return getWords().contains(location);
	}

	/**
	 * hasWord method if there is the word in the inverted index
	 * 
	 * @param word to check if found in the inverted index
	 * @return true if the word was found
	 */
	public boolean hasWord(String word) {
		return invertedIndex.containsKey(word);
	}

	/**
	 * hasIndex method to check if an index is in the inverted index
	 * 
	 * @param word     to check if index exist
	 * @param location to check if index exist
	 * @param index    to check if exist to specific word and location
	 * @return true if there is an index for the word and location
	 */
	public boolean hasIndex(String word, String location, int index) {
		return getIndexes(word, location).contains(index);
	}

	/**
	 * indexesSize method return size of index set
	 * 
	 * @param word     the word to find
	 * @param location the file where the word found
	 * @return size if the indexes of where the word was found
	 */
	public int indexesSize(String word, String location) {
		return getIndexes(word, location).size();
	}

	/**
	 * wordSize method of all the size of invertedIndex
	 * 
	 * @return the number of words in the inverted index
	 */
	public int wordsSize() {
		return invertedIndex.keySet().size();
	}

	/**
	 * locationSize method to the number of locations for a word
	 * 
	 * @param word of the to finds size
	 * @return size if the indexes set
	 */
	public int locationSize(String word) {
		return getLocations(word).size();
	}

	/**
	 * sizeCounts method to get the size of wordCounts
	 * 
	 * @return size of wordCounts
	 */
	public int sizeCounts() {
		return wordCount.size();
	}

	/**
	 * sizeCountMethod to get the number of stem words in a file
	 * 
	 * @param location file to get the count from
	 * @return the number of stem words in a give file
	 */
	public int sizeCountLocation(String location) {
		return wordCount.getOrDefault(location, 0);
	}

	@Override
	public String toString() {
		return invertedIndex.toString();
	}

	/**
	 * toStringCounts method converts entire internal data structure into a string
	 * representation
	 * 
	 * @return the map as a string
	 */
	public String toStringCounts() {
		return wordCount.toString();
	}

	/**
	 * indexJson method to write the invertedIndex into the json file
	 * 
	 * @param path to write into
	 * @throws IOException when IO error accrue
	 */
	public void indexJson(Path path) throws IOException {
		JsonWriter.writeInverted(invertedIndex, path);
	}

	/**
	 * search method to search for queries in the inverted index. Calling
	 * partial/exact search.
	 * 
	 * @param queries to search for
	 * @param exact   if true, call exact search, if false, call partial search
	 * @return list of search result objects with all the results for this query
	 */
	public List<SearchResult> search(Set<String> queries, boolean exact) {
		return exact ? exactSearch(queries) : partialSearch(queries);
	}

	/**
	 * exactSearch method to search exactly for queries in the inverted index. For
	 * matches, creating new SearchResult objects (with all the search data). and
	 * add them to a list of the results.
	 * 
	 * @param queries to search exactly for
	 * @return list of exact search result objects with all the results for this
	 *         query
	 */
	public List<SearchResult> exactSearch(Set<String> queries) {
		List<SearchResult> results = new ArrayList<SearchResult>();
		HashMap<String, SearchResult> matches = new HashMap<String, SearchResult>();
		for (String query : queries) {
			searchLogic(query, matches, results);
		}
		Collections.sort(results);
		return results;
	}

	/**
	 * partialSearch method to search partially for queries in the inverted index.
	 * For matches, creating new SearchResult objects (with all the search data).
	 * and add them to a list of the results.
	 * 
	 * @param queries to search partially for
	 * @return list of partial search result objects with all the results for this
	 *         query
	 */
	public List<SearchResult> partialSearch(Set<String> queries) {
		List<SearchResult> results = new ArrayList<SearchResult>();
		HashMap<String, SearchResult> matches = new HashMap<String, SearchResult>();
		for (String query : queries) {
			for (var word : invertedIndex.tailMap(query).entrySet()) {
				String invertedWord = word.getKey();
				if (invertedWord.startsWith(query)) {
					searchLogic(invertedWord, matches, results);
				} else {
					break;
				}
			}
		}
		Collections.sort(results);
		return results;
	}

	/**
	 * searchLogic helper method that holds the common logic for exact search and
	 * partial search
	 * 
	 * @param word    to update in searchResult
	 * @param matches map to look in
	 * @param results list to update with the new searchResult object
	 */
	private void searchLogic(String word, HashMap<String, SearchResult> matches, List<SearchResult> results) {
		var locations = invertedIndex.get(word);
		if (locations != null) {
			for (var entry : locations.entrySet()) {
				String location = entry.getKey();
				SearchResult result = matches.get(location);
				if (result == null) {
					result = new SearchResult(location);
					results.add(result);
					matches.put(location, result);
				}
				result.update(word);
			}
		}
	}

	/**
	 * SearchResult class
	 * 
	 * @author Noga Gottlieb
	 *
	 */
	public class SearchResult implements Comparable<SearchResult> {

		/**
		 * score to hold the score of a search object
		 */
		private double score;
		/**
		 * count to hold the count number of a search object
		 */
		private int count;
		/**
		 * location to hold the location of a search object
		 */
		private final String location;

		/**
		 * Constructor
		 * 
		 * @param location to initialize
		 */
		public SearchResult(String location) {
			this.score = 0.0;
			this.count = 0;
			this.location = location;
		}

		/**
		 * update method to update the query information
		 * 
		 * @param query to update
		 */
		private void update(String query) {
			this.count += invertedIndex.get(query).get(location).size();
			this.score = (double) count / wordCount.get(location);
		}

		/**
		 * @return the score of the search object
		 */
		public double getScore() {
			return this.score;
		}

		/**
		 * @return the count of the search object
		 */
		public int getCount() {
			return this.count;
		}

		/**
		 * @return the location of the search object
		 */
		public String getLocation() {
			return this.location;
		}

		@Override
		public int compareTo(SearchResult other) {
			int compareRanking = Double.compare(other.score, this.score);
			if (compareRanking != 0) {
				return compareRanking;
			}
			int compareCount = Integer.compare(other.count, this.count);
			if (compareCount != 0) {
				return compareCount;
			}
			return this.location.compareToIgnoreCase(other.location);
		}
	}

}
