package com.qingcheng.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.pojo.system.Admin;
import com.qingcheng.service.system.AdminService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserDetailServiceImpl implements UserDetailsService {

    @Reference
    private  AdminService adminService;


    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
       /* 查询管理员*/
        Map map = new HashMap();
        map.put("loginName",s);
        map.put("status","1");
        List<Admin> admin  = adminService.findList(map);
        if (admin.size() ==0){
            return null;
        }
        ArrayList<GrantedAuthority> grantedAuthorities = new ArrayList<GrantedAuthority>();
        grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
       /* 去做密码加密验证*/
        return new User(s,admin.get(0).getPassword(),grantedAuthorities);
    }
}
