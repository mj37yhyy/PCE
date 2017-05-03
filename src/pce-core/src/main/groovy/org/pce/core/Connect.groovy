package org.pce.core

import groovy.util.logging.Log4j2
import org.pce.core.utils.GroovyExecutorUtils

/**
 * 这是有向线的类
 */
@Log4j2
class Connect {
    String id
    String name
    Node from//有向线的起点
    Node to//有向线的终点
    String expression//有向线的条件表达式，当满足条件时就可以跳到to执行

    /**
     * 执行有向线的条件表达式，当满足条件时就跳到to执行
     * @param msg 消息体
     * @param globalMemory 共公内存
     */
    void goTo(Map globalMemory) {
        log.info("Project id '${globalMemory.get("projectId")}' - Connect '$name' from '${from.name_}' to '${to.name_}'")
        if (expression != null && !expression.isEmpty()) {//如果存在表达式，则执行
            log.info("Project id '${globalMemory.get("projectId")}' - Connect '$name''s expression is : '$expression'")
            Map params = ["globalMemory": globalMemory]
            def isPass = GroovyExecutorUtils.evaluate(expression, params)//执行条件表达式
            if (isPass as boolean) {//如果表达式返回true，则执行to的handle方法
                log.info("Project id '${globalMemory.get("projectId")}' - Connect '$name''s expression is pass!")
                to.handle(globalMemory)
            }
        } else to.handle(globalMemory)//如果不存在，直接调用
    }
}