package main.java.com.eweware.service.rest.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author rk@post.harvard.edu
 *         Date: 1/9/13 Time: 1:11 PM
 *
 *         TODO: this is a filter that's not used for now
 *         Provides an appropriate response to all preflight COR OPTIONS method calls.
TODO: this should be added to web.xml:
<filter>
    <filter-name>OptionsMethodFilter</filter-name>
    <filter-class>
main.java.com.eweware.service.rest.filter.OptionsMethodFilter
    </filter-class>
    <init-param>
    </init-param>
</filter>
<filter-mapping>
    <filter-name>OptionsMethodFilter</filter-name>
    <url-pattern>/*</url-pattern>
</filter-mapping>
 */
public class OptionsMethodFilter implements Filter {


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        final HttpServletRequest req = (HttpServletRequest) request;
        final String method = req.getMethod();
        System.out.println(method);
        if (method.toLowerCase().equals("options")) {
            final HttpServletResponse res = (HttpServletResponse) response;
            res.setHeader("Access-Control-Allow-Origin", "*");  //  TODO change * to the actual host to whom we're granting access
            res.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT");
            final String acrh = req.getHeader("Access-Control-Request-Headers");
            if (acrh != null && acrh.length() != 0) {
                res.setHeader("Access-Control-Request-Headers", acrh); // TODO test this
            }
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {

    }
}
