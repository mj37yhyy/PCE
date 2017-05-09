package org.pce.core.utils

import groovy.transform.CompileStatic
import groovy.util.logging.Log4j2
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.runtime.InvokerHelper

import java.util.concurrent.ConcurrentHashMap

/**
 * Created by Administrator on 2017/1/23.
 */
@CompileStatic
@Log4j2
class GroovyExecutorUtils {

    /**
     * 执行脚本
     * @param script 脚本
     * @param params 入参
     * @return 执行结果
     */
    static evaluate(String script, Map params) {
        CompilerConfiguration conf = new CompilerConfiguration()
        conf.setSourceEncoding("UTF-8")

        return _evaluate(script, params, conf)
    }

    /**
     * 执行脚本
     * @param script 脚本
     * @param params 入参
     * @param cll 要引用的包路径的集合
     * @return 执行结果
     */
    static evaluate(String script, Map params, List<String> cll) {
        CompilerConfiguration conf = new CompilerConfiguration()
        conf.setSourceEncoding("UTF-8")
        conf.setClasspathList(cll)

        return _evaluate(script, params, conf)

    }

    private static Object _evaluate(String script, Map params, CompilerConfiguration conf) {
        Binding binding = new Binding(params)

        def cacheKey = MD5Utils.getMD5(script)
        Object scriptObject = null
        try {

            Script shell = null
            if (scriptCache.containsKey(cacheKey)) {
                shell = (Script) scriptCache.get(cacheKey)
            } else {
                shell = new GroovyShell(this.class.getClass().getClassLoader(), conf).parse(script)
            }

            shell.setBinding(binding)
            scriptObject = (Object) InvokerHelper.createScript(shell.getClass(), binding).run()

            // Cache
            if (!scriptCache.containsKey(cacheKey)) {
                scriptCache.put(cacheKey, shell)
            }
        } catch (Throwable t) {
            log.error("groovy script eval error. script: " + script, t)
        }

        return scriptObject
    }

    private static Map<String, Object> scriptCache = new ConcurrentHashMap<String, Object>()
}

