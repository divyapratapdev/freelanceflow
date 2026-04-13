package com.freelanceflow.payment.dto;

public class PaymentLinkResponse {

    private String linkId;
    private String linkUrl;

    public PaymentLinkResponse(String linkId, String linkUrl) {
        this.linkId = linkId;
        this.linkUrl = linkUrl;
    }

    public String getLinkId() { return linkId; }
    public void setLinkId(String linkId) { this.linkId = linkId; }

    public String getLinkUrl() { return linkUrl; }
    public void setLinkUrl(String linkUrl) { this.linkUrl = linkUrl; }
}
