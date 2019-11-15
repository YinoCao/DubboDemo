import javafx.scene.paint.Stop;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.ServerSocket;
import java.net.Socket;

public class RpcFramework {


    /**
     * 暴露服务
     *
     * @param service 服务实现
     * @param port 端口
     * @throws IOException
     */
    public static void export(final Object service,int port) throws IOException {
        if(service == null)
            throw new IllegalArgumentException("service instance == null");
        if(port <=0 || port > 65535)
            throw new IllegalArgumentException("Invalid port :"+port);
        System.out.println("Export service:"+service.getClass().getName() + "on port:"+port);

        ServerSocket serverSocket = new ServerSocket(port);
        for(;;){
            final Socket socket = serverSocket.accept();
            new Thread(new Runnable() {
                public void run() {
                    try {
                        ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
                        String methodName = input.readUTF();
                        Class<?>[] parameterTypes = (Class<?>[]) input.readObject();
                        Object[] arguments = (Object[])input.readObject();
                        ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());

                        Method method = service.getClass().getMethod(methodName,parameterTypes);
                        Object result = method.invoke(service,arguments);
                        output.writeObject(result);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                    finally {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }).start();
        }
    }


    public static <T> T refer(final Class<T> interfaceClass, final String host, final int port) throws Exception{
        if(interfaceClass == null)
            throw new IllegalArgumentException("interface class  == null");
        if(!interfaceClass.isInterface())
            throw new IllegalArgumentException("the" + interfaceClass.getName() + "must be interface class");
        if(host == null || host.length() == 0)
            throw new IllegalArgumentException("host == null");
        if(port<0 || port >65535)
            throw new IllegalArgumentException("Invalid port:"+port);

        System.out.println("get remote server:"+ interfaceClass.getName()+"from server :"+host+":"+port);

        return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class<?>[]{interfaceClass}, new InvocationHandler() {


            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                Socket socket = new Socket(host,port);
                ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());

                output.writeUTF(method.getName());
                output.writeObject(method.getParameterTypes());
                output.writeObject(args);
                ObjectInputStream input = new ObjectInputStream(socket.getInputStream());

                Object result = input.readObject();
                return result;
            }
        });

    }

}
