package olfa.laarif.chatapp.service;

import java.util.List;



import olfa.laarif.chatapp.dto.FileLogResponse;
import olfa.laarif.chatapp.dto.MessageLogResponse;
import java.util.List;

public interface LogService {
    List<MessageLogResponse> getAllMessageLogs();
    List<FileLogResponse> getAllFileLogs();
}