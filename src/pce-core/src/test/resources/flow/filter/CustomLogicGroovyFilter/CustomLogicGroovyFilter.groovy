package flow.filter.CustomLogicGroovyFilter

import com.ksyun.mc.utils.GroovyExcutorUtils
import groovy.util.logging.Log4j2
import org.pce.core.Node

/**
 * 自定义逻辑Groovy过滤器<p/>
 * 不同与自定义过滤器，这里的next由开发自己管理
 */
@Log4j2
class CustomLogicGroovyFilter extends Node {
    @Override
    handle(Map globalMemory) {
        try {
            log.info(this.getClass().getName())
            def script = super.params.get("script")
            log.debug("script={}", script)
            String msg = globalMemory.get("msg")
            Map<String, Object> params = ["msg": msg, "globalMemory": globalMemory, "next": this.next]
            if (script != null) {
                GroovyExcutorUtils.evaluate(script as String, params)
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e)
        }
    }
}
