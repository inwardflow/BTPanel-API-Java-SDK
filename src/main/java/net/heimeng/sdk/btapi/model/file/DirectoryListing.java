package net.heimeng.sdk.btapi.model.file;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.Data;

/**
 * 目录列表的一页（{@code files?action=GetDirNew} 的响应）。
 *
 * <p>面板把目录和文件合并分页，目录排在前面：每页最多 {@code showRow} 个条目，其中一部分可能在 {@link #getDirectories()}，其余在 {@link
 * #getFiles()}。分页信息只以 HTML 片段返回，SDK 从中解析出 {@link #getPage()}、{@link #getTotalPages()} 和 {@link
 * #getTotalCount()}；无法解析时对应值为 {@code -1}，原始片段见 {@link #getPageHtml()}。
 *
 * <p>响应中的其他字段（例如 {@code dir_history}、{@code store}、{@code file_recycle}）可通过 {@link #getRaw()} 读取。
 */
@Data
public class DirectoryListing {

  /** 面板实际列出的目录。 */
  private String path;

  /** 本页的子目录。 */
  private List<FileEntry> directories = List.of();

  /** 本页的文件。 */
  private List<FileEntry> files = List.of();

  /** 当前页码，从 1 开始；无法解析时为 {@code -1}。 */
  private int page = -1;

  /** 总页数；无法解析时为 {@code -1}。 */
  private int totalPages = -1;

  /** 目录与文件的条目总数（所有页）；无法解析时为 {@code -1}。 */
  private int totalCount = -1;

  /** 面板返回的分页 HTML 片段（{@code page}）。 */
  private String pageHtml;

  /** 面板返回的原始字段，只读。 */
  private Map<String, Object> raw = Map.of();

  /** 按面板顺序返回本页的全部条目：先目录，后文件。 */
  public List<FileEntry> getEntries() {
    List<FileEntry> entries = new ArrayList<>(directories.size() + files.size());
    entries.addAll(directories);
    entries.addAll(files);
    return entries;
  }

  /** 判断是否还有下一页。分页信息无法解析时返回 {@code false}。 */
  public boolean hasNextPage() {
    return page > 0 && totalPages > 0 && page < totalPages;
  }
}
