
# Java Web Crawler

A simple Java-based web crawler that recursively explores web pages starting from a base URL, constructs an adjacency matrix representing the hyperlink structure, and analyzes which pages are most referenced by others.

## Features

- Recursively extracts links from web pages up to a maximum depth.
- Bypasses invalid SSL certificates (useful for development).
- Constructs an adjacency matrix to represent the web graph.
- Identifies the most referenced (contained) page using:
  - Outgoing links (`Length`)
  - Incoming links (`Width`)
  - Redirection counts
- Displays a mapping of links and their assigned indices.

## Technologies Used

- Java
- [JSoup](https://jsoup.org/) for HTML parsing
- Standard Java libraries

## File Structure

- `Crawler.java`: Main class containing crawling logic, link indexing, and analysis.
- `DirectedGraph.java`: Class to represent the directed graph using an adjacency matrix.

## How It Works

1. **SSL Verification Disabled**: Temporarily bypasses certificate validation for testing with `disableSSLVerification()`.
2. **Link Extraction**: `extractWebpageLinks()` recursively finds all links from the base URL.
3. **Crawling**: `crawl()` goes through each link, adds it to the graph, and tracks link references.
4. **Graph Construction**: Uses `DirectedGraph` to store the link relationships.
5. **Analysis**: Finds the most referenced page using:
   - Outgoing edges (`searchByLength`)
   - Incoming edges (`searchByWidth`)
   - Link reference frequency

## Example Output
```bash
Index - Link Reference List:
[0]: https://example.com
[1]: https://example.com/about
[2]: https://example.com/contact

Detailed Crawl Information
Crawling: https://example.com
Link from: 0 -> 1 [https://example.com/about]
...

Adjacency Matrix:
0 1 0
0 0 1
...

Most Contained Link by Length: https://example.com/about
Most Contained Link by Width: https://example.com/contact
Most Contained Link by redirection Count: https://example.com/about with 3 occurrences.
```

## How to Run

1. **Prerequisites**:
   - Java 8 or higher
   - JSoup library (add to your classpath)

2. **Compile**:
   ```bash
   $ javac -cp .:jsoup-1.14.3.jar com/example/Crawler.java

3. **Run**:
   ```bash
   $ java -cp .:jsoup-1.14.3.jar com.example.Crawler
   
## Notes

- Max Depth is currently set to 3. You can change it via MAX_DEPTH constant.
- SSL verification is disabled by default for development purposes. Do not use in production.
- The crawler does not handle robots.txt compliance.
