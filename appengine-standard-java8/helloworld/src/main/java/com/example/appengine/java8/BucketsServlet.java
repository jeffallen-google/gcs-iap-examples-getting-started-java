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

import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "Buckets", value = "/buckets")
public class BucketsServlet extends HttpServlet {

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

            Storage storage = StorageOptions.getDefaultInstance().getService();

            try (PrintWriter w = resp.getWriter()) {
                for (Bucket currentBucket : storage.list().iterateAll()) {
                    w.println(currentBucket.getName());
                }
            }

        }
        catch (Exception ex) {
            // should log this
            resp.sendError(500);
            return;
        }
    }
}
