package iob.data;


import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;


/*
MESSAGES Table
ACTIVITYID      | TYPE         | INSTANCE     | CREATEDTIMESTAMP | INVOKEDBY    | ACTIVITYATTRIBUTES    |
VARCHAR(255)    | VARCHAR(255) | VARCHAR(255) | TIMESTAMP        | VARCHAR(255) | CLOB					|        
  <PK>          |              |              |                  |			    |              			|
 */
@Entity
@Table(name="ACTIVITY")
public class ActivityEntity {
	
	private String activityId;
	private String type;			
	private String instance;
	private Date createdTimestamp;
	private String invokedBy;
	private String activityAttributes;
	
	public ActivityEntity() {}

	@Id
	public String getActivityId() {
		return activityId;
	}

	public void setActivityId(String activityId) {
		this.activityId = activityId;
	}

	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}

	public String getInstance() {
		return instance;
	}

	public void setInstance(String instance) {
		this.instance = instance;
	}

	public Date getCreatedTimestamp() {
		return createdTimestamp;
	}

	public void setCreatedTimestamp(Date createdTimestamp) {
		this.createdTimestamp = createdTimestamp;
	}

	public String getInvokedBy() {
		return invokedBy;
	}

	public void setInvokedBy(String invokedBy) {
		this.invokedBy = invokedBy;
	}

	@Lob
	public String getActivityAttributes() {
		return activityAttributes;
		
	}

	public void setActivityAttributes(String activityAttributes) {
		this.activityAttributes = activityAttributes;
	};
	

}
