package net.heimeng.sdk.btapi.api.file;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import net.heimeng.sdk.btapi.api.BtApi;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;
import net.heimeng.sdk.btapi.model.file.DirectoryListing;
import net.heimeng.sdk.btapi.model.file.FileEntry;

@DisplayName("GetDirectoryListingApi 单元测试")
class GetDirectoryListingApiTest {

  private static final String DIR = "/www/wwwroot/test.example.com/it-capture";

  /** 2026-10-03 在面板 9.0 上抓取的响应：1 个子目录、2 个文件，showRow=500。dir_history 已脱敏。 */
  private static final String FULL_PAGE =
      """
      {"store": [], "file_recycle": false,
       "page": "<div><span class='Pcurrent'>1</span><span class='Pnumber'>1/1</span><span class='Pline'>从1-3条</span><span class='Pcount'>共3条</span></div>",
       "path": "/www/wwwroot/test.example.com/it-capture",
       "dir": [{"nm": "sub-dir", "sz": 4096, "mt": 1791053547, "acc": "755", "user": "www", "lnk": "", "durl": "", "cmp": 0, "fav": "0", "rmk": "", "top": 0, "sn": "sub-dir"}],
       "files": [
         {"nm": "b.txt", "sz": 0, "mt": 1791053564, "acc": "755", "user": "www", "lnk": "", "durl": "", "cmp": 0, "fav": "0", "rmk": "", "top": 0, "sn": "b.txt"},
         {"nm": "a.txt", "sz": 5, "mt": 1791053558, "acc": "644", "user": "root", "lnk": "/www/real.txt", "durl": "", "cmp": 0, "fav": "1", "rmk": "note", "top": 1, "sn": "a.txt"}],
       "dir_history": [{"val": "/www/wwwroot/example", "time": 1731154412}],
       "search_history": [], "tamper_data": {"status": false, "msg": "插件不存在!"}, "bt_sync": []}
      """;

  /** 同一目录，showRow=1、p=2：目录与文件合并分页，第 2 页是第一个文件。 */
  private static final String SECOND_PAGE =
      """
      {"store": [], "file_recycle": false,
       "page": "<div><a class='Pstart' href='p=1'>首页</a><a class='Ppren' href='p=1'>上一页</a><a class='Pnum' href='p=1'>1</a><span class='Pcurrent'>2</span><a class='Pnum' href='p=3'>3</a><a class='Pnext' href='p=3'>下一页</a><a class='Pend' href='p=3'>尾页</a><span class='Pnumber'>2/3</span><span class='Pline'>从2-2条</span><span class='Pcount'>共3条</span></div>",
       "path": "/www/wwwroot/test.example.com/it-capture",
       "dir": [],
       "files": [{"nm": "b.txt", "sz": 0, "mt": 1791053564, "acc": "755", "user": "www", "lnk": "", "durl": "", "cmp": 0, "fav": "0", "rmk": "", "top": 0, "sn": "b.txt"}]}
      """;

  /** 请求不存在的路径时，面板返回 /www/wwwroot 的列表而不是错误。 */
  private static final String FALLBACK_TO_WWWROOT =
      """
      {"page": "<div><span class='Pcurrent'>1</span><span class='Pnumber'>1/1</span><span class='Pcount'>共1条</span></div>",
       "path": "/www/wwwroot",
       "dir": [{"nm": "test.example.com", "sz": 4096, "mt": 1791053537, "acc": "755", "user": "root", "lnk": "", "fav": "0", "rmk": "", "top": 0}],
       "files": []}
      """;

  @Test
  @DisplayName("默认参数与面板 UI 抓包一致")
  void defaultsMatchUiCapture() {
    GetDirectoryListingApi api = new GetDirectoryListingApi().setPath(DIR);

    assertEquals("files?action=GetDirNew", api.getEndpoint());
    assertEquals(BtApi.HttpMethod.POST, api.getMethod());
    assertEquals(
        Map.of("path", DIR, "p", "1", "showRow", "500", "sort", "", "reverse", "True"),
        api.getParams());
    assertTrue(invokeValidate(api));
  }

  @Test
  @DisplayName("setter 应写入对应的面板参数")
  void settersWriteParams() {
    GetDirectoryListingApi api =
        new GetDirectoryListingApi()
            .setPath("//www//wwwroot/site/")
            .setPage(3)
            .setRows(20)
            .setSort("mtime")
            .setReverse(false);

    Map<String, Object> params = api.getParams();
    assertEquals("/www/wwwroot/site", params.get("path"));
    assertEquals("3", params.get("p"));
    assertEquals("20", params.get("showRow"));
    assertEquals("mtime", params.get("sort"));
    assertEquals("False", params.get("reverse"));
    assertEquals("", new GetDirectoryListingApi().setSort(null).getParams().get("sort"));
  }

  @Test
  @DisplayName("应拒绝不安全的路径和非法分页参数")
  void rejectsInvalidInput() {
    GetDirectoryListingApi api = new GetDirectoryListingApi();

    assertThrows(IllegalArgumentException.class, () -> api.setPath("www/wwwroot"));
    assertThrows(IllegalArgumentException.class, () -> api.setPath("/www/wwwroot/../../etc"));
    assertThrows(IllegalArgumentException.class, () -> api.setPath(" "));
    assertThrows(IllegalArgumentException.class, () -> api.setPage(0));
    assertThrows(IllegalArgumentException.class, () -> api.setRows(0));
    assertFalse(invokeValidate(api));
  }

  @Test
  @DisplayName("应解析目录、文件和分页信息")
  void parsesCapturedResponse() {
    BtResult<DirectoryListing> result =
        new GetDirectoryListingApi().setPath(DIR).parseResponse(FULL_PAGE);

    assertTrue(result.isSuccess());
    DirectoryListing listing = result.getData();
    assertEquals(DIR, listing.getPath());
    assertEquals(1, listing.getPage());
    assertEquals(1, listing.getTotalPages());
    assertEquals(3, listing.getTotalCount());
    assertFalse(listing.hasNextPage());

    assertEquals(1, listing.getDirectories().size());
    FileEntry directory = listing.getDirectories().get(0);
    assertEquals("sub-dir", directory.getName());
    assertTrue(directory.isDirectory());
    assertEquals(4096L, directory.getSize());
    assertEquals("755", directory.getPermissions());
    assertEquals("www", directory.getOwner());
    assertFalse(directory.isSymlink());
    assertFalse(directory.isFavorite());
    assertFalse(directory.isPinned());

    FileEntry file = listing.getFiles().get(1);
    assertEquals("a.txt", file.getName());
    assertFalse(file.isDirectory());
    assertEquals(5L, file.getSize());
    assertEquals(Instant.ofEpochSecond(1791053558L), file.getModifiedAt());
    assertEquals("644", file.getPermissions());
    assertEquals("root", file.getOwner());
    assertTrue(file.isSymlink());
    assertEquals("/www/real.txt", file.getLinkTarget());
    assertEquals("note", file.getRemark());
    assertTrue(file.isFavorite());
    assertTrue(file.isPinned());

    assertEquals(
        List.of("sub-dir", "b.txt", "a.txt"),
        listing.getEntries().stream().map(FileEntry::getName).toList());
  }

  @Test
  @DisplayName("未建模的字段应可通过 raw 读取且只读")
  void keepsUnknownFields() {
    DirectoryListing listing =
        new GetDirectoryListingApi().setPath(DIR).parseResponse(FULL_PAGE).getData();

    assertEquals(false, listing.getRaw().get("file_recycle"));
    assertTrue(listing.getRaw().containsKey("dir_history"));
    assertTrue(listing.getPageHtml().contains("Pcount"));
    FileEntry entry = listing.getFiles().get(0);
    assertEquals("b.txt", entry.getRaw().get("sn"));
    assertEquals(0, entry.getRaw().get("cmp"));
    assertThrows(UnsupportedOperationException.class, () -> entry.getRaw().put("x", 1));
    assertThrows(UnsupportedOperationException.class, () -> listing.getFiles().clear());
  }

  @Test
  @DisplayName("应解析带翻页链接的分页信息")
  void parsesPagination() {
    DirectoryListing listing =
        new GetDirectoryListingApi()
            .setPath(DIR)
            .setPage(2)
            .setRows(1)
            .parseResponse(SECOND_PAGE)
            .getData();

    assertEquals(2, listing.getPage());
    assertEquals(3, listing.getTotalPages());
    assertEquals(3, listing.getTotalCount());
    assertTrue(listing.hasNextPage());
    assertTrue(listing.getDirectories().isEmpty());
    assertEquals("b.txt", listing.getFiles().get(0).getName());
  }

  @Test
  @DisplayName("分页片段无法解析时使用 -1")
  void unknownPaginationIsMinusOne() {
    DirectoryListing listing =
        new GetDirectoryListingApi()
            .setPath("/www")
            .parseResponse("{\"path\":\"/www\",\"dir\":[],\"files\":[]}")
            .getData();

    assertEquals(-1, listing.getPage());
    assertEquals(-1, listing.getTotalPages());
    assertEquals(-1, listing.getTotalCount());
    assertFalse(listing.hasNextPage());
    assertTrue(listing.getEntries().isEmpty());
  }

  @Test
  @DisplayName("面板列出的目录与请求不一致时应返回失败")
  void detectsPanelFallbackDirectory() {
    BtResult<DirectoryListing> result =
        new GetDirectoryListingApi().setPath(DIR + "/missing").parseResponse(FALLBACK_TO_WWWROOT);

    assertFalse(result.isSuccess());
    assertNull(result.getData());
    assertTrue(result.getMsg().contains(DIR + "/missing"));
    assertTrue(result.getMsg().contains("/www/wwwroot"));
  }

  @Test
  @DisplayName("响应路径带末尾斜杠时仍视为同一目录")
  void normalizesListedPath() {
    BtResult<DirectoryListing> result =
        new GetDirectoryListingApi()
            .setPath("/www/wwwroot")
            .parseResponse("{\"path\":\"/www/wwwroot/\",\"dir\":[],\"files\":[]}");

    assertTrue(result.isSuccess());
  }

  @Test
  @DisplayName("应透传面板的 status/msg 错误")
  void parsesStatusFailure() {
    BtResult<DirectoryListing> result =
        new GetDirectoryListingApi()
            .setPath(DIR)
            .parseResponse("{\"status\":false,\"msg\":\"denied\"}");

    assertFalse(result.isSuccess());
    assertEquals("denied", result.getMsg());
    assertNull(result.getData());
  }

  @Test
  @DisplayName("应拒绝空响应和非法响应")
  void rejectsInvalidResponses() {
    GetDirectoryListingApi api = new GetDirectoryListingApi().setPath(DIR);

    assertThrows(BtApiException.class, () -> api.parseResponse(" "));
    assertThrows(BtApiException.class, () -> api.parseResponse("<html>"));
    assertThrows(BtApiException.class, () -> api.parseResponse("[]"));
    assertThrows(BtApiException.class, () -> api.parseResponse("{\"path\":\"/www\"}"));
  }

  private boolean invokeValidate(GetDirectoryListingApi api) {
    try {
      Method method = api.getClass().getDeclaredMethod("validateParams");
      method.setAccessible(true);
      return (boolean) method.invoke(api);
    } catch (ReflectiveOperationException exception) {
      throw new IllegalStateException(exception);
    }
  }
}
