package org.pce.core.utils

import groovy.util.logging.Log4j2

/**
 * Created by Administrator on 2017/1/23.
 */
@Log4j2
class GroovyExcutorUtils {

    static evaluate(String script, Map<String, Object> params) {
        Binding binding = new Binding(params)
        GroovyShell shell = new GroovyShell(binding)
        // 执行groovy脚本
        shell.evaluate(script as String)
    }
}

