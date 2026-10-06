package net.heimeng.sdk.btapi.model.file;

import java.time.Instant;
import java.util.Map;

import lombok.Data;

/**
 * 目录列表中的一个条目（{@code files?action=GetDirNew} 返回的 {@code dir} 或 {@code files} 数组元素）。
 *
 * <p>面板使用缩写字段名，本类按 2026-10-03 在面板 9.0 上抓取的响应映射常用字段。其余字段（例如 {@code durl}、{@code cmp}、{@code
 * sn}）及面板后续新增的字段可通过 {@code getRaw()} 读取。
 */
@Data
public class FileEntry {

  /** 文件或目录名（{@code nm}），不含路径。 */
  private String name;

  /** 是否为目录（来自 {@code dir} 数组）。 */
  private boolean directory;

  /** 大小，单位字节（{@code sz}）。目录为目录项本身的大小，通常是 4096，不是目录内容的总大小。 */
  private long size;

  /** 修改时间，Unix 时间戳，单位秒（{@code mt}）。 */
  private long modifiedTime;

  /** 权限，八进制字符串，例如 {@code 755}（{@code acc}）。 */
  private String permissions;

  /** 所有者用户名（{@code user}）。 */
  private String owner;

  /** 符号链接的目标路径（{@code lnk}），不是符号链接时为空字符串。 */
  private String linkTarget;

  /** 面板中为该条目设置的备注（{@code rmk}）。 */
  private String remark;

  /** 是否已在面板中收藏（{@code fav}）。 */
  private boolean favorite;

  /** 是否已在面板中置顶（{@code top}）。 */
  private boolean pinned;

  /** 面板返回的原始字段，只读。 */
  private Map<String, Object> raw = Map.of();

  /** 返回修改时间。 */
  public Instant getModifiedAt() {
    return Instant.ofEpochSecond(modifiedTime);
  }

  /** 判断该条目是否为符号链接。 */
  public boolean isSymlink() {
    return linkTarget != null && !linkTarget.isEmpty();
  }
}
