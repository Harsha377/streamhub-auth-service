package com.streamhub.auth.device;

import lombok.Builder;

/**
 * Represents the client's device information extracted
 * from the HTTP request.
 * <p>
 * This DTO is populated by DeviceInfoService after parsing
 * the User-Agent header and client IP address.
 * <p>
 * Why do we use a DTO?
 * --------------------
 * AuthenticationService should only perform authentication.
 * Device detection is delegated to DeviceInfoService.
 * <p>
 * This follows the Single Responsibility Principle (SRP)
 * and keeps authentication logic independent of
 * User-Agent parsing.
 */
@Builder
public record DeviceInfo(

        /*
         * Human-readable device name.
         * <p>
         * Examples:
         * Desktop
         * Samsung Galaxy S24 Ultra
         * iPhone
         * MacBook Pro
         */
        String deviceName,

        /*
         * Browser name.
         * <p>
         * Examples:
         * Chrome
         * Firefox
         * Edge
         * Safari
         */
        String browser,

        /*
         * Operating System.
         * <p>
         * Examples:
         * Windows 11
         * Android 15
         * macOS
         * iOS 18
         */
        String operatingSystem,

        /*
         * Client IP Address.
         * <p>
         * Examples:
         * 192.168.1.20
         * 103.xxx.xxx.xxx
         */
        String ipAddress

) {
}