package server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;


@Slf4j
public class Server {
    public static void main(String[] args) {
        AuthorizationService authorizationService = new AuthorizationService();
        try{
            authorizationService.start();
        } catch (SQLException e){
            log.error("e=", e);
        }
        HandlerProvider provider = new HandlerProvider(authorizationService);
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
            authorizationService.stop();
            auth.shutdownGracefully();
            worker.shutdownGracefully();

        }
    }

}
