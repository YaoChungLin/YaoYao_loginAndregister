package com.yaoyao.sso.service;


import java.util.Date;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;



import com.fasterxml.jackson.databind.ObjectMapper;
import com.yaoyao.common.service.RedisService;
import com.yaoyao.sso.mapper.UserMapper;
import com.yaoyao.sso.pojo.User;



@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private RedisService redisService;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final Integer REDIS_TIME = 60 * 30;
    
    /*
     * 检查用户名，手机号以及邮箱是否重复
     */
	public Boolean check(String param, Integer type) {
		User record=new User();
		switch (type) {
		case 1:
			record.setUsername(param);
			break;
		case 2:
			record.setPhone(param);
			break;
		case 3:
			record.setEmail(param);
			break;

		default:
			return null;
		}
		return this.userMapper.selectOne(record)==null;
	}
	
	/*
	 * 注册
	 */
	public Boolean saveUser(User user) {
		user.setId(null);
		user.setCreated(new Date());
		user.setUpdated(user.getCreated());
		
		//密码加密处理，使用MD5
		user.setPassword(DigestUtils.md5Hex(user.getPassword()));
		return this.userMapper.insert(user)==1;
	}

	
	
	 public String doLogin(String username, String password) throws Exception {

	        User record = new User();
	        record.setUsername(username);
	        User user = this.userMapper.selectOne(record);
	        if (null == user) {
	            // 用户不存在
	            return null;
	        }

	        if (!StringUtils.equals(DigestUtils.md5Hex(password), user.getPassword())) {
	            // 密码错误
	            return null;
	        }

	        // 登录成功，将用户的信息保存到Redis中
	        String token = DigestUtils.md5Hex(username + System.currentTimeMillis());

	        this.redisService.set("TOKEN_" + token, MAPPER.writeValueAsString(user), REDIS_TIME);

	        return token;
	    }

	 
	 public User queryUserByToken(String token) {
	        String key = "TOKEN_" + token;
	        String jsonData = this.redisService.get(key);
	        if (StringUtils.isEmpty(jsonData)) {
	            // 登录超时
	            return null;
	        }
	        
	        //重新设置Redis中的生存时间
	        this.redisService.expire(key, REDIS_TIME);
	        
	        try {
	            return MAPPER.readValue(jsonData, User.class);
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	        return null;
	    }
	    

}
