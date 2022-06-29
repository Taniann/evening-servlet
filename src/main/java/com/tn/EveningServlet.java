package com.tn;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;

@WebServlet("/evening")
public class EveningServlet extends HttpServlet {
    private static final String DEFAULT_NAME = "Buddy";
    private static final String PARAMETER_NAME = "name";
    private static final String SESSION_ID = "SESSION_ID";

    private Map<String, Map<String, Object>> sessionAttributesMap = new HashMap<>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        var writer = resp.getWriter();
        var name = resolveName(req, resp);
        writer.printf("Good evening, %s!%n", name);
    }

    private String resolveName(HttpServletRequest req, HttpServletResponse resp) {
        var cookies = ofNullable(req.getCookies()).map(Arrays::asList)
                                                  .orElse(emptyList());
        var sessionId = cookies.stream()
                               .filter(cookie -> SESSION_ID.equals(cookie.getName()))
                               .findFirst()
                               .map(Cookie::getValue)
                               .orElse(null);

        if (isNull(sessionId)) {
            sessionId = UUID.randomUUID()
                            .toString();
            addSessionAttribute(sessionId, req, resp);
        } else {
            changeNameIfProvided(sessionId, req);
        }
        return (String) sessionAttributesMap.get(sessionId)
                                            .get(PARAMETER_NAME);
    }

    private void addSessionAttribute(String sessionId, HttpServletRequest req, HttpServletResponse resp) {
        resp.addCookie(new Cookie(SESSION_ID, sessionId));
        sessionAttributesMap.put(sessionId, Map.of(PARAMETER_NAME,
                                                   ofNullable(req.getParameter(PARAMETER_NAME)).orElse(DEFAULT_NAME)));
    }

    private void changeNameIfProvided(String sessionId, HttpServletRequest req) {
        String name = req.getParameter(PARAMETER_NAME);
        if (nonNull(name)) {
            sessionAttributesMap.put(sessionId, Map.of(PARAMETER_NAME, name));
        }
    }
}
