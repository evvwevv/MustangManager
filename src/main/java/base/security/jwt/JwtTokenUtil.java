package base.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mobile.device.Device;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtTokenUtil implements Serializable {

    static final String CLAIM_KEY_USERNAME = "sub";
    static final String CLAIM_KEY_AUDIENCE = "audience";
    static final String CLAIM_KEY_CREATED = "created";
    private static final long serialVersionUID = -3301605591108950415L;
    private static final String AUDIENCE_UNKNOWN = "unknown";
    private static final String AUDIENCE_WEB = "web";
    private static final String AUDIENCE_MOBILE = "mobile";
    private static final String AUDIENCE_TABLET = "tablet";

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    public String getUsernameFromToken(String token) {
        String username;
        final Claims claims = getClaimsFromToken(token);
        if (claims != null) {
            if (claims.getSubject() != null) {
                username = claims.getSubject();
            } else {
                username = null;
            }
        } else {
            username = null;
        }
        return username;
    }

    public Date getCreatedDateFromToken(String token) {
        Date created;
        final Claims claims = getClaimsFromToken(token);
        if (claims != null) {
            if (claims.get(CLAIM_KEY_CREATED) != null) {
                created = new Date((Long) claims.get(CLAIM_KEY_CREATED));
            } else {
                created = null;
            }
        } else {
            created = null;
        }
        return created;
    }

    public Date getExpirationDateFromToken(String token) {
        Date expire;
        final Claims claims = getClaimsFromToken(token);
        if (claims != null) {
            if (claims.getExpiration() != null) {
                expire = claims.getExpiration();
            } else {
                expire = null;
            }
        } else {
            expire = null;
        }
        return expire;
    }

    public String getAudienceFromToken(String token) {
        String audience;
        final Claims claims = getClaimsFromToken(token);
        if (claims != null) {
            if (claims.get(CLAIM_KEY_AUDIENCE) != null) {
                audience = (String) claims.get(CLAIM_KEY_AUDIENCE);
            } else {
                audience = null;
            }
        } else {
            audience = null;
        }
        return audience;
    }

    private Claims getClaimsFromToken(String token) {
        Claims claims;
        try {
            claims = Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            claims = null;
        }
        return claims;
    }

    private Date generateExpirationDate() {
        return new Date(System.currentTimeMillis() + expiration * 1000);
    }

    private Boolean isTokenExpired(String token) {
        final Date expire = getExpirationDateFromToken(token);
        return expire.before(new Date());
    }

    private Boolean isCreatedBeforeLastPasswordReset(Date created, Date lastPasswordReset) {
        return lastPasswordReset != null && created.before(lastPasswordReset);
    }

    private String generateAudience(Device device) {
        String audience = AUDIENCE_UNKNOWN;
        if (device.isNormal()) {
            audience = AUDIENCE_WEB;
        } else if (device.isTablet()) {
            audience = AUDIENCE_TABLET;
        } else if (device.isMobile()) {
            audience = AUDIENCE_MOBILE;
        }
        return audience;
    }

    private Boolean ignoreTokenExpiration(String token) {
        String audience = getAudienceFromToken(token);
        return AUDIENCE_TABLET.equals(audience) || AUDIENCE_MOBILE.equals(audience);
    }

    public String generateToken(UserDetails userDetails, Device device) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_KEY_USERNAME, userDetails.getUsername());
        claims.put(CLAIM_KEY_AUDIENCE, generateAudience(device));
        claims.put(CLAIM_KEY_CREATED, new Date());
        return generateToken(claims);
    }

    String generateToken(Map<String, Object> claims) {
        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(generateExpirationDate())
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    public Boolean canTokenBeRefreshed(String token, Date lastPasswordReset) {
        final Date created = getCreatedDateFromToken(token);
        return !isCreatedBeforeLastPasswordReset(created, lastPasswordReset)
                && (!isTokenExpired(token) || ignoreTokenExpiration(token));
    }

    public String refreshToken(String token) {
        String refreshedToken;
        final Claims claims = getClaimsFromToken(token);
        if (claims != null) {
            claims.put(CLAIM_KEY_CREATED, new Date());
            refreshedToken = generateToken(claims);
        } else {
            refreshedToken = null;
        }

        return refreshedToken;
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        UserDetails user = userDetails;
        final String username = getUsernameFromToken(token);
        return
                username.equals(user.getUsername())
                        && !isTokenExpired(token);
    }
}