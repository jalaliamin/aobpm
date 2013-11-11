package org.yawlfoundation.yawl.aspectService;

public class Advice {
	private long id; // a hibernate primary key
    
	private String parentSpecID;
	private String parentSpecName;
	private String parentCaseID;
	private String adjpID;
	private String adjpChildID;
	private String adjpDataString;
	
	private String adviceName;
	private String adviceCaseID;
	private String proceedID;
	private boolean synched;
    private String proceedName;
    
	public Advice(){}

	public String getProceedName() {
		return proceedName;
	}

	public void setProceedName(String proceedName) {
		this.proceedName = proceedName;
	}

	public String getAdviceName() {
		return adviceName;
	}

	public void setAdviceName(String adviceName) {
		this.adviceName = adviceName;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	public String getParentSpecID() {
		return parentSpecID;
	}

	public void setParentSpecID(String parentSpecID) {
		this.parentSpecID = parentSpecID;
	}

	public String getAdjpChildID() {
		return adjpChildID;
	}

	public void setAdjpChildID(String adjpChildID) {
		this.adjpChildID = adjpChildID;
	}

	public String getParentSpecName() {
		return parentSpecName;
	}

	public void setParentSpecName(String parentSpecName) {
		this.parentSpecName = parentSpecName;
	}

	public String getParentCaseID() {
		return parentCaseID;
	}

	public void setParentCaseID(String parentCaseID) {
		this.parentCaseID = parentCaseID;
	}

	public String getAdjpID() {
		return adjpID;
	}

	public void setAdjpID(String adjpID) {
		this.adjpID = adjpID;
	}

	public String getAdjpDataString() {
		return adjpDataString;
	}

	public void setAdjpDataString(String adjpDataString) {
		this.adjpDataString = adjpDataString;
	}

	public String getAdviceCaseID() {
		return adviceCaseID;
	}

	public void setAdviceCaseID(String adviceCaseID) {
		this.adviceCaseID = adviceCaseID;
	}

	public String getProceedID() {
		return proceedID;
	}

	public void setProceedID(String proceedID) {
		this.proceedID = proceedID;
	}

	public boolean isSynched() {
		return synched;
	}

	public void setSynched(boolean synched) {
		this.synched = synched;
	}

}
