package net.heimeng.sdk.btapi.api.file;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONException;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import net.heimeng.sdk.btapi.api.BaseBtApi;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;
import net.heimeng.sdk.btapi.model.file.DirectoryListing;
import net.heimeng.sdk.btapi.model.file.FileEntry;

/**
 * 列出目录内容的 API。
 *
 * <p>对应面板 9.0 文件管理的目录列表：{@code files?action=GetDirNew}，参数为 {@code path}、{@code p}、{@code
 * showRow}、{@code sort} 和 {@code reverse}。默认值与 UI 一致：第 1 页、每页 500 条、{@code sort} 为空、{@code
 * reverse=True}。旧的 {@code files?action=GetDir} 在 9.0 上返回空列表。
 *
 * <p>路径会经过 {@link RemotePaths#requireSafeAbsolutePath(String)} 校验。
 *
 * <p>面板在路径不存在或不是目录时不会报错，而是返回另一个目录的列表：不存在的路径返回 {@code /www/wwwroot}，文件路径返回其父目录。 本类比较响应中的 {@code
 * path} 与请求的路径，不一致时返回失败结果（{@code data} 为 {@code null}），客户端据此抛出 {@link BtApiException}。
 */
public class GetDirectoryListingApi extends BaseBtApi<BtResult<DirectoryListing>> {

  private static final String ENDPOINT = "files?action=GetDirNew";

  /** UI 默认每页条目数。 */
  public static final int DEFAULT_ROWS = 500;

  private static final Pattern CURRENT_PAGE = classPattern("Pcurrent", "(\\d+)");
  private static final Pattern PAGE_NUMBER = classPattern("Pnumber", "(\\d+)\\s*/\\s*(\\d+)");
  private static final Pattern TOTAL_COUNT = classPattern("Pcount", "\\D*(\\d+)");

  public GetDirectoryListingApi() {
    super(ENDPOINT, HttpMethod.POST);
    setPage(1);
    setRows(DEFAULT_ROWS);
    setSort("");
    setReverse(true);
  }

  /**
   * 设置要列出的目录。
   *
   * @param path 服务器上的目录绝对路径，会被规范化（合并重复的 {@code /}，去掉末尾的 {@code /}）
   * @return 当前 API 实例
   * @throws IllegalArgumentException 路径不是安全的绝对路径
   */
  public GetDirectoryListingApi setPath(String path) {
    addParam("path", RemotePaths.requireSafeAbsolutePath(path));
    return this;
  }

  /**
   * 设置页码（{@code p}）。
   *
   * @param page 页码，从 1 开始
   * @return 当前 API 实例
   * @throws IllegalArgumentException 页码小于 1
   */
  public GetDirectoryListingApi setPage(int page) {
    if (page < 1) {
      throw new IllegalArgumentException("page must be >= 1: " + page);
    }
    addParam("p", String.valueOf(page));
    return this;
  }

  /**
   * 设置每页条目数（{@code showRow}），目录和文件合计。
   *
   * @param rows 每页条目数
   * @return 当前 API 实例
   * @throws IllegalArgumentException 条目数小于 1
   */
  public GetDirectoryListingApi setRows(int rows) {
    if (rows < 1) {
      throw new IllegalArgumentException("rows must be >= 1: " + rows);
    }
    addParam("showRow", String.valueOf(rows));
    return this;
  }

  /**
   * 设置排序字段（{@code sort}）。
   *
   * @param sort 排序字段，原样发送给面板；{@code null} 或空字符串表示面板默认排序（与 UI 默认一致）
   * @return 当前 API 实例
   */
  public GetDirectoryListingApi setSort(String sort) {
    addParam("sort", sort == null ? "" : sort);
    return this;
  }

  /**
   * 设置是否倒序（{@code reverse}，发送 {@code True} 或 {@code False}）。
   *
   * @param reverse 是否倒序，UI 默认为 {@code true}
   * @return 当前 API 实例
   */
  public GetDirectoryListingApi setReverse(boolean reverse) {
    addParam("reverse", reverse ? "True" : "False");
    return this;
  }

  @Override
  protected boolean validateParams() {
    return params.get("path") instanceof String path && !path.isBlank();
  }

  @Override
  public BtResult<DirectoryListing> parseResponse(String response) {
    if (response == null || response.isBlank()) {
      throw new BtApiException("Empty response received");
    }

    String normalizedResponse = response.trim();
    try {
      if (!JSONUtil.isTypeJSON(normalizedResponse)) {
        throw new BtApiException("Invalid JSON response: " + normalizedResponse);
      }

      JSON json = JSONUtil.parse(normalizedResponse);
      if (!(json instanceof JSONObject jsonObject)) {
        throw new BtApiException("Directory listing response must be a JSON object");
      }

      BtResult<DirectoryListing> result = new BtResult<>();
      if (!jsonObject.containsKey("dir") && !jsonObject.containsKey("files")) {
        if (!jsonObject.containsKey("status")) {
          throw new BtApiException("Directory listing response is missing dir and files fields");
        }
        boolean status = jsonObject.getBool("status", false);
        result.setStatus(status);
        result.setMsg(jsonObject.getStr("msg", status ? "获取成功" : "获取失败"));
        return result;
      }

      DirectoryListing listing = toListing(jsonObject);
      String requestedPath = params.get("path") instanceof String path ? path : null;
      if (requestedPath != null && !requestedPath.equals(normalizeListedPath(listing.getPath()))) {
        result.setStatus(false);
        result.setMsg(
            "Not a directory or does not exist: "
                + requestedPath
                + " (panel listed "
                + listing.getPath()
                + " instead)");
        return result;
      }

      result.setStatus(true);
      result.setMsg("获取成功");
      result.setData(listing);
      return result;
    } catch (JSONException exception) {
      throw new BtApiException("Invalid JSON response: " + normalizedResponse, exception);
    }
  }

  private static DirectoryListing toListing(JSONObject json) {
    DirectoryListing listing = new DirectoryListing();
    listing.setPath(json.getStr("path"));
    listing.setDirectories(toEntries(json.getJSONArray("dir"), true));
    listing.setFiles(toEntries(json.getJSONArray("files"), false));

    String pageHtml = json.getStr("page");
    listing.setPageHtml(pageHtml);
    if (pageHtml != null) {
      Matcher pageNumber = PAGE_NUMBER.matcher(pageHtml);
      if (pageNumber.find()) {
        listing.setPage(Integer.parseInt(pageNumber.group(1)));
        listing.setTotalPages(Integer.parseInt(pageNumber.group(2)));
      } else {
        Matcher currentPage = CURRENT_PAGE.matcher(pageHtml);
        if (currentPage.find()) {
          listing.setPage(Integer.parseInt(currentPage.group(1)));
        }
      }
      Matcher totalCount = TOTAL_COUNT.matcher(pageHtml);
      if (totalCount.find()) {
        listing.setTotalCount(Integer.parseInt(totalCount.group(1)));
      }
    }

    listing.setRaw(Collections.unmodifiableMap(new LinkedHashMap<>(json)));
    return listing;
  }

  private static List<FileEntry> toEntries(JSONArray array, boolean directory) {
    if (array == null || array.isEmpty()) {
      return List.of();
    }
    List<FileEntry> entries = new ArrayList<>(array.size());
    for (int i = 0; i < array.size(); i++) {
      JSONObject item = array.getJSONObject(i);
      if (item == null) {
        continue;
      }
      FileEntry entry = new FileEntry();
      entry.setName(item.getStr("nm", ""));
      entry.setDirectory(directory);
      entry.setSize(item.getLong("sz", 0L));
      entry.setModifiedTime(item.getLong("mt", 0L));
      entry.setPermissions(item.getStr("acc", ""));
      entry.setOwner(item.getStr("user", ""));
      entry.setLinkTarget(item.getStr("lnk", ""));
      entry.setRemark(item.getStr("rmk", ""));
      entry.setFavorite("1".equals(item.getStr("fav")));
      entry.setPinned(item.getInt("top", 0) != 0);
      entry.setRaw(Collections.unmodifiableMap(new LinkedHashMap<>(item)));
      entries.add(entry);
    }
    return Collections.unmodifiableList(entries);
  }

  private static String normalizeListedPath(String path) {
    if (path == null) {
      return null;
    }
    try {
      return RemotePaths.requireSafeAbsolutePath(path);
    } catch (IllegalArgumentException exception) {
      return path;
    }
  }

  private static Pattern classPattern(String className, String valuePattern) {
    return Pattern.compile("class=['\"]" + className + "['\"][^>]*>" + valuePattern);
  }
}
