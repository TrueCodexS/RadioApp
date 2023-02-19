package com.radio.kutai.gdpr;

/**
 * @author:YPY Global
 * @Email: bl911vn@gmail.com
 * @Website: http://radio.com
 * Created by YPY Global on 2/25/19.
 */
class GDPRModel {

    private final String publisherId;
    private final String urlPolicy;
    private final String testId;

    GDPRModel(String publisherId, String urlPolicy, String testId) {
        this.publisherId = publisherId;
        this.urlPolicy = urlPolicy;
        this.testId = testId;
    }

    String getPublisherId() {
        return publisherId;
    }

    String getUrlPolicy() {
        return urlPolicy;
    }

    String getTestId() {
        return testId;
    }

}
