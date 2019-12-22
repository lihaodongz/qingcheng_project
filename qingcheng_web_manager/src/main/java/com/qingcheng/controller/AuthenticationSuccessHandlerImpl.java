package com.qingcheng.controller;



import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.Util.WebUtil;
import com.qingcheng.pojo.system.LoginLog;
import com.qingcheng.service.system.LoginLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;


@Slf4j
public class AuthenticationSuccessHandlerImpl implements AuthenticationSuccessHandler {

    @Reference
    private LoginLogService loginLogService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {


        String name = authentication.getName();
        String remoteAddr = request.getRemoteAddr();  /* 获取ip*/
        String agent = request.getHeader("user-agent");
        LoginLog loginLog = new LoginLog();
        loginLog.setIp(remoteAddr);
        loginLog.setLoginName(name);
        loginLog.setLoginTime(new Date());
        loginLog.setLocation(WebUtil.getCityByIP(remoteAddr));
        loginLog.setBrowserName(WebUtil.getBrowserName(agent));
        loginLogService.add(loginLog);
        request.getRequestDispatcher("/main.html").forward(request,response);
    }
}
