package flow.finisher.BaseFinisher

import org.pce.core.FinishHandler
import org.pce.core.Node

/**
 * Created by merce on 2017/2/8.
 */
class BaseFinisher extends Node {
    @Override
    def handle(Map globalMemory) {
        List<FinishHandler> finisherList = globalMemory.get("finishHandler") as List<FinishHandler>
        if (finisherList != null) {
            finisherList.each { it.handle() }
        }
        return null
    }
}
