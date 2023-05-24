package edu.usfca.cs272;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import opennlp.tools.stemmer.snowball.SnowballStemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM;

/**
 * @author Noga Gottlieb WebCrawler class to take a link, process it, and build
 *         inverted index
 *
 */
public class WebCrawler {

	/**
	 * seed with the link
	 */
	public final String seed;
	/**
	 * invertedIndex to build
	 */
	public final ThreadSafeInvertedIndex invertedIndex;

	/**
	 * threads with the number of threads to initialize
	 */
	public final int threads;

	/**
	 * seeds with the max number of links to process
	 */
	public int seeds;

	/**
	 * work queue for multithreding
	 */
	public final WorkQueue tasks;

	/**
	 * visited set to track all the visited links
	 */
	HashSet<String> visited = new HashSet<String>();

	/**
	 * count variable to keep track on the links number
	 */
	public int count = 0;

	/**
	 * Constructor
	 * 
	 * @param seed          with the link to process
	 * @param seeds         with number of links to process
	 * @param threads       with number of threads
	 * @param invertedIndex to build
	 */
	public WebCrawler(String seed, int seeds, int threads, ThreadSafeInvertedIndex invertedIndex) {
		this.seed = seed;
		this.seeds = seeds;
		this.threads = threads;
		this.invertedIndex = invertedIndex;
		tasks = new WorkQueue(threads);
	}

	/**
	 * build method to take a seed, process it, and build invertedIndex
	 * 
	 * @throws IOException        when needed
	 * @throws URISyntaxException when needed
	 */
	public void crawl() throws IOException, URISyntaxException {
		try {
			visited.add(seed);
			count++;
			tasks.execute(new Task(seed, invertedIndex));
		} finally {
			tasks.join();
		}
	}

	/**
	 * build link method to take a link, crawl, find all links and add them to the
	 * inverted index
	 * 
	 * @param seed  to process
	 * @param index to build
	 * @throws URISyntaxException when needed
	 * @throws IOException        when needed
	 */
	public void buildLink(String seed, InvertedIndex index) throws URISyntaxException, IOException {
		String htmlString;
		Map<String, List<String>> headers = HttpsFetcher.fetchUrl(seed);
		if (HtmlFetcher.isHtml(headers)) {
			URL url = new URL(seed);
			URL cleanedUrl = LinkFinder.normalize(url);
			htmlString = HtmlFetcher.fetch(cleanedUrl, 3);
			if (htmlString != null) {
				String html = HtmlCleaner.stripBlockElements(htmlString);
				ArrayList<URL> links = LinkFinder.listUrls(cleanedUrl, html);
				if (!links.isEmpty()) {
					synchronized (visited) {
						for (URL link : links) {
							if (!visited.contains(link.toString()) && count < seeds) {
								visited.add(link.toString());
								count++;
								tasks.execute(new Task(link.toString(), invertedIndex));
							}
						}
					}
				}
				String stripHtml = HtmlCleaner.stripHtml(htmlString);
				int countIndex = 0;
				SnowballStemmer stemmer = new SnowballStemmer(ALGORITHM.ENGLISH);
				String[] splitted = FileStemmer.parse(stripHtml);
				for (String word : splitted) {
					countIndex++;
					word = stemmer.stem(word).toString();
					index.addIndex(word, seed, countIndex);
				}
			}
		}
	}

	/**
	 * The non-static task class that will update the shared paths and pending
	 * members in our task manager instance.
	 */
	private class Task implements Runnable {
		/**
		 * seed with the link
		 */
		private final String seed;
		/**
		 * invertedIndex to build
		 */
		private final ThreadSafeInvertedIndex threadSafeInvertedIndex;

		/**
		 * Initializes a task
		 * 
		 * @param seed                    to process
		 * @param threadSafeInvertedIndex to build
		 */
		public Task(String seed, ThreadSafeInvertedIndex threadSafeInvertedIndex) {
			this.seed = seed;
			this.threadSafeInvertedIndex = threadSafeInvertedIndex;
		}

		@Override
		public void run() {
			try {
				InvertedIndex local = new InvertedIndex();
				buildLink(seed, local);
				threadSafeInvertedIndex.addAll(local);
			} catch (IOException | URISyntaxException e) {
				throw new UncheckedIOException((IOException) e);
			}

		}
	}
}
