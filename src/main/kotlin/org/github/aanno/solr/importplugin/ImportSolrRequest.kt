package org.github.aanno.solr.importplugin

import org.apache.solr.common.params.SolrParams
import org.apache.solr.core.SolrCore
import org.apache.solr.request.SolrQueryRequestBase

class ImportSolrRequest(core: SolrCore, params: SolrParams) : SolrQueryRequestBase(core, params) {

}
