package ykk.xc.com.glswms.bean;

/**
 * 角色
 * @author Administrator
 *
 */
public class Role {

	/*角色id*/
	private Integer id;

	/*角色名称*/
	private String rolename;

	/*角色描述*/
	private String roledesc;

	/*创建时间*/
	private String createTime;

	/*创建者id*/
	private Integer createrId;

	/*创建者名称*/
	private String createrName;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getRolename() {
		return rolename;
	}

	public void setRolename(String rolename) {
		this.rolename = rolename;
	}

	public String getRoledesc() {
		return roledesc;
	}

	public void setRoledesc(String roledesc) {
		this.roledesc = roledesc;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public Integer getCreaterId() {
		return createrId;
	}

	public void setCreaterId(Integer createrId) {
		this.createrId = createrId;
	}

	public String getCreaterName() {
		return createrName;
	}

	public void setCreaterName(String createrName) {
		this.createrName = createrName;
	}

	public Role() {
		super();
	}

	@Override
	public String toString() {
		return "Role [id=" + id + ", rolename=" + rolename + ", roledesc=" + roledesc + ", createTime=" + createTime
				+ ", createrId=" + createrId + ", createrName=" + createrName + "]";
	}


}