package flow.input.FindUserInput

import com.ksyun.mc.dao.entity.User
import com.ksyun.mc.dao.mapper.UserProjectMapper
import com.ksyun.mc.utils.SpringContextUtils
import groovy.util.logging.Log4j2
import org.pce.core.Node

/**
 * 这是个业务上的input，用于查询用户信息
 */
@Log4j2
class FindUserInput extends Node {
    @Override
    def handle(Map globalMemory) {
        try {
            log.info(this.getClass().getName())
            def projectId = globalMemory.get("projectId")
            UserProjectMapper userProjectMapper = SpringContextUtils.getBean(UserProjectMapper.class)
            //查询后更新公共内存
            List<User> userList = userProjectMapper.findUserByProjectId(projectId as long)
            List<Map> userInfo = []
            for (User user : userList) {
                def userInfoMap = ["phone": user.phone, "email": user.email]
                userInfo << userInfoMap
            }
            globalMemory.put("userInfo", userInfo)
            if (this.next != null)
                this.next.handle(this, globalMemory)//调用下一个MessageHandler
        } catch (Exception e) {
            log.error(e.getMessage(), e)
        }
    }
}
