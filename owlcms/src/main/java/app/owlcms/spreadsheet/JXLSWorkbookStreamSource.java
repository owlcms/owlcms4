/*
 * Copyright 2009-2012, Jean-François Lamy
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package app.owlcms.spreadsheet;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.HashMap;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.server.InputStreamFactory;

import app.owlcms.data.athlete.Athlete;
import app.owlcms.data.competition.Competition;
import app.owlcms.data.group.Group;
import app.owlcms.utils.LoggerUtils;
import net.sf.jxls.transformer.XLSTransformer;

/**
 * Encapsulate a spreadsheet as a StreamSource so that it can be used as a source of data when the user clicks on a link. This class
 * converts the output stream to an input stream that the vaadin framework can consume.
 */
@SuppressWarnings("serial")
public abstract class JXLSWorkbookStreamSource implements InputStreamFactory {
    private final static Logger logger = LoggerFactory.getLogger(JXLSWorkbookStreamSource.class);

    protected List<Athlete> athletes;

    private HashMap<String, Object> reportingBeans;

    private boolean excludeNotWeighed;

    public JXLSWorkbookStreamSource(boolean excludeNotWeighed) {
        this.excludeNotWeighed = excludeNotWeighed;
        init();
    }

    protected void init() {
//        System.err.println("JXLSWorkbookStreamSource init");
        setReportingBeans(new HashMap<String, Object>());
        getSortedAthletes();
        if (athletes != null) {
            getReportingBeans().put("athletes", athletes);
        }
        getReportingBeans().put("masters", Competition.getCurrent().isMasters());
    }

    /**
     * Return athletes as they should be sorted.
     */
    abstract protected void getSortedAthletes();

    public InputStream getStream() {
        try {
            PipedInputStream in = new PipedInputStream();
            final PipedOutputStream out = new PipedOutputStream(in);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        XLSTransformer transformer = new XLSTransformer();
                        configureTransformer(transformer);
                        HashMap<String, Object> reportingBeans2 = getReportingBeans();
                        Workbook workbook = null;
                        try {
                            workbook = transformer.transformXLS(getTemplate(), reportingBeans2);
                        } catch (Exception e) {
                        	logger.error(LoggerUtils.stackTrace());
                        }
                        if (workbook != null) {
                            postProcess(workbook);
                            workbook.write(out);
                        }
                    } catch (IOException e) {
                        // ignore
                    } catch (Throwable e) {
                    	logger.error(LoggerUtils.stackTrace());
                        throw new RuntimeException(e);
                    }
                }
            }).start();

            return in;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    protected void configureTransformer(XLSTransformer transformer) {
        // do nothing, to be overridden as needed,
    }

    protected void postProcess(Workbook workbook) {
        // do nothing, to be overridden as needed,
    }

    /**
     * Attempt to erase a pair of adjoining cells.
     *
     * @param workbook
     * @param rownum
     * @param cellnum
     */
    public void zapCellPair(Workbook workbook, int rownum, int cellnum) {
        Row row = workbook.getSheetAt(0).getRow(rownum);
        final Cell cellLeft = row.getCell(cellnum);
        if (cellLeft == null) return;

        cellLeft.setCellValue("");

        Cell cellRight = row.getCell(cellnum + 1);
        if (cellRight == null) return;

        cellRight.setCellValue("");

        CellStyle blank = workbook.createCellStyle();
        blank.setBorderBottom(CellStyle.BORDER_NONE);
        cellLeft.setCellStyle(blank);
        cellRight.setCellStyle(blank);
    }

    abstract public InputStream getTemplate() throws IOException;

    public int size() {
        return athletes.size();
    }

    public List<Athlete> getAthletes() {
        return athletes;
    }

    public void setReportingBeans(HashMap<String, Object> jXLSBeans) {
        this.reportingBeans = jXLSBeans;
    }

    public HashMap<String, Object> getReportingBeans() {
        return reportingBeans;
    }

    public void setExcludeNotWeighed(boolean excludeNotWeighed) {
        this.excludeNotWeighed = excludeNotWeighed;
    }

    public boolean isExcludeNotWeighed() {
        return excludeNotWeighed;
    }
    
	@Override
	public InputStream createInputStream() {
		try {
			return getTemplate();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
    protected Group getCurrentCompetitionSession() {
		// FIXME getCurrentCompetitionSession
		return null;
	}

}
