package kep.main.KEP.model;

import com.sun.istack.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name= "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq")
    @SequenceGenerator(name = "user_seq", allocationSize = 10)
    private Long userId;

    @NotNull
    @Column(name = "username")
    private String username;

    @NotNull
    @Column(name = "password")
    private String password;

    @NotNull
    @Column(name = "repeat_password")
    private String repeatPassword;

    @NotNull
    @Column(name = "firstname")
    private String firstname;

    @NotNull
    @Column(name = "lastname")
    private String lastname;

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", repeatPassword='" + repeatPassword + '\'' +
                ", firstname='" + firstname + '\'' +
                ", lastname='" + lastname + '\'' +
                '}';
    }
}
