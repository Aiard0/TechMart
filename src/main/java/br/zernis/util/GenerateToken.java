package br.zernis.util;

import br.zernis.entity.User;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class GenerateToken {

    @ConfigProperty(name = "mp.jwt.verify.issuer")
    private String issuer;

    public String generateToken(User user) {
        return Jwt.issuer(issuer)
            .upn(user.getEmail())
            .subject(user.getId().toString())
            .groups(user.getRole().name())
            .sign();
    }

}
