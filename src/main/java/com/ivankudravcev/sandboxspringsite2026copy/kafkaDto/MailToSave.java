package com.ivankudravcev.sandboxspringsite2026copy.kafkaDto;

import com.ivankudravcev.sandboxspringsite2026copy.domain.MailType;
import com.ivankudravcev.sandboxspringsite2026copy.entity.User;

import java.util.Properties;

public record MailToSave(User user, MailType type, Properties params) {}
