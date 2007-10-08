/**
 * <p>Title: CollectionProtocolAction Class>
 * <p>Description:	This class initializes the fields in the CollectionProtocol Add/Edit webpage.</p>
 * Copyright:    Copyright (c) year
 * Company: Washington University, School of Medicine, St. Louis.
 * @author Mandar Deshmukh
 * @version 1.00
 * Created on Mar 22, 2005
 */

package edu.wustl.catissuecore.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import edu.wustl.catissuecore.actionForm.CollectionProtocolForm;
import edu.wustl.catissuecore.bean.CollectionProtocolBean;
import edu.wustl.catissuecore.bizlogic.BizLogicFactory;
import edu.wustl.catissuecore.bizlogic.CollectionProtocolBizLogic;
import edu.wustl.catissuecore.domain.CollectionProtocol;
import edu.wustl.catissuecore.domain.ConsentTier;
import edu.wustl.catissuecore.util.global.Constants;
import edu.wustl.catissuecore.util.global.Utility;
import edu.wustl.common.bizlogic.IBizLogic;
import edu.wustl.common.cde.CDEManager;
import edu.wustl.common.util.MapDataParser;
import edu.wustl.common.util.dbManager.DAOException;
import edu.wustl.common.util.logger.Logger;

/**
 * This class initializes the fields in the CollectionProtocol Add/Edit webpage.
 * @author Mandar Deshmukh
 */
public class CollectionProtocolAction extends SpecimenProtocolAction 
{
	//This will keep track of no of consents for a particular participant
	int consentCounter=0;	
    /**
     * Overrides the execute method of Action class.
     * Sets the various fields in CollectionProtocol Add/Edit webpage.
     * @param mapping object of ActionMapping
	 * @param form object of ActionForm
	 * @param request object of HttpServletRequest
	 * @param response object of HttpServletResponse
	 * @throws Exception generic exception
     * */
    protected ActionForward executeSecureAction(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception
    {
    	super.executeSecureAction(mapping, form, request, response);
    	//pageOf required for Advance Search Object View.
    	String pageOf = (String)request.getParameter(Constants.PAGEOF);
    	String invokeFunction = (String)request.getParameter("invokeFunction");
    	IBizLogic bizLogic = BizLogicFactory.getInstance().getBizLogic(Constants.DEFAULT_BIZ_LOGIC);
        //Gets the value of the operation attribute.
    	String operation = (String)request.getParameter(Constants.OPERATION);
    	if(invokeFunction!=null)
    	{
    		initCollectionProtocolPage(request, form, pageOf, mapping);	
    	}
    	else
    	{
    		initCleanSession(request);
    	}
        Logger.out.debug("operation in coll prot action"+operation);
        //Sets the operation attribute to be used in the Edit/View Collection Protocol Page in Advance Search Object View. 
        request.setAttribute(Constants.OPERATION,operation);

    	
    	CollectionProtocolForm collectionProtocolForm = (CollectionProtocolForm)form; 
    	String cp_id = String.valueOf(collectionProtocolForm.getId());
    	if(!cp_id.equalsIgnoreCase("0"))
    	{
			CollectionProtocol collectionProtocol = getCPObj(cp_id);
			//Resolved lazy --- collectionProtocol.getConsentTierCollection();
			Collection consentTierCollection=(Collection)bizLogic.retrieveAttribute(CollectionProtocol.class.getName(), collectionProtocol.getId(), "elements(consentTierCollection)");
			Map tempMap= prepareConsentMap(consentTierCollection);
	    	collectionProtocolForm.setConsentValues(tempMap);
	    	collectionProtocolForm.setConsentTierCounter(consentCounter);
	    	
    	}
    	if(collectionProtocolForm.getStartDate() == null)
    	{
    		collectionProtocolForm.setStartDate(Utility.parseDateToString(Calendar.getInstance().getTime(), Constants.DATE_PATTERN_MM_DD_YYYY));
    	}
    	//Name of delete button clicked
        String button = request.getParameter("button");
         
        //Row number of outerblock
        String outer = request.getParameter("blockCounter");
         
//      Gets the map from ActionForm
        Map map = collectionProtocolForm.getValues();
         
//       List of keys used in map of ActionForm
	    List key = new ArrayList();
		key.add("CollectionProtocolEvent:outer_SpecimenRequirement:inner_specimenClass");
		key.add("CollectionProtocolEvent:outer_SpecimenRequirement:inner_specimenType");
		key.add("CollectionProtocolEvent:outer_SpecimenRequirement:inner_tissueSite");
		key.add("CollectionProtocolEvent:outer_SpecimenRequirement:inner_pathologyStatus");
		key.add("CollectionProtocolEvent:outer_SpecimenRequirement:inner_quantity_value");
        
        if(button != null)
        {
         	if(button.equals("deleteSpecimenReq"))
         	{
         	    MapDataParser.deleteRow(key,map,request.getParameter("status"),outer);
         	}
         	else 
         	{
         		//keys of outer block
         		key.add("CollectionProtocolEvent:outer_clinicalStatus");
         		key.add("CollectionProtocolEvent:outer_studyCalendarEventPoint");
         		MapDataParser.deleteRow(key,map,request.getParameter("status"));
         	}
        }
    	
//    	NameValueBean undefinedVal = new NameValueBean(Constants.UNDEFINED,Constants.UNDEFINED);
    	List clinicalStatusList = CDEManager.getCDEManager().getPermissibleValueList(Constants.CDE_NAME_CLINICAL_STATUS,null);
    	request.setAttribute(Constants.CLINICAL_STATUS_LIST, clinicalStatusList);
	    	
    	 // ---------- Used for Add new
//		String reqPath = request.getParameter(Constants.REQ_PATH);
//		if (reqPath != null)
//			request.setAttribute(Constants.REQ_PATH, reqPath);
//		Logger.out.debug("CP Action reqPath : " + reqPath ); 
		Logger.out.debug("page of in collectionProtocol action:"+pageOf);
		request.setAttribute(Constants.PAGEOF,pageOf);

//		// Mandar : code for Addnew PI data 24-Jan-06
//		String addNewUserTo = request.getParameter(Constants.ADD_NEW_USER_TO);
//		if (addNewUserTo != null)
//		{
//			if(addNewUserTo.trim().length() > 0 && addNewUserTo.equalsIgnoreCase("PI" ))
//			{
//				String principalInvestigatorID = (String)request.getAttribute(Constants.ADD_NEW_USER_ID);
//				if(principalInvestigatorID != null && principalInvestigatorID.trim().length() > 0 )
//				{
//					Logger.out.debug(">>>>>>>>>>><<<<<<<<<<<<<<<<>>>>>>>>>>>>> User ID in CP for PI : "+ principalInvestigatorID  );
//					collectionProtocolForm.setPrincipalInvestigatorId(Long.parseLong(principalInvestigatorID ) ); 
//				}
//			}
//			if(addNewUserTo.trim().length() > 0 && addNewUserTo.equalsIgnoreCase("PC" ))
//			{
//				String coordinatorID = (String)request.getAttribute(Constants.ADD_NEW_USER_ID);
//				if(coordinatorID != null && coordinatorID.trim().length() > 0 )
//				{
//					Logger.out.debug(">>>>>>>>>>><<<<<<<<<<<<<<<<>>>>>>>>>>>>> User ID in CP for PI : "+ coordinatorID  );
//					long pcoordIDs[] = {Long.parseLong(coordinatorID )};
//					collectionProtocolForm.setProtocolCoordinatorIds(pcoordIDs ); 
//				}
//			}
//		}
//		// -- 24-Jan-06 end
        return mapping.findForward(pageOf);
    }
    /**
	 * This function will return CollectionProtocolRegistration object 
	 * @param cp_id Selected SpecimenCollectionGroup ID
	 * @return collectionProtocolObject
	 */
	private CollectionProtocol getCPObj(String cp_id) throws DAOException
	{
		CollectionProtocolBizLogic collectionProtocolBizLogic = (CollectionProtocolBizLogic)BizLogicFactory.getInstance().getBizLogic(Constants.COLLECTION_PROTOCOL_FORM_ID);
		String colName = "id";			
		List getCPFromDB = collectionProtocolBizLogic.retrieve(CollectionProtocol.class.getName(), colName, cp_id);		
		CollectionProtocol collectionProtocolObject = (CollectionProtocol)getCPFromDB.get(0);
		return collectionProtocolObject;
	}

	private Map prepareConsentMap(Collection consentTierColl)
	{
		Map tempMap = new HashMap();
		if(consentTierColl!=null)
		{
			Iterator consentTierCollIter = consentTierColl.iterator();			
			int i = 0;
			while(consentTierCollIter.hasNext())
			{
				ConsentTier consent = (ConsentTier)consentTierCollIter.next();
				String statement = "ConsentBean:"+i+"_statement";
				String statementkey = "ConsentBean:"+i+"_consentTierID";
				tempMap.put(statement, consent.getStatement());
				tempMap.put(statementkey, consent.getId());
				i++;
			}
			consentCounter=i;
			return tempMap;
		}
		else
		{
			return null;
		}
    }
	private ActionForward initCollectionProtocolPage(HttpServletRequest request, ActionForm form, String pageOf,ActionMapping mapping)
	{
		CollectionProtocolForm collectionProtocolForm = (CollectionProtocolForm)form;
		HttpSession session = request.getSession();
		CollectionProtocolBean collectionProtocolBean =(CollectionProtocolBean)session.getAttribute(Constants.COLLECTION_PROTOCOL_SESSION_BEAN);
		collectionProtocolForm.setPrincipalInvestigatorId(collectionProtocolBean.getPrincipalInvestigatorId());
		collectionProtocolForm.setProtocolCoordinatorIds(collectionProtocolBean.getProtocolCoordinatorIds());
		collectionProtocolForm.setTitle(collectionProtocolBean.getTitle());
		collectionProtocolForm.setShortTitle(collectionProtocolBean.getShortTitle());
		collectionProtocolForm.setStartDate(collectionProtocolBean.getStartDate());
		collectionProtocolForm.setConsentWaived(collectionProtocolBean.isConsentWaived());
		collectionProtocolForm.setEnrollment(collectionProtocolBean.getEnrollment());
		collectionProtocolForm.setDescriptionURL(collectionProtocolBean.getDescriptionURL());
		collectionProtocolForm.setIrbID(collectionProtocolBean.getIrbID());
		collectionProtocolForm.setActivityStatus(collectionProtocolBean.getActivityStatus());
		collectionProtocolForm.setEndDate(collectionProtocolBean.getEndDate());
		//For Consent Tab
		collectionProtocolForm.setConsentTierCounter(collectionProtocolBean.getConsentTierCounter());
		collectionProtocolForm.setConsentValues(collectionProtocolBean.getConsentValues());
		collectionProtocolForm.setUnsignedConsentURLName(collectionProtocolBean.getUnsignedConsentURLName());
		return (mapping.findForward(pageOf));
	}
	
	private void initCleanSession(HttpServletRequest request)
	{
		HttpSession session = request.getSession();
		session.removeAttribute(Constants.COLLECTION_PROTOCOL_SESSION_BEAN);
		session.removeAttribute(Constants.COLLECTION_PROTOCOL_EVENT_SESSION_MAP);
	}
}