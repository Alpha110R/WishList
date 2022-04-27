package iob.data;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

@Entity
@Table(name="INSTANCE")
public class InstanceEntity {

	private String instanceId, type, name, createdByEmail, createdByDomain;
	private boolean active;
	private Date createdTimestamp;
	private double lng, lat;
	private String instanceAttributes;
	
	public InstanceEntity() {
		
	}
	
	public InstanceEntity(String id, String type, String name, boolean active, Date createdTimestamp,
			String createdByEmail, String createdByDomain, Double lat, Double lng) {
		
		super();
		this.instanceId = id;
		this.type = type;
		this.name = name;
		this.active = active;
		this.createdTimestamp = createdTimestamp;
		this.createdByEmail = createdByEmail;
		this.createdByDomain = createdByDomain;
		this.lat = lat;
		this.lng = lng;
	}
	
	@Id
	public String getInstanceId() {
		return instanceId;
	}
	public void setInstanceId(String id) {
		this.instanceId = id;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}
	public Date getCreatedTimestamp() {
		return createdTimestamp;
	}
	public void setCreatedTimestamp(Date createdTimestamp) {
		this.createdTimestamp = createdTimestamp;
	}
	public String getCreatedByEmail() {
		return createdByEmail;
	}
	public void setCreatedByEmail(String createdByEmail) {
		this.createdByEmail = createdByEmail;
	}
	public String getCreatedByDomain() {
		return createdByDomain;
	}
	public void setCreatedByDomain(String createdByDomain) {
		this.createdByDomain = createdByDomain;
	}
	public Double getLat() {
		return lat;
	}
	public void setLat(Double lat) {
		this.lat = lat;
	}
	public Double getLng() {
		return lng;
	}
	public void setLng(Double lng) {
		this.lng = lng;
	}
	
	@Lob
	public String getInstanceAttributes() {
		return instanceAttributes;
	}

	public void setInstanceAttributes(String attributes) {
		this.instanceAttributes = attributes;
	}
	
	
}
