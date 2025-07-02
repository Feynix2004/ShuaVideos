package org.shuavideos.service.user;

import com.baomidou.mybatisplus.extension.service.IService;

import org.shuavideos.entity.user.User;
import org.shuavideos.entity.vo.FindPWVO;
import org.shuavideos.entity.vo.RegisterVO;

public interface UserService extends IService<User> {


    Boolean register(RegisterVO registerVO);

    Boolean findPassword(FindPWVO findPWVO);
}
