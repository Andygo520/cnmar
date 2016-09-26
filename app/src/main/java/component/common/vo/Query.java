package component.common.vo;

import java.util.Date;

import org.apache.commons.lang.StringUtils;

public class Query implements Cloneable {

	private String username;
	private String name;
	private String code;
	private String supplyCode;

	private int pid;

	private Boolean isEnable;

	private Integer stockType;
	private Integer mixType;
	private Integer status;

	private Date sdate;
	private Date edate;

	public static final Query getInstance(Query query) throws Exception {
		Query clone = (Query) query.clone();
		if (StringUtils.isNotEmpty(clone.getUsername())) {
			clone.setUsername("%" + clone.getUsername() + "%");
		}
		if (StringUtils.isNotEmpty(clone.getName())) {
			clone.setName("%" + clone.getName() + "%");
		}
		if (StringUtils.isNotEmpty(clone.getCode())) {
			clone.setCode("%" + clone.getCode() + "%");
		}
		if (StringUtils.isNotEmpty(clone.getSupplyCode())) {
			clone.setSupplyCode("%" + clone.getSupplyCode() + "%");
		}
		return clone;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getSupplyCode() {
		return supplyCode;
	}

	public void setSupplyCode(String supplyCode) {
		this.supplyCode = supplyCode;
	}

	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	public Boolean getIsEnable() {
		return isEnable;
	}

	public void setIsEnable(Boolean isEnable) {
		this.isEnable = isEnable;
	}

	public Integer getStockType() {
		return stockType;
	}

	public void setStockType(Integer stockType) {
		this.stockType = stockType;
	}

	public Integer getMixType() {
		return mixType;
	}

	public void setMixType(Integer mixType) {
		this.mixType = mixType;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Date getSdate() {
		return sdate;
	}

	public void setSdate(Date sdate) {
		this.sdate = sdate;
	}

	public Date getEdate() {
		return edate;
	}

	public void setEdate(Date edate) {
		this.edate = edate;
	}

}
