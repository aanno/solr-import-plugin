package org.github.aanno.solr.importplugin

import org.apache.solr.common.SolrInputDocument
import org.apache.solr.common.SolrInputField
import org.apache.solr.update.AddUpdateCommand
import org.apache.solr.update.CommitUpdateCommand
import org.apache.solr.update.DeleteUpdateCommand
import org.apache.solr.update.MergeIndexesCommand
import org.apache.solr.update.processor.UpdateRequestProcessor
import org.slf4j.LoggerFactory
import java.io.IOException
import java.lang.invoke.MethodHandles
import java.util.*
import java.util.concurrent.BlockingDeque
import java.util.function.BiConsumer
import java.util.function.Supplier

class MappingImportRequestProcessor(
    private val queue: BlockingDeque<AddUpdateCommand>,
    next: UpdateRequestProcessor)
    : UpdateRequestProcessor(next) {

    private val log =
        LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())

    init {
        log.warn("MappingImportRequestProcessor.init")
    }

    private fun createAdd(cmd: AddUpdateCommand): AddUpdateCommand {
        val schema = cmd.req.schema
        val req = ImportSolrRequest(cmd.req.core, cmd.req.params)
        // req.json =
        val jsonMap: HashMap<String, Any> = HashMap()
        cmd.solrDoc?.values?.stream()?.collect(
                toSupplier { -> jsonMap},
                toBiConsumer { a: HashMap<String, Any>, b: SolrInputField ->
                    a.put(b.name, b.value)},
                toBiConsumer { a: HashMap<String, Any>, b: HashMap<String, Any> ->
                    a.putAll(b) }
        )

        // mappings added to input
        addMapField(jsonMap, "stream_source_info", "id")
        addMapField(jsonMap, "dc:title", "title")
        addMapField(jsonMap, "dc:format", "format")
        addMapField(jsonMap, "pdf:docinfo:modified", "_version_")
        addMapField(jsonMap, "Author", "author")
        addMapField(jsonMap, "content", "text")

        val schemaMap: HashMap<String, Any> = HashMap()
        for (name in schema.fields.keys) {
            val inVal = jsonMap.get(name)
            if (inVal != null) {
                schemaMap.put(name, inVal)
            }
        }

        val result = AddUpdateCommand(req)
        result.solrDoc = SolrInputDocument(toInputDocMap(schemaMap))
        return result
    }

    private fun addMapField(jsonMap: HashMap<String, Any>, src: String, tgt: String) {
        val value = jsonMap.get(src)
        if (value != null) {
            jsonMap.put(tgt, value)
        }
    }

    @Throws(IOException::class)
    override fun processAdd(cmd: AddUpdateCommand?) {
        log.warn("processAdd: $cmd")
        if (cmd != null) {
            // TODO (tp): cmd.solrDoc contains the document -> AddUpdateCommand
            val created = createAdd(cmd)
            // next.processAdd(created)
            queue.put(created)
        }
        /*
        if (cmd?.solrDoc?.getField("id") != null) {
            next?.processAdd(cmd)
        }
         */
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
        // ???
        // queue.put(END_UPDATES)
    }

}

fun <R> toSupplier(lambda: () -> R): Supplier<R> {
    return Supplier<R>(lambda)
}

fun <T, U> toBiConsumer(lambda: (T, U) -> Unit): BiConsumer<T, U> {
    return BiConsumer(lambda)
}

fun toInputDocMap(json: Map<String,Any>): Map<String, SolrInputField> {
    val result: MutableMap<String, SolrInputField> = HashMap()
    for (k in json.keys) {
        val v = json.get(k)
        val sif = SolrInputField(k)
        sif.value = v
        result.put(k, sif)
    }
    return result
}

