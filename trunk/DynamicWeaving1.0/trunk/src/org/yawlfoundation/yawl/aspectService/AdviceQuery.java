package org.yawlfoundation.yawl.aspectService;

import java.util.List;

public class AdviceQuery {
	private Persister _reader ;
	private static final String _baseQuery = "FROM Advice AS adv";

	public AdviceQuery() {
		super();
		_reader = Persister.getInstance();
	}
	
	public List getByAdviceCaseID(String caseID){
		String query = String.format("%s WHERE adv.adviceCaseID = '%s'", _baseQuery, caseID);
		return execQuery(query);
	}
	
	public List getByAdviceName(String adviceName){
		String query = String.format("%s WHERE adv.adviceName = '%s'", _baseQuery, adviceName);
		return execQuery(query);
	}
	
	public List getAdvicesByAdviceJoinPointID(String adviceJoinPointID){
		String query = String.format("%s WHERE adv.adjpID = '%s'", _baseQuery, adviceJoinPointID);
		return execQuery(query);
	}
	
	private List execQuery(String query) {
        List rows = null;
        if (_reader != null) {
            rows = _reader.execQuery(query) ;
            _reader.commit();
        }
        return rows;
    }
}
