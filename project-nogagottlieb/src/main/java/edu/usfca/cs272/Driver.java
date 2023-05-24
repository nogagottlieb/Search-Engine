package edu.usfca.cs272;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Arrays;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * Class responsible for running this project based on the provided command-line
 * arguments. See the README for details.
 *
 * @author Noga Gottlieb
 * @author CS 272 Software Development (University of San Francisco)
 * @version Spring 2023
 */
public class Driver {
	/////////////////////////////

	/**
	 * Initializes the classes necessary based on the provided command-line
	 * arguments. This includes (but is not limited to) how to build or search an
	 * inverted index.
	 *
	 * @param args flag/value pairs used to start this program
	 */
	public static void main(String[] args) {
		System.out.println(Arrays.toString(args));
		//////////////////////////////////////////
		ArgumentPraser flagsMap = new ArgumentPraser(args);
		InvertedIndex invertedIndex;
		QuerySearchInterface searchData;
		WebCrawler crawler;
		ThreadSafeInvertedIndex threadSafe = null;
		int threads = 5;
		int maxLinks = 1;
		int PORT = 8080;

		// flag "-threads"
		if (flagsMap.hasFlag("-threads") || flagsMap.hasFlag("-html") || flagsMap.hasFlag("-server")) {
			threads = flagsMap.getInteger("-threads", 5);
			if (threads < 1)
				threads = 5;
			threadSafe = new ThreadSafeInvertedIndex();
			searchData = new ThreadSafeQuerySearch(threadSafe, threads);
			invertedIndex = threadSafe;
		} else {
			invertedIndex = new InvertedIndex();
			searchData = new QuerySearch(invertedIndex);
		}

		// flag "-html"
		if (flagsMap.hasFlag("-html")) {
			String seed = flagsMap.getString("-html");
			if (flagsMap.hasFlag("-crawl")) {
				maxLinks = flagsMap.getInteger("-crawl", 1);
			}
			try {
				crawler = new WebCrawler(seed, maxLinks, threads, threadSafe);
				crawler.crawl();
			} catch (IOException e) {
				System.out.println("can't proccess html");
			} catch (URISyntaxException e) {
				System.out.println("can't proccess html1");
			}

		}

		if (flagsMap.hasFlag("-server")) {
			try {
				PORT = flagsMap.getInteger("-server", 8080);
				Server server = new Server(PORT);
				ServletHandler handler = new ServletHandler();
				handler.addServletWithMapping(new ServletHolder(new SearchServlet(threadSafe, threads)), "/search");
				handler.addServletWithMapping(new ServletHolder(new CountsServlet(threadSafe)), "/counts");
				handler.addServletWithMapping(new ServletHolder(new IndexServlet(threadSafe)), "/index");
				server.setHandler(handler);
				server.start();
				server.join();
			} catch (Exception e) {
				System.out.println("can't build the server");
			}
		}

		// flag "-text
		if (flagsMap.hasFlag("-text") && flagsMap.hasValue("-text")) {
			Path path = flagsMap.getPath("-text");
			try {
				if (threadSafe != null) {
					InvertedIndexBuilder.threadedBuild(path, threadSafe, threads);
				} else {
					InvertedIndexBuilder.build(path, invertedIndex);
				}
			} catch (IOException e) {
				System.out.println("can't build inverted index");
			}
		}

		// flag "-counts"
		if (flagsMap.hasFlag("-counts")) {
			Path output = flagsMap.getPath("-counts", Path.of("counts.json"));
			try {
				JsonWriter.writeObject(invertedIndex.getCounts(), output);
			} catch (IOException e) {
				System.out.println("can't write into json file");
			}
		}

		// flag "-index"
		if (flagsMap.hasFlag("-index")) {
			Path output = flagsMap.getPath("-index", Path.of("index.json"));
			try {
				invertedIndex.indexJson(output);
			} catch (IOException e) {
				System.out.println("can't write into json file");
			}
		}

		// flag "-query"
		if (flagsMap.hasFlag("-query") && flagsMap.hasValue("-query")) {
			boolean exact = !flagsMap.hasFlag("-partial");
			Path path = flagsMap.getPath("-query");
			try {
				searchData.processQueryFile(path, exact);
			} catch (IOException e) {
				System.out.println("can't process query flag");
			}
		}

		// flag "-result"
		if (flagsMap.hasFlag("-results")) {
			Path output = flagsMap.getPath("-results", Path.of("results.json"));
			try {
				searchData.writeJson(output);
			} catch (IOException e) {
				System.out.println("can't write into json file");
			}
		}
	}
}
