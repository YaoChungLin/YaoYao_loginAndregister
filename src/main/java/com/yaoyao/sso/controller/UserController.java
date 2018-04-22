package com.yaoyao.sso.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yaoyao.common.utils.CookieUtils;
import com.yaoyao.sso.pojo.User;
import com.yaoyao.sso.service.UserService;


@RequestMapping("user")
@Controller
public class UserController {
	
	@Autowired
	private UserService userService;
	
	private static final String COOKIE_NAME = "TT_TOKEN";
	/*
	 * 注册页面
	 */
	@RequestMapping(value="register",method=RequestMethod.GET)
	public String toRegister() {
		return "register";
	}
	
	/*
	 * 登录页面
	 */
	@RequestMapping(value="login",method=RequestMethod.GET)
	public String toLogin() {
		return "login";
	}
	
	/*
	 * 检查用户，电话，邮箱是否可用
	 */
	@RequestMapping(value="{param}/{type}",method=RequestMethod.GET)
	public ResponseEntity<Boolean> check(@PathVariable("param") String param,
			@PathVariable("type") Integer type){
		try {
			Boolean bool=this.userService.check(param,type);
			if(null==bool) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
			}
			//为了兼容前端JS逻辑，将返回值取反
			System.out.println(bool);
			return ResponseEntity.ok(!bool);
		} catch (Exception e) {
			// TODO: handle exception
		}
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
	}
	
	
	/*
	 * 注册
	 */
	@RequestMapping(value="doRegister",method=RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> doRegister(User user){
		HashMap<String, Object> result = new HashMap<String,Object>();
		try {
			Boolean bool=this.userService.saveUser(user);
			if(bool) {
				result.put("status", "200");
			}else {
				result.put("status", "500");
				result.put("data", "哈哈");
			}
		} catch (Exception e) {
			e.printStackTrace();
			result.put("status", "500");
			result.put("data", "哈哈");
		}
		return result;
	}
	
	/*
	 * 登录
	 
	@RequestMapping(value="doLogin",method=RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> doLogin(User user){
		Map<String, Object> result=new HashMap<String,Object>();
		String token=this.userService.doLogin(user.getUsername(),user.getPassword());
		if(StringUtils.isEmpty(token)) {
			//登录失败
			result.put("status", 500);
			return result;
		}
		
		//登录成功，保存token到cookie
		result.put("status", 200);
		return result;
	}*/
	
	/**
     * 登录
     * 
     * @param user
     * @return
     */
    @RequestMapping(value = "doLogin", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> doLogin(User user, HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> result = new HashMap<String, Object>();
        try {
            String token = this.userService.doLogin(user.getUsername(), user.getPassword());
            if (StringUtils.isEmpty(token)) {
                // 登录失败
                result.put("status", 500);
                return result;
            }

            // 登录成功，保存token到cookie
            result.put("status", 200);

            CookieUtils.setCookie(request, response, COOKIE_NAME, token);

        } catch (Exception e) {
            e.printStackTrace();
            // 登录失败
            result.put("status", 500);
        }
        return result;
    }
	
    
    @RequestMapping(value = "logout", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> doLogout(User user, HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> result = new HashMap<String, Object>();
        try {
            String token = this.userService.doLogin(user.getUsername(), user.getPassword());
            if (StringUtils.isEmpty(token)) {
                // 登录失败
                result.put("status", 500);
                return result;
            }

            // 登录成功，保存token到cookie
            result.put("status", 200);

            CookieUtils.setCookie(request, response, COOKIE_NAME, token);

        } catch (Exception e) {
            e.printStackTrace();
            // 登录失败
            result.put("status", 500);
        }
        return result;
    }
    
    /**
     * 根据token查询用户信息
     * 
     * @param token
     * @return
     */
    @RequestMapping(value = "{token}", method = RequestMethod.GET)
    public ResponseEntity<User> queryUserByToken(@PathVariable("token") String token) {
        try {
            User user = this.userService.queryUserByToken(token);
            if (null == user) {
                // 资源不存在
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    
    }
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
