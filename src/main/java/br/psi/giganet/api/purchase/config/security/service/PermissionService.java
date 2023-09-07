package br.psi.giganet.api.purchase.config.security.service;

import br.psi.giganet.api.purchase.config.security.model.Permission;
import br.psi.giganet.api.purchase.config.security.repository.PermissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PermissionService {

    @Autowired
    private PermissionRepository permissionRepository;

    public Optional<Permission> findById(String id){
        return this.permissionRepository.findById(id);
    }

    public Optional<Permission> findByName(String name){
        return this.permissionRepository.findByName(name);
    }

}
