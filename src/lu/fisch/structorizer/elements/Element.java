/*
    Structorizer
    A little tool which you can use to create Nassi-Schneiderman Diagrams (NSD)

    Copyright (C) 2009  Bob Fisch

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or any
    later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package lu.fisch.structorizer.elements;

/******************************************************************************************************
 *
 *      Author:         Bob Fisch
 *
 *      Description:    Abstract class for all Elements.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date			Description
 *      ------			----			-----------
 *      Bob Fisch       2007.12.09      First Issue
 *      Kay Gürtzig     2014.11.11      Operator highlighting modified (sse comment)
 *      Kay Gürtzig     2015.10.09      Methods selectElementByCoord(x,y) and getElementByCoord() merged
 *      Kay Gürtzig     2015.10.11      Comment drawing centralized and breakpoint mechanism prepared
 *      Kay Gürtzig     2015.10.13      Execution state separated from selected state
 *      Kay Gürtzig     2015.11.01      operator unification and intermediate syntax transformation ready
 *      Kay Gürtzig     2015.11.12      Issue #25 (= KGU#80) fixed in unifyOperators, highlighting corrected
 *      Kay Gürtzig     2015.12.01      Bugfixes #39 (= KGU#91) and #41 (= KGU#92)
 *      Kay Gürtzig     2015.12.11      Enhancement #54 (KGU#101): Method splitExpressionList added
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      
 *      2015.12.01 (KGU#91/KGU#92)
 *      - Methods setText() were inconsistent and caused nasty effects including data losses (bug #39).
 *      - Operator unification enhanced (issue #41)
 *      2015.11.03 (KGU#18/KGU#23/KGU#63)
 *      - Methods writeOutVariables() and getWidthOutVariables re-merged, lexical splitter extracted from
 *        them.
 *      2015.11.01 (KGU#18/KGU#23)
 *      - Methods unifyOperators(), transformIntermediate() and getIntermediateText() now support different
 *        activities like code generation and execution in a unique way.
 *      2015.10.11/13 (KGU#41 + KGU#43)
 *      - New fields added to distinguish states of selection from those of current execution, this way
 *        inducing more stable colouring and execution path tracking
 *      - a field and several methods introduced to support the setting of breakpoints for execution (it had
 *        always been extremely annoying that for the investigation of some issues near the end of the diagram
 *        either the entire execution had to be started in step more or you had to be utterly quick to pause
 *        in the right moment. Now breakpoints allow to catch the execution wherever necessary.
 *      2014.10.18 / 2014.11.11
 *      - Additions for highlighting of logical operators (both C and Pascal style) in methods
 *        writeOutVariables() and getWidthOutVariables(),
 *      - minor code revision respecting 2- and 3-character operator symbols
 *      2015.10.09
 *      - In E_SHOWCOMMENTS mode, substructures had been eclipsed by the top-level elements popping their
 *        comments. This was due to an incomplete subclassing of method getElementByCoord (in contrast
 *        to the nearly identical method selectElementByCoord), both methods were merged by means of a
 *        discriminating additional parameter to identifyElementByCoord(_x, _y, _forSelection)
 *      20015.10.11 / 2015.10.13
 *      - New field breakpoint and specific drawing extension for setting and drawing breakpoints
 *      - The new breakpoint mechanism made clear that the execution status had to be logically separated
 *        from selection status, which required a new field and an additional drawing mechanism 
 *
 ******************************************************************************************************///


import java.awt.Color;
import java.awt.Font;

import lu.fisch.utils.*;
import lu.fisch.graphics.*;
import lu.fisch.structorizer.parsers.*;
import lu.fisch.structorizer.generators.Generator;
import lu.fisch.structorizer.io.*;

import com.stevesoft.pat.*;  //http://www.javaregex.com/

import java.awt.Point;

public abstract class Element {
	// Program CONSTANTS
	public static String E_VERSION = "3.23-05dev";
	public static String E_THANKS =
	"Developed and maintained by\n"+
	" - Robert Fisch <robert.fisch@education.lu>\n"+
	"\n"+
	"Having also put his fingers into the code\n"+
        " - Kay Gürtzig <kay.guertzig@fh-erfurt.de>\n"+
	"\n"+
	"Export classes written and maintained by\n"+
	" - Oberon: Klaus-Peter Reimers <k_p_r@freenet.de>\n"+
	" - Perl: Jan Peter Klippel <structorizer@xtux.org>\n"+
	" - KSH: Jan Peter Klippel <structorizer@xtux.org>\n"+
	" - BASH: Markus Grundner <markus@praised-land.de>\n"+
	" - Java: Gunter Schillebeeckx <gunter.schillebeeckx@tsmmechelen.be>\n"+
	" - C: Gunter Schillebeeckx <gunter.schillebeeckx@tsmmechelen.be>\n"+
	" - C#: Kay Gürtzig <kay.guertzig@fh-erfurt.de>\n"+
	" - PHP: Rolf Schmidt <rolf.frogs@t-online.de>\n"+
	"\n"+
	"License setup and checking done by\n"+
	" - Marcus Radisch <radischm@googlemail.com>\n"+
	" - Stephan <clauwn@freenet.de>\n"+
	"\n"+
	"User manual edited by\n"+
	" - David Morais <narutodc@hotmail.com>\n"+
	" - Praveen Kumar <praveen_sonal@yahoo.com>\n"+
	" - Jan Ollmann <bkgmjo@gmx.net>\n"+
	"\n"+
	"Translations realised by\n"+
	" - NL: Jerone <jeronevw@hotmail.com>\n"+
	" - DE: Klaus-Peter Reimers <k_p_r@freenet.de>\n"+
	" - LU: Laurent Zender <laurent.zender@hotmail.de>\n"+
	" - ES: Andres Cabrera <andrescabrera20@gmail.com>\n"+
        " - PT/BR: Theldo Cruz <cruz@pucminas.br>\n"+
        " - IT: Andrea Maiani <andreamaiani@gmail.com>\n"+
        " - CN: Wang Lei <wanglei@hollysys.com>\n"+
        " - CZ: Vladimír Vaščák <vascak@spszl.cz>\n"+
        " - RU: Юра Лебедев <elita.alegator@gmail.com>\n"+
	"\n"+
	"Different good ideas and improvements provided by\n"+
	" - Serge Marelli <serge.marelli@education.lu>\n"+
	" - T1IF1 2006/2007\n"+
	" - Gil Belling <gil.belling@education.lu>\n"+
	" - Guy Loesch <guy.loesch@education.lu>\n"+
	" - Claude Sibenaler <claude.sibenaler@education.lu>\n"+
	" - Tom Van Houdenhove <tom@vanhoudenhove.be>\n"+
	" - Sylvain Piren <sylvain.piren@education.lu>\n"+
	" - Bernhard Wiesner <bernhard.wiesner@informatik.uni-erlangen.de>\n"+
	" - Christian Fandel <christian_fandel@web.de>\n"+
	" - Sascha Meyer <harlequin2@gmx.de>\n"+
	" - Andreas Jenet <ajenet@gmx.de>\n"+
	" - Jan Peter Klippel <structorizer@xtux.org>\n"+
	                
	"\n"+
	"File dropper class by\n"+
	" - Robert W. Harder <robertharder@mac.com>\n"+
	"\n"+
	"Pascal parser (GOLDParser) engine by\n"+
	" - Matthew Hawkins <hawkini@barclays.net>\n"+
	"\n"+
	"Delphi grammar by\n"+
	" - Rob F.M. van den Brink <R.F.M.vandenBrink@hccnet.nl>\n"+
	"\n"+
	"Regular expression engine by\n"+
	" - Steven R. Brandt, <sbrandt@javaregex.com>\n"+
	"\n"+
	"Vector graphics export by\n"+
	" - FreeHEP Team <http://java.freehep.org/vectorgraphics>\n"+
	"\n"+
	"Command interpreter provided by\n"+
	" - Pat Niemeyer <pat@pat.net>\n"+
	"\n"+
	"Turtle icon designed by\n"+
	" - rainie_billybear@yahoo.com <rainiew@cass.net>\n"+
	"";
	public final static String E_CHANGELOG = "";

	// some static constants
	protected static int E_PADDING = 20;
	static int E_INDENT = 2;
	public static Color E_DRAWCOLOR = Color.YELLOW;
	public static Color E_COLLAPSEDCOLOR = Color.LIGHT_GRAY;
	// START KGU#41 2015-10-13: Executing status now independent from selection
	public static Color E_RUNNINGCOLOR = Color.ORANGE;		// used for Elements currently (to be) executed 
	// END KGU#41 2015-10-13
	public static Color E_WAITCOLOR = new Color(255,255,210);	// used for Elements with pending execution
	static Color E_COMMENTCOLOR = Color.LIGHT_GRAY;
	// START KGU#43 2015-10-11: New fix color for breakpoint marking
	static Color E_BREAKPOINTCOLOR = Color.RED;				// Colour of the breakpoint bar at element top
	// END KGU#43 2015-10-11
	public static boolean E_VARHIGHLIGHT = false;
	public static boolean E_SHOWCOMMENTS = true;
	public static boolean E_TOGGLETC = false;
	public static boolean E_DIN = false;
	public static boolean E_ANALYSER = true;

	// some colors
	public static Color color0 = Color.decode("0xFFFFFF");
	public static Color color1 = Color.decode("0xFF8080");
	public static Color color2 = Color.decode("0xFFFF80");
	public static Color color3 = Color.decode("0x80FF80");
	public static Color color4 = Color.decode("0x80FFFF");
	public static Color color5 = Color.decode("0x0080FF");
	public static Color color6 = Color.decode("0xFF80C0");
	public static Color color7 = Color.decode("0xC0C0C0");
	public static Color color8 = Color.decode("0xFF8000");
	public static Color color9 = Color.decode("0x8080FF");

	// text "constants"
	public static String preAlt = "(?)";
	public static String preAltT = "T";
	public static String preAltF = "F";
	public static String preCase = "(?)\n?\n?\nelse";
	public static String preFor = "for ? <- ? to ?";
	public static String preWhile = "while (?)";
	public static String preRepeat = "until (?)";
	
	// used font
	protected static Font font = new Font("Helvetica", Font.PLAIN, 12);

	public static final String COLLAPSED =  "...";
	public static boolean altPadRight = true;

	// element attributes
	protected StringList text = new StringList();
	public StringList comment = new StringList();
        
	public boolean rotated = false;

	public Element parent = null;
	public boolean selected = false;
	// START KGU#41 2015-10-13: Execution mark had to be separated from selection
	public boolean executed = false;	// Is set while being executed
	// END KGU#41 2015-10-13
	public boolean waited = false;		// Is set while a substructure Element is under execution
	private Color color = Color.WHITE;

	private boolean collapsed = false;
	
	// START KGU 2015-10-11: States whether the element serves as breakpoint for execution (stop before!)
	protected boolean breakpoint = false;
	// END KGU 2015-10-11

	// used for drawing
	public Rect rect = new Rect();
	// START KGU#64 2015-11-03: Is to improve drawing performance
	protected boolean isRectUpToDate = false;		// Will be set and used by prepareDraw() - to be reset on changes
	private static StringList specialSigns = null;	// Strings to be highlighted in the text (lazy initialisation)
	/**
	 * Resets my cached drawing info
	 */
	protected final void resetDrawingInfo()
	{
		this.isRectUpToDate = false;
	}
	/**
	 * Resets my drawing info and that of all of my ancestors
	 */
	public final void resetDrawingInfoUp()
	{
		// If this element is touched then all ancestry information must be invalidated
		Element ancestor = this;
		do {
			ancestor.resetDrawingInfo();
		} while ((ancestor = ancestor.parent) != null);
	}
	/**
	 * Recursively clears all drawing info this subtree down
	 * (To be overridden by structured sub-classes!)
	 */
	public abstract void resetDrawingInfoDown();
	// END KGU#64 2015-11-03

	// abstract things
	public abstract Rect prepareDraw(Canvas _canvas);
	public abstract void draw(Canvas _canvas, Rect _top_left);
	public abstract Element copy();

        // draw point
        Point drawPoint = new Point(0,0);

        public StringList getCollapsedText()
        {
            StringList sl = new StringList();
            // START KGU#91 2015-12-01: Bugfix #39: This is for drawing, so use switch-sensitive methods
            //if(getText().count()>0) sl.add(getText().get(0));
            if(getText(false).count()>0) sl.add(getText(false).get(0));
            // END KGU#91 2015-12-01
            sl.add(COLLAPSED);
            return sl;
        }
        
        public Point getDrawPoint()
        {
            Element ele = this;
            while(ele.parent!=null) ele=ele.parent;
            return ele.drawPoint;
        }

        public void setDrawPoint(Point point)
        {
            Element ele = this;
            while(ele.parent!=null) ele=ele.parent;
            ele.drawPoint=point;
        }


        public Element()
	{
	}

	public Element(String _string)
	{
		setText(_string);
	}

	public Element(StringList _strings)
	{
		setText(_strings);
	}

	public void setText(String _text)
	{
		// START KGU#91 2015-12-01: Should never set in swapped mode!
		//getText().setText(_text);
		text.setText(_text);
		// END KGU#91 2015-12-01
	}

	public void setText(StringList _text)
	{
		text = _text;
	}

	// START KGU#91 2015-12-01: We need a way to get the true value
	/**
	 * Returns the content of the text field no matter if mode isSwitchedTextAndComment
	 * is active, use getText(boolean) for a mode-sensitive effect.
	 * @return the text StringList (in normal mode) the comment StringList otherwise
	 */
	public StringList getText()
	{
		return text;
	}
	/**
	 * Returns the content of the text field unless _alwaysTrueText is false and
	 * mode isSwitchedTextAndComment is active, in which case the comment field
	 * is returned instead 
	 * @param _alwaysTrueText - if true then mode isSwitchTextAndComment is ignored
	 * @return either the text or the comment
	 */
	public StringList getText(boolean _alwaysTrueText)
	// END KGU#91 2015-12-01
	{
            Root root = null;
            // START KGU#91 2015-12-01: Bugfix #39
    		//if ((root = getRoot(this))!=null && root.isSwitchTextAndComments())
            if (!_alwaysTrueText && 
            		(root = getRoot(this))!=null && root.isSwitchTextAndComments())
            // START KGU#91 2015-12-01
            {
            	return comment;
            }
            else
            {
            	return text;
            }
	}

	public void setComment(String _comment)
	{
		comment.setText(_comment);
	}

	public void setComment(StringList _comment)
	{
		comment = _comment;
	}

	// START KGU#91 2015-12-01: We need a way to get the true value
	/**
	 * Returns the content of the comment field no matter if mode isSwitchedTextAndComment
	 * is active, use getComment(boolean) for a mode-sensitive effect.
	 * @return the text StringList (in normal mode) the comment StringList otherwise
	 */
	public StringList getComment()
	{
		return comment;
	}
	/**
	 * Returns the content of the text field unless _alwaysTrueComment is false and
	 * mode isSwitchedTextAndComment is active, in which case the comment field
	 * is returned instead 
	 * @param _alwaysTrueText - if true then mode isSwitchTextAndComment is ignored
	 * @return either the text or the comment
	 */
	public StringList getComment(boolean _alwaysTrueComment)
	// END KGU#91 2015-12-01
	{
            Root root = null;
            // START KGU#91 2015-12-01: Bugfix #39
      		//if ((root = getRoot(this))!=null && root.isSwitchTextAndComments())
            if (!_alwaysTrueComment && 
            		(root = getRoot(this))!=null && root.isSwitchTextAndComments())
            // END KGU#91 2015-12-01
            {
            	return text;
            }
            else
            {
            	return comment;
            }
	}

	public boolean getSelected()
	{
		return selected;
	}

	public void setSelected(boolean _sel)
	{
		selected=_sel;
	}

	public Color getColor()
	{
		return color;
	}

	public String getHexColor()
	{
		String rgb = Integer.toHexString(color.getRGB());
		return rgb.substring(2, rgb.length());
	}

	public static String getHexColor(Color _color)
	{
		String rgb = Integer.toHexString(_color.getRGB());
		return rgb.substring(2, rgb.length());
	}

	public void setColor(Color _color)
	{
		color = _color;
	}
	
	// START KGU#41 2015-10-13: The highlighting rules are getting complex
	// but are more ore less the same for all kinds of elements
	protected Color getFillColor()
	{
		// This priority might be arguable but represents more or less what was found in the draw methods before
		if (this.waited) {
			return Element.E_WAITCOLOR; 
		}
		else if (this.executed) {
			return Element.E_RUNNINGCOLOR;
		}
		else if (this.selected) {
			return Element.E_DRAWCOLOR;
		}
		else if (this.collapsed) {
			return Element.E_COLLAPSEDCOLOR;
		}
		return getColor();
	}
	// END KGU#41 2015-10-13
	
	// START KGU#43 2015-10-12: Methods to control the new breakpoint property
	public void toggleBreakpoint()
	{
		this.breakpoint = !this.breakpoint;
	}
	
	// Returns whether this Element works as breakpoint on execution
	public boolean isBreakpoint()
	{
		return this.breakpoint;
	}
	
	// 
	/**
	 * Recursively clears all breakpoints in this branch
	 * (To be overridden by structured sub-classes!)
	 */
	public void clearBreakpoints()
	{
		this.breakpoint = false;
	}
	// END KGU#43 2015-10-12

	// START KGU#41 2015-10-13
	/**
	 * Recursively clears all execution flags in this branch
	 * (To be overridden by structured sub-classes!)
	 */
	public void clearExecutionStatus()
	{
		this.executed = false;
		this.waited = false;
	}
	// END KGU#41 2015-10-13

	// START KGU 2015-10-09 Methods selectElementByCoord(int, int) and getElementByCoord(int, int) merged
	/**
	 * Retrieves the smallest (deepest) Element containing coordinate (_x, _y) and flags it as selected
	 * @param _x
	 * @param _y
	 * @return the selected Element (if any)
	 */
	public Element selectElementByCoord(int _x, int _y)
	{
//            Point pt=getDrawPoint();
//
//            if ((rect.left-pt.x<_x)&&(_x<rect.right-pt.x)&&
//                    (rect.top-pt.y<_y)&&(_y<rect.bottom-pt.y))
//            {
//                    return this;
//            }
//            else
//            {
//                    selected=false;
//                    return null;
//            }
		return this.getElementByCoord(_x, _y, true);
	}

	// 
	/**
	 * Retrieves the smallest (deepest) Element containing coordinate (_x, _y)
	 * @param _x
	 * @param _y
	 * @return the (sub-)Element at the given coordinate (if there is none, returns null)
	 */
	public Element getElementByCoord(int _x, int _y)
	{
//            Point pt=getDrawPoint();
//
//            if ((rect.left-pt.x<_x)&&(_x<rect.right-pt.x)&&
//                    (rect.top-pt.y<_y)&&(_y<rect.bottom-pt.y))
//            {
//                    return this;
//            }
//            else
//            {
//                    return null;
//            }
		return this.getElementByCoord(_x, _y, false);
	}

	public Element getElementByCoord(int _x, int _y, boolean _forSelection)
	{
		Point pt=getDrawPoint();

		if ((rect.left-pt.x < _x) && (_x < rect.right-pt.x) &&
				(rect.top-pt.y < _y) && (_y < rect.bottom-pt.y))
		{
			return this;         
		}
		else 
		{
			if (_forSelection)	
			{
				selected = false;	
			}
			return null;    
		}
	}
	// END KGU 2015-10-09
	
	// START KGU 2015-10-11: Helper methods for all Element types' drawing
	
	/**
	 * Draws the marker bar on the left-hand side of the given _rect 
	 * @param _canvas - the canvas to be drawn in
	 * @param _rect - supposed to be the Element's surrounding rectangle
	 */
	protected void drawCommentMark(Canvas _canvas, Rect _rect)
	{
		_canvas.setBackground(E_COMMENTCOLOR);
		_canvas.setColor(E_COMMENTCOLOR);
		
		Rect markerRect = _rect.copy();
		
		markerRect.left += 2;
		if (breakpoint)
		{
			// spare the area of the breakpoint bar
			markerRect.top += 7;
		}
		else
		{
			markerRect.top += 2;
		}
		markerRect.right = markerRect.left+4;
		markerRect.bottom -= 2;
		
		_canvas.fillRect(markerRect);
	}
 
	/**
	 * Draws the marker bar on the top side of the given _rect
	 * @param _canvas - the canvas to be drawn in
	 * @param _rect - the surrounding rectangle of the Element (or relevant part of it)
	 */
	protected void drawBreakpointMark(Canvas _canvas, Rect _rect)
	{
		if (breakpoint) {
			_canvas.setBackground(E_BREAKPOINTCOLOR);
			_canvas.setColor(E_BREAKPOINTCOLOR);

			Rect markerRect = _rect.copy();

			markerRect.left += 2;
			markerRect.top += 2;
			markerRect.right -= 2;
			markerRect.bottom = markerRect.top+4;

			_canvas.fillRect(markerRect);
		}
	}
	// END KGU 2015-10-11

        public Rect getRect()
        {
            return new Rect(rect.left,rect.top,rect.right,rect.bottom);
        }

	public static Font getFont()
	{
		return font;
	}

	public static void setFont(Font _font)
	{
		font=_font;
	}

	/************************
	 * static things
	 ************************/

	public static void loadFromINI()
	{
		try
		{
			Ini ini = Ini.getInstance();
			ini.load();
			// elements
			preAltT=ini.getProperty("IfTrue","V");
			preAltF=ini.getProperty("IfFalse","F");
			preAlt=ini.getProperty("If","()");
			StringList sl = new StringList();
			sl.setCommaText(ini.getProperty("Case","\"?\",\"?\",\"?\",\"sinon\""));
			preCase=sl.getText();
			preFor=ini.getProperty("For","pour ? <- ? \u00E0 ?");
			preWhile=ini.getProperty("While","tant que ()");
			preRepeat=ini.getProperty("Repeat","jusqu'\u00E0 ()");
			// font
			setFont(new Font(ini.getProperty("Name","Dialog"), Font.PLAIN,Integer.valueOf(ini.getProperty("Size","12")).intValue()));
			// colors
			color0=Color.decode("0x"+ini.getProperty("color0","FFFFFF"));
			color1=Color.decode("0x"+ini.getProperty("color1","FF8080"));
			color2=Color.decode("0x"+ini.getProperty("color2","FFFF80"));
			color3=Color.decode("0x"+ini.getProperty("color3","80FF80"));
			color4=Color.decode("0x"+ini.getProperty("color4","80FFFF"));
			color5=Color.decode("0x"+ini.getProperty("color5","0080FF"));
			color6=Color.decode("0x"+ini.getProperty("color6","FF80C0"));
			color7=Color.decode("0x"+ini.getProperty("color7","C0C0C0"));
			color8=Color.decode("0x"+ini.getProperty("color8","FF8000"));
			color9=Color.decode("0x"+ini.getProperty("color9","8080FF"));
		}
		catch (Exception e)
		{
			System.out.println(e);
		}
	}

	public static void saveToINI()
	{
		try
		{
			Ini ini = Ini.getInstance();
			ini.load();
			// elements
			ini.setProperty("IfTrue",preAltT);
			ini.setProperty("IfFalse",preAltF);
			ini.setProperty("If",preAlt);
			StringList sl = new StringList();
			sl.setText(preCase);
			ini.setProperty("Case",sl.getCommaText());
			ini.setProperty("For",preFor);
			ini.setProperty("While",preWhile);
			ini.setProperty("Repeat",preRepeat);
			// font
			ini.setProperty("Name",getFont().getFamily());
			ini.setProperty("Size",Integer.toString(getFont().getSize()));
			// colors
			ini.setProperty("color0", getHexColor(color0));
			ini.setProperty("color1", getHexColor(color1));
			ini.setProperty("color2", getHexColor(color2));
			ini.setProperty("color3", getHexColor(color3));
			ini.setProperty("color4", getHexColor(color4));
			ini.setProperty("color5", getHexColor(color5));
			ini.setProperty("color6", getHexColor(color6));
			ini.setProperty("color7", getHexColor(color7));
			ini.setProperty("color8", getHexColor(color8));
			ini.setProperty("color9", getHexColor(color9));

			ini.save();
		}
		catch (Exception e)
		{
			System.out.println(e);
		}
	}

	public static Root getRoot(Element now)
	{
		while(now.parent!=null)
		{
			now=now.parent;
		}
                if(now instanceof Root)
                    return (Root) now;
                else
                    return null;
	}

	private String cutOut(String _s, String _by)
	{
		System.out.print(_s+" -> ");
		Regex rep = new Regex("(.*?)"+BString.breakup(_by)+"(.*?)","$1\",\""+_by+"\",\"$2");
		_s=rep.replaceAll(_s);
		System.out.println(_s);
		return _s;
	}
	
	// START KGU#18/KGU#23 2015-11-04: Lexical splitter extracted from writeOutVariables
	/**
	 * Splits the given _text into lexical morphemes (lexemes). This will possibly overdo
	 * somewhat (e. g. split float literal 123.45 into "123", ".", "45").
	 * By setting _restoreStrings true, at least string literals can be reassambled again,
	 * consuming more time, of course. 
	 * @param _text - String to be exploded into lexical units
	 * @param _restoreLiterals - if true then accidently split numeric and string literals will be reassembled 
	 * @return StringList consisting ofvthe separated lexemes including isolated spaces etc.
	 */
	public static StringList splitLexically(String _text, boolean _restoreStrings)
	{
		StringList parts = new StringList();
		parts.add(_text);
		
		// split
		parts=StringList.explodeWithDelimiter(parts," ");
		parts=StringList.explodeWithDelimiter(parts,"\t");
		parts=StringList.explodeWithDelimiter(parts,"\n");
		parts=StringList.explodeWithDelimiter(parts,".");
		parts=StringList.explodeWithDelimiter(parts,",");
		parts=StringList.explodeWithDelimiter(parts,";");
		parts=StringList.explodeWithDelimiter(parts,"(");
		parts=StringList.explodeWithDelimiter(parts,")");
		parts=StringList.explodeWithDelimiter(parts,"[");
		parts=StringList.explodeWithDelimiter(parts,"]");
		parts=StringList.explodeWithDelimiter(parts,"-");
		parts=StringList.explodeWithDelimiter(parts,"+");
		parts=StringList.explodeWithDelimiter(parts,"/");
		parts=StringList.explodeWithDelimiter(parts,"*");
		parts=StringList.explodeWithDelimiter(parts,">");
		parts=StringList.explodeWithDelimiter(parts,"<");
		parts=StringList.explodeWithDelimiter(parts,"=");
		parts=StringList.explodeWithDelimiter(parts,":");
		parts=StringList.explodeWithDelimiter(parts,"!");
		parts=StringList.explodeWithDelimiter(parts,"'");
		parts=StringList.explodeWithDelimiter(parts,"\"");

		parts=StringList.explodeWithDelimiter(parts,"\\");
		parts=StringList.explodeWithDelimiter(parts,"%");

		// reassamble symbols
		int i = 0;
		while (i < parts.count())
		{
			if (i < parts.count()-1)
			{
				if (parts.get(i).equals("<") && parts.get(i+1).equals("-"))
				{
					parts.set(i,"<-");
					parts.delete(i+1);
					// START KGU 2014-10-18 potential three-character assignment symbol?
					if (i < parts.count()-1 && parts.get(i+1).equals("-"))
					{
						parts.delete(i+1);
					}
					// END KGU 2014-10-18
				}
				else if (parts.get(i).equals(":") && parts.get(i+1).equals("="))
				{
					parts.set(i,":=");
					parts.delete(i+1);
				}
				else if (parts.get(i).equals("!") && parts.get(i+1).equals("="))
				{
					parts.set(i,"!=");
					parts.delete(i+1);
				}
				// START KGU 2015-11-04
				else if (parts.get(i).equals("=") && parts.get(i+1).equals("="))
				{
					parts.set(i,"==");
					parts.delete(i+1);
				}
				// END KGU 2015-11-04
				else if (parts.get(i).equals("<"))
				{
					if (parts.get(i+1).equals(">"))
					{
						parts.set(i,"<>");
						parts.delete(i+1);
					}
					else if (parts.get(i+1).equals("="))
					{
						parts.set(i,"<=");
						parts.delete(i+1);
					}
					// START KGU#92 2015-12-01: Bugfix #41
					else if (parts.get(i+1).equals("<"))
					{
						parts.set(i,"<<");
						parts.delete(i+1);
					}					
					// END KGU#92 2015-12-01
				}
				else if (parts.get(i).equals(">"))
				{
					if (parts.get(i+1).equals("="))
					{
						parts.set(i,">=");
						parts.delete(i+1);
					}
					// START KGU#92 2015-12-01: Bugfix #41
					else if (parts.get(i+1).equals(">"))
					{
						parts.set(i,">>");
						parts.delete(i+1);
					}					
					// END KGU#92 2015-12-01
				}
				// START KGU#24 2014-10-18: Logical two-character operators should be detected, too ...
				else if (parts.get(i).equals("&") && parts.get(i+1).equals("&"))
				{
					parts.set(i,"&&");
					parts.delete(i+1);
				}
				else if (parts.get(i).equals("|") && parts.get(i+1).equals("|"))
				{
					parts.set(i,"||");
					parts.delete(i+1);
				}
				// END KGU#24 2014-10-18
				// START KGU#26 2015-11-04: Find escaped quotes
				else if (parts.get(i).equals("\\"))
				{
					if (parts.get(i+1).equals("\""))
					{
						parts.set(i, "\\\"");
						parts.delete(i+1);					}
					else if (parts.get(i+1).equals("\\"))
					{
						parts.set(i, "\\\\");
						parts.delete(i+1);					}
				}
				// END KGU#26 2015-11-04
			}
			i++;
		}
		
		if (_restoreStrings)
		{
			String[] delimiters = {"\"", "'"};
			for (int d = 0; d < delimiters.length; d++)
			{
				boolean withinString = false;
				String composed = "";
				i = 0;
				while (i < parts.count())
				{
					String lexeme = parts.get(i);
					if (withinString)
					{
						composed = composed + lexeme;
						if (lexeme.equals(delimiters[d]))
						{
							parts.set(i, composed+"");
							composed = "";
							withinString = false;
							i++;
						}
						else
						{
							parts.delete(i);
						}
					}
					else if (lexeme.equals(delimiters[d]))
					{
						withinString = true;
						composed = lexeme+"";
						parts.delete(i);
					}
					else
					{
						i++;
					}
				}
			}
		}
		return parts;
	}
	// END KGU#18/KGU#23
	
	// START KGU#101 2015-12-11: Enhancement #54: We need to split expression lists (might go to a helper class)
	/**
	 * Splits the _text supposed to represent a list of expressions separated by _listSeparator
	 * into strings representing one of the listed expressions each.
	 * This does not mean mere string splitting but is be aware of string literals, argument lists
	 * of function calls etc. These must not be broken.
	 * @param _text - string containing one or more expressions
	 * @param _listSeparator - a character sequence serving as separator among the expressions (default: ",") 
	 * @return a StringList, each element of which contains one of the separated expressions (order preserved)
	 */
	public static StringList splitExpressionList(String _text, String _listSeparator)
	{
		StringList expressionList = new StringList();
		if (_listSeparator == null) _listSeparator = ",";
		StringList tokens = Element.splitLexically(_text, true);
		
		int parenthDepth = 0;
		int tokenCount = tokens.count();
		String currExpr = "";
		for (int i = 0; i < tokenCount; i++)
		{
			String token = tokens.get(i);
			if (token.equals(_listSeparator) && parenthDepth == 0)
			{
				// store the current expression and start a new one
				expressionList.add(currExpr + "");
				currExpr = new String();
			}
			else
			{ 
				if (token.equals("("))
				{
					parenthDepth++;
				}
				else if (token.equals(")") && parenthDepth > 0)
				{
					parenthDepth--;
				}
				currExpr += token;
			}
		}
		// add the last expression if it's not empty
		if (!currExpr.trim().isEmpty())
		{
			expressionList.add(currExpr + "");
		}
		return expressionList;
	}
	// END KGU#101 2015-12-11

	// START KGU#63 2015-11-03: getWidthOutVariables and writeOutVariables were nearly identical (and had to be!)
	// Now it's two wrappers and a common algorithm -> ought to avoid duplicate work and prevents from divergence
	public static int getWidthOutVariables(Canvas _canvas, String _text, Element _this)
	{
		return writeOutVariables(_canvas, 0, 0, _text, _this, false);
	}

	public static void writeOutVariables(Canvas _canvas, int _x, int _y, String _text, Element _this)
	{
		writeOutVariables(_canvas, _x, _y, _text, _this, true);
	}
	
	private static int writeOutVariables(Canvas _canvas, int _x, int _y, String _text, Element _this, boolean _actuallyDraw)
	// END KGU#63 2015-11-03
	{
		// init total
		int total = 0;

		Root root = getRoot(_this);

		if(root!=null)
		{
			if (root.hightlightVars==true)
			{
				StringList parts = Element.splitLexically(_text, true);

				// bold font
				Font boldFont = new Font(Element.font.getName(),Font.BOLD,Element.font.getSize());
				// backup the original font
				Font backupFont = _canvas.getFont();

				// START KGU#64 2015-11-03: Not to be done again and again. Private static field now!
				//StringList specialSigns = new StringList();
				if (specialSigns == null)	// lazy initialisiation
				{
					specialSigns = new StringList();
					// ENDU KGU#64 2015-11-03
					specialSigns.add(".");
					specialSigns.add("[");
					specialSigns.add("]");
					specialSigns.add("\u2190");
					specialSigns.add(":=");

					specialSigns.add("+");
					specialSigns.add("/");
					// START KGU 2015-11-03: This operator had been missing
					specialSigns.add("%");
					// END KGU 2015-11-03
					specialSigns.add("*");
					specialSigns.add("-");
					specialSigns.add("var");
					specialSigns.add("mod");
					specialSigns.add("div");
					specialSigns.add("<=");
					specialSigns.add(">=");
					specialSigns.add("<>");
					specialSigns.add("<<");
					specialSigns.add(">>");
					specialSigns.add("<");
					specialSigns.add(">");
					specialSigns.add("==");
					specialSigns.add("!=");
					specialSigns.add("=");
					specialSigns.add("!");
					// START KGU#24 2014-10-18
					specialSigns.add("&&");
					specialSigns.add("||");
					specialSigns.add("and");
					specialSigns.add("or");
					specialSigns.add("xor");
					specialSigns.add("not");
					// END KGU#24 2014-10-18

					specialSigns.add("'");
					specialSigns.add("\"");	// KGU 2015-11-12: Quotes alone will hardly occur anymore
				// START KGU#64 2015-11-03: See above
				}
				// END KGU#64 2015-11-03

				// These might have changed by configuration, so don't cache them
				StringList ioSigns = new StringList();
				ioSigns.add(D7Parser.input.trim());
				ioSigns.add(D7Parser.output.trim());
				
				for(int i=0; i < parts.count(); i++)
				{
					String display = parts.get(i);

					display = BString.replace(display, "<-","\u2190");

					if(!display.equals(""))
					{
						// if this part has to be colored
						if(root.variables.contains(display))
						{
							// set color
							_canvas.setColor(Color.decode("0x000099"));
							// set font
							_canvas.setFont(boldFont);
						}
						// if this part has to be colored with special color
						else if(specialSigns.contains(display))
						{
							// set color
							_canvas.setColor(Color.decode("0x990000"));
							// set font
							_canvas.setFont(boldFont);
						}
						// if this part has to be colored with io color
						else if(ioSigns.contains(display))
						{
							// set color
							_canvas.setColor(Color.decode("0x007700"));
							// set font
							_canvas.setFont(boldFont);
						}
						// START KGU 2015-11-12
						// if it's a String or Character literal color it as such
						else if (display.startsWith("\"") && display.endsWith("\"") ||
								display.startsWith("'") && display.endsWith("'"))
						{
							// set colour
							_canvas.setColor(Color.decode("0x770077"));
						}
						// END KGU 2015-11-12
					}

					if (_actuallyDraw)
					{
						// write out text
						_canvas.writeOut(_x + total, _y, display);
					}

					// add to the total
					total += _canvas.stringWidth(display);

					// reset color
					_canvas.setColor(Color.BLACK);
					// reset font
					_canvas.setFont(backupFont);

				}
				//System.out.println(parts.getCommaText());
			}
			else
			{
				if (_actuallyDraw)
				{
					_canvas.writeOut(_x + total, _y, _text);
				}

                // add to the total
                total += _canvas.stringWidth(_text);

			}
		}
		
		return total;
	}



    public boolean isCollapsed() {
        return collapsed;
    }

    public void setCollapsed(boolean collapsed) {
        this.collapsed = collapsed;
    }

    // START KGU 2015-10-16: Some Root stuff properly delegated to the Element subclasses
    // (The obvious disadvantage is slightly reduced performance, of course)
    /**
     * Returns the serialised texts held within this element and its substructure.
     * The argument _instructionsOnly controls whether mere expressions like logical conditions or
     * even call statements are included. As a rule, no lines that may not potentially introduce new
     * variables are added if true (which not only reduces time and space requirements but also avoids
     * "false positives" in variable detection). 
     * Uses addFullText() - so possibly better override that method if necessary.
     * @param _instructionsOnly - if true then only the texts of Instruction elements are included
     * @return the composed StringList
     */
    public StringList getFullText(boolean _instructionsOnly)
    {
    	// The default...
    	StringList sl = new StringList();
    	this.addFullText(sl, _instructionsOnly);
    	return sl;
    }
    
    /**
     * Appends all the texts held within this element and its substructure to the given StringList.
     * The argument _instructionsOnly controls whether mere expressions like logical conditions or
     * even call statements are included. As a rule, no lines that may not potentially introduce new
     * variables are added if true (which not only reduces time and space requirements but also avoids
     * "false positives" in variable detection). 
     * (To be overridden by structured subclasses)
     * @param _lines - the StringList to append to 
     * @param _instructionsOnly - if true then texts not possibly containing variable declarations are omitted
     */
    protected abstract void addFullText(StringList _lines, boolean _instructionsOnly);
    // END KGU 2015-10-16
    
    // START KGU#18/KGU#23 2015-10-24 intermediate transformation added and decomposed
    /**
     * Converts the operator symbols accepted by Structorizer into padded Java operators
     * (note the surrounding spaces - no double spaces will exist):
     * - Assignment:		" <- "
     * - Comparison:		" == ", " < ", " > ", " <= ", " >= ", " != "
     * - Logic:				" && ", " || ", " ! ", " ^ "
     * - Arithmetics:		" div " and usual Java operators with or without padding
     * @param _expression an Element's text in practically unknown syntax
     * @return an equivalent of the _expression String with replaced operators
     */
    public static String unifyOperators(String _expression)
    {
    	return unifyOperators(_expression, false);
    }
    
    // START KGU#18/KGU#23 2015-10-24 intermediate transformation added and decomposed
    /**
     * Converts the operator symbols accepted by Structorizer into padded Java operators
     * (note the surrounding spaces - no double spaces will exist):
     * - Assignment:		" <- "
     * - Comparison*:		" == ", " < ", " > ", " <= ", " >= ", " != "
     * - Logic*:			" && ", " || ", " ! ", " ^ "
     * - Arithmetics*:		" div " and usual Java operators without padding (e. g. " mod " -> " % ")
     * @param _expression an Element's text in practically unknown syntax
     * @param _assignmentOnly if true then only assignment operator will be unified
     * @return an equivalent of the _expression String with replaced operators
     */
    public static String unifyOperators(String _expression, boolean _assignmentOnly)
    {
    	
        String interm = _expression.trim();	// KGU#54
        // variable assignment
        interm = interm.replace("<--", " §ASGN§ ");
        interm = interm.replace("<-", " §ASGN§ ");
        interm = interm.replace(":=", " §ASGN§ ");
        
        if (!_assignmentOnly)
        {
        	// testing
        	interm = interm.replace("!=", " §UNEQ§ ");
        	interm = interm.replace("==", " §EQU§ ");
        	interm = interm.replace("<=", " §LE§ ");
        	interm = interm.replace(">=", " §GE§ ");
        	interm = interm.replace("<>", " §UNEQ§ ");
        	// START KGU#92 2015-12-01: Bugfix #41
        	interm = interm.replace("<<", " §SHL§ ");
        	interm = interm.replace(">>", " §SHR§ ");
        	// END KGU#92 2015-12-01
        	interm = interm.replace("<", " < ");
        	interm = interm.replace(">", " > ");
        	interm = interm.replace("=", " §EQU§ ");

        	// Parenthesis/bracket padding as preparation for the following replacements
        	interm = interm.replace(")", " ) ");
        	interm = interm.replace("(", "( ");
        	interm = interm.replace("]", "] ");	// Do NOT pad '[' (would spoil the array detection)
        	// arithmetics and signs
        	interm = interm.replace("+", " +");	// Fortunately, ++ isn't accepted as working operator by the Structorizer
        	interm = interm.replace("-", " -");	// Fortunately, -- isn't accepted as working operator by the Structorizer
        	//interm = interm.replace(" div "," / ");	// We must still distinguish integer division
        	interm = interm.replace(" mod ", " % ");
        	interm = interm.replace(" MOD ", " % ");
        	interm = interm.replace(" mod(", " % (");
        	interm = interm.replace(" MOD(", " % (");
        	interm = interm.replace(" div(", " div (");
        	interm = interm.replace(" DIV ", " div ");
        	interm = interm.replace(" DIV(", " div (");
        	// START KGU#92 2015-12-01: Bugfix #41
        	interm = interm.replace(" shl ", " §SHL§ ");
        	interm = interm.replace(" shr ", " §SHR§ ");
        	interm = interm.replace(" SHL ", " §SHL§ ");
        	interm = interm.replace(" SHR ", " §SHR§ ");
        	// END KGU#92 2015-12-01
        	// Logic
        	interm = interm.replace( "&&", " && ");
        	interm = interm.replace( "||", " || ");
        	interm = interm.replace( " and ", " && ");
        	interm = interm.replace( " AND ", " && ");
        	interm = interm.replace( " and(", " && (");
        	interm = interm.replace( " AND(", " && (");
        	interm = interm.replace( " or ", " || ");
        	interm = interm.replace( " OR ", " || ");
        	interm = interm.replace( " or(", " || (");
        	interm = interm.replace( " OR(", " || (");
        	interm = interm.replace( " not ", " §NOT§ ");
        	interm = interm.replace( " NOT ", " §NOT§ ");
        	interm = interm.replace( " not(", " §NOT§ (");
        	interm = interm.replace( " NOT(", " §NOT§ (");
        	String lower = interm.toLowerCase();
        	if (lower.startsWith("not ") || lower.startsWith("not(")) {
        		interm = " §NOT§ " + interm.substring(3);
        	}
        	interm = interm.replace( "!", " §NOT§ ");
        	interm = interm.replace( " xor ", " ^ ");	// Might cause some operator preference trouble
        	interm = interm.replace( " XOR ", " ^ ");	// Might cause some operator preference trouble
        }

        String unified = interm.replace(" §ASGN§ ", " <- ");
        if (!_assignmentOnly)
        {
        	unified = unified.replace(" §EQU§ ", " == ");
        	unified = unified.replace(" §UNEQ§ ", " != ");
        	unified = unified.replace(" §LE§ ", " <= ");
        	unified = unified.replace(" §GE§ ", " >= ");
        	unified = unified.replace(" §NOT§ ", " ! ");
        	// START KGU#92 2015-12-01: Bugfix #41
        	unified = unified.replace(" §SHL§ ", " << ");
        	unified = unified.replace(" §SHR§ ", " >> ");
        	// END KGU#92 2015-12-01
        }
        unified = BString.replace(unified, "  ", " ");	// shrink multiple blanks
        unified = BString.replace(unified, "  ", " ");	// do it again to catch odd-numbered blanks as well
        
        return unified;
    }

	// START KGU#92 2015-12-01: Bugfix #41 Okay now, here is the new approach (still a sketch)
    /**
     * Converts the operator symbols accepted by Structorizer into intermediate operators
     * (mostly Java operators), mostly padded:
     * - Assignment:		" <- "
     * - Comparison*:		" == ", " < ", " > ", " <= ", " >= ", " != "
     * - Logic*:			" && ", " || ", " ! ", " ^ "
     * - Arithmetics*:		" div " and usual Java operators (e. g. " mod " -> " % ")
     * @param _tokens a tokenised line of an Element's text (in practically unknown syntax)
     * @param _assignmentOnly if true then only assignment operator will be unified
     * @return an equivalent of the _expression String with replaced operators
     */
    public static int unifyOperators(StringList _tokens, boolean _assignmentOnly)
    {
    	int count = 0;
        count += _tokens.replaceAll(":=", " <- ");
        if (_assignmentOnly)
        {
        	count += _tokens.replaceAll("=", " == ");
        	count += _tokens.replaceAll("<", " < ");
        	count += _tokens.replaceAll(">", " > ");
        	count += _tokens.replaceAll("<=", " <= ");
        	count += _tokens.replaceAll(">=", " >= ");
        	count += _tokens.replaceAll("<>", " != ");
        	count += _tokens.replaceAll("%", " % ");
        	count += _tokens.replaceAllCi("mod", " % ");
        	count += _tokens.replaceAllCi("div", " div ");
        	count += _tokens.replaceAllCi("shl", " << ");
        	count += _tokens.replaceAllCi("shr", " >> ");
        	count += _tokens.replaceAllCi("and", " && ");
        	count += _tokens.replaceAllCi("or", " || ");
        	count += _tokens.replaceAllCi("not", " ! ");
        	count += _tokens.replaceAllCi("xor", " ^ ");
        }
    	return count;
    }
	// END KGU#92 2015-12-01

    /**
     * Returns a (hopefully) lossless representation of the stored text as a
     * StringList in a common intermediate language (code generation phase 1).
     * This allows the language-specific Generator subclasses to concentrate on the translation
     * into their respective target languages (code generation phase 2).
     * Conventions of the intermediate language:
     * Operators (note the surrounding spaces - no double spaces will exist):
     * - Assignment:		" <- "
     * - Comparison:		" = ", " < ", " > ", " <= ", " >= ", " <> "
     * - Logic:				" && ", " || ", " §NOT§ ", " ^ "
     * - Arithmetics:		usual Java operators without padding
     * - Control key words:
     * -	If, Case:		none (wiped off)
     * -	While, Repeat:	none (wiped off)
     * -	For:			unchanged
     * -	Forever:		none (wiped off)
     * 
     * @return a padded intermediate language equivalent of the stored text
     */
    
    public StringList getIntermediateText()
    {
    	StringList interSl = new StringList();
    	for (int i = 0; i < text.count(); i++)
    	{
    		interSl.add(transformIntermediate(text.get(i)));
    	}
    	return interSl;
    }
    
    /**
     * Creates a (hopefully) lossless representation of the _text String as a
     * line of a common intermediate language (code generation phase 1).
     * This allows the language-specific Generator subclasses to concentrate on the translation into their
     * target language (code generation phase 2).
     * Conventions of the intermediate language:
     * Operators (note the surrounding spaces - no double spaces will exist):
     * - Assignment:		" <- "
     * - Comparison:		" = ", " < ", " > ", " <= ", " >= ", " <> "
     * - Logic:				" && ", " || ", " §NOT§ ", " ^ "
     * - Arithmetics:		usual Java operators without padding
     * - Control key words:
     * -	If, Case:		none (wiped off)
     * -	While, Repeat:	none (wiped off)
     * -	For:			unchanged
     * -	Forever:		none (wiped off)
     * 
     * @return a padded intermediate language equivalent of the stored text
     */
    public static String transformIntermediate(String _text)
    {
    	//final String regexMatchers = ".?*+[](){}\\^$";
    	
    	// Collect redundant placemarkers to be deleted from the text
        StringList redundantMarkers = new StringList();
        redundantMarkers.addByLength(D7Parser.preAlt);
        redundantMarkers.addByLength(D7Parser.preCase);
        //redundantMarkers.addByLength(D7Parser.preFor);	// will be handled separately
        redundantMarkers.addByLength(D7Parser.preWhile);
        redundantMarkers.addByLength(D7Parser.preRepeat);

        redundantMarkers.addByLength(D7Parser.postAlt);
        redundantMarkers.addByLength(D7Parser.postCase);
        //redundantMarkers.addByLength(D7Parser.postFor);	// will be handled separately
        //redundantMarkers.addByLength(D7Parser.stepFor);	// will be handled separately
        redundantMarkers.addByLength(D7Parser.postWhile);
        redundantMarkers.addByLength(D7Parser.postRepeat);
       
        String interm = " " + _text + " ";

        //System.out.println(interm);
        // Now, we eliminate redundant keywords according to the Parser configuration
        // Unfortunately, regular expressions are of little use here, because the prefix and infix keywords may
        // consist of or contain Regex matchers like '?' and hence aren't suitable as part of the pattern
        // The harmful characters to be inhibited or masked are: .?*+[](){}\^$
        //System.out.println(interm);
        for (int i=0; i < redundantMarkers.count(); i++)
        {
        	String marker = redundantMarkers.get(i);
        	if (!marker.isEmpty())
        	{
        		// If the marker has not been padded then we must care for proper isolation
        		if (marker.equals(marker.trim()))
        		{
        			int len = marker.length();
        			int pos = 0;
        			while ((pos = interm.indexOf(marker, pos)) >= 0)
        			{
        				if (!Character.isJavaIdentifierPart(interm.charAt(pos-1)) &&
        						(pos + len) < interm.length() &&
        						!Character.isJavaIdentifierPart(interm.charAt(pos + len)))
        				{
        					interm = interm.substring(0, pos) + interm.substring(pos + len);
        				}
        			}
        		}
        		else
        		{
        			// Already padded, so just replace it everywhere
        			interm = interm.replace( marker, ""); 
        		}
        		interm = " " + interm + " ";	// Ensure the string being padded for easier matching
                interm = interm.replace("  ", " ");
        		//System.out.println("transformIntermediate: " + interm);	// FIXME (KGU): Remove or deactivate after test!
        	}
        }
        
        interm = unifyOperators(interm);

		// START KGU 2015-11-30: Adopted from Root.getVarNames(): 
        // pascal: convert "inc" and "dec" procedures
        // (Of course we could omit it for Pascal, and for C offsprings there are more efficient translations, but this
        // works for all, and so we avoid trouble. 
        Regex r;
        r = new Regex(BString.breakup("inc")+"[(](.*?)[,](.*?)[)](.*?)","$1 <- $1 + $2"); interm = r.replaceAll(interm);
        r = new Regex(BString.breakup("inc")+"[(](.*?)[)](.*?)","$1 <- $1 + 1"); interm = r.replaceAll(interm);
        r = new Regex(BString.breakup("dec")+"[(](.*?)[,](.*?)[)](.*?)","$1 <- $1 - $2"); interm = r.replaceAll(interm);
        r = new Regex(BString.breakup("dec")+"[(](.*?)[)](.*?)","$1 <- $1 - 1"); interm = r.replaceAll(interm);
        // END KGU 2015-11-30
        
        // Reduce multiple space characters
        interm = interm.replace("  ", " ");
        interm = interm.replace("  ", " ");	// By repetition we eliminate the remnants of odd-number space sequences

        return interm/*.trim()*/;
    }
    
    // END KGU#18/KGU#23 2015-10-24
       
}
