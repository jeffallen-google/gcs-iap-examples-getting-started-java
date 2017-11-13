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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Metadata {

    static String projectId = null;
    static Long projectNumber = 0L;
    static String serviceAccount = null;

    static {
        try {
            projectId = Metadata.getProject("project-id");
            projectNumber = Long.parseLong(Metadata.getProject("numeric-project-id"));
            serviceAccount = "";
        }
        catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private static String get(String url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection)(new URL(url)).openConnection();
        conn.setRequestProperty("Metadata-Flavor", "Google");
        StringBuffer content = new StringBuffer();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
        }
        conn.disconnect();
        return content.toString();
    }

    static String getProject(String path) throws IOException {
        return get("http://metadata.google.internal/computeMetadata/v1/project" + (path.startsWith("/") ? "" : "/") + path);
    }

    static String getInstance(String path) throws IOException {
        return get("http://metadata.google.internal/computeMetadata/v1/instance" + (path.startsWith("/") ? "" : "/") + path);
    }

}
