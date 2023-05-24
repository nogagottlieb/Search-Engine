package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Noga Gottlieb
 * 
 *         ThreadSafeInvertedIndex class that extend the InvertedIndex class and
 *         use thread safe methods
 */
public class ThreadSafeInvertedIndex extends InvertedIndex {
	/** The lock used to protect concurrent access to the underlying set. */
	private final MultiReaderLock lock;

	/**
	 * Constructor
	 */
	public ThreadSafeInvertedIndex() {
		lock = new MultiReaderLock();
	}

	@Override
	public boolean addIndex(String word, String location, Integer index) {
		lock.writeLock().lock();
		try {
			return super.addIndex(word, location, index);
		} finally {
			lock.writeLock().unlock();
		}
	}

	@Override
	public void addAll(InvertedIndex inverted) {
		lock.writeLock().lock();
		try {
			super.addAll(inverted);
		} finally {
			lock.writeLock().unlock();
		}
	}

	@Override
	public Set<String> getWords() {
		lock.readLock().lock();
		try {
			return super.getWords();
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public Set<String> getLocations(String word) {
		lock.readLock().lock();
		try {
			return super.getLocations(word);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public Set<Integer> getIndexes(String words, String location) {
		lock.readLock().lock();
		try {
			return super.getIndexes(words, location);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public Set<String> getAllLocations() {
		lock.readLock().lock();
		try {
			return super.getAllLocations();
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public Map<String, Integer> getCounts() {
		lock.readLock().lock();
		try {
			return super.getCounts();
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public boolean hasLocation(String word, String location) {
		lock.readLock().lock();
		try {
			return super.hasLocation(word, location);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public boolean hasWord(String word) {
		lock.readLock().lock();
		try {
			return super.hasWord(word);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public boolean hasIndex(String word, String location, int index) {
		lock.readLock().lock();
		try {
			return super.hasIndex(word, location, index);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public int indexesSize(String word, String location) {
		lock.readLock().lock();
		try {
			return super.indexesSize(word, location);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public int wordsSize() {
		lock.readLock().lock();
		try {
			return super.wordsSize();
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public int locationSize(String word) {
		lock.readLock().lock();
		try {
			return super.locationSize(word);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public int sizeCounts() {
		lock.readLock().lock();
		try {
			return super.sizeCounts();
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public int sizeCountLocation(String location) {
		lock.readLock().lock();
		try {
			return super.sizeCountLocation(location);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public String toString() {
		lock.readLock().lock();
		try {
			return super.toString();
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public String toStringCounts() {
		lock.readLock().lock();
		try {
			return super.toStringCounts();
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public void indexJson(Path path) throws IOException {
		lock.readLock().lock();
		try {
			super.indexJson(path);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public List<SearchResult> exactSearch(Set<String> queries) {
		lock.readLock().lock();
		try {
			return super.exactSearch(queries);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public List<SearchResult> partialSearch(Set<String> queries) {
		lock.readLock().lock();
		try {
			return super.partialSearch(queries);
		} finally {
			lock.readLock().unlock();
		}
	}

}
