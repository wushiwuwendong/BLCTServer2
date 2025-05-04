package cn.bw.looters.ms.biz.facade;

import cn.bw.looters.ms.biz.model.User;
import cn.bw.looters.ms.biz.repo.UserRepo;
import cn.bw.looters.ms.ex.BizEx;
import cn.bw.looters.ms.ex.BizException;
import cn.bw.looters.ms.web.vo.Res;
import cn.bw.looters.ms.web.vo.ResUtil;
import cn.bw.looters.ms.web.vo.req.LoginReq;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author zhYou
 */
@Component
public class MsFacade {
    public Res userLogin(LoginReq req) {
        User user = userRepo.getUser(req.getUsername());
        if (user == null) {
            throw new BizException(BizEx.USER_NOT_FOUND, req.getUsername());
        }
        if (!user.getPassword().equals(req.getPassword())) {
            throw new BizException(BizEx.PASSWORD_NOT_MATCH, req.getUsername());
        }
        return ResUtil.SUCCESS;
    }

    @Resource
    private UserRepo userRepo;
}
