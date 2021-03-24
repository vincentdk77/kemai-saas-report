package entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SaasOrder implements Serializable {

    private Integer id;

    private Integer tenantId;

    private Double orderPrice;

    private Double discountPrice;

    private Date createTime;

    private Date updateTime;

    private Double realPrice;

    private Double finalPrice;

    private Double actuallyPrice;

    private Integer status;

    private Date auditTime;

    private Integer userId;

    private Integer createAdminId;

    private Integer type;

    private Integer auditAdminId;

    private Integer year;

    private String payAccout;

    private String payName;

    private String payOpenBank;

    private Integer payType;

    private Integer servicesId;

    private String note;

    private String payIdentify;

    private String auditNote;

    private Date payTime;

    private Date beginTime;

    private Date closeTime;

    private String orderNo;

    private String createServiceUserId;

    private Integer orderProperty;

    private String customerProtectionTenantName;

    private Integer governorAuditAdminId;

    private String governorAuditNote;

    private Date governorAuditTime;

    private int governorAuditStatus;

    private int auditStatus;

    private Date releaseTime;

    private Integer releaseAdminId;

    private int releaseStatus;

    private Integer isRenew;

}
