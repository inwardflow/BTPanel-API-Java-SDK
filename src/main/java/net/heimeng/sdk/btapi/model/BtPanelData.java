package net.heimeng.sdk.btapi.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BtPanelData {
    private List<BtSiteInfo> sites;
    private List<BtSiteInfo> ftps; // Assuming ftps is a list of some type, use Object for now
    private List<BtSiteInfo> databases; // Assuming databases is a list of some type, use Object for now
    private List<BtSiteInfo> crontab; // Assuming crontab is a list of some type, use Object for now
    private List<BtSiteInfo> paths;
    private Boolean zip;

    private BtPanelData(Builder builder) {
        this.sites = builder.sites;
        this.ftps = builder.ftps;
        this.databases = builder.databases;
        this.crontab = builder.crontab;
        this.paths = builder.paths;
        this.zip = builder.zip;
    }

    @Override
    public String toString() {
        return "BtPanelData{" +
               "sites=" + sites +
               ", ftps=" + ftps +
               ", databases=" + databases +
               ", crontab=" + crontab +
               '}';
    }

    public static class Builder {
        private List<BtSiteInfo> sites;
        private List<BtSiteInfo> ftps;
        private List<BtSiteInfo> databases;
        private List<BtSiteInfo> crontab;
        private List<BtSiteInfo> paths;
        private Boolean zip;

        public Builder sites(List<BtSiteInfo> sites) {
            this.sites = sites;
            return this;
        }

        public Builder ftps(List<BtSiteInfo> ftps) {
            this.ftps = ftps;
            return this;
        }

        public Builder databases(List<BtSiteInfo> databases) {
            this.databases = databases;
            return this;
        }

        public Builder crontab(List<BtSiteInfo> crontab) {
            this.crontab = crontab;
            return this;
        }

        public Builder paths(List<BtSiteInfo> paths) {
            this.paths = paths;
            return this;
        }

        public Builder zip(Boolean zip) {
            this.zip = zip;
            return this;
        }

        public BtPanelData build() {
            return new BtPanelData(this);
        }
    }
}
