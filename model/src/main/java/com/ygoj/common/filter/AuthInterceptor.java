package com.ygoj.common.filter;

import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

public class AuthInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        Boolean res = checker(request, response, handler);
        if(!res){
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        }

        return res;
    }

    public boolean checker(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        //跳过非控制器请求
        if(!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();

        //检查是否由权限限制注释
        if(!method.isAnnotationPresent(Permission.class)) {
            return true;
        }

        //获取注解信息
        Permission permission = method.getAnnotation(Permission.class);

        //解析jwt
        String token = request.getHeader("JWT");
        if(token.equals("NULL")){
            return false;
        }
        JWT jwt = JWTUtil.parseToken(token);

        //权限验证
        Long userPermission = Long.parseLong(jwt.getPayload("permission").toString());
        Long res = userPermission&(1L << permission.auth());

        System.out.println(res);

        return res != 0;
    }
}
