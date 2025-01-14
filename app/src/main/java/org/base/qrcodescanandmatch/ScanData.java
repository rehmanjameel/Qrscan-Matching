package org.base.qrcodescanandmatch;

public class ScanData {
    private String timestamp;
    private String tagType;
    private String ctnr;
    private String partNr;
    private String dnr;
    private String qty;

    public ScanData(String timestamp, String tagType, String ctnr, String partNr, String dnr, String qty) {
        this.timestamp = timestamp;
        this.tagType = tagType;
        this.ctnr = ctnr;
        this.partNr = partNr;
        this.dnr = dnr;
        this.qty = qty;
    }

    // Getters
    public String getTimestamp() { return timestamp; }
    public String getTagType() { return tagType; }
    public String getCtnr() { return ctnr; }
    public String getPartNr() { return partNr; }
    public String getDnr() { return dnr; }
    public String getQty() { return qty; }
}

