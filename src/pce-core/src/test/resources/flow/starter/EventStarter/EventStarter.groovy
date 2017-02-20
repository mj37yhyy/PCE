package flow.starter.EventStarter

import org.pce.core.Node

/**
 */
class EventStarter extends Node {
    @Override
    def handle(Map globalMemory) {
        EventStarter eventStarter = this
//        fixedThreadPool.execute(new Runnable() {
//            @Override
//            void run() {
        String msg = globalMemory.get("msg")
        next.handle(eventStarter, globalMemory)//调用下一个MessageHandler
//            }
//        })
        while (!isShutdown) {
            println "EventStarter已停止! \nmsg=$msg\nglobalMemory=$globalMemory"
        }
    }

    @Override
    void shutdown() {
//        fixedThreadPool.shutdown()
        isShutdown = true
    }

    private boolean isShutdown = false
//    private ExecutorService fixedThreadPool = Executors.newFixedThreadPool(100)
}
