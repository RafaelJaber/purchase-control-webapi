package br.psi.giganet.api.purchase.config.security.provider;

import br.psi.giganet.api.purchase.config.security.adapter.SystemUserAdapter;
import br.psi.giganet.api.purchase.config.security.model.User;
import br.psi.giganet.api.purchase.config.security.service.AbstractUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.Optional;

@Component
public class RemoteAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private AbstractUserService userService;

    @Override
    @Transactional
    public Authentication authenticate(Authentication authentication)
            throws AuthenticationException {

        String name = authentication.getName();
        String password = authentication.getCredentials().toString();

        Optional<User> user = userService.remoteAuthenticateHandler(name, password);
        if (user.isPresent()) {
            UserDetails details = SystemUserAdapter.create(user.get());
            return new UsernamePasswordAuthenticationToken(
                    details,
                    details.getPassword(),
                    details.getAuthorities());
        } else {
            throw new BadCredentialsException("Usuário e/ou senha inválido");
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}