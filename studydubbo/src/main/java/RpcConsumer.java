import static java.lang.Integer.*;

public class RpcConsumer {

    public static void main(String[] args) throws Exception {
        HelloService helloService = RpcFramework.refer(HelloService.class, "127.0.0.1", 1234);
        for (int i = 0; i < MAX_VALUE; i++) {
            String hello = helloService.hello("yino--"+i);
            System.out.println(hello);
            Thread.sleep(500);
        }
    }
}
