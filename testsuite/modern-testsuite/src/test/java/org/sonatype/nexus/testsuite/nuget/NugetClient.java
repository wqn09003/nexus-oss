/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.testsuite.nuget;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import javax.annotation.Nullable;

import com.sonatype.nexus.repository.nuget.security.NugetApiKey;

import org.sonatype.nexus.testsuite.repository.FormatClientSupport;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.message.BasicHeader;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A simple NuGet client for ITs.
 */
public class NugetClient
    extends FormatClientSupport
{
  public static final String VS_SEARCH_COUNT_TEMPLATE = "Search()/$count?$filter=IsAbsoluteLatestVersion&searchTerm='%s'&targetFramework='net45'&includePrerelease=%s";

  public static final String VS_SEARCH_FEED_TEMPLATE = "Search()?$filter=IsAbsoluteLatestVersion&$skip=0&$top=30&searchTerm='%s'&targetFramework='net45'&includePrerelease=true";

  public static final String VS_PACKAGE_COUNT = "Packages()/$count";

  public static final String VS_INITIAL_COUNT_QUERY = "Search()/$count?$filter=IsLatestVersion&searchTerm=''&targetFramework='net45'&includePrerelease=false";

  public static final String VS_INITIAL_FEED_QUERY =
      "Search()?$filter=IsLatestVersion&$orderby=DownloadCount%20desc,Id&$skip=0&$top=30&searchTerm=''&targetFramework='net45'&includePrerelease=false";

  private String apiKey;

  public NugetClient(final HttpClient httpClient,
                     final HttpClientContext httpClientContext,
                     final URI repositoryBaseUri,
                     @Nullable final String apiKey)
  {
    super(httpClient, httpClientContext, repositoryBaseUri);
    this.apiKey = apiKey;

    checkArgument(repositoryBaseUri.toString().endsWith("/"));
  }

  public String getRepositoryMetadata() throws Exception {
    return asString(get("$metadata"));
  }

  /**
   * Publishes a file to the nuget repository
   *
   * @return the HTTP status code
   */
  public int publish(final File file) throws Exception {
    checkNotNull(file);
    HttpPut put = new HttpPut(repositoryBaseUri);

    MultipartEntityBuilder reqEntity = MultipartEntityBuilder.create();
    reqEntity.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
    reqEntity.addPart("package", new FileBody(file));

    put.setEntity(reqEntity.build());

    addNugetApiKey(put);
    return status(httpClient.execute(put, httpClientContext));
  }

  public String feedXml(final String query) throws IOException {
    return asString(get(query));
  }

  private void addNugetApiKey(final HttpRequest request) {
    if (apiKey != null) {
      request.setHeader(new BasicHeader(NugetApiKey.NAME, apiKey));
    }
  }

  public int count(final String query) throws IOException {
    final String s = asString(get(query));
    return Integer.parseInt(s);
  }

  public HttpResponse entry(final String packageId, final String version) throws IOException {
    return get(String.format("Packages(Id='%s',Version='%s')", packageId, version));
  }

  /**
   * Get feed xml for given component
   * @param packageId
   * @param version
   * @return feed entry for given component
   */
  public String entryXml(final String packageId, final String version) throws IOException {
    return asString(entry(packageId, version));
  }

  /**
   * Download component binary
   * @param packageId
   * @param version
   * @return the nupkg file
   */
  public HttpResponse packageContent(final String packageId, final String version) throws IOException {
    return get(String.format("%s/%s", packageId, version));
  }

  /**
   * Exercise Search() with no searchTerm
   * @return feed with matching records
   */
  public String vsSearchFeedXml() throws IOException {
    return feedXml(String.format(VS_SEARCH_FEED_TEMPLATE, "", false));
  }

  /**
   * Exercise Search() with no searchTerm and including inlinecount
   * @return feed with matching records and inlinecount
   */
  public String vsSearchFeedXmlInlineCount() throws IOException {
    return feedXml(String.format(VS_SEARCH_FEED_TEMPLATE, "", false) + "&$inlinecount=allpages");
  }

  /**
   * Exercise Search() for given searchTerm
   * @param searchTerm
   * @return feed with matching records
   */
  public String vsSearchFeedXml(final String searchTerm) throws IOException {
    return feedXml(String.format(VS_SEARCH_FEED_TEMPLATE, searchTerm, true));
  }

  /**
   * Exercise a Search/$count query with no search term
   * @return the count of all records
   */
  public int vsCount() throws IOException {
    return count(VS_INITIAL_COUNT_QUERY);
  }

  /**
   * Exercise a Search/$count query with the given search term
   * @param searchTerm
   * @return the count of matching records
   */
  public int vsSearchCount(final String searchTerm) throws IOException {
    return count(String.format(VS_SEARCH_COUNT_TEMPLATE, searchTerm, true));
  }

  /**
   * Exercise a Package/$count query for all packages
   * @return the count of all packages
   */
  public int vsPackageCount() throws IOException {
    return count(VS_PACKAGE_COUNT);
  }

  /**
   * Issues a delete request to the NuGet repository.
   */
  public HttpResponse delete(final String packageId, final String version) throws IOException {
    final URI deleteURI = resolve(String.format("%s/%s", packageId, version));

    final HttpDelete delete = new HttpDelete(deleteURI);
    addNugetApiKey(delete);
    return execute(delete);
  }
}
