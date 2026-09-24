package com.streamhub.auth.device.impl;

import com.streamhub.auth.device.DeviceInfo;
import com.streamhub.auth.device.DeviceInfoService;
import jakarta.servlet.http.HttpServletRequest;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import org.springframework.stereotype.Service;

/**
 * Production implementation of DeviceInfoService.
 *<p>
 * Responsibilities:
 * -----------------
 * - Read the User-Agent header.
 * - Detect the device name.
 * - Detect the browser.
 * - Detect the operating system.
 * - Extract the client's IP address.
 *<p>
 * Why this class?
 * ---------------
 * AuthenticationService should only authenticate users.
 * Parsing User-Agent headers is a separate responsibility.
 *<p>
 * This implementation uses the YAUAA library to accurately
 * identify client devices, browsers and operating systems.
 */
@Service
public class DeviceInfoServiceImpl implements DeviceInfoService {

    /**
     * Thread-safe User-Agent parser.
     *<p>
     * Created once and reused for all requests.
     * Creating a new parser for every request would
     * be expensive.
     */
    private static final UserAgentAnalyzer USER_AGENT_ANALYZER =
            UserAgentAnalyzer
                    .newBuilder()
                    .hideMatcherLoadStats()
                    .withCache(1000)
                    .build();

    @Override
    public DeviceInfo extract(HttpServletRequest request) {

        String userAgentHeader = request.getHeader("User-Agent");

        UserAgent userAgent = USER_AGENT_ANALYZER.parse(userAgentHeader);

        return DeviceInfo.builder()

                .deviceName(getDeviceName(userAgent))
                .browser(getBrowser(userAgent))
                .operatingSystem(getOperatingSystem(userAgent))
                .ipAddress(getClientIpAddress(request))
                .build();

    }

    /**
     * Returns a human-readable device name.
     *<p>
     * Examples:
     * Desktop
     * Samsung Galaxy S24 Ultra
     * iPhone
     * MacBook Pro
     */
    private String getDeviceName(UserAgent userAgent) {

        return userAgent.getValue("DeviceName");

    }

    /**
     * Returns browser name.
     * <p>
     * Examples:
     * Chrome
     * Firefox
     * Safari
     * Edge
     */
    private String getBrowser(UserAgent userAgent) {

        return userAgent.getValue("AgentName");

    }

    /**
     * Returns operating system.
     * <p>
     * Examples:
     * Windows 11
     * Android 15
     * macOS
     * iOS
     */
    private String getOperatingSystem(UserAgent userAgent) {

        return userAgent.getValue("OperatingSystemNameVersion");

    }

    /**
     * Extracts the real client IP address.
     *<p>
     * Production environments often place the application
     * behind reverse proxies or load balancers.
     *<p>
     * In such cases the original client IP is forwarded
     * using the X-Forwarded-For header.
     *<p>
     * If the header is not present, the servlet container's
     * remote address is used.
     */
    private String getClientIpAddress(HttpServletRequest request) {

        String forwardedFor = request.getHeader("X-Forwarded-For");

        if (forwardedFor != null && !forwardedFor.isBlank()) {

            return forwardedFor
                    .split(",")[0]
                    .trim();

        }

        return request.getRemoteAddr();

    }

}

/**
 * Internal Flow
 * React

 * ↓

 * POST /verify-otp

 * ↓

 * Chrome

 * ↓

 * Automatically Adds

 * User-Agent Header

 * ↓

 * Tomcat

 * ↓

 * HttpServletRequest

 * ↓

 * OtpController

 * ↓

 * OtpService

 * ↓

 * AuthenticationService

 * ↓

 * DeviceInfoService

 * ↓

 * request.getHeader("User-Agent")

 * ↓

 * YAUAA

 * ↓

 * DeviceInfo

 * ↓

 * UserSession

 * ↓

 * Database
 */