
package olfa.laarif.chatapp.controller.request;

import lombok.Data;

@Data
public class UserLoginDto {
    private String phoneNumber;
    private String password;
}
