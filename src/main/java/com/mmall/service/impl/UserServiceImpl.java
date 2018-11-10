package com.mmall.service.impl;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.common.TokenCache;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service("iUserService")
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserMapper  userMapper;

    @Override
    public ServerResponse<User> login(String username, String password) {
        int resultCount = userMapper.checkUsername(username);
        if(resultCount == 0){
            return ServerResponse.createByErrorMessage("用户名不存在");
        }

        String md5password = MD5Util.MD5EncodeUtf8(password);

        User user = userMapper.selectLogin(username, md5password);
        if(user == null){
            return  ServerResponse.createByErrorMessage("密码错误");
        }
        user.setPassword(StringUtils.EMPTY);

        return ServerResponse.createBySuccess("登录成功", user);
    }

    @Override
    public ServerResponse<String> register(User user) {
        ServerResponse vaildResponse = checkVaild(user.getUsername(), Const.CHECK_USERNAME);
        if(!vaildResponse.isSuccess()){
            return vaildResponse;
        }
        vaildResponse = checkVaild(user.getEmail(), Const.CHECK_EMAIL);
        if(!vaildResponse.isSuccess()){
            return vaildResponse;
        }

        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));
        user.setRole(Const.Role.ROLE_CUSTOMER);

        int resultCount = userMapper.insert(user);
        if(resultCount == 0){
            return ServerResponse.createByErrorMessage("注册失败");
        }

        return ServerResponse.createBySuccessMessage("注册成功");
    }

    @Override
    public ServerResponse<String> checkVaild(String str, String type) {
        if(StringUtils.isNoneBlank(type)){
            int resultCount;
            if(type.equals(Const.CHECK_EMAIL)){
                resultCount = userMapper.checkUsername(str);
                if(resultCount > 0){
                    return ServerResponse.createByErrorMessage("email已存在");
                }
            }
            if(type.equals(Const.CHECK_USERNAME)){
                resultCount = userMapper.checkUsername(str);
                if(resultCount > 0){
                    return ServerResponse.createByErrorMessage("用户名已存在");
                }
            }
        }
        else {
            return ServerResponse.createByErrorMessage("参数错误");
        }
        return ServerResponse.createBySuccessMessage("校验成功");
    }

    @Override
    public ServerResponse<String> selectQuestion(String username) {
        ServerResponse validResponse = this.checkVaild(username, Const.CHECK_USERNAME);
        //todo 与gelly不一样的逻辑
        if(!validResponse.isSuccess()){
            return ServerResponse.createByErrorMessage("用户不存在");
        }
        String question = userMapper.selectQuestionByUsername(username);
        if(StringUtils.isNoneBlank(question)){
            return ServerResponse.createBySuccess(question);
        }
        return ServerResponse.createByErrorMessage("找回密码问题为空");
    }

    @Override
    public ServerResponse<String> checkAnswer(String username, String question, String answer) {
        String answerOfUser = userMapper.getAnswer(username, question);
        if(StringUtils.isNoneBlank(answerOfUser)){
            if(answerOfUser.equals(answer)){
                String forgetToken = UUID.randomUUID().toString();
                TokenCache.setKey(TokenCache.TOKEN_PREFIX+username, forgetToken);
                return ServerResponse.createBySuccess(forgetToken);
            }
        }
        return ServerResponse.createByErrorMessage("找回密码答案错误");
    }

    @Override
    public ServerResponse<String> forgetResetPassword(String username, String passwordNew, String forgetToken){
        if(StringUtils.isBlank(forgetToken)){
            return ServerResponse.createByErrorMessage("参数错误，forgetToken为空");
        }
        ServerResponse validResponse = this.checkVaild(username, Const.CHECK_USERNAME);
        //todo 与gelly不一样的逻辑
        if(!validResponse.isSuccess()){
            return ServerResponse.createByErrorMessage("用户不存在");
        }
        String token = TokenCache.getKey(TokenCache.TOKEN_PREFIX+username);
        if(StringUtils.isBlank(token)){
            return ServerResponse.createByErrorMessage("token无效或过期");
        }
        if(StringUtils.equals(forgetToken, token)){
            String md5Password = MD5Util.MD5EncodeUtf8(passwordNew);
            int rowCount = userMapper.updatePasswordByUsername(username, md5Password);
            if(rowCount>0){
                return ServerResponse.createBySuccessMessage("修改密码成功");
            }
        }
        else {
            return ServerResponse.createByErrorMessage("token错误，请重新获取");
        }
        return ServerResponse.createByErrorMessage("修改密码失败");
    }
}
