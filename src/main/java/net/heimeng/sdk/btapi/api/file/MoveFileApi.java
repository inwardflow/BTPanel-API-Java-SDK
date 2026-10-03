package net.heimeng.sdk.btapi.api.file;

import java.util.Set;

/** 移动或复制文件的 API。 */
public class MoveFileApi extends AbstractFileBooleanApi {

  private static final String ENDPOINT = "files?action=MoveFile";
  private static final Set<String> ALLOWED_TYPES = Set.of("move", "copy");

  public MoveFileApi() {
    super(ENDPOINT, "操作成功", "操作失败");
  }

  public MoveFileApi setSource(String source) {
    addParam("source", source);
    return this;
  }

  public MoveFileApi setTarget(String target) {
    addParam("target", target);
    return this;
  }

  public MoveFileApi setType(String type) {
    addParam("type", type);
    return this;
  }

  public MoveFileApi setMoveType(Integer moveType) {
    addParam("moveType", moveType);
    return this;
  }

  @Override
  protected boolean validateParams() {
    return hasRequiredParams("source", "target") && hasAllowedStringValue("type", ALLOWED_TYPES);
  }
}
