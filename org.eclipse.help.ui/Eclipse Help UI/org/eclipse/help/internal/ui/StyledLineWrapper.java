package org.eclipse.help.internal.ui;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import java.text.*;
import java.util.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.*;
import org.eclipse.help.internal.contributors.ContextContributor;
import org.eclipse.help.internal.util.TString;

public class StyledLineWrapper implements StyledTextContent {
	/** Text to display (with embedded styles) */
	private String text;
	/** Lines after splitting */
	ArrayList lines = new ArrayList();
	/** Style ranges, per line */
	ArrayList lineStyleRanges = new ArrayList();
	/** Line breaker */
	private static BreakIterator lineBreaker = BreakIterator.getLineInstance();
	/** Beyond this length, lines should wrap */
	public final static int MAX_LINE_LENGTH = 72;
	
	/**
	 * Constructor
	 */
	public StyledLineWrapper(String text)
	{
		setText(text);
	}
	
	/**
	 * @see StyledTextContent#addTextChangeListener(TextChangeListener)
	 */
	public void addTextChangeListener(TextChangeListener l) {
		// do nothing
	}

	/**
	 * @see StyledTextContent#getCharCount()
	 */
	public int getCharCount() {
		int count = 0;
		for (Iterator i=lines.iterator(); i.hasNext(); )
			count += ((String)i.next()).length();
		return count;
	}

	/**
	 * @see StyledTextContent#getLine(int)
	 */
	public String getLine(int i) {
		//if (i < lines.size())
			return (String)lines.get(i);
	}

	/**
	 * @see StyledTextContent#getLineAtOffset(int)
	 */
	public int getLineAtOffset(int offset) {
		int count = 0;
		int line = -1;
		while(count <= offset)
		{
			count += getLine(++line).length();
		}
		return line;
	}

	/**
	 * @see StyledTextContent#getLineCount()
	 */
	public int getLineCount() {
		if (lines.size() == 0)
			return 1;
		else
			return lines.size();
	}

	/**
	 * @see StyledTextContent#getLineDelimiter()
	 */
	public String getLineDelimiter() {
		return null;
	}

	/**
	 * @see StyledTextContent#getOffsetAtLine(int)
	 */
	public int getOffsetAtLine(int line) {
		if (lines.size() == 0)
			return 0;
			
		int offset = 0;
		for (int i=0; i<line; i++)
			offset += getLine(i).length();
		return offset;
	}

	/**
	 * @see StyledTextContent#getTextRange(int, int)
	 */
	public String getTextRange(int start, int end) {
		int l1 = getLineAtOffset(start);
		int l2 = getLineAtOffset(end);
		if (l1 == l2)
			return getLine(l1).substring(start-getOffsetAtLine(l1), end-start);
		else
		{
			StringBuffer range = new StringBuffer(getLine(l1).substring(start-getOffsetAtLine(l1)));
			for (int i=l1+1; i<l2; i++)
				range.append(getLine(i));
			range.append(getLine(l2).substring(0, end-getOffsetAtLine(l2)));
			return range.toString();
		}
	}

	/**
	 * @see StyledTextContent#removeTextChangeListener(TextChangeListener)
	 */
	public void removeTextChangeListener(TextChangeListener arg0) {
		// do nothing
	}

	/**
	 * @see StyledTextContent#replaceTextRange(int, int, String)
	 */
	public void replaceTextRange(int arg0, int arg1, String arg2) {
		// do nothing
	}

	/**
	 * @see StyledTextContent#setText(String)
	 */
	public void setText(String text) {
		processLineBreaks(text);
		processStyles(text);
	}

	/**
	 * Returns the array of styles.
	 */
	public StyleRange[] getStyles()
	{
		StyleRange[] array = new StyleRange[lineStyleRanges.size()];
		lineStyleRanges.toArray(array);
		return array;
	}
	
	/**
	 * Create an array of lines with sytles stripped off.
	 * Each lines is at most MAX_LINE_LENGTH characters.
	 */
	private void processLineBreaks(String text)
	{	
		// create a new set of lines
		lines = new ArrayList();
		
		// Create the original lines with style stripped
		StringTokenizer st = new StringTokenizer(getUnstyledText(text), "\r\n");
		while (st.hasMoreTokens()) {
			lines.add(st.nextToken());
		}

		// Break long lines
		for (int i = 0; i < lines.size(); i++) {
			String line = (String) lines.get(i);
			while (line.length() > 0) {
				int linebreak = getLineBreak(line);

				if (linebreak == 0 || linebreak == line.length())
					break;

				String newline = line.substring(0, linebreak);
				lines.remove(i);
				lines.add(i, newline);
				line = line.substring(linebreak);
				lines.add(++i, line);
			}
		}
	}
	/**
	 * Returns the text without the style
	 */
	private static String getUnstyledText(String styledText) {
		String s = TString.change(styledText, ContextContributor.BOLD_TAG, "");
		s = TString.change(s, ContextContributor.BOLD_CLOSE_TAG, "");
		return s;
	}

	/**
	 * Finds a good line breaking point
	 */
	private static int getLineBreak(String line) {
		lineBreaker.setText(line);
		int lastGoodIndex = 0;
		int currentIndex = lineBreaker.first();
		while (currentIndex < MAX_LINE_LENGTH && currentIndex != BreakIterator.DONE) {
			lastGoodIndex = currentIndex;
			currentIndex = lineBreaker.next();
		}
		return lastGoodIndex;
	}
	
	/**
	 * Creates all the (bold) style ranges for the text.
	 * It is assumed that the text has been split across lines.
	 */
	private void processStyles(String text)
	{
		// create a new array of styles
		lineStyleRanges = new ArrayList();
		
		// first, remove the line breaks
		text = TString.change(text, "\r", "");
		text = TString.change(text, "\n", "");
		
		String unstyledText = getUnstyledText(text); 
		int offset = 0;
		do
		{
			// create a style
			StyleRange style = new StyleRange();
			style.fontStyle = SWT.BOLD;
			
			// the index of the starting style in styled text
			int start = text.indexOf(ContextContributor.BOLD_TAG, offset);
			if (start == -1)
				break;
				
			String prefix = getUnstyledText(text.substring(0, start));
			style.start = prefix.length();

			// the index of the ending style in styled text
			offset = start+1;
			int end = text.indexOf(ContextContributor.BOLD_CLOSE_TAG, offset);
			if (end == -1) 
				break;			
				
			prefix = getUnstyledText(text.substring(0, end));
			style.length = prefix.length() - style.start;
			
			lineStyleRanges.add(style);
			
			offset = end + 1;
		} while (offset < text.length());
	}
	
}
