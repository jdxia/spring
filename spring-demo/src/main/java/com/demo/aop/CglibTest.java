package com.demo.aop;


public class CglibTest {

	public static void main(String[] args) {

		// 被代理对象
//		UserService target = new UserService();
//
//		Enhancer enhancer = new Enhancer();
////		enhancer.setSuperclass(UserService.class);
////		enhancer.setInterfaces(new Class[] {UserInterface.class});
//		enhancer.setCallbacks(new Callback[]{new MethodInterceptor() {
//			@Override
//			public Object intercept(Object obj, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
//				System.out.println("before切面逻辑");
//				method.invoke(target, args);
//
//				return null;
//			}
//		}, NoOp.INSTANCE});
//
//		enhancer.setCallbackFilter(new CallbackFilter() {
//			@Override
//			public int accept(Method method) {
//				if (method.getName().equals("test")) {
//					return 0;
//				} else {
//					return 1;
//				}
//			}
//		});
//
//		UserService proxy = (UserService) enhancer.create();
//		proxy.test();


//		UserInterface userInterface = (UserInterface) Proxy.newProxyInstance(CglibTest.class.getClassLoader(), new Class[]{UserInterface.class}, new InvocationHandler() {
//			@Override
//			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
//				System.out.println("切面逻辑");
//				method.invoke(target, args);
//				return null;
//			}
//		});
//
//		userInterface.test();
	}
}
