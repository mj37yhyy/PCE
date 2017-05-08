package org.pce.core.utils

import groovy.util.logging.Log4j2
import org.codehaus.groovy.control.CompilerConfiguration

/**
 * Created by Administrator on 2017/1/23.
 */
@Log4j2
class GroovyExecutorUtils {


    /**
     * 执行脚本
     * @param script 脚本
     * @param params 入参
     * @return 执行结果
     */
    static evaluate(String script, Map<String, Object> params) {
        Binding binding = new Binding(params)
        GroovyShell shell = new GroovyShell(binding)
        // 执行groovy脚本
        def result = shell.evaluate(script as String)
        shell.getClassLoader().clearCache()
        shell = null
        System.gc()
        result
    }

    /**
     * 执行脚本
     * @param script 脚本
     * @param params 入参
     * @param cll 要引用的包路径的集合
     * @return 执行结果
     */
    static evaluate(String script, Map<String, Object> params, List<String> cll) {
        CompilerConfiguration conf = new CompilerConfiguration()
        conf.setSourceEncoding("UTF-8")
        conf.setClasspathList(cll)

        Binding binding = new Binding(params)
        GroovyShell shell = new GroovyShell(binding, conf)
        // 执行groovy脚本
        def result = shell.evaluate(script as String)
        shell.getClassLoader().clearCache()
        shell = null
        System.gc()
        result
    }
}

