package iob.restapi.boundaries;

import java.util.Date;
import java.util.Map;

import iob.restapi.objects.ActivityId;
import iob.restapi.objects.Instance;
import iob.restapi.objects.InvokedBy;

public class ActivityBoundary {
	
	private ActivityId activityId;
	private String type;			// may change in the future to enum
	private Instance instance;
	private Date createdTimestamp;
	private InvokedBy invokedBy;
	private Map<String,Object> activityAttributes;
	
	public ActivityBoundary() {
	}

	public ActivityBoundary(ActivityId activityId, String type, Instance instance, Date createdTimestamp,
			InvokedBy invokedBy, Map<String,Object> activityAttributes) {
		super();
		this.activityId = activityId;
		this.type = type;
		this.instance = instance;
		this.createdTimestamp = createdTimestamp;
		this.invokedBy = invokedBy;
		this.activityAttributes = activityAttributes;
	}
	

	public ActivityId getActivityId() {
		return activityId;
	}

	public void setActivityId(ActivityId activityId) {
		this.activityId = activityId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Instance getInstance() {
		return instance;
	}

	public void setInstance(Instance instance) {
		this.instance = instance;
	}

	public Date getCreatedTimestamp() {
		return createdTimestamp;
	}

	public void setCreatedTimestamp(Date createdTimestamp) {
		this.createdTimestamp = createdTimestamp;
	}

	public InvokedBy getInvokedBy() {
		return invokedBy;
	}

	public void setInvokedBy(InvokedBy invokedBy) {
		this.invokedBy = invokedBy;
	}

	public Map<String,Object> getActivityAttributes() {
		return activityAttributes;
	}

	public void setActivityAttributes(Map<String,Object> activityAttributes) {
		this.activityAttributes = activityAttributes;
	}

	@Override
	public String toString() {
		return "ActivityBoundary [activityId=" + activityId + ", type=" + type + ", instance=" + instance
				+ ", createdTimestamp=" + createdTimestamp + ", invokedBy=" + invokedBy + ", activityAttributes="
				+ activityAttributes + "]";
	}
	
}
