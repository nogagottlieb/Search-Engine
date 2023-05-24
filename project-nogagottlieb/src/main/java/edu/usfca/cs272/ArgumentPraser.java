package edu.usfca.cs272;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Set;

/**
 * Parses and stores command-line arguments into simple flag/value pairs.
 *
 * @author CS 272 Software Development (University of San Francisco) and Noga
 *         Gottlieb
 * @version Spring 2023
 */
public class ArgumentPraser {
	/**
	 * Stores command-line arguments in flag/value pairs.
	 */
	private final HashMap<String, String> map;

	/**
	 * Initializes this argument map.
	 */
	public ArgumentPraser() {
		this.map = new HashMap<>();
	}

	/**
	 * Initializes this argument map and then parsers the arguments into flag/value
	 * pairs where possible. Some flags may not have associated values. If a flag is
	 * repeated, its value is overwritten.
	 *
	 * @param args the command line arguments to parse
	 */
	public ArgumentPraser(String[] args) {
		this();
		parse(args);
	}

	/**
	 * Determines whether the argument is a flag. The argument is considered a flag
	 * if it is a dash "-" character followed by any character that is not a digit
	 * or whitespace. For example, "-hello" and "-@world" are considered flags, but
	 * "-10" and "- hello" are not.
	 *
	 * @param arg the argument to test if its a flag
	 * @return {@code true} if the argument is a flag
	 *
	 * @see String#startsWith(String)
	 * @see String#length()
	 * @see String#codePointAt(int)
	 * @see Character#isDigit(int)
	 * @see Character#isWhitespace(int)
	 */
	public static boolean isFlag(String arg) {
		// check if the string is null or the second char is null
		if (arg == null || arg.isEmpty() || arg.length() == 1)
			return false;
		else if (arg.charAt(0) == '-' && !(Character.isDigit(arg.charAt(1)))
				&& !(Character.isWhitespace(arg.charAt(1))))
			return true;
		else
			return false;
	}

	/**
	 * Determines whether the argument is a value. Anything that is not a flag is
	 * considered a value.
	 *
	 * @param arg the argument to test if its a value
	 * @return {@code true} if the argument is a value
	 */
	public static boolean isValue(String arg) {
		return !isFlag(arg);
	}

	/**
	 * Parses the arguments into flag/value pairs where possible. Some flags may not
	 * have associated values. If a flag is repeated, its value will be overwritten.
	 *
	 * @param args the command line arguments to parse
	 */
	public void parse(String[] args) {

		if (args.length == 1 && isFlag(args[0]))
			this.map.put(args[0], null);

		else if (args.length > 1) {
			for (int i = 0; i < args.length; i++) {
				if (isFlag(args[i]) && i + 1 < args.length && isFlag(args[i + 1]))
					this.map.put(args[i], null);

				else if (isFlag(args[i]) && i + 1 < args.length) {
					this.map.put(args[i], args[i + 1]);
					i++;
				} else if (isFlag(args[i]))
					this.map.put(args[i], null);
			}

		}

		// throw new UnsupportedOperationException("Not yet implemented.");
	}

	/**
	 * Returns the number of unique flags.
	 *
	 * @return number of unique flags
	 */
	public int numFlags() {
		return this.map.size();
	}

	/**
	 * Determines whether the specified flag exists.
	 *
	 * @param flag the flag check
	 * @return {@code true} if the flag exists
	 */
	public boolean hasFlag(String flag) {
		if (this.map.containsKey(flag))
			return true;
		else
			return false;

	}

	/**
	 * Determines whether the specified flag is mapped to a non-null value.
	 *
	 * @param flag the flag to find
	 * @return {@code true} if the flag is mapped to a non-null value
	 */
	public boolean hasValue(String flag) {
		if (this.map.get(flag) != null)
			return true;
		else
			return false;
	}

	/**
	 * Returns the value to which the specified flag is mapped as a {@link String}
	 * or the backup value if there is no mapping.
	 *
	 * @param flag   the flag whose associated value is to be returned
	 * @param backup the backup value to return if there is no mapping
	 * @return the value to which the specified flag is mapped, or the backup value
	 *         if there is no mapping
	 */
	public String getString(String flag, String backup) {
		if (hasValue(flag))
			return this.map.get(flag);
		else
			return backup;
	}

	/**
	 * Returns the value to which the specified flag is mapped as a {@link String}
	 * or null if there is no mapping.
	 *
	 * @param flag the flag whose associated value is to be returned
	 * @return the value to which the specified flag is mapped or {@code null} if
	 *         there is no mapping
	 */
	public String getString(String flag) {
		if (hasValue(flag))
			return this.map.get(flag);
		else
			return null;
	}

	/**
	 * Returns the value the specified flag is mapped as a {@link Path}, or the
	 * backup value if unable to retrieve this mapping (including being unable to
	 * convert the value to a {@link Path} or if no value exists).
	 *
	 * This method should not throw any exceptions!
	 *
	 * @param flag   the flag whose associated value will be returned
	 * @param backup the backup value to return if there is no valid mapping
	 * @return the value the specified flag is mapped as a {@link Path}, or the
	 *         backup value if there is no valid mapping
	 *
	 * @see Path#of(String, String...)
	 */
	public Path getPath(String flag, Path backup) {
		if (hasValue(flag)) {
			if (Path.of(flag, getString(flag)) != null)
				return (Path.of(getString(flag).toString()));
			else
				return backup;
		} else
			return backup;
	}

	/**
	 * Returns the value to which the specified flag is mapped as a {@link Path}, or
	 * {@code null} if unable to retrieve this mapping (including being unable to
	 * convert the value to a {@link Path} or no value exists).
	 *
	 * This method should not throw any exceptions!
	 *
	 * @param flag the flag whose associated value is to be returned
	 * @return the value to which the specified flag is mapped, or {@code null} if
	 *         unable to retrieve this mapping
	 *
	 * @see #getPath(String, Path)
	 */
	public Path getPath(String flag) {
		if (hasValue(flag)) {
			if (Path.of(flag, getString(flag)) != null)
				return (Path.of(getString(flag).toString()));
			else
				return null;
		} else
			return null;
	}

	/**
	 * Returns the value the specified flag is mapped as an int value, or the backup
	 * value if unable to retrieve this mapping (including being unable to convert
	 * the value to an int or if no value exists).
	 *
	 * @param flag   the flag whose associated value will be returned
	 * @param backup the backup value to return if there is no valid mapping
	 * @return the value the specified flag is mapped as an int, or the backup value
	 *         if there is no valid mapping
	 *
	 * @see Integer#parseInt(String)
	 */
	public int getInteger(String flag, int backup) {
		if (hasValue(flag)) {
			try {
				return Integer.parseInt(this.map.get(flag));
			} catch (NumberFormatException e) {
				return backup;
			}
		} else
			return backup;
	}

	/**
	 * Returns the value the specified flag is mapped as an int value, or 0 if
	 * unable to retrieve this mapping (including being unable to convert the value
	 * to an int or if no value exists).
	 *
	 * @param flag the flag whose associated value will be returned
	 * @return the value the specified flag is mapped as an int, or 0 if there is no
	 *         valid mapping
	 *
	 * @see #getInteger(String, int)
	 */
	public int getInteger(String flag) {
		if (hasValue(flag)) {
			try {
				return Integer.parseInt(this.map.get(flag));
			} catch (NumberFormatException e) {
				return 0;
			}
		} else
			return 0;
	}

	@Override
	public String toString() {
		return this.map.toString();
	}

	/**
	 * 
	 *
	 * @return set of keys
	 */
	public Set<String> keySet() {
		return this.map.keySet();
	}

}
