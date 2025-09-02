package net.heimeng.sdk.btapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

/**
 * 网站信息模型
 *
 * @author InwardFlow
 */
@Data
public class BtSite {
    /**
     * 网站ID
     */
    private int id;

    /**
     * 网站域名
     */
    private String name;

    /**
     * 网站目录
     */
    private String path;

    /**
     * 网站状态
     */
    private boolean status;

    /**
     * 网站创建时间
     */
    @JsonProperty("addtime")
    private Date addTime;

    /**
     * PHP版本
     */
    private String phpVersion;

    /**
     * 数据库名称
     */
    @JsonProperty("database_name")
    private String databaseName;

    /**
     * 是否有SSL证书
     */
    @JsonProperty("has_ssl")
    private boolean hasSsl;

    /**
     * 运行模式
     */
    private String type;

    /**
     * 网站备注
     */
    private String ps;
}