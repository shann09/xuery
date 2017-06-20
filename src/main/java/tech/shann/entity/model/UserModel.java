package tech.shann.entity.model;

import tech.shann.entity.User;

/**
 * Created by shann on 17/6/20.
 */
public class UserModel extends User{
    private Long companyId;
    private String companyName;

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }
}
