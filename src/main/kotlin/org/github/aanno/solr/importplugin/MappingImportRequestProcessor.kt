package org.github.aanno.solr.importplugin

import org.apache.solr.common.SolrInputField
import org.apache.solr.update.AddUpdateCommand
import org.apache.solr.update.CommitUpdateCommand
import org.apache.solr.update.DeleteUpdateCommand
import org.apache.solr.update.MergeIndexesCommand
import org.apache.solr.update.processor.UpdateRequestProcessor
import org.jose4j.json.internal.json_simple.JSONObject
import org.slf4j.LoggerFactory
import java.io.IOException
import java.lang.invoke.MethodHandles
import java.util.HashMap
import java.util.function.BiConsumer
import java.util.function.Supplier

class MappingImportRequestProcessor(next: UpdateRequestProcessor) : UpdateRequestProcessor(next) {

    private val log =
        LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())

    init {
        log.warn("MappingImportRequestProcessor.init")
    }

    private fun createAdd(cmd: AddUpdateCommand): AddUpdateCommand {
        val req = ImportSolrRequest(cmd.req.core, cmd.req.params)
        // req.json =
        val jsonMap: HashMap<String, Any> = HashMap();
        cmd.solrDoc?.values?.stream()?.collect(
                toSupplier { -> jsonMap},
                toBiConsumer { a: HashMap<String, Any>, b: SolrInputField ->
                    a.put(b.name, b.value)},
                toBiConsumer { a: HashMap<String, Any>, b: HashMap<String, Any> ->
                    a.putAll(b) }
        )
        val json: JSONObject = JSONObject(jsonMap)
        json.put("id", json.get("stream_source_info"))
        // json.put("title", json.get("dc:title"))
        json.put("format", json.get("dc:format"))
        json.put("_version_", json.get("pdf:docinfo:modified"))
        req.json = json as Map<String, Object>
        val result = AddUpdateCommand(req)
        return result
    }

    @Throws(IOException::class)
    override fun processAdd(cmd: AddUpdateCommand?) {
        log.warn("processAdd: $cmd")
        if (cmd != null) {
            // TODO (tp): cmd.solrDoc contains the document -> AddUpdateCommand
            val created = createAdd(cmd)
            next.processAdd(created)
        }
        if (cmd?.solrDoc?.getField("id") != null) {
            next?.processAdd(cmd)
        }
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

fun <R> toSupplier(lambda: () -> R): Supplier<R> {
    return Supplier<R>(lambda)
}

fun <T, U> toBiConsumer(lambda: (T, U) -> Unit): BiConsumer<T, U> {
    return BiConsumer(lambda)
}

