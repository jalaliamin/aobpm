package org.yawlfoundation.yawl.aspectService;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.jdom.input.SAXBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.interfce.SpecificationData;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.engine.interfce.interfaceA.InterfaceA_EnvironmentBasedClient;
import org.yawlfoundation.yawl.engine.interfce.interfaceB.InterfaceBWebsideController;
import org.yawlfoundation.yawl.engine.interfce.interfaceB.InterfaceB_EnvironmentBasedClient;
import org.yawlfoundation.yawl.engine.interfce.interfaceX.InterfaceX_Service;
import org.yawlfoundation.yawl.engine.interfce.interfaceX.InterfaceX_ServiceSideClient;
import org.yawlfoundation.yawl.exceptions.YAWLException;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.worklet.exception.ExceptionService;


/**
 * This service is implemented to enable dynamic weaving of aspects to business processes
 *
 * @author Amin Jalali
 * @date 9/10/2012
 * last version
 */

public class AspectService  extends InterfaceBWebsideController implements InterfaceX_Service {

	private static String _observer = "http://localhost:8080/aspectService/ib";
	
	private static String _handle = null;
	private static InterfaceB_EnvironmentBasedClient _ibClient = null;
	private InterfaceX_ServiceSideClient _ixClient = new InterfaceX_ServiceSideClient("http://localhost:8080/yawl/ix");
	private Persister _Persister;
	
	private static String _ruleFolder = "C:\\Java\\aspect\\rules\\";
	private static String _specFolder = "C:\\Java\\aspect\\specs\\";
	
	//amin
//	private static List<Advice> _advices = new ArrayList<Advice>();
	
//	private static String _ruleFolder = "C:/Java/aspect/rules/";
//	private static String _specFolder = "C:/Java/aspect/specs/";
	
	// Initiations -------------------------------------------------------------------------------------------------------------------
	public synchronized void handleEngineInitialisationCompletedEvent(){
		try {
			_ixClient.addInterfaceXListener("http://localhost:8080/aspectService/ix");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
    public AspectService() {
		super();
		_Persister = Persister.getInstance();
	}

    //InterfaceB =======================================================================================	
//	@Override
	public void handleEnabledWorkItemEvent(WorkItemRecord enabledWorkItem) {
		handleProceedPlaceholder(enabledWorkItem);
	}
//	@Override
    public void handleWorkItemStatusChangeEvent(WorkItemRecord workItem, 
                                                String oldStatus, String newStatus) { 
		if( oldStatus.equalsIgnoreCase("Fired") && newStatus.equalsIgnoreCase("Executing")){
			ItemPreConstraint(workItem, getWorkItemData(workItem.getTaskID(), workItem.getID()));
		}
	}

	//InterfaceX =======================================================================================
//	@Override
	public void handleCheckCaseConstraintEvent(YSpecificationID specID,
			String caseID, String data, boolean precheck) {
		if(!precheck)
			CasePostConstraint(specID, caseID, data);
	}
//	@Override
	public void handleCheckWorkItemConstraintEvent(WorkItemRecord wir,
			String data, boolean precheck) {
		if(!precheck)
			ItemPostConstraint(wir, data);
	}

	//Main methods =======================================================================================
//	private void ItemPreConstraint(WorkItemRecord wir, String data) {
//		//evaluate rules
//		Collection<String> advices = evaluateRule(wir.getSpecURI(), wir.getTaskName(), data);
//		
//		if (advices.size()>0){
//			suspendCase(wir.getCaseID().substring(0, wir.getCaseID().indexOf('.')));
//			
//			for(String s: advices){
//				try {
//					if (!isUploaded(getAdviceID(s))){
//						uploadAdvice(s);
//					}
//					
//					//2. get input params
//					String res = mapItemParamsToAdviceCaseParams(s, getInputParams(getAdviceID(s)), data);
//					
//					//3. lunch advices
//					String adviceCaseID = launchAdvice(getAdviceID(s), res);
//					
//					Advice advice = new Advice();
//					advice.setParentSpecID(wir.getSpecIdentifier());
//					advice.setParentSpecName(wir.getSpecURI());
//					advice.setParentCaseID(wir.getCaseID().substring(0, wir.getCaseID().indexOf('.')));
//					//child one
//					advice.setAdjpID(wir.getID());
//					advice.setAdjpDataString(data);
//					advice.setAdviceName(s);
//					advice.setAdviceCaseID(adviceCaseID);
//					advice.setSynched(false);
//					
//					//4. persists
//					_Persister.beginTransaction();
//					_Persister.insert(advice);
//					_Persister.commit();
//					
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			}
//		}
//	}
	
	private void ItemPreConstraint(WorkItemRecord wir, String data) {
		//evaluate rules
		Collection<String> advices = evaluateRule(wir.getSpecURI(), wir.getTaskName(), data);
		if (advices.size()>0){
			suspendCase(wir.getCaseID().substring(0, wir.getCaseID().indexOf('.')));
			
			for(String s: advices){
				try {
					uploadAdvice(s);
					//2. get input params
					String res = mapItemParamsToAdviceCaseParams(s, getInputParams(getAdviceID(s)), data);
					
					Advice advice = new Advice();
					advice.setParentSpecID(wir.getSpecIdentifier());
					advice.setParentSpecName(wir.getSpecURI());
					advice.setParentCaseID(wir.getCaseID().substring(0, wir.getCaseID().indexOf('.')));
					//child one
					advice.setAdjpID(wir.getID());
					advice.setAdjpDataString(data);
					advice.setAdviceName(s);
					
					advice.setSynched(false);
					
//					//4. persists
					_Persister.beginTransaction();
					_Persister.insert(advice);
					_Persister.commit();
					//amin
//					_advices.add(advice);
//					int index= _advices.indexOf(advice);

					
					//3. lunch advices
					String adviceCaseID = launchAdvice(getAdviceID(s), res);

					//amin
//					_advices.get(index).setAdviceCaseID(adviceCaseID);

					advice.setAdviceCaseID(adviceCaseID);
					
					_Persister.beginTransaction();
					_Persister.update(advice);
					_Persister.commit();

					
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private void ItemPostConstraint(WorkItemRecord wir, String data) {
		
		AdviceQuery query = new AdviceQuery();
		//List<Advice> advs = query.getAdvicesByAdviceJoinPointID(wir.getParentID());
		List<Advice> advs = query.getAdvicesByAdviceJoinPointID(wir.getID());
		
		if(advs!=null && advs.size()>0){
			
			suspendCase(wir.getCaseID().substring(0, wir.getCaseID().indexOf('.')));
			System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
			System.out.println("suspended case is " + wir.getCaseID().substring(0, wir.getCaseID().indexOf('.')));

			
			for (Advice advice : advs){
				SAXBuilder builder = new SAXBuilder();
				
				List<org.jdom.Element> ls = null;
				org.jdom.Element root = null;
				
				try {
			    	org.jdom.Document document = builder.build(new StringReader(data));
			    	ls = document.getRootElement().getChildren();
		    	} catch (Exception e) {
		    		System.out.println(e);
			    }
			   

//	            //create child element, add an attribute, and add to root
				String s = "<" + advice.getProceedName() + ">";
	            for(org.jdom.Element l: ls){
	            	s += "<" + l.getName() + ">" + l.getText() + "</" + l.getName() + ">";
	            }
	            s += "</" + advice.getProceedName() + ">";
//				//end
	            
				//_ibClient.getTaskInformationStr(specID, taskID, sessionHandle)
				
//	            String s = "<" + advice.getProceedName() + " />";
	            
	            connect();
	            
	            advice.setSynched(false);
	            _Persister.beginTransaction();
				_Persister.update(advice);
				_Persister.commit();
	            
	            System.out.println("*** try to checkin workitem with proceed id of " + advice.getProceedID() + " - with data: " + s);
				try {
					_ibClient.checkInWorkItem(advice.getProceedID(), s, "" , _handle); //"<P><Name>Ali</Name><LastName>Nikoo</LastName></P>", _handle);
				} catch (IOException e) {
					System.out.println("************************ problem in checkin");
					e.printStackTrace();
				}
			}
		}
	}
	
	private void CasePostConstraint(YSpecificationID specID, String caseID, String data) {
		AdviceQuery query = new AdviceQuery();
		List<Advice> advs = query.getByAdviceCaseID(caseID);
		if(advs!=null && advs.size()>0){
			Advice advice = (Advice)advs.get(0);
			
			//TODO 2 update advjp data using advice data
//			org.jdom.Element d = getDataforCase(advice.getParentSpecName(), advice.getParentCaseID(), data);
//			
//			try {
//				_ixClient.updateCaseData(advice.getParentCaseID(), d, _handle);
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
			
			System.out.println("==================== advice is finished" + advice.getId());
			//1.2 remove row from the advice table
			_Persister.beginTransaction();
			_Persister.delete(advice);
			_Persister.commit();

			//1.3 call weaving - advice.getAdjpID() 
			weaving(advice.getParentCaseID(), advice.getAdjpID(), true);
			System.out.println("==================== advice is weaved");
		}
	}
	
	private void handleProceedPlaceholder(WorkItemRecord enabledWorkItem) {
		connect();
		WorkItemRecord childWorkItem;
		try {
			childWorkItem = checkOut(enabledWorkItem.getID(), _handle);
			AdviceQuery query = new AdviceQuery();
			
			List<Advice> advs = query.getByAdviceName(enabledWorkItem.getSpecURI());
			System.out.println(">>>> Number of the same advices are " + advs.size());
			if(advs!=null && advs.size()>0){
			
				System.out.println(">>>> Number of the launched advices are " + advs.size());
				do{
					advs = query.getByAdviceCaseID(enabledWorkItem.getCaseID());
				}while(advs==null || advs.size()==0);
				System.out.println(">>>> Number of the launched advices are " + advs.size());
				
				//TODO 1 update parent workitem data
				
				Advice advice = advs.get(0);
				advice.setSynched(true);
				advice.setProceedName(enabledWorkItem.getTaskName());
				advice.setProceedID(childWorkItem.getID());

				_Persister.beginTransaction();
				_Persister.update(advice);
				_Persister.commit();
				
				System.out.println(">>>> Proceed Name is " + advice.getProceedName());
				System.out.println(">>>> Proceed ID is " + advice.getProceedID());
				//update case data
				org.jdom.Element d = getDataforCase(advice.getParentSpecName(), advice.getParentCaseID(),
						_ibClient.getCaseData(enabledWorkItem.getCaseID(), _handle));
				
				_ixClient.updateCaseData(advice.getParentCaseID(), d, _handle);
				
				weaving(advice.getParentCaseID(), advice.getAdjpID(), false);
			}
		} catch (IOException | YAWLException e) {
			e.printStackTrace();
		}
	}

	//commons =======================================================================================
	private void connect(){
		if(_ibClient==null){
			_ibClient = new InterfaceB_EnvironmentBasedClient("http://localhost:8080/yawl/ib");
			try {
				_handle = _ibClient.connect(engineLogonName, engineLogonPassword);
			} catch (IOException e) {
				System.out.println("************ Error ***************");
				e.printStackTrace();
				return;
			}
		}
	}
	
	private String getWorkItemData(String taskID, String workitemID){
		String result = null;
		connect();
		try {
			List<WorkItemRecord> wirl = _ibClient.getWorkItemsForTask(taskID, _handle);
			for (WorkItemRecord r : wirl) {
				if(r.getID().equalsIgnoreCase(workitemID)){
					result = r.getDataListString(); 
					if (result==null){
						System.out.println("---------- the workitem is not yet updated ------------");
						result = getWorkItemData(taskID, workitemID);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	private Collection<String> evaluateRule(String specName, String taskName, String data){
		Collection<String> advices = new ArrayList<String>();

		String ruleFileName = _ruleFolder + "\\aobpm.xml";
//		String ruleFileName = _ruleFolder + "/aobpm.xml";
		
		File file = new File(ruleFileName);
		if (file.exists()){
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = null;
			Document doc = null;
			try {
				dBuilder = dbFactory.newDocumentBuilder();
				doc = dBuilder.parse(ruleFileName);
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			if(dBuilder==null || doc==null)
				return advices;
			
			doc.getDocumentElement().normalize();
			NodeList nList = doc.getElementsByTagName("pointcut");
			
			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node nNode = nList.item(temp);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
			      Element eElement = (Element) nNode;
			      if (specName.equals(eElement.getAttribute("process")) && 
			    		  (taskName.equals(eElement.getAttribute("task")))){
			    	  // The process and task are met, so evaluate condition to true
			    	  if (evaluatePointcutCondition(eElement.getAttribute("condition"), data)){
			    		  // read and add advices to the advices variable
			    		  Element eAdvice = (Element) nNode.getParentNode().getParentNode();	  
			    		  advices.add(eAdvice.getAttribute("process"));
			    	  }
		    	  }
				}
			}
		}
		
		return advices;
	}
	
	private boolean evaluatePointcutCondition(String xpath, String xmlData){
		//conditionValue has an xpath which should be evaluated using data
		if(xpath.equalsIgnoreCase("true".trim()))
			return true;
		else if(xmlData!=null && xmlData!="")
			try {
				DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
			    domFactory.setNamespaceAware(false);
		    	InputSource inputSource = new InputSource(new StringReader(xmlData));
		        XPath xPath = XPathFactory.newInstance().newXPath();
		        Boolean result = (Boolean) xPath.evaluate(xpath, inputSource, XPathConstants.BOOLEAN);
		        return result;
		    } catch (Exception e) {
		    	System.out.println("some eeroors happened !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		    }
		
		return false;
	}

	private void suspendCase(String caseID){
		connect();
		try {
			List<WorkItemRecord> wirList = null;
			wirList = _ibClient.getLiveWorkItemsForIdentifier("case", caseID, _handle);
			
			System.out.println("---- There are totally " + (wirList==null?"null":wirList.size()) + " workitems");
			for(WorkItemRecord i : wirList){
				if(! _ibClient.successful(_ibClient.suspendWorkItem(i.getID(), _handle))){
					System.out.println(" * problem is suspending the workitem with id " + i.getID());
				}
			}
		} catch (Exception e1) {
			System.out.println("---- error is suspendsion:" + e1.getMessage());
			e1.printStackTrace();
		}
	}
	
	private boolean isUploaded(String adviceID) throws IOException {
		connect();
        if (adviceID == null) return false;
		List<SpecificationData> _loadedSpecs = new ArrayList<SpecificationData>();
        _loadedSpecs = _ibClient.getSpecificationList(_handle);
         for (SpecificationData onespec : _loadedSpecs) {
             if (adviceID.equals(onespec.getSpecIdentifier())) return true ;
         }
         return false;
    }
	
	protected boolean uploadAdvice(String adviceName) throws IOException {
		connect();
	 	String fileName = _specFolder + adviceName + ".yawl"; 
	 	
    	if (isUploaded(getAdviceID(adviceName))) {
            return true ;
        }

        //upload spec
        try {
        	String engineURI = "http://localhost:8080/yawl/ia";
        	
        	InterfaceA_EnvironmentBasedClient _interfaceAClient =
                    new InterfaceA_EnvironmentBasedClient(engineURI);
        	String h = _interfaceAClient.connect("admin", "YAWL");

        	
    		File file = new File(fileName);
    		if (file.exists()){
    			System.out.println("- - - - - - file name is " + file.getName());
    			System.out.println("- - - - - - file path is " + file.getPath());
    			System.out.println("- - - - - - file getAbsolutePath() is " + file.getAbsolutePath());
    			System.out.println("- - - - - - _handle is " + h);
    			System.out.println("- - - - - - _interfaceAClient.getBackEndURI() is " + _interfaceAClient.getBackEndURI());
    			
	    		String result = _interfaceAClient.uploadSpecification(file, h);
	    		
	    		if (_ibClient.successful(result)) {
	                //_log.info("Successfully uploaded worklet specification: " + workletName) ;
	                return true ;
	            } else {
	                //_log.error("Unsuccessful worklet specification upload : " + workletName)	;
	                return false ;
	            }
    		}else{
                //_log.error("Unsuccessful worklet specification upload : " + workletName)	;
    			System.out.println("Aspect not exists");
    		}
        }
        catch (IOException ioe) {
            //_log.error("Unsuccessful worklet specification upload : " + workletName)	;
            return false ;
        }
        return false;
    }
	
	private String mapItemParamsToAdviceCaseParams(String adviceName, List<YParameter> inAspectParams, String data) {
		org.jdom.Element result = new org.jdom.Element(adviceName);
		
		SAXBuilder builder = new SAXBuilder();
		org.jdom.Element root = null;
	    try {
	    	org.jdom.Document document = builder.build(new StringReader(data));
	    	root = document.getRootElement();
    	} catch (Exception e) {
    		System.out.println(e);
	    }
	   
	    for (YParameter param : inAspectParams) {
	    	 String paramName = param.getName() ;
		     org.jdom.Element advElem = root.getChild(paramName) ;
		     try {
		    	 if (advElem != null) {
		    		 org.jdom.Element copy = (org.jdom.Element) advElem.clone() ;
		    		 result.addContent(copy) ;
	    		 } else {
	    			 result.addContent(new org.jdom.Element(paramName)) ;
    			 }
		     }
	         catch (org.jdom.IllegalAddException iae) {
	        	 System.out.println("+++++++++++++++++ error:" + iae.getMessage());
		                //_log.error("Exception adding content to worklet data list", iae) ;
	         }
	    }
	    return JDOMUtil.elementToString(result);
	}
	
	private String launchAdvice(String  adviceID, String caseParams) throws IOException {
		connect();
		return _ibClient.launchCase(adviceID, caseParams, _handle, _observer);
	}
	
	private void weaving(String caseID, String adviceJoinPointID, boolean isCaseConstraintcalled){
		AdviceQuery query = new AdviceQuery();
		List<Advice> advs = query.getAdvicesByAdviceJoinPointID(adviceJoinPointID);
		boolean unsuspend = true;
		if(advs!=null && advs.size()>0){
			for(Advice adv : advs){
				if (adv.isSynched()==false)
					unsuspend = false;
			}
		}
		
		System.out.println("==== ===== " + (advs==null?"null":advs.size()));
		if(unsuspend || advs==null || advs.size()==0){
			unsuspendCase(caseID);
		}
	}
	
	private void unsuspendCase(String caseID){
		connect();
		try {
			List<WorkItemRecord> wirList = _ibClient.getWorkItemsForCase(caseID, _handle);
			for(WorkItemRecord i : wirList){
				if(i.getStatus().equalsIgnoreCase("Suspended")){
					_ibClient.unsuspendWorkItem(i.getID(), _handle);
//					List<WorkItemRecord> childList = _ibClient.getChildrenOfWorkItem(i.getID(), _handle);
//					if(childList!=null && childList.size()>0){
//						for(int index=0; index<childList.size(); index++)
//							_ibClient.unsuspendWorkItem(childList.get(index).getID(), _handle);
//					}
				}
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	private String getAdviceID(String adviceName){
		if (adviceName != null) {
			String specFileName = _specFolder + adviceName + ".yawl";
			
			File file = new File(specFileName);
    		if (file.exists()){
    			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    			DocumentBuilder dBuilder = null;
    			Document doc = null;
    			try {
					dBuilder = dbFactory.newDocumentBuilder();
					doc = dBuilder.parse(specFileName);
				} catch (ParserConfigurationException e) {
					e.printStackTrace();
				} catch (SAXException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

    			if(dBuilder==null || doc==null)
    				return "";

    			doc.getDocumentElement().normalize();
    			
    			
    			NodeList nList = doc.getElementsByTagName("identifier");
    			return nList.item(0).getTextContent();
    		} else {
    			return ""; 
    		}
		}
		return "";
	}
	
	private org.jdom.Element getDataforCase(String specName, String caseID, String data) {
		String caseData = "";
		try {
			caseData = _ibClient.getCaseData(caseID, _handle);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// result is the return
		org.jdom.Element result = new org.jdom.Element(specName);
		
		SAXBuilder builder = new SAXBuilder();
		org.jdom.Element root = null;
		List<org.jdom.Element> ls = null;
	    try {
	    	org.jdom.Document document = builder.build(new StringReader(data));
	    	root = document.getRootElement();
	    	
	    	ls = document.getRootElement().getChildren();
    	} catch (Exception e) {
    		System.out.println(e);
	    }
	   
	    for(org.jdom.Element l: ls){
	    	//System.out.println("+++++++++++++++++l: " + l.toString());///////////
	    		String paramName = l.getName(); //"Name" ;
		       org.jdom.Element advElem = root.getChild(paramName) ;
		       //System.out.println("+++++++++++++++++advElem.toString(): " + advElem.toString());///////////
		       try {
		           if (advElem != null) {
		        	   org.jdom.Element copy = (org.jdom.Element) advElem.clone() ;
		        	   result.addContent(copy) ;
		           } else {
		        	   result.addContent(new org.jdom.Element(paramName)) ;
		           }
	          }
	          catch (org.jdom.IllegalAddException iae) {
		                //_log.error("Exception adding content to worklet data list", iae) ;
	          }
	    }
	    return result;
	}
	
	private List<YParameter> getInputParams(String specId) {
		connect();
		List<SpecificationData> loadedSpecs = new ArrayList<SpecificationData>() ;
        try {
            loadedSpecs = _ibClient.getSpecificationList(_handle);
        }
        catch (IOException ioe) {
            //_log.error("IO Exception in getLoadedSpecs", ioe);
        }
        
        // locate input params for the specified spec id
       for (SpecificationData thisSpec : loadedSpecs) {
    	   if (specId.equals(thisSpec.getSpecIdentifier()))
    		   return thisSpec.getInputParams() ;
       }
       
       return null ;
	}
	//end of Implementation =======================================================================================

	//InterfaceB ----------------------
//	@Override
	public void handleCancelledWorkItemEvent(WorkItemRecord workItemRecord) {
	}
//	@Override
    public void handleCompleteCaseEvent(String caseID, String casedata) { 
	}
	//InterfaceX ----------------------
//	@Override
	public String handleWorkItemAbortException(WorkItemRecord wir,
			String caseData) {
		return null;
	}
//	@Override
	public void handleTimeoutEvent(WorkItemRecord wir, String taskList) {
	}
//	@Override
	public void handleResourceUnavailableException(String resourceID,
			WorkItemRecord wir, String caseData, boolean primary) {
	}
//	@Override
	public String handleConstraintViolationException(WorkItemRecord wir,
			String caseData) {
		return null;
	}
//	@Override
	public void handleCaseCancellationEvent(String caseID) {
	}
	
	//Not used =======================================================================================
//public WorkItemRecord getWorkItemRecord(String itemID) {
//    connect();
//	try {
//        return getEngineStoredWorkItem(itemID, _handle);
//    }
//    catch (IOException ioe) {
//        return null ;
//    }
//}
//
//private boolean suspendWorkitem(String id) throws IOException
//{
//	connect();
//    String msg = _ibClient.suspendWorkItem(id, _handle);
//    return _ibClient.successful(msg);
//}
}
