package com.streamhub.auth.device;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Extracts client device information from an HTTP request.
 * <p>
 * Responsibilities:
 * -----------------
 * - Read the User-Agent header.
 * - Detect the browser.
 * - Detect the operating system.
 * - Detect the device name.
 * - Extract the client IP address.
 * <p>
 * Implementations should hide all User-Agent parsing logic
 * from the AuthenticationService.
 */
public interface DeviceInfoService {

    /**
     * Extracts device information for the current request.
     *
     * @param request Current HTTP request.
     * @return Parsed device information.
     */
    DeviceInfo extract(HttpServletRequest request);

}