package org.github.aanno.solr.importplugin

import org.apache.solr.request.SolrQueryRequest
import org.apache.solr.response.SolrQueryResponse
import org.apache.solr.update.processor.UpdateRequestProcessor
import org.apache.solr.update.processor.UpdateRequestProcessorFactory
import java.lang.NullPointerException

class ImportRequestProcessorFactory : UpdateRequestProcessorFactory() {

    override fun getInstance(
        req: SolrQueryRequest?,
        rsp: SolrQueryResponse?,
        next: UpdateRequestProcessor?
    ): UpdateRequestProcessor {
        if (next == null) {
            throw NullPointerException();
        }
        return ImportRequestProcessor(next);
    }
}
