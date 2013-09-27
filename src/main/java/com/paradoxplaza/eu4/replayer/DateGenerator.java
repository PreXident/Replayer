package com.paradoxplaza.eu4.replayer;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * Generates Dates.
 */
public class DateGenerator
		implements Iterable<Date>, Iterator<Date> {
	
	Calendar cal;
	
	// Format used in EU4 save game files
	private static final DateFormat FORMATTER = new SimpleDateFormat("yyyy.MM.dd");
	
	/** Minimal date of the generator. */
	final Date min;
	
	/** Maximal date of the generator. */
	final Date max;
	
	/** Current date of the generator. */
	private final ObjectProperty<Date> date;
	
	
	/** Progress in timeline. */
	private final DoubleProperty progress = new SimpleDoubleProperty(0);
	
	/**
	 * Only constructor.
	 * 
	 * @param min minimal date
	 * @param max maximal date
	 */
	public DateGenerator(final Date min, final Date max) {
		cal = Calendar.getInstance();
		cal.setTime(min);
		
		this.min = min;
		date = new SimpleObjectProperty<>(min);
		this.max = max;
	}
	
	/**
	 * Returns current date.
	 * 
	 * @return current date
	 */
	public ReadOnlyObjectProperty<Date> dateProperty() {
		return date;
	}
	
	/**
	 * Returns progress.
	 * 
	 * @return progress
	 */
	public ReadOnlyDoubleProperty progressProperty() {
		progress.set(cal.getTime().getTime() / (min.getTime() - max.getTime()));
		return progress;
	}
	
	/**
	 * Returns whether {@link #prev()} can be called.
	 * 
	 * @return true if prev() can be called, false otherwise
	 */
	public boolean hasPrev() {
		return min.compareTo(date.get()) < 0;
	}
	
	@Override
	public boolean hasNext() {
		return date.get().compareTo(max) <= 0;
	}
	
	@Override
	public Iterator<Date> iterator() {
		return this;
	}
	
	@Override
	public Date next() {
		assert hasNext() : "No next date!";
		cal.add(Calendar.DATE, 1);
		date.set(cal.getTime());
		return date.get();
	}
	
	/**
	 * Moves date one day back and returns this new date.
	 * 
	 * @return new date one day back
	 */
	public Date prev() {
		assert hasPrev() : "No previous date!";
		cal.add(Calendar.DATE, -1);
		date.set(cal.getTime());
		return date.get();
	}
	
	/**
	 * Convenience method to compute the number of days between two dates
	 * @param start Duh
	 * @param end Duh
	 * @return Difference in days (negative number will be returned if (start > end)
	 */
	public static int getDaysBetween(Date start, Date end) {
		// No convenience method in JavaSE library for getting distance between two days
		// This is approved by stackoverflow
		return (int) ((end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24));
	}
	
	@Override
	public void remove() {
		// nothing
	}
	
	/**
	 * Using the defined FORMATTER, parses the encountered string.
	 * @param word String in save game file
	 * @return The parsed date
	 */
	public static Date parse(String word) {
		try {
			return FORMATTER.parse(word);
		} catch (ParseException e) {
			throw new IllegalArgumentException("Encountered unparseable date " + word, e);
		}
	}
}
