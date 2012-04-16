/*
 * 02/25/2012
 *
 * Copyright (C) 2012 Robert Futrell
 * robert_futrell at users.sourceforge.net
 * http://fifesoft.com/rsyntaxtextarea
 *
 * This library is distributed under a modified BSD license.  See the included
 * RSTALanguageSupport.License.txt file for details.
 */
package org.fife.rsta.ac.js;

import java.io.StringReader;

import org.fife.rsta.ac.js.ast.TypeDeclaration;
import org.fife.rsta.ac.js.ast.TypeDeclarationFactory;
import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.Token;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.ExpressionStatement;
import org.mozilla.javascript.ast.ForLoop;
import org.mozilla.javascript.ast.FunctionCall;
import org.mozilla.javascript.ast.IfStatement;
import org.mozilla.javascript.ast.InfixExpression;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.NewExpression;
import org.mozilla.javascript.ast.NodeVisitor;
import org.mozilla.javascript.ast.PropertyGet;
import org.mozilla.javascript.ast.SwitchStatement;
import org.mozilla.javascript.ast.WhileLoop;


public class JavaScriptHelper {

	private static final String INFIX = org.mozilla.javascript.ast.InfixExpression.class
			.getName();


	/**
	 * Test whether the start of the variable is the same name as the variable
	 * being initialised. This is not possible.
	 * 
	 * @param target Name of variable being created
	 * @param initialiser name of initialiser
	 * @return true if name is different
	 */
	public static boolean canResolveVariable(AstNode target, AstNode initialiser) {
		String varName = target.toSource();
		String init = initialiser.toSource();
		String[] splitInit = init.split("\\.");
		if (splitInit.length > 0) {
			return !varName.equals(splitInit[0]);
		}
		return false;
	}


	/**
	 * Parse Text with JavaScript Parser and return AstNode from the expression
	 * etc..
	 * 
	 * @param text to parse
	 * @return expression statement text from source
	 */
	public static final String parseEnteredText(String text) {
		CompilerEnvirons env = new CompilerEnvirons();
		env.setIdeMode(true);
		env.setErrorReporter(new ErrorReporter() {

			public void error(String message, String sourceName, int line,
					String lineSource, int lineOffset) {
			}


			public EvaluatorException runtimeError(String message,
					String sourceName, int line, String lineSource,
					int lineOffset) {
				return null;
			}


			public void warning(String message, String sourceName, int line,
					String lineSource, int lineOffset) {

			}
		});
		env.setRecoverFromErrors(true);
		Parser parser = new Parser(env);
		StringReader r = new StringReader(text);
		try {
			AstRoot root = parser.parse(r, null, 0);
			AstNode child = (AstNode) root.getFirstChild();
			switch (child.getType()) {
				case Token.EXPR_VOID:
				case Token.EXPR_RESULT:
					return ((ExpressionStatement) child).getExpression()
							.toSource();
				case Token.SWITCH:
					return ((SwitchStatement) child).getExpression().toSource();
				case Token.IF:
					return ((IfStatement) child).getCondition().toSource();
				case Token.WHILE:
					return ((WhileLoop) child).getCondition().toSource();
				case Token.FOR:
					return ((ForLoop) child).getInitializer().toSource();
				case Token.ERROR:
					return "";
					// TODO return other types
				default:
					return child.toSource();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}


	/**
	 * @param node AstNode to look for function
	 * @return function lookup name from it's AstNode. i.e concat function name
	 *         and parameters. If no function is found, then return null
	 */
	public static String getFunctionNameLookup(AstNode node) {
		FunctionCall call = findFunctionCallFromNode(node);
		if (call != null) {
			StringBuffer sb = new StringBuffer();
			if (call.getTarget() instanceof PropertyGet) {
				PropertyGet get = (PropertyGet) call.getTarget();
				sb.append(get.getProperty().getIdentifier());
			}
			sb.append("(");
			int count = call.getArguments().size();
			for (int i = 0; i < count; i++) {
				sb.append("p");
				if (i < count - 1) {
					sb.append(",");
				}
			}
			sb.append(")");
			return sb.toString();
		}
		return null;
	}

	/**
	 * Iterate back up through parent nodes and check whether inside a function
	 * @param node
	 * @return
	 */
	private static FunctionCall findFunctionCallFromNode(AstNode node) {
		AstNode parent = node;
		while (parent != null && !(parent instanceof AstRoot)) {
			if (parent instanceof FunctionCall) {
				return (FunctionCall) parent;
			}
			parent = parent.getParent();
		}
		return null;
	}


	/**
	 * Convert AstNode to TypeDeclaration
	 * @param typeNode AstNode to convert
	 * @param provider SourceProvider 
	 * @return TypeDeclaration if node resolves to supported type, e.g Number, New etc.., otherwise null
	 */
	public static final TypeDeclaration tokenToNativeTypeDeclaration(
			AstNode typeNode, SourceCompletionProvider provider) {
		if (typeNode != null) {
			switch (typeNode.getType()) {
				case Token.CATCH:
					return getTypeDeclaration(TypeDeclarationFactory.ECMA_ERROR);
				case Token.NAME:
					return provider.resolveTypeDeclation(((Name) typeNode)
							.getIdentifier());
				case Token.NEW:
					return getTypeDeclaration(findNewExpressionString(typeNode));
				case Token.NUMBER:
					return getTypeDeclaration(TypeDeclarationFactory.ECMA_NUMBER);
				case Token.OBJECTLIT:
					return getTypeDeclaration(TypeDeclarationFactory.ECMA_OBJECT);
				case Token.STRING:
					return getTypeDeclaration(TypeDeclarationFactory.ECMA_STRING);
				case Token.TRUE:
				case Token.FALSE:
					return getTypeDeclaration(TypeDeclarationFactory.ECMA_BOOLEAN);
				case Token.ARRAYLIT:
					return getTypeDeclaration(TypeDeclarationFactory.ECMA_ARRAY);

			}

			if (typeNode instanceof InfixExpression
					&& typeNode.getClass().getName().equals(INFIX)) {
				TypeDeclaration dec = getTypeFromInFixExpression(typeNode);
				if (dec != null) {
					return dec;
				}
			}
		}
		return null;

	}


	/**
	 * Visitor for infix expression to work out whether the variable should be a
	 * string number literal Only works by determining the presence of
	 * StringLiterals and NumberLiterals. StringLiteral will override type to
	 * evaluate to String.
	 * 
	 * TODO most probably need some work on this
	 */
	private static class InfixVisitor implements NodeVisitor {

		private String type = null;


		public boolean visit(AstNode node) {

			if (!(node instanceof InfixExpression)) // ignore infix expression
			{
				switch (node.getType()) {
					case Token.STRING:
						type = TypeDeclarationFactory.ECMA_STRING;
						break;
					case Token.NUMBER:
						if (type == null) {
							type = TypeDeclarationFactory.ECMA_NUMBER;
						}
						break;
					default:
						if (type == null
								|| !TypeDeclarationFactory.ECMA_STRING
										.equals(type)) {
							type = TypeDeclarationFactory.ANY;
						}
						break;
				}
			}

			return true;
		}
	}

	/**
	 * Use a visitor to visit all the nodes to work out which type to return 
	 * e.g
	 * 1 + 1 returns Number
	 * 1 + "" returns String
	 * true returns Boolean
	 * etc..
	 * 
	 * @param node 
	 * @return
	 */
	private static TypeDeclaration getTypeFromInFixExpression(AstNode node) {
		InfixVisitor visitor = new InfixVisitor();
		node.visit(visitor);
		return getTypeDeclaration(visitor.type);
	}

	
	/**
	 * Returns the node name from 'Token.NEW' AstNode
	 * e.g
	 * new Object --> Object
	 * new Date --> Date
	 * etc..
	 * @param node NewExpression node
	 * @return Extracts the Name identifier from NewExpression
	 */
	private static String findNewExpressionString(AstNode node) {
		NewExpression newEx = (NewExpression) node;
		AstNode target = newEx.getTarget();
		switch (target.getType()) {
			case Token.NAME:
				return ((Name) target).getIdentifier();
		}
		return null;
	}


	/**
	 * Convenience method to lookup TypeDeclaration through the TypeDeclarationFactory. 
	 * @param name
	 * @return
	 */
	private static TypeDeclaration getTypeDeclaration(String name) {
		return TypeDeclarationFactory.Instance().getTypeDeclaration(name);
	}

}