package net.heimeng.sdk.btapi.model.website;

import lombok.Data;

/**
 * 网站分类模型类，用于表示宝塔面板中的网站分类信息
 * <p>
 * 包含分类的ID和名称。
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
@Data
public class WebsiteType {
    
    /**
     * 分类ID
     */
    private Integer id;
    
    /**
     * 分类名称
     */
    private String name;
}