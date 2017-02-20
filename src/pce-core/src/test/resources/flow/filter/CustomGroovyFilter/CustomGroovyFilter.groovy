package flow.filter.CustomGroovyFilter

import com.ksyun.mc.utils.GroovyExcutorUtils
import groovy.util.logging.Log4j2
import org.pce.core.Node

/**
 * 自定义Groovy过滤器
 */
@Log4j2
class CustomGroovyFilter extends Node {
    @Override
    handle(Map globalMemory) {
        try {
            log.info(this.getClass().getName())
            def script = super.params.get("script")
            log.debug("script={}", script)
            String msg = globalMemory.get("msg")
            Map<String, Object> params = ["msg": msg, "globalMemory": globalMemory]
            if (script != null) {
                msg = GroovyExcutorUtils.evaluate(script as String, params)
                globalMemory.put("msg", msg)
            }

            if (this.next != null)
                this.next.handle(this, globalMemory)//调用下一个MessageHandler
        } catch (Exception e) {
            log.error(e.getMessage(), e)
        }
    }
}
