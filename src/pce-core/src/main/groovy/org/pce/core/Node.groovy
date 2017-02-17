package org.pce.core
/**
 * Node节点
 * 需要实现类实现handle方法
 * Created by MiaoJia(miaojia@kingsoft.com) on 2017/1/18.
 */

abstract class Node {
    String id_
    String name_
    String describe_
    String type_
    String class_
    public Map params
    public ConnectHandler next

    void init(Map params) {
        this.params = params
    }

    /**
     * 处理
     * @param globalMemory 公共内存
     * @return
     */
    abstract handle(Map globalMemory)

    /**
     * 关闭
     */
    void shutdown(){

    }
}