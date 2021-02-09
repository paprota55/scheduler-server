package pl.rafalpaprota.schedulerserver.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.rafalpaprota.schedulerserver.model.Role;
import pl.rafalpaprota.schedulerserver.repositories.RoleRepository;

@Service
public class RoleService {
    private final RoleRepository roleRepository;

    @Autowired
    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public Long addRole(String name) {
        Role role = new Role();
        role.setName(name);
        role.setId(null);
        return this.roleRepository.save(role).getId();
    }

    public Role getRoleByName(String name) {
        return this.roleRepository.findByName(name);
    }
}
