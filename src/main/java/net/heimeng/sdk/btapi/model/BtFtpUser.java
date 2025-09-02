package net.heimeng.sdk.btapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

/**
 * FTP用户信息模型
 *
 * @author InwardFlow
 */
@Data
public class BtFtpUser {
    /**
     * FTP用户ID
     */
    private int id;

    /**
     * FTP用户名
     */
    private String name;

    /**
     * FTP用户密码
     */
    private String password;

    /**
     * FTP用户目录
     */
    private String path;

    /**
     * FTP用户状态
     */
    private boolean status;

    /**
     * FTP用户权限
     */
    private String perm;

    /**
     * FTP用户创建时间
     */
    @JsonProperty("addtime")
    private Date addTime;

    /**
     * FTP用户备注
     */
    private String ps;
}