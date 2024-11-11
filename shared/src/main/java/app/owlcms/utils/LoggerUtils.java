/*******************************************************************************
 * Copyright (c) 2009-2023 Jean-François Lamy
 *
 * Licensed under the Non-Profit Open Software License version 3.0  ("NPOSL-3.0")
 * License text at https://opensource.org/licenses/NPOSL-3.0
 *******************************************************************************/
package app.owlcms.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

import ch.qos.logback.classic.Logger;

/**
 * The Class LoggerUtils.
 */
public class LoggerUtils {

	/**
	 * @param e1
	 * @return
	 */
	public static String exceptionMessage(Throwable e1) {
		String message = null;
		if (e1.getCause() != null) {
			message = e1.getCause().getMessage();
		}
		if (message == null) {
			message = e1.getMessage();
		}
		if (message == null) {
			message = e1.getClass().getSimpleName();
		}
		return message;
	}

	public static void logError(Logger logger, Throwable e, Boolean... shortMessage) {
		if (shortMessage.length > 0 && shortMessage[0]) {
			logger.error(shortStackTrace(e));
		} else {
			logger.error(stackTrace(e));
		}
	}

	/**
	 * Where from.
	 *
	 * @return the string
	 */
	public static String stackTrace() {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		StackTraceElement[] trace = Thread.currentThread().getStackTrace();
		return shortStackTrace(sw, pw, trace);
	}

	public static String fullStackTrace() {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		StackTraceElement[] trace = Thread.currentThread().getStackTrace();
		int i = 0;
		for (StackTraceElement ste : trace) {
			String string = ste.toString();

			// dealing with traces created in UIEvents. lines at the top are not useful.
			// first line is java.base and we skip it.

			// System.err.println("processing "+string+" "+i);

			if (i > 1 && string.trim().startsWith("app.owlcms.uievents.UIEvent")) {
				continue;
			}
			if (string.startsWith("com.vaadin.flow.server.")
			        || string.startsWith("com.vaadin.flow.internal")
			        || string.startsWith("com.vaadin.flow.router")
					// || string.startsWith("com.vaadin.flow.component")
			        || (i > 1 && string.startsWith("java.base"))) {
				break;
			}
			if (i > 1) {
				pw.println("\t" + string);
			}

			i++;
		}
		return sw.toString();
	}

	private static String shortStackTrace(StringWriter sw, PrintWriter pw, StackTraceElement[] trace) {
		int i = 0;
		for (StackTraceElement ste : trace) {
			String string = ste.toString();

			// dealing with traces created in UIEvents. lines at the top are not useful.
			// first line is java.base and we skip it.
			if (i > 1 && string.trim().startsWith("app.owlcms.uievents.UIEvent")) {
				continue;
			}
			if (string.startsWith("com.vaadin.flow.server.")
			        || string.startsWith("com.vaadin.flow.internal")
			        || string.startsWith("com.vaadin.flow.router")
			        || string.startsWith("com.vaadin.flow.component")
			        || (i > 1 && string.startsWith("java.base"))) {
				break;
			}
			pw.println("\t" + string);
			i++;
		}
		return sw.toString();
	}

	/**
	 * @param t
	 * @return
	 */
	public static String shortStackTrace(Throwable t) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		StackTraceElement[] trace = t.getStackTrace();
		return shortStackTrace(sw, pw, trace);
	}

	/**
	 * @param t
	 * @return
	 */
	public static String stackTrace(Throwable t) {
		// IDEA: skip from "at jakarta.servlet.http.HttpServlet.service" to line starting
		// with "Caused by"
		StringWriter sw = new StringWriter();
		t.printStackTrace(new PrintWriter(sw));
		return sw.toString();
	}

	/**
	 * Where from.
	 *
	 * @return the string
	 */
	public static String whereFrom() {
		return whereFrom(1);
	}

	/**
	 * Where from, additional depth
	 *
	 * @return the string
	 */
	public static String whereFrom(int depth) {
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		String where = stackTrace[3 + depth >= stackTrace.length ? 3 : 3 + depth].toString();
		int firstBracketIx = where.indexOf('(');
		return where.substring(firstBracketIx);
	}
}
