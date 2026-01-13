package rs.getgo.backend.security.auth;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;
import rs.getgo.backend.utils.TokenUtils;

import java.io.IOException;

public class TokenAuthenticationFilter extends OncePerRequestFilter {

	private final TokenUtils tokenUtils;
	private final UserDetailsService userDetailsService;
	protected final Log LOGGER = LogFactory.getLog(getClass());

	public TokenAuthenticationFilter(TokenUtils tokenUtils, UserDetailsService userDetailsService) {
		this.tokenUtils = tokenUtils;
		this.userDetailsService = userDetailsService;
	}

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain chain
	) throws ServletException, IOException {

		try {
			String token = tokenUtils.getToken(request);

			if (token != null) {
				String email = tokenUtils.getUsernameFromToken(token);

				if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
					UserDetails userDetails = userDetailsService.loadUserByUsername(email);

					if (tokenUtils.validateToken(token, userDetails)) {
						TokenBasedAuthentication authentication =
								new TokenBasedAuthentication(userDetails);
						authentication.setToken(token);
						SecurityContextHolder.getContext().setAuthentication(authentication);
					}
				}
			}
		} catch (ExpiredJwtException ex) {
			LOGGER.debug("JWT expired");
		}

		chain.doFilter(request, response);
	}
}