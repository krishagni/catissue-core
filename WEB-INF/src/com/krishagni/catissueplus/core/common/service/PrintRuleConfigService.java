package com.krishagni.catissueplus.core.common.service;

import java.util.List;

import com.krishagni.catissueplus.core.common.events.BulkDeleteEntityOp;
import com.krishagni.catissueplus.core.common.events.PrintRuleConfigDetail;
import com.krishagni.catissueplus.core.common.events.RequestEvent;
import com.krishagni.catissueplus.core.common.events.ResponseEvent;
import com.krishagni.catissueplus.core.common.repository.PrintRuleConfigsListCriteria;

public interface PrintRuleConfigService {
	ResponseEvent<List<PrintRuleConfigDetail>> getPrintRuleConfigs(RequestEvent<PrintRuleConfigsListCriteria> req);

	ResponseEvent<PrintRuleConfigDetail> getPrintRuleConfig(RequestEvent<Long> req);

	ResponseEvent<PrintRuleConfigDetail> createPrintRuleConfig(RequestEvent<PrintRuleConfigDetail> req);

	ResponseEvent<PrintRuleConfigDetail> updatePrintRuleConfig(RequestEvent<PrintRuleConfigDetail> req);

	ResponseEvent<List<PrintRuleConfigDetail>> deletePrintRuleConfigs(RequestEvent<BulkDeleteEntityOp> req);
}