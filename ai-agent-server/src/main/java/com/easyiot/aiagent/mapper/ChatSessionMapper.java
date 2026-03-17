package com.easyiot.aiagent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.easyiot.aiagent.model.entity.ChatSession;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSession> {
}
