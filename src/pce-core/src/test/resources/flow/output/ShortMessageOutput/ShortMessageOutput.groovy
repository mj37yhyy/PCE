package flow.output.ShortMessageOutput

import com.ksyun.mc.utils.GroovyExcutorUtils
import com.ksyun.mc.utils.OkHttpUtils
import groovy.util.logging.Log4j2
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.apache.commons.lang3.StringUtils
import org.pce.core.Node
import org.springframework.util.Assert

/**
 * 短信输出
 */
@Log4j2
class ShortMessageOutput extends Node {
    String path = "http://10.16.49.3:8901/post_sms.do"
    String Id = "kscnet"
    String MD5_td_code = "c761f38793b7df263900c03f72ad79a7"

    @Override
    def handle(Map globalMemory) {
        try {
            log.info(this.getClass().getName())
            List<Map> userInfo = globalMemory["userInfo"] as List<Map>
            Assert.notNull(userInfo)

            def templet = super.params.get("templet")
            String msg = globalMemory.get("msg")
            Map<String, Object> param = ["msg": msg]
            msg = GroovyExcutorUtils.evaluate(templet as String, param)

            //获取参数
            String _Id = super.params.get("Id")
            if (StringUtils.isEmpty(_Id)) _Id = Id

            String _MD5_td_code = super.params.get("MD5_td_code")
            if (StringUtils.isEmpty(_MD5_td_code)) _MD5_td_code = MD5_td_code

            String _path = super.params.get("path")
            if (StringUtils.isEmpty(_path)) _path = path

            //拼装参数
            Map params = new HashMap() {
                {
                    put("id", [_Id])
                    put("MD5_td_code", [_MD5_td_code])
                    put("msg_content", [msg])
                    put("msg_id", [""])
                    put("ext", ["1"])
                }
            }

            userInfo.each {
                String mobile = it["phone"]
                Assert.notNull(mobile)

                params.put("mobile", [mobile])

                //异步发送短信
                log.debug("Send message params: {}", params)
                OkHttpUtils.asynchronousPost(_path, null, params, new Callback() {

                    @Override
                    void onFailure(Call call, IOException e) {
                        log.error("Send message to $mobile error:${e.getMessage()}", e)
                    }

                    @Override
                    void onResponse(Call call, Response response) throws IOException {
                        try {
                            if (!response.isSuccessful())
                                log.error("Send message to $mobile error: ${response.body().string()}")
                            else
                                log.info("Send message to $mobile successful!")
                        } finally {
                            response.close()
                        }
                    }
                })
            }
            if (this.next != null) {
                this.next.handle(this, globalMemory)//调用下一个MessageHandler
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e)
        }
        return null
    }

}
