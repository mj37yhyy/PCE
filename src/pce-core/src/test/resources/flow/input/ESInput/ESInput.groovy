package flow.input.ESInput

import com.ksyun.mc.utils.GroovyExcutorUtils
import com.ksyun.mc.utils.OkHttpUtils
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.util.logging.Log4j2
import okhttp3.Response
import org.pce.core.Node

/**
 * 从elasticsearch里获取数据
 */
@Log4j2
class ESInput extends Node {
    @Override
    handle(Map globalMemory) {
        log.info(this.getClass().getName())
        String url = super.params.get("url")
        String httpMethod = super.params.get("httpMethod")
        String body = super.params.get("body")

        Response response = null
        try {
            if (httpMethod.equalsIgnoreCase("GET")) {
                response = OkHttpUtils.get(url)
            } else if (httpMethod.equalsIgnoreCase("POST")) {
                String _body = GroovyExcutorUtils.evaluate(body, new HashMap<>())
                response = OkHttpUtils.post(url, null, null, _body)
            }
            if (response != null) {
                def json = response.body().string()
                def states = new JsonSlurper().parseText(json)//解析JSON
                if (this.next != null) {
                    List<Map> msgList = states["hits"]["hits"] as List<Map>
                    msgList.each {//循环日志实体部
                        globalMemory.put("msg", JsonOutput.toJson(it))
                        this.next.handle(this, globalMemory)//调用下一个MessageHandler
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e)
        } finally {
            if (response != null)
                response.close()
        }
        return null
    }

}
