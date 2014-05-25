package com.kensai.gui.services.connectors;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.protobuf.ProtobufDecoder;
import org.jboss.netty.handler.codec.protobuf.ProtobufEncoder;
import org.jboss.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import org.jboss.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;

import com.kensai.protocol.Trading;

public class GuiChannelPipelineFactory implements ChannelPipelineFactory {

	private GuiChannelHandler handler;

	public GuiChannelPipelineFactory(GuiChannelHandler handler) {
		this.handler = handler;
	}

	@Override
	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline p = Channels.pipeline();
		p.addLast("frameDecoder", new ProtobufVarint32FrameDecoder());
		p.addLast("protobufDecoder", new ProtobufDecoder(Trading.Messages.getDefaultInstance()));
		p.addLast("frameEncoder", new ProtobufVarint32LengthFieldPrepender());
		p.addLast("protobufEncoder", new ProtobufEncoder());
		p.addLast("handler", handler);
		return p;
	}
}
