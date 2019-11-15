import java.io.IOException;

public class RpcProvider {
    public static void main(String[] args) throws IOException {
        HelloService helloService = new HelloServiceImpl();
        RpcFramework.export(helloService,1234);
    }

}
