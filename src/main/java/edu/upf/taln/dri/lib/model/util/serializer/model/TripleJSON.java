package edu.upf.taln.dri.lib.model.util.serializer.model;


public class TripleJSON {
	
	private Integer id;
	
	private String relation;
	
	private String fromName;
	private Integer fromId;
	
	private String toName;
	private Integer toId;
	
	
	
	// Constructor
	public TripleJSON(String relation) {
		super();
		this.relation = relation;
	}
	
	public TripleJSON(String relation, String fromName, Integer fromId,
			String toName, Integer toId) {
		super();
		this.relation = relation;
		this.fromName = fromName;
		this.fromId = fromId;
		this.toName = toName;
		this.toId = toId;
	}
	
	
	
	// Setters and getters
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}
	
	public String getRelation() {
		return relation;
	}
	
	public void setRelation(String relation) {
		this.relation = relation;
	}

	public String getFromName() {
		return fromName;
	}

	public void setFromName(String fromName) {
		this.fromName = fromName;
	}

	public Integer getFromId() {
		return fromId;
	}

	public void setFromId(Integer fromId) {
		this.fromId = fromId;
	}

	public String getToName() {
		return toName;
	}

	public void setToName(String toName) {
		this.toName = toName;
	}

	public Integer getToId() {
		return toId;
	}

	public void setToId(Integer toId) {
		this.toId = toId;
	}
	
}
