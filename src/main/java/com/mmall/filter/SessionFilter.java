package com.mmall.filter;

import com.mmall.pojo.User;
import com.mmall.redis.SessionRedisManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class SessionFilter extends OncePerRequestFilter {

    @Autowired
    private SessionRedisManager sessionRedisManager;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException{
        //不过滤的uri
        String[] notFilter = new String[]{"/mmall/user/login", "index.jsp", "/mmall/index", "/mmall/logout"};
        //请求的uri
        String uri = request.getRequestURI();
        boolean doFilter = true;

        System.err.println(sessionRedisManager);
        for (String s : notFilter)
        {
            if (uri.indexOf(s) != -1)
            {
                // 如果uri中包含不过滤的uri，则不进行过滤
                doFilter = false;
                break;
            }
        }
        if (doFilter)
        {
            // 执行过滤
            // 从redis中获取登录者实体
            //Object obj = request.getSession().getAttribute(Const.CURRENT_USER);
            User user = null;
            if(sessionRedisManager!=null){
                user = sessionRedisManager.getSession();
            }

            if (null == user)
            {
                logger.debug("SessionFilter.doFilterInternal()... obj=" + user);
                // 设置request和response的字符集，防止乱码
                request.setCharacterEncoding("UTF-8");
                //todo 过滤 用户未登陆返回登陆界面
                filterChain.doFilter(request, response);
            }
            else
            {
                // 如果session中存在登录者实体，则继续
                filterChain.doFilter(request, response);
            }
        }
        else
        {
            // 如果不执行过滤，则继续
            filterChain.doFilter(request, response);
        }
    }
}
