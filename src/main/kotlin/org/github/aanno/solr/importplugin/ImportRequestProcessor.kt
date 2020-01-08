package org.github.aanno.solr.importplugin

import org.apache.solr.update.processor.UpdateRequestProcessor
import org.slf4j.LoggerFactory
import java.lang.invoke.MethodHandles

class ImportRequestProcessor(next: UpdateRequestProcessor) : UpdateRequestProcessor(next) {

    private val log =
        LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())

    init {
        log.warn("ImportRequestProcessor.init")
    }

}
