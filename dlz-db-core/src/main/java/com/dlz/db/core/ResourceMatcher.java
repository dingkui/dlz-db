package com.dlz.db.core;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

/**
 * 自研 classpath 资源匹配器。
 * <p>支持 {@code classpath:} / {@code classpath*:} 前缀，以及 {@code *} / {@code **} / {@code ?} 通配符。</p>
 * <p>同时支持开发环境的文件系统资源和打包后的 jar 内资源。</p>
 *
 * <h3>支持的模式</h3>
 * <ul>
 *   <li>{@code classpath:sql/comm.sql} —— 单个资源（仅取第一个匹配）</li>
 *   <li>{@code classpath*:sql/comm.sql} —— 跨 classpath 多源同名资源</li>
 *   <li>{@code classpath*:sql/framework/*.sql} —— 单层通配</li>
 *   <li>{@code classpath*:sql/&#42;&#42;/*.sql} —— 多层通配</li>
 * </ul>
 *
 * @author dingkui
 * @since 7.0.0
 */
@Slf4j
public class ResourceMatcher {

    private static final String CLASSPATH_ALL_PREFIX = "classpath*:";
    private static final String CLASSPATH_PREFIX = "classpath:";

    private ResourceMatcher() {
    }

    /**
     * 根据 location 查找所有匹配的资源 URL。
     *
     * @param location classpath 路径，支持通配符
     * @return 匹配到的 URL 列表
     */
    public static List<URL> getResources(String location) throws IOException {
        if (location == null) {
            return new ArrayList<>();
        }
        boolean classpathAll;
        String path;
        if (location.startsWith(CLASSPATH_ALL_PREFIX)) {
            classpathAll = true;
            path = location.substring(CLASSPATH_ALL_PREFIX.length());
        } else if (location.startsWith(CLASSPATH_PREFIX)) {
            classpathAll = false;
            path = location.substring(CLASSPATH_PREFIX.length());
        } else {
            classpathAll = false;
            path = location;
        }
        // 去掉开头多余斜杠
        while (path.startsWith("/")) {
            path = path.substring(1);
        }

        if (!hasWildcard(path)) {
            return findExactResources(path, classpathAll);
        }
        return findPatternResources(path, classpathAll);
    }

    /**
     * 加载所有匹配资源的 InputStream。
     */
    public static InputStream[] getResourceStreams(String location) throws IOException {
        List<URL> urls = getResources(location);
        List<InputStream> streams = new ArrayList<>(urls.size());
        for (URL url : urls) {
            try {
                streams.add(url.openStream());
            } catch (IOException e) {
                log.warn("打开资源流失败: " + url, e);
            }
        }
        return streams.toArray(new InputStream[0]);
    }

    /**
     * 加载单个资源（取第一个匹配）。
     */
    public static InputStream getResourceStream(String location) throws IOException {
        List<URL> urls = getResources(location);
        if (urls.isEmpty()) {
            return null;
        }
        return urls.get(0).openStream();
    }

    // ============== 内部实现 ==============

    private static boolean hasWildcard(String path) {
        return path.contains("*") || path.contains("?");
    }

    private static List<URL> findExactResources(String path, boolean all) throws IOException {
        List<URL> result = new ArrayList<>();
        ClassLoader cl = currentClassLoader();
        if (all) {
            Enumeration<URL> urls = cl.getResources(path);
            while (urls.hasMoreElements()) {
                result.add(urls.nextElement());
            }
        } else {
            URL url = cl.getResource(path);
            if (url != null) {
                result.add(url);
            }
        }
        return result;
    }

    private static List<URL> findPatternResources(String path, boolean all) throws IOException {
        // 拆分基础路径与模式部分
        int wildcardIdx = firstWildcardIndex(path);
        int slashIdx = path.lastIndexOf('/', wildcardIdx);
        String basePath = slashIdx >= 0 ? path.substring(0, slashIdx + 1) : "";
        String pattern = slashIdx >= 0 ? path.substring(slashIdx + 1) : path;
        Pattern regex = wildcardToRegex(pattern);

        List<URL> result = new ArrayList<>();
        ClassLoader cl = currentClassLoader();
        Enumeration<URL> roots = all ? cl.getResources(basePath) : enumOf(cl.getResource(basePath));
        while (roots.hasMoreElements()) {
            URL rootUrl = roots.nextElement();
            String protocol = rootUrl.getProtocol();
            if ("file".equals(protocol)) {
                collectFromFile(rootUrl, basePath, regex, result);
            } else if ("jar".equals(protocol) || "zip".equals(protocol) || "wsjar".equals(protocol)) {
                collectFromJar(rootUrl, basePath, regex, result);
            } else {
                log.debug("不支持的资源协议: {}, 跳过 url={}", protocol, rootUrl);
            }
        }
        return result;
    }

    private static int firstWildcardIndex(String path) {
        int starIdx = path.indexOf('*');
        int qIdx = path.indexOf('?');
        if (starIdx < 0) return qIdx;
        if (qIdx < 0) return starIdx;
        return Math.min(starIdx, qIdx);
    }

    /**
     * 将通配符模式转为正则。
     **/
    private static Pattern wildcardToRegex(String pattern) {
        StringBuilder sb = new StringBuilder("^");
        int i = 0;
        int n = pattern.length();
        while (i < n) {
            char c = pattern.charAt(i);
            if (c == '*') {
                boolean doubleStar = i + 1 < n && pattern.charAt(i + 1) == '*';
                if (doubleStar) {
                    // **/ -> (?:.*/)?
                    if (i + 2 < n && pattern.charAt(i + 2) == '/') {
                        sb.append("(?:.*/)?");
                        i += 3;
                        continue;
                    }
                    // /** 已在上一轮吃掉的 '/' 之后 —— 不会走到这，但裸 ** 作兜底
                    sb.append(".*");
                    i += 2;
                    continue;
                }
                sb.append("[^/]*");
                i++;
            } else if (c == '/' && i + 2 < n && pattern.charAt(i + 1) == '*' && pattern.charAt(i + 2) == '*') {
                // /** (后面可能还有 /xxx 或直接结束)
                if (i + 3 < n && pattern.charAt(i + 3) == '/') {
                    // /**/ -> (?:/.*)?/  —— 但要让整体等价于 "/可选多层/"
                    sb.append("(?:/.*)?/");
                    i += 4;
                } else {
                    sb.append("(?:/.*)?");
                    i += 3;
                }
            } else if (c == '?') {
                sb.append(".");
                i++;
            } else if ("\\.[](){}+^$|".indexOf(c) >= 0) {
                sb.append('\\').append(c);
                i++;
            } else {
                sb.append(c);
                i++;
            }
        }
        sb.append('$');
        return Pattern.compile(sb.toString());
    }

    private static void collectFromFile(URL rootUrl, String basePath, Pattern regex, List<URL> result) {
        try {
            File rootDir = new File(rootUrl.toURI());
            if (!rootDir.isDirectory()) {
                return;
            }
            walkFile(rootDir, "", basePath, regex, result);
        } catch (Exception e) {
            log.warn("扫描文件资源失败: " + rootUrl, e);
        }
    }

    private static void walkFile(File dir, String relative, String basePath, Pattern regex, List<URL> result) {
        File[] children = dir.listFiles();
        if (children == null) return;
        for (File child : children) {
            String childRel = relative.isEmpty() ? child.getName() : relative + "/" + child.getName();
            if (child.isDirectory()) {
                walkFile(child, childRel, basePath, regex, result);
            } else {
                if (regex.matcher(childRel).matches()) {
                    try {
                        result.add(child.toURI().toURL());
                    } catch (Exception e) {
                        log.warn("转换文件URL失败: " + child, e);
                    }
                }
            }
        }
    }

    private static void collectFromJar(URL rootUrl, String basePath, Pattern regex, List<URL> result) {
        try {
            URLConnection conn = rootUrl.openConnection();
            JarFile jarFile;
            String jarBaseUrl;
            if (conn instanceof JarURLConnection) {
                JarURLConnection jarConn = (JarURLConnection) conn;
                jarConn.setUseCaches(false);
                jarFile = jarConn.getJarFile();
                jarBaseUrl = jarConn.getJarFileURL().toString();
            } else {
                // 回退处理（罕见）
                String urlFile = rootUrl.getFile();
                int sep = urlFile.indexOf("!/");
                if (sep < 0) return;
                String jarPath = urlFile.substring(0, sep);
                if (jarPath.startsWith("file:")) {
                    jarPath = jarPath.substring("file:".length());
                }
                jarFile = new JarFile(jarPath);
                jarBaseUrl = "file:" + jarPath;
            }

            String basePathInJar = basePath; // jar 内的路径不会有前导斜杠
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.isDirectory()) continue;
                String name = entry.getName();
                if (basePathInJar.isEmpty() || name.startsWith(basePathInJar)) {
                    String relative = name.substring(basePathInJar.length());
                    if (regex.matcher(relative).matches()) {
                        URL entryUrl = new URL("jar:" + jarBaseUrl + "!/" + name);
                        result.add(entryUrl);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("扫描 jar 资源失败: " + rootUrl, e);
        }
    }

    private static ClassLoader currentClassLoader() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        return cl != null ? cl : ResourceMatcher.class.getClassLoader();
    }

    private static Enumeration<URL> enumOf(final URL url) {
        return new Enumeration<URL>() {
            boolean served = url == null;
            public boolean hasMoreElements() { return !served; }
            public URL nextElement() { served = true; return url; }
        };
    }
}
