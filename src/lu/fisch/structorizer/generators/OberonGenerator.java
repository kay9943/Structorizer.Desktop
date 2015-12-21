/*
    This file is part of Structorizer.

    Structorizer is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Structorizer is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

 ***********************************************************************

    OBERON Source Code Generator

    Copyright (C) 2008 Klaus-Peter Reimers

    This file has been released under the terms of the GNU General
    Public License as published by the Free Software Foundation.

 */

package lu.fisch.structorizer.generators;

/******************************************************************************************************
 *
 *      Author:         Klaus-Peter Reimers
 *
 *      Description:    This class generates Oberon code.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author					Date			Description
 *      ------                      		----			-----------
 *      Klaus-Peter Reimers                     2008.01.08              First Issue
 *      Bob Fisch				2008.01.08		Modified "private String transform(String _input)"
 *      Bob Fisch				2008.04.12		Added "Fields" section for generator to be used as plugin
 *      Bob Fisch				2008.08.14		Added declaration output. A comment line in the root element
 *												with a "#" is ignored. All other lines are written to the code.
 *      Bob Fisch				2011.11.07		Fixed an issue while doing replacements
 *      Kay Gürtzig				2014.11.10		Operator conversion modified (see comment)
 *      Kay Gürtzig				2014.11.16		Operator conversion corrected (see comment)
 *      Kay Gürtzig				2014.12.02		Additional replacement of long assignment operator "<--" by "<-"
 *      Kay Gürtzig				2015.10.18		Indentation issue fixed and comment generation revised
 *
 ******************************************************************************************************
 *
 *      Comment:		Based on "PasGenerator.java" from Bob Fisch
 *      
 *      2015.10.18 - Bugfix / Code revision (Kay Gürtzig)
 *      - Indentation had worked in an exponential way (duplicated every level: _indent+_indent)
 *      - Interface of comment insertion methods modified
 *      
 *      2014.11.16 - Bugfix / Enhancements
 *      - operator conversion had to be adjusted to comply with Oberon2 syntax
 *      - case structure wasn't properly exported
 *      - comment export inserted
 *
 *      2014.11.10 - Enhancement
 *      - Conversion of C-style logical operators to the Pascal-like ones added
 *      - assignment operator conversion now preserves or ensures surrounding spaces
 *
 ******************************************************************************************************///

import java.util.regex.Matcher;

import lu.fisch.utils.*;
import lu.fisch.structorizer.parsers.*;
import lu.fisch.structorizer.elements.*;

public class OberonGenerator extends Generator {
	
	/************ Fields ***********************/
	protected String getDialogTitle()
	{
		return "Export Oberon Code ...";
	}
	
	protected String getFileDescription()
	{
		return "Oberon Source Code";
	}
	
	protected String getIndent()
	{
		return "  ";
	}
	
	protected String[] getFileExtensions()
	{
		String[] exts = {"Mod"};
		return exts;
	}

    // START KGU 2015-10-18: New pseudo field
    @Override
    protected String commentSymbolLeft()
    {
    	return "(*";
    }

    @Override
    protected String commentSymbolRight()
    {
    	return "*)";
    }
    // END KGU 2015-10-18
	
	// START KGU#78 2015-12-18: Enh. #23 We must know whether to create labels for simple breaks
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#supportsSimpleBreak()
	 */
	@Override
	protected boolean supportsSimpleBreak()
	{
		return true;
	}
	// END KGU#78 2015-12-18

    
    /************ Code Generation **************/

	// START KGU#18/KGU#23 2015-11-01 Transformation decomposed
	/**
	 * A pattern how to embed the variable (right-hand side of an input instruction)
	 * into the target code
	 * @return a regex replacement pattern, e.g. "$1 = (new Scanner(System.in)).nextLine();"
	 */
	protected String getInputReplacer()
	{
		return "In.TYPE($1)";
	}

	/**
	 * A pattern how to embed the expression (right-hand side of an output instruction)
	 * into the target code
	 * @return a regex replacement pattern, e.g. "System.out.println($1);"
	 */
	protected String getOutputReplacer()
	{
		return "Out.TYPE($1)";
	}

	// START KGU#16 2015-11-30
	/**
	 * Transforms type identifier into the target language (as far as possible)
	 * @param _type - a string potentially meaning a datatype (or null)
	 * @param _default - a default string returned if _type happens to be null
	 * @return a type identifier (or the unchanged _type value if matching failed)
	 */
	protected String transformType(String _type, String _default) {
		if (_type == null)
			_type = _default;
		else {
			_type = _type.trim();
			if (_type.equalsIgnoreCase("long") ||
					_type.equalsIgnoreCase("unsigned long")) _type = "LONGINT";
			else if (_type.equalsIgnoreCase("int") ||
					_type.equalsIgnoreCase("unsigned") ||
					_type.equalsIgnoreCase("unsigned int")) _type = "INTEGER";
			else if (_type.equalsIgnoreCase("short") ||
					_type.equalsIgnoreCase("unsigned short") ||
					_type.equalsIgnoreCase("unsigned char")) _type = "SHORTINT";
			else if (_type.equalsIgnoreCase("char") ||
					_type.equalsIgnoreCase("character")) _type = "CHAR";
			else if (_type.equalsIgnoreCase("float") ||
					_type.equalsIgnoreCase("single") ||
					_type.equalsIgnoreCase("real")) _type = "REAL";
			else if (_type.equalsIgnoreCase("double") ||
					_type.equalsIgnoreCase("longreal")) _type = "LONGREAL";
			else if (_type.equalsIgnoreCase("bool")) _type = "BOOLEAN";
			else if (_type.equalsIgnoreCase("string")) _type = "ARRAY 100 OF CHAR"; // may be too short but how can we guess?
			// To be continued if required...
		}
		return _type;
	}
	// END KGU#16 2015-11-30	

	/**
	 * Transforms assignments in the given intermediate-language code line.
	 * Replaces "<-" by ":=" here
	 * @param _interm - a code line in intermediate syntax
	 * @return transformed string
	 */
	protected String transformAssignment(String _interm)
	{
		return _interm.replace(" <- ", " := ");
	}
	// END KGU#18/KGU#23 2015-11-01
    
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#transform(java.lang.String, boolean)
	 */
	@Override
	protected String transform(String _input, boolean _doInputOutput)
	{
		// START KGU#18/KGU#23 2015-11-02
		_input = super.transform(_input, _doInputOutput);
		// END KGU#18/KGU#23 2015-11-02
		// START KGU 2014-11-16: Comparison operator had to be converted properly first
        _input=BString.replace(_input," == "," = ");
        _input=BString.replace(_input," != "," # ");
        _input=BString.replace(_input," <> "," # ");
        // C and Pascal division operators
        _input=BString.replace(_input," div "," DIV ");
        _input=BString.replace(_input," % "," MOD ");
        // logical operators required transformation, too
        _input=BString.replace(_input," && "," & ");
        _input=BString.replace(_input," || "," OR ");
        _input=BString.replace(_input," ! "," ~ ");
        _input=BString.replace(_input,"!"," ~ ");
        // END KGU 2014-11-16
            
		return _input.trim();
	}
	
	protected void generateCode(Instruction _inst, String _indent)
	{
    	// START KGU 2015-10-18: The "export instructions as comments" configuration had been ignored here
//		insertComment(_inst, _indent);
//		for(int i=0;i<_inst.getText().count();i++)
//		{
//			code.add(_indent+transform(_inst.getText().get(i))+";");
//		}
		if (!insertAsComment(_inst, _indent)) {
			
			insertComment(_inst, _indent);

			for (int i=0; i<_inst.getText().count(); i++)
			{
				// START KGU#101/KGU#108 2015-12-20 Issue #51/#54
				//code.add(_indent+transform(_inst.getText().get(i))+";");
				String line = _inst.getText().get(i);
				String matcherInput = "^" + Matcher.quoteReplacement(D7Parser.input);
				String matcherOutput = "^" + Matcher.quoteReplacement(D7Parser.output);
				if (Character.isJavaIdentifierPart(D7Parser.input.charAt(D7Parser.input.length()-1))) { matcherInput += "[ ]"; }
				if (Character.isJavaIdentifierPart(D7Parser.output.charAt(D7Parser.output.length()-1))) { matcherOutput += "[ ]"; }
				boolean isInput = (line.trim()+" ").matches(matcherInput + "(.*)");			// only non-empty input instructions relevant  
				boolean isOutput = (line.trim()+" ").matches(matcherOutput + "(.*)"); 	// also empty output instructions relevant
				if (isInput)
				{
					code.add(_indent + "In.Open;");
					if (line.substring(D7Parser.input.trim().length()).trim().isEmpty())
					{
						code.add(_indent + "In.Char(dummyInputChar);");
					}
					else
					{	
						insertComment("TODO: Replace \"TYPE\" by the the actual data type name!", _indent);
						code.add(_indent + transform(line) + ";");
					}
				}
				else if (isOutput)
				{
					insertComment("TODO: Replace \"TYPE\" by the the actual data type name and add a length argument where needed!", _indent);	
					StringList expressions = Element.splitExpressionList(line.substring(D7Parser.output.length()).trim(), ",");
					// Produce an output isntruction for every expression (according to type)
					for (int j = 0; j < expressions.count(); j++)
					{
						code.add(_indent + transform(D7Parser.output + " " + expressions.get(j)) + ";");
					}
					code.add(_indent + "Out.Ln;");
				}
				else
				{
					code.add(_indent + transform(line) + ";");
				}
				// END KGU#101/KGU#108 2015-12-20
			}

		}
		// END KGU 2015-10-18
	}
	
	protected void generateCode(Alternative _alt, String _indent)
	{
        // START KGU 2014-11-16
        insertComment(_alt, _indent);
        // END KGU 2014-11-16
		code.add(_indent+"IF "+ transform(_alt.getText().getLongString()) + " THEN");
		generateCode(_alt.qTrue, _indent+this.getIndent());
		if (_alt.qFalse.getSize()!=0)
		{
			code.add(_indent+"END");
			code.add(_indent+"ELSE");
			generateCode(_alt.qFalse,_indent+this.getIndent());
		}
		code.add(_indent+"END;");
	}
	
	protected void generateCode(Case _case, String _indent)
	{
        // START KGU 2014-11-16
        insertComment(_case, _indent);
        // END KGU 2014-11-16
		code.add(_indent+"CASE "+transform(_case.getText().get(0))+" OF");
		
		for(int i=0;i<_case.qs.size()-1;i++)
		{
			code.add(_indent+this.getIndent()+_case.getText().get(i+1).trim()+":");
			generateCode((Subqueue) _case.qs.get(i),_indent+this.getIndent());
			// START KGU 2014-11-16: Wrong case separator replaced
			//code.add(_indent+"END;");
			code.add(_indent+"|");
			// END KGU 2014-11-16
		}
		
		if(!_case.getText().get(_case.qs.size()).trim().equals("%"))
		{
			code.add(_indent+this.getIndent()+"ELSE");
			generateCode((Subqueue) _case.qs.get(_case.qs.size()-1),_indent+this.getIndent()+this.getIndent());
		}
		// START KGU 2014-11-16: Wrong indentation mended
		//code.add(_indent+this.getIndent()+"END;");
		code.add(_indent+"END;");
		// END KGU 2014-11-16
	}
	
	protected void generateCode(For _for, String _indent)
	{
        // START KGU 2014-11-16
        insertComment(_for, _indent);
        // END KGU 2014-11-16
        // START KGU#3 2015-11-02: New reliable loop parameter mechanism
		//code.add(_indent+"FOR "+BString.replace(transform(_for.getText().getText()),"\n","")+" DO");
        int step = _for.getStepConst();
        String incr = (step == 1) ? "" : " BY "+ step;
		code.add(_indent + "FOR " + _for.getCounterVar() + " := " + transform(_for.getStartValue(), false) +
				" TO " + transform(_for.getEndValue(), false) + incr +" DO");
		// END KGU#3 2015-11-02
		generateCode(_for.q,_indent+this.getIndent());
		code.add(_indent+"END;");
	}
	
	protected void generateCode(While _while, String _indent)
	{
        // START KGU 2014-11-16
        insertComment(_while, _indent);
        // END KGU 2014-11-16
		code.add(_indent+"WHILE "+BString.replace(transform(_while.getText().getText()),"\n","")+" DO");
		generateCode(_while.q, _indent + this.getIndent());
		code.add(_indent+"END;");
	}
	
	protected void generateCode(Repeat _repeat, String _indent)
	{
        // START KGU 2014-11-16
        insertComment(_repeat, _indent);
        // END KGU 2014-11-16
		code.add(_indent+"REPEAT");
		generateCode(_repeat.q,_indent+this.getIndent());
		code.add(_indent+"UNTIL "+BString.replace(transform(_repeat.getText().getText()),"\n","")+";");
	}
	
	protected void generateCode(Forever _forever, String _indent)
	{
        // START KGU 2014-11-16
        insertComment(_forever, _indent);
        // END KGU 2014-11-16
		code.add(_indent+"LOOP");
		generateCode(_forever.q,_indent+this.getIndent());
		code.add(_indent+"END;");
	}
	
	protected void generateCode(Call _call, String _indent)
	{
        // START KGU 2014-11-16
        insertComment(_call, _indent);
        // END KGU 2014-11-16
		for(int i=0;i<_call.getText().count();i++)
		{
			code.add(_indent+transform(_call.getText().get(i))+";");
		}
	}
	
	protected void generateCode(Jump _jump, String _indent)
	{
        // START KGU 2014-11-16
        insertComment(_jump, _indent);
        // END KGU 2014-11-16
        
        // TODO: EXIT (= break) and RETURN exist, no further jump allowed
		for(int i=0;i<_jump.getText().count();i++)
		{
			code.add(_indent+transform(_jump.getText().get(i))+";");
		}
	}
	
	// START KGU#47 2015-12-20: Offer at least a sequential execution (which is one legal execution order)
	protected void generateCode(Parallel _para, String _indent)
	{
		insertComment(_para, _indent);

		code.add("");
		insertComment("==========================================================", _indent);
		insertComment("================= START PARALLEL SECTION =================", _indent);
		insertComment("==========================================================", _indent);
		insertComment("TODO: add the necessary code to run the threads concurrently", _indent);
		code.add(_indent + "BEGIN");

		for (int i = 0; i < _para.qs.size(); i++) {
			code.add("");
			insertComment("----------------- START THREAD " + i + " -----------------", _indent + this.getIndent());
			code.add(_indent + this.getIndent() + "BEGIN");
			generateCode((Subqueue) _para.qs.get(i), _indent + this.getIndent() + this.getIndent());
			code.add(_indent + this.getIndent() + "END;");
			insertComment("------------------ END THREAD " + i + " ------------------", _indent + this.getIndent());
			code.add("");
		}

		code.add(_indent + "END;");
		insertComment("==========================================================", _indent);
		insertComment("================== END PARALLEL SECTION ==================", _indent);
		insertComment("==========================================================", _indent);
		code.add("");
	}
	// END KGU#47 2015-12-20

//	protected void generateCode(Subqueue _subqueue, String _indent)
//	{
//		// code.add(_indent+"");
//		for(int i=0;i<_subqueue.getSize();i++)
//		{
//			generateCode((Element) _subqueue.getElement(i),_indent);
//		}
//		// code.add(_indent+"");
//	}
	
	// START KGU 2015-12-20: Decomposition accoring to super class Generator
//	public String generateCode(Root _root, String _indent)
//	{
//		String pr = "MODULE";
//		String modname = _root.getText().get(0);
//		if(_root.isProgram==false) {pr="PROCEDURE";}
//		
//		code.add(pr+" "+modname+";");
//		code.add("");
//
//		// Add comments and/or declarations to the program (Bob)
//		for(int i=0;i<_root.getComment().count();i++)
//		{
//			if(!_root.getComment().get(i).startsWith("#"))
//			{
//				code.add(_root.getComment().get(i));
//			}
//	        // START KGU 2014-11-16: Don't get the comments get lost
//			else {
//				insertComment(_root.getComment().get(i).substring(1), "");
//			}
//	        // END KGU 2014-11-16
//			
//		}
//		
//		//code.add("// declare your variables here");
//		code.add("");
//		code.add("BEGIN");
//		generateCode(_root.children,_indent+this.getIndent());
//		code.add("END "+modname+".");
//		
//		return code.getText();
//	}
	
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#generateHeader(lu.fisch.structorizer.elements.Root, java.lang.String, java.lang.String, lu.fisch.utils.StringList, lu.fisch.utils.StringList, java.lang.String)
	 */
	@Override
	protected String generateHeader(Root _root, String _indent, String _procName,
			StringList _paramNames, StringList _paramTypes, String _resultType)
	{
		String header = (_root.isProgram ? "MODULE " : "PROCEDURE ") + _procName;
		if (!_root.isProgram)
		{
			header += "*";	// Marked for export as default
			String lastType = "";
			int nParams = _paramNames.count();
			for (int p = 0; p < nParams; p++) {
				String type = transformType(_paramTypes.get(p), "(*type?*)");
				if (p == 0) {
					header += "(";
				}
				else if (type.equals("(*type?*)") || !type.equals(lastType)) {
					header += ": " + lastType + "; ";
				}
				else {
					header += ", ";
				}
				header += _paramNames.get(p).trim();
				if (p+1 == nParams) {
					header += ": " + type + ")";
				}
				lastType = type;
			}
			if (_resultType != null || this.returns || this.isFunctionNameSet || this.isResultSet)
			{
				header += ": " + transformType(_resultType, "");
			}
		}
		
		code.add(_indent + header + ";");

		// START KGU 2015-12-20: Don't understand what this was meant to achieve
		// Add comments and/or declarations to the program (Bob)
//		for(int i=0; i<_root.getComment().count(); i++)
//		{
//			if(!_root.getComment().get(i).startsWith("#"))
//			{
//				code.add(_indent + _root.getComment().get(i));
//			}
//	        // START KGU 2014-11-16: Don't get the comments get lost
//			else {
//				insertComment(_root.getComment().get(i).substring(1), _indent);
//			}
//	        // END KGU 2014-11-16
//		}
		insertBlockComment(_root.getComment(), _indent, this.commentSymbolLeft(),
				" * ", " " + this.commentSymbolRight());
		// END KGU 2015-12-20

		return _indent;
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#generatePreamble(lu.fisch.structorizer.elements.Root, java.lang.String, lu.fisch.utils.StringList)
	 */
	@Override
	protected String generatePreamble(Root _root, String _indent, StringList varNames)
	{
		String indentPlusOne = _indent + this.getIndent();
		code.add(_indent + "VAR");
		insertComment("TODO: Declare and initialise local variables here:", indentPlusOne);
		code.add(indentPlusOne + "dummyInputChar: Char;	" +
				this.commentSymbolLeft() + " for void input " + this.commentSymbolRight());
		for (int v = 0; v < varNames.count(); v++) {
			insertComment(varNames.get(v), indentPlusOne);
		}
		code.add(_indent + "BEGIN");
		return indentPlusOne;
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#generateResult(lu.fisch.structorizer.elements.Root, java.lang.String, boolean, lu.fisch.utils.StringList)
	 */
	@Override
	protected String generateResult(Root _root, String _indent, boolean alwaysReturns, StringList varNames)
	{
		if ((this.returns || _root.getResultType() != null || isFunctionNameSet || isResultSet) && !alwaysReturns) {
			String result = "0";
			if (isFunctionNameSet) {
				result = _root.getMethodName();
			} else if (isResultSet) {
				int vx = varNames.indexOf("result", false);
				result = varNames.get(vx);
			}
			code.add(_indent);
			code.add(_indent + this.getIndent() + "RETURN " + result + ";");
		}
		return _indent;
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#generateFooter(lu.fisch.structorizer.elements.Root, java.lang.String)
	 */
	@Override
	protected void generateFooter(Root _root, String _indent)
	{
		// Method block close
		code.add(_indent + "END " + _root.getMethodName() + ";");

		super.generateFooter(_root, _indent);
	}
	// END KGU 2015-12-20
	
}
