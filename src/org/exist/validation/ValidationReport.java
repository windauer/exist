/*
 *  eXist Open Source Native XML Database
 *  Copyright (C) 2001-04 The eXist Project
 *  http://exist-db.org
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 *  $Id$
 */

package org.exist.validation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Report containing all validation info (errors, warnings).
 * @author dizzz
 * @see org.xml.sax.ErrorHandler
 */
public class ValidationReport implements ErrorHandler {
    
    private ArrayList validationReport = new ArrayList();
    private long duration = -1;
    
    private ValidationReportItem getValidationReportItem(int type, SAXParseException exception){
        
        ValidationReportItem vri = new ValidationReportItem();
        vri.type=type;
        vri.lineNumber=exception.getLineNumber();
        vri.columnNumber=exception.getColumnNumber();
        vri.message=exception.getMessage();
        return vri;
    }
    
    /**
     *  Receive notification of a recoverable error.
     * @param exception The warning information encapsulated in a
     *                      SAX parse exception.
     * @throws SAXException Any SAX exception, possibly wrapping another
     *                      exception.
     */
    public void error(SAXParseException exception) throws SAXException {
        
        validationReport.add( getValidationReportItem(ValidationReportItem.ERROR, exception) );
        
    }
    
    /**
     *  Receive notification of a non-recoverable error.
     *
     * @param exception     The warning information encapsulated in a
     *                      SAX parse exception.
     * @throws SAXException Any SAX exception, possibly wrapping another
     *                      exception.
     */
    public void fatalError(SAXParseException exception) throws SAXException {
        validationReport.add( getValidationReportItem(ValidationReportItem.FATAL, exception) );
    }
    
    /**
     * Receive notification of a warning.
     *
     * @param exception     The warning information encapsulated in a
     *                      SAX parse exception.
     * @throws SAXException Any SAX exception, possibly wrapping another
     *                      exception.
     */
    public void warning(SAXParseException exception) throws SAXException {
        validationReport.add( getValidationReportItem(ValidationReportItem.WARNING, exception) );
    }
    
    
    /**
     *  Give validation information of the XML document.
     *
     * @return FALSE if no errors and warnings occurred.
     */
    public boolean isValid(){
        return (validationReport.size()==0);
    }
    
    public List getValidationReport(){
        
        List textReport = new ArrayList();
        
        if( isValid() ){
            textReport.add("Document is valid");
        } else {
            textReport.add("Document is not valid");
        }
        
        Iterator allReportItems = validationReport.iterator();
        while(allReportItems.hasNext()){
            ValidationReportItem ri = (ValidationReportItem) allReportItems.next();
            
            textReport.add( ri.toString() );   
        }
        
        textReport.add("Validated in "+duration+" millisec");
        return textReport;
    }
    
    public String[] getValidationReportArray(){
        
        List validationReport = getValidationReport();
        String report[] = new String[validationReport.size()];
        
        Iterator allReportItems = validationReport.iterator();
        int counter=0;
        while(allReportItems.hasNext()){
            
            report[counter]=allReportItems.next().toString();
            counter++;
        }
        return report;
    }
    
    public void setValidationDuration(long time) {
        duration=time;
    }
    
    public long getValidationDuration() {
        return duration;
    }
    
    public String toString(){
        
       StringBuffer validationReport =  new  StringBuffer();
       
       Iterator reportIterator = getValidationReport().iterator();
       while(reportIterator.hasNext()){
           validationReport.append(reportIterator.next().toString());
           validationReport.append("\n");
       }
       
       return validationReport.toString();
    }
}

class ValidationReportItem {
    
    public static final int WARNING = 1;
    public static final int ERROR = 2;
    public static final int FATAL = 4;
    
    public int type = -1;
    public int lineNumber = -1;
    public int columnNumber = -1;
    public String message ="";
    
    
    public String toString(){
        
        String reportType="UNKNOWN";
        
        switch (type) {
            case WARNING:  reportType="Warning"; break;
            case ERROR:    reportType="Error"; break;
            case FATAL:    reportType="Fatal"; break;
            default:       reportType="Unknown Error type"; break;
        }
        
        return (reportType
                + " (" + lineNumber +","+ columnNumber + ") : " + message);
    }
}
