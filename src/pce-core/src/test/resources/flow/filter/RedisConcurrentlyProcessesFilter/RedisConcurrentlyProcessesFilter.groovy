package flow.filter.RedisConcurrentlyProcessesFilter

import com.ksyun.mc.utils.SpringContextUtils
import groovy.json.JsonSlurper
import groovy.util.logging.Log4j2
import org.pce.core.FinishHandler
import org.pce.core.Node
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ValueOperations

import java.util.concurrent.TimeUnit

/**
 * Created by merce on 2017/1/22.
 */
@Log4j2
class RedisConcurrentlyProcessesFilter extends Node {
    @Override
    def handle(Map globalMemory) {
        try {
            long interval = super.params.get("interval") as long
            RedisTemplate redisTemplate = SpringContextUtils.getBean("redisTemplate") as RedisTemplate
            ValueOperations<String, String> valueOperations = redisTemplate.opsForValue()
            String msg = globalMemory.get("msg")
            def states = new JsonSlurper().parseText(msg)//解析JSON
            String id = states["_id"] as String
            if (!redisTemplate.hasKey(id)) {
                valueOperations.set(id, "", interval, TimeUnit.SECONDS)
                if (this.next != null)
                    this.next.handle(this, globalMemory)//调用下一个MessageHandler
            }

            List<FinishHandler> finisherList = globalMemory.get("finishHandler") as List<FinishHandler>
            if (finisherList != null)
                finisherList << new FinishHandler() {

                    @Override
                    void handle() {
                        redisTemplate.delete(id)
                    }
                }
        } catch (Exception e) {
            log.error(e.getMessage(), e)
        }
    }
}
