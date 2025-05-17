package com.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class Crawler {

    public static Map<String, Integer> index_links = new LinkedHashMap<>();
    public static HashSet<String> visited = new HashSet<String>();
    public static DirectedGraph adj_matrix;
    public static Map<String, Integer> link_count = new LinkedHashMap<>();
    public static final int MAX_DEPTH = 3;

//==========================================================================

    /*
     * Disable the SSL Verification in order to crawl into websites with invalid SSL certificates
     * WARNING: Only good for testing, NOT for production
     */
    public static void disableSSLVerification() {
        try {
            TrustManager[] trustAllCertificates = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }
            };

            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCertificates, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//==========================================================================

    /*
     * Extract all webpage links starting from the base URL
     * @param url: base URL
     * @param level: search level -> same as crawl depth
     */

    public static void extractWebpageLinks(String url, int level) {
        if (level > MAX_DEPTH) return;  
    
        if (!index_links.containsKey(url)) {
            index_links.put(url, index_links.size()); 
        }
    
        try {
            Document doc = Jsoup.connect(url).get();
            Elements links = doc.select("a[href]");
    
            for (Element link : links) {
                String absUrl = link.attr("abs:href");
    
                if (!index_links.containsKey(absUrl)) {
                    index_links.put(absUrl, index_links.size());
                }
                extractWebpageLinks(absUrl, level + 1);
            }
        } catch (IOException e) {
            System.err.println("Error fetching URL: " + url);
            e.printStackTrace();
        }
    }
    
//==========================================================================

    /*
     * Perform the crawl and populate the adjacency matrix
     * @param url: base URL
     * @param level: search level -> same as crawl depth
     */

    public static void crawl(String url, int level) {
        if (level > MAX_DEPTH) return;  
        System.out.println("\n>>Crawling: " + url);
    
        try {
            Document doc = Jsoup.connect(url).get();
            Elements links = doc.select("a[href]");
    
            for (Element link : links) {
                String absUrl = link.attr("abs:href");
    
                if (!visited.contains(absUrl)) {
                    visited.add(absUrl);
                    System.out.println("Link from: " + index_links.get(url) + " -> " + index_links.get(absUrl) + "[" + absUrl + "]");
    
                    if (index_links.containsKey(url) && index_links.containsKey(absUrl)) {
                        int sourceIndex = index_links.get(url);
                        int destinationIndex = index_links.get(absUrl);
                        adj_matrix.addEdge(sourceIndex, destinationIndex);
                        System.out.println(sourceIndex + "->" + destinationIndex);
    
                        link_count.put(absUrl, link_count.getOrDefault(absUrl, 0) + 1);
                    }
    
                    // [DEBUG]: show current level and the link being crawled
                    System.out.println("Recursively crawling level " + (level + 1) + ": " + absUrl + "\n");
                    crawl(absUrl, level + 1);
                }
            }
        } catch (IOException e) {
            System.err.println("Error fetching URL: " + url);
            e.printStackTrace();
        }
    }
//==========================================================================

    public static String searchByWidth() {
        int maxContainment = 0;
        String mostContainedLink = "";
        int[][] matrix = adj_matrix.getAdjMatrix();

        // BFS traversal: Queue to explore each level
        Queue<Integer> queue = new LinkedList<>();
        boolean[] visited = new boolean[index_links.size()];

        // Start BFS from each link
        for (int i = 0; i < index_links.size(); i++) {
            if (!visited[i]) {
                visited[i] = true;
                queue.add(i);
                int containmentCount = 0;

                // Explore links at this level
                while (!queue.isEmpty()) {
                    int currentIndex = queue.poll();
                    for (int j = 0; j < index_links.size(); j++) {
                        if (matrix[currentIndex][j] == 1 && !visited[j]) {
                            visited[j] = true;
                            queue.add(j);
                            containmentCount++;
                        }
                    }
                }

                // Update if this link has the most contaiment by other links(most links directing to this link)
                if (containmentCount > maxContainment) {
                    maxContainment = containmentCount;
                    final int current_index = i;
                    mostContainedLink = index_links.entrySet().stream()
                            .filter(entry -> entry.getValue() == current_index)
                            .map(Map.Entry::getKey)
                            .findFirst()
                            .orElse("");
                }
            }
        }

        return mostContainedLink;
    }

//==========================================================================

    public static String searchByLength() {
        int maxContainment = 0;
        String mostContainedLink = "";
        int[][] matrix = adj_matrix.getAdjMatrix();

        for (int i = 0; i < index_links.size(); i++) {
            int containmentCount = 0;
            for (int j = 0; j < index_links.size(); j++) {
                containmentCount += matrix[i][j];
            }
            if (containmentCount > maxContainment) {
                maxContainment = containmentCount;
                final int current_index = i;
                mostContainedLink = index_links.entrySet().stream()
                        .filter(entry -> entry.getValue() == current_index)
                        .map(Map.Entry::getKey)
                        .findFirst()
                        .orElse("");
            }
        }
        return mostContainedLink;
    }
//==========================================================================

    public static void main(String[] args) {
        String url = "https://example.com";
    
        disableSSLVerification();
    
        extractWebpageLinks(url, 1);
    
        System.out.println(">> Index - Link Reference List:\n");
        for (Map.Entry<String, Integer> entry : index_links.entrySet()) {
            System.out.println("[" + entry.getValue() + "]: " + entry.getKey());
        }
    
        int matrixSize = index_links.size();
        adj_matrix = new DirectedGraph(matrixSize);
    
        System.out.println("\n>> Detailed Crawl Information\n");
        crawl(url, 0);
    
        System.out.println("\n>> Adjacency Matrix:\n");
        //adj_matrix.printGraph();
    
       
        String mostContainedLinkByLength = searchByLength();
        System.out.println("\n>> Most Contained Link by Length: " + mostContainedLinkByLength);

        String mostContainedLinkByWidth = searchByWidth();
        System.out.println("\n>> Most Contained Link by Width: " + mostContainedLinkByWidth);
    
        // Find and print the most contained link by link count
        String mostContainedLink = null;
        int maxCount = 0;
        for (Map.Entry<String, Integer> entry : link_count.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                mostContainedLink = entry.getKey();
            }
        }
    
        System.out.println("\n>>Most Contained Link by redirection Count: " + mostContainedLink + " with " + maxCount + " occurrences.");
       
    }
}     
//=============================================================================

class DirectedGraph {

    private int adj_matrix[][];
    private int n_vertices;

    public DirectedGraph(int n_vertices) {
        this.n_vertices = n_vertices;
        adj_matrix = new int[n_vertices][n_vertices];  
    }

    public void addEdge(int source, int destination) {
        adj_matrix[source][destination] = 1;  
    }
    public int[][] getAdjMatrix() {
        return adj_matrix;
    }
    
    public void printGraph() {
        for (int i = 0; i < n_vertices; i++) {
            for (int j = 0; j < n_vertices; j++) {
                System.out.print(adj_matrix[i][j] + " ");  
            }
            System.out.println();
        }
    }
}
