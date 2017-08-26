package com.krishagni.catissueplus.core.biospecimen.services.impl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.MessageSource;
import org.springframework.security.web.util.matcher.IpAddressMatcher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.krishagni.catissueplus.core.administrative.domain.User;
import com.krishagni.catissueplus.core.biospecimen.ConfigParams;
import com.krishagni.catissueplus.core.biospecimen.domain.Specimen;
import com.krishagni.catissueplus.core.biospecimen.events.FileDetail;
import com.krishagni.catissueplus.core.biospecimen.repository.DaoFactory;
import com.krishagni.catissueplus.core.common.PlusTransactional;
import com.krishagni.catissueplus.core.common.domain.LabelPrintJob;
import com.krishagni.catissueplus.core.common.domain.LabelPrintJobItem;
import com.krishagni.catissueplus.core.common.domain.LabelPrintJobItem.Status;
import com.krishagni.catissueplus.core.common.domain.LabelTmplToken;
import com.krishagni.catissueplus.core.common.domain.LabelTmplTokenRegistrar;
import com.krishagni.catissueplus.core.common.domain.PrintItem;
import com.krishagni.catissueplus.core.common.domain.PrintRuleConfig;
import com.krishagni.catissueplus.core.common.errors.OpenSpecimenException;
import com.krishagni.catissueplus.core.common.service.ChangeLogService;
import com.krishagni.catissueplus.core.common.service.ConfigChangeListener;
import com.krishagni.catissueplus.core.common.service.ConfigurationService;
import com.krishagni.catissueplus.core.common.util.AuthUtil;


public class DefaultSpecimenLabelPrinter extends AbstractLabelPrinter<Specimen> implements InitializingBean {
	private static final Log logger = LogFactory.getLog(DefaultSpecimenLabelPrinter.class);

	private List<SpecimenLabelPrintRule> rules = new ArrayList<>();
	
	private DaoFactory daoFactory;
	
	private ConfigurationService cfgSvc;

	private LabelTmplTokenRegistrar printLabelTokensRegistrar;
	
	private MessageSource messageSource;

	private ChangeLogService changeLogSvc;

	public void setDaoFactory(DaoFactory daoFactory) {
		this.daoFactory = daoFactory;
	}

	public void setCfgSvc(ConfigurationService cfgSvc) {
		this.cfgSvc = cfgSvc;
	}

	public void setPrintLabelTokensRegistrar(LabelTmplTokenRegistrar printLabelTokensRegistrar) {
		this.printLabelTokensRegistrar = printLabelTokensRegistrar;
	}

	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	public void setChangeLogSvc(ChangeLogService changeLogSvc) {
		this.changeLogSvc = changeLogSvc;
	}

	@Override
	public List<LabelTmplToken> getTokens() {
		return printLabelTokensRegistrar.getTokens();
	}

	@Override
	public LabelPrintJob print(List<PrintItem<Specimen>> printItems) {		
		try {
			String ipAddr = AuthUtil.getRemoteAddr();
			User currentUser = AuthUtil.getCurrentUser();
			
			LabelPrintJob job = new LabelPrintJob();
			job.setSubmissionDate(Calendar.getInstance().getTime());
			job.setSubmittedBy(currentUser);
			job.setItemType(Specimen.getEntityName());

			List<Map<String, Object>> labelDataList = new ArrayList<Map<String,Object>>();
			for (PrintItem<Specimen> printItem : printItems) {				
				boolean found = false;
				Specimen specimen = printItem.getObject();
				for (SpecimenLabelPrintRule rule : rules) {
					if (!rule.isApplicableFor(specimen, currentUser, ipAddr)) {
						continue;
					}
					
					Map<String, String> labelDataItems = rule.getDataItems(printItem);

					LabelPrintJobItem item = new LabelPrintJobItem();
					item.setJob(job);
					item.setPrinterName(rule.getPrinterName());
					item.setItemLabel(specimen.getLabel());
					item.setCopies(printItem.getCopies());
					item.setStatus(Status.QUEUED);
					item.setLabelType(rule.getLabelType());
					item.setData(new ObjectMapper().writeValueAsString(labelDataItems));

					job.getItems().add(item);
					labelDataList.add(makeLabelData(item, rule, labelDataItems));

					found = true;
					break;
				}

				if (!found) {
					logger.warn("No print rule matched specimen: " + specimen.getLabel());
				}
			}
			
			if (job.getItems().isEmpty()) {
				return null;				
			}
			
			generateCmdFiles(labelDataList);
			daoFactory.getLabelPrintJobDao().saveOrUpdate(job);			
			return job;
		} catch (Exception e) {
			logger.error("Error printing specimen labels", e);
			throw OpenSpecimenException.serverError(e);			
		}
	}	

	@Override
	@PlusTransactional
	public void afterPropertiesSet()
	throws Exception {
		boolean dbMigrationDone = changeLogSvc.doesChangeLogExists(PR_MIGRATION_ID, PR_MIGRATION_AUTHOR, PR_MIGRATION_FILE);
		if (!dbMigrationDone && migrateRulesToDb()) {
			changeLogSvc.insertChangeLog(PR_MIGRATION_ID, PR_MIGRATION_AUTHOR, PR_MIGRATION_FILE);
		}
	}
	
	private boolean migrateRulesToDb() {
		FileDetail fileDetail = cfgSvc.getFileDetail(ConfigParams.MODULE, ConfigParams.SPECIMEN_LABEL_PRINT_RULES);
		if (fileDetail == null || fileDetail.getFileIn() == null) {
			return true;
		}

		List<SpecimenLabelPrintRule> rules = new ArrayList<>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(fileDetail.getFileIn()));

			String ruleLine = null;
			while ((ruleLine = reader.readLine()) != null) {
				SpecimenLabelPrintRule rule = parseRule(ruleLine);
				if (rule == null) {
					continue;
				}

				rules.add(rule);
				logger.info(String.format("Adding print rule: [%s]", rule));
			}

			saveToDb(rules);
			return true;
		} catch (Exception e) {
			logger.error("Error migrating print rules from file: " + fileDetail.getFilename(), e);
			return false;
		} finally {
			IOUtils.closeQuietly(fileDetail.getFileIn());
			IOUtils.closeQuietly(reader);
		}
	}

	//
	// Format of each rule
	// 	cp_short_title	visit_site	specimen_class	specimen_type
	//	user_login	ip_address	label_type	label_tokens	label_design
	//	printer_name	dir_path
	//
	private SpecimenLabelPrintRule parseRule(String ruleLine) {
		if (ruleLine.startsWith("#")) {
			return null;
		}

		String[] ruleLineFields = ruleLine.split("\\t");
		if (ruleLineFields.length < 12 || ruleLineFields.length > 13) {
			logger.error(String.format("Invalid rule [%s]. Expected variables: 12/13, Actual: [%d]", ruleLine, ruleLineFields.length));
			return null;
		}

		int idx = 0;
		SpecimenLabelPrintRule rule = new SpecimenLabelPrintRule();
		rule.setCpShortTitle(ruleLineFields[idx++]);
		rule.setVisitSite(ruleLineFields[idx++]);
		rule.setSpecimenClass(ruleLineFields[idx++]);
		rule.setSpecimenType(ruleLineFields[idx++]);
		rule.setUserLogin(ruleLineFields[idx++]);

		if (!ruleLineFields[idx++].equals("*")) {
			rule.setIpAddressMatcher(new IpAddressMatcher(ruleLineFields[idx - 1]));
		}
		rule.setLabelType(ruleLineFields[idx++]);

		List<LabelTmplToken> tokens = new ArrayList<>();
		for (String labelToken : ruleLineFields[idx++].split(",")) {
			LabelTmplToken token = printLabelTokensRegistrar.getToken(labelToken);
			if (token == null) {
				String errorMsg = String.format("Invalid rule [%s]. Unknown token: [%s]", ruleLine, labelToken);
				throw new IllegalArgumentException(errorMsg);
			}

			tokens.add(token);
		}

		rule.setDataTokens(tokens);
		rule.setLabelDesign(ruleLineFields[idx++]);
		rule.setPrinterName(ruleLineFields[idx++]);
		rule.setCmdFilesDir(ruleLineFields[idx++]);

		if (!ruleLineFields[idx++].equals("*")) {
			rule.setCmdFileFmt(ruleLineFields[idx - 1]);
		}

		rule.setLineage(ruleLineFields.length > 12 ? ruleLineFields[idx++] : "*");
		rule.setMessageSource(messageSource);
		return rule;
	}

	private void saveToDb(List<SpecimenLabelPrintRule> rules) {
		User systemUser = daoFactory.getUserDao().getSystemUser();
		for (SpecimenLabelPrintRule rule : rules) {
			PrintRuleConfig ruleCfg = getPrintRuleConfig(rule, systemUser);
			daoFactory.getPrintRuleConfigDao().saveOrUpdate(ruleCfg);
		}
	}

	private PrintRuleConfig getPrintRuleConfig(SpecimenLabelPrintRule rule, User systemUser) {
		PrintRuleConfig printRuleConfig = new PrintRuleConfig();
		printRuleConfig.setObjectType("SPECIMEN");
		printRuleConfig.setRule(replaceWildcardsWithNull(rule));
		printRuleConfig.setUpdatedBy(systemUser);
		printRuleConfig.setUpdatedOn(Calendar.getInstance().getTime());
		printRuleConfig.setActivityStatus("Active");
		return printRuleConfig;
	}

	private SpecimenLabelPrintRule replaceWildcardsWithNull(SpecimenLabelPrintRule rule) {
		rule.setCpShortTitle(replaceWildcardWithNull(rule.getCpShortTitle()));
		rule.setVisitSite(replaceWildcardWithNull(rule.getVisitSite()));
		rule.setLineage(replaceWildcardWithNull(rule.getLineage()));
		rule.setSpecimenClass(replaceWildcardWithNull(rule.getSpecimenClass()));
		rule.setSpecimenType(replaceWildcardWithNull(rule.getSpecimenType()));
		rule.setUserLogin(replaceWildcardWithNull(rule.getUserLogin()));
		rule.setLabelType(replaceWildcardWithNull(rule.getLabelType()));
		rule.setLabelDesign(replaceWildcardWithNull(rule.getLabelDesign()));
		rule.setPrinterName(replaceWildcardWithNull(rule.getPrinterName()));
		return rule;
	}

	private String replaceWildcardWithNull(String input) {
		return StringUtils.equals(input, "*") ? null : input;
	}

	private static final String PR_MIGRATION_ID = "Migration of specimen print rules to DB";

	private static final String PR_MIGRATION_AUTHOR = "$system";

	private static final String PR_MIGRATION_FILE = "specimen-print-rules.csv";
}
