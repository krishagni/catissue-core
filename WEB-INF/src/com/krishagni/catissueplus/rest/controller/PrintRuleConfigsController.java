package com.krishagni.catissueplus.rest.controller;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.krishagni.catissueplus.core.common.events.BulkDeleteEntityOp;
import com.krishagni.catissueplus.core.common.events.PrintRuleConfigDetail;
import com.krishagni.catissueplus.core.common.events.RequestEvent;
import com.krishagni.catissueplus.core.common.events.ResponseEvent;
import com.krishagni.catissueplus.core.common.repository.PrintRuleConfigsListCriteria;
import com.krishagni.catissueplus.core.common.service.PrintRuleConfigService;

@Controller
@RequestMapping("/print-rules")
public class PrintRuleConfigsController {
	@Autowired
	private PrintRuleConfigService printRuleConfigSvc;

	@RequestMapping(method = RequestMethod.GET)
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public List<PrintRuleConfigDetail> getConfigPrintRules(
		@RequestParam(value = "objectType", required = false)
		String objectType,

		@RequestParam(value = "instituteName", required = false)
		String instituteName,

		@RequestParam(value = "userName", required = false)
		String userName,

		@RequestParam(value = "startAt", required = false, defaultValue = "0")
		int startAt,

		@RequestParam(value = "maxResults", required = false, defaultValue = "100")
		int maxResults) {

		PrintRuleConfigsListCriteria crit = new PrintRuleConfigsListCriteria()
			.objectType(objectType)
			.instituteName(instituteName)
			.userName(userName)
			.startAt(startAt)
			.maxResults(maxResults);
		return response(printRuleConfigSvc.getPrintRuleConfigs(request(crit)));
	}

	@RequestMapping(method = RequestMethod.GET, value = "/{id}")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public PrintRuleConfigDetail getConfigPrintRule(@PathVariable Long id) {
		return response(printRuleConfigSvc.getPrintRuleConfig(request(id)));
	}

	@RequestMapping(method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public PrintRuleConfigDetail createConfigPrintRule(@RequestBody PrintRuleConfigDetail detail) {
		return response(printRuleConfigSvc.createPrintRuleConfig(request(detail)));
	}

	@RequestMapping(method = RequestMethod.PUT, value = "/{id}")
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	public PrintRuleConfigDetail updateConfigPrintRule(@PathVariable Long id, @RequestBody PrintRuleConfigDetail detail) {
		detail.setId(id);
		return response(printRuleConfigSvc.updatePrintRuleConfig(request(detail)));
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/{id}")
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	public List<PrintRuleConfigDetail>  deleteConfigPrintRule(@PathVariable Long id) {
		BulkDeleteEntityOp op = new BulkDeleteEntityOp();
		op.setIds(Collections.singleton(id));
		return response(printRuleConfigSvc.deletePrintRuleConfigs(request(op)));
	}

	@RequestMapping(method = RequestMethod.DELETE)
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	public List<PrintRuleConfigDetail>  deleteConfigPrintRules(@RequestParam(value = "id") Long[] ids) {
		BulkDeleteEntityOp op = new BulkDeleteEntityOp();
		op.setIds(new HashSet<>(Arrays.asList(ids)));
		return response(printRuleConfigSvc.deletePrintRuleConfigs(request(op)));
	}

	private <T> RequestEvent<T> request(T payload) {
		return new RequestEvent<>(payload);
	}

	private <T> T response(ResponseEvent<T> resp) {
		resp.throwErrorIfUnsuccessful();
		return resp.getPayload();
	}
}
