package olfa.laarif.chatapp.dto;

import olfa.laarif.chatapp.enums.AttachmentType;

public record AttachmentResponse(
        String id,
        String filename,
        String url,
        String mimeType,
        AttachmentType type,
        int sizeBytes
) {}