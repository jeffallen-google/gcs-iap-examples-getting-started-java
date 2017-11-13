/*
 *
 *  Copyright 2017 Google Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.example.appengine.java8;

import com.google.appengine.api.appidentity.AppIdentityServiceFactory;
import com.google.appengine.api.appidentity.AppIdentityService;
import com.google.cloud.storage.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;
import java.util.Base64;
import java.net.URLEncoder;

@WebServlet(name = "SignedUrlServlet", value = "/sign/*")
public class SignedUrlServlet extends HttpServlet {

    static VerifyIapRequestHeader verify = new VerifyIapRequestHeader();

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        try {

            // verify IAP header - this should be done at the beginning of every request handler
            if (!verify.verifyJwtForAppEngine(req, Metadata.projectNumber, Metadata.projectId)) {
                resp.sendError(403);
                return;
            }

            String storagePath = req.getPathInfo();

            Long validForSeconds = 60L;
            String expiration = ((Long)(Instant.now().getEpochSecond() + validForSeconds)).toString();

            AppIdentityService identity = AppIdentityServiceFactory.getAppIdentityService();
            String serviceAccount = identity.getServiceAccountName();
            String stringToSign = "GET\n"
                    + "\n"
                    + "\n"
                    + expiration + "\n"
                    + storagePath;

            byte[] raw = stringToSign.getBytes("UTF-8");
            byte[] signed = identity.signForApp(raw).getSignature();
            byte[] base64 = Base64.getEncoder().encode(signed);
            String base64String = new String(base64,"UTF-8");
            String urlEncoded = URLEncoder.encode(base64String,"UTF-8");

            String base = "https://storage.googleapis.com";
            String signedUrl = base + storagePath
                    + "?GoogleAccessId=" + serviceAccount
                    + "&Expires=" + expiration
                    + "&Signature=" + urlEncoded;

            try (PrintWriter w = resp.getWriter()) {
                    w.print(signedUrl);
            }

        }
        catch (Exception ex) {
            // should log this
            resp.sendError(500);
            return;
        }
    }
}
