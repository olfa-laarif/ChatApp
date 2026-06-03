package olfa.laarif.chatapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateGroupRequest(

        @NotBlank(message = "Group name is required")
        @Size(max = 100, message = "Group name cannot exceed 100 characters")
        String name,

        // Phone numbers of members to add, in addition to the creator.
        // The creator is added automatically and does not need to be listed.
        @NotEmpty(message = "A group must have at least one other member")
        List<String> memberPhoneNumbers
) {}