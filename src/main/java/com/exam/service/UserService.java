package com.exam.service;

import com.exam.dto.UserInfoDTO;
import com.exam.securitymodel.User;

public interface UserService {
	
	public User idCheck(String username);
	
	public UserInfoDTO selectAll(String username);

}