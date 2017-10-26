/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.connector.integration.test.facebook;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.connector.integration.test.base.ConnectorIntegrationTestBase;
import org.wso2.connector.integration.test.base.RestResponse;

public class FacebookConnectorIntegrationTest extends ConnectorIntegrationTestBase {

    private Map< String, String > esbRequestHeadersMap = new HashMap< String, String >();

    private Map< String, String > apiRequestHeadersMap = new HashMap< String, String >();

    private Map< String, String > headersMap = new HashMap< String, String >();

    private long timeOut;

    private String multipartProxyUrl;

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        String connectorName =
                System.getProperty("connector_name") + "-connector-" + System.getProperty("connector_version") + ".zip";
        init(connectorName);
        esbRequestHeadersMap.put("Accept-Charset", "UTF-8");
        esbRequestHeadersMap.put("Content-Type", "application/json");

        apiRequestHeadersMap.put("Accept-Charset", "UTF-8");
        apiRequestHeadersMap.put("Content-Type", "application/x-www-form-urlencoded");
        timeOut = Long.parseLong(connectorProperties.getProperty("timeOut"));

        String multipartPoxyName = connectorProperties.getProperty("multipartProxyName");
        multipartProxyUrl = getProxyServiceURLHttp(multipartPoxyName);
    }

    /**
     * Positive test case for getEventDetails method with mandatory parameters.
     *
     * @throws JSONException
     * @throws IOException
     */
    @Test(priority = 1,
          groups = { "wso2.esb" },
          description = "facebook {getEventDetails} integration test with mandatory parameters.")
    public void testGetEventDetailsWithMandatoryParameters() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:getEventDetails");
        String apiEndPoint =
                connectorProperties.getProperty("apiUrl") + "/" + connectorProperties.getProperty("apiVersion") + "/"
                        + connectorProperties.getProperty("eventId") + "?access_token=" + connectorProperties
                        .getProperty("accessToken");

        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getEventDetails_mandatory.txt");

        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);

        Assert.assertEquals(esbRestResponse.getBody().get("start_time"), apiRestResponse.getBody().get("start_time"));
        Assert.assertEquals(esbRestResponse.getBody().get("name"), apiRestResponse.getBody().get("name"));
        Assert.assertEquals(esbRestResponse.getBody().get("id"), apiRestResponse.getBody().get("id"));

    }

    /**
     * Positive test case for getEventDetails method with optional parameters.
     */
    @Test(priority = 1,
          groups = { "wso2.esb" },
          dependsOnMethods = { "testGetEventDetailsWithMandatoryParameters" },
          description = "facebook {getEventDetails} integration test with optional parameters.")
    public void testGetEventDetailsWithOptionalParameters() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:getEventDetails");
        String apiEndPoint =
                connectorProperties.getProperty("apiUrl") + "/" + connectorProperties.getProperty("apiVersion") + "/"
                        + connectorProperties.getProperty("eventId") + "/?access_token=" + connectorProperties
                        .getProperty("accessToken") + "&fields=owner";

        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getEventDetails_optional.txt");

        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);

        Assert.assertEquals(esbRestResponse.getBody().get("owner").toString(), apiRestResponse.getBody().get("owner")
                .toString());

    }

    /**
     * Negative test case for getEventDetails method.
     */
    @Test(priority = 1,
          groups = { "wso2.esb" },
          dependsOnMethods = { "testGetEventDetailsWithOptionalParameters" },
          description = "facebook {getEventDetails} integration test with negative case.")
    public void testGetEventDetailsWithNegativeCase() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:getEventDetails");
        String apiEndPoint =
                "https://graph.facebook.com/Negative/?access_token=" + connectorProperties.getProperty("accessToken");

        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getEventDetails_negative.txt");

        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);

        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), apiRestResponse.getHttpStatusCode());
    }

    /**
     * Positive test case for createAttendingRSVP method with mandatory parameters.
     *
     * @throws InterruptedException
     */
    @Test(priority = 1,
          groups = { "wso2.esb" },
          dependsOnMethods = { "testGetEventDetailsWithMandatoryParameters" },
          description = "facebook {createAttendingRSVP} integration test with mandatory parameters.")
    public void testCreateAttendingRSVPWithMandatoryParameters()
            throws IOException, JSONException, InterruptedException {

        esbRequestHeadersMap.put("Action", "urn:createAttendingRSVP");

        // This API call is to reset User attending status
        String apiResetEndPoint =
                connectorProperties.getProperty("apiUrl") + "/" + connectorProperties.getProperty("apiVersion") + "/"
                        + connectorProperties.getProperty("eventId") + "/declined";
        sendJsonRestRequest(apiResetEndPoint, "POST", apiRequestHeadersMap, "api_accessToken.txt");

        sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_createRSVP_mandatory.txt");

        Thread.sleep(timeOut);

        String apiEndPoint =
                connectorProperties.getProperty("apiUrl") + "/" + connectorProperties.getProperty("apiVersion") + "/"
                        + connectorProperties.getProperty("eventId") + "/attending?access_token=" + connectorProperties
                        .getProperty("accessToken");

        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);

        JSONArray jsonArray = (JSONArray) apiRestResponse.getBody().get("data");
        boolean success = false;
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject element = (JSONObject) jsonArray.get(i);

            if (element.get("id").toString().equals(connectorProperties.getProperty("userId"))) {
                success = true;
                break;
            }

        }

        Assert.assertTrue(success);

    }

    /**
     * Negative test case for createAttendingRSVP method .
     */
    @Test(priority = 1,
          groups = { "wso2.esb" },
          description = "facebook {createAttendingRSVP} integration test with negative case.")
    public void testCreateAttendingRSVPWithNegativeCase() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:createAttendingRSVP");
        String apiEndPoint = "https://graph.facebook.com/Negative/attending";
        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_createRSVP_negative.txt");

        RestResponse< JSONObject > apiRestResponse =
                sendJsonRestRequest(apiEndPoint, "POST", apiRequestHeadersMap, "api_accessToken.txt");

        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), apiRestResponse.getHttpStatusCode());

    }

    /**
     * Positive test case for createDeclinedRSVP method with mandatory parameters.
     *
     * @throws InterruptedException
     */
    @Test(priority = 1,
          groups = { "wso2.esb" },
          dependsOnMethods = { "testGetEventDetailsWithMandatoryParameters" },
          description = "facebook {createDeclinedRSVP} integration test with mandatory parameters.")
    public void testCreateDeclinedRSVPWithMandatoryParameters()
            throws IOException, JSONException, InterruptedException {

        esbRequestHeadersMap.put("Action", "urn:createDeclinedRSVP");

        // This API call is to reset User attending status
        String apiResetEndPoint =
                connectorProperties.getProperty("apiUrl") + "/" + connectorProperties.getProperty("apiVersion") + "/"
                        + connectorProperties.getProperty("eventId") + "/attending";
        sendJsonRestRequest(apiResetEndPoint, "POST", apiRequestHeadersMap, "api_accessToken.txt");

        sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_createRSVP_mandatory.txt");

        Thread.sleep(timeOut);

        String apiEndPoint =
                connectorProperties.getProperty("apiUrl") + connectorProperties.getProperty("eventId")
                        + "/declined?access_token=" + connectorProperties.getProperty("accessToken");
        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);

        JSONArray jsonArray = (JSONArray) apiRestResponse.getBody().get("data");
        boolean success = false;
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject element = (JSONObject) jsonArray.get(i);

            if (element.get("id").toString().equals(connectorProperties.getProperty("userId"))) {
                success = true;
                break;
            }
        }

        Assert.assertTrue(success);

    }

    /**
     * Negative test case for createDeclinedRSVP method .
     */
    @Test(priority = 1,
          groups = { "wso2.esb" },
          description = "facebook {createDeclinedRSVP} integration test with negative case.")
    public void testCreateDeclinedRSVPWithNegativeCase() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:createDeclinedRSVP");
        String apiEndPoint = "https://graph.facebook.com/Negative/declined";
        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_createRSVP_negative.txt");

        RestResponse< JSONObject > apiRestResponse =
                sendJsonRestRequest(apiEndPoint, "POST", apiRequestHeadersMap, "api_accessToken.txt");

        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), apiRestResponse.getHttpStatusCode());

    }

    /**
     * Positive test case for createMaybeRSVP method with mandatory parameters.
     *
     * @throws InterruptedException
     */
    @Test(priority = 1,
          groups = { "wso2.esb" },
          dependsOnMethods = { "testGetEventDetailsWithMandatoryParameters" },
          description = "facebook {createMaybeRSVP} integration test with mandatory parameters.")
    public void testCreateMaybeRSVPWithMandatoryParameters() throws IOException, JSONException, InterruptedException {

        esbRequestHeadersMap.put("Action", "urn:createMaybeRSVP");

        // This API call is to reset User attending status
        String apiResetEndPoint =
                connectorProperties.getProperty("apiUrl") + "/" + connectorProperties.getProperty("apiVersion") + "/"
                        + connectorProperties.getProperty("eventId") + "/attending";
        sendJsonRestRequest(apiResetEndPoint, "POST", apiRequestHeadersMap, "api_accessToken.txt");

        sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_createRSVP_mandatory.txt");

        Thread.sleep(timeOut);

        String apiEndPoint =
                connectorProperties.getProperty("apiUrl") + connectorProperties.getProperty("eventId")
                        + "/maybe?access_token=" + connectorProperties.getProperty("accessToken");
        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);

        JSONArray jsonArray = (JSONArray) apiRestResponse.getBody().get("data");
        boolean success = false;
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject element = (JSONObject) jsonArray.get(i);

            if (element.get("id").toString().equals(connectorProperties.getProperty("userId"))) {
                success = true;
                break;
            }

        }

        Assert.assertTrue(success);

    }

    /**
     * Negative test case for createMaybeRSVP method .
     */
    @Test(priority = 1,
          groups = { "wso2.esb" },
          description = "facebook {createMaybeRSVP} integration test with negative case.")
    public void testcreateMaybeRSVPWithNegativeCase() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:createMaybeRSVP");
        String apiEndPoint = "https://graph.facebook.com/Negative/maybe";
        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_createRSVP_negative.txt");

        RestResponse< JSONObject > apiRestResponse =
                sendJsonRestRequest(apiEndPoint, "POST", apiRequestHeadersMap, "api_accessToken.txt");

        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), apiRestResponse.getHttpStatusCode());

    }

    /**
     * Positive test case for createPostOnEventWall method with mandatory parameters. Some times direct call
     * giving an unexpected error.
     */
    @Test(priority = 1,
          groups = { "wso2.esb" },
          dependsOnMethods = { "testGetEventDetailsWithMandatoryParameters" },
          description = "facebook {createPostOnEventWall} integration test with mandatory parameters.")
    public void testCreatePostOnEventWallWithMandatoryParameters() throws IOException, JSONException {

        try {
            esbRequestHeadersMap.put("Action", "urn:createPostOnEventWall");

            RestResponse< JSONObject > esbRestResponse =
                    sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap,
                            "esb_createPostOnEventWall_mandatory.txt");

            esbRestResponse.getBody().get("id");

            Thread.sleep(timeOut);

            String apiEndPoint =
                    connectorProperties.getProperty("apiUrl") + "/" + connectorProperties.getProperty("apiVersion")
                            + "/" + connectorProperties.getProperty("eventId") + "/feed";

            RestResponse< JSONObject > apiRestResponse =
                    sendJsonRestRequest(apiEndPoint, "POST", apiRequestHeadersMap,
                            "api_createPostOnEventWall_mandatory.txt");

            Assert.assertTrue(apiRestResponse.getBody().has("id") && esbRestResponse.getBody().has("id"));
            Assert.assertTrue(apiRestResponse.getBody().getString("id").contains("_")
                    && esbRestResponse.getBody().getString("id").contains("_"));

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Positive test case for createPostOnEventWall method with optional parameters. Some times direct call
     * giving an unexpected error.
     *
     * @throws InterruptedException
     */
    @Test(priority = 1,
          groups = { "wso2.esb" },
          dependsOnMethods = { "testCreatePostOnEventWallWithMandatoryParameters" },
          description = "facebook {createPostOnEventWall} integration test with optional parameters.")
    public void testCreatePostOnEventWallWithOptionalParameters()
            throws IOException, JSONException, InterruptedException {

        esbRequestHeadersMap.put("Action", "urn:createPostOnEventWall");

        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_createPostOnEventWall_optional.txt");

        esbRestResponse.getBody().get("id");

        Thread.sleep(timeOut);
        String apiEndPoint =
                connectorProperties.getProperty("apiUrl") + "/" + connectorProperties.getProperty("apiVersion") + "/"
                        + connectorProperties.getProperty("eventId") + "/feed";

        RestResponse< JSONObject > apiRestResponse =
                sendJsonRestRequest(apiEndPoint, "POST", apiRequestHeadersMap, "api_createPostOnEventWall_optional.txt");

        Assert.assertTrue(apiRestResponse.getBody().has("id") && esbRestResponse.getBody().has("id"));
        Assert.assertTrue(apiRestResponse.getBody().getString("id").contains("_")
                && esbRestResponse.getBody().getString("id").contains("_"));

    }

    /**
     * Negative test case for createPostOnEventWall method.
     */
    @Test(priority = 1,
          groups = { "wso2.esb" },
          dependsOnMethods = { "testGetEventDetailsWithMandatoryParameters" },
          description = "facebook {createPostOnEventWall} integration test with negative.")
    public void testCreatePostOnEventWallWithNegativeCase() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:createPostOnEventWall");

        String apiEndPoint =
                connectorProperties.getProperty("apiUrl") + connectorProperties.getProperty("eventId") + "/feed";
        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_createPostOnEventWall_negative.txt");

        RestResponse< JSONObject > apiRestResponse =
                sendJsonRestRequest(apiEndPoint, "POST", apiRequestHeadersMap, "api_createPostOnEventWall_negative.txt");

        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), apiRestResponse.getHttpStatusCode());

    }

    /**
     * Positive test case for uploadVideo
     *
     * @throws InterruptedException
     */
    @Test(priority = 1,
          groups = { "wso2.esb" },
          description = "facebook {uploadVideo} integration test positive case.")
    public void testUploadVideoMandatoryParameters() throws IOException, JSONException, InterruptedException {

        esbRequestHeadersMap.put("Action", "urn:uploadVideo");

        headersMap.put("Action", "urn:uploadVideo");

        MultipartFormdataProcessor multipartProcessor = new MultipartFormdataProcessor(
                multipartProxyUrl + "?apiUrl=" + connectorProperties.getProperty("apiUrl") + "&apiVersion="
                        + connectorProperties.getProperty("apiVersion") + "&resourceId=" + connectorProperties
                        .getProperty("userId") + "&accessToken=" + connectorProperties.getProperty("accessToken"),
                headersMap);

        multipartProcessor.addFormDataToRequest("message", "via new ESb");
        //multipartProcessor.addFormDataToRequest("access_token", connectorProperties.getProperty("accessToken"));
        multipartProcessor.addFileToRequest("source", connectorProperties.getProperty("videoName"));

        RestResponse< JSONObject > esbRestResponse = multipartProcessor.processForJsonResponse();
        String videoId = esbRestResponse.getBody().getString("id");
        connectorProperties.put("videoId", videoId);
        Thread.sleep(Long.parseLong(connectorProperties.getProperty("videoUploadTimeOut")));
        Assert.assertTrue(esbRestResponse.getBody().has("id"));

    }

    /**
     * Positive test case for getVideoDetails method with mandatory parameters.
     */
    @Test(priority = 1,
          groups = { "wso2.esb" },
          dependsOnMethods = { "testUploadVideoMandatoryParameters" },
          description = "facebook {getVideoDetails} integration test with mandatory parameters.")
    public void testGetVideoDetailsWithMandatoryParameters() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:getVideoDetails");

        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getVideoDetails_mandatory.txt");

        String apiEndPoint =
                connectorProperties.getProperty("apiUrl") + "/" + connectorProperties.getProperty("apiVersion") + "/"
                        + connectorProperties.getProperty("videoId") + "?access_token=" + connectorProperties
                        .getProperty("accessToken");

        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);

        Assert.assertEquals(esbRestResponse.getBody().get("id"), apiRestResponse.getBody().get("id"));

        Assert.assertEquals(esbRestResponse.getBody().get("updated_time"), apiRestResponse.getBody().get("updated_time"));

    }

    /**
     * Positive test case for getVideoDetails method with optional parameters.
     */
    @Test(priority = 1,
          groups = { "wso2.esb" },
          dependsOnMethods = { "testUploadVideoMandatoryParameters" },
          description = "facebook {getVideoDetails} integration test with optional parameters.")
    public void testGetVideoDetailsWithOptionalParameters() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:getVideoDetails");

        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getVideoDetails_optional.txt");
        String apiEndPoint =
                connectorProperties.getProperty("apiUrl") + "/" + connectorProperties.getProperty("apiVersion") + "/"
                        + connectorProperties.getProperty("videoId") + "?access_token=" + connectorProperties
                        .getProperty("accessToken") + "&fields=from,picture";

        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);

        Assert.assertEquals(esbRestResponse.getBody().get("picture"), apiRestResponse.getBody().get("picture"));
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("from").get("name"), apiRestResponse.getBody()
                .getJSONObject("from").get("name"));
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("from").get("id"), apiRestResponse.getBody()
                .getJSONObject("from").get("id"));

    }

    /**
     * Negative test case for getVideoDetails method.
     */
    @Test(priority = 1,
          groups = { "wso2.esb" },
          description = "facebook {getVideoDetails} integration test with negative.")
    public void testGetVideoDetailsWithNegativeCase() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:getVideoDetails");

        String apiEndPoint =
                "https://graph.facebook.com/Negative?access_token=" + connectorProperties.getProperty("accessToken")
                        + "&fields=from,picture";
        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getVideoDetails_negative.txt");

        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);

        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), apiRestResponse.getHttpStatusCode());

    }

    /**
     * Positive test case for createComment method with mandatory parameters.
     *
     * @throws InterruptedException
     */
    @Test(priority = 1,
          groups = { "wso2.esb" },
          dependsOnMethods = { "testPublishPagePostWithMandatoryParameters" },
          description = "facebook {createComment} integration test with mandatory parameters.")
    public void testCreateCommentWithMandatoryParameters() throws IOException, JSONException, InterruptedException {

        esbRequestHeadersMap.put("Action", "urn:createComment");

        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_createComment_mandatory.txt");

        String commentId = esbRestResponse.getBody().get("id").toString();

        Thread.sleep(timeOut);
        connectorProperties.put("commentId", commentId);
        String apiEndPoint =
                connectorProperties.getProperty("apiUrl") + commentId + "?access_token="
                        + connectorProperties.getProperty("accessToken");
        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);

        Assert.assertEquals(connectorProperties.getProperty("commentId"), apiRestResponse.getBody().get("id"));
        Assert.assertEquals(connectorProperties.getProperty("commentMessage"),
                apiRestResponse.getBody().get("message"));

    }

    /**
     * Negative test case for createComment method .
     *
     * @throws InterruptedException
     */
    @Test(priority = 1,
          groups = { "wso2.esb" },
          dependsOnMethods = { "testUploadVideoMandatoryParameters" },
          description = "facebook {createComment} integration test with negative case.")
    public void testCreateCommentWithNegativeCase() throws IOException, JSONException, InterruptedException {

        esbRequestHeadersMap.put("Action", "urn:createComment");

        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_createComment_negative.txt");

        Thread.sleep(timeOut);

        String apiEndPoint =
                connectorProperties.getProperty("apiUrl") + connectorProperties.getProperty("videoId") + "/comments";
        RestResponse< JSONObject > apiRestResponse =
                sendJsonRestRequest(apiEndPoint, "POST", apiRequestHeadersMap, "api_accessToken.txt");

        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), apiRestResponse.getHttpStatusCode());

    }

    /**
     * Positive test case for updateComment method with mandatory parameters.
     *
     * @throws InterruptedException
     */
    @Test(priority = 1,
          groups = { "wso2.esb" },
          dependsOnMethods = { "testCreateCommentWithMandatoryParameters" },
          description = "facebook {updateComment} integration test with mandatory parameters.")
    public void testUpdateCommentWithMandatoryParameters() throws IOException, JSONException, InterruptedException {

        esbRequestHeadersMap.put("Action", "urn:updateComment");

        String apiEndPoint =
                connectorProperties.getProperty("apiUrl") + connectorProperties.getProperty("commentId")
                        + "?access_token=" + connectorProperties.getProperty("accessToken");
        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);

        String originalComment = "";
        if (apiRestResponse.getBody().has("message")) {
            originalComment = apiRestResponse.getBody().getString("message");
        }

        sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_updateComment_mandatory.txt");

        Thread.sleep(timeOut);

        apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);

        String updatedComment = "";
        if (apiRestResponse.getBody().has("message")) {
            updatedComment = apiRestResponse.getBody().getString("message");
        }

        Assert.assertNotEquals(originalComment, updatedComment);
        Assert.assertEquals(updatedComment, "Updated Comment");
    }

    /**
     * Negative test case for updateComment method.
     */
    @Test(priority = 1,
          groups = { "wso2.esb" },
          dependsOnMethods = { "testUpdateCommentWithMandatoryParameters" },
          description = "facebook {updateComment} integration test with negative.")
    public void testUpdateCommentWithNegativeCase() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:updateComment");

        String apiEndPoint = "https://graph.facebook.com/Negative";
        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_updateComment_negative.txt");

        RestResponse< JSONObject > apiRestResponse =
                sendJsonRestRequest(apiEndPoint, "POST", apiRequestHeadersMap, "api_updateComment_negative.txt");

        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), apiRestResponse.getHttpStatusCode());

    }

    /**
     * Positive test case for createLike method with mandatory parameters.
     *
     * @throws InterruptedException
     */
    @Test(priority = 1,
          groups = { "wso2.esb" },
          dependsOnMethods = { "testCreateCommentWithMandatoryParameters" },
          description = "facebook {createLike} integration test with mandatory parameters.")
    public void testCreateLikeWithMandatoryParameters() throws IOException, JSONException, InterruptedException {

        esbRequestHeadersMap.put("Action", "urn:createLike");

        String apiEndPoint =
                connectorProperties.getProperty("apiUrl") + connectorProperties.getProperty("commentId")
                        + "/likes?access_token=" + connectorProperties.getProperty("accessToken");
        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "DELETE", apiRequestHeadersMap);

        sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_createLike_mandatory.txt");

        Thread.sleep(timeOut);

        apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);

        Assert.assertTrue(apiRestResponse.getBody().getJSONArray("data").length() != 0);

    }

    /**
     * Negative test case for CreateLike method.
     */
    @Test(priority = 1,
          groups = { "wso2.esb" },
          dependsOnMethods = { "testCreateLikeWithMandatoryParameters" },
          description = "facebook {CreateLike} integration test with negative.")
    public void testCreateLikeWithNegativeCase() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:createLike");

        String apiEndPoint =
                connectorProperties.getProperty("apiUrl") + "invalid" + "/likes";
        RestResponse< JSONObject > apiRestResponse =
                sendJsonRestRequest(apiEndPoint, "POST", apiRequestHeadersMap, "api_accessToken.txt");

        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_createLike_negative.txt");

        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), apiRestResponse.getHttpStatusCode());

    }

    /**
     * Positive test case for getCommentDetails method with mandatory parameters.
     */
    @Test(priority = 1,
          groups = { "wso2.esb" },
          dependsOnMethods = { "testCreateLikeWithMandatoryParameters" },
          description = "facebook {getCommentDetails} integration test with mandatory parameters.")
    public void testGetCommentDetailsWithMandatoryParameters() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:getCommentDetails");

        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getCommentDetails_mandatory.txt");

        String apiEndPoint =
                connectorProperties.getProperty("apiUrl") + connectorProperties.getProperty("commentId")
                        + "?access_token=" + connectorProperties.getProperty("accessToken");
        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);

        Assert.assertEquals(esbRestResponse.getBody().get("id"), apiRestResponse.getBody().get("id"));
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("from").get("name"), apiRestResponse.getBody()
                .getJSONObject("from").get("name"));
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("from").get("id"), apiRestResponse.getBody()
                .getJSONObject("from").get("id"));

    }

    /**
     * Positive test case for getCommentDetails method with optional parameters.
     */
    @Test(priority = 1,
          groups = { "wso2.esb" },
          dependsOnMethods = { "testGetCommentDetailsWithMandatoryParameters" },
          description = "facebook {getCommentDetails} integration test with optional parameters.")
    public void testGetCommentDetailsWithOptionalParameters() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:getCommentDetails");

        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getCommentDetails_optional.txt");

        String apiEndPoint =
                connectorProperties.getProperty("apiUrl") + connectorProperties.getProperty("commentId")
                        + "/likes?access_token=" + connectorProperties.getProperty("accessToken");
        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);

        Assert.assertEquals(esbRestResponse.getBody().getJSONArray("data").length(), apiRestResponse.getBody()
                .getJSONArray("data").length());

    }

    /**
     * Negative test case for getCommentDetails method.
     */
    @Test(priority = 1,
          groups = { "wso2.esb" },
          dependsOnMethods = { "testGetCommentDetailsWithOptionalParameters" },
          description = "facebook {getCommentDetails} integration test with negative.")
    public void testGetCommentDetailsWithNegativeCase() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:getCommentDetails");

        String apiEndPoint = "https://graph.facebook.com/Negative/likes";
        RestResponse< JSONObject > apiRestResponse =
                sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap, "api_accessToken.txt");

        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getCommentDetails_negative.txt");

        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), apiRestResponse.getHttpStatusCode());

    }

    /**
     * Positive test case for deleteLike method with mandatory parameters.
     *
     * @throws InterruptedException
     */
    @Test(priority = 1,
          groups = { "wso2.esb" },
          dependsOnMethods = { "testGetCommentDetailsWithOptionalParameters" },
          description = "facebook {deleteLike} integration test with mandatory parameters.")
    public void testDeleteLikeWithMandatoryParameters() throws IOException, JSONException, InterruptedException {

        esbRequestHeadersMap.put("Action", "urn:deleteLike");

        sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_createLike_mandatory.txt");

        Thread.sleep(timeOut);
        String apiEndPoint =
                connectorProperties.getProperty("apiUrl") + connectorProperties.getProperty("commentId")
                        + "/likes?access_token=" + connectorProperties.getProperty("accessToken");

        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);

        Assert.assertTrue(apiRestResponse.getBody().getJSONArray("data").length() == 0);

    }

    /**
     * Negative test case for deleteLike method.
     */
    @Test(priority = 1,
          groups = { "wso2.esb" },
          dependsOnMethods = { "testDeleteLikeWithMandatoryParameters" },
          description = "facebook {deleteLike} integration test with negative.")
    public void testDeleteLikeWithNegativeCase() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:deleteLike");

        String apiEndPoint =
                connectorProperties.getProperty("apiUrl") + "invalid" + "/likes";
        RestResponse< JSONObject > apiRestResponse =
                sendJsonRestRequest(apiEndPoint, "DELETE", apiRequestHeadersMap, "api_accessToken.txt");

        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_createLike_negative.txt");

        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), apiRestResponse.getHttpStatusCode());

    }

    /**
     * Positive test case for deleteComment method with mandatory parameters.
     *
     * @throws InterruptedException
     */
    @Test(priority = 1,
          groups = { "wso2.esb" },
          dependsOnMethods = {
                  "testDeleteLikeWithNegativeCase", "testUpdateCommentWithMandatoryParameters"
          },
          description = "facebook {deleteComment} integration test with mandatory parameters.")
    public void testDeleteCommentWithMandatoryParameters() throws IOException, JSONException, InterruptedException {

        esbRequestHeadersMap.put("Action", "urn:deleteComment");

        sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_deleteComment_mandatory.txt");

        Thread.sleep(timeOut);

        String apiEndPoint =
                connectorProperties.getProperty("apiUrl") + connectorProperties.getProperty("commentId")
                        + "?access_token=" + connectorProperties.getProperty("accessToken");

        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);

        Assert.assertTrue(apiRestResponse.getBody().has("error"));

    }

    /**
     * Negative test case for deleteComment method .
     */
    @Test(priority = 1,
          groups = { "wso2.esb" },
          dependsOnMethods = { "testDeleteCommentWithMandatoryParameters" },
          description = "facebook {deleteComment} integration test with negative case.")
    public void testDeleteCommentWithNegativeCase() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:deleteComment");

        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_deleteComment_mandatory.txt");

        String apiEndPoint =
                connectorProperties.getProperty("apiUrl") + connectorProperties.getProperty("commentId")
                        + "?access_token=" + connectorProperties.getProperty("accessToken");

        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "DELETE", apiRequestHeadersMap);

        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), apiRestResponse.getHttpStatusCode());
    }

    /**
     * Positive test case for getAppAccessToken method with mandatory parameters.
     */
    @Test(priority = 1,
          groups = { "wso2.esb" },
          description = "facebook {getAppAccessToken} integration test with mandatory parameters.")
    public void testgetAppAccessTokenWithMandatoryParameters() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:getAppAccessToken");

        RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap,
                "esb_getAppAccessToken_mandatory.txt");
        String appAccessToken = esbRestResponse.getBody().get("access_token").toString();

        connectorProperties.put("appAccessToken", appAccessToken);
        String apiEndPoint =
                connectorProperties.getProperty("apiUrl") + connectorProperties.getProperty("clientId")
                        + "?access_token=" + connectorProperties.getProperty("appAccessToken");

        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);

        Assert.assertTrue(apiRestResponse.getBody().has("id"));

    }

    /**
     * Negative test case for getAppAccessToken method .
     */
    @Test(priority = 1,
          groups = { "wso2.esb" },
          description = "facebook {getAppAccessToken} integration test with negative case.")
    public void testgetAppAccessTokenWithNegativeCase() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:getAppAccessToken");

        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getAppAccessToken_negative.txt");

        String apiEndPoint =
                "https://graph.facebook.com/oauth/access_token?client_id="
                        + connectorProperties.getProperty("clientId")
                        + "&client_secret=Negative&grant_type=client_credentials";

        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);

        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("error").get("message"),
                apiRestResponse.getBody().getJSONObject("error").get("message"));

    }

    /**
     * Positive test case for isFriend method with mandatory parameters.
     */
    @Test(priority = 1,
          groups = { "wso2.esb" },
          description = "facebook {isFriend} integration test with mandatory parameters.")
    public void testIsFriendWithMandatoryParameters() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:isFriend");

        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_isFriend_mandatory.txt");

        String apiEndPoint =
                connectorProperties.getProperty("apiUrl") + connectorProperties.getProperty("sourceUserId")
                        + "/friends/" + connectorProperties.getProperty("targetUserId") + "?access_token="
                        + connectorProperties.getProperty("accessToken");

        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);

        Assert.assertEquals(esbRestResponse.getBody().getJSONArray("data").length(), apiRestResponse.getBody()
                .getJSONArray("data").length());

    }

    /**
     * Positive test case for isFriend method with mandatory parameters.
     */
    @Test(priority = 1,
          groups = { "wso2.esb" },
          description = "facebook {isFriend} integration test with optional parameters.")
    public void testIsFriendWithOptionalParameters() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:isFriend");

        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_isFriend_optional.txt");

        String apiEndPoint =
                connectorProperties.getProperty("apiUrl") + connectorProperties.getProperty("sourceUserId")
                        + "/friends/" + connectorProperties.getProperty("targetUserId") + "?access_token="
                        + connectorProperties.getProperty("accessToken") + "&fields=name,gender,link";

        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);

        JSONArray esbResponseArray = esbRestResponse.getBody().getJSONArray("data");
        JSONArray apiResponseArray = apiRestResponse.getBody().getJSONArray("data");

        if (esbResponseArray.length() != 0 && apiResponseArray.length() != 0) {
            Assert.assertEquals(((JSONObject) esbResponseArray.get(0)).get("name"),
                    ((JSONObject) apiResponseArray.get(0)).get("name"));
            Assert.assertEquals(((JSONObject) esbResponseArray.get(0)).get("gender"),
                    ((JSONObject) apiResponseArray.get(0)).get("gender"));
            Assert.assertEquals(((JSONObject) esbResponseArray.get(0)).get("link"),
                    ((JSONObject) apiResponseArray.get(0)).get("link"));
        } else {
            Assert.assertEquals(esbResponseArray.length(), apiResponseArray.length());
        }

    }

    /**
     * Negative test case for isFriend method .
     */
    @Test(priority = 1,
          groups = { "wso2.esb" },
          description = "facebook {isFriend} integration test with negative case.")
    public void testIsFriendWithNegativeCase() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:isFriend");

        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_isFriend_negative.txt");

        String apiEndPoint =
                connectorProperties.getProperty("apiUrl") + connectorProperties.getProperty("sourceUserId")
                        + "/friends/Negative?access_token=" + connectorProperties.getProperty("accessToken");

        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);

        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), apiRestResponse.getHttpStatusCode());

    }

    /**
     * Positive test case for getMutualFriends method with mandatory parameters.
     */
    @Test(priority = 1,
          groups = { "wso2.esb" },
          description = "facebook {getMutualFriends} integration test with mandatory parameters.")
    public void testGetMutualFriendsWithMandatoryParameters() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:getMutualFriends");

        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getMutualFriends_mandatory.txt");

        String apiEndPoint =
                connectorProperties.getProperty("apiUrl") + connectorProperties.getProperty("userId")
                        + "?fields=context.fields(mutual_friends)&access_token="
                        + connectorProperties.getProperty("accessToken");

        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);

        Assert.assertEquals(
                esbRestResponse.getBody().getJSONObject("context").getJSONObject("mutual_friends").getJSONArray("data")
                        .length(),
                apiRestResponse.getBody().getJSONObject("context").getJSONObject("mutual_friends").getJSONArray("data")
                        .length());

    }

    /**
     * Negative test case for getMutualFriends method .
     */
    @Test(priority = 1,
          groups = { "wso2.esb" },
          description = "facebook {getMutualFriends} integration test with negative case.")
    public void testGetMutualFriendsWithNegativeCase() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:getMutualFriends");

        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getMutualFriends_negative.txt");

        String apiEndPoint = connectorProperties.getProperty("apiUrl") + connectorProperties.getProperty("apiVersion")
                + "/Negative?fields=context.fields(mutual_friends)&access_token=" + connectorProperties
                .getProperty("accessToken");

        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);

        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), apiRestResponse.getHttpStatusCode());

    }

    /**
     * Positive test case for publishNotification method with mandatory parameters.
     */
    @Test(priority = 1,
          groups = { "wso2.esb" },
          dependsOnMethods = { "testgetAppAccessTokenWithMandatoryParameters" },
          description = "facebook {publishNotification} integration test with mandatory parameters.")
    public void testPublishNotificationWithMandatoryParameters() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:publishNotification");

        RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap,
                "esb_publishNotification_mandatory.txt");

        Assert.assertTrue((Boolean) esbRestResponse.getBody().get("success"));

    }

    /**
     * Positive test case for publishNotification method with optional parameters.
     *
     * @throws InterruptedException
     */
    @Test(priority = 1,
          groups = { "wso2.esb" },
          dependsOnMethods = { "testPublishNotificationWithMandatoryParameters" },
          description = "facebook {publishNotification} integration test with optional parameters.")
    public void testPublishNotificationWithOptionalParameters()
            throws IOException, JSONException, InterruptedException {

        esbRequestHeadersMap.put("Action", "urn:publishNotification");
        Thread.sleep(timeOut);
        RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap,
                "esb_publishNotification_optional.txt");
        Assert.assertTrue((Boolean) esbRestResponse.getBody().get("success"));

    }

    /**
     * Negative test case for publishNotification method .
     */
    @Test(priority = 1,
          groups = { "wso2.esb" },
          dependsOnMethods = { "testPublishNotificationWithOptionalParameters" },
          description = "facebook {publishNotification} integration test with negative case.")
    public void testPublishNotificationWithNegativeCase() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:publishNotification");

        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_publishNotification_negative.txt");

        String apiEndPoint =
                connectorProperties.getProperty("apiUrl") + connectorProperties.getProperty("userAId")
                        + "/notifications";

        RestResponse< JSONObject > apiRestResponse =
                sendJsonRestRequest(apiEndPoint, "POST", apiRequestHeadersMap, "api_appAccessToken.txt");

        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), apiRestResponse.getHttpStatusCode());

    }

    /**
     * Positive test case for createAppAchievements method with mandatory parameters.
     */
    @Test(priority = 1,
          groups = { "wso2.esb" },
          dependsOnMethods = { "testgetAppAccessTokenWithMandatoryParameters" },
          description = "facebook {createAppAchievements} integration test with mandatory parameters.")
    public void testCreateAppAchievementsWithMandatoryParameters() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:createAppAchievements");
        String apiEndPoint =
                connectorProperties.getProperty("apiUrl") + connectorProperties.getProperty("clientId")
                        + "/achievements?access_token=" + connectorProperties.getProperty("appAccessToken")
                        + "&achievement=" + connectorProperties.getProperty("achievementURL");

        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "DELETE", apiRequestHeadersMap);

        RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap,
                "esb_createAppAchievements_mandatory.txt");
        Assert.assertTrue((Boolean) esbRestResponse.getBody().get("success"));

        apiEndPoint =
                connectorProperties.getProperty("apiUrl") + connectorProperties.getProperty("clientId")
                        + "/achievements?access_token=" + connectorProperties.getProperty("appAccessToken");

        apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);

        JSONArray jsonArray = apiRestResponse.getBody().getJSONArray("data");
        JSONObject element = (JSONObject) jsonArray.get(0);
        Assert.assertEquals(element.get("type"), "game.achievement");
    }

    /**
     * Positive test case for createAppAchievements method with optional parameters.
     */
    @Test(priority = 1,
          groups = { "wso2.esb" },
          dependsOnMethods = { "testCreateAppAchievementsWithMandatoryParameters" },
          description = "facebook {createAppAchievements} integration test with optional parameters.")
    public void testCreateAppAchievementsWithOptionalParameters() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:createAppAchievements");

        RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap,
                "esb_createAppAchievements_optional.txt");
        Assert.assertTrue((Boolean) esbRestResponse.getBody().get("success"));
    }

    /**
     * Negative test case for createAppAchievements method .
     */
    @Test(priority = 1,
          groups = { "wso2.esb" },
          dependsOnMethods = { "testCreateAppAchievementsWithMandatoryParameters" },
          description = "facebook {createAppAchievements} integration test with negative case.")
    public void testCreateAppAchievementsWithNegativeCase() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:createAppAchievements");

        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_createAppAchievements_negative.txt");

        String apiEndPoint =
                connectorProperties.getProperty("apiUrl") + connectorProperties.getProperty("clientId")
                        + "/achievements";

        RestResponse< JSONObject > apiRestResponse =
                sendJsonRestRequest(apiEndPoint, "POST", apiRequestHeadersMap, "api_createAppAchievements_negative.txt");

        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), apiRestResponse.getHttpStatusCode());

    }

    /**
     * Positive test case for deleteAppAchievements method with mandatory parameters.
     */
    @Test(priority = 3,
          groups = { "wso2.esb" },
          dependsOnMethods = "testCreateAppAchievementsWithOptionalParameters",
          description = "facebook {deleteAppAchievements} integration test with mandatory parameters.")
    public void testDeleteAppAchievementsWithMandatoryParameters() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:deleteAppAchievements");

        sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_deleteAppAchievements_mandatory.txt");

        String apiEndPoint =
                connectorProperties.getProperty("apiUrl") + connectorProperties.getProperty("clientId")
                        + "/achievements?access_token=" + connectorProperties.getProperty("appAccessToken")
                        + "&achievement=" + connectorProperties.getProperty("achievementURL");

        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "DELETE", apiRequestHeadersMap);

        Assert.assertTrue(apiRestResponse.getBody().has("error"));

    }

    /**
     * Negative test case for deleteAppAchievements method .
     */
    @Test(priority = 3,
          groups = { "wso2.esb" },
          description = "facebook {deleteAppAchievements} integration test with negative case.")
    public void testDeleteAppAchievementsWithNegativeCase() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:deleteAppAchievements");

        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_deleteAppAchievements_negative.txt");

        String apiEndPoint =
                connectorProperties.getProperty("apiUrl") + connectorProperties.getProperty("clientId")
                        + "/achievements?access_token=" + connectorProperties.getProperty("appAccessToken")
                        + "&achievement=www.invalidAchievement.com/invalid";

        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "DELETE", apiRequestHeadersMap);

        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), apiRestResponse.getHttpStatusCode());

    }

    /**
     * Positive test case for getAppDetails method with mandatory parameters.
     */
    @Test(priority = 1,
          groups = { "wso2.esb" },
          dependsOnMethods = { "testgetAppAccessTokenWithMandatoryParameters" },
          description = "facebook {getAppDetails} integration test with mandatory parameters.")
    public void testGetAppDetailsWithMandatoryParameters() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:getAppDetails");

        String apiEndPoint =
                connectorProperties.getProperty("apiUrl") + connectorProperties.getProperty("appId") + "?access_token="
                        + connectorProperties.getProperty("appAccessToken");
        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);

        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getAppDetails_mandatory.txt");

        Assert.assertEquals(esbRestResponse.getBody().get("link"), apiRestResponse.getBody().get("link"));
        Assert.assertEquals(esbRestResponse.getBody().get("name"), apiRestResponse.getBody().get("name"));
        Assert.assertEquals(esbRestResponse.getBody().get("id"), apiRestResponse.getBody().get("id"));

    }

    /**
     * Positive test case for getEventDetails method with optional parameters.
     */
    @Test(priority = 1,
          groups = { "wso2.esb" },
          dependsOnMethods = { "testgetAppAccessTokenWithMandatoryParameters" },
          description = "facebook {getAppDetails} integration test with optional parameters.")
    public void testGetAppDetailsWithOptionalParameters() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:getAppDetails");
        String apiEndPoint =
                connectorProperties.getProperty("apiUrl") + connectorProperties.getProperty("appId")
                        + "/?access_token=" + connectorProperties.getProperty("appAccessToken") + "&fields=app_name";

        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getAppDetails_optional.txt");

        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);

        Assert.assertEquals(esbRestResponse.getBody().get("app_name").toString(),
                apiRestResponse.getBody().get("app_name").toString());

    }

    /**
     * Negative test case for getAppDetails method.
     */
    @Test(priority = 1,
          groups = { "wso2.esb" },
          dependsOnMethods = { "testgetAppAccessTokenWithMandatoryParameters" },
          description = "facebook {getAppDetails} integration test with negative case.")
    public void testGetAppDetailsWithNegativeCase() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:getAppDetails");
        String apiEndPoint =
                connectorProperties.getProperty("apiUrl") + connectorProperties.getProperty("invalid")
                        + "/?access_token=" + connectorProperties.getProperty("appAccessToken");

        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getAppDetails_negative.txt");

        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);

        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), apiRestResponse.getHttpStatusCode());
    }

    /**
     * Positive test case for banAppUser method with mandatory parameters.
     */
    @Test(priority = 1,
          groups = { "wso2.esb" },
          dependsOnMethods = { "testgetAppAccessTokenWithMandatoryParameters" },
          description = "facebook {banAppUser} integration test with optional parameters.")
    public void testBanAppUserWithMandatoryParameters() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:banAppUser");
        String apiEndPoint =
                connectorProperties.getProperty("apiUrl") + connectorProperties.getProperty("appId") + "/banned";

        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_banAppUser_mandatory.txt");
        RestResponse< JSONObject > apiRestResponse =
                sendJsonRestRequest(apiEndPoint, "POST", apiRequestHeadersMap, "api_banAppUser_mandatory.txt");
        Assert.assertEquals(esbRestResponse.getBody().get("success").toString(),
                apiRestResponse.getBody().get("success").toString());

    }

    /**
     * Negative test case for banAppUser method with optional parameters.
     */
    @Test(priority = 1,
          groups = { "wso2.esb" },
          dependsOnMethods = { "testgetAppAccessTokenWithMandatoryParameters" },
          description = "facebook {banAppUser} integration test with negative case.")
    public void testBanAppUserWithNegativeCase() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:banAppUser");
        String apiEndPoint = "https://graph.facebook.com/invalid/banned";

        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_banAppUser_negative.txt");
        RestResponse< JSONObject > apiRestResponse =
                sendJsonRestRequest(apiEndPoint, "POST", apiRequestHeadersMap, "api_banAppUser_mandatory.txt");
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), apiRestResponse.getHttpStatusCode());

    }

    /**
     * Positive test case for isAppUserBanned method with mandatory parameters.
     */
    @Test(priority = 1,
          dependsOnMethods = { "testBanAppUserWithMandatoryParameters" },
          groups = { "wso2.esb" },
          description = "facebook {isAppUserBanned} integration test with mandatory parameters.")
    public void testIsAppUserBannedWithMandatoryParameters() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:isAppUserBanned");
        String apiEndPoint =
                connectorProperties.getProperty("apiUrl") + connectorProperties.getProperty("appId") + "/banned/"
                        + connectorProperties.getProperty("appUserId") + "?access_token="
                        + connectorProperties.getProperty("appAccessToken");

        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_isAppUserBanned_mandatory.txt");

        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getBody().getJSONArray("data").getString(0), apiRestResponse.getBody()
                .getJSONArray("data").getString(0));

    }

    /**
     * Negative test case for isAppUserBanned method.
     */
    @Test(priority = 1,
          dependsOnMethods = { "testBanAppUserWithMandatoryParameters" },
          groups = { "wso2.esb" },
          description = "facebook {isAppUserBanned} integration test with negative case.")
    public void testIsAppUserBannedWithNegativeCase() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:isAppUserBanned");
        String apiEndPoint =
                connectorProperties.getProperty("apiUrl") + connectorProperties.getProperty("appId")
                        + "/banned/invalid" + "?access_token=" + connectorProperties.getProperty("appAccessToken");

        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_isAppUserBanned_negative.txt");

        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), apiRestResponse.getHttpStatusCode());
    }

    /**
     * Positive test case for unbanAppUser method with mandatory parameters.
     */
    @Test(priority = 1,
          dependsOnMethods = { "testBanAppUserWithMandatoryParameters" },
          groups = { "wso2.esb" },
          description = "facebook {unbanAppUser} integration test with mandatory parameters.")
    public void testUnbanAppUserWithMandatoryParameters() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:unbanAppUser");
        String apiEndPoint =
                connectorProperties.getProperty("apiUrl") + connectorProperties.getProperty("appId") + "/banned/"
                        + connectorProperties.getProperty("appUserId") + "?access_token="
                        + connectorProperties.getProperty("appAccessToken");

        RestResponse< JSONObject > apiRestResponse1 = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);

        sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_unbanAppUser_mandatory.txt");
        RestResponse< JSONObject > apiRestResponse2 = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertNotEquals(apiRestResponse1.getBody().getJSONArray("data"), apiRestResponse2.getBody()
                .getJSONArray("data"));

    }

    /**
     * Negative test case for unbanAppUser method.
     */
    @Test(priority = 1,
          dependsOnMethods = { "testBanAppUserWithMandatoryParameters" },
          groups = { "wso2.esb" },
          description = "facebook {unbanAppUser} integration test with negative case.")
    public void testUnbanAppUserWithNegativeCase() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:unbanAppUser");
        String apiEndPoint =
                "https://graph.facebook.com/invalid/banned/" + connectorProperties.getProperty("appUserId")
                        + "?access_token=" + connectorProperties.getProperty("appAccessToken");

        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_unbanAppUser_negative.txt");

        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "DELETE", apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), apiRestResponse.getHttpStatusCode());
    }

    /**
     * Positive test case for getPageDetails method with mandatory parameters.
     */
    @Test(priority = 1,
          groups = { "wso2.esb" },
          description = "facebook {getPageDetails} integration test with mandatory parameters.")
    public void testGetPageDetailsWithMandatoryParameters() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:getPageDetails");
        String apiEndPoint =
                connectorProperties.getProperty("apiUrl") + connectorProperties.getProperty("pageId")
                        + "?access_token=" + connectorProperties.getProperty("pageAccessToken");

        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getPageDetails_mandatory.txt");

        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getBody().get("name"), apiRestResponse.getBody().get("name"));
        Assert.assertEquals(esbRestResponse.getBody().get("id"), apiRestResponse.getBody().get("id"));

    }

    /**
     * Positive test case for getPageDetails method with optional parameters.
     */
    @Test(priority = 1,
          groups = { "wso2.esb" },
          description = "facebook {getPageDetails} integration test with optional parameters.")
    public void testGetPageDetailsWithOptionalParameters() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:getPageDetails");
        String apiEndPoint =
                connectorProperties.getProperty("apiUrl") + connectorProperties.getProperty("pageId")
                        + "/?access_token=" + connectorProperties.getProperty("pageAccessToken") + "&fields=category";

        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getPageDetails_optional.txt");
        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getBody().get("category").toString(),
                apiRestResponse.getBody().get("category").toString());

    }

    /**
     * Negative test case for getPageDetails method.
     */
    @Test(priority = 1,
          groups = { "wso2.esb" },
          description = "facebook {getPageDetails} integration test with negative case.")
    public void testGetPageDetailsWithNegativeCase() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:getPageDetails");
        String apiEndPoint =
                "https://graph.facebook.com/invalid?access_token=" + connectorProperties.getProperty("pageAccessToken");

        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getPageDetails_negative.txt");

        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), apiRestResponse.getHttpStatusCode());
    }

    /**
     * Positive test case for createPageAlbum method with mandatory parameters.
     *
     * @throws InterruptedException
     */
    @Test(priority = 1,
          groups = { "wso2.esb" },
          description = "facebook {createPageAlbum} integration test with optional parameters.")
    public void testCreatePageAlbumWithMandatoryParameters() throws IOException, JSONException, InterruptedException {

        esbRequestHeadersMap.put("Action", "urn:createPageAlbum");

        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_createPageAlbum_mandatory.txt");
        Thread.sleep(timeOut);
        String apiEndPoint =
                connectorProperties.getProperty("apiUrl") + esbRestResponse.getBody().get("id").toString()
                        + "?access_token=" + connectorProperties.getProperty("pageAccessToken");
        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getBody().get("id"), apiRestResponse.getBody().get("id"));
        Assert.assertEquals(apiRestResponse.getBody().get("name").toString(),
                connectorProperties.getProperty("albumName"));

    }

    /**
     * Positive test case for createPageAlbum method with optional parameters.
     *
     * @throws InterruptedException
     */
    @Test(priority = 1,
          groups = { "wso2.esb" },
          description = "facebook {createPageAlbum} integration test with optional parameters.")
    public void testCreatePageAlbumWithOptionalParameters() throws IOException, JSONException, InterruptedException {

        esbRequestHeadersMap.put("Action", "urn:createPageAlbum");

        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_createPageAlbum_optional.txt");
        Thread.sleep(timeOut);
        String apiEndPoint =
                connectorProperties.getProperty("apiUrl") + esbRestResponse.getBody().get("id").toString()
                        + "?access_token=" + connectorProperties.getProperty("pageAccessToken");
        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getBody().get("id"), apiRestResponse.getBody().get("id"));
        Assert.assertEquals(apiRestResponse.getBody().get("name").toString(),
                connectorProperties.getProperty("albumName"));
    }

    /**
     * Negative test case for createPageAlbum method.
     */
    @Test(priority = 1,
          groups = { "wso2.esb" },
          description = "facebook {createPageAlbum} integration test with negative case.")
    public void testCreatePageAlbumWithNegativeCase() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:createPageAlbum");

        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_createPageAlbum_negative.txt");
        String apiEndPoint =
                "https://graph.facebook.com/invalid/albums?access_token="
                        + connectorProperties.getProperty("pageAccessToken");
        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), apiRestResponse.getHttpStatusCode());
    }

    /**
     * Positive test case for blockUserFromPage method with mandatory parameters.
     *
     * @throws InterruptedException
     */
    @Test(priority = 1,
          groups = { "wso2.esb" },
          description = "facebook {blockUserFromPage} integration test with optional parameters.")
    public void testBlockUserFromPageWithMandatoryParameters() throws IOException, JSONException, InterruptedException {

        esbRequestHeadersMap.put("Action", "urn:blockUserFromPage");
        String apiEndPoint =
                connectorProperties.getProperty("apiUrl") + connectorProperties.getProperty("pageId")
                        + "/blocked?access_token=" + connectorProperties.getProperty("pageAccessToken");

        sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_blockUserFromPage_mandatory.txt");

        Thread.sleep(timeOut);
        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);

        JSONObject response = (JSONObject) apiRestResponse.getBody().getJSONArray("data").get(0);
        Assert.assertEquals(response.get("name").toString(), connectorProperties.getProperty("blockedUserName"));
    }

    /**
     * Negative test case for blockUserFromPage method with optional parameters.
     */
    @Test(priority = 1,
          groups = { "wso2.esb" },
          description = "facebook {blockUserFromPage} integration test with negative case.")
    public void testBlockUserFromPageWithNegativeCase() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:blockUserFromPage");
        String apiEndPoint = "https://graph.facebook.com/invalid/blocked";

        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_blockUserFromPage_negative.txt");
        RestResponse< JSONObject > apiRestResponse =
                sendJsonRestRequest(apiEndPoint, "POST", apiRequestHeadersMap, "api_blockUserFromPage_mandatory.txt");
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), apiRestResponse.getHttpStatusCode());

    }

    /**
     * Positive test case for unblockUserFromPage method with mandatory parameters.
     */
    @Test(priority = 1,
          dependsOnMethods = { "testBlockUserFromPageWithMandatoryParameters" },
          groups = { "wso2.esb" },
          description = "facebook {unblockUserFromPage} integration test with mandatory parameters.")
    public void testUnblockUserFromPageWithMandatoryParameters() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:unblockUserFromPage");
        String apiEndPoint =
                connectorProperties.getProperty("apiUrl") + connectorProperties.getProperty("pageId") + "/blocked/"
                        + connectorProperties.getProperty("appUserId") + "?access_token="
                        + connectorProperties.getProperty("pageAccessToken");

        RestResponse< JSONObject > apiRestResponse1 = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);

        sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_unblockUserFromPage_mandatory.txt");
        RestResponse< JSONObject > apiRestResponse2 = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertNotEquals(apiRestResponse1.getBody().getJSONArray("data"), apiRestResponse2.getBody()
                .getJSONArray("data"));

    }

    /**
     * Negative test case for unblockUserFromPage method.
     */
    @Test(priority = 1,
          dependsOnMethods = { "testBlockUserFromPageWithMandatoryParameters" },
          groups = { "wso2.esb" },
          description = "facebook {unblockUserFromPage} integration test with negative case.")
    public void testUnblockUserFromPageWithNegativeCase() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:unblockUserFromPage");
        String apiEndPoint =
                "https://graph.facebook.com/invalid/blocked/" + connectorProperties.getProperty("appUserId")
                        + "?access_token=" + connectorProperties.getProperty("pageAccessToken");

        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_unblockUserFromPage_negative.txt");

        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "DELETE", apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), apiRestResponse.getHttpStatusCode());
    }

    /**
     * Positive test case for publishPagePost method with mandatory parameters.
     *
     * @throws InterruptedException
     */
    @Test(priority = 1,
          groups = { "wso2.esb" },
          description = "facebook {publishPagePost} integration test with optional parameters.")
    public void testPublishPagePostWithMandatoryParameters() throws IOException, JSONException, InterruptedException {

        esbRequestHeadersMap.put("Action", "urn:publishPagePost");

        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_publishPagePost_mandatory.txt");
        Thread.sleep(timeOut);

        String pagePostId = esbRestResponse.getBody().get("id").toString();
        connectorProperties.put("pagePostId", pagePostId);

        String apiEndPoint =
                connectorProperties.getProperty("apiUrl") + esbRestResponse.getBody().get("id").toString()
                        + "?access_token=" + connectorProperties.getProperty("pageAccessToken");

        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);

        Assert.assertEquals(esbRestResponse.getBody().get("id"), apiRestResponse.getBody().get("id"));
        Assert.assertEquals(apiRestResponse.getBody().get("message"), connectorProperties.getProperty("message"));

    }

    /**
     * Positive test case for publishPagePost method with optional parameters.
     */
    @Test(priority = 1,
          groups = { "wso2.esb" },
          description = "facebook {publishPagePost} integration test with optional parameters.")
    public void testPublishPagePostWithOptionalParameters() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:publishPagePost");

        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_publishPagePost_optional.txt");

        String apiEndPoint =
                connectorProperties.getProperty("apiUrl") + esbRestResponse.getBody().get("id").toString()
                        + "?fields=message,link&access_token=" + connectorProperties.getProperty("pageAccessToken");
        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getBody().get("id"), apiRestResponse.getBody().get("id"));
        Assert.assertEquals(apiRestResponse.getBody().get("message"), connectorProperties.getProperty("groupName"));
        Assert.assertEquals(apiRestResponse.getBody().get("link"), "https://www.google.lk/");
    }

    /**
     * Negative test case for publishPagePost method.
     */
    @Test(priority = 1,
          groups = { "wso2.esb" },
          description = "facebook {publishPagePost} integration test with negative case.")
    public void testPublishPagePostWithNegativeCase() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:publishPagePost");

        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_publishPagePost_negative.txt");
        String apiEndPoint = "https://graph.facebook.com/invalid/feed";
        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), apiRestResponse.getHttpStatusCode());
    }

    /**
     * Positive test case for updatePageDetails method with optional parameters.
     *
     * @throws InterruptedException
     */
    @Test(priority = 1,
          groups = { "wso2.esb" },
          description = "facebook {updatePageDetails} integration test with optional parameters.")
    public void testUpdatePageDetailsWithOptionalParameters() throws IOException, JSONException, InterruptedException {

        esbRequestHeadersMap.put("Action", "urn:updatePageDetails");
        String apiEndPoint =
                connectorProperties.getProperty("apiUrl") + connectorProperties.getProperty("pageId")
                        + "?fields=about&access_token=" + connectorProperties.getProperty("pageAccessToken");
        sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_updatePageDetails_optional.txt");

        Thread.sleep(timeOut);
        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);

        Assert.assertEquals(apiRestResponse.getBody().get("about"), connectorProperties.getProperty("about"));

    }

    /**
     * Negative test case for updatePageDetails method.
     */
    @Test(priority = 1,
          groups = { "wso2.esb" },
          description = "facebook {updatePageDetails} integration test with negative case.")
    public void testUpdatePageDetailsWithNegativeCase() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:updatePageDetails");

        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_updatePageDetails_negative.txt");
        String apiEndPoint = "https://graph.facebook.com/invalid";
        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), apiRestResponse.getHttpStatusCode());
    }

    /**
     * Positive test case for updateAppDetails method with optional parameters.
     */
    @Test(priority = 1,
          groups = { "wso2.esb" },
          dependsOnMethods = { "testgetAppAccessTokenWithMandatoryParameters" },
          description = "facebook {updateAppDetails} integration test with optional parameters.")
    public void testUpdateAppDetailsWithOptionalParameters() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:updateAppDetails");
        String apiEndPoint =
                connectorProperties.getProperty("apiUrl") + connectorProperties.getProperty("appId") + "?access_token="
                        + connectorProperties.getProperty("appAccessToken") + "&fields=canvas_url";
        sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_updateAppDetails_optional.txt");
        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);

        Assert.assertEquals(apiRestResponse.getBody().get("canvas_url"), connectorProperties.getProperty("canvasUrl"));

    }

    /**
     * Negative test case for updateAppDetails method.
     */
    @Test(priority = 1,
          groups = { "wso2.esb" },
          dependsOnMethods = { "testgetAppAccessTokenWithMandatoryParameters" },
          description = "facebook {updateAppDetails} integration test with negative case.")
    public void testUpdateAppDetailsWithNegativeCase() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:updateAppDetails");

        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_updateAppDetails_negative.txt");
        String apiEndPoint =
                "https://graph.facebook.com/invalid?access_token=" + connectorProperties.getProperty("appAccessToken");
        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), apiRestResponse.getHttpStatusCode());
    }

    /**
     * Positive test case for replyToConversation method with mandatory parameters.
     */
    @Test(priority = 1,
          groups = { "wso2.esb" },
          description = "facebook {replyToConversation} integration test with optional parameters.")
    public void testReplyToConversationWithMandatoryParameters() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:replyToConversation");

        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_replyToConversation_mandatory.txt");

        String apiEndPoint =
                connectorProperties.getProperty("apiUrl") + esbRestResponse.getBody().get("id").toString()
                        + "?access_token=" + connectorProperties.getProperty("pageAccessToken") + "&fields=message";
        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(apiRestResponse.getBody().get("message"), connectorProperties.getProperty("description"));
    }

    /**
     * Negative test case for replyToConversation method.
     */
    @Test(priority = 1,
          groups = { "wso2.esb" },
          description = "facebook {replyToConversation} integration test with negative case.")
    public void testReplyToConversationWithNegativeCase() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:replyToConversation");

        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_replyToConversation_negative.txt");
        String apiEndPoint =
                "https://graph.facebook.com/invalid?access_token=" + connectorProperties.getProperty("pageAccessToken");
        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), apiRestResponse.getHttpStatusCode());
    }

    /**
     * Negative test case for createAppUserRole method.
     */
    @Test(priority = 1,
          groups = { "wso2.esb" },
          dependsOnMethods = { "testgetAppAccessTokenWithMandatoryParameters" },
          description = "facebook {createAppUserRole} integration test with negative case.")
    public void testCreateAppUserRoleWithNegativeCase() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:createAppUserRole");

        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_createAppUserRole_negative.txt");
        String apiEndPoint =
                "https://graph.facebook.com/invalid/roles?access_token="
                        + connectorProperties.getProperty("appAccessToken");
        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), apiRestResponse.getHttpStatusCode());
    }

    /**
     * Positive test case for deleteAppUserRole method with mandatory parameters.
     */
    @Test(priority = 1,
          dependsOnMethods = { "testgetAppAccessTokenWithMandatoryParameters" },
          groups = { "wso2.esb" },
          description = "facebook {deleteAppUserRole} integration test with optional parameters.")
    public void testDeleteAppUserRoleWithMandatoryParameters() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:deleteAppUserRole");
        String apiEndPoint =
                connectorProperties.getProperty("apiUrl") + connectorProperties.getProperty("appId")
                        + "/roles?access_token=" + connectorProperties.getProperty("appAccessToken");
        RestResponse< JSONObject > apiRestResponse1 = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);

        sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_deleteAppUserRole_mandatory.txt");

        RestResponse< JSONObject > apiRestResponse2 = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);

        Assert.assertNotEquals(apiRestResponse1.getBody().getJSONArray("data"), apiRestResponse2.getBody()
                .getJSONArray("data"));
    }

    /**
     * Negative test case for deleteAppUserRole method.
     */
    @Test(priority = 1,
          groups = { "wso2.esb" },
          dependsOnMethods = { "testgetAppAccessTokenWithMandatoryParameters" },
          description = "facebook {deleteAppUserRole} integration test with negative case.")
    public void testDeleteAppUserRoleWithNegativeCase() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:deleteAppUserRole");

        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_deleteAppUserRole_negative.txt");
        String apiEndPoint =
                "https://graph.facebook.com/invalid/roles?access_token="
                        + connectorProperties.getProperty("appAccessToken");
        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), apiRestResponse.getHttpStatusCode());
    }


    @Test(priority = 1,
          groups = { "wso2.esb" },
          description = "facebook {createPageMilestone} integration test with mandatory parameters.")
    public void testCreatePageMilestoneMandatoryParameters() throws IOException, JSONException, InterruptedException {

        esbRequestHeadersMap.put("Action", "urn:publishPageMilestone");
        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_createPageMilestone_mandatory.txt");
        connectorProperties.put("milestoneId", esbRestResponse.getBody().get("id").toString());
        String apiEndPoint =
                connectorProperties.getProperty("apiUrl") + connectorProperties.getProperty("milestoneId")
                        + "?access_token=" + connectorProperties.getProperty("pageAccessToken");
        Thread.sleep(timeOut);
        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertTrue(apiRestResponse.getBody().has("id"));
    }

    /**
     * Negative test case for createPageMilestone.
     */

    @Test(priority = 1,
          groups = { "wso2.esb" },
          dependsOnMethods = { "testCreatePageMilestoneMandatoryParameters" },
          description = "facebook {createPageMilestone} integration test negative case.")
    public void testCreatePageMilestoneNegativeCase() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:publishPageMilestone");
        String apiEndPoint =
                connectorProperties.getProperty("apiUrl") + connectorProperties.getProperty("pageId") + "/milestones";
        RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap,
                "esb_createPageMilestone_negative.txt");
        RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "POST", apiRequestHeadersMap,
                "api_createPageMilestone_negative.txt");
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("error").get("message").toString(),
                apiRestResponse.getBody().getJSONObject("error").get("message").toString());
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("error").get("code").toString(),
                apiRestResponse.getBody().getJSONObject("error").get("code").toString());
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("error").get("type").toString(),
                apiRestResponse.getBody().getJSONObject("error").get("type").toString());
    }

    /**
     * Positive test case for getPageMilestoneDetails method with mandatory parameters.
     */

    @Test(priority = 1,
          groups = { "wso2.esb" },
          dependsOnMethods = { "testCreatePageMilestoneNegativeCase" },
          description = "facebook {getPageMilestoneDetails} integration test with mandatory parameters.")
    public void testGetPageMilestoneDetailsMandatoryParameters() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:getPageMilestoneDetails");
        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getPageMilestoneDetails_mandatory.txt");
        String apiEndPoint =
                connectorProperties.getProperty("apiUrl") + connectorProperties.getProperty("pageId")
                        + "/milestones?access_token=" + connectorProperties.getProperty("pageAccessToken");
        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getBody().get("data").toString(), apiRestResponse.getBody().get("data")
                .toString());
    }

    /**
     * Positive test case for getPageMilestoneDetails method with optional parameters.
     */

    @Test(priority = 1,
          groups = { "wso2.esb" },
          dependsOnMethods = { "testGetPageMilestoneDetailsMandatoryParameters" },
          description = "facebook {getPageMilestoneDetails} integration test with optional parameters.")
    public void testGetPageMilestoneDetailsOptionalParameters() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:getPageMilestoneDetails");
        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getPageMilestoneDetails_optional.txt");
        String apiEndPoint =
                connectorProperties.getProperty("apiUrl") + connectorProperties.getProperty("pageId")
                        + "/milestones?fields=data&access_token=" + connectorProperties.getProperty("pageAccessToken");
        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getBody().get("data").toString(), apiRestResponse.getBody().get("data")
                .toString());
    }

    /**
     * Negative test case for getPageMilestoneDetails method
     */

    @Test(priority = 1,
          groups = { "wso2.esb" },
          dependsOnMethods = { "testGetPageMilestoneDetailsOptionalParameters" },
          description = "facebook {getPageMilestoneDetails} integration test with optional parameters.")
    public void testGetPageMilestoneDetailsNegativeCase() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:getPageMilestoneDetails");
        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getPageMilestoneDetails_negative.txt");
        String apiEndPoint =
                "https://graph.facebook.com/invalid12342/milestones?fields=id&access_token="
                        + connectorProperties.getProperty("pageAccessToken");
        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("error").get("message").toString(),
                apiRestResponse.getBody().getJSONObject("error").get("message").toString());
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("error").get("code").toString(),
                apiRestResponse.getBody().getJSONObject("error").get("code").toString());
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("error").get("type").toString(),
                apiRestResponse.getBody().getJSONObject("error").get("type").toString());
    }

    /**
     * Positive test case for updatePageMilestone method with optional parameters.
     */

    @Test(priority = 1,
          groups = { "wso2.esb" },
          dependsOnMethods = { "testGetPageMilestoneDetailsNegativeCase" },
          description = "facebook {updatePageMilestone} integration test with optional parameters.")
    public void testUpdatePageMilestoneOptionalParameters() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:updatePageMilestone");
        String apiEndPoint = connectorProperties.getProperty("apiUrl") + connectorProperties.getProperty("milestoneId")
                + "?fields=title&access_token=" + connectorProperties.getProperty("pageAccessToken");
        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_updatePageMilestone_optional.txt");
        RestResponse< JSONObject > apiRestResponse =
                sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(connectorProperties.getProperty("title"), apiRestResponse.getBody().get("title")
                .toString());
    }

    /**
     * Negative test case for updatePageMilestone method
     */

    @Test(priority = 1,
          groups = { "wso2.esb" },
          dependsOnMethods = { "testUpdatePageMilestoneOptionalParameters" },
          description = "facebook {updatePageMilestone} integration test negative case.")
    public void testUpdatePageMilestoneNegativeCase() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:updatePageMilestone");
        String apiEndPoint = connectorProperties.getProperty("apiUrl") + connectorProperties.getProperty("milestoneId");
        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_updatePageMilestone_negative.txt");
        RestResponse< JSONObject > apiRestResponse =
                sendJsonRestRequest(apiEndPoint, "POST", apiRequestHeadersMap, "api_updatePageMilestone_negative.txt");
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("error").get("message").toString(),
                apiRestResponse.getBody().getJSONObject("error").get("message").toString());
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("error").get("code").toString(),
                apiRestResponse.getBody().getJSONObject("error").get("code").toString());
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("error").get("type").toString(),
                apiRestResponse.getBody().getJSONObject("error").get("type").toString());
    }

    /**
     * Negative test case for deletePageMilestone method
     */

    @Test(priority = 1,
          groups = { "wso2.esb" },
          dependsOnMethods = { "testUpdatePageMilestoneNegativeCase" },
          description = "facebook {deletePageMilestone} integration test negative case.")
    public void testDeletePageMilestoneNegativeCase() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:deletePageMilestone");
        String apiEndPoint =
                connectorProperties.getProperty("apiUrl") + connectorProperties.getProperty("milestoneId")
                        + "?access_token=" + connectorProperties.getProperty("pageAccessToken");
        RestResponse< JSONObject > firstApiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_deletePageMilestone_negative.txt");
        RestResponse< JSONObject > secondApiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertTrue(firstApiRestResponse.getBody().has("id") && secondApiRestResponse.getBody().has("id"));
    }

    /**
     * Positive test case for deletePageMilestone method with mandatory parameters
     */

    @Test(priority = 1,
          groups = { "wso2.esb" },
          dependsOnMethods = { "testDeletePageMilestoneNegativeCase" },
          description = "facebook {deletePageMilestone} integration test with optional parameters.")
    public void testDeletePageMilestoneMandatoryParameters() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:deletePageMilestone");
        String apiEndPoint =
                connectorProperties.getProperty("apiUrl") + connectorProperties.getProperty("milestoneId")
                        + "?access_token=" + connectorProperties.getProperty("pageAccessToken");
        RestResponse< JSONObject > firstApiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_deletePageMilestone_mandatory.txt");
        RestResponse< JSONObject > secondApiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertTrue(firstApiRestResponse.getBody().has("id") && secondApiRestResponse.getBody().has("error"));
    }

    /**
     * Positive test case for getFriendListDetails method with mandatory parameters.
     */

    @Test(priority = 1,
          groups = { "wso2.esb" },
          description = "facebook {getFriendListDetails} integration test with mandatory parameters.")
    public void testGetFriendListDetailsMandatoryParameters() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:getFriendListDetails");
        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getFriendListDetails_mandatory.txt");
        String apiEndPoint =
                connectorProperties.getProperty("apiUrl") + connectorProperties.getProperty("friendListId")
                        + "?access_token=" + connectorProperties.getProperty("accessToken");
        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(apiRestResponse.getBody().get("id").toString(), esbRestResponse.getBody().get("id")
                .toString());
    }

    /**
     * Positive test case for getFriendListDetails method with optional parameters.
     */

    @Test(priority = 1,
          groups = { "wso2.esb" },
          dependsOnMethods = { "testGetFriendListDetailsMandatoryParameters" },
          description = "facebook {getFriendListDetails} integration test with optional parameters.")
    public void testGetFriendListDetailsOptionalParameters() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:getFriendListDetails");
        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getFriendListDetails_optional.txt");
        String apiEndPoint =
                connectorProperties.getProperty("apiUrl") + connectorProperties.getProperty("friendListId")
                        + "?fields=name&access_token=" + connectorProperties.getProperty("accessToken");
        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(apiRestResponse.getBody().get("name").toString(), esbRestResponse.getBody().get("name")
                .toString());
    }

    /**
     * Negative test case for getFriendListDetails method
     */

    @Test(priority = 1,
          groups = { "wso2.esb" },
          dependsOnMethods = { "testGetFriendListDetailsOptionalParameters" },
          description = "facebook {getFriendListDetails} integration test negative case.")
    public void testGetFriendListDetailsNegativeCase() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:getFriendListDetails");
        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getFriendListDetails_negative.txt");
        String apiEndPoint =
                "https://graph.facebook.com/invalid12342?fields=id&access_token="
                        + connectorProperties.getProperty("accessToken");
        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("error").get("message").toString(),
                apiRestResponse.getBody().getJSONObject("error").get("message").toString());
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("error").get("code").toString(),
                apiRestResponse.getBody().getJSONObject("error").get("code").toString());
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("error").get("type").toString(),
                apiRestResponse.getBody().getJSONObject("error").get("type").toString());
    }

    /**
     * Positive test case for getUserDetails method with mandatory parameters
     */

    @Test(priority = 1,
          groups = { "wso2.esb" },
          description = "facebook {getUserDetails} integration test with mandatory parameters.")
    public void testGetUserDetailsMandatoryParameters() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:getUserDetails");
        String apiEndPoint =
                "https://graph.facebook.com/me?access_token=" + connectorProperties.getProperty("accessToken");
        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getUserDetails_mandatory.txt");
        Assert.assertEquals(apiRestResponse.getBody().get("name").toString(), esbRestResponse.getBody().get("name")
                .toString());
    }

    /**
     * Positive test case for getUserDetails method with optional parameters
     */

    @Test(priority = 1,
          groups = { "wso2.esb" },
          description = "facebook {getUserDetails} integration test with optional parameters.")
    public void testGetUserDetailsOptionalParameters() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:getUserDetails");
        String apiEndPoint =
                "https://graph.facebook.com/me?fields=id&access_token="
                        + connectorProperties.getProperty("accessToken");
        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getUserDetails_optional.txt");
        Assert.assertEquals(apiRestResponse.getBody().get("id").toString(), esbRestResponse.getBody().get("id")
                .toString());
    }

    /**
     * Negative test case for getUserDetails method
     */

    @Test(priority = 1,
          groups = { "wso2.esb" },
          description = "facebook {getUserDetails} integration test negative case.")
    public void testGetUserDetailsNegativeCase() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:getUserDetails");
        String apiEndPoint =
                "https://graph.facebook.com/me?fields=abc&access_token="
                        + connectorProperties.getProperty("accessToken");
        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getUserDetails_negative.txt");
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("error").get("message").toString(),
                apiRestResponse.getBody().getJSONObject("error").get("message").toString());
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("error").get("code").toString(),
                apiRestResponse.getBody().getJSONObject("error").get("code").toString());
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("error").get("type").toString(),
                apiRestResponse.getBody().getJSONObject("error").get("type").toString());
    }

    /**
     * Positive test case for createAlbum method with mandatory parameters
     */

    @Test(priority = 1,
          groups = { "wso2.esb" },
          description = "facebook {createAlbum} integration test with mandatory parameters.")
    public void testCreateAlbumMandatoryParameters() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:createAlbum");
        connectorProperties.put("name", "test" + new Date().toString());
        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_createAlbum_mandatory.txt");
        connectorProperties.put("albumId", esbRestResponse.getBody().get("id").toString());
        String apiEndPoint =
                connectorProperties.getProperty("apiUrl") + connectorProperties.getProperty("albumId")
                        + "?access_token=" + connectorProperties.getProperty("accessToken");
        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertTrue(apiRestResponse.getBody().has("id"));
    }

    /**
     * Positive test case for createAlbum method with optional parameters
     */

    @Test(priority = 1,
          groups = { "wso2.esb" },
          dependsOnMethods = { "testCreateAlbumMandatoryParameters" },
          description = "facebook {createAlbum} integration test with optional parameters.")
    public void testCreateAlbumOptionalParameters() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:createAlbum");
        connectorProperties.put("name", "test" + new Date().toString());
        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_createAlbum_optional.txt");
        connectorProperties.put("albumId", esbRestResponse.getBody().get("id").toString());
        String apiEndPoint =
                connectorProperties.getProperty("apiUrl") + connectorProperties.getProperty("albumId")
                        + "?access_token=" + connectorProperties.getProperty("accessToken");
        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertTrue(apiRestResponse.getBody().has("id"));
    }

    /**
     * Negative test case for createAlbum method
     */

    @Test(priority = 1,
          groups = { "wso2.esb" },
          dependsOnMethods = { "testCreateAlbumOptionalParameters" },
          description = "facebook {createAlbum} integration test negative case.")
    public void testCreateAlbumNegativeCase() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:createAlbum");
        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_createAlbum_negative.txt");
        String apiEndPoint = "https://graph.facebook.com/me/albums";
        RestResponse< JSONObject > apiRestResponse =
                sendJsonRestRequest(apiEndPoint, "POST", apiRequestHeadersMap, "api_createAlbum_negative.txt");
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("error").get("message").toString(),
                apiRestResponse.getBody().getJSONObject("error").get("message").toString());
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("error").get("code").toString(),
                apiRestResponse.getBody().getJSONObject("error").get("code").toString());
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("error").get("type").toString(),
                apiRestResponse.getBody().getJSONObject("error").get("type").toString());
    }

    /**
     * Positive test case for getAlbumDetails method with mandatory parameters
     */

    @Test(priority = 1,
          groups = { "wso2.esb" },
          dependsOnMethods = { "testCreateAlbumNegativeCase" },
          description = "facebook {getAlbumDetails} integration test with mandatory parameters.")
    public void testGetAlbumDetailsMandatoryParameters() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:getAlbumDetails");
        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getAlbumDetails_mandatory.txt");
        String apiEndPoint =
                connectorProperties.getProperty("apiUrl") + connectorProperties.getProperty("albumId")
                        + "?access_token=" + connectorProperties.getProperty("accessToken");
        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(apiRestResponse.getBody().get("name").toString(), esbRestResponse.getBody().get("name")
                .toString());
    }

    /**
     * Positive test case for getAlbumDetails method with optional parameters
     */

    @Test(priority = 1,
          groups = { "wso2.esb" },
          dependsOnMethods = { "testGetAlbumDetailsMandatoryParameters" },
          description = "facebook {getAlbumDetails} integration test with optional parameters.")
    public void testGetAlbumDetailsOptionalParameters() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:getAlbumDetails");
        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getAlbumDetails_optional.txt");
        String apiEndPoint =
                connectorProperties.getProperty("apiUrl") + connectorProperties.getProperty("albumId")
                        + "?fields=id&access_token=" + connectorProperties.getProperty("accessToken");
        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(apiRestResponse.getBody().get("id").toString(), esbRestResponse.getBody().get("id")
                .toString());
    }

    /**
     * Negative test case for getAlbumDetails method
     */

    @Test(priority = 1,
          groups = { "wso2.esb" },
          dependsOnMethods = { "testGetAlbumDetailsOptionalParameters" },
          description = "facebook {getAlbumDetails} integration test negative case.")
    public void testGetAlbumDetailsNegativeCase() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:getAlbumDetails");
        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getAlbumDetails_negative.txt");
        String apiEndPoint =
                "https://graph.facebook.com/invalid12342?fields=id&access_token="
                        + connectorProperties.getProperty("accessToken");
        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("error").get("message").toString(),
                apiRestResponse.getBody().getJSONObject("error").get("message").toString());
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("error").get("code").toString(),
                apiRestResponse.getBody().getJSONObject("error").get("code").toString());
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("error").get("type").toString(),
                apiRestResponse.getBody().getJSONObject("error").get("type").toString());
    }


    /**
     * Positive test case for createPost: post status method with mandatory parameters
     *
     * @throws InterruptedException
     */

    @Test(priority = 1,
          groups = { "wso2.esb" },
          description = "facebook {createPost} integration test with mandatory parameters.")
    public void testCreatePostStatusMandatoryParameters() throws IOException, JSONException, InterruptedException {

        esbRequestHeadersMap.put("Action", "urn:createPost");
        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_createPost_postStatus_mandatory.txt");

        connectorProperties.put("statusMessageId", esbRestResponse.getBody().getString("id"));

        Thread.sleep(timeOut);
        String apiEndPoint =
                connectorProperties.getProperty("apiUrl") + connectorProperties.getProperty("statusMessageId")
                        + "?access_token=" + connectorProperties.getProperty("accessToken");
        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);

        Assert.assertTrue(apiRestResponse.getBody().has("id"));

    }

    /**
     * Positive test case for createPost: post link method with mandatory parameters
     *
     * @throws InterruptedException
     */

    @Test(priority = 1,
          groups = { "wso2.esb" },
          description = "facebook {createPost} integration test with mandatory parameters.")
    public void testCreatePostLinkMandatoryParameters() throws IOException, JSONException, InterruptedException {

        esbRequestHeadersMap.put("Action", "urn:createPost");
        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_createPost_postLink_mandatory.txt");
        connectorProperties.put("postId", esbRestResponse.getBody().getString("id"));
        Thread.sleep(timeOut);
        String apiEndPoint =
                connectorProperties.getProperty("apiUrl") + connectorProperties.getProperty("postId")
                        + "?access_token=" + connectorProperties.getProperty("accessToken");
        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertTrue(apiRestResponse.getBody().has("id"));
    }

    /**
     * Positive test case for createPost: post link method with optional parameters
     *
     * @throws InterruptedException
     */

    @Test(priority = 1,
          groups = { "wso2.esb" },
          description = "facebook {createPost} integration test with optional parameters.")
    public void testCreatePostLinkOptionalParameters() throws IOException, JSONException, InterruptedException {

        esbRequestHeadersMap.put("Action", "urn:createPost");
        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_createPost_postLink_optional.txt");
        connectorProperties.put("postId", esbRestResponse.getBody().getString("id"));
        Thread.sleep(timeOut);
        String apiEndPoint =
                connectorProperties.getProperty("apiUrl") + connectorProperties.getProperty("postId")
                        + "?access_token=" + connectorProperties.getProperty("accessToken");
        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertTrue(apiRestResponse.getBody().has("id"));
    }

    /**
     * Negative test case for createPost
     */

    @Test(priority = 1,
          groups = { "wso2.esb" },
          description = "facebook {createPost} integration test negative case.")
    public void testCreatePostNegativeCase() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:createPost");
        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_createPost_negative.txt");
        String apiEndPoint = "https://graph.facebook.com/me/feed";
        RestResponse< JSONObject > apiRestResponse =
                sendJsonRestRequest(apiEndPoint, "POST", apiRequestHeadersMap, "api_createPost_negative.txt");
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("error").get("message").toString(),
                apiRestResponse.getBody().getJSONObject("error").get("message").toString());
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("error").get("code").toString(),
                apiRestResponse.getBody().getJSONObject("error").get("code").toString());
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("error").get("type").toString(),
                apiRestResponse.getBody().getJSONObject("error").get("type").toString());
    }

    /**
     * Positive test case for getPost with mandatory parameters
     */

    @Test(priority = 1,
          groups = { "wso2.esb" },
          dependsOnMethods = { "testCreatePostLinkOptionalParameters" },
          description = "facebook {getPost} integration test with mandatory parameters.")
    public void testGetPostMandatoryParameters() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:getPost");
        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getPost_mandatory.txt");
        String apiEndPoint =
                connectorProperties.getProperty("apiUrl") + connectorProperties.getProperty("postId")
                        + "?access_token=" + connectorProperties.getProperty("accessToken");
        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(apiRestResponse.getBody().get("id").toString(),
                esbRestResponse.getBody().get("id").toString());
        Assert.assertEquals(apiRestResponse.getBody().get("message").toString(),
                esbRestResponse.getBody().get("message").toString());
        Assert.assertEquals(apiRestResponse.getBody().get("created_time").toString(),
                esbRestResponse.getBody().get("created_time").toString());
    }

    /**
     * Positive test case for getPost with optional parameters
     */

    @Test(priority = 1,
          groups = { "wso2.esb" },
          dependsOnMethods = { "testGetPostMandatoryParameters" },
          description = "facebook {getPost} integration test with optional parameters.")
    public void testGetPostOptionalParameters() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:getPost");
        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getPost_optional.txt");
        String apiEndPoint =
                connectorProperties.getProperty("apiUrl") + connectorProperties.getProperty("postId")
                        + "?fields=id&access_token=" + connectorProperties.getProperty("accessToken");
        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(apiRestResponse.getBody().get("id").toString(), esbRestResponse.getBody().get("id")
                .toString());
    }

    /**
     * Negative test case for getPost with optional parameters
     */

    @Test(priority = 1,
          groups = { "wso2.esb" },
          description = "facebook {getPost} integration test negative case.")
    public void testGetPostNegativeCase() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:getPost");
        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getPost_negative.txt");
        String apiEndPoint =
                "https://graph.facebook.com/invalid12342?fields=id&access_token="
                        + connectorProperties.getProperty("accessToken");
        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("error").get("message").toString(),
                apiRestResponse.getBody().getJSONObject("error").get("message").toString());
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("error").get("code").toString(),
                apiRestResponse.getBody().getJSONObject("error").get("code").toString());
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("error").get("type").toString(),
                apiRestResponse.getBody().getJSONObject("error").get("type").toString());
    }

    /**
     * Negative test case for deletePost
     */

    @Test(priority = 1,
          groups = { "wso2.esb" },
          dependsOnMethods = { "testGetPostOptionalParameters" },
          description = "facebook {deletePost} integration test negative case.")
    public void testDeletePostNegativeCase() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:deletePost");
        String apiEndPoint =
                connectorProperties.getProperty("apiUrl") + connectorProperties.getProperty("postId")
                        + "?access_token=" + connectorProperties.getProperty("accessToken");
        RestResponse< JSONObject > firstApiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_deletePost_negative.txt");
        RestResponse< JSONObject > secondApiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertTrue(firstApiRestResponse.getBody().has("id") && secondApiRestResponse.getBody().has("id"));
    }

    /**
     * Positive test case for deletePost with mandatory parameters
     */

    @Test(priority = 1,
          groups = { "wso2.esb" },
          dependsOnMethods = { "testDeletePostNegativeCase" },
          description = "facebook {deletePost} integration test with mandatory parameters.")
    public void testDeletePostMandatoryParameters() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:deletePost");
        String apiEndPoint =
                connectorProperties.getProperty("apiUrl") + connectorProperties.getProperty("postId")
                        + "?access_token=" + connectorProperties.getProperty("accessToken");
        RestResponse< JSONObject > firstApiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_deletePost_mandatory.txt");
        RestResponse< JSONObject > secondApiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertTrue(firstApiRestResponse.getBody().has("id") && secondApiRestResponse.getBody().has("error"));
    }

    /**
     * Positive test case for createPhotoTag with mandatory parameters
     */
    @Test(priority = 1,
          groups = { "wso2.esb" },
          dependsOnMethods = { "testUploadPhotoMandatoryParameters" },
          description = "facebook {createPhotoTag} integration test with mandatory parameters.")
    public void testCreatePhotoTagMandatoryParameters() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:createPhotoTag");
        String apiEndPoint =
                connectorProperties.getProperty("apiUrl") + connectorProperties.getProperty("photoId")
                        + "/tags?access_token=" + connectorProperties.getProperty("accessToken");
        sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_createPhotoTag_mandatory.txt");
        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(apiRestResponse.getBody().getJSONArray("data").getJSONObject(0).get("name").toString(),
                connectorProperties.getProperty("TagFriendName"));
    }

    /**
     * Negative test case for createPhotoTag
     */
    @Test(priority = 1,
          groups = { "wso2.esb" },
          dependsOnMethods = { "testCreatePhotoTagMandatoryParameters" },
          description = "facebook {createPhotoTag} integration test negative case.")
    public void testCreatePhotoTagNegativeCase() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:createPhotoTag");
        String apiEndPoint =
                connectorProperties.getProperty("apiUrl") + connectorProperties.getProperty("photoId") + "/tags";
        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_createPhotoTag_negative.txt");
        RestResponse< JSONObject > apiRestResponse =
                sendJsonRestRequest(apiEndPoint, "POST", apiRequestHeadersMap, "api_createPhotoTag_negative.txt");
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("error").get("message").toString(),
                apiRestResponse.getBody().getJSONObject("error").get("message").toString());
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("error").get("code").toString(),
                apiRestResponse.getBody().getJSONObject("error").get("code").toString());
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("error").get("type").toString(),
                apiRestResponse.getBody().getJSONObject("error").get("type").toString());
    }

    /**
     * Positive test case for getGroupDetails method with mandatory parameters.
     *
     * @throws IOException, JSONException
     */
    @Test(priority = 1,
          groups = { "wso2.esb" },
          description = "facebook {getGroupDetails} integration test with mandatory parameters.")
    public void testGetGroupDetailsWithMandatoryParameters() throws IOException, JSONException {

        // calling ESB to get group ID
        esbRequestHeadersMap.put("Action", "urn:getGroupDetails");
        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getGroupDetails_mandatory.txt");
        // calling API to get group ID
        String apiEndPoint =
                connectorProperties.getProperty("apiUrl") + connectorProperties.getProperty("userGroupId")
                        + "?access_token=" + connectorProperties.getProperty("accessToken");
        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getBody().get("id").toString(), apiRestResponse.getBody().get("id")
                .toString());
        Assert.assertEquals(esbRestResponse.getBody().get("name").toString(), apiRestResponse.getBody().get("name")
                .toString());
    }

    /**
     * Positive test case for getGroupDetails method with optional parameters.
     *
     * @throws IOException, JSONException
     */
    @Test(priority = 1,
          groups = { "wso2.esb" },
          description = "facebook {getGroupDetails} integration test with optional parameters.")
    public void testGetGroupDetailsWithOptionalParameters() throws IOException, JSONException {

        // calling ESB to get group ID
        esbRequestHeadersMap.put("Action", "urn:getGroupDetails");

        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getGroupDetails_optional.txt");
        // calling API to get group ID
        String apiEndPoint =
                connectorProperties.getProperty("apiUrl") + connectorProperties.getProperty("userGroupId")
                        + "?fields=id&access_token=" + connectorProperties.getProperty("accessToken");
        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getBody().get("id").toString(), apiRestResponse.getBody().get("id")
                .toString());
    }

    /**
     * Negative test case for getGroupDetails method with mandatory parameters.
     *
     * @throws IOException, JSONException
     */
    @Test(priority = 1,
          groups = { "wso2.esb" },
          description = "facebook {getGroupDetails} integration test with negative parameters.")
    public void testGetGroupDetailsWithNegativeParameters() throws IOException, JSONException {

        // calling ESB to get group ID
        esbRequestHeadersMap.put("Action", "urn:getGroupDetails");

        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getGroupDetails_negative.txt");

        String apiEndPoint =
                "https://graph.facebook.com/negative" + "?access_token="
                        + connectorProperties.getProperty("accessToken");
        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("error").get("message").toString(),
                apiRestResponse.getBody().getJSONObject("error").get("message").toString());
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("error").get("code").toString(),
                apiRestResponse.getBody().getJSONObject("error").get("code").toString());
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("error").get("type").toString(),
                apiRestResponse.getBody().getJSONObject("error").get("type").toString());
    }

    /**
     * Positive test case for getStatus method with mandatory parameters.
     *
     * @throws IOException, JSONException
     */
    @Test(priority = 1,
          groups = { "wso2.esb" },
          dependsOnMethods = { "testCreatePostStatusMandatoryParameters" },
          description = "facebook {getStatus} integration test with mandatory parameters.")
    public void testGetStatusWithMandatoryParameters() throws IOException, JSONException {

        // calling ESB to get status
        esbRequestHeadersMap.put("Action", "urn:getStatus");

        RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap,
                "esb_getStatus_mandatory.txt");
        // calling API to get status
        String apiEndPoint =
                connectorProperties.getProperty("apiUrl") + connectorProperties.getProperty("statusMessageId")
                        + "?access_token=" + connectorProperties.getProperty("accessToken");
        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getBody().get("id").toString(),
                apiRestResponse.getBody().get("id").toString());
        Assert.assertEquals(esbRestResponse.getBody().get("message").toString(),
                apiRestResponse.getBody().get("message").toString());
    }

    /**
     * Positive test case for getStatus method with optional parameters.
     *
     * @throws IOException, JSONException
     */
    @Test(priority = 1,
          groups = { "wso2.esb" },
          dependsOnMethods = { "testCreatePostStatusMandatoryParameters" },
          description = "facebook {getStatus} integration test with optional parameters.")
    public void testGetStatusWithOptionalParameters() throws IOException, JSONException {

        // calling ESB to get status
        esbRequestHeadersMap.put("Action", "urn:getStatus");
        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getStatus_optional.txt");
        // calling API to get status
        String apiEndPoint =
                connectorProperties.getProperty("apiUrl") + connectorProperties.getProperty("statusMessageId")
                        + "?access_token=" + connectorProperties.getProperty("accessToken") + "&fields=id";
        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getBody().get("id").toString(), apiRestResponse.getBody().get("id")
                .toString());

    }

    /**
     * Negative test case for getStatus method with mandatory parameters.
     *
     * @throws IOException, JSONException
     */
    @Test(priority = 1,
          groups = { "wso2.esb" },
          description = "facebook {getStatus} integration test with negative parameters.")
    public void testGetStatusWithNegativeParameters() throws IOException, JSONException {

        // calling ESB to get status
        esbRequestHeadersMap.put("Action", "urn:getStatus");

        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getStatus_negative.txt");
        // calling API to get status
        String apiEndPoint =
                "https://graph.facebook.com/negative/" + "?access_token="
                        + connectorProperties.getProperty("accessToken");
        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("error").get("message").toString(),
                apiRestResponse.getBody().getJSONObject("error").get("message").toString());
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("error").get("code").toString(),
                apiRestResponse.getBody().getJSONObject("error").get("code").toString());
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("error").get("type").toString(),
                apiRestResponse.getBody().getJSONObject("error").get("type").toString());
    }

    /**
     * Positive test case for getMessage method with mandatory parameters.
     *
     * @throws IOException, JSONException
     */
    @Test(priority = 1,
          groups = { "wso2.esb" },
          description = "facebook {getMessage} integration test with mandatory parameters.")
    public void testGetMessageWithMandatoryParameters() throws IOException, JSONException {

        // calling ESB to get message
        esbRequestHeadersMap.put("Action", "urn:getMessage");

        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getMessage_mandatory.txt");
        // calling API to get Message
        String apiEndPoint =
                connectorProperties.getProperty("apiUrl") + connectorProperties.getProperty("messageId")
                        + "?access_token=" + connectorProperties.getProperty("pageAccessToken");
        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);

        Assert.assertEquals(esbRestResponse.getBody().get("id").toString(),
                apiRestResponse.getBody().get("id").toString());
        Assert.assertEquals(esbRestResponse.getBody().get("created_time").toString(),
                apiRestResponse.getBody().get("created_time").toString());
    }

    /**
     * Positive test case for getMessage method with optional parameters.
     *
     * @throws IOException, JSONException
     */
    @Test(priority = 1,
          groups = { "wso2.esb" },
          description = "facebook {getMessage} integration test with optional parameters.")
    public void testGetMessageWithOptionalParameters() throws IOException, JSONException {

        // calling ESB to get message
        esbRequestHeadersMap.put("Action", "urn:getMessage");

        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getMessage_optional.txt");
        // calling API to get message
        String apiEndPoint =
                connectorProperties.getProperty("apiUrl") + connectorProperties.getProperty("messageId")
                        + "?fields=from&access_token=" + connectorProperties.getProperty("pageAccessToken");
        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getBody().get("from").toString(),
                apiRestResponse.getBody().get("from").toString());
    }

    /**
     * Negative test case for getMessage method with mandatory parameters.
     *
     * @throws IOException, JSONException
     */
    @Test(priority = 1,
          groups = { "wso2.esb" },
          description = "facebook {getMessage} integration test with negative parameters.")
    public void testGetMessageWithNegativeParameters() throws IOException, JSONException {

        // calling ESB to get status
        esbRequestHeadersMap.put("Action", "urn:getMessage");

        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getMessage_negative.txt");
        // calling API to get message
        String apiEndPoint =
                "https://graph.facebook.com/negative/" + "?access_token="
                        + connectorProperties.getProperty("accessToken");
        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("error").get("message").toString(),
                apiRestResponse.getBody().getJSONObject("error").get("message").toString());
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("error").get("code").toString(),
                apiRestResponse.getBody().getJSONObject("error").get("code").toString());
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("error").get("type").toString(),
                apiRestResponse.getBody().getJSONObject("error").get("type").toString());
    }

    /**
     * Positive test case for getPhotoDetails method with mandatory parameters.
     *
     * @throws IOException, JSONException
     */
    @Test(priority = 1,
          groups = { "wso2.esb" },
          dependsOnMethods = { "testUploadPhotoMandatoryParameters" },
          description = "facebook {getPhotoDetails} integration test with mandatory parameters.")
    public void testGetPhotoDetailsWithMandatoryParameters() throws IOException, JSONException {

        // calling ESB to get Photo Details
        esbRequestHeadersMap.put("Action", "urn:getPhotoDetails");

        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getPhotoDetails_mandatory.txt");
        // calling API to get Photo Details
        String apiEndPoint =
                connectorProperties.getProperty("apiUrl") + connectorProperties.getProperty("photoId")
                        + "?access_token=" + connectorProperties.getProperty("accessToken");
        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getBody().get("id").toString(),
                apiRestResponse.getBody().get("id").toString());
        Assert.assertEquals(esbRestResponse.getBody().get("name").toString(),
                apiRestResponse.getBody().get("name").toString());
        Assert.assertEquals(esbRestResponse.getBody().get("created_time").toString(),
                apiRestResponse.getBody().get("created_time").toString());
    }

    /**
     * Positive test case for getPhotoDetails method with optional parameters.
     *
     * @throws IOException, JSONException
     */
    @Test(priority = 1,
          groups = { "wso2.esb" },
          dependsOnMethods = { "testUploadPhotoMandatoryParameters" },
          description = "facebook {getPhotoDetails} integration test with optional parameters.")
    public void testGetPhotoDetailsWithOptionalParameters() throws IOException, JSONException {

        // calling ESB to get photo details
        esbRequestHeadersMap.put("Action", "urn:getPhotoDetails");

        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getPhotoDetails_optional.txt");
        // calling API to get photo details
        String apiEndPoint =
                connectorProperties.getProperty("apiUrl") + connectorProperties.getProperty("photoId")
                        + "?fields=id&access_token=" + connectorProperties.getProperty("accessToken");
        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getBody().get("id").toString(), apiRestResponse.getBody().get("id")
                .toString());
    }

    /**
     * Negative test case for getPhotoDetails method with mandatory parameters.
     *
     * @throws IOException, JSONException
     */
    @Test(priority = 1,
          groups = { "wso2.esb" },
          dependsOnMethods = { "testUploadPhotoMandatoryParameters" },
          description = "facebook {getPhotoDetails} integration test with negative parameters.")
    public void testGetPhotoDetailsWithNegativeParameters() throws IOException, JSONException {

        // calling ESB to get photo details
        esbRequestHeadersMap.put("Action", "urn:getPhotoDetails");

        RestResponse< JSONObject > esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "esb_getPhotoDetails_negative.txt");
        // calling API to get photo details
        String apiEndPoint = "https://graph.facebook.com/negative" + "?access_token=" + connectorProperties
                .getProperty("accessToken");
        RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("error").get("message").toString(),
                apiRestResponse.getBody().getJSONObject("error").get("message").toString());
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("error").get("code").toString(),
                apiRestResponse.getBody().getJSONObject("error").get("code").toString());
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("error").get("type").toString(),
                apiRestResponse.getBody().getJSONObject("error").get("type").toString());
    }

    /**
     * Positive test case for postPhotoToAlbum
     */
    @Test(priority = 1,
          groups = { "wso2.esb" },
          dependsOnMethods = { "testCreateAlbumMandatoryParameters" },
          description = "facebook {postPhotoToAlbum} integration test mandatory parameters.")
    public void testUploadPhotoMandatoryParameters() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:postPhotoToAlbum");

        headersMap.put("Action", "urn:postPhotoToAlbum");

        MultipartFormdataProcessor multipartProcessor =
                new MultipartFormdataProcessor(multipartProxyUrl + "?apiUrl=" + connectorProperties.getProperty("apiUrl")
                        + "&apiVersion=" + connectorProperties.getProperty("apiVersion")+"&album_id="
                        + connectorProperties.getProperty("albumId"), headersMap);

        multipartProcessor.addFormDataToRequest("message", "via new ESb");
        multipartProcessor.addFormDataToRequest("access_token", connectorProperties.getProperty("accessToken"));
        multipartProcessor.addFileToRequest("source", connectorProperties.getProperty("imageName"));

        RestResponse< JSONObject > esbRestResponse = multipartProcessor.processForJsonResponse();

        String photoId = esbRestResponse.getBody().getString("id");
        connectorProperties.put("photoId", photoId);
        String apiEndPoint =
                connectorProperties.getProperty("apiUrl") + photoId + "?access_token="
                        + connectorProperties.getProperty("accessToken");
        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);

        Assert.assertTrue(apiRestResponse.getBody().has("id"));

    }

    /**
     * Positive test case for addPhotoToPage
     */
    @Test(priority = 1,
          groups = { "wso2.esb" },
          description = "facebook {addPhotoToPage} integration test negative case.")
    public void testAddPhotoToPageMandatoryParameters() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:addPhotoToPage");

        headersMap.put("Action", "urn:addPhotoToPage");
        MultipartFormdataProcessor multipartProcessor = new MultipartFormdataProcessor(
                multipartProxyUrl + "?apiUrl=" + connectorProperties.getProperty("apiUrl") + "&apiVersion="
                        + connectorProperties.getProperty("apiVersion") + "&pageId=" + connectorProperties
                        .getProperty("pageId"), headersMap);

        multipartProcessor.addFormDataToRequest("message", "via new ESb");
        multipartProcessor.addFormDataToRequest("access_token", connectorProperties.getProperty("accessToken"));
        multipartProcessor.addFileToRequest("source", connectorProperties.getProperty("imageName"));

        RestResponse< JSONObject > esbRestResponse = multipartProcessor.processForJsonResponse();
        String photoId = esbRestResponse.getBody().getString("id");
        connectorProperties.put("photoId", photoId);
        String apiEndPoint =
                connectorProperties.getProperty("apiUrl") + photoId + "?access_token="
                        + connectorProperties.getProperty("accessToken");
        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertTrue(apiRestResponse.getBody().has("id"));

    }

    /**
     * Positive test case for updatePagePicture
     *
     * @throws InterruptedException
     */
    @Test(priority = 1,
          groups = { "wso2.esb" },
          description = "facebook {updatePagePicture} integration test negative case.")
    public void testUpdatePagePictureMandatoryParameters() throws IOException, JSONException, InterruptedException {

        esbRequestHeadersMap.put("Action", "urn:updatePagePicture");

        headersMap.put("Action", "urn:updatePagePicture");
        Thread.sleep(timeOut);

        MultipartFormdataProcessor multipartProcessor = new MultipartFormdataProcessor(
                multipartProxyUrl + "?apiUrl=" + connectorProperties.getProperty("apiUrl") + "&apiVersion="
                        + connectorProperties.getProperty("apiVersion") + "&pageId=" + connectorProperties
                        .getProperty("pageId"), headersMap);

        multipartProcessor.addFormDataToRequest("message", "via new ESb");
        multipartProcessor.addFormDataToRequest("access_token", connectorProperties.getProperty("pageAccessToken"));
        multipartProcessor.addFileToRequest("source", connectorProperties.getProperty("imageName"));

        RestResponse< JSONObject > esbRestResponse = multipartProcessor.processForJsonResponse();
        Assert.assertTrue(esbRestResponse.getBody().toString().contains("true"));

    }

    /**
     * Positive test case for addPageVideo
     */
    @Test(priority = 1,
          groups = { "wso2.esb" },
          description = "facebook {addPageVideo} integration test negative case.")
    public void testAddPageVideoMandatoryParameters() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:addPageVideo");

        headersMap.put("Action", "urn:addPageVideo");
        MultipartFormdataProcessor multipartProcessor = new MultipartFormdataProcessor(
                multipartProxyUrl + "?apiUrl=" + connectorProperties.getProperty("apiUrl") + "&apiVersion="
                        + connectorProperties.getProperty("apiVersion") + "&pageId=" + connectorProperties
                        .getProperty("pageId"), headersMap);

        multipartProcessor.addFormDataToRequest("description", "via new ESb");
        multipartProcessor.addFormDataToRequest("access_token", connectorProperties.getProperty("pageAccessToken"));
        multipartProcessor.addFileToRequest("source", "env.3gp");

        RestResponse< JSONObject > esbRestResponse = multipartProcessor.processForJsonResponse();

        Assert.assertTrue(esbRestResponse.getBody().has("id"));

    }

    /**
     * Revert Facebook Changes.
     *
     * @throws JSONException
     * @throws IOException
     */
    @AfterClass(alwaysRun = true)
    public void revertFacebookChanges() throws IOException, JSONException {

        // Remove user status messages after running all the methods to avoid application banning
        String apiEndPoint =
                connectorProperties.getProperty("apiUrl") + connectorProperties.getProperty("statusMessageId")
                        + "?access_token=" + connectorProperties.getProperty("accessToken");
        sendJsonRestRequest(apiEndPoint, "DELETE", apiRequestHeadersMap);

        // Delete Friend List after running all methods to avoid duplicate Friend list creation.

        apiEndPoint =
                connectorProperties.getProperty("apiUrl") + connectorProperties.getProperty("friendlistId")
                        + "?access_token=" + connectorProperties.getProperty("accessToken");
        RestResponse< JSONObject > apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);

        if (apiRestResponse.getBody().has("id")) {
            apiEndPoint =
                    connectorProperties.getProperty("apiUrl") + connectorProperties.getProperty("friendlistId")
                            + "?access_token=" + connectorProperties.getProperty("accessToken");
            apiRestResponse = sendJsonRestRequest(apiEndPoint, "DELETE", apiRequestHeadersMap);

        }

    }

}
