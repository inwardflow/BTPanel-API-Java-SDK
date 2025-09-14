package net.heimeng.sdk.btapi.model.website;

import lombok.Data;

/**
 * PHP版本模型类，用于表示宝塔面板中已安装的PHP版本信息
 * <p>
 * 包含PHP版本标识和版本名称。
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
@Data
public class PhpVersion {
    
    /**
     * 版本标识
     */
    private String version;
    
    /**
     * 版本名称
     */
    private String name;
}