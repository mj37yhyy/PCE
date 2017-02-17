package org.pce.core
/**
 * 通过from查找下一个或多个与之相连的节点
 * Created by miaojia on 2017/2/8.
 */
class ConnectHandler {
    private List<Connect> connects

    void handle(Node from, Map globalMemory) {
        connects.each {
            if (it.from == from) {//如果from相同，则运行goTo
                it.goTo(globalMemory)
            }
        }
    }
}
