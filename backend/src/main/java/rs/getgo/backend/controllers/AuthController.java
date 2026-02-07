// ...existing code...

// Mobile/browser entry point for activation (email link)
@GetMapping(value = "/activate-mobile", produces = {MediaType.TEXT_HTML_VALUE, MediaType.APPLICATION_JSON_VALUE})
public ResponseEntity<?> activateAccountMobileEntry(
        @RequestParam("token") String token,
        @RequestHeader(value = "Accept", required = false) String accept
) {
    boolean activated = authService.activateAccount(token);

    if (accept != null && accept.contains(MediaType.APPLICATION_JSON_VALUE)) {
        if (activated) {
            return ResponseEntity.ok(Map.of("activated", true, "message", "Account activated"));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("activated", false, "message", "Invalid or expired activation token"));
    }

    String title = activated ? "GetGo - Account activated" : "GetGo - Activation failed";
    String deepLink = "getgo://activate/?token=" + token;
    String webFallback = webBaseUrl + "/login";

    String body = activated
            ? ("<h2>✓ Your account has been activated!</h2>"
               + "<p>Redirecting to app...</p>"
               + "<p><a href='" + deepLink + "' style='display:inline-block;padding:12px 24px;background:#667eea;color:white;text-decoration:none;border-radius:6px;margin:16px 0;'>Open GetGo App</a></p>"
               + "<p style='margin-top:24px;color:#666;'>Or <a href='" + webFallback + "'>continue in browser</a></p>")
            : ("<h2>✗ Activation failed</h2>"
               + "<p>This link is invalid or expired.</p>"
               + "<p><a href='" + webFallback + "'>Return to login</a></p>");

    String html =
            "<!doctype html><html><head><meta charset='utf-8'/>" +
            "<meta name='viewport' content='width=device-width, initial-scale=1'/>" +
            "<title>" + title + "</title>" +
            "<style>body{font-family:Arial,sans-serif;padding:24px;text-align:center;background:linear-gradient(135deg,#667eea 0%,#764ba2 100%);color:white;min-height:100vh;display:flex;flex-direction:column;justify-content:center;}</style>" +
            "</head><body>" +
            "<div style='background:rgba(255,255,255,0.95);color:#333;padding:32px;border-radius:12px;max-width:500px;margin:0 auto;'>" +
            body +
            "</div>" +
            "<script>" +
            "setTimeout(function(){" +
            "  window.location.href='" + deepLink + "';" +
            "}, 500);" +
            "</script>" +
            "</body></html>";

    return ResponseEntity
            .status(activated ? HttpStatus.OK : HttpStatus.BAD_REQUEST)
            .contentType(MediaType.TEXT_HTML)
            .body(html);
}

// ...existing code...
