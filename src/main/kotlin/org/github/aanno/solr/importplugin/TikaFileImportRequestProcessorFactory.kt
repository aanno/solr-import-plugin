package org.github.aanno.solr.importplugin

import org.apache.solr.common.util.NamedList
import org.apache.solr.request.SolrQueryRequest
import org.apache.solr.response.SolrQueryResponse
import org.apache.solr.update.processor.UpdateRequestProcessor
import org.apache.solr.update.processor.UpdateRequestProcessorFactory
import org.slf4j.LoggerFactory
import java.lang.invoke.MethodHandles

class TikaFileImportRequestProcessorFactory : UpdateRequestProcessorFactory() {

    private val log =
        LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())

    override fun getInstance(
        req: SolrQueryRequest?,
        rsp: SolrQueryResponse?,
        next: UpdateRequestProcessor?
    ): UpdateRequestProcessor {
        if (next == null) {
            throw NullPointerException();
        }
        return TikaFileImportRequestProcessor(next);
    }

    override fun init(args: NamedList<*>?) {
        // could process the Node
        super.init(args)
        log.warn("TikaFileImportRequestProcessorFactory.init")
        if (args != null) {
            for (entry in args) {
                log.warn(entry.toString())
            }
        }
    }

}
