package br.psi.giganet.api.purchase.config.security.model;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class SystemUser extends org.springframework.security.core.userdetails.User {

    private static final long serialVersionUID = 1L;

    @Getter
    private final User user;

    public SystemUser(User user, Collection<? extends GrantedAuthority> authorities) {
        super(user.getEmail(), user.getPassword(), authorities);
        this.user = user;
    }
}
