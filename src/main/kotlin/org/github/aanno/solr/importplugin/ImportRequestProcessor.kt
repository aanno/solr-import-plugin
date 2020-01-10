package org.github.aanno.solr.importplugin

import org.apache.solr.common.SolrException
import org.apache.solr.common.util.ContentStreamBase
import org.apache.solr.handler.extraction.ExtractingDocumentLoader
import org.apache.solr.handler.extraction.ParseContextConfig
import org.apache.solr.handler.extraction.SolrContentHandlerFactory
import org.apache.solr.response.SolrQueryResponse
import org.apache.solr.update.*
import org.apache.solr.update.processor.UpdateRequestProcessor
import org.apache.tika.config.TikaConfig
import org.apache.tika.exception.TikaException
import org.slf4j.LoggerFactory
import org.xml.sax.SAXException
import java.io.File
import java.io.IOException
import java.lang.invoke.MethodHandles

class ImportRequestProcessor(next: UpdateRequestProcessor) : UpdateRequestProcessor(next) {

    private val log =
        LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())

    // TODO (tp)
    private val parseContextConfig: ParseContextConfig = ParseContextConfig()

    private var extractingDocumentLoader: ExtractingDocumentLoader? = null;

    // TODO (tp)
    private var tikaConfig: TikaConfig? = null

    // TODO (tp)
    var first = true

    init {
        log.warn("ImportRequestProcessor.init")
    }

    private fun extractingDocumentLoader(cmd: UpdateCommand): ExtractingDocumentLoader {
        if (extractingDocumentLoader == null) {
            // fill tikaConfig
            tikaConfig(cmd)
            extractingDocumentLoader = ExtractingDocumentLoader(
                    cmd.req, this, tikaConfig, parseContextConfig,
                    SolrContentHandlerFactory())
        }
        return extractingDocumentLoader as ExtractingDocumentLoader;
    }

    private fun tikaConfig(cmd: UpdateCommand): TikaConfig {
        if (tikaConfig == null) {
            try {
                cmd.req.core.resourceLoader.classLoader
                        .getResourceAsStream("solr-default-tika-config.xml")
                        .use({ `is` -> tikaConfig = TikaConfig(`is`) })
            } catch (e: IOException) {
                throw SolrException(SolrException.ErrorCode.SERVER_ERROR, e)
            } catch (e: SAXException) {
                throw SolrException(SolrException.ErrorCode.SERVER_ERROR, e)
            } catch (e: TikaException) {
                throw SolrException(SolrException.ErrorCode.SERVER_ERROR, e)
            }
        }
        return tikaConfig as TikaConfig
    }

    @Throws(IOException::class)
    override fun processAdd(cmd: AddUpdateCommand?) {
        log.warn("processAdd: $cmd")
        if (first && cmd != null) {
            first = false
            val stream = ContentStreamBase.FileStream(File(
                    "/home2/tpasch/java/solr-8.3.1/example/exampledocs/solr-word.pdf"))
            extractingDocumentLoader(cmd).load(cmd.req, SolrQueryResponse(), stream, this)
        }
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
