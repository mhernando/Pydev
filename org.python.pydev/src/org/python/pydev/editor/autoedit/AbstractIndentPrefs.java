/*
 * Created on May 5, 2005
 *
 * @author Fabio Zadrozny
 */
package org.python.pydev.editor.autoedit;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IDocument;
import org.python.pydev.core.docutils.DocUtils;

/**
 * @author Fabio Zadrozny
 */
public abstract class AbstractIndentPrefs implements IIndentPrefs{

    private boolean forceTabs = false;

    public boolean getForceTabs() {
        return forceTabs;
    }

    public void setForceTabs(boolean forceTabs) {
        this.forceTabs = forceTabs;
    }

    /**
     * Naive implementation. Always redoes the indentation string based in the
     * spaces and tabs settings. 
     * 
     * @see org.python.pydev.editor.autoedit.IIndentPrefs#getIndentationString()
     */
    public String getIndentationString() {
        if (getUseSpaces() && !getForceTabs())
            return DocUtils.createSpaceString(getTabWidth());
        else
            return DocUtils.TAB_STRING;
    }

	public void convertToStd(IDocument document, DocumentCommand command){
		try {
            if (getUseSpaces()) {
                command.text = convertTabsToSpaces(document, command.length, command.text, command.offset, getIndentationString());
            }

            else {
                command.text = convertSpacesToTabs(document, command.length, command.text, command.offset, getIndentationString());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

	}


	
	
	//------------------------------------------------------------- UTILS
    
	/**
	 * Replaces tabs if needed by ident string or just a space depending of the
	 * tab location
	 * 
	 */
	private String convertTabsToSpaces(
		IDocument document, int length, String text, int offset, 
		String indentString) throws BadLocationException 
	{
		// only interresting if it contains a tab (also if it is a tab only)
		if (text.indexOf(DocUtils.TAB_STRING) != -1) {
			// get some text infos
			
			if (text.equals(DocUtils.TAB_STRING)) {
			    //only a single tab?
				deleteWhitespaceAfter(document, offset);
				text = indentString;
				
			} else {
			    // contains a char (pasted text)
				byte[] byteLine = text.getBytes();
				StringBuffer newText = new StringBuffer();
				for (int count = 0; count < byteLine.length; count++) {
					if (byteLine[count] == DocUtils.TAB){
						newText.append(indentString);
						
					} else { // if it is not a tab add the char
						newText.append((char) byteLine[count]);
					}
				}
				text = newText.toString();
			}
		}
		return text;
	}
	
	/**
	 * Converts spaces to strings. Useful when pasting
	 */
	private String convertSpacesToTabs(IDocument document, int length,
			String text, int offset, String indentString)
			throws BadLocationException
	{
		String spaceStr = DocUtils.createSpaceString(getTabWidth());
		while(text.startsWith(spaceStr)){
		    text = text.replaceAll(spaceStr, "\t");
		}
        return text;
	}

	/**
	 * When hitting TAB, delete the whitespace after the cursor in the line
	 */
	private void deleteWhitespaceAfter(IDocument document, int offset)
		throws BadLocationException {
		if (offset < document.getLength() && !endsWithNewline(document, document.get(offset, 1))) {
			
		    int lineLength = document.getLineInformationOfOffset(offset).getLength();
			int lineStart = document.getLineInformationOfOffset(offset).getOffset();
			String textAfter = document.get(offset, (lineStart + lineLength) - offset);
			
			if (textAfter.length() > 0
				&& isWhitespace(textAfter)) {
				document.replace(offset, textAfter.length(), DocUtils.EMPTY_STRING);
			}
		}
	}


	private boolean isWhitespace(String s) {
		for (int i = s.length() - 1; i > -1 ; i--)
			if (!Character.isWhitespace(s.charAt(i)))
				return false;
		return true;
	}
	

	/**
	 * True if text ends with a newline delimiter
	 */
	public static boolean endsWithNewline(IDocument document, String text) {
		String[] newlines = document.getLegalLineDelimiters();
		boolean ends = false;
		for (int i = 0; i < newlines.length; i++) {
			String delimiter = newlines[i];
			if (text.indexOf(delimiter) != -1)
				ends = true;
		}
		return ends;
	}

}
