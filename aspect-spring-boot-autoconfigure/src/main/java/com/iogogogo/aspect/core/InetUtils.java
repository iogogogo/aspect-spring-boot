package com.iogogogo.aspect.core;

import com.iogogogo.aspect.properties.InetUtilsProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.Closeable;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * spring-cloud-commons
 * <p>
 * InetUtils
 * <p>
 * Created by tao.zeng on 2021/6/22.
 */
public class InetUtils implements Closeable {

    // TODO: maybe shutdown the thread pool if it isn't being used?
    private final ExecutorService executorService;

    private final InetUtilsProperties properties;

    private final Log log = LogFactory.getLog(InetUtils.class);

    public InetUtils(final InetUtilsProperties properties) {
        this.properties = properties;
        this.executorService = Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r);
            thread.setName(InetUtilsProperties.PREFIX);
            thread.setDaemon(true);
            return thread;
        });
    }

    @Override
    public void close() {
        this.executorService.shutdown();
    }

    public InetUtils.HostInfo findFirstNonLoopbackHostInfo() {
        InetAddress address = findFirstNonLoopbackAddress();
        if (address != null) {
            return convertAddress(address);
        }
        InetUtils.HostInfo hostInfo = new InetUtils.HostInfo();
        hostInfo.setHostname(this.properties.getDefaultHostname());
        hostInfo.setIpAddress(this.properties.getDefaultIpAddress());
        return hostInfo;
    }

    public InetAddress findFirstNonLoopbackAddress() {
        InetAddress result = null;
        try {
            int lowest = Integer.MAX_VALUE;
            for (Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces(); nics.hasMoreElements(); ) {
                NetworkInterface ifc = nics.nextElement();
                if (ifc.isUp()) {
                    this.log.trace("Testing interface: " + ifc.getDisplayName());
                    if (ifc.getIndex() < lowest || result == null) {
                        lowest = ifc.getIndex();
                    } else {
                        continue;
                    }

                    // @formatter:off
                    if (!ignoreInterface(ifc.getDisplayName())) {
                        for (Enumeration<InetAddress> addrs = ifc
                                .getInetAddresses(); addrs.hasMoreElements(); ) {
                            InetAddress address = addrs.nextElement();
                            if (address instanceof Inet4Address
                                    && !address.isLoopbackAddress()
                                    && isPreferredAddress(address)) {
                                this.log.trace("Found non-loopback interface: "
                                        + ifc.getDisplayName());
                                result = address;
                            }
                        }
                    }
                    // @formatter:on
                }
            }
        } catch (IOException ex) {
            this.log.error("Cannot get first non-loopback address", ex);
        }

        if (result != null) {
            return result;
        }

        try {
            return InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            this.log.warn("Unable to retrieve localhost");
        }

        return null;
    }

    // For testing.
    boolean isPreferredAddress(InetAddress address) {

        if (this.properties.isUseOnlySiteLocalInterfaces()) {
            final boolean siteLocalAddress = address.isSiteLocalAddress();
            if (!siteLocalAddress) {
                this.log.trace("Ignoring address: " + address.getHostAddress());
            }
            return siteLocalAddress;
        }
        final List<String> preferredNetworks = this.properties.getPreferredNetworks();
        if (preferredNetworks.isEmpty()) {
            return true;
        }
        for (String regex : preferredNetworks) {
            final String hostAddress = address.getHostAddress();
            if (hostAddress.matches(regex) || hostAddress.startsWith(regex)) {
                return true;
            }
        }
        this.log.trace("Ignoring address: " + address.getHostAddress());
        return false;
    }

    // For testing
    boolean ignoreInterface(String interfaceName) {
        for (String regex : this.properties.getIgnoredInterfaces()) {
            if (interfaceName.matches(regex)) {
                this.log.trace("Ignoring interface: " + interfaceName);
                return true;
            }
        }
        return false;
    }

    public InetUtils.HostInfo convertAddress(final InetAddress address) {
        InetUtils.HostInfo hostInfo = new InetUtils.HostInfo();
        Future<String> result = this.executorService.submit(address::getHostName);

        String hostname;
        try {
            hostname = result.get(this.properties.getTimeoutSeconds(), TimeUnit.SECONDS);
        } catch (Exception e) {
            this.log.info("Cannot determine local hostname");
            hostname = "localhost";
        }
        hostInfo.setHostname(hostname);
        hostInfo.setIpAddress(address.getHostAddress());
        return hostInfo;
    }

    /**
     * Host information pojo.
     */
    public static class HostInfo {

        /**
         * Should override the host info.
         */
        public boolean override;

        private String ipAddress;

        private String hostname;

        public HostInfo(String hostname) {
            this.hostname = hostname;
        }

        public HostInfo() {
        }

        public int getIpAddressAsInt() {
            InetAddress inetAddress = null;
            String host = this.ipAddress;
            if (host == null) {
                host = this.hostname;
            }
            try {
                inetAddress = InetAddress.getByName(host);
            } catch (final UnknownHostException e) {
                throw new IllegalArgumentException(e);
            }
            return ByteBuffer.wrap(inetAddress.getAddress()).getInt();
        }

        public boolean isOverride() {
            return this.override;
        }

        public void setOverride(boolean override) {
            this.override = override;
        }

        public String getIpAddress() {
            return this.ipAddress;
        }

        public void setIpAddress(String ipAddress) {
            this.ipAddress = ipAddress;
        }

        public String getHostname() {
            return this.hostname;
        }

        public void setHostname(String hostname) {
            this.hostname = hostname;
        }
    }


    @Slf4j
    public static class XInetAddress {
        /**
         * 获取IP地址 * <p>
         * 使用Nginx等反向代理软件， 则不能通过request.getRemoteAddr()获取IP地址
         * 如果使用了多级反向代理的话，X-Forwarded-For的值并不止一个，而是一串IP地址，X-Forwarded-For中第一个非unknown的有效IP字符串，则为真实IP地址
         */
        public static String findFirstNonLoopbackHostInfo(HttpServletRequest request) {
            String ip = null;
            try {
                if (request == null) {
                    return "";
                }
                ip = request.getHeader("x-forwarded-for");
                if (validIp(ip)) {
                    ip = request.getHeader("Proxy-Client-IP");
                }
                if (validIp(ip)) {
                    ip = request.getHeader("WL-Proxy-Client-IP");
                }
                if (validIp(ip)) {
                    ip = request.getHeader("HTTP_CLIENT_IP");
                }
                if (validIp(ip)) {
                    ip = request.getHeader("HTTP_X_FORWARDED_FOR");
                }
                if (validIp(ip)) {
                    ip = request.getRemoteAddr();
                    if ("127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip)) {
                        // 根据网卡取本机配置的IP
                        ip = getLocalAddress();
                    }
                }
            } catch (Exception e) {
                log.error("getLocalAddress ERROR", e);
            }

            //使用代理，则获取第一个IP地址
            if (StringUtils.hasLength(ip) && ip.length() > 15 && ip.indexOf(",") > 0) {
                ip = ip.substring(0, ip.indexOf(","));
            }

            return ip;
        }

        private static boolean validIp(String ip) {
            return !StringUtils.hasLength(ip) || "unknown".equalsIgnoreCase(ip);
        }

        /**
         * 获取本机的IP地址
         */
        private static String getLocalAddress() {
            try {
                return InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException e) {
                log.error("InetAddress.getLocalHost()-error", e);
            }
            return "";
        }
    }
}
