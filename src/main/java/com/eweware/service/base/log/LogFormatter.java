package com.eweware.service.base.log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * @author rk@post.harvard.edu
 *         Date: 4/26/13 Time: 3:43 PM
 */
public class LogFormatter extends Formatter {

    public String format(LogRecord rec) {
        final StringBuilder b = new StringBuilder(1000);
        b.append(rec.getLevel());
        b.append(' ');
        b.append(calcDate(rec.getMillis()));
        b.append(' ');
        b.append(formatMessage(rec));
        b.append('\n');
        return b.toString();
    }

    private String calcDate(long millisecs) {
        final SimpleDateFormat f = new SimpleDateFormat("EEE, yyyy.MM.dd HH:mm, z");
        Date resultdate = new Date(millisecs);
        return f.format(resultdate);
    }

    public String getHead(Handler h) {
        return "\n";
//        return "<HTML>\n<HEAD>\n" + (new Date())
//                + "\n</HEAD>\n<BODY>\n<PRE>\n"
//                + "<table width=\"100%\" border>\n  "
//                + "<tr><th>Level</th>" +
//                "<th>Time</th>" +
//                "<th>Log Message</th>" +
//                "</tr>\n";
    }

    public String getTail(Handler h) {
        return "\n";
    }
}