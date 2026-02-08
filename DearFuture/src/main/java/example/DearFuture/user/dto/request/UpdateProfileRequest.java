package example.DearFuture.user.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @Size(max = 50, message = "First name must be less than 50 characters")
    private String firstName;

    @Size(max = 50, message = "Last name must be less than 50 characters")
    private String lastName;

}
