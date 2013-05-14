<%@ page  language="java" import="java.util.*" errorPage="" %>
<%
final String msg = main.java.com.eweware.service.base.AWSUtilities.getDefaultHtmlFromS3();
out.print(msg.substring(1));
%>