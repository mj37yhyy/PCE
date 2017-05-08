package org.pce.core

import groovy.transform.CompileStatic

/**
 * 最终处理器<p/>
 */
@CompileStatic
interface FinishHandler {
    void handle()
}