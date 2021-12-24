package server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class Server {
    public static void main(String[] args) {

        HandlerProvider provider = new HandlerProvider(new AuthorizationService());
        EventLoopGroup auth = new NioEventLoopGroup(1);
        EventLoopGroup worker = new NioEventLoopGroup();
        try{
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(auth,worker).
                    channel(NioServerSocketChannel.class).
                    childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel channel) throws Exception {
                            channel.pipeline().addLast(provider.getSerializePipeline());
                        }
                    });
            ChannelFuture future = bootstrap.bind(8189).sync();
            log.debug("Server started...");
            future.channel().closeFuture().sync();
        }catch (Exception e){
            log.error("e=", e);
        } finally {
            auth.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

}
