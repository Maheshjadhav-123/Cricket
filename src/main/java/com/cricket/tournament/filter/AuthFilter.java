package com.cricket.tournament.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * CO2 — Servlet Filter for session-based authentication.
 *
 * This class implements jakarta.servlet.Filter directly,
 * satisfying the CO2 requirement for Servlet/Filter usage.
 *
 * How it works:
 *  1. Every incoming request passes through this filter FIRST.
 *  2. Public paths (login, register, static files) are allowed through.
 *  3. Protected paths check for a valid session attribute "loggedUser".
 *  4. If no session found → redirect to login page.
 *  5. If role mismatch (USER trying to access /admin/**) → redirect to user page.
 *
 * @WebFilter("/*") → intercepts ALL URLs
 */
@WebFilter("/*")
public class AuthFilter implements Filter {

    // Paths that do NOT require login
    private static final String[] PUBLIC_PATHS = {
        "/login", "/register", "/api/auth/",
        "/index.html", "/login.html", "/register.html",
        "/css/", "/js/", "/images/",
        "/favicon.ico"
    };

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest  request  = (HttpServletRequest)  req;
        HttpServletResponse response = (HttpServletResponse) res;

        String path = request.getRequestURI();

        // ─── 1. Allow public paths through without session check ───────
        for (String pub : PUBLIC_PATHS) {
            if (path.contains(pub)) {
                chain.doFilter(req, res);
                return;
            }
        }

        // ─── 2. Allow all API calls to pass through (handled by controllers) ─
        // The API controllers themselves check session via @SessionAttribute
        if (path.startsWith("/api/")) {
            chain.doFilter(req, res);
            return;
        }

        // ─── 3. Check session for protected HTML pages ─────────────────
        HttpSession session = request.getSession(false);
        Object loggedUser   = (session != null) ? session.getAttribute("loggedUser") : null;

        if (loggedUser == null) {
            // Not logged in → redirect to login
            response.sendRedirect(request.getContextPath() + "/login.html");
            return;
        }

        // ─── 4. Role-based path guard ───────────────────────────────────
        String role = (String) session.getAttribute("userRole");

        // USER trying to access admin pages → redirect to user dashboard
        if (path.contains("/admin.html") && !"ADMIN".equalsIgnoreCase(role)) {
            response.sendRedirect(request.getContextPath() + "/user-dashboard.html");
            return;
        }

        // Feature 5: TEAM_MANAGER role — only ADMIN and TEAM_MANAGER can access
        if (path.contains("/team-manager.html") &&
            !"ADMIN".equalsIgnoreCase(role) &&
            !"TEAM_MANAGER".equalsIgnoreCase(role)) {
            response.sendRedirect(request.getContextPath() + "/user-dashboard.html");
            return;
        }

        // ─── 5. All checks passed → continue ───────────────────────────
        chain.doFilter(req, res);
    }

    @Override public void init(FilterConfig cfg) {}
    @Override public void destroy() {}
}