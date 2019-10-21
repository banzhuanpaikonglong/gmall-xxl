package com.littlestore.shoppingmall.config;

import com.alibaba.fastjson.JSON;
import com.littlestore.shoppingmall.util.HttpClientUtil;
import io.jsonwebtoken.impl.Base64UrlCodec;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.Map;

/**
 * @author xxl
 * @create 2019-07-15 21:06
 */
@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {

    // 在进入控制器之前
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String token = request.getParameter("newToken");
        if (token != null ){
            // 将token 放入cookie 中
            CookieUtil.setCookie(request,response,"token",token,WebConst.COOKIE_MAXAGE,false);
        }
        // 如果说登录之后，用户继续访问 商品详情？ 此时是否由token？
        if (token == null){
            token = CookieUtil.getCookieValue(request,"token",false);

        }
        // 获取到了token ，解密token 获取用户昵称。
        if (token != null ){
            // 解密token 获取用户昵称！
            // 使用base64 编码
            Map map = getUserMapByToken(token);

            String nickName = (String)map.get("nickName");
            // 将用户昵称先保存到作用域
            request.setAttribute("nickName",nickName);
        }
        // 获取到方法上的注解@LoginRequire
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        // 获取到方法上面的注解
        LoginRequire methodAnnotation = handlerMethod.getMethodAnnotation(LoginRequire.class);

        if (methodAnnotation != null){
            // 有注解,直接进行认证！调用verify控制器
            // 获取currentIp
            String currentIp = request.getHeader("X-forwarded-for");
            // 使用远程调用 WebConst.VERIFY_ADDRESS;
            String result = HttpClientUtil.doGet(WebConst.VERIFY_ADDRESS+"?token="+token+"&currentIp="+currentIp);

            if ("success".equals(result)){
                // 认证成功！说明用户已经登录！
                Map map = getUserMapByToken(token);
                String userId = (String) map.get("userId");
                // 将用户昵称先保存到作用域
                request.setAttribute("userId",userId);
                return true;
            }else {
                if (methodAnnotation.autoRedirect()){
                    // 必须登录！跳转到登录页面  获取当前请求的url
                    String requestURL = request.getRequestURI().toString();
                    System.out.println(requestURL);
                    String encodeURL = URLEncoder.encode(requestURL,"UTF-8");
                    System.out.println(encodeURL);

                    //页面跳转
                    response.sendRedirect(WebConst.LOGIN_ADDRESS+"?originUrl="+encodeURL);
                    return false;
                }
            }

        }
        return true;
    }

    private Map getUserMapByToken(String token) {
        // 只需要取出私有部分字符串对其进行base64解码即可！
        String tokenUserInfo = StringUtils.substringBetween(token, ".");
        //创建对象
        Base64UrlCodec base64UrlCodec = new Base64UrlCodec();
        //调用方法进行性解密
        byte[] decode = base64UrlCodec.decode(tokenUserInfo);
        //将字节数组变成字符串
        String tokenJson = null;
        try {
            tokenJson = new String (decode,"UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Map map = JSON.parseObject(tokenJson, Map.class);
        return map;
    }


    // 进入控制器之后，试图解析之前
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
    }
    // 试图解析完成
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
    }
}
