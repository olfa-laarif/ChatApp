package olfa.laarif.chatapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SendMessageRequest(

        @NotBlank(message = "Receiver phone number is required")
        String receiverPhoneNumber,

        @NotBlank(message = "Message content cannot be blank")
        @Size(max = 500, message = "Message cannot exceed 500 characters")
        String content
) {}