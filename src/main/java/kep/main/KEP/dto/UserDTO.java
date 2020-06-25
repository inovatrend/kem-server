package kep.main.KEP.dto;

import kep.main.KEP.model.Permissions;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long userId;

    private String username;

    private String password;

    private String repeatPassword;

    private String firstname;

    private String lastname;

    private Set<Permissions> permissions = new HashSet<>();
}
