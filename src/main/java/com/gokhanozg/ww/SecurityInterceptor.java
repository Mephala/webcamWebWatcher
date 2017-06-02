package com.gokhanozg.ww;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by mephala on 6/2/17.
 */

public class SecurityInterceptor implements HandlerInterceptor {


    public static final String serverAppRoot = "/ww";
    private Log logger = LogFactory.getLog(getClass());

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        request.getSession().setAttribute("serverAppRoot", serverAppRoot);
        return true;
    }


    private void redirectToUri(HttpServletRequest request, HttpServletResponse response, String uri) throws IOException {
        response.sendRedirect(uri);
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
    }


}