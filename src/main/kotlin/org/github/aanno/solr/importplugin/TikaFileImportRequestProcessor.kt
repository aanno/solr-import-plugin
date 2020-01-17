package org.github.aanno.solr.importplugin

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
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
import org.github.aanno.solr.importplugin.tika.FileWalker
import org.slf4j.LoggerFactory
import org.xml.sax.SAXException
import java.io.IOException
import java.lang.invoke.MethodHandles
import java.nio.file.Path
import java.nio.file.Paths
import java.util.regex.Pattern


class TikaFileImportRequestProcessor(next: UpdateRequestProcessor) : UpdateRequestProcessor(next) {

    private val log =
        LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())

    private val mapping = MappingImportRequestProcessor(next)

    // TODO (tp)
    private val parseContextConfig: ParseContextConfig = ParseContextConfig()

    private var extractingDocumentLoader: ExtractingDocumentLoader? = null;

    // TODO (tp)
    private var tikaConfig: TikaConfig? = null

    init {
        log.warn("TikaFileImportRequestProcessor.init")
    }

    private fun extractingDocumentLoader(cmd: UpdateCommand): ExtractingDocumentLoader {
        if (extractingDocumentLoader == null) {
            // fill tikaConfig
            tikaConfig(cmd)
            extractingDocumentLoader = ExtractingDocumentLoader(
                    cmd.req, mapping, tikaConfig, parseContextConfig,
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
    override fun processAdd(cmd: AddUpdateCommand?) = runBlocking {
        /*
        if (!DebugProbes.isInstalled) {
            DebugProbes.install()
        }
         */
        log.warn("processAdd: $cmd")
        if (cmd != null) {
            val channel = Channel<Path>(4)
            val fw = FileWalker(Paths.get("/home2/tpasch/java/solr-8.3.1/example/exampledocs/"), Pattern.compile("."))
            val job = withContext(Dispatchers.Default) {
                // fw.walk(channel, { p -> log.warn("walk: $p")})
                fw.walk(channel, { p ->
                    if (p.toFile().isFile) {
                        val stream = ContentStreamBase.FileStream(p.toFile())
                        try {
                            extractingDocumentLoader(cmd).load(cmd.req, SolrQueryResponse(), stream, mapping)
                        } catch (e: Exception) {
                            log.error("tika failed: " + e, e)
                        }
                    }
                })
            }
            // DebugProbes.dumpCoroutines()
        }
        // we delegate to mapping (and then to next)
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
