package edu.usfca.cs272;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.text.StringSubstitutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Index Servlet class to create a web page to display the inverted index data
 * using the Bulma CSS framework.
 *
 *
 * @author Noga Gottlieb
 */
public class IndexServlet extends HttpServlet {
	/** Class version for serialization, in [YEAR][TERM] format (unused). */
	private static final long serialVersionUID = 202301;

	/** The title to use for this webpage. */
	private static final String title = "Gottit";

	/** The logger to use for this servlet. */
	private static final Logger log = LogManager.getLogger();

	/** Base path with HTML templates. */
	private static final Path base = Path.of("src/main/resources/html");

	/** The data structure to use for storing messages. */

	/** Template for starting HTML **/
	private final String headTemplate;

	/** Template for individual message HTML. **/
	private final String textTemplate;

	/** location template to display the location */
	private final String locationTemplate;

	/** inverted index to get the data from */
	private final ThreadSafeInvertedIndex inverted;

	/**
	 * Initializes this index servlet
	 * 
	 * @param inverted to get the data from
	 * @throws IOException if unable to read templates
	 */
	public IndexServlet(ThreadSafeInvertedIndex inverted) throws IOException {
		super();
		this.inverted = inverted;
		// load templates
		headTemplate = Files.readString(base.resolve("index-head.html"), UTF_8);
		textTemplate = Files.readString(base.resolve("index-text.html"), UTF_8);
		locationTemplate = Files.readString(base.resolve("index-location-text.html"), UTF_8);
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		log.info("{} handling: {}", this.hashCode(), request);

		// used to substitute values in our templates
		Map<String, String> values = new HashMap<>();
		values.put("title", title);
		values.put("thread", Thread.currentThread().getName());

		// output generated html
		PrintWriter out = response.getWriter();
		StringSubstitutor replacer = new StringSubstitutor(values);
		String head = replacer.replace(headTemplate);
		out.println(head);

		Set<String> words = inverted.getWords();
		for (String word : words) {
			values.put("word", word);
			String text = replacer.replace(textTemplate);
			out.println(text);
			String locationText;
			Set<String> locations = inverted.getLocations(word);
			for (String location : locations) {
				values.put("location", "<a href=\"" + location + "\">" + location + "</a>");
				values.put("count", String.valueOf(inverted.indexesSize(word, location)));
				locationText = replacer.replace(locationTemplate);
				out.println(locationText);
			}
		}
		out.flush();
	}
}
