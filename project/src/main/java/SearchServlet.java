package edu.usfca.cs272;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.text.StringSubstitutor;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.usfca.cs272.InvertedIndex.SearchResult;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Search Servlet class to create a web page and search in the inverted index
 * using the Bulma CSS framework.
 *
 *
 * @author Noga Gottlieb
 */
public class SearchServlet extends HttpServlet {
	/** Class version for serialization, in [YEAR][TERM] format (unused). */
	private static final long serialVersionUID = 202301;

	/** The title to use for this webpage. */
	private static final String title = "Gottit";

	/** The logger to use for this servlet. */
	private static final Logger log = LogManager.getLogger();

	/** Base path with HTML templates. */
	private static final Path base = Path.of("src/main/resources/html");

	/** Template for starting HTML **/
	private final String headTemplate;

	/** Template for ending HTML **/
	private final String footTemplate;

	/** Template for text HTML. **/
	private final String textTemplate;

	/** Template for each result */
	private final String resultsHeadTemplate;

	/** Thread safe search to perform the search for the query */
	private final ThreadSafeQuerySearch search;

	/** Used to fetch the visited count from a cookie. */
	private static final String VISIT_COUNT = "Count";

	/**
	 * Initializes this search servlet
	 * 
	 * @param inverted to get the data from
	 * @param threads  to use in the query search
	 * @throws IOException if unable to read templates
	 */
	public SearchServlet(ThreadSafeInvertedIndex inverted, int threads) throws IOException {
		super();
		search = new ThreadSafeQuerySearch(inverted, threads);
		// load templates
		headTemplate = Files.readString(base.resolve("search-head.html"), UTF_8);
		footTemplate = Files.readString(base.resolve("search-foot.html"), UTF_8);
		textTemplate = Files.readString(base.resolve("search-text.html"), UTF_8);
		resultsHeadTemplate = Files.readString(base.resolve("results-head.html"), UTF_8);
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		log.info("{} handling: {}", this.hashCode(), request);

		// used to substitute values in our templates
		Map<String, String> values = new HashMap<>();
		values.put("title", title);
		values.put("thread", Thread.currentThread().getName());

		values.put("method", "POST");
		values.put("action", request.getServletPath());

		// generate html from template
		StringSubstitutor replacer = new StringSubstitutor(values);
		String head = replacer.replace(headTemplate);
		String foot = replacer.replace(footTemplate);

		// output generated html
		PrintWriter out = response.getWriter();
		out.println(head);

		out.println(foot);
		out.flush();
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		log.info("{} handling: {}", this.hashCode(), request);
		// set start time to calculate
		long startTime = System.currentTimeMillis();

		String query = request.getParameter("query");
		String privateSearch = request.getParameter("private");
		Map<String, Cookie> cookies = getCookieMap(request);

		// if clicked on private search, clearing cookies
		if (privateSearch != null && privateSearch.equals("true")) {
			log.info("Clearing cookies...");
			clearCookies(request, response);

			// else, enable cookies
		} else {
			log.info("Cookies are enabled.");
			// Set a cookie to enable tracking
			// set initial count
			int count = 1;
			// check for existing count
			if (cookies.containsKey(VISIT_COUNT)) {
				try {
					Cookie visitCount = cookies.get(VISIT_COUNT);
					count = Integer.parseInt(visitCount.getValue()) + 1;
				} catch (NumberFormatException e) {
					log.catching(Level.DEBUG, e);
				}
			}
			response.addCookie(new Cookie(VISIT_COUNT, Integer.toString(count)));
		}
		// create value map and add values
		Map<String, String> values = new HashMap<>();
		values.put("title", "Gottit");
		values.put("thread", Thread.currentThread().getName());
		values.put("query", query);

		// output generated html
		PrintWriter out = response.getWriter();
		StringSubstitutor replacer = new StringSubstitutor(values);

		if (query.isEmpty()) {
			out.printf("    <p>No queries submitted.</p>%n");
		} else {
			search.processQueryLine(query, false);
			List<SearchResult> results = search.getResults(query);
			// set end time after the search
			long endTime = System.currentTimeMillis();
			// calculate the time it took to search, and add it
			long elapsedTime = endTime - startTime;
			values.put("time", String.valueOf(elapsedTime / 1000F));
			// put the number of results in the value map
			values.put("results", String.valueOf(results.size()));
			// display the head
			String head = replacer.replace(resultsHeadTemplate);
			out.println(head);
			// display the results
			if (results.isEmpty()) {
				out.printf("    <p>No results found</p>%n");
			} else {
				for (SearchResult result : results) {
					values.put("location", "<a href=\"" + result.getLocation() + "\">" + result.getLocation() + "</a>");
					values.put("score", String.valueOf(result.getScore()));
					values.put("matches", String.valueOf(result.getCount()));
					String text = replacer.replace(textTemplate);
					out.println(text);
				}
			}
		}
		out.flush();
	}

	/**
	 * Gets the cookies from the HTTP request and maps the cookie name to the cookie
	 * object.
	 *
	 * @param request the HTTP request from web server
	 * @return map from cookie key to cookie value
	 */
	public static Map<String, Cookie> getCookieMap(HttpServletRequest request) {
		HashMap<String, Cookie> map = new HashMap<>();
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				map.put(cookie.getName(), cookie);
			}
		}
		return map;
	}

	/**
	 * Clears all of the cookies included in the HTTP request.
	 *
	 * @param request  the HTTP request
	 * @param response the HTTP response
	 */
	public static void clearCookies(HttpServletRequest request, HttpServletResponse response) {
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				// update cookie values to trigger delete
				cookie.setValue(null);
				cookie.setMaxAge(0);
				// add new cookie to the response
				response.addCookie(cookie);
			}
		}
	}
}
