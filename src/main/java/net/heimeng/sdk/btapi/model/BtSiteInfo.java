package net.heimeng.sdk.btapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Objects;

/**
 * 宝塔站点信息
 *
 * @author InwardFlow
 */
@Data
public class BtSiteInfo {
    private int id;
    private String name;
    private String path;
    private String ps;
    private String status;
    @JsonProperty("addtime")
    private String addtime;

    public BtSiteInfo(int id, String name, String path, String ps, String status, String addtime) {
        this.id = id;
        this.name = name;
        this.path = path;
        this.ps = ps;
        this.status = status;
        this.addtime = addtime;
    }

    public BtSiteInfo() {
    }

    private BtSiteInfo(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.path = builder.path;
        this.ps = builder.ps;
        this.status = builder.status;
        this.addtime = builder.addtime;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public String getPs() {
        return ps;
    }

    public String getStatus() {
        return status;
    }

    public String getAddtime() {
        return addtime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BtSiteInfo siteInfo = (BtSiteInfo) o;
        return id == siteInfo.id &&
                Objects.equals(name, siteInfo.name) &&
                Objects.equals(path, siteInfo.path) &&
                Objects.equals(ps, siteInfo.ps) &&
                Objects.equals(status, siteInfo.status) &&
                Objects.equals(addtime, siteInfo.addtime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, path, ps, status, addtime);
    }

    @Override
    public String toString() {
        return "BtSiteInfo{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", path='" + path + '\'' +
                ", ps='" + ps + '\'' +
                ", status='" + status + '\'' +
                ", addtime='" + addtime + '\'' +
                '}';
    }

    public static class Builder {
        private int id;
        private String name;
        private String path;
        private String ps;
        private String status;
        private String addtime;

        public Builder id(int id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder ps(String ps) {
            this.ps = ps;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder addtime(String addtime) {
            this.addtime = addtime;
            return this;
        }

        public BtSiteInfo build() {
            return new BtSiteInfo(this);
        }
    }
}
