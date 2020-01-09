package org.github.aanno.solr.importplugin

import org.apache.solr.update.AddUpdateCommand
import org.apache.solr.update.CommitUpdateCommand
import org.apache.solr.update.DeleteUpdateCommand
import org.apache.solr.update.MergeIndexesCommand
import org.apache.solr.update.processor.UpdateRequestProcessor
import org.slf4j.LoggerFactory
import java.io.IOException
import java.lang.invoke.MethodHandles

class ImportRequestProcessor(next: UpdateRequestProcessor) : UpdateRequestProcessor(next) {

    private val log =
        LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())

    init {
        log.warn("ImportRequestProcessor.init")
    }

    @Throws(IOException::class)
    override fun processAdd(cmd: AddUpdateCommand?) {
        log.warn("processAdd: $cmd")
        next?.processAdd(cmd)
    }

    @Throws(IOException::class)
    override fun processDelete(cmd: DeleteUpdateCommand?) {
        log.warn("processDelete: $cmd")
        next?.processDelete(cmd)
    }

    @Throws(IOException::class)
    override fun processMergeIndexes(cmd: MergeIndexesCommand?) {
        log.warn("processMergeIndexes: $cmd")
        next?.processMergeIndexes(cmd)
    }

    @Throws(IOException::class)
    override fun processCommit(cmd: CommitUpdateCommand?) {
        log.warn("processCommit: $cmd")
        next?.processCommit(cmd)
    }

    override fun doClose() {
        super.doClose();
        log.warn("doClose")
    }

}
